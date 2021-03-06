/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Ons.RA;

import Ons.*;
import Ons.Util.*;

/**
 * This is a sample algorithm for the Routing and Wavelength Assignment problem.
 *
 * Fixed path routing is the simplest approach to finding a lightpath. The same
 * fixed route for a given source and destination pair is always used. This path
 * is computed using Dijkstra's Algorithm.
 *
 * First-Fit wavelength set assignment tries to establish the lightpath using the
 * first wavelength available sought in the increasing order.
 * @author onsteam
 */
public class MyRWA implements RA {
    
    private ControlPlaneForRA cp;
    private WeightedGraph graph;

    @Override
    public void simulationInterface(ControlPlaneForRA cp) {
        this.cp = cp;
        this.graph = cp.getPT().getWeightedGraph();
    }

    @Override
    public void flowArrival(Flow flow) {
        int[] nodes;
        int[] links;
        int[] wvls;
        long id;
        LightPath[] lps = new LightPath[1];

        // Shortest-Ons.Path routing
        nodes = Dijkstra.getShortestPath(graph, flow.getSource(), flow.getDestination());

        // If no possible path found, block the call
        if (nodes.length == 0) {
            cp.blockFlow(flow.getID()); 
            return;
        }

        // Create the links vector
        links = new int[nodes.length - 1];
        for (int j = 0; j < nodes.length - 1; j++) {
            links[j] = cp.getPT().getLink(nodes[j], nodes[j + 1]).getID();
        }

        // First-Fit wavelength assignment
        wvls = new int[links.length];
        for (int i = 0; i < ((WDMPhysicalTopology) cp.getPT()).getNumWavelengths(); i++) {
            // Create the wavelengths vector
            for (int j = 0; j < links.length; j++) {
                wvls[j] = i;
            }
            // Now you create the lightpath to use the createLightpath VT
            WDMLightPath lp = new WDMLightPath(1, flow.getSource(), flow.getDestination(), links, wvls);
            // Now you try to establish the new lightpath, accept the call
            if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                // Single-hop routing (end-to-end lightpath)
                lps[0] = cp.getVT().getLightpath(id);
                if (cp.acceptFlow(flow.getID(), lps)) {
                    return;
                } else {
                    // Something wrong
                    // Dealocates the lightpath in VT and try again
                    cp.getVT().deallocatedLightpath(id);
                }
            }
        }
        // Block the call
        cp.blockFlow(flow.getID());
    }

    @Override
    public void flowDeparture(long id) {
        // Do anything before flow departure
    }
    
}
