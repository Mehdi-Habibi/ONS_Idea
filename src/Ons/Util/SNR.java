package Ons.Util;

import Ons.EONLightPath;
import Ons.EONLink;

/**
 * Based on: A Detailed Analytical Derivation of the GN Model of Non-Linear Interference in Coherent
 *           Optical Transmission Systems (pages 18 & 19 for nonlinear part of noise)
 * Authors: Pierluigi Poggiolini, Gabriella Bosco, Andrea Carena, Vittorio Curri, Yanchao Jiang and
 *          Fabrizio Forghieri
 *
 * @author Mehdi Habibi
 */
public class SNR {

    public static double alpha = ((0.22)/1000d)/8.685889638;     // =2.5328e-5 Np/m (= 0.22 d/km), 1 Np = 8.685889638 dB
    public static double ls = 80000d;
    public static double beta2 = 21.7 * Math.pow(10,-27);
    public static double gamma = 1.3 * Math.pow(10,-3);
    public static double nsp = 1.8;
    public static double pi = 3.14159265359;     // pi!
    public static double h = 6.62607004 * Math.pow(10,-34);     // Plank constant
    public static double e = 2.71828182845;     // Euler's number
    public static double power = 3d;     // power of channels (dB)
    public static double slotSize = 12.5 * Math.pow(10,9);
    public EONLink[] links;
    public EONLightPath lp;



    public SNR(EONLightPath lp , EONLink[] links){
        this.lp = lp;
        this.links = links;
    }

    /**
     * Retrieves the bandwidth of channels
     *
     * @return the bandwidth (Hz), center frequency (Hz) of each channel and number of channels in the link
     * bandLength[][0] is for bandwidths, bandLength[][1] is for center frequencies, bandLength[][2] is for
     * first slot index
     */
    double[][] getLinkBW(int id){
        int numSlots = links[id].getNumSlots();
        long[] slots = links[id].slots;
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
     *
     * @return the SNR of each channel
     */
    public double[] getLinkSNR(int id){
        double[][] BW = getLinkBW(id);     // bandwidth and center frequencies
        int numSlots = links[id].getNumSlots();
        int channelNum = (int)BW[numSlots - 1][0];     // number of channels
        double L = links[id].getWeight() * 1000d;     // fiber length
        double Ns = Math.ceil(L / ls);     // number of spans
        double leff = (1d - Math.pow(e,-2d*alpha*ls))/(2d*alpha);     // effective length
        double[][] psi = new double[channelNum][channelNum];     // psi in nonlinear part of noise
        double den = (2d * pi * beta2)/(2d * alpha);     // denominator of psi = 2.691542e-21
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

    /**
     * Retrieves the SNR of links
     *
     * @return the SNR of lightpath
     */
    public double getLightPathSNR(){
        double SNR =0;
        int[] index = new int[links.length];
        for(int k = 0; k < links.length; k++) {
            double[][] bandwidth = getLinkBW(k);
            for (int a = 0; a < bandwidth[(EONLink.numSlots - 1)][0]; a++) {     // finding the index of lightpath in the links
                if (bandwidth[a][2] == (double) lp.getFirstSlot()) {
                    index[k] = a;
                }
            }
            double temp = getLinkSNR(k)[index[k]];
            if (k == 0) {
                SNR = temp;
            } else {
                SNR = (SNR * temp) / (SNR + temp);
            }
        }
        return SNR;
    }

}
