package com.dji.TSP;

import com.amap.api.maps.model.LatLng;
import com.dji.GSDemo.GaodeMap.Util;

import java.util.List;

import dji.common.mission.waypoint.Waypoint;


public class TspEntrance {
    public static List<Waypoint> Main(List<Waypoint> WaypointList){
        LatLng latLng1,latLng2;
        int len = WaypointList.size();
        double Distance [][]= new double[len][len];
        for (int p = 0; p<len; p++){
            for (int q = 0; q<len; q++){
                latLng1 = new LatLng(WaypointList.get(p).coordinate.getLatitude(),WaypointList.get(p).coordinate.getLongitude());
                latLng2 = new LatLng(WaypointList.get(q).coordinate.getLatitude(),WaypointList.get(q).coordinate.getLongitude());
                Distance[p][q] = Util.getDistance(latLng1,latLng2);
            }
        }
        Tsp tsp = new Tsp(Distance);
        return tsp.DP(WaypointList);
    }
}
