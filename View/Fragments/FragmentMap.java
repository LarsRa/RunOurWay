package de.hsf.mobcomgroup1.runourway.View.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;

import de.hsf.mobcomgroup1.runourway.HelpfulClasses.Permission;
import de.hsf.mobcomgroup1.runourway.View.MapInteraction;
import de.hsf.mobcomgroup1.runourway.R;
import de.hsf.mobcomgroup1.runourway.View.MainActivity;
import de.hsf.mobcomgroup1.runourway.View.SecondActivity;

public class FragmentMap extends Fragment implements LocationListener {
    //Tag
    private static final String TAG = "FragmentMap";

    public static MapView mMapView;
    private MapController mMapController;
    private View view;
    private RoadManager roadManager;

    private LocationManager locationManager;

    private MapInteraction actor;

    public static void setMapActor(MapInteraction actor){
        FragmentMap map = SecondActivity.activity.getFragment(FragmentMap.class);
        map.setActor(actor);
    }

    public static void setActorIfActorIsNull(MapInteraction actor){
        FragmentMap map = SecondActivity.activity.getFragment(FragmentMap.class);
        if(map.actor == null){
            map.setActor(actor);
        }
    }

    public void setActor(MapInteraction actor){
        this.actor = actor;
    }

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.map_fragment_layout, container, false);

        // muss rein wegen osm bonus pack
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        roadManager = new MapQuestRoadManager("ZnexF76JjHuoqAxV84B0LDzigeN04DJq");
        roadManager.addRequestOption("routeType=bicycle");

        //Map ansprechen
        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);
        mMapController = (MapController) mMapView.getController();
        mMapController.setZoom(4);
        initializeMapViewEvent();
        mMapView.invalidate();

        locationManager = (LocationManager) MainActivity.activity.getSystemService(Context.LOCATION_SERVICE);
        if(Permission.askForPermissions(getActivity())) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, this);
        }
        return view;
    }

    private void initializeMapViewEvent(){
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public synchronized boolean singleTapConfirmedHelper(GeoPoint p) {
                if(actor != null){
                    actor.shortClick(p);
                }
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                if(actor != null){
                    actor.longClick(p);
                }
                return false;
            }
        };
        MapEventsOverlay overlayEvents = new MapEventsOverlay(MainActivity.activity.getBaseContext(), mReceive);
        mMapView.getOverlays().add(overlayEvents);
    }

    @Override
    public void onLocationChanged(Location location) {
        mMapController.setCenter(new GeoPoint(location.getLatitude(),location.getLongitude()));
        mMapController.setZoom(16);
        locationManager.removeUpdates(this);
        mMapView.invalidate();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

