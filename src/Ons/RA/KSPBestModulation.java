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
        int maxModulation = 0;
        double slotNum = 0d;
        double rate = 0d;
        long counting = 0;
        int[] nodes;
        int[] links;
        long id;
        LightPath[] lps = new LightPath[1];

        // Set k=4
        int ksp = 1;

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
            rate = (double)flow.getRate() * Math.pow(10,6);
            slotNum = (rate / SNR.slotSize);
            //System.out.println("slotNum: " + Double.toString(slotNum));
            for(int vvv = (Modulation.N_MOD - 1); vvv > -1; vvv--){
                if(vvv == (Modulation.N_MOD - 1)){
                    if(slotNum >= (vvv)){
                        maxModulation = vvv;
                        break;
                    }
                }else{
                    if((slotNum >= (vvv)) && (slotNum < (vvv + 1))){
                        maxModulation = vvv;
                        break;
                    }
                }
            }
            //System.out.println("maxModulation: " + Integer.toString(maxModulation));
            for(int mod = maxModulation; mod > -1; mod--){
                this.modulation = mod;
                // Calculates the required slots
                int requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), modulation);
                // First-Fit spectrum assignment in BPSK Ons.Modulation
                int[] firstSlot;
                // Try the slots available in each link
                firstSlot = ((EONLink) cp.getPT().getLink(links[0])).getSlotsAvailableToArray(requiredSlots);
                for (int j = 0; j < firstSlot.length; j++) {
                    // Now you create the lightpath to use the createLightpath VT
                    EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), links, firstSlot[j],
                            (firstSlot[j] + requiredSlots - 1), modulation);
                    // Now you try to establish the new lightpath, accept the call
                    if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                        // Single-hop routing (end-to-end lightpath)
                        lps[0] = cp.getVT().getLightpath(id);

                        EONLink[] usedLinks = new EONLink[links.length];
                        for (int xx = 0; xx < links.length; xx++){
                            usedLinks[xx] = (EONLink) cp.getPT().getLink(links[xx]);
                        }

                        SNR snr = new SNR(lp,usedLinks );
                        double lightpathSNR = snr.getLightPathSNR();
                        //System.out.println("lightpathSNR: "+Double.toString(lightpathSNR));

                        if (cp.MyacceptFlow(flow.getID(), lps)) {
                            counting++;
                            if(Modulation.getSNR(modulation) < lightpathSNR) {
                                cp.acceptFlow(flow.getID(), lps);
                                return;
                            }
                            else{
                                cp.getVT().deallocatedLightpath(id);
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
        // Block the call
        if(counting > 0){
            cp.SNRblockFlow(flow.getID());
        }
        else{
            cp.blockFlow(flow.getID());
        }
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
