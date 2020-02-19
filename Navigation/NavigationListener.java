package de.hsf.mobcomgroup1.runourway.Navigation;

import org.osmdroid.util.GeoPoint;

public interface NavigationListener {

    void pointAdded(GeoPoint points);

    void checkPointReached(GeoPoint point);

    void runEnd();

}
