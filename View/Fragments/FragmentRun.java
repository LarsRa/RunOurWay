package de.hsf.mobcomgroup1.runourway.View.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.hsf.mobcomgroup1.runourway.Database.BitmapUtils;
import de.hsf.mobcomgroup1.runourway.Database.Database;
import de.hsf.mobcomgroup1.runourway.Database.Screenshot;
import de.hsf.mobcomgroup1.runourway.GenerationPackage.RouteGeneration.Route;
import de.hsf.mobcomgroup1.runourway.View.MenuClickListener;
import de.hsf.mobcomgroup1.runourway.Navigation.NavigationListener;
import de.hsf.mobcomgroup1.runourway.Navigation.RouteRecordEvaluator;
import de.hsf.mobcomgroup1.runourway.Navigation.Statistic.NavigatorStatistician;
import de.hsf.mobcomgroup1.runourway.View.OnBackPressed;
import de.hsf.mobcomgroup1.runourway.HelpfulClasses.Permission;
import de.hsf.mobcomgroup1.runourway.R;
import de.hsf.mobcomgroup1.runourway.View.AlertBox;
import de.hsf.mobcomgroup1.runourway.View.MainActivity;
import de.hsf.mobcomgroup1.runourway.View.SecondActivity;

public class FragmentRun extends Fragment
        implements NavigationListener, LocationListener, OnBackPressed, MenuClickListener {

    public static int timerCounter;

    Marker myPositionMarker;
    // ArrayList<GeoPoint> myPositionList = new ArrayList<>();
    GeoPoint lastPoint;
    ArrayList<Polyline> myLines = new ArrayList<>();

    private final int MIN_MARKER_DISTANCE_IN_METER = 50;

    Drawable[] nextMarkers = {gr(R.drawable.t_1),gr(R.drawable.t_2), gr(R.drawable.t_3)} ;

    private Drawable gr(int id){
        return SecondActivity.activity.getResources().getDrawable(id);
    }

    List<Marker> wayMarker;

    MapView mMapView;
    LocationManager locationManager;
    LocationListener locationListener;

    private NavigatorStatistician statistician;

    private Button startPauseBtn;
    private Button stopBtn;
    boolean run = true;

    private final int REQUEST_FREQUENCY = 3000;

    /**
     * milliseconds since 1970 -> changes value when run ends
     */
    private long pauseStarted;

    /**
     * milliseconds since 1970 -> changes value when run ends
     */
    private long pauseEnded;

    View view;
    Handler handler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.run_fragment_layout, container, false);
        statistician = new RouteRecordEvaluator(getContext());

        startPauseBtn = (Button) view.findViewById(R.id.startPauseBtn);
        stopBtn = (Button) view.findViewById(R.id.stopBtn);

        //Map ansprechen
        mMapView = FragmentMap.mMapView;

        locationManager = (LocationManager) MainActivity.activity.getSystemService(Context.LOCATION_SERVICE);
        locationListener = this;

        startPauseBtn.setOnClickListener(new View.OnClickListener() {
            /**
             * permission is nor ignored, the compiler is just too stupid to detect usage
             * @param v
             */
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {

                if (run) {
                    startPauseBtn.setText("Start");
                    locationManager.removeUpdates(locationListener);
                    run = false;
                    stopTimer();
                } else {
                    if(Permission.askForPermissions(getActivity())) {
                        startPauseBtn.setText("Pause");
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, REQUEST_FREQUENCY, 1, locationListener);
                        run = true;
                        startTimer();
                    }
                }
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sure();
            }
        });

        return view;
    }

    public void sure(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        stopTimer();

                        statistician.endRun();
                        //((SecondActivity) getActivity()).finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        String message = "Sind sie sicher, dass Sie den Lauf beenden wollen?";
        AlertBox.showAlertBox(dialogClickListener,getActivity(),message, "Ja","Nein");
    }

    /**
     * permission is not really missing, the compiler just doesnt see, that the permission
     * was checked already
     * @param r
     */
    @SuppressLint("MissingPermission")
    public void startRun(Road r) {
        if(Permission.askForPermissions(getActivity())) {

            wayMarker = new ArrayList<>();
            for(Drawable d : nextMarkers){
                Marker marker = new Marker(mMapView);
                marker.setIcon(d);
                wayMarker.add(marker);
                mMapView.getOverlays().add(marker);
            }

            getStatistician().startRun(new Route(r));
            FragmentMap.setMapActor(null);
            startTimer();

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, REQUEST_FREQUENCY, 1, this);

            getStatistician().addNavigationListener(this);

            ((SecondActivity) getActivity()).menu.getItem(0).setEnabled(false);
            ((SecondActivity) getActivity()).menu.getItem(1).setEnabled(false);
            ((SecondActivity) getActivity()).menu.getItem(2).setEnabled(false);
            ((SecondActivity) getActivity()).menu.getItem(3).setEnabled(false);
            timerCounter = 0;

            SecondActivity.activity.setOnBackPressed(this);
            SecondActivity.activity.setMenuListener(this);

            displayNextCheckpoints();
        }
    }

    public void startTimer() {
        handler = new Handler();
        handler.postDelayed(runnable, 1000);
    }

    public void stopTimer() {
        handler.removeCallbacks(runnable);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            timerCounter++;
            messageHandler();
            handler.postDelayed(this, 1000);
        }
    };

    public void messageHandler() {
        TextView tv = view.findViewById(R.id.runtime);

        final double scale3600 = 1.0 / 3600;
        final double scale60 = 1.0 / 60;
        int hh = (int) (timerCounter * scale3600);
        int mm = (int) (timerCounter * scale60);
        int ss = timerCounter - mm * 60 - hh * 3600;

        DecimalFormat format = new DecimalFormat("00");

        tv.setText(
                format.format(hh)
                        + ":" + format.format(mm)
                        + ":" + format.format(ss));


    }

    @Override
    public void pointAdded(GeoPoint point) {

        mMapView.getOverlays().remove(myPositionMarker);

        myPositionMarker = new Marker(mMapView);
        myPositionMarker.setPosition(point);
        myPositionMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        myPositionMarker.setIcon(getResources().getDrawable(R.drawable.myposition));
        mMapView.getOverlays().add(myPositionMarker);
        mMapView.invalidate();
        if (lastPoint != null) {
            ArrayList<GeoPoint> myPositionList = new ArrayList<>();
            myPositionList.add(point);
            myPositionList.add(lastPoint);

            Polyline newPolyline = new Polyline(mMapView);
            newPolyline.setPoints(myPositionList);
            newPolyline.setColor(Color.GREEN);
            mMapView.getOverlays().add(newPolyline);
            mMapView.invalidate();

            myLines.add(newPolyline);
        }
        lastPoint = point;
    }

    private void displayNextCheckpoints(){
        List<GeoPoint> nextCheckPoints =
                statistician.getNextCheckPoints(wayMarker.size(), MIN_MARKER_DISTANCE_IN_METER);

        for(int i = 0; i< wayMarker.size(); i++){
            if(nextCheckPoints.size() > i){
                wayMarker.get(i).setPosition(nextCheckPoints.get(i));
            }else{
                mMapView.getOverlays().remove(wayMarker.get(i));
            }
        }
        mMapView.invalidate();

    }

    @Override
    public void checkPointReached(GeoPoint point) {
        Log.d(getClass().getSimpleName(),"Checkpoint reached");
        displayNextCheckpoints();
    }

    @Override
    public void runEnd() {
        Database db = new Database(MainActivity.activity);

        Screenshot screenshot =  new Screenshot();
        Bitmap bitmap = screenshot.takeScreenshot(mMapView);
        BitmapUtils bitmapUtils = new BitmapUtils();
        byte[] getBytes = bitmapUtils.getBytes(bitmap);
        db.insertRun(timerCounter * 1000, statistician.getDistanceInKm(), getBytes);

        stopTimer();

        ((SecondActivity) getActivity()).menu.getItem(0).setEnabled(true);
        ((SecondActivity) getActivity()).menu.getItem(1).setEnabled(true);
        ((SecondActivity) getActivity()).menu.getItem(2).setEnabled(true);
        ((SecondActivity) getActivity()).menu.getItem(3).setEnabled(true);

        Log.d(getClass().getSimpleName(),"Run Over");

        SecondActivity.activity.setOnBackPressed(null);

        locationManager.removeUpdates(locationListener);
        SecondActivity.activity.finish();
        MainActivity.activity.setViewPager(1);

        FragmentStatistic reloadRunBtnList = (FragmentStatistic) MainActivity.activity.mSectionStatePagerAdapter.getItem(1);
        reloadRunBtnList.deleteRunBtnList();
        reloadRunBtnList.createRunBtnList();
    }

    @Override
    public void onLocationChanged(Location location) {
        statistician.locationChanged(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public NavigatorStatistician getStatistician() {
        return statistician;
    }


    @Override
    public void handleOnBackPressed() {
        sure();
    }

    @Override
    public void menuItemClickedSingleCallback(MenuItem item) {
        if (myLines != null) {
            for (Polyline p : myLines) {
                mMapView.getOverlays().remove(p);
            }
            myLines.clear();
        }

    }
}