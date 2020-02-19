package de.hsf.mobcomgroup1.runourway.Navigation;

import android.content.Context;
import android.location.Location;
import android.speech.tts.TextToSpeech;

import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Stack;

import de.hsf.mobcomgroup1.runourway.GenerationPackage.MathC.MathC;
import de.hsf.mobcomgroup1.runourway.GenerationPackage.RouteGeneration.Route;
import de.hsf.mobcomgroup1.runourway.Navigation.Statistic.NavigatorStatistician;
import de.hsf.mobcomgroup1.runourway.View.Fragments.FragmentRun;

public class RouteRecordEvaluator implements NavigatorStatistician {

    /**
     * -> 10 meters next to the checkpoint counts as reached
     */
    private final float CHECKPOINT_REACHED_ACCEPTED_VARIANCE_IN_METER = 10;

    private final String reachedDestination = "You have arrived at your destination";

    private Stack<GeoPoint> runnerPositions;

    private float currentDistanceInKm;

    private TextToSpeech reader;

    private List<NavigationListener> subscriber = new ArrayList<>();

    private Queue<RoadNode> checkPoints;

    private Queue<String> speechTexts;

    private boolean isSpeakerReady;

    /**
     * this can be handed over in the constructor to correct incorrect geopoints
     */
    private IncorrectLocationFixer incorrectLocationFixer;

    private boolean isRunOver;

    public RouteRecordEvaluator(Context context){
        this(context,null);
    }

    public RouteRecordEvaluator(Context context, IncorrectLocationFixer locationFixer) {
        this.incorrectLocationFixer = locationFixer;
        this.speechTexts = new ArrayDeque<>();
        initializeReader(context);
        initializeNavigator();
    }

    private void initializeReader(Context context){
        this.reader = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                reader.setLanguage(Locale.GERMAN);
                reader.setPitch(0.6666f);
                isSpeakerReady = true;
                readQueuedTexts();
            }
        });
    }

    private void addSpeechText(String text){
        /**
         * "you arrived at your destination" nicht vorlesen, außer es ist der letzte punkt
         */
        if(!text.equals(reachedDestination) || checkPoints.size() == 1) {
            if (isSpeakerReady) {
                reader.speak(text, TextToSpeech.QUEUE_ADD, null);
            } else {
                speechTexts.add(text);
            }
        }
    }

    private void readQueuedTexts(){
        while(speechTexts.size() > 0){
            reader.speak(speechTexts.poll(),TextToSpeech.QUEUE_ADD,null);
        }
    }

    public void startRun(Route route){
        startRun(route.nodes);
    }

    @Override
    public List<GeoPoint> getNextCheckPoints(int count) {
        return getNextCheckPoints(count, 50);
    }

    /**
     * returns the next n-checkpoints which are at least minDistanceInMeter meter away
     * @param count
     * @param minDistanceInMeter
     * @return
     */
    @Override
    public List<GeoPoint> getNextCheckPoints(int count, int minDistanceInMeter) {
        List<GeoPoint> result = new ArrayList<>();
        int counter = 0;
        int currentDistance = minDistanceInMeter;
        for (RoadNode node : checkPoints) {
            if (currentDistance >= minDistanceInMeter) {
                result.add(node.mLocation);
                counter++;
                currentDistance = 0;
                if (counter >= count) {
                    break;
                }
            }
            currentDistance += node.mLength * 1000;
        }
        return result;
    }

    public void startRun(List<RoadNode> checkPoints){
        initializeNavigator();
        runnerPositions = new Stack<>();
        this.checkPoints = new ArrayDeque<>();
        for(RoadNode p : checkPoints){
            this.checkPoints.add(p);
        }
        addSpeechText(this.checkPoints.peek().mInstructions);
    }

    void initializeNavigator(){
        currentDistanceInKm = 0;
        isRunOver = false;
    }

    @Override
    public void locationChanged(Location location) {
        GeoPoint point;

        if(incorrectLocationFixer != null){
            point = incorrectLocationFixer.checkGeoPoint(location);
        }else{
            point = new GeoPoint(location.getLatitude(),location.getLongitude());
        }

        addPointDistance(point);

        runnerPositions.push(point);

        callSubscriberPointAdded(point);

        isCheckPointReached(point);
    }

    private void addPointDistance(GeoPoint point){
        if(!runnerPositions.isEmpty()) {
            GeoPoint last = runnerPositions.peek();
            float distanceInKm = MathC.distance(point,last) / 1000;
            currentDistanceInKm += distanceInKm;
        }
    }

    private void isCheckPointReached(GeoPoint current){
        RoadNode currentCheckPoint = checkPoints.peek();
        float distanceToCheckPoint = MathC.distance(current,currentCheckPoint.mLocation);
        if(distanceToCheckPoint <= CHECKPOINT_REACHED_ACCEPTED_VARIANCE_IN_METER){
            /**
             * remove the first checkPoint since it is reached
             */
            checkPoints.poll();
            callSubscriberCheckPointReached(current);
            if(checkPoints.isEmpty()){
                /**
                 * if no checkpoint are remaining the route is finished
                 */
                callSubscriberRouteFinished();
                isRunOver = true;
            }else{
                addSpeechText(this.checkPoints.peek().mInstructions);
                /**
                 * in case the next checkpoint is very close to the last one
                 * the function checks if the next checkpoint is reached as well
                 */
                isCheckPointReached(current);
            }
        }
    }

    private void callSubscriberCheckPointReached(GeoPoint checkPoint){
        for(NavigationListener listener : this.subscriber){
            listener.checkPointReached(checkPoint);
        }
    }

    private void callSubscriberPointAdded(GeoPoint point){
        for(NavigationListener listener : this.subscriber){
            listener.pointAdded(point);
        }
    }

    private void callSubscriberRouteFinished(){
        for(NavigationListener listener : this.subscriber){
            listener.runEnd();
        }
    }

    @Override
    public float getRunTimeInMilliseconds() {
        return FragmentRun.timerCounter * 1000;
    }

    @Override
    public float getAverageSpeedInKMH() {
        /**
         * milliseconds to hours
         */
        float runTimeInHours = getRunTimeInMilliseconds() / 3600000;

        /**
         * kilometers per hour
         */
        return getDistanceInKm() / runTimeInHours;
    }

    @Override
    public float getDistanceInKm() {
        return currentDistanceInKm;
    }

    @Override
    public void addNavigationListener(NavigationListener subscriber) {
        this.subscriber.add(subscriber);
    }

    @Override
    public void removeNavigationListener(NavigationListener subscriber) {
        this.subscriber.remove(subscriber);
    }

    @Override
    public void clearSubscriber() {
        this.subscriber.clear();
    }

    @Override
    public void endRun() {
        this.isRunOver = false;
        callSubscriberRouteFinished();
    }
}
