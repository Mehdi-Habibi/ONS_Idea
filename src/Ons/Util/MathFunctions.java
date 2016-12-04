package Ons.Util;

/**
 * Created by Mehdi on 16/12/01.
 */
public class MathFunctions {

    public static double asinh(double a){
        return Math.log(a + Math.sqrt(1 + a * a));
    }
}
