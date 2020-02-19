package de.hsf.mobcomgroup1.runourway.Navigation;

import android.location.Location;

import org.osmdroid.util.GeoPoint;

/**
 * interface for correcting corrupted gps data, which probably happened due to bad GPS Connection
 */
public interface IncorrectLocationFixer {

    GeoPoint checkGeoPoint(Location point);

}
