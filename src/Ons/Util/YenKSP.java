/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ons.Util;

import java.util.ArrayList;

/**
 * Yen's algorithm computes single-source K-shortest loopless 
 * paths for a graph with non-negative edge cost.
 * 
 * The algorithm was published by Jin Y. Yen in 1971 and employs any shortest path algorithm 
 * to find the best path, then proceeds to find K − 1 deviations of the best path.
 * 
 * @author onsteam
 */
public class YenKSP {
    
    /**
     * Retrieves the specific k-shortest path from source and destination (in path nodes)
     * @param graph the WeightedGraph object
     * @param source the source node
     * @param destination the destination node
     * @param index the k index path
     * @return the path nodes of specific k-shortest path index
     */
    public static int[] kShortestPathsIndex(WeightedGraph graph, int source, int destination, int index){
        ArrayList<Integer>[] paths = kShortestPaths(graph, source, destination, index + 1);
        int[] path = new int[paths[index].size()];
        for (int i = 0; i < path.length; i++) {
            path[i] = paths[index].get(i);
        }
        return path;
    }
    
    /**
     * Retrieves the k-shortest path from source and destination (in path nodes)
     * @param graph the WeightedGraph object
     * @param source the source node
     * @param destination the destination node
     * @param K the number of k-paths
     * @return the k-shortest paths
     */
    public static ArrayList<Integer>[] kShortestPaths(WeightedGraph graph, int source, int destination, int K){
        
        ArrayList<Integer>[] paths = new ArrayList[K];
        
        for (int i = 0; i < paths.length; i++){
            paths[i] = new ArrayList<>();
        }
        
        int spurNode;
        ArrayList<Integer> rootPath, spurPath = new ArrayList(), totalPath = new ArrayList();
        WeightedGraph auxGraph = new WeightedGraph(graph);
        int[] auxroute;
        
        // Determine the shortest path from the source to the sink.
        int[] route1 = Dijkstra.getShortestPath(graph, source, destination);
        for (int i = 0; i < route1.length; i++){
            paths[0].add(route1[i]);
        }
        
        // Initialize the heap to store the potential kth shortest path.
        ArrayList<ArrayList<Integer>> pathsTh = new ArrayList();
        
        for(int k = 1; k < K; k++){
            // The spur node ranges from the first node to the next to last node in the previous k-shortest path.
            for(int i = 0; i < paths[k-1].size() -1; i++){
                // Spur node is retrieved from the previous k-shortest path, k − 1.
                spurNode = paths[k-1].get(i);
                // The sequence of nodes from the source to the spur node of the previous k-shortest path.
                rootPath = subList(paths[k-1],0, i);
                // for each path p in paths:
                for(int p = 0; p < paths.length && !paths[p].isEmpty(); p++){
                    if(rootPath.size() < paths[p].size() && rootPath.equals(subList(paths[p],0, i))){
                        // Remove the links that are part of the previous shortest paths which share the same root path.
                        auxGraph.removeEdge(paths[p].get(i), paths[p].get(i+1));
                    }
                }
                for (Integer node : rootPath) {
                    if (node != spurNode) {
                        auxGraph.removeNodeEdge(node);
                    }
                }
                // Calculate the spur path from the spur node to the sink.
                auxroute = Dijkstra.getShortestPath(auxGraph, spurNode, destination);
                for (int j = 0; j < auxroute.length; j++) {
                    spurPath.add(auxroute[j]);
                }
                // Entire path is made up of the root path and spur path.
                totalPath = joinPaths(rootPath,spurPath);
                // Add the potential k-shortest path to the heap unique, and spurPath > 0.
                if(!spurPath.isEmpty()){
                    addUniquePath(pathsTh,totalPath);
                }
                // Add back the edges and nodes that were removed from the graph.
                auxGraph = new WeightedGraph(graph);
                spurPath = new ArrayList();
            }
            if (pathsTh.isEmpty()) {
                // This handles the case of there being no spur paths, or no spur paths left.
                // This could happen if the spur paths have already been exhausted (added to A), 
                // or there are no spur paths at all - such as when both the source and sink vertices 
                // lie along a "dead end".
                break;
            }
            // Sort the potential k-shortest paths by cost.
            // Find the pathMinWeight, the lowest cost path becomes the k-shortest path.
            int weight = Integer.MAX_VALUE, pathMinWeight = 0;
            for(int i = 0; i < pathsTh.size(); i++){
                int pathWeight = 0;
                for(int j = 0; j < pathsTh.get(i).size() - 1; j++){
                    pathsTh.get(i).get(j);
                    pathWeight += graph.getWeight(pathsTh.get(i).get(j), pathsTh.get(i).get(j+1));
                }
                if (pathWeight < weight){
                    weight = pathWeight;
                    pathMinWeight = i;
                }
            }
            // Add the lowest cost path becomes the k-shortest path.
            paths[k] = pathsTh.get(pathMinWeight);
            pathsTh.remove(pathMinWeight);
        }
        return paths;
    }
    
    private static ArrayList<Integer> subList(ArrayList<Integer> array, int fromIndex, int toIndex){
        ArrayList<Integer> list = new ArrayList<>();
        for(int i = fromIndex; i <= toIndex; i++){
            list.add(array.get(i));
        }
        return list;
    }

    private static ArrayList<Integer> joinPaths(ArrayList<Integer> rootPath, ArrayList<Integer> spurPath) {
        ArrayList<Integer> list = new ArrayList<>();
        list.addAll(rootPath);
        for(int i = 1; i < spurPath.size(); i++){
            list.add(spurPath.get(i));
        }
        return list;
    }

    private static void addUniquePath(ArrayList<ArrayList<Integer>> pathsTh, ArrayList<Integer> totalPath) {
        for (ArrayList<Integer> pathsTh1 : pathsTh) {
            if (pathsTh1.equals(totalPath)) {
                return;
            }
        }
        pathsTh.add(totalPath);
    }
}
