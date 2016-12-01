package Ons;

/**
 * The WDM Optical Cross-Connects (EDMOXCs) can switch the optical signal 
 * coming in on a wavelenght of an input fiber link to the same wavelength
 * in an output fiber link. The Ons.WDMOXC may also switch the optical signal
 * on an incoming wavelength of an input fiber link to some other wavelength
 * on an output fiber link.
 * 
 * @author onsteam
 */
public class WDMOXC extends OXC {

    protected int wvlConverters;
    protected int freeWvlConverters;
    protected int wvlConversionRange;
    
    /**
     * Creates a new Ons.WDMOXC object. All its attributes must be given
     * given by parameter, except for the free grooming input and output
     * ports, that, at the beginning of the simulation, are the same as 
     * the total number of grooming input and output ports.
     * 
     * @param id The Ons.OXC's unique identifier
     * @param groomingInputPorts Total number of grooming input ports
     * @param groomingOutputPorts Total number of grooming output ports
     * @param wvlConverters Total number of wavelength converters
     * @param wvlConversionRange The range of wavelength conversion
     */
    public WDMOXC(int id, int groomingInputPorts, int groomingOutputPorts, int wvlConverters, int wvlConversionRange) {
        super(id, groomingInputPorts, groomingOutputPorts);
        this.wvlConverters = this.freeWvlConverters = wvlConverters;
        this.wvlConversionRange = wvlConversionRange;
    }

    /**
     * This function says whether or not a given Ons.OXC has free wavelength
     * converter(s).
     *
     * @return true if the Ons.OXC has free wavelength converter(s)
     */
    public boolean hasFreeWvlConverters() {
        return freeWvlConverters > 0;
    }

    /**
     * By decreasing the number of free wavelength converters, this function
     * "reserves" a wavelength converter.
     *
     * @return false if there are no free wavelength converters
     */
    public boolean reserveWvlConverter() {
        if (freeWvlConverters > 0) {
            freeWvlConverters--;
            return true;
        } else {
            return false;
        }
    }

    /**
     * By increasing the number of free wavelength converters, this function
     * "releases" a wavelength converters.
     *
     * @return false if there are no wavelength converters to be freed
     */
    public boolean releaseWvlConverter() {
        if (freeWvlConverters < wvlConverters) {
            freeWvlConverters++;
            return true;
        } else {
            return false;
        }
    }

    /**
     * This function provides the wavelength conversion range of a given Ons.OXC.
     *
     * @return the Ons.OXC's wvlConversionRange attribute
     */
    public int getWvlConversionRange() {
        return wvlConversionRange;
    }
}
