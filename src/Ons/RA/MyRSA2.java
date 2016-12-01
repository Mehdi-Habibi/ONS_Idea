/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Ons.RA;

import Ons.*;
import Ons.Util.*;
/**
 * This is a sample algorithm for the Routing and Spectrum Alocation problem.
 *
 * Fixed path routing is the simplest approach to finding a lightpath. The same
 * fixed route for a given source and destination pair is always used. This path
 * is computed using Dijkstra's Algorithm.
 *
 * First-Fit slots set assignment tries to establish the lightpath using the
 * first slots set available sought in the increasing order.
 * @author onsteam
 */
public class MyRSA2 implements RA {
    
    private ControlPlaneForRA cp;
    private WeightedGraph graph;
    private int modulation;

    @Override
    public void simulationInterface(ControlPlaneForRA cp) {
        this.cp = cp;
        this.graph = cp.getPT().getWeightedGraph();
        //Set the default modulation
        this.modulation = Modulation._BPSK;
    }

    @Override
    public void flowArrival(Flow flow) {
        int[] nodes;
        int[] links;
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

        // Calculates the required slots
        int requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), modulation);
        
        // Evaluate if each link have space to the required slots
        for (int i = 0; i < links.length; i++){
            if (!((EONLink) cp.getPT().getLink(links[i])).hasSlotsAvaiable(requiredSlots)){
                cp.blockFlow(flow.getID()); 
                return;
            }
        }
        
        // First-Fit spectrum assignment in modulation
        int[] firstSlot;
        for (int i = 0; i < links.length; i++) {
            // Try the slots available in each link
            // Note the difference here:
            firstSlot = ((EONLink) cp.getPT().getLink(links[i])).getSlotsAvailableToArray(requiredSlots);
            for (int j = 0; j < firstSlot.length; j++) {
                // Now you create the lightpath to use the createLightpath VT
                EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), links,
                        firstSlot[j], (firstSlot[j] + requiredSlots - 1), modulation);
                // Now you try to establish the new lightpath, accept the call
                if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                    // Single-hop routing (end-to-end lightpath)
                    lps[0] = cp.getVT().getLightpath(id);
                    if(cp.acceptFlow(flow.getID(), lps)){
                        return;
                    } else {
                        // Something wrong
                        // Dealocates the lightpath in VT and try again
                        cp.getVT().deallocatedLightpath(id);
                    }
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
