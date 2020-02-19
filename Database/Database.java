package de.hsf.mobcomgroup1.runourway.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateFormat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Database extends SQLiteOpenHelper implements DatabaseHelper{
    private static final String TAG = "Database";
    private static final int VERSION = 6;

    /*
    ------------------------------------------------------------------------------------------------
                                Datenbankschema wird definiert
    ------------------------------------------------------------------------------------------------
     */

    private static final String DATABASE_NAME = "RunOurWay.db";

    //Spalten für die Tabelle der einzelnen Läufe werden benannt
    private static final String TABLE_NAME_1 = "Runs";       //PK
    private static final String RUNS_COL_1 = "ID";
    private static final String RUNS_COL_2 = "Date";
    private static final String RUNS_COL_3 = "Distance";
    private static final String RUNS_COL_4 = "Time";
    private static final String RUNS_COL_5 = "Pace";
    private static final String RUNS_COL_6 = "Image";

    //String in SQL für die Erstellung der Runs-Tabelle wird definiert
    private static final String RUNS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_1 + " ("
            +RUNS_COL_1+" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +RUNS_COL_2+" INTEGER NOT NULL, "
            +RUNS_COL_3+" FLOAT(4,2) NOT NULL, "
            +RUNS_COL_4+" INTEGER NOT NULL, "
            +RUNS_COL_5+" INTEGER NOT NULL, "
            +RUNS_COL_6+" BLOB "+")";

    /*
    ------------------------------------------------------------------------------------------------
                   Erstellung oder Update des oben festgelegten Datenbankschemas
    ------------------------------------------------------------------------------------------------
     */

    //Konstruktor, Datenbank wird erstellt
    public Database(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        Log.d(TAG,"Database created");
    }

    //Die Tabellen werden erstellt und initial Daten für die Statistik eingefügt.
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG,"onCreate");
        db.execSQL(RUNS_TABLE_CREATE);
        insertRunWithDate(db,50,10,"13.11.18 15:50:05");
        insertRunWithDate(db,40,9,"17.11.18 15:50:05");
        insertRunWithDate(db,60,14,"20.11.18 15:50:05");
        insertRunWithDate(db,55,11,"23.11.18 15:50:05");
        insertRunWithDate(db,15,4,"25.11.18 15:50:05");
        insertRunWithDate(db,30,5,"02.12.18 15:50:05");
    }

    //Upgrade der Version. Wenn die Tabelle schon bestehen werden sie gelöscht und neu erstellt
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG,"onUpgrade");
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME_1);
        onCreate(db);
    }

     /*
    ------------------------------------------------------------------------------------------------
                    Methoden für das Einfügen von Daten in die beiden Tabellen
    ------------------------------------------------------------------------------------------------
     */

    /*
     * Statistiken für die einzelnen gesamten Läufe werden in die Tabelle eingefügt.
     * Übergabeparameter sind die Zeit in Millisekunden(int), die Distanz in km (float)
     * und ein Bild als Bitmap (Array aus bytes).
     * Gibt true zurück, wenn erfolgreich eingefügt wurde.
     */
    public boolean insertRun(int time, float distance, byte[] image){
        Log.d(TAG,"insertRun() mit bitmap");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(RUNS_COL_2, System.currentTimeMillis()/1000);
        contentValues.put(RUNS_COL_3, distance);
        contentValues.put(RUNS_COL_4, time);
        contentValues.put(RUNS_COL_5, getPace(time,distance));
        contentValues.put(RUNS_COL_6, image);
        long result = db.insert(TABLE_NAME_1,null,contentValues);
        if (result == -1){
            Log.d(TAG,"Einfügen in Runs fehlgeschlagen");
            return false;
        }else{
            Log.d(TAG,"Einfügen in Runs erfolgreich");
            return true;
        }
    }

    /*
     * Methode nur zum Füllen mit Daten in der Vergangenheit um Satistiken manipulieren zu
     * können. Statistiken werden zu presentationszwecken manipuliert, damit die Statistik
     * mit Dummy-Daten gefüllt ist. Die Methode ird im Betrieb nicht benötigt!
     * Übergabeparameter sind die Zeit in Minuten(int) und die Distanz in km (float)
     */
    public boolean insertRunWithDate( SQLiteDatabase db, int timeMin, float distance, String date){
        Log.d(TAG,"insertRunWithDate()");
        int time = timeMin*60*1000;
        long dateLong = dateToLong(date);
        ContentValues contentValues = new ContentValues();
        contentValues.put(RUNS_COL_2, dateLong);
        contentValues.put(RUNS_COL_3, distance);
        contentValues.put(RUNS_COL_4, time);
        contentValues.put(RUNS_COL_5, getPace(time,distance));
        long result = db.insert(TABLE_NAME_1,null,contentValues);
        if (result == -1){
            Log.d(TAG,"Einfügen in Runs fehlgeschlagen");
            return false;
        }else{
            Log.d(TAG,"Einfügen in Runs erfolgreich");
            return true;
        }
    }

     /*
    ------------------------------------------------------------------------------------------------
                           Methoden für die Ausgabe/Abfrage von Daten
    ------------------------------------------------------------------------------------------------
     */

    /*
     * Gibt alle in der Tabelle Runs gespeicherten Daten zurück.
     * Der Rückgabeparameter ist ein Cursor. Für die Darstellung bietet sich ein ListView an.
     */
    public Cursor getAllDataRuns(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM "+ TABLE_NAME_1;
        Cursor data = db.rawQuery(query,null);
        return data;
    }

    /*
     * Es werden das Datum und die gewünschten Werte der letzten übergebenen Anzahl an Tagen
     * aus der Datenbank ausgelesen. Anschließend werden die Werte in einer Hashmap gespeichert
     * und zurückgegeben.
     * Übergabeparamter:
     * -days: Anzahl der Tage, die ab dem Zeitpunkt der Abfrage zurückgeblickt werden
     * -mode: welche Werte zurückgegeben werden sollen. Möglich sind "time","distance" und "pace".
     * Rückgabeparameter:
     * -Hashmap mit dem Datum als Schlüssel(Long) und die geforderten Daten als zugeordnetem
     *  Wert(Float).
     */
    public Map<Long,Float> getBarChartaData(int days, String mode){
        Log.d(TAG,"getBarChartaData()");
        SQLiteDatabase db = this.getWritableDatabase();
        long dateAgo = getDateAgo(days);
        Log.d(TAG,"given date ago: "+dateAgo);
        String index="";
        switch (mode){
            case "distance":
                index=RUNS_COL_3;
                break;
            case "time":
                index=RUNS_COL_4;
                break;
            case "pace":
                index=RUNS_COL_5;
                break;
        }
        String query = "SELECT "+RUNS_COL_2+","+index+" FROM "+ TABLE_NAME_1+" WHERE "+dateAgo+" < "+RUNS_COL_2;
        Cursor data = db.rawQuery(query,null);

        Map<Long,Float> daysMap = new HashMap<>();

        if (data.getCount()>0) {
            data.moveToFirst();
            do{
                daysMap.put(data.getLong(0), data.getFloat(1));
            }while(data.moveToNext());
        }
        Map<Long,Float> sortedDaysMap = new TreeMap<>(daysMap);

        return sortedDaysMap;
    }

    /*
     * Es wird eine Map mit den Daten als Schlüssel(Long) und der dazugehörigen id als Wert(int)
     * für den übergebenen Zeitraum(Tage als Integerwert) zurückgegeben.
     */
    public Map<Long,Integer> getDateWithId(int days){
        Log.d(TAG,"getDateWithId()");
        SQLiteDatabase db = this.getWritableDatabase();
        long dateAgo = getDateAgo(days);
        String query = "SELECT "+RUNS_COL_1+","+RUNS_COL_2+" FROM "+ TABLE_NAME_1+" WHERE "+dateAgo+" < "+RUNS_COL_2;
        Cursor data = db.rawQuery(query,null);

        Map<Long,Integer> daysMap = new HashMap<>();
        if (data.getCount()>0) {
            data.moveToFirst();
            do{
                daysMap.put(data.getLong(1), data.getInt(0));
                Log.d(TAG, "Datum " + formatDateToString(data.getLong(1)));
                Log.d(TAG, "Id " + data.getInt(0));
                Log.d(TAG, "mapgröße " + daysMap.size());
            }while(data.moveToNext());
        }
        return daysMap;
    }

    /*
     * Es wird eine id(int) für einen Lauf übergeben und ein Cursor mit allen Daten zu diesem Lauf
     * zurückgegeben.
     */
    public Cursor getRunData(int id){
        Log.d(TAG,"getRunData()");
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM "+ TABLE_NAME_1+" WHERE "+id+" = "+RUNS_COL_1;
        Cursor data = db.rawQuery(query,null);
        return data;
    }


    //Maximal gelaufene Strecke wird aus der Datenbank ausgelesen und als String zurückgegeben
    public String getMaxDistance(){
        Log.d(TAG,"getMaxDistance()");
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT "+RUNS_COL_3+" FROM "+TABLE_NAME_1+" ORDER BY "+RUNS_COL_3+" DESC LIMIT 1";
        Cursor data = db.rawQuery(query,null);
        Log.d(TAG,"Query getMaxDistance ausgeführt");
        String result;
        if (data.getCount()>0){
            Log.d(TAG,"Werte in Cursor");
            data.moveToFirst();
            result  = data.getString(data.getColumnIndex(RUNS_COL_3));
        }else{
            result="0";
        }
        Log.d(TAG,"MaxDistance: "+result);
        return result;
    }

    //Die Gesamtstrecke der Läufe, die in der Datenbank gespeichert sind, wird zurückgegeben.
    public String getTotalDistance(){
        Log.d(TAG,"getTotalDistance()");
        float sum = 0;
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT SUM("+RUNS_COL_3+") FROM "+TABLE_NAME_1;
        Cursor data = db.rawQuery(query, null);
        if(data.moveToFirst()) {
            sum = data.getFloat(0);
        }
        String result = String.valueOf(sum);
        Log.d(TAG,"Total Distance: "+result);
        return result;
    }

    //Die Gesamtzeit aller gespeicherten Läufe wird zurückgegeben
    public String getTotalTime(){
        Log.d(TAG,"getTotalTime()");
        float sum = 0;
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT SUM("+RUNS_COL_4+") FROM "+TABLE_NAME_1;
        Cursor data = db.rawQuery(query, null);
        if(data.moveToFirst()) {
            sum = data.getFloat(0);
        }
        String result = String.valueOf(sum);
        Log.d(TAG,"Total Time: "+result);
        return result;
    }

    //Die Anzahl der in der Datenbank gelisteten Läufe wird zurückgegeben.
    public String getCountOfRuns(){
        Log.d(TAG,"getCountOfRuns()");
        int count = 0;
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT COUNT("+RUNS_COL_1+") FROM "+TABLE_NAME_1;
        Cursor data = db.rawQuery(query, null);
        if(data.moveToFirst()) {
            count = data.getInt(0);
        }
        String result = String.valueOf(count);
        Log.d(TAG,"Count of runs: "+result);
        return result;
    }

    /*
    ------------------------------------------------------------------------------------------------
                           Hilfsmethoden zum Dateneinfügen in die Datenbank
                                (keine öffentlichen Funktionen)
    ------------------------------------------------------------------------------------------------
     */

    /*
     * Formatiert das Datum von long(Datenbank) zu String(UI-Ausgabe)
     */
    public String formatDateToString(long milliseconds) {
        Log.d(TAG,"getDateFormatted()");
        String formattedDate = DateFormat.format("dd.MMM.yyyy", new Date(milliseconds*1000)).toString();
        Log.d(TAG,"formatted "+formattedDate);
        return formattedDate;
    }

    /*
     * Formatiert das Datum von String(eingabe) zu long(Datenbank)
     * -Nur zur manipulation der Statistiken benötigt
     */
    public long dateToLong(String dateString){
        Log.d(TAG,"dateToLong(): "+dateString);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        long dateLong=0;
        try {
            Date date = sdf.parse(dateString);
            dateLong = date.getTime()/1000;
        } catch (ParseException e){
            e.printStackTrace();
        }
        Log.d(TAG,"dateLong: "+dateLong);
        return dateLong;
    }

    /*
     * Berechnet das Datum um die Tage zurück, die übergeben werden(int).
     * Wird benötigt um die Statistiken für den übergebenen Zeitraum aus der Datenbank auszulesen
     * (es wird nach Zahlen gesucht, die größer als die zu diesem Zeitpunkt ist).
     */
    private long getDateAgo(int days){
        Log.d(TAG,"getDateTwoWeeksAgo()");
        Calendar now =Calendar.getInstance();
        now.add(Calendar.DAY_OF_YEAR,-days);
        long dateAgo = now.getTimeInMillis()/1000;
        Log.d(TAG,"two weeks ago: "+ formatDateToString(dateAgo)+ "long "+dateAgo);

        return dateAgo;
    }

    /*
     * Berechnet die Durchschnittliche Geschwindigkeit in Millisekunden
     */
    private int getPace(int time, float distance){
        Log.d(TAG,"getPace()");
        int pace = (int) (time/distance);
        Log.d(TAG,"Pace in Millisec: "+pace);
        return pace;
    }
}