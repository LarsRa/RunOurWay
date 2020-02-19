package de.hsf.mobcomgroup1.runourway.Navigation.Statistic;

import org.osmdroid.bonuspack.routing.RoadNode;

import java.util.List;

import de.hsf.mobcomgroup1.runourway.GenerationPackage.RouteGeneration.Route;
import de.hsf.mobcomgroup1.runourway.Navigation.LocationChangeListener;
import de.hsf.mobcomgroup1.runourway.Navigation.NavigationListener;

public interface NavigatorStatistician extends LocationChangeListener, RunStatistician {

    void addNavigationListener(NavigationListener subscriber);

    void removeNavigationListener(NavigationListener subscriber);

    void clearSubscriber();

    void endRun();

    void startRun(List<RoadNode> checkPoints);

    void startRun(Route route);

}
