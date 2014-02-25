package com.mcintyret.mandelbrot;

/**
 * User: mcintyret2
 * Date: 15/02/2014
 */
public class Mandelbrot {

    public static int doMandelbrot(final double re, final double im, int iterations) {
        double real = re;
        double imaginary = im;

        for (int i = 0; i < iterations; i++) {
            double tempR = (real * real) - (imaginary * imaginary);
            double tempI = 2 * (real * imaginary);

            real = tempR + re;
            imaginary = tempI + im;

            if ((real * real) + (imaginary * imaginary) > 4) {
                return i;
            }
        }
        return -1;
    }

}
