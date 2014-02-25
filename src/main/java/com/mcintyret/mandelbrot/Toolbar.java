package com.mcintyret.mandelbrot;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: mcintyret2
 * Date: 15/02/2014
 */
public class Toolbar extends JPanel {

    public Toolbar(final MandelbrotPanel.MandelbrotCanvas mandelbrotCanvas) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        JButton reset = new JButton("Reset");
        add(reset);
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mandelbrotCanvas.reset();
            }
        });

        JButton back = new JButton("Back");
        add(back);
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mandelbrotCanvas.back();
            }
        });

        int initialIterations = mandelbrotCanvas.getIterations();
        final JSlider iterationsSlider = new JSlider(1, 10000, initialIterations);
        final JLabel iterationsValue = new JLabel(Integer.toString(initialIterations));

        iterationsSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int val = iterationsSlider.getValue();
                iterationsValue.setText(Integer.toString(val));
                mandelbrotCanvas.setIterations(val);
            }
        });

        add(iterationsSlider);
        add(iterationsValue);

        JButton draw = new JButton("Draw");
        draw.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mandelbrotCanvas.repaint();
            }
        });
        add(draw);
    }

}
