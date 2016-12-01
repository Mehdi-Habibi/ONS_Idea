/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ons.Util;

import java.util.ArrayList;

/**
 * This is the algorithm to find the disjoint shortest paths in WeightedGraph graph object.
 * 
 * @author onsteam
 */
public class DSP {
    
    /**
     * Retrieves the disjoint shortest paths.
     * @param graph the WeightedGraph object
     * @param source the source node
     * @param destination the destination node
     * @return the ArrayList object containing the disjoint shortest paths.
     */
    public static ArrayList<Integer>[] disjointShortestPaths(WeightedGraph graph, int source, int destination){
        ArrayList<Integer>[] paths = null;
        int nodes[];
        WeightedGraph g = new WeightedGraph(graph);
        ArrayList<Integer> set;
        ArrayList<ArrayList<Integer>> dsp = new ArrayList<>();
        
        while(true){
            nodes = Dijkstra.getShortestPath(g, source, destination);
            if (nodes.length == 0) {
                break;
            }
            set = new ArrayList<>();
            for (int i = 0; i < nodes.length; i++) {
                set.add(nodes[i]);
            }
            // Add path to the set
            dsp.add(set);
            // Remove edges from graph
            for (int i = 0; i < nodes.length - 1; i++) {
                g.removeEdge(nodes[i], nodes[i + 1]);
            }
        }
        if(!dsp.isEmpty()){
            paths = new ArrayList[dsp.size()];
        }
        for (int i = 0; i < dsp.size(); i++) {
            paths[i] = new ArrayList<>(dsp.get(i));
        }
        return paths;
    }    
}

