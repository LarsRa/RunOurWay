package de.hsf.mobcomgroup1.runourway.Navigation.Statistic;

import org.osmdroid.util.GeoPoint;

import java.util.List;

public interface RunStatistician {

    float getRunTimeInMilliseconds();

    float getAverageSpeedInKMH();

    float getDistanceInKm();

    List<GeoPoint> getNextCheckPoints(int count);

    List<GeoPoint> getNextCheckPoints(int count, int minDistanceInMeter);

}
