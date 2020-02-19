package de.hsf.mobcomgroup1.runourway.View;

import org.osmdroid.util.GeoPoint;

public interface MapInteraction {

    void shortClick(GeoPoint point);

    void longClick(GeoPoint point);

}
