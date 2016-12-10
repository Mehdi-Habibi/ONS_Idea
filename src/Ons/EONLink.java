package Ons;

import Ons.Util.MathFunctions;

import java.util.TreeSet;
import java.util.function.DoubleToIntFunction;

/**
 * The Elastic Optical Network (EON) Ons.Link represents a Fiberlink in an optical
 * network divided by slots.
 *
 * @author onsteam
 */
public class EONLink extends Link {

    public static int numSlots;
    public static int numberofSlots;
    protected long slots[];
    protected static int guardband;
    protected double slotSize = 12.5 * Math.pow(10,9);



    public EONLink(int id, int src, int dst, double delay, double weight, int numSlots, int guardband) {
        super(id, src, dst, delay, weight);
        this.numSlots = numSlots;
        this.slots = new long[this.numSlots];
        for (int i = 0; i < this.numSlots; i++) {
            this.slots[i] = 0;
        }
        this.guardband = guardband;
    }

    /**
     * Retrieves the guardband size.
     *
     * @return the guardband size
     */
    public int getGuardband() {
        return this.guardband;
    }

    /**
     * Retrieves the number of slots in this link.
     *
     * @return the number of slots in this link
     */
    public int getNumSlots() {
        return numSlots;
    }

    /**
     * Retrieves the slots available in this link.
     *
     * @return the number slots available
     */
    public int getAvaiableSlots() {
        int cont = 0;
        for (int i = 0; i < this.slots.length; i++) {
            if (this.slots[i] == 0) {
                cont++;
            }
        }
        return cont;
    }
    
    /**
     * Retrieves the used slots in this link.
     *
     * @return the number used slots
     */
    public int getUsedSlots() {
        return numSlots - getAvaiableSlots();
    }

    /**
     * Check if there is available slots to accommodate the request.
     *
     * @param requiredSlots the request slots
     * @return true if can be accommodate, false otherwise
     */
    public boolean hasSlotsAvaiable(int requiredSlots) {
        if (requiredSlots > this.slots.length) {
            throw (new IllegalArgumentException());
        }
        int i = 0, cont = 0;
        while (i < this.slots.length) {
            if (this.slots[i] == 0) {
                cont++;
                if (cont == requiredSlots) {
                    return true;
                }
                i++;
            } else {
                cont = 0;
                i++;
            }
        }
        return false;
    }

    /**
     * Retrieves the first slot available to accommodate this requisition.
     *
     * @param requiredSlots the required slots
     * @return the first slot available to accommodate this requisition
     */
    public int getFirstSlotAvailable(int requiredSlots) {
        if (requiredSlots > this.slots.length) {
            throw (new IllegalArgumentException());
        }
        int i = 0, cont = 0;
        while (i < this.slots.length) {
            if (this.slots[i] == 0) {
                cont++;
                if (cont == requiredSlots) {
                    return i - requiredSlots + 1;
                }
                i++;
            } else {
                cont = 0;
                i++;
            }
        }
        return -1;
    }

    /**
     * Retrieves the set of slots available given a minimum size.
     *
     * @param requiredSlots the required slots of set
     * @return the set with first slots available to 'requiredSlots'
     */
    public TreeSet<Integer> getSlotsAvailable(int requiredSlots) {//olha todos os espacos disponiveis levando em cosideracao a banda de guarda
        TreeSet<Integer> slotsAvailable = new TreeSet<>();
        for (int i = 0; i <= this.slots.length - requiredSlots; i++) {
            if (this.slots[i] == 0) {
                if (areSlotsAvaiable(i, i + requiredSlots - 1)) {
                    slotsAvailable.add(i);
                }
            }
        }
        return slotsAvailable;
    }
    
    /**
     * Retrieves the set of slots available given a minimum size [but in array
     * of 'int'].
     *
     * @param requiredSlots the required slots of set
     * @return the array with first slots available to 'requiredSlots'
     */
    public int[] getSlotsAvailableToArray(int requiredSlots) {
        TreeSet<Integer> slotsAvailable = getSlotsAvailable(requiredSlots);
        int[] out = new int[slotsAvailable.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = slotsAvailable.pollFirst();
        }
        return out;
    }

    /**
     * Checks for available slots considering the guard band.
     *
     * @param begin the begin slot
     * @param end the end slot
     * @return true if is avaiable slots, false otherwise
     */
    public boolean areSlotsAvaiable(int begin, int end) {
        if (begin < 0 || end >= this.slots.length || begin > end) {
            throw (new IllegalArgumentException());
        }
        if (begin < this.guardband) {
            while (begin <= end) {
                if (this.slots[begin] == 0) {
                    begin++;
                } else {
                    return false;
                }
            }
            for (int i = 0; i < this.guardband; i++) {
                if (this.slots[begin] == -1 || this.slots[begin] == 0) {
                    begin++;
                } else {
                    return false;
                }
            }
            return true;
        } else {
            if (end > (this.slots.length - 1) - this.guardband) {
                for (int i = (begin - this.guardband); i < begin; i++) {
                    if (!(this.slots[i] == -1 || this.slots[i] == 0)) {
                        return false;
                    }
                }
                while (begin <= end) {
                    if (this.slots[begin] == 0) {
                        begin++;
                    } else {
                        return false;
                    }
                }
                return true;
            } else {
                for (int i = (begin - this.guardband); i < begin; i++) {
                    if (!(this.slots[i] == -1 || this.slots[i] == 0)) {
                        return false;
                    }
                }
                while (begin <= end) {
                    if (this.slots[begin] == 0) {
                        begin++;
                    } else {
                        return false;
                    }
                }
                for (int i = 0; i < this.guardband; i++) {
                    if (this.slots[begin] == -1 || this.slots[begin] == 0) {
                        begin++;
                    } else {
                        return false;
                    }
                }
                return true;
            }
        }
    }

    /**
     * Reserve slots (with guard band) in this link, ie reserve lightpath.
     *
     * @param id the id of lightpath reserved
     * @param begin the begin of lightpath
     * @param end the end of lightpath
     */
    public void reserveSlots(long id, int begin, int end) {
        if (begin < 0 || end >= this.slots.length || begin > end) {
            throw (new IllegalArgumentException());
        }
        if (begin < this.guardband) {
            while (begin <= end) {
                this.slots[begin] = id;
                begin++;
            }
            for (int i = 0; i < this.guardband; i++) {
                this.slots[begin] = -1;
                begin++;
            }
        } else {
            if (end > (this.slots.length - 1) - this.guardband) {
                for (int i = (begin - this.guardband); i < begin; i++) {
                    this.slots[i] = -1;
                }
                while (begin <= end) {
                    this.slots[begin] = id;
                    begin++;
                }
            } else {
                for (int i = (begin - this.guardband); i < begin; i++) {
                    this.slots[i] = -1;
                }
                while (begin <= end) {
                    this.slots[begin] = id;
                    begin++;
                }
                for (int i = 0; i < this.guardband; i++) {
                    this.slots[begin] = -1;
                    begin++;
                }
            }
        }
    }

    /**
     * Release slots from this link. Examines the corresponding guard bands
     *
     * @param begin the begin
     * @param end the end
     */
    public void releaseSlots(int begin, int end) {
        if (begin < 0 || end >= this.slots.length || begin > end) {
            throw (new IllegalArgumentException());
        }
        if (begin < this.guardband) {
            while (begin <= end) {
                this.slots[begin] = 0;
                begin++;
            }
            if (this.slots[begin + this.guardband] == 0) {
                for (int i = 0; i < this.guardband; i++) {
                    this.slots[begin] = 0;
                    begin++;
                }
            } else {
                if (this.slots[begin + this.guardband] == -1) {
                    int k = begin + this.guardband;
                    while (this.slots[k] == -1) {
                        k++;
                    }
                    int tirar = k - (begin + this.guardband);
                    for (int i = 0; i < tirar; i++) {
                        this.slots[begin] = 0;
                        begin++;
                    }
                }
            }
        } else {
            if (end > (this.slots.length - 1) - this.guardband) {
                if (this.slots[begin - this.guardband - 1] == 0) {
                    for (int i = (begin - this.guardband); i < begin; i++) {
                        this.slots[i] = 0;
                    }
                }
                if (this.slots[begin - this.guardband - 1] == -1) {
                    int k = begin - this.guardband - 1;
                    while (this.slots[k] == -1) {
                        k--;
                    }
                    int tirar = (begin - this.guardband - 1) - k;
                    for (int i = (begin - tirar); i < begin; i++) {
                        this.slots[i] = 0;
                    }
                }
                while (begin <= end) {
                    this.slots[begin] = 0;
                    begin++;
                }
            } else {
                if (begin == this.guardband) {
                    for (int i = (begin - this.guardband); i < begin; i++) {
                        this.slots[i] = 0;
                    }
                } else {
                    if (this.slots[begin - this.guardband - 1] == 0) {
                        for (int i = (begin - this.guardband); i < begin; i++) {
                            this.slots[i] = 0;
                        }
                    }
                    if (this.slots[begin - this.guardband - 1] == -1) {
                        int k = begin - this.guardband - 1;
                        while (this.slots[k] == -1) {
                            k--;
                        }
                        int tirar = (begin - this.guardband - 1) - k;
                        for (int i = (begin - tirar); i < begin; i++) {
                            this.slots[i] = 0;
                        }
                    }
                }
                while (begin <= end) {
                    this.slots[begin] = 0;
                    begin++;
                }
                if (end == this.slots.length - 1 - this.guardband) {
                    for (int i = 0; i < this.guardband; i++) {
                        this.slots[begin] = 0;
                        begin++;
                    }
                } else {
                    if (this.slots[begin + this.guardband] == 0) {
                        for (int i = 0; i < this.guardband; i++) {
                            this.slots[begin] = 0;
                            begin++;
                        }
                    } else {
                        if (this.slots[begin + this.guardband] == -1) {
                            int k = begin + this.guardband;
                            while (this.slots[k] == -1) {
                                k++;
                            }
                            int tirar = k - (begin + this.guardband);
                            for (int i = 0; i < tirar; i++) {
                                this.slots[begin] = 0;
                                begin++;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Retrieves the max size of contiguous slots available.
     *
     * @return the max size of contiguous slots available
     */
    public int maxSizeAvaiable() {
        int cont = 0, max = 0;
        for (int i = 0; i < this.slots.length; i++) {
            if (this.slots[i] == 0) {
                cont++;
            } else {
                if (cont > max) {
                    max = cont;
                }
                cont = 0;
            }
        }
        if (cont > max) {
            return cont;
        }
        return max;
    }

    /**
     * Retrieves the minimun size of contiguous slots available.
     *
     * @return the minimun size of contiguous slots available
     */
    public int minSizeAvaiable() {
        int cont = 0, min = this.slots.length;
        boolean flag = true;
        for (int i = 0; i < this.slots.length; i++) {
            if (this.slots[i] == 0) {
                cont++;
                flag = true;
            } else {
                if (cont < min && flag) {
                    min = cont;
                    flag = false;
                }
                cont = 0;
            }
        }
        if (cont < min && flag) {
            return cont;
        }
        return min;
    }

    /**
     * Check if this lightpath is this link
     *
     * @param id the id of lightpath
     * @return if it is true, false otherwise
     */
    public boolean hasThisLightPath(long id) {
        if (id < 0) {
            throw (new IllegalArgumentException());
        }
        int i = 0;
        while (i < this.slots.length) {
            if (this.slots[i] == id) {
                return true;
            } else {
                i++;
            }
        }
        return false;
    }

    /**
     * Retrieves the information of this link
     *
     * @return the string information
     */
    @Override
    public String toString() {
        String link = Long.toString(id) + ": " + Integer.toString(src) + "->" + Integer.toString(dst) + " delay:" + Double.toString(delay) + "; slots:" + this.slots.length + "; weight:" + Double.toString(weight);
        return link;
    }

    /**
     * Print this link for debbug.
     */
    public void printLink() {
        System.out.print("\n ID:" + this.id + "_" + this.src + "->" + this.dst + " |");
        for (int i = 0; i < this.slots.length; i++) {
            System.out.print(this.slots[i] + "|");
        }
        System.out.println("");
    }

    /**
     * Retrieves the lightpath slot position.
     *
     * @param lp the id of lightpath
     * @return the array[2] with begin position and end position
     */
    public int[] findLightpath(long lp) {
        if (lp <= 0) {
            throw (new IllegalArgumentException());
        }
        int[] position = {-1, -1};
        int i;
        boolean b_flag = true, e_flag = false, flag = false;
        for (i = 0; i < numSlots; i++) {
            if (e_flag) {
                position[1] = i - 1;
                e_flag = false;
            }
            if (slots[i] == lp) {
                if (b_flag) {
                    position[0] = i;
                    b_flag = false;
                }
                e_flag = true;
                flag = true;
            }
            if(flag && !e_flag){
                break;
            }
        }
        if (e_flag) {
            position[1] = i - 1;
        }
        return position;
    }

    /**
     * Retrieves the number of free ranges to allocate these required slots
     * @param requiredSlots the number of required slots
     * @return the number of possibilities to allocate these required slots
     */
    public int rangeFree(int requiredSlots) {
        if (requiredSlots > this.slots.length) {
            throw (new IllegalArgumentException());
        }
        int i = 0, cont = 0, contPossibles = 0;
        while (i < this.slots.length) {
            if (this.slots[i] == 0) {
                cont++;
                if (cont == requiredSlots) {
                    contPossibles++;
                    cont = 0;
                }
                i++;
            } else {
                cont = 0;
                i++;
            }
        }
        return contPossibles;
    }

    /**
     * Retrieves the bandwidth of channels
     *
     * @return the bandwidth (Hz), center frequency (Hz) of each channel and number of channels in the link
     * bandLength[][0] is for bandwidths, bandLength[][1] is for center frequencies, bandLength[][2] is for
     * first slot index
     */
    public double[][] getBW(){
        int chNum = -1;
        double bandLength[][] = new double[numSlots][3];
        for (int a = 0; a < numSlots; a++) {
                bandLength[a][0] = 0d;
                bandLength[a][1] = 0d;
                bandLength[a][2] = 0d;
        }
        for (int i = 0; i < numSlots; i++) {
            if (slots[i] != 0 && slots[i] != -1){
                if(i == 0 || slots[i - 1] == 0 || slots[i - 1] == -1){
                    chNum++;
                    bandLength[chNum][1] = 192d * Math.pow(10,12)+ slotSize * (double)i;
                    //System.out.println("initial value: "+Double.toString(bandLength[chNum][1]));
                    bandLength[chNum][2] = (double)i;
                }
                bandLength[chNum][0] = bandLength[chNum][0] + slotSize;
                bandLength[chNum][1] = bandLength[chNum][1] + (slotSize/2d);
            }
        }
        bandLength[numSlots - 1][0] = (double)chNum + 1d;
        return bandLength;
    }

    /**
     * Retrieves the SNR of channels
     * Based on: A Detailed Analytical Derivation of the GN Model of Non-Linear Interference in Coherent
     *           Optical Transmission Systems (pages 18 & 19 for nonlinear part of noise)
     * Authors: Pierluigi Poggiolini, Gabriella Bosco, Andrea Carena, Vittorio Curri, Yanchao Jiang and
     *          Fabrizio Forghieri
     *
     * @return the SNR of each channel
     */
    public double[] getSNR(){
        double alpha = ((0.22)/1000d)/8.685889638;     // =2.5328e-5 Np/m (= 0.22 d/km), 1 Np = 8.685889638 dB
        //System.out.println("alpha: "+Double.toString(alpha));

        double ls = 80000d;
        //System.out.println("ls: "+Double.toString(ls));

        double beta2 = 21.7 * Math.pow(10,-27);
        //System.out.println("beta2: "+Double.toString(beta2));

        double gamma = 1.3 * Math.pow(10,-3);
        //System.out.println("gamma: "+Double.toString(gamma));

        double nsp = 1.8;
        //System.out.println("nsp: "+Double.toString(nsp));

        double[][] BW = getBW();     // bandwidth and center frequencies

        int channelNum = (int)BW[numSlots - 1][0];     // number of channels
        //System.out.println("channelNum: "+Integer.toString(channelNum));

        double pi = 3.14159265359;     // pi!
        //System.out.println("pi: "+Double.toString(pi));

        double h = 6.62607004 * Math.pow(10,-34);     // Plank constant
        //System.out.println("h: "+Double.toString(h));

        double e = 2.71828182845;     // Euler's number
        //System.out.println("e: "+Double.toString(e));

        double L = getWeight() * 1000d;     // fiber length
        //System.out.println("L: "+Double.toString(L));

        double Ns = Math.ceil(L / ls);     // number of spans
        //System.out.println("Ns: "+Double.toString(Ns));

        double leff = (1d - Math.pow(e,-2d*alpha*ls))/(2d*alpha);     // effective length
        //System.out.println("leff: "+Double.toString(leff));

        double power = -3d;     // power of channels (dB)
        //System.out.println("power: "+Double.toString(power));

        double[][] psi = new double[channelNum][channelNum];     // psi in nonlinear part of noise
        double den = (2d * pi * beta2)/(2d * alpha);     // denominator of psi = 2.691542e-21
        //System.out.println("den: "+Double.toString(den));

        double num1,num2;     // two parts of nominator of psi for j!=k
        double temp;

        double[] G = new double[channelNum];     // PSD of channels
        double[] Gnli = new double[channelNum];     // PSD of nonlinear part of noise
        double[] GASE = new double[channelNum];     // PSD of ASE noise
        double[] SNR = new double[channelNum];     // SNR vector for channels

        for(int i = 0; i < channelNum; i++){
            G[i] = Math.pow(10, (power - 30d)/10d)/BW[i][0];
            //System.out.println("G[i]: "+Double.toString(G[i]));
            //System.out.println("BW[i][0]: "+Double.toString(BW[i][0]));
            Gnli[i] = 0d;
            GASE[i] = Ns * (Math.pow(e,alpha*ls) - 1d) * nsp * h * BW[i][1];     // Ns is needed
            //System.out.println("center frequency: "+Double.toString(BW[i][1]));
            //System.out.println("exp: "+Double.toString((Math.pow(e,alpha*ls) - 1d)));
            //System.out.println("GASE:"+Double.toString(GASE[i]));

        }

        for (int k = 0; k < channelNum; k++) {
            for(int j = 0; j < channelNum; j++){
                if(j == k){
                    psi[j][j] = (Ns * MathFunctions.asinh((pi * pi / 2d) * (1d / (2d * alpha)) * beta2 * BW[j][0] * BW[j][0]))/den;
                    //System.out.println("psi[j][j]:"+Double.toString(psi[j][j]));
                    //System.out.println("asinh:"+Double.toString(MathFunctions.asinh(100)));     // testing asinh()
                    //System.out.println("psi[j][j]*den:"+Double.toString(psi[j][j] * den));

                    temp = (2d) * Math.pow((gamma * leff),2) * G[k] * G[j] * G[j] * psi[j][k];
                    //System.out.println("temp(k,k):"+Double.toString(temp));

                    Gnli[k] = Gnli[k] + temp;
                    //System.out.println("Gnli[k]:"+Double.toString(Gnli[k]));
                    temp = 0d;
                }
                else{
                    num1 = Ns * MathFunctions.asinh((pi * pi) * (1d / (2d * alpha)) * beta2 * (BW[j][1] - BW[k][1] + (BW[j][0]/2d)) * BW[k][0]);
                    //System.out.println("num1:"+Double.toString(num1));

                    num2 = Ns * MathFunctions.asinh((pi * pi) * (1d / (2d * alpha)) * beta2 * (BW[j][1] - BW[k][1] - (BW[j][0]/2d)) * BW[k][0]);
                    //System.out.println("num2:"+Double.toString(num2));

                    //System.out.println("num1-num2:"+Double.toString(num1 - num2));

                    psi[j][k] = (num1 - num2) / (2d * den);
                    //System.out.println("psi[j][k]:"+Double.toString(psi[j][k]));

                    temp = (2d) * Math.pow((gamma * leff),2) * G[k] * G[j] * G[j] * psi[j][k] * 2d;
                    //System.out.println("temp(j,k):"+Double.toString(temp));

                    Gnli[k] = Gnli[k] + temp;
                    temp = 0d;
                }
            }
            //System.out.println("Gnli[k]: "+Double.toString(Gnli[k]));



        }
        for(int i = 0; i < channelNum; i++){
            SNR[i] =G[i] / (Gnli[i] + GASE[i]);
            //System.out.println("G[i]: " + Double.toString(G[i]));
            //System.out.println("(Gnli[i] + GASE[i]): " + Double.toString((Gnli[i] + GASE[i])));

            /*
            if(SNR[i]<12) {
                System.out.println("SNR[i]: " + Double.toString(SNR[i]));
                System.out.println("Gnli[i]: " + Double.toString(Gnli[i]));
                System.out.println("i: " + Integer.toString(i));
                System.out.println("Ns: " + Double.toString(Ns));
            }
            */
        }
        return SNR;
    }


}
