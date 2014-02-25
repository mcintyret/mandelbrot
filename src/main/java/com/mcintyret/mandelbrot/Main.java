package com.mcintyret.mandelbrot;

import javax.swing.*;
import java.awt.*;

/**
 * User: mcintyret2
 * Date: 15/02/2014
 */
public class Main {

    public static void main(String[] args) {
        final JFrame frame = new JFrame("Mandelbrot");
        frame.setBounds(100, 100, 800, 600);
        MandelbrotPanel mandelbrotPanel = new MandelbrotPanel(1000);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(mandelbrotPanel, BorderLayout.CENTER);
        frame.getContentPane().add(new Toolbar(mandelbrotPanel.getCanvas()), BorderLayout.NORTH);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.toFront();

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
