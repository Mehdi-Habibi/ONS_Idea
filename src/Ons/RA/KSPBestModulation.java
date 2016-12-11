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
public class KSPBestModulation implements RA {

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

            for(int mo = 7; mo > -1; mo--){     // testing modulations
                this.modulation = mo;
                // Calculates the required slots
                int requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), modulation);

                // First-Fit spectrum assignment in BPSK Ons.Modulation
                int[] firstSlot;
                for (int i = 0; i < 1; i++) {
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

                            int[] index = new int[links.length];
                            double SNR = 0;
                            for(k = 0; k < links.length; k++){
                                double[][] bandwidth = ((EONLink) cp.getPT().getLink(links[k])).getBW();
                                for (int a = 0; a < bandwidth[(EONLink.numSlots - 1)][0]; a++){     // finding the index of lightpath in the links
                                    if(bandwidth[a][2] == (double)firstSlot[j]){
                                        //System.out.println("firstSlot[j]: " + Integer.toString(firstSlot[j]));
                                        //System.out.println("firstSlot[j] (double): " + Double.toString((double)firstSlot[j]));
                                        //System.out.println("bandwidth[a][2]: " + Double.toString(bandwidth[a][2]));
                                        //System.out.println("a: " + Integer.toString(a));
                                        index[k] = a;
                                    }
                                }
                                double temp = ((EONLink) cp.getPT().getLink(links[k])).getSNR()[index[k]];
                                //System.out.println("temp: " + Double.toString(temp));
                                if(k == 0){
                                    SNR = temp;
                                }
                                else{
                                    SNR = (SNR * temp) / (SNR + temp);
                                }
                            }
                            //System.out.println("final SNR: " + Double.toString(SNR));
                            //System.out.println("Threshodl: " + Double.toString(Modulation.getSNR(modulation)));

                            if (cp.acceptFlow(flow.getID(), lps)) {
                                if(Modulation.getSNR(modulation) < SNR){
                                    return;
                                }
                            } else {
                                // Something wrong
                                // Dealocates the lightpath in VT and try again
                                cp.getVT().deallocatedLightpath(id);
                            }
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
