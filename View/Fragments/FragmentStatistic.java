package de.hsf.mobcomgroup1.runourway.View.Fragments;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;

import de.hsf.mobcomgroup1.runourway.Database.BitmapUtils;
import de.hsf.mobcomgroup1.runourway.Database.Database;
import de.hsf.mobcomgroup1.runourway.R;
import de.hsf.mobcomgroup1.runourway.View.MainActivity;

public class FragmentStatistic extends Fragment {

    private static final String TAG = "FragmentStatistic";

    //Anlegen aller Elemente
    private Button navHomeBtn;
    private Button showTrackBtn;
    private Button showDurationBtn;
    private Button showSpeedBtn;
    private Button sharingButton;
    public Spinner spinner;
    public BarData data;
    public BarChart chart;
    public TableRow tableRowMaxTrack;
    public ImageView viewBitMap;
    public View viewChart;
    public LinearLayout scrollRunViewLayout;
    public ArrayList<Button> runBtns;
    public TextView averagePace;
    public TextView averageDistance;
    public TextView averageTime;
    public TextView longestRun;
    public TableRow tableRowLongestRun;
    public TextView textViewTableTrack;
    public TextView textViewTableTime;
    public LinearLayout chartControllBtns;

    private enum DisplayedStatistik {MeterProSekunde, Kilometer, Minuten}

    ArrayList<Float> barValue = new ArrayList<>();
    //Map zum abspeichern, der von der Datenbank erhaltenen Werte
    Map<Long, Float> dataValuesNew;
    //enum, um in initializeAverageValues() zwischen den drei Einheiten wechseln zu können.
    public enum Column {time, distance, pace}

    //Initialisierung der Datenbank!!
    Database database = new Database(MainActivity.activity);
    //int, für den Zeitraum der Datenbankabfrage
    public int weeks = 4;
    //Der String hält fest, welche Char aktiv ist.
    public String activeUnit = DisplayedStatistik.Kilometer.toString();

    //Die folgenden Werte werden in initializeAverageValues() verwendet;
    public Float maxDistance = Float.valueOf(0);
    public Float[] statValue = new Float[3];
    public int i = 0;
    public int count = 0;
    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Verbinden des layouts mit den Elementen
        view = inflater.inflate(R.layout.statistic_fragment_layout, container, false);
        sharingButton = view.findViewById(R.id.shareBtn);
        spinner = (Spinner) view.findViewById(R.id.statSpinner);
        showTrackBtn = (Button) view.findViewById(R.id.showTrack);
        showDurationBtn = (Button) view.findViewById(R.id.showDuration);
        showSpeedBtn = (Button) view.findViewById(R.id.showSpeed);
        chart = (BarChart) view.findViewById(R.id.barchart);
        navHomeBtn = (Button) view.findViewById(R.id.navHomeBtn);
        tableRowMaxTrack = (TableRow) view.findViewById(R.id.tableRowMaxTrack);
        scrollRunViewLayout = (LinearLayout) view.findViewById(R.id.scrollRunViewLayout);
        viewBitMap = (ImageView) view.findViewById(R.id.viewBitMap);
        viewChart = (View) view.findViewById(R.id.viewChart);
        averagePace = (TextView) view.findViewById(R.id.statAveragePace);
        averageDistance = (TextView) view.findViewById(R.id.statAverageDistance);
        averageTime = (TextView) view.findViewById(R.id.statAverageTime);
        longestRun = (TextView) view.findViewById(R.id.statLongestRun);
        tableRowLongestRun = (TableRow) view.findViewById(R.id.tableRowLongestRun);
        textViewTableTrack = (TextView) view.findViewById(R.id.textViewTableTrack);
        textViewTableTime = (TextView) view.findViewById(R.id.textViewTableTime);
        chartControllBtns = (LinearLayout) view.findViewById(R.id.chartControllBtns);
        //Xml Buttons werden in Java erstellt, um eine Liste aus den Läufen zu erstellen

        createRunBtnList();
        //Button-Clicklistener zum verlassen der Statistik
        navHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setViewPager(0);
            }
        });

        //Standart Abfrage der Datenbank, damit beim Besuchen der Statistik Daten vorhanden sind.
        initializeValuesFromDatabank(activeUnit, weeks * 7);
        initializeAverageValues(weeks * 7);

        //Button-ClickListener um die chart auf zurückgelegte Strecken umzustellen.
        showTrackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Festlegung der angezeigten Einheit für weitere Verwendung von initializeValuesFromDatabank()
                activeUnit = DisplayedStatistik.Kilometer.toString();
                //Aktualisierung der Chart (siehe initializeValuesFromDatabank())
                initializeValuesFromDatabank(activeUnit, weeks * 7);
                chart.setDescription(DisplayedStatistik.Kilometer.toString());
            }
        });
        showDurationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Festlegung der angezeigten Einheit für weitere Verwendung von initializeValuesFromDatabank()
                activeUnit = DisplayedStatistik.Minuten.toString();
                //Aktualisierung der Chart (siehe initializeValuesFromDatabank())
                initializeValuesFromDatabank(activeUnit, weeks * 7);
                chart.setDescription(DisplayedStatistik.Minuten.toString());
            }
        });
        showSpeedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Festlegung der angezeigten Einheit für weitere Verwendung von initializeValuesFromDatabank()
                activeUnit = DisplayedStatistik.MeterProSekunde.toString();
                //Aktualisierung der Chart (siehe initializeValuesFromDatabank())
                initializeValuesFromDatabank(activeUnit, weeks * 7);
                chart.setDescription(DisplayedStatistik.MeterProSekunde.toString());
            }
        });

        //Initialisierung des ClickListeners für den Share Button
        sharingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //share Funktion. Siehe shareIt()
                shareIt();
            }
        });
        //Initialisierung des ClickListeners für den Zeitraum-Spinners
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // Durch die if Abfrage wird geklärt, welches Element im Spinner ausgewählt wurde.
                // Danach wird die Variable weeks auf die ausgewählte Einheit angepasst und
                // die Chart sowie die Durchschnittswerte werden auf den Zeitraum aktualisiert.
                if (parent.getItemAtPosition(position).toString().equals("1 Woche")) {
                    weeks = 1;
                    initializeValuesFromDatabank(activeUnit, weeks * 7);
                    initializeAverageValues(weeks * 7);
                } else if (parent.getItemAtPosition(position).toString().equals("4 Wochen")) {
                    weeks = 4;
                    initializeValuesFromDatabank(activeUnit, weeks * 7);
                    initializeAverageValues(weeks * 7);
                } else if (parent.getItemAtPosition(position).toString().equals("2 Monate")) {
                    weeks = 8;
                    initializeValuesFromDatabank(activeUnit, weeks * 7);
                    initializeAverageValues(weeks * 7);
                } else if (parent.getItemAtPosition(position).toString().equals("6 Monate")) {
                    weeks = 4*6;
                    initializeValuesFromDatabank(activeUnit, weeks * 7);
                    initializeAverageValues(weeks * 7);
                } else if (parent.getItemAtPosition(position).toString().equals("1 Jahr")) {
                    weeks = 4*12;
                    initializeValuesFromDatabank(activeUnit, weeks * 7);
                    initializeAverageValues(weeks * 7);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        return view;
    }

    public void deleteRunBtnList() {
        if(scrollRunViewLayout.getChildCount() > 0)
            scrollRunViewLayout.removeAllViews();
    }

    private String mapEnumToDataBaseColumn(String statisticString){
        String result;
        if(statisticString.equals(DisplayedStatistik.Kilometer.toString())){
            result = "distance";
        }else if(statisticString.equals(DisplayedStatistik.MeterProSekunde.toString())){
            result = "pace";
        }else if(statisticString.equals(DisplayedStatistik.Minuten.toString())){
            result = "time";
        }
        else{
            throw new IllegalArgumentException(statisticString + " is not an accepted input.");
        }
        return result;
    }

    public void createRunBtnList() {
        runBtns = new ArrayList<>();

        Button fullStatisticBtn = new Button(getActivity());

        runBtns.add(fullStatisticBtn);
        fullStatisticBtn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        fullStatisticBtn.setText("Gesamt");
        fullStatisticBtn.setBackgroundColor(0xffe2d9b4);
        fullStatisticBtn.getLayoutParams().height= ViewGroup.LayoutParams.MATCH_PARENT;

        //Initialisierung des ClickListeners des "Gesamt" Buttons.
        fullStatisticBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // viewBitMap.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,160));

                //Die unsichtbar gestellten Elemente der Statistik werden wieder sichtbar.
                chartControllBtns.setVisibility(View.VISIBLE);
                chart.setVisibility(View.VISIBLE);
                tableRowLongestRun.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.VISIBLE);
                viewBitMap.setVisibility(View.GONE);
                viewChart.setVisibility(View.VISIBLE);

                for (Button r : runBtns) {
                    r.setBackgroundColor(0xffdfd098);
                }
                view.setBackgroundColor(0xffe2d9b4);
                //averagePace.setText("1");
                textViewTableTrack.setText(getResources().getString(R.string.tableRowTitle2));
                textViewTableTime.setText(getResources().getString(R.string.tableRowTitle3));
                initializeAverageValues(7*weeks);
            }
        });

        scrollRunViewLayout.addView(fullStatisticBtn);
        dataValuesNew = database.getBarChartaData(200, "time");
        final Map<Long,Integer> map= database.getDateWithId(200);

        for (final Long key : dataValuesNew.keySet()) {

            Button runBtn = new Button(getActivity());
            runBtns.add(runBtn);
            runBtn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            runBtn.setText(database.formatDateToString(key).substring(0,6));
            runBtn.setBackgroundColor(0xffdfd098);
            runBtn.getLayoutParams().height= ViewGroup.LayoutParams.MATCH_PARENT;

            scrollRunViewLayout.addView(runBtn);
            runBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int id = map.get(key);

                    Cursor run = database.getRunData(id);

                    run.moveToFirst();
                    float distance = run.getFloat(2);
                    float time = run.getFloat(3);
                    float pace = run.getFloat(4);
                    byte[] image = run.getBlob(5);

                    //Die Durchschnittswerte werden durch Werte des einzelnen Laufs ersetzt, da es keine
                    //Chart für diese Einzelwerte gibt.
                    //Durch 1000000/ wird der Wert von Millisekunden pro Kilometer in Meter pro Sekunde
                    //umgeformt.
                    averagePace.setText(formateFloatToString(1000000/ pace)+" m/s");
                    //Durch /60000 wird der Wert von Millisekunden in Minuten umgerechnet.
                    averageTime.setText(formateFloatToString(time / 60000)+" min");
                    averageDistance.setText(formateFloatToString(distance)+" km");
                    if(image != null){
                        setByteArrayInImageView(image);
                    }

                    textViewTableTrack.setText(getResources().getString(R.string.tableRowTitle5));
                    textViewTableTime.setText(getResources().getString(R.string.tableRowTitle6));
                    //Elemente der Statistik werden ausgeschaltet, damit Platz für die Karte eines einzelnen Laufs
                    //geschaffen wird.
                    chart.setVisibility(View.GONE);
                    tableRowLongestRun.setVisibility(View.GONE);
                    spinner.setVisibility(View.GONE);
                    chartControllBtns.setVisibility(View.GONE);
                  //  getRunView();
                    //setByteArrayInImageView(c.getBlob(5));
                    viewBitMap.setVisibility(View.VISIBLE);
                    viewChart.setVisibility(View.GONE);
                    //Zeigt welcher Lauf angezeigt wird
                    for (Button r : runBtns) {
                        r.setBackgroundColor(0xffdfd098);
                    }
                    view.setBackgroundColor(0xffe2d9b4);
                }
            });
        }
    }

    private void setByteArrayInImageView(byte[] byteArray){
        Bitmap bitmap = BitmapUtils.getImage(byteArray);
        int height = viewBitMap.getHeight();
        int width = viewBitMap.getWidth();
        viewBitMap.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 1000,
                1000, false));
    }

    //Funktion zum erzeugen der Durchschnittswerte. int days bestimmt, wie weit die Abfrage
    //der Datenbank in die Vergangenheit geht.
    private void initializeAverageValues(int days) {
        //die Variable i wird zurückgesetzt. i wird benutzt, um die Zeit, Strecke und Geschwindigkeit
        //in unterschiedlichen Positionen vom Array statValue[] zu speichern.
        i = 0;
        // Die Variable zum speichern des längsten Laufs im Zeitparameter "days" wird hier zurückgesetzt.
        maxDistance = Float.valueOf(0);
        //Die for-Schleife iteriert durch die Einheiten "Zeit","Strecke" und "Geschwindigkeit"
        for (Column keyS : Column.values()) {
            //Resetten der Variable count;
            count = 0;
            //Resetten des in statValue[i] gespeicherten Werts.
            statValue[i] = Float.valueOf(0);
            //Anfrage an die Datenbank. Es wird eine Map zurückgegeben.
            dataValuesNew = database.getBarChartaData(days, keyS.toString());
            //Die for-Schleife iteriert durch alle von der Datenbank erhaltenen Läufe.
            //die Strecke/Zeit/Geschwindikeit der Läufe wird addiert und durch die Anzahl der Läufe
            //genommen, um einen Durchschnitt zu ermitteln.
            for (Long key : dataValuesNew.keySet()) {
                if (keyS.toString()=="distance") {
                    statValue[i] +=dataValuesNew.get(key);
                    //der weiteste Lauf wird ermittelt.
                    if (maxDistance < dataValuesNew.get(key)) {
                        maxDistance = dataValuesNew.get(key);
                    }
                } else if(keyS.toString() == "time"){
                    //Durch /60000 wird der Wert von Millisekunden in Minuten umgerechnet.
                    statValue[i] +=dataValuesNew.get(key)/60000;
                } else {
                    //Durch 1000000/ wird der Wert von Millisekunden pro Kilometer in Meter pro Sekunde
                    //umgeformt.
                    statValue[i] +=1000000/dataValuesNew.get(key);
                }
                //Count wird erhöht.
                count++;
            }
            //Der Durchschnittswert aller Läufe wird hier errechnet und abgespeichert.
            statValue[i] = statValue[i] / count;
            //i wird erhöht, um zur nächsten Einheit im enum Column zu gelangen.
            i++;
        }
        //Die Elemente im Layout werden nun mit den Daten aus dem Array gefüllt.
        averagePace.setText(formateFloatToString(statValue[2])+" m/s");
        averageDistance.setText(formateFloatToString(statValue[1])+" km");
        averageTime.setText(formateFloatToString(statValue[0])+" min");
        longestRun.setText(maxDistance + "km");
    }

    private String formateFloatToString(Float f) {
        String result = f.toString();
        return result.format ("%.2f", f);

    }
    //initializeValuesFromDatabank() bekommt mit "unit" und "days" die Werte aus der Datenbank und
    //bindet sie in die chart ein.
    private void initializeValuesFromDatabank(String unit, int days) {
        //Eine ArrayList aus Strings wird erstellt, um alle Balken im Chart mit einem Datum beschriften zu können.
        ArrayList<String> xAxis = new ArrayList<>();
        //barValue wird vor Benutzung geleert.
        barValue.clear();
        //Anfrage an die Datenbank. Es wird eine Map zurückgegeben.
        dataValuesNew = database.getBarChartaData(days, mapEnumToDataBaseColumn(unit));
        // Die for-Schleife läuft durch alle von der Datenbank erhaltenen Läufe
        for (Long key : dataValuesNew.keySet()) {
            //Die if-Abfrage unterscheidet zwischen Strecke, Zeit und Geschwindigkeit.
            if (activeUnit.equals(DisplayedStatistik.Kilometer.toString())) {
                //Die Strecke wird in Kilometer abgespeichert. Es bedarf also keiner Umrechnung wie bei "pace" oder "time"
                barValue.add(dataValuesNew.get(key));
            } else if(activeUnit.equals(DisplayedStatistik.Minuten.toString())){
                //Durch /60000 wird der Wert von Millisekunden in Minuten umgerechnet.
                float milliseconds = dataValuesNew.get(key);
                int totalSeconds = (int) milliseconds / 1000;
                float minutes = (float)Math.ceil(totalSeconds / 60.0);
                barValue.add(minutes);
            } else if(activeUnit.equals(DisplayedStatistik.MeterProSekunde.toString())){
                //Durch 1000000/ wird der Wert von Millisekunden pro Kilometer in Meter pro Sekunde
                //umgeformt.
                barValue.add((1000000/dataValuesNew.get(key)));
            }
            //Das Datum des Laufs wird in der ArrayList eingespeichert.
            xAxis.add(database.formatDateToString(key).substring(0,6));
        }
        //Aus der ArrayList mit den Werten und der ArrayList mit den Daten(Mehrzahl von Datum???)
        //wird ein Datensatz für die chart erstellt.
        data = new BarData(xAxis, getDataSet(barValue, unit));
        //Der Datensatz wird in die chart eingebunden.
        chart.setData(data);
        //Anstatt die Balken im Diagramm aufploppen zu lassen, bauen sie sich durch .animateXY() langsam auf.
        chart.animateXY(1000, 1000);
        chart.fitScreen();
        //die Chart wird gezeichnet.
        chart.invalidate();
        //Die chart kann mit diesem Befehl nicht mehr durch doppeltes Drücken auf die Chart zoomen.
        chart.setDoubleTapToZoomEnabled(false);
        //Initialisierung des ClickListeners für jeden Balken im Chart
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener()
        {
            //Nicht alle Beschriftungen von jedem Lauf passen in die X-Achse. Deshalb kann der User auf
            //den Balken drücken und so das genaue Datum des Laufes durch einen Toast in Erfahrung bringen.
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                //Abfrage verhindert eine NullpointerException, da auf die chart drücken, wenn kein Balken
                // generiert wurde zum Absturz der App führt.
                if (e != null) {
                    //Das Datum des ausgewählten Balkens wird angezeigt.
                    Toast.makeText(getActivity(), chart.getXValue(e.getXIndex()), Toast.LENGTH_LONG).show(); // For example
                }
            }
            @Override
            public void onNothingSelected() { }
        });
    }
    //getDataSet() nimmt die ArrayList mit Floats und wandelt sie in eine ArrayList mit BarDataSets um,
    //damit die Chart damit arbeiten kann.
    private ArrayList<BarDataSet> getDataSet(ArrayList<Float> dataValues, String unit) {
        ArrayList<BarDataSet> dataSets = null;
        //Abfangen einer NullPointerException
        if (dataValues != null) {

            ArrayList<BarEntry> valueSet1 = new ArrayList<>();
            //Alle Läufe werden durchlaufen.
            for (int i = 0; i < dataValues.size(); i++) {
                valueSet1.add(new BarEntry(dataValues.get(i), i));
            }

            BarDataSet barDataSet1 = new BarDataSet(valueSet1, unit);
            barDataSet1.setColor(Color.rgb(37, 151, 204));

            dataSets = new ArrayList<>();
            dataSets.add(barDataSet1);
        }
        //Eine ArrayList aus BarDataSets wird zurückgegeben.
        return dataSets;
    }

    private File takeScreenshot() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        File imageFile = null;
        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/screenshot.jpg";

            // create bitmap screen capture
            View v1 = view;
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            //openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
        return imageFile;
    }

    private void shareIt() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("image/jpg");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
        sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(takeScreenshot()));
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
}