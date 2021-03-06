package Ons;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 * The Ons.Flow class defines an object that can be thought of as a flow
 * of data, going from a source node to a destination node. 
 * 
 * @author onsteam
 */
public class Flow {

    private long id;
    private int src;
    private int dst;
    private int bw;
    private int duration;
    private int cos;

    /**
     * Creates a new Ons.Flow object.
     * 
     * @param id            unique identifier
     * @param src           source node
     * @param dst           destination node
     * @param bw            bandwidth required (Mbps)
     * @param duration      duration time (seconds)
     * @param cos           class of service
     */
    public Flow(long id, int src, int dst, int bw, int duration, int cos) {
        if (id < 0 || src < 0 || dst < 0 || bw < 1 || duration < 0 || cos < 0) {
            throw (new IllegalArgumentException());
        } else {
            this.id = id;
            this.src = src;
            this.dst = dst;
            this.bw = bw;
            this.duration = duration;
            this.cos = cos;
        }
    }
    
    /**
     * Retrieves the unique identifier for a given Ons.Flow.
     * 
     * @return the value of the Ons.Flow's id attribute
     */
    public long getID() {
        return id;
    }
    
    /**
     * Retrieves the source node for a given Ons.Flow.
     * 
     * @return the value of the Ons.Flow's src attribute
     */
    public int getSource() {
        return src;
    }
    
    /**
     * Retrieves the destination node for a given Ons.Flow.
     * 
     * @return the value of the Ons.Flow's dst attribute
     */
    public int getDestination() {
        return dst;
    }
    
    /**
     * Retrieves the required bandwidth for a given Ons.Flow.
     * 
     * @return the value of the Ons.Flow's bw attribute.
     */
    public int getRate() {
        return bw;
    }
    
    /**
     * Assigns a new value to the required bandwidth of a given Ons.Flow.
     * 
     * @param bw new required bandwidth 
     */
    public void setRate(int bw) {
        this.bw = bw;
    }
    
    /**
     * Retrieves the duration time, in seconds, of a given Ons.Flow.
     * 
     * @return the value of the Ons.Flow's duration attribute
     */
    public int getDuration() {
        return duration;
    }
    
    /**
     * Retrieves a given Ons.Flow's "class of service".
     * A "class of service" groups together similar types of traffic
     * (for example, email, streaming video, voice,...) and treats
     * each type with its own level of service priority.
     * 
     * @return the value of the Ons.Flow's cos attribute
     */
    public int getCOS() {
        return cos;
    }
    
    /**
     * Prints all information related to a given Ons.Flow.
     * 
     * @return string containing all the values of the flow's parameters
     */
    @Override
    public String toString() {
        String flow = Long.toString(id) + ": " + Integer.toString(src) + "->" + Integer.toString(dst) + " rate: " + Integer.toString(bw) + " duration: " + Integer.toString(duration) + " cos: " + Integer.toString(cos);
        return flow;
    }
    
    /**
     * Creates a string with relevant information about the flow, to be
     * printed on the Trace file.
     * 
     * @return string with values of the flow's parameters
     */
    public String toTrace()
    {
    	String trace = Long.toString(id) + " " + Integer.toString(src) + " " + Integer.toString(dst) + " " + Integer.toString(bw) + " " + Integer.toString(duration) + " " + Integer.toString(cos);
    	return trace;
    }
}
