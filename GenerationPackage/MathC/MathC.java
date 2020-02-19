package de.hsf.mobcomgroup1.runourway.GenerationPackage.MathC;

import org.osmdroid.util.GeoPoint;

import java.util.Random;

import de.hsf.mobcomgroup1.runourway.GenerationPackage.MathC.Geometry.Coordinate;
import de.hsf.mobcomgroup1.runourway.GenerationPackage.MathC.Geometry.Line;
import de.hsf.mobcomgroup1.runourway.GenerationPackage.MathC.Geometry.Vector;

public class MathC
{

    private static double radian = Math.PI / 180;

    private static Random rand = new Random();

    /**
     * one meter in latitude
     */
    private static double meterToLat = 0.0000089831528411952032673527364336916;

    /**
     * one meter in longitude
     */
    private static double meterToLong = 0.00001557681162607860555433991372286;

    public static void calculateCurrentMeterToLatLong(GeoPoint position){
        float longToMeter = (float)measure(position.getLatitude(), position.getLongitude(),
                position.getLatitude(),position.getLongitude() + 1);
        float latToMeter = (float)measure(position.getLatitude(), position.getLongitude(),
                position.getLatitude() + 1,position.getLongitude());
        meterToLat = 1 / latToMeter;
        meterToLong = 1 / longToMeter;
    }

    /**
     * needed to rotate a vector
     * @param lat
     * @return
     */
    public static float latToLong(float lat) {
        return (float)(lat * meterToLong / meterToLat);
    }

    /**
     * needed to rotate a vector
     * @param longitude
     * @return
     */
    public static float longToLat(float longitude) {
        return (float)(longitude * meterToLat / meterToLong);
    }

    /**
     *
     * @param point
     * @param meters
     * @param vector (not a real vector) dont just hand any vector over! |longitude| + |latitude| must be 1!
     * @return
     */
    private static GeoPoint translatePointDist(GeoPoint point, int meters, GeoPoint vector){
        double newX = point.getLatitude() + (vector.getLatitude() * meters * meterToLat);
        double newY = point.getLongitude() + (vector.getLongitude() * meters * meterToLong);
        return new GeoPoint(newX,newY);
    }

    /**
     * translates the point into a random direction
     * @param point
     * @param meters
     * @return
     */
    public static GeoPoint translatePointDist(GeoPoint point, int meters){
        double x = maybeInvertNumber(rand.nextDouble());
        double y = maybeInvertNumber(Math.sqrt(1 - x * x));
        return translatePointDist(point, meters, new GeoPoint(x,y));
    }

    private static double maybeInvertNumber(double number){
        if(rand.nextBoolean()){
            number *= -1;
        }
        return number;
    }

    public static GeoPoint buildMissingEquilateralTriangleGeoPoint(GeoPoint a, GeoPoint b){
        Coordinate result =
                buildMissingEquilateralTriangleCoordinate(new Coordinate(a),new Coordinate(b));
        return new GeoPoint(result.x, result.y);
    }

    public static float addValueSameSigned(float value, float add){
        if(value > 0){
            return value + add;
        }else{
            return value - add;
        }
    }

    /***
     * EquilateralTriangle -> Gleichseitiges Dreieck
     * @param a
     * @param b
     * @return
     */
    private static Coordinate buildMissingEquilateralTriangleCoordinate(Coordinate a, Coordinate b){
        return buildMissingEquilateralTriangleLine(a,b).getLineEnd();
    }

    public static Line buildMissingEquilateralTriangleLine(Coordinate a, Coordinate b){
        Vector vector = new Vector(a,b);
        Coordinate middle = new Coordinate(a, vector.multiplyVector(0.5f));
        Vector vectorToLastPoint = vector.rotateVector90Degrees(rand.nextBoolean());
        float vectorSideLength = vectorLength(vector);
        float vectorShrinkFactor = triangleHeight(vectorSideLength) / vectorSideLength;
        vectorToLastPoint = vectorToLastPoint.multiplyVector(vectorShrinkFactor);
        return new Line(middle, vectorToLastPoint);
    }

    private static float triangleHeight(GeoPoint a, GeoPoint b){
        return triangleHeight(vectorLength(new Vector(a,b)));
    }

    private static float triangleHeight(float sideLength){
        return (float)(Math.sqrt(3) / 2 * sideLength);
    }

    private static float vectorLength(Vector v){
        return (float)Math.sqrt(v.deltaX * v.deltaX + v.deltaY*v.deltaY);
    }

    /**
     * calculates distance in meter
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    public static double measure(float lat1,float lon1,float lat2,float lon2){  // generally used geo measurement function
        double R = 6378.137; // Radius of earth in KM
        double dLat = lat2 * radian - lat1 * radian;
        double dLon = lon2 * radian - lon1 * radian;
        double a = square(Math.sin(dLat/2)) +
                square(Math.cos(lat1 * radian)) *
                        square(Math.sin(dLon/2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return d * 1000; // meters
    }


    public static double measure(double lat1,double lon1,double lat2,double lon2) {
        return measure((float)lat1,(float)lon1,(float)lat2,(float)lon2);
    }

    public static float distance(GeoPoint g1, GeoPoint g2){
        return Math.abs((float)measure(g1.getLatitude(),g1.getLongitude(),g2.getLatitude(),g2.getLongitude()));
    }

    private static double square(double x){
        return x*x;
    }

}
