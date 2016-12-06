/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Ons.RA;

import Ons.*;
import Ons.Util.*;

import java.util.ArrayList;

/**
 * The proposed Xin Wan.
 * Article: "Dynamic routing and spectrum assignment in spectrum flexible transparent optical networks",
 * Xin Wan, Nan Hua, and Xiaoping Zheng,
 * Journal of Optical Communications and Networking, IEEE/OSA,
 * Aug 2012.
 * @author onsteam
 */
public class KSPwithSNR implements RA {

    private ControlPlaneForRA cp;
    private WeightedGraph graph;
    private int modulation;

    @Override
    public void simulationInterface(ControlPlaneForRA cp) {
        this.cp = cp;
        this.graph = cp.getPT().getWeightedGraph();
        //The default modulation
        this.modulation = Modulation._QPSK;
    }

    @Override
    public void flowArrival(Flow flow) {
        int[] nodes;
        int[] links;
        long id;
        LightPath[] lps = new LightPath[1];

        // Set k=4
        int ksp = 4;

        // k-Shortest Paths routing

        ArrayList<Integer>[] kpaths = YenKSP.kShortestPaths(graph, flow.getSource(), flow.getDestination(), ksp);

        for (int k = 0; k < kpaths.length; k++) {

            nodes = route(kpaths,k);
            // If no possible path found, block the call
            if (nodes.length == 0 || nodes == null) {
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

            // First-Fit spectrum assignment in BPSK Ons.Modulation
            int[] firstSlot;
            for (int i = 0; i < links.length; i++) {
                // Try the slots available in each link
                firstSlot = ((EONLink) cp.getPT().getLink(links[i])).getSlotsAvailableToArray(requiredSlots);
                for (int j = 0; j < firstSlot.length; j++) {
                    // Now you create the lightpath to use the createLightpath VT
                    EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), links,
                            firstSlot[j], (firstSlot[j] + requiredSlots - 1), modulation);
                    // Now you try to establish the new lightpath, accept the call
                    if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                        // Single-hop routing (end-to-end lightpath)
                        lps[0] = cp.getVT().getLightpath(id);
                        double centerFrequency = 192d * Math.pow(10,12) + (double)firstSlot[j]+ (double)(requiredSlots/2) * 12.5 * Math.pow(10,9);
                        int[] index = new int[links.length];
                        double SNR = 0;
                        for(k = 0; k < links.length; k++){
                            double[][] bandwidth = ((EONLink) cp.getPT().getLink(links[k])).getBW();
                            for (int a = 0; a < bandwidth[EONLink.numSlots][0]; a++){
                                if(bandwidth[a][1] == centerFrequency){
                                    index[k] = a;
                                }
                            }
                            SNR = SNR + ((EONLink) cp.getPT().getLink(links[k])).getSNR()[index[k]];
                        }
                        if (cp.acceptFlow(flow.getID(), lps) && SNR >= Modulation.getSNR(modulation)) {
                            return;
                        } else {
                            // Something wrong
                            // Dealocates the lightpath in VT and try again
                            cp.getVT().deallocatedLightpath(id);
                        }
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

    /**
     * Gets the node route in ArrayList object and retrieves in Array object
     * @param kpaths the Array ArrayList object
     * @param k the index of Array ArrayList object
     * @return the Array object
     */
    private int[] route(ArrayList<Integer>[] kpaths, int k) {
        if (kpaths[k] != null) {
            int[] path = new int[kpaths[k].size()];
            for (int i = 0; i < path.length; i++) {
                path[i] = kpaths[k].get(i);
            }
            return path;
        } else {
            return null;
        }
    }
}
