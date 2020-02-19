package de.hsf.mobcomgroup1.runourway.GenerationPackage.RouteGeneration;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class Route {
    public List<RoadNode> nodes;

    public List<GeoPoint> getGeoPointsFromRoute(){
        List<GeoPoint> result = new ArrayList<>();

        for(RoadNode node : nodes){
            result.add(node.mLocation);
        }

        return result;
    }

    public float length;

    public Route(Road road){
        this.nodes = road.mNodes;
        this.length = (float)road.mLength;
    }

    public void addRoad(Road road){
        nodes.addAll(road.mNodes);
        this.length += road.mLength;
    }

}
