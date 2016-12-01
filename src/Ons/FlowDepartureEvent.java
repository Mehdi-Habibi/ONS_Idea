package Ons;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 * Methods to treat the outgoing of a Ons.Flow object.
 * 
 * @author onsteam
 */
public class FlowDepartureEvent extends Event{

    private long id;
    
    /**
     * Creates a new Ons.FlowDepartureEvent object.
     * 
     * @param id unique identifier of the outgoing flow
     */
    public FlowDepartureEvent(long id) {
        this.id = id;
    }
    
    /**
     * Retrieves the identifier of the Ons.FlowDepartureEvent object.
     * 
     * @return the Ons.FlowDepartureEvent's id attribute
     */
    public long getID() {
        return this.id;
    }
    
    /**
     * Prints all information related to the outgoing flow.
     * 
     * @return string containing all the values of the flow's parameters
     */
    @Override
    public String toString() {
        return "Departure: "+Long.toString(id);
    }
}
