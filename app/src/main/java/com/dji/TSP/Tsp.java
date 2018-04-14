package com.dji.TSP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dji.common.mission.waypoint.Waypoint;


class Tsp {
    private double[][] graph;
    private HashMap<Integer, ArrayList<Integer>> idtoset = new HashMap<>();
    // get subset by id
    private HashMap<ArrayList<Integer>, Integer> settoid = new HashMap<>();

    // get id by subset
    Tsp(double[][] graph) {
        this.graph = graph;
    }

    /**
     * Solve Traveling Salesperson Probl em by Dynamic Programming
     *
     * @return the min length
     */
    List<Waypoint> DP(List<Waypoint> WaypointList) {
        int n = graph.length;
        int[] vertex = new int[n - 1];
        int vertexId = 1;
        for (int i = 0; i < vertex.length; i++) {
            vertex[i] = vertexId;
            vertexId++;
        }
        getsubsets(vertex);
        double[][] D = new double[n][settoid.size()];// To record the distance
        int[][] P = new int[n][settoid.size()];// To track the path

        for (int i = 1; i < n; i++) {
            D[i][0] = this.graph[i][0];
        }
        for (int k = 1; k <= n - 2; k++) {
            for (int id = 0; id < idtoset.size(); id++) {
                ArrayList<Integer> subset = idtoset.get(id);
                if (subset.size() != k)
                    continue;
                for (int i = 1; i < n; i++) {
                    if (subset.contains(i))
                        continue;
                    double min = 10000;
                    double value = 0;
                    for (int j : subset) {
                        ArrayList<Integer> Aminusj = remove(subset, j);
                        int idj = settoid.get(Aminusj);
                        try {
                            value = this.graph[i][j] + D[j][idj];
                        } catch (Exception e) {
                            System.out.print("Error!___");
                            System.out.println("i: " + String.valueOf(i) + " j: " + String.valueOf(j));
                            int size = this.graph[i].length;
                            System.out.println(" graph.length: " + String.valueOf(size));
                            System.out.println(" D.length: " + String.valueOf(D.length));
                        }
                        if (value < min && value != 0) {
                            min = value;
                            P[i][id] = j;
                        }
                    }
//					if (min < 9999)
//						;
                    D[i][id] = min;
                }
            }
        }
        ArrayList<Integer> Vminusv0 = new ArrayList<>();
        for (int i = 0; i < vertex.length; i++) {
            Vminusv0.add(vertex[i]);
        }
        int vminusv0id = settoid.get(Vminusv0);
        double min = Integer.MAX_VALUE;
        for (int j : Vminusv0) {
            ArrayList<Integer> Vminusv0vj = remove(Vminusv0, j);
            int idj = settoid.get(Vminusv0vj);

            double value = (this.graph[0][j] != 0 && D[j][idj] != 0) ? this.graph[0][j] + D[j][idj] : 0;

            if (value < min && value != 0) {
                min = value;
                P[0][vminusv0id] = j;
            }
        }
//		if (min < 99999)
//			;
        D[0][vminusv0id] = min;

        return generateOpttour(P, Vminusv0, WaypointList);
        //return D[0][vminusv0id];
    }


    /**
     * Generate optimal tour by P, and print it
     *
     * @param P
     *
     *            containing all vertexes except V0
     */
    private List<Waypoint> generateOpttour(int[][] P, ArrayList<Integer> V, List<Waypoint> WaypointList) {
        List<Waypoint> newWaypointList = new ArrayList<>();
        //String path = "1->";
        newWaypointList.add(WaypointList.get(0));
        ArrayList<Integer> Set = V;
        int start = 0;
        while (!Set.isEmpty()) {
            int id = settoid.get(Set);
//            String vertex = String.valueOf(P[start][id] + 1);
            int vertex = P[start][id];
//            newWaypointList.add(WaypointList.get(Integer.parseInt(vertex)-1));
            newWaypointList.add(WaypointList.get(vertex));//从原来的WaypointList中取出Waypoint并添加到新的List中
            //path += vertex + "->";
            Set = remove(Set, P[start][id]);
            start = P[start][id];
        }
        //path += "1";
        //System.out.println(path);
        return newWaypointList;
    }

    /**
     * Get all subsets of a input set. And number subsets All results will be
     * recorded in member variables
     *
     * @param set
     *            input set
     */
    private void getsubsets(int[] set) {
        idtoset.clear();
        settoid.clear();
        int max = 1 << set.length; // how many sub sets
        int id = 0;
        for (int i = 0; i < max; i++) {
            int index = 0;
            int k = i;
            ArrayList<Integer> s = new ArrayList<>();
            while (k > 0) {
                if ((k & 1) > 0) {
                    s.add(set[index]);
                }
                k >>= 1;
                index++;
            }
            idtoset.put(id, s);
            settoid.put(s, id);
            id++;
        }
    }

    /**
     * Remove an input value in a list
     *
     * @param src
     *            source list
     * @param n
     *            the value to be removed
     * @return list after removing n
     */
    private ArrayList<Integer> remove(ArrayList<Integer> src, int n) {
        ArrayList<Integer> dest = new ArrayList<>();
        //int j = 0;
        for (int i = 0; i < src.size(); i++) {
            int vertex = src.get(i);
            if (vertex == n)
                continue;
            dest.add(vertex);
        }
        return dest;
    }
}
