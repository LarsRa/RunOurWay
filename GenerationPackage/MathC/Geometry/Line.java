package de.hsf.mobcomgroup1.runourway.GenerationPackage.MathC.Geometry;

import org.osmdroid.util.GeoPoint;

/**
 * a line is describes by a start point and a vector
 */
public class Line {

    public Line(Coordinate coordinate, Vector vector){
       setCoordinate(coordinate);
       setVector(vector);
    }

    private Coordinate coordinate;

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    private Vector vector;

    public Vector getVector() {
        return vector;
    }

    public void setVector(Vector vector) {
        this.vector = vector;
    }

    public Coordinate getLineEnd(){
        return new Coordinate(coordinate ,vector);
    }

    public GeoPoint getLineEndGeoPoint(){
        Coordinate coo = getLineEnd();
        return new GeoPoint(coo.x,coo.y);
    }

}
