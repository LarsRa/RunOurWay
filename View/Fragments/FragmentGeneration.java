package de.hsf.mobcomgroup1.runourway.View.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

import de.hsf.mobcomgroup1.runourway.GenerationPackage.RouteGeneration.Exceptions.RouteGenerationFailedException;
import de.hsf.mobcomgroup1.runourway.GenerationPackage.RouteGeneration.Exceptions.RouteTooLongException;
import de.hsf.mobcomgroup1.runourway.GenerationPackage.RouteGeneration.Route;
import de.hsf.mobcomgroup1.runourway.GenerationPackage.RouteGeneration.RouteGeneration;
import de.hsf.mobcomgroup1.runourway.GenerationPackage.RouteGeneration.RouteGenerator;
import de.hsf.mobcomgroup1.runourway.View.MapInteraction;
import de.hsf.mobcomgroup1.runourway.R;
import de.hsf.mobcomgroup1.runourway.View.SecondActivity;

public class FragmentGeneration extends Fragment implements MapInteraction {

    /**
     * start the route generation from this point
     */
    private GeoPoint generationPoint;

    ArrayList<Marker> markerlist = new ArrayList<>();
    ArrayList<Polyline> polylinelist = new ArrayList<>();

    MapView mMapView;
    public static View view;
    RoadManager roadManager;
    Road road;

    private Button regenerateRoute;
    private Button navRunBtn;
    private Button navCusBtn;
    private Button helpBtn;
    EditText distanceView;
    SeekBar varianzView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.generator_fragment_layout, container, false);

        FragmentMap.setActorIfActorIsNull(SecondActivity.activity.getFragmentWithMapInteraction(FragmentGeneration.class));

        navRunBtn = (Button) view.findViewById(R.id.goBtn);
        navCusBtn = (Button) view.findViewById(R.id.navCusBtn);
        helpBtn = (Button) view.findViewById(R.id.helpBtn);
        regenerateRoute = (Button) view.findViewById(R.id.btnGenerateNew);

        distanceView = view.findViewById(R.id.distance);
        varianzView = view.findViewById(R.id.varianz);
        varianzView.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub
                TextView tv = view.findViewById(R.id.varianzValue);
                tv.setText((progress * 100) + " m");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        regenerateRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(generationPoint != null){
                    shortClick(generationPoint);
                }
            }
        });

        navRunBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(markerlist != null && markerlist.size() > 0){
                    FragmentRun fragmentRun = SecondActivity.activity.getFragment(FragmentRun.class);
                    ((SecondActivity) getActivity()).setViewPagerRun(2);
                    fragmentRun.startRun(road);
                }
            }
        });

        navCusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeOldPath();
                FragmentMap.setMapActor(SecondActivity.activity.getFragmentWithMapInteraction(FragmentCustomRoute.class));
                ((SecondActivity) getActivity()).setViewPagerRun(1);
            }
        });

        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Klicke auf die Map um einen Marker zu setzen\n" +
                        "Es wird folgend eine Route geneeriert\n" +
                        "Klicke noch einmal auf die Map um eine neue Route zu genneerieren\n" +
                        "Die Varianz gibt an wie weit der \nRoutengenerator abweichen darf.";
                showMessage(message, 6);
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

    public void removeOldPath() {

        if (markerlist != null) {
            for (Marker m : markerlist) {
                mMapView.getOverlays().remove(m);
            }
            markerlist.removeAll(markerlist);
        }
        if (polylinelist != null) {
            for (Polyline p : polylinelist) {
                mMapView.getOverlays().remove(p);
            }
            polylinelist.removeAll(polylinelist);
        }
        mMapView.invalidate();
    }

    @Override
    public synchronized void shortClick(GeoPoint point) {
        removeOldPath();
        generationPoint = point;
        RouteGeneration generator = new RouteGenerator(roadManager);
        try {
            float distance = 0;
            if(distanceView.getText().toString() != ""){
                distance = Integer.parseInt(distanceView.getText().toString());
            }

            float varianz = varianzView.getProgress();
            varianz /= 10;
            Log.d("FragmentMap", "" + varianz);

            Log.d("FragmentMap", "");
            Route route = generator.generateRouteWithLength(point, point, distance, varianz);
            ArrayList<GeoPoint> points = new ArrayList<>();

            for (RoadNode node : route.nodes) {
                Log.d("FragmentMap", "added geoPoint:" +
                        node.mLocation + " with length: " + node.mLength);
                points.add(node.mLocation);
            }
            Marker newMarker = new Marker(mMapView);
            newMarker.setPosition(route.nodes.get(0).mLocation);
            newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            newMarker.setIcon(getResources().getDrawable(R.drawable.finish));
            mMapView.getOverlays().add(newMarker);
            markerlist.add(newMarker);

            Polyline newPolyline = new Polyline(mMapView);
            newPolyline.setColor(Color.RED);
            Road newRoad = roadManager.getRoad(points);
            ArrayList<RoadNode> nodes = newRoad.mNodes;
            for(int i = nodes.size()-1;i>0;i-=2){
                nodes.remove(i);
            }
            float displayedKilometer = ((int)(newRoad.mLength * 10)) / 10.0f;
            Toast.makeText(getContext(),"Routenlänge: " + displayedKilometer + "km",Toast.LENGTH_LONG).show();

            newPolyline = RoadManager.buildRoadOverlay(newRoad);

            mMapView.getOverlays().add(newPolyline);
            polylinelist.add(newPolyline);

            road = newRoad;
        } catch (RouteTooLongException x) {
            Log.d("FragmentMap", "RouteTooLongException: " + x.toString());
            String message = "Der Startpunkt liegt weiter von dem Endpunkt entfernt, als die" +
                    " maximale Länge der Strecke lang sein darf.";
            showMessage(message,5);
        } catch (RouteGenerationFailedException x) {
            Log.d("FragmentMap", "RouteGenerationFailedException: " + x.toString());
            String message = "Die Strecke konnte nicht generiert werden. Bitte wählen Sie " +
                    "falls möglich eine höhere Varianz oder versuchen sie es in " +
                    "einer Straßenreicheren Umgebung.";
            showMessage(message,5);
        } catch (Exception x){
            Log.d("FragmentMap", "General Exception: " + x.toString());
            String message = "Die Strecke konnte nicht generiert werden. Bitte " +
                    "versuchen Sie es in einer Straßenreicheren Umgebung erneut.";
            showMessage(message,4);
        }
        finally {
            mMapView.invalidate();
        }
    }

    @Override
    public void longClick(GeoPoint point) {  }

    public void showMessage(String errorMessage, int lines){
        final Snackbar snackbar = Snackbar.make(FragmentGeneration.view, errorMessage, 10000);
        snackbar.setAction("X", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        View snackbarView = snackbar.getView();
        TextView textView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(lines);
        snackbar.show();
    }

}
