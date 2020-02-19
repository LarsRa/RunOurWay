package de.hsf.mobcomgroup1.runourway.GenerationPackage.MathC.Geometry;

import org.osmdroid.util.GeoPoint;

public class Coordinate {

    public Coordinate(){}

    public Coordinate(float x, float y){
        this.x=x;
        this.y=y;
    }

    public Coordinate(GeoPoint point){
        this.x = (float)point.getLatitude();
        this.y =(float)point.getLongitude();
    }

    public Coordinate(Coordinate point, Vector vector){
        this.x = point.x + vector.deltaX;
        this.y = point.y + vector.deltaY;
    }

    public float x;
    public float y;
}
