package Modules;

import java.util.List;
import Modules.Route;

/**
 * Created by Deep on 7/21/2018.
 */

public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}
