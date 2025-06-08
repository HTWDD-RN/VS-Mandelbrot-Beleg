import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Präsentations-Logik: verknüpft View und Model, liest alle Parameter
 * aus der GUI aus und steuert den asynchronen Mandelbrot‐Thread.
 */
public class ApfelPresenter {
    private final ApfelView view;
    private final MasterInterface master;

    // xmin, xmax, ymin, ymax als Felder, damit sie von SwingWorker gelesen/gesetzt werden können
    private double xmin, xmax, ymin, ymax;

    public ApfelPresenter(MasterInterface master) {
        this.master = master;
        this.view   = new ApfelView(this);
        SwingUtilities.invokeLater(() -> view.setVisible(true));
    }

    public void onStart() {
        System.out.println("[PRESENTER] onStart() called");
        view.resetStatus();

        int rounds   = view.getRounds();
        double cr    = view.getCr();
        double ci    = view.getCi();
        double zoomR = view.getZoomRate();
        int maxIter  = view.getMaxIter();
        int threads  = view.getThreads();
        int layers   = view.getLayers();
        int imgW     = view.getImageWidth();
        int imgH     = view.getImageHeight();

        double aspect = imgH / (double) imgW;
        double halfWidth = 0.2;
        double halfHeight = halfWidth * aspect;

        this.xmin = cr - halfWidth;
        this.xmax = cr + halfWidth;
        this.ymin = ci - halfHeight;
        this.ymax = ci + halfHeight;

        String prefix = view.isLocalOnly() ? "[LOKAL]" : "[VERTEILT]";

        runWithModel(prefix, rounds, cr, ci, zoomR, maxIter, threads, layers, imgW, imgH);
    }

    private void runWithModel(String prefix, int rounds, double cr, double ci, double zoomR,
                              int maxIter, int threads, int layers, int imgW, int imgH) {

        double aspect = imgH / (double) imgW;
        double halfWidth = 0.2;
        double halfHeight = halfWidth * aspect;

        xmin = cr - halfWidth;
        xmax = cr + halfWidth;
        ymin = ci - halfHeight;
        ymax = ci + halfHeight;

        List<Double> timestamps = new ArrayList<>();

        new SwingWorker<Void, BufferedImage>() {
            @Override
            protected Void doInBackground() throws Exception {
                long startTimeNanos = System.nanoTime();

                for (int i = 1; i <= rounds && !isCancelled(); i++) {
                    System.out.printf("%s Round %d/%d%n", prefix, i, rounds);
                    view.updateStatus(String.format("%s Schritt %d/%d", prefix, i, rounds));

                    BufferedImage img;
                    if (view.isLocalOnly()) {
                        img = computeLocalMandel(xmin, xmax, ymin, ymax, maxIter, imgW, imgH);
                    } else {
                        ApfelModel model = new ApfelModel(master);
                        img = model.computeMandel(
                                xmin, xmax, ymin, ymax,
                                maxIter, threads, layers,
                                imgW, imgH
                        );
                    }

                    publish(img);

                    double dx = xmax - xmin;
                    double dy = ymax - ymin;
                    double newDx = dx * zoomR;
                    double newDy = dy * zoomR;
                    xmin = cr - newDx / 2.0;
                    xmax = cr + newDx / 2.0;
                    ymin = ci - newDy / 2.0;
                    ymax = ci + newDy / 2.0;

                    double elapsedSec = (System.nanoTime() - startTimeNanos) / 1_000_000_000.0;
                    timestamps.add(elapsedSec);
                    view.updateTime(elapsedSec);

                    // VON JEDEM EINZELNEN SCHRITT EIN BILD SPEICHERN
                    
                    /*try {
                        ImageIO.write(img, "png", new File(prefix.replace("[", "").replace("]", "") + "_step" + i + ".png"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                }

                if (timestamps.size() > 1) {
                    double totalTime = timestamps.get(timestamps.size() - 1);
                    System.out.printf("%s Gesamtzeit: %.3fs\n", prefix, totalTime);
                    for (int i = 1; i < timestamps.size(); i++) {
                        double diff = timestamps.get(i) - timestamps.get(i - 1);
                        System.out.printf("%s Schritt %d: %.3fs\n", prefix, i + 1, diff);
                    }

                    try (PrintWriter out = new PrintWriter(new FileWriter(prefix.replace("[","" ).replace("]","" ).toLowerCase() + "_times.txt"))) {
                        out.println("Einstellungen:");
                        out.printf("Bildgröße: %dx%d\n", imgW, imgH);
                        out.printf("Zoom-Mittelpunkt: Cr=%.15f, Ci=%.15f\n", cr, ci);
                        out.printf("Zoomfaktor: %.2f\n", zoomR);
                        out.printf("Runden: %d\n", rounds);
                        out.printf("Iterationen: %d\n", maxIter);
                        out.printf("Threads: %d\n", threads);
                        out.printf("Layers: %d\n", layers);
                        out.println();
                        out.println("Round,Time");
                        for (int i = 0; i < timestamps.size(); i++) {
                            out.printf("%d,%.4f\n", i + 1, timestamps.get(i));
                        }
                    }
                }

                return null;
            }

            @Override
            protected void process(java.util.List<BufferedImage> chunks) {
                BufferedImage latest = chunks.get(chunks.size() - 1);
                view.updateImage(latest);
            }

            @Override
            protected void done() {
                try {
                    get();
                    view.updateStatus(prefix + " Fertig");
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    view.updateStatus(prefix + " Fehler beim Berechnen");
                }
            }
        }.execute();
    }

    private BufferedImage computeLocalMandel(double xmin, double xmax, double ymin, double ymax,
                                             int maxIter, int imgW, int imgH) {
        BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);

        for (int px = 0; px < imgW; px++) {
            for (int py = 0; py < imgH; py++) {
                double x0 = xmin + px * (xmax - xmin) / imgW;
                double y0 = ymin + py * (ymax - ymin) / imgH;
                double x = 0, y = 0;
                int iter = 0;
                while (x*x + y*y <= 4 && iter < maxIter) {
                    double xtemp = x*x - y*y + x0;
                    y = 2*x*y + y0;
                    x = xtemp;
                    iter++;
                }
                float hue = iter < maxIter ? iter / (float) maxIter : 0;
                int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, iter < maxIter ? 1.0f : 0);
                img.setRGB(px, py, rgb);
            }
        }
        return img;
    }

    public void onReset() {
        view.resetStatus();
    }
}


