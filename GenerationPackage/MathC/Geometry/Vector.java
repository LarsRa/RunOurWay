package de.hsf.mobcomgroup1.runourway.GenerationPackage.MathC.Geometry;

import de.hsf.mobcomgroup1.runourway.GenerationPackage.MathC.MathC;

import org.osmdroid.util.GeoPoint;

public class Vector {
    public Vector(GeoPoint a, GeoPoint b){
        deltaX = (float)(b.getLatitude() - a.getLatitude());
        deltaY = (float)(b.getLongitude() - a.getLongitude());
    }

    public Vector(Coordinate a, Coordinate b){
        deltaX = (b.x - a.x);
        deltaY = (b.y - a.y);
    }

    public Vector(float deltaX , float deltaY){
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    public Vector invertVector(){
        return new Vector(-this.deltaX, -this.deltaY);
    }

    /**
     * not quite right, but will work for this application
     * @param clockwiseDirection
     * @return
     */
    public Vector rotateVector90Degrees(boolean clockwiseDirection){
        if(clockwiseDirection){
            return new Vector(MathC.longToLat(this.deltaY), MathC.latToLong(-this.deltaX));
        }else{
            return new Vector(MathC.longToLat(-this.deltaY), MathC.latToLong(this.deltaX));
        }
    }

    public Vector multiplyVector(float factor){
        return new Vector(deltaX * factor,deltaY * factor);
    }

    public float getLength(){
        return (float)Math.sqrt(deltaX * deltaY + deltaY * deltaY);
    }

    public float deltaX;
    public float deltaY;

}
