package de.hsf.mobcomgroup1.runourway.GenerationPackage.RouteGeneration;

import de.hsf.mobcomgroup1.runourway.GenerationPackage.MathC.Geometry.Coordinate;
import de.hsf.mobcomgroup1.runourway.GenerationPackage.MathC.Geometry.Line;
import de.hsf.mobcomgroup1.runourway.GenerationPackage.MathC.MathC;
import de.hsf.mobcomgroup1.runourway.GenerationPackage.RouteGeneration.Exceptions.RouteGenerationFailedException;
import de.hsf.mobcomgroup1.runourway.GenerationPackage.RouteGeneration.Exceptions.RouteTooLongException;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class RouteGenerator implements RouteGeneration{

    public RouteGenerator(RoadManager roadManager){
        this.roadManager = roadManager;
    }

    private RoadManager roadManager;

    /**
     * the estimated variance between direct distance and distance following streets
     */
    private float estimatedLengthVariance = 0.375f;

    private int generateTriesBeforeExit = 2;

    private int fixRouteTriesBeforeReturn = 4;

    /**
     * if the generated route is not accepted but the extra length lies within 20% of the
     * accepted route length the route points will slightly be modified.
     */
    private float devianceForRouteModifyMethod = 0.3f;

    private float devianceForRouteModifyMethodInKm = 0.75f;

    private boolean tryModifyPoint(float difference, float expectedLength){
        return difference <= Math.max((devianceForRouteModifyMethodInKm),
                expectedLength*devianceForRouteModifyMethod);
    }

    /**
     * @param startPoint
     * @param endPoint
     * @param kilometers
     * @param maxVarianceInKm should probably be at least at 500 meter
     * @return
     */
    @Override
    public Route generateRouteWithLength(GeoPoint startPoint, GeoPoint endPoint, float kilometers, float maxVarianceInKm)
            throws RouteGenerationFailedException, RouteTooLongException {

        Road firstRoad = null;
        float detourLeft;
        if(startPoint != endPoint) {
            ArrayList<GeoPoint> points = new ArrayList<>();
            points.add(startPoint);
            points.add(endPoint);

            firstRoad = roadManager.getRoad(points);
            detourLeft = (float)(kilometers - firstRoad.mLength);
        }else{
            detourLeft = kilometers;
        }


        /**
         * if the route generation is only used for circles this can never happen
         */
        if(detourLeft < maxVarianceInKm){
            throw new RouteTooLongException("The route between your chosen points is " +
                    "already longer than your intended way length");
        }

        /**
         * in order to build a equilateral Triangle the local meter to latitude /longitude must
         * be calculated
         */
        MathC.calculateCurrentMeterToLatLong(startPoint);
        Route detourPart = buildDetour(startPoint, detourLeft, maxVarianceInKm,
                estimatedLengthVariance,generateTriesBeforeExit);

        if(firstRoad != null) {
            detourPart.addRoad(firstRoad);
        }

        return detourPart;
    }

    /**
     * @param startPoint
     * @param kilometers estimated length of the route
     * @param maxVariance the route will be accepted when the final length is within this distance from the expected length
     * @param estimatedLengthVariance the estimated length in the length will increase from direct length to roadLength
     * @param triesBeforeExit if the route fails to generate the function calls itself with different parameters. generate try has a big influence on the parameter changes
     * @return
     */
    private Route buildDetour(GeoPoint startPoint, float kilometers, float maxVariance, float estimatedLengthVariance, int triesBeforeExit)
            throws RouteGenerationFailedException{
        triesBeforeExit--;
        float expectedLengthVariance = kilometers * estimatedLengthVariance;
        float generateLength = kilometers - expectedLengthVariance;
        float triangleSideLength = generateLength / 3;
        GeoPoint firstPoint = MathC.translatePointDist(startPoint, (int)(triangleSideLength * 1000));

        /**
         * get line to second point. Since a line is easy to modify it is a good choice,
         * as the generated length may deviate too much from expected length so the
         * line can be extended or shortened
         */
        Line lineToSecondPoint =  MathC.buildMissingEquilateralTriangleLine(
                new Coordinate(startPoint),new Coordinate(firstPoint));

        Coordinate secondPointCoo = lineToSecondPoint.getLineEnd();
        GeoPoint secondPoint = new GeoPoint(secondPointCoo.x,secondPointCoo.y);
        ArrayList<GeoPoint> detourPoints = new ArrayList<>();
        detourPoints.add(startPoint);
        detourPoints.add(firstPoint);
        detourPoints.add(secondPoint);
        detourPoints.add(startPoint);
        //can throw exception build try catch
        try {
            Road detourRoad = roadManager.getRoad(detourPoints);


            Route result = correctRoad(detourRoad, startPoint, firstPoint, lineToSecondPoint, kilometers,
                    maxVariance, estimatedLengthVariance, fixRouteTriesBeforeReturn);

            if(result == null){
                return tryNextRoute(triesBeforeExit,startPoint,kilometers,maxVariance);
            }

            return result;
        }catch(Exception x){
            return tryNextRoute(triesBeforeExit,startPoint,kilometers,maxVariance);
        }
    }

    private Route tryNextRoute(int triesBeforeExit, GeoPoint startPoint, float kilometers, float maxVariance/*geoPoint first)*/)throws RouteGenerationFailedException{
        Route result = null;
        if(triesBeforeExit > 0){
            /**
             * for a better procedure the problematic triangle point should be found
             * and replaced. Also if a totaly new route is generated it should
             * be with the inverted vector to the first point
             */
            result = buildDetour(startPoint, kilometers, maxVariance, estimatedLengthVariance, triesBeforeExit);
        }
        if(result==null){
            throw new RouteGenerationFailedException("The Route failed to be generated. Make sure " +
                    "to be in a region with sufficient connected ways and try again or create your own route.");
        }
        return  result;
    }

    private Route correctRoad(Road detourRoad, GeoPoint frst, GeoPoint scnd, Line toThird, float kilometers,
                              float maxVariance, float estimatedLengthVariance, int correctTries) throws RouteGenerationFailedException{
        Route result = null;
        //float expectedLengthVariance = kilometers * estimatedLengthVariance;
        if(Math.abs(detourRoad.mLength - kilometers) <= Math.abs(maxVariance)){
            result = new Route(detourRoad);
        }else if(correctTries > 0){
            correctTries--;
            /**
             * difference before route length would be accepted
             */
            float difference = (float)(detourRoad.mLength - kilometers);
            /**
             * sign of the difference -> either 1 or -1
             */
            //int sign = (int)(difference / Math.abs(difference));
            difference = Math.abs(difference);

            /**
             * checks whether the route is lost or not. route is
             * defined at lost when its actual length is not even close
             * to the expected length
             */
            if(tryModifyPoint(difference, kilometers)){
                /**
                 * new expected length variance -> factor may be changing too much
                 */
                //float adjustedLengthVariance = (float)Math.pow(difference / expectedLengthVariance,sign) * estimatedLengthVariance;

                float factorShrink =/* 2.0f/3f*/1;
                float factor = kilometers / (float)detourRoad.mLength;
                toThird.setVector(toThird.getVector().multiplyVector(factor * factorShrink));
                ArrayList<GeoPoint> points = new ArrayList<>();
                points.add(frst);
                points.add(scnd);
                points.add(toThird.getLineEndGeoPoint());
                points.add(frst);
                Road nextRoad = roadManager.getRoad(points);
                result = correctRoad(nextRoad, frst,scnd,toThird,kilometers,maxVariance,estimatedLengthVariance,correctTries);
            }else{
                //result = null;
            }
        }else{
            //result = null;
        }
        return result;
    }

}
