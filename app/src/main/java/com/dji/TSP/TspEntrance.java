package com.dji.TSP;

import com.amap.api.maps.model.LatLng;
import com.dji.GSDemo.GaodeMap.PositionUtil;
import com.dji.GSDemo.GaodeMap.Util;

import java.util.List;

import dji.common.mission.waypoint.Waypoint;


public class TspEntrance {
    public static List<Waypoint> Main(List<Waypoint> WaypointList){
//        LatLng latLng1,latLng2;
//        double altitude1,altitude2;
//        double surfaceDifference, altitudeDifference;
        int len = WaypointList.size();
        double Distance [][]= new double[len][len];//初始化距离矩阵
        for (int p = 0; p<len; p++){
            for (int q = 0; q<len; q++){
//                latLng1 = new LatLng(WaypointList.get(p).coordinate.getLatitude(),
//                        WaypointList.get(p).coordinate.getLongitude());//将第一个Waypoint转化为Latlng
//                latLng2 = new LatLng(WaypointList.get(q).coordinate.getLatitude(),
//                        WaypointList.get(q).coordinate.getLongitude());//将第二个Waypoint转化为Latlng
//                altitude1 = WaypointList.get(p).altitude;
//                altitude2 = WaypointList.get(q).altitude;
//                surfaceDifference = Util.getDistance(latLng1,latLng2);//求平面距离
//                altitudeDifference = altitude1-altitude2;//求高度差
//                Distance[p][q] = Math.sqrt(surfaceDifference * surfaceDifference +
//                        altitudeDifference * altitudeDifference);//求空间内距离
                Distance[p][q] = PositionUtil.get3Ddistance(WaypointList.get(p),WaypointList.get(q));
            }
        }
        Tsp tsp = new Tsp(Distance);
        return tsp.DP(WaypointList);
    }
}
