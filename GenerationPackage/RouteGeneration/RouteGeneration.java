package de.hsf.mobcomgroup1.runourway.GenerationPackage.RouteGeneration;

import org.osmdroid.util.GeoPoint;

import de.hsf.mobcomgroup1.runourway.GenerationPackage.RouteGeneration.Exceptions.RouteGenerationFailedException;
import de.hsf.mobcomgroup1.runourway.GenerationPackage.RouteGeneration.Exceptions.RouteTooLongException;

public interface RouteGeneration {

    Route generateRouteWithLength(GeoPoint startPoint, GeoPoint endPoint, float kilometers, float maxVariance)
            throws RouteGenerationFailedException, RouteTooLongException;

}
