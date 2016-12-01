/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Ons.RA;


import Ons.*;

/**
 * This is the interface that provides some methods for the Ons.RA class.
 * These methods basically deal with the simulation interface and with
 the arriving and departing flows.
 
 The Routing Assignment (Ons.RA) is a optical networking problem
 that has the goal of maximizing the number of optical connections.
 * 
 * @author onsteam
 */
public interface RA {
    
    public void simulationInterface(ControlPlaneForRA cp);

    public void flowArrival(Flow flow);
    
    public void flowDeparture(long id);

}
