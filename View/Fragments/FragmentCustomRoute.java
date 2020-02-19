package de.hsf.mobcomgroup1.runourway.View.Fragments;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

import de.hsf.mobcomgroup1.runourway.View.MapInteraction;
import de.hsf.mobcomgroup1.runourway.R;
import de.hsf.mobcomgroup1.runourway.View.SecondActivity;

public class FragmentCustomRoute extends Fragment implements MapInteraction {

    ArrayList<GeoPoint> geopointList = new ArrayList<>();
    ArrayList<Marker> markerList = new ArrayList<>();
    ArrayList<Polyline> polylineList = new ArrayList<>();

    MapView mMapView;
    View view;
    RoadManager roadManager;
    Road road;

    private Button deleteBtn;
    private Button navRunBtn;
    private Button navGenBtn;
    private Button helpBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_click_fragment_layout, container, false);

        FragmentMap.setActorIfActorIsNull(SecondActivity.activity.getFragmentWithMapInteraction(FragmentCustomRoute.class));

        deleteBtn = (Button) view.findViewById(R.id.deleteBtn);
        navRunBtn = (Button) view.findViewById(R.id.goBtn);
        navGenBtn = (Button) view.findViewById(R.id.navGenBtn);
        helpBtn = (Button) view.findViewById(R.id.helpBtn);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeLastPath();
            }
        });
        navRunBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(geopointList != null && geopointList.size() > 1){

                    showOnlyStartEndFlags();

                    FragmentRun fragmentRun = SecondActivity.activity.getFragment(FragmentRun.class);
                    ((SecondActivity) getActivity()).setViewPagerRun(2);
                    fragmentRun.startRun(road);
                }

            }
        });
        navGenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeOldPath();
                FragmentMap.setMapActor(SecondActivity.activity.getFragmentWithMapInteraction(FragmentGeneration.class));
                ((SecondActivity) getActivity()).setViewPagerRun(0);
            }
        });
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Klicke auf die Map um Marker zu setzen\n" +
                        "Es wird eine Route ab 2 Marker erstellt\n" +
                        "Loeschen loescht den letzten Schritt.";
                final Snackbar snackbar = Snackbar.make(FragmentGeneration.view, message, 10000);
                snackbar.setAction("X", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                });

                View snackbarView = snackbar.getView();
                TextView textView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setMaxLines(3);
                snackbar.show();
            }
        });

        // muss rein wegen osm bonus pack
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        roadManager = new MapQuestRoadManager("ZnexF76JjHuoqAxV84B0LDzigeN04DJq");
        roadManager.addRequestOption("routeType=bicycle");

        mMapView = FragmentMap.mMapView;

        return view;
    }

    private void removeLastPath() {

        if ( geopointList != null && geopointList.size() != 0 ) {

            GeoPoint toRemoveGeopoint = geopointList.get(geopointList.size() - 1);
            Marker toRemoveMarker = markerList.get(markerList.size() - 1);
            Polyline toRemovePolyline = polylineList.get(polylineList.size() - 1);

            mMapView.getOverlays().remove(toRemoveGeopoint);
            mMapView.getOverlays().remove(toRemoveMarker);
            mMapView.getOverlays().remove(toRemovePolyline);

            geopointList.remove(toRemoveGeopoint);
            markerList.remove(toRemoveMarker);
            polylineList.remove(toRemovePolyline);

            mMapView.invalidate();
        }
    }
    public void removeOldPath() {

        if (geopointList != null) {
            geopointList.clear();
        }
        if (markerList != null) {
            for (Marker m : markerList) {
                mMapView.getOverlays().remove(m);
            }
            markerList.clear();
        }
        if (polylineList != null) {
            for (Polyline p : polylineList) {
                mMapView.getOverlays().remove(p);
            }
            polylineList.clear();
        }
        mMapView.invalidate();
    }

    @Override
    public void shortClick(GeoPoint point) {

        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(20);
        canvas.drawText("1000000", 10, 25, paint);

        Marker newMarker = new Marker(mMapView);

        newMarker.setPosition(point);
        newMarker.setIcon(getResources().getDrawable(R.drawable.standart));
        //newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        newMarker.draw(canvas, mMapView, true);


        mMapView.getOverlays().add(newMarker);
        markerList.add(newMarker);

        geopointList.add(point);

        Road newRoad = roadManager.getRoad(geopointList);
        Polyline newPolyline = RoadManager.buildRoadOverlay(newRoad);
        newPolyline.setColor(Color.RED);
        mMapView.getOverlays().add(newPolyline);
        mMapView.invalidate();

        polylineList.add(newPolyline);
        road = newRoad;
    }

    @Override
    public void longClick(GeoPoint point) {

    }

    public void showOnlyStartEndFlags() {
        markerList.get(0).setIcon(getResources().getDrawable(R.drawable.start));
        int i;
        for (i = 1; i < geopointList.size() - 1; i++) {
            mMapView.getOverlays().remove(markerList.get(i));

        }
        markerList.get(i).setIcon(getResources().getDrawable(R.drawable.finish));
        mMapView.invalidate();
    }

}
