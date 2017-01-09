package com.mcintyret.mandelbrot;

/**
 * User: mcintyret2
 * Date: 15/02/2014
 */
public class Mandelbrot {

    public static int doMandelbrot(final double re, final double im, int iterations) {
        double real = re;
        double imaginary = im;

        double realSq = real * real;
        double imSq = imaginary * imaginary;

        for (int i = 0; i < iterations; i++) {
            double tempR = realSq - imSq;
            double tempI = 2 * (real * imaginary);

            real = tempR + re;
            imaginary = tempI + im;

            realSq = real * real;
            imSq = imaginary * imaginary;

            if (realSq + imSq > 4.0) {
                return i;
            }
        }
        return -1;
    }

}
