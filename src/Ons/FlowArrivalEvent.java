package Ons;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 * Methods to treat the incoming of a Ons.Flow object.
 * 
 * @author onsteam
 */
public class FlowArrivalEvent extends Event {
	
    private Flow flow;
    
    /**
     * Creates a new Ons.FlowArrivalEvent object.
     * 
     * @param flow the arriving flow
     */
    public FlowArrivalEvent(Flow flow) {
        this.flow = flow;
    }
    
    /**
     * Retrives the flow attribute of the Ons.FlowArrivalEvent object.
     * 
     * @return the Ons.FlowArrivalEvent's flow attribute
     */
    public Flow getFlow() {
        return this.flow;
    }
    
    /**
     * Prints all information related to the arriving flow.
     * 
     * @return string containing all the values of the flow's parameters
     */
    @Override
    public String toString() {
        return "Arrival: "+flow.toString();
    }
}
