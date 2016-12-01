package Ons;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import Ons.RA.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The Control Plane is responsible for managing resources and
 * connection within the network.
 * 
 * @author onsteam
 */
public class ControlPlane implements ControlPlaneForRA { // Ons.RA is Routing Assignment Problem

    private RA ra;
    private PhysicalTopology pt;
    private VirtualTopology vt;
    private Map<Flow, Path> mappedFlows; // Flows that have been accepted into the network
    private Map<Long, Flow> activeFlows; // Flows that have been accepted or that are waiting for a Ons.RA decision
    private Tracer tr = Tracer.getTracerObject();
    private MyStatistics st = MyStatistics.getMyStatisticsObject();

    /**
     * Creates a new Ons.ControlPlane object.
     * 
     * @param raModule the name of the Ons.RA class
     * @param pt the network's physical topology
     * @param vt the network's virtual topology
     */
    public ControlPlane(String raModule, PhysicalTopology pt, VirtualTopology vt) {
        Class RAClass;

        mappedFlows = new HashMap<Flow, Path>();
        activeFlows = new HashMap<Long, Flow>();

        this.pt = pt;
        this.vt = vt;

        try {
            RAClass = Class.forName(raModule);
            ra = (RA) RAClass.newInstance();
            ra.simulationInterface(this);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    /**
     * Deals with an Ons.Event from the event queue.
     * If it is of the Ons.FlowArrivalEvent kind, adds it to the list of active flows.
     * If it is from the Ons.FlowDepartureEvent, removes it from the list.
     * 
     * @param event the Ons.Event object taken from the queue
     */
    public void newEvent(Event event) {

        if (event instanceof FlowArrivalEvent) {
            newFlow(((FlowArrivalEvent) event).getFlow());
            ra.flowArrival(((FlowArrivalEvent) event).getFlow());
        } else if (event instanceof FlowDepartureEvent) {
            ra.flowDeparture(((FlowDepartureEvent) event).getID());
            removeFlow(((FlowDepartureEvent) event).getID());
        }
    }

    /**
     * Adds a given active Ons.Flow object to a determined Physical Topology.
     * 
     * @param id unique identifier of the Ons.Flow object
     * @param lightpaths the Ons.Path, or list of LighPath objects
     * @return true if operation was successful, or false if a problem occurred
     */
    @Override
    public boolean acceptFlow(long id, LightPath[] lightpaths) {
        Flow flow;

        if (id < 0 || lightpaths.length < 1) {
            throw (new IllegalArgumentException());
        } else {
            if (!activeFlows.containsKey(id)) {
                return false;
            }
            flow = activeFlows.get(id);
            if (!canAddFlowToPT(flow, lightpaths)) {
                return false;
            }
            if(!checkLightpathContinuity(flow, lightpaths)){
                return false;
            }
            int usedTransponders = 0;
            for (LightPath lightpath : lightpaths) {
                if(vt.isLightpathIdle(lightpath.getID())){
                    usedTransponders++;
                }
            }
            addFlowToPT(flow, lightpaths);
            mappedFlows.put(flow, new Path(lightpaths));
            tr.acceptFlow(flow, lightpaths);
            st.userTransponder(usedTransponders);
            st.acceptFlow(flow, lightpaths);
            return true;
        }
    }

    /**
     * Removes a given Ons.Flow object from the list of active flows.
     * 
     * @param id unique identifier of the Ons.Flow object
     * @return true if operation was successful, or false if a problem occurred
     */
    @Override
    public boolean blockFlow(long id) {
        Flow flow;

        if (id < 0) {
            throw (new IllegalArgumentException());
        } else {
            if (!activeFlows.containsKey(id)) {
                return false;
            }
            flow = activeFlows.get(id);
            if (mappedFlows.containsKey(flow)) {
                return false;
            }
            activeFlows.remove(id);
            tr.blockFlow(flow);
            st.blockFlow(flow);
            return true;
        }
    }
    
    /**
     * Removes a given Ons.Flow object from the Physical Topology and then
     * puts it back, but with a new route (set of Ons.LightPath objects).
     * 
     * @param id unique identifier of the Ons.Flow object
     * @param lightpaths list of Ons.LightPath objects, which form a Ons.Path
     * @return true if operation was successful, or false if a problem occurred
     */
    @Override
    public boolean rerouteFlow(long id, LightPath[] lightpaths) {
        Flow flow;
        Path oldPath;

        if (id < 0 || lightpaths.length < 1) {
            throw (new IllegalArgumentException());
        } else {
            if (!activeFlows.containsKey(id)) {
                return false;
            }
            flow = activeFlows.get(id);
            if (!mappedFlows.containsKey(flow)) {
                return false;
            }
            oldPath = mappedFlows.get(flow);
            removeFlowFromPT(flow, lightpaths);
            if (!canAddFlowToPT(flow, lightpaths)) {
                addFlowToPT(flow, oldPath.getLightpaths());
                return false;
            }
            if(!checkLightpathContinuity(flow, lightpaths)){
                return false;
            }
            addFlowToPT(flow, lightpaths);
            mappedFlows.put(flow, new Path(lightpaths));
            //tr.flowRequest(id, true);
            return true;
        }
    }
    
    /**
     * Adds a given Ons.Flow object to the HashMap of active flows.
     * The HashMap also stores the object's unique identifier (ID). 
     * 
     * @param flow Ons.Flow object to be added
     */
    private void newFlow(Flow flow) {
        activeFlows.put(flow.getID(), flow);
    }
    
    /**
     * Removes a given Ons.Flow object from the list of active flows.
     * 
     * @param id the unique identifier of the Ons.Flow to be removed
     */
    private void removeFlow(long id) {
        Flow flow;
        LightPath[] lightpaths;

        if (activeFlows.containsKey(id)) {
            flow = activeFlows.get(id);
            if (mappedFlows.containsKey(flow)) {
                lightpaths = mappedFlows.get(flow).getLightpaths();
                removeFlowFromPT(flow, lightpaths);
                mappedFlows.remove(flow);
            }
            activeFlows.remove(id);
        }
    }
    
    /**
     * Removes a given Ons.Flow object from a Physical Topology.
     * 
     * @param flow the Ons.Flow object that will be removed from the PT
     * @param lightpaths a list of LighPath objects
     */
    private void removeFlowFromPT(Flow flow, LightPath[] lightpaths) {
        for (LightPath lightpath : lightpaths) {
            pt.removeFlow(flow, lightpath);
            // Can the lightpath be removed?
            if (vt.isLightpathIdle(lightpath.getID())) {
                vt.removeLightPath(lightpath.getID());
            }
        }
    }
    
    /**
     * Says whether or not a given Ons.Flow object can be added to a
     * determined Physical Topology, based on the amount of bandwidth the
     * flow requires opposed to the available bandwidth.
     * 
     * @param flow the Ons.Flow object to be added
     * @param lightpaths list of Ons.LightPath objects the flow uses
     * @return true if Ons.Flow object can be added to the PT, or false if it can't
     */
    private boolean canAddFlowToPT(Flow flow, LightPath[] lightpaths) {
        for (LightPath lightpath : lightpaths) {
            if (!pt.canAddFlow(flow, lightpath)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Adds a Ons.Flow object to a Physical Topology.
     * This means adding the flow to the network's traffic,
     * which simply decreases the available bandwidth.
     * 
     * @param flow the Ons.Flow object to be added
     * @param lightpaths list of Ons.LightPath objects the flow uses
     */
    private void addFlowToPT(Flow flow, LightPath[] lightpaths) {
        for (LightPath lightpath : lightpaths) {
            pt.addFlow(flow, lightpath);
        }
    }
    
    /**Checks the lightpaths continuity in multihop and if flow src and dst is equal in lightpaths
     * 
     * @param flow the flow requisition
     * @param lightpaths the set of lightpaths
     * @return true if evething is ok, false otherwise
     */
    private boolean checkLightpathContinuity(Flow flow, LightPath[] lightpaths) {
        if(flow.getSource() == lightpaths[0].getSource() && flow.getDestination() == lightpaths[lightpaths.length-1].getDestination()){
            for (int i = 0; i < lightpaths.length - 1; i++) {
                if(!(lightpaths[i].getDestination() == lightpaths[i+1].getSource())){
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Retrieves a Ons.Path object, based on a given Ons.Flow object.
     * That's possible thanks to the HashMap mappedFlows, which
     * maps a Ons.Flow to a Ons.Path.
     * 
     * @param flow Ons.Flow object that will be used to find the Ons.Path object
     * @return Ons.Path object mapped to the given flow
     */
    @Override
    public Path getPath(Flow flow) {
        return mappedFlows.get(flow);
    }
    
    /**
     * Retrieves the complete set of Ons.Flow/Ons.Path pairs listed on the
     * mappedFlows HashMap.
     * 
     * @return the mappedFlows HashMap
     */
    @Override
    public Map<Flow, Path> getMappedFlows() {
        return mappedFlows;
    }
    
    /**
     * Retrieves a Ons.Flow object from the list of active flows.
     * 
     * @param id the unique identifier of the Ons.Flow object
     * @return the required Ons.Flow object
     */
    @Override
    public Flow getFlow(long id) {
        return activeFlows.get(id);
    }
    
    /**
     * Counts number of times a given Ons.LightPath object
     * is used within the Ons.Flow objects of the network.
     * 
     * @param id unique identifier of the Ons.LightPath object
     * @return integer with the number of times the given Ons.LightPath object is used
     */
    @Override
    public int getLightpathFlowCount(long id) {
        int num = 0;
        Path p;
        LightPath[] lps;
        ArrayList<Path> ps = new ArrayList<>(mappedFlows.values());
        for (Path p1 : ps) {
            p = p1;
            lps = p.getLightpaths();
            for (LightPath lp : lps) {
                if (lp.getID() == id) {
                    num++;
                    break;
                }
            }
        }
        return num;
    }
    
    /**
     * Retrieves the Ons.PhysicalTopology object
     * @return Ons.PhysicalTopology object
     */
    @Override
    public PhysicalTopology getPT(){
        return pt;
    }
    
    /**
     * Retrieves the Ons.VirtualTopology object
     * @return Ons.VirtualTopology object
     */
    @Override
    public VirtualTopology getVT(){
        return vt;
    }
    
    /**
     * Creates a WDM Ons.LightPath candidate to put in the Virtual Topology (this method should be used by Ons.RA classes)
     * @param src the source node of the lightpath
     * @param dst the destination node of the lightpath
     * @param links the id links used by lightpath
     * @param wavelengths the wavelengths used by lightpath
     * @return the Ons.WDMLightPath object
     */
    @Override
    public WDMLightPath createCandidateWDMLightPath(int src, int dst, int[] links, int[] wavelengths) {
        return new WDMLightPath(1, src, dst, links, wavelengths);
    }

    /**
     * Creates a EON Ons.LightPath candidate to put in the Virtual Topology (this method should be used by Ons.RA classes)
     * @param src the source node of the lightpath
     * @param dst the destination node of the lightpath
     * @param links the id links used by lightpath
     * @param firstSlot the first slot used in this lightpath
     * @param lastSlot the last slot used in this lightpath
     * @param modulation the modulation id used in this lightpath
     * @return the Ons.EONLightPath object
     */
    @Override
    public EONLightPath createCandidateEONLightPath(int src, int dst, int[] links, int firstSlot, int lastSlot, int modulation) {
        return new EONLightPath(1, src, dst, links, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
    }
}
