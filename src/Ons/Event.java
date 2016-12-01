package Ons;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 * Ons.Event objects have only one attribute: scheduled time.
 * It is the time slot the event is set to happen.
 * @author onsteam
 */
public abstract class Event {
    
    private double time;
   
    /**
     * Sets a new time for the Ons.Event to happen.
     * 
     * @param time new scheduled period
     */
    public void setTime(double time){
        this.time = time;
    }
    
    /**
     * Retrieves current scheduled time for a given Ons.Event.
     * 
     * @return value of the Ons.Event's time attribute
     */
    public double getTime() {
        return this.time;
    }
}
