package com.mcintyret.mandelbrot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: mcintyret2
 * Date: 15/02/2014
 */

public class MandelbrotPanel extends JPanel {

    private static final int RUNNABLES = 10;

    private static final int BLACK_RGB = Color.BLACK.getRGB();

    private static final double MAX_X = 1D;

    private static final double MIN_X = -2D;

    private static final double MAX_Y = 1D;

    private static final double MIN_Y = -1D;

    private final MandelbrotCanvas canvas;

    private final JProgressBar progressBar = new JProgressBar();

    public MandelbrotPanel(int iterations) {
        setLayout(new BorderLayout());
        this.canvas = new MandelbrotCanvas(iterations);
        add(canvas, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);

        progressBar.setValue(progressBar.getMinimum());
    }

    public MandelbrotCanvas getCanvas() {
        return canvas;
    }

    public class MandelbrotCanvas extends JPanel implements PropertyChangeListener {

        private int iterations;

        private final Stack<ViewPort> history = new Stack<ViewPort>();

        private ViewPort rect = new ViewPort(MAX_X, MAX_Y, MIN_X, MIN_Y);

        private final Palette palette = new Palette();

        private Rectangle clickDragRectangle;

        private final AtomicReference<BufferedImage> image = new AtomicReference<BufferedImage>();

        private volatile BufferedImage prevImage;

        public MandelbrotCanvas(int iterations) {
            this.iterations = iterations;
            MouseAdapter mouseAdapter = new MouseAdapter() {

                Point p;

                public void mousePressed(MouseEvent e) {
                    p = e.getPoint();
                    clickDragRectangle = null;
                }

                public void mouseDragged(MouseEvent e) {
                    if (p != null) {
                        Point n = e.getPoint();
                        ViewPort vp = ViewPort.of(n, p);
                        clickDragRectangle = new Rectangle((int) vp.minX, (int) vp.minY, (int) (vp.maxX - vp.minX), (int) (vp.maxY - vp.minY));
                        repaint();
                    }
                }

                public void mouseReleased(MouseEvent e) {
                    if (p != null) {
                        Point n = e.getPoint();
                        if (!n.equals(p)) {
                            ViewPort r = ViewPort.of(p, n);
                            final double xIncrement = (rect.maxX - rect.minX) / getWidth();
                            final double yIncrement = (rect.maxY - rect.minY) / getHeight();
                            ViewPort newRect = new ViewPort(
                                rect.minX + (r.maxX * xIncrement),
                                rect.minY + (r.maxY * yIncrement),
                                rect.minX + (r.minX * xIncrement),
                                rect.minY + (r.minY * yIncrement)
                            );
                            p = null;
                            history.push(rect);
                            rect = newRect;
                            clickDragRectangle = null;
                            MandelbrotCanvas.this.repaint();
                        }
                    }
                }
            };
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        public void back() {
            if (hasBack()) {
                setViewPort(history.pop());
            }
        }

        public void setIterations(int iterations) {
            this.iterations = iterations;
        }

        public int getIterations() {
            return iterations;
        }

        public boolean hasBack() {
            return !history.isEmpty();
        }

        public void reset() {
            if (hasBack()) {
                ViewPort first = history.iterator().next();
                history.clear();
                setViewPort(first);
            }
        }

        private void setViewPort(ViewPort vp) {
            rect = vp;
            repaint();
        }

        private void makeImage(final int width, final int height, final int iterations, final Graphics2D g2) {
            final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            final double xIncrement = (rect.maxX - rect.minX) / width;
            final double yIncrement = (rect.maxY - rect.minY) / height;

            final int chunk = width / RUNNABLES;
            final double total = width * height;
            final AtomicInteger pixelColsDone = new AtomicInteger();
            final AtomicInteger runnablesRemaining = new AtomicInteger(RUNNABLES);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            for (int i = 0; i < RUNNABLES; i++) {
                final int finI = i;
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        int start = finI * chunk;
                        double real = rect.minX + start * xIncrement;
                        int end = Math.min(width, start + chunk);
                        for (int x = start; x < end; x++) {
                            double im = rect.minY;
                            for (int y = 0; y < height; y++) {
                                int result = Mandelbrot.doMandelbrot(real, im, iterations);
                                if (result < 0) {
                                    image.setRGB(x, y, BLACK_RGB);
                                } else {
                                    image.setRGB(x, y, palette.getColor(result / (double) iterations).getRGB());
                                }
                                im += yIncrement;
                            }
                            real += xIncrement;
                            int doneNow = pixelColsDone.addAndGet(height);
                            final int progressVal = (int) (progressBar.getMaximum() * doneNow / total);
                            setProgress(progressVal);
                        }

                        return null;
                    }

                    @Override
                    public void done() {
                        setCursor(null);
                        if (runnablesRemaining.decrementAndGet() == 0) {
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    MandelbrotCanvas.this.image.set(image);
                                    MandelbrotCanvas.this.repaint();
                                }
                            });
                        }
                    }
                };

                worker.addPropertyChangeListener(this);
                worker.execute();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            if (clickDragRectangle == null) {
                BufferedImage img = image.getAndSet(null);
                if (img != null) {
                    prevImage = img;
                } else {
                    makeImage(getWidth(), getHeight(), iterations, g2);
                }
                g2.drawImage(prevImage, null, 0, 0);
            } else {
                g2.drawImage(prevImage, null, 0, 0);
                g2.setColor(Color.BLACK);
                g2.draw(clickDragRectangle);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("progress" == evt.getPropertyName()) {
                int progress = (Integer) evt.getNewValue();
                progressBar.setValue(progress);
            }
        }
    }

    private static class ViewPort {
        private final double maxX;
        private final double maxY;
        private final double minX;
        private final double minY;

        private ViewPort(double maxX, double maxY, double minX, double minY) {
            this.maxX = maxX;
            this.maxY = maxY;
            this.minX = minX;
            this.minY = minY;
        }

        private static ViewPort of(Point one, Point two) {
            double maxX, minX;
            if (one.x > two.x) {
                maxX = one.x;
                minX = two.x;
            } else {
                maxX = two.x;
                minX = one.x;
            }

            double maxY, minY;
            if (one.y > two.y) {
                maxY = one.y;
                minY = two.y;
            } else {
                maxY = two.y;
                minY = one.y;
            }
            return new ViewPort(maxX, maxY, minX, minY);
        }

        @Override
        public String toString() {
            return "X: " + minX + "-" + maxX + ", Y: " + minY + "-" + maxY;
        }
    }
}
