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
        this.modulation = Modulation._QPSK;
    }

    @Override
    public void flowArrival(Flow flow) {
        long counting = 0;
        double maxSNR = 0;
        int[] nodes;
        int[] links;
        long id;
        LightPath[] lps = new LightPath[1];

        int ksp = 1;

        ArrayList<Integer>[] kpaths = YenKSP.kShortestPaths(graph, flow.getSource(), flow.getDestination(), ksp);

        for (int k = 0; k < kpaths.length; k++) {
            nodes = route(kpaths,k);
            if (nodes.length == 0 || nodes == null) {
                cp.blockFlow(flow.getID());
                return;
            }
            links = new int[nodes.length - 1];
            for (int j = 0; j < nodes.length - 1; j++) {
                links[j] = cp.getPT().getLink(nodes[j], nodes[j + 1]).getID();
            }
            int requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), modulation);
            int[] firstSlot;
            firstSlot = ((EONLink) cp.getPT().getLink(links[0])).getSlotsAvailableToArray(requiredSlots);
            for (int j = 0; j < firstSlot.length; j++) {
                EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), links, firstSlot[j],
                                                                 (firstSlot[j] + requiredSlots - 1), modulation);
                if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                    lps[0] = cp.getVT().getLightpath(id);

                    EONLink[] usedLinks = new EONLink[links.length];
                    for (int xx = 0; xx < links.length; xx++){
                        usedLinks[xx] = (EONLink) cp.getPT().getLink(links[xx]);
                    }
                    SNR snr = new SNR(lp,usedLinks );
                    double lightpathSNR = snr.getLightPathSNR();
                    if(lightpathSNR > maxSNR){
                        maxSNR = lightpathSNR;
                    }
                    if (cp.MyacceptFlow(flow.getID(), lps)) {
                        counting++;
                        if(Modulation.getSNR(modulation) < lightpathSNR) {
                            cp.acceptFlow(flow.getID(), lps);
                            Main.sumAcceptedSNR += maxSNR;
                            return;
                        }
                        else{
                            cp.getVT().deallocatedLightpath(id);
                        }
                    } else {
                        cp.getVT().deallocatedLightpath(id);
                    }

                }
            }
        }
        if(counting > 0){
            Main.sumBlockedSNR += maxSNR;
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
