import java.awt.Color;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Model: verteilt das Bild in vertikale Layer‐Blöcke, ruft
 * Master.computeBlock(...) per RMI auf und fügt die Teil-Bilder
 * zu einem BufferedImage zusammen.
 */
public class ApfelModel {
    private final MasterInterface master;
    private final ExecutorService executor;

    public ApfelModel(MasterInterface master) {
        this.master = master;
        // Mit CachedThreadPool können beliebig viele Layer gleichzeitig bearbeitet werden
        this.executor = Executors.newCachedThreadPool();
    }

    /**
     * Berechnet das komplette Mandelbrot-Bild.
     *
     * @param xmin    linke Grenze (Real-Achse)
     * @param xmax    rechte Grenze (Real-Achse)
     * @param ymin    untere Grenze (Imaginär-Achse)
     * @param ymax    obere Grenze (Imaginär-Achse)
     * @param maxIter maximale Iterationszahl
     * @param threads (aktuell nicht direkt genutzt, da RMI-Parallelisierung)
     * @param layers  Anzahl vertikaler Teilblöcke
     * @param imgW    Bildbreite in Pixeln
     * @param imgH    Bildhöhe in Pixeln
     * @return BufferedImage mit dem vollständigen Mandelbrot
     */
    public BufferedImage computeMandel(
            double xmin, double xmax,
            double ymin, double ymax,
            int maxIter, int threads, int layers,
            int imgW, int imgH
    ) {
        System.out.printf(
                "[MODEL] computeMandel: W=%d H=%d maxIter=%d layers=%d xmin=%f xmax=%f ymin=%f ymax=%f%n",
                imgW, imgH, maxIter, layers, xmin, xmax, ymin, ymax
        );

        // 1) Neues BufferedImage anlegen
        BufferedImage img = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);

        // 2) Höhe pro Layer (vertikale Aufteilung)
        int hLayer = imgH / layers;

        // 3) Sammle Futures für jedes Layer
        List<Future<Color[][]>> futures = new ArrayList<>();

        for (int L = 0; L < layers; L++) {
            final int y0 = L * hLayer;
            final int h0 = (L == layers - 1) ? (imgH - y0) : hLayer;

            // Alle Parameter in final-lokale Variablen packen, damit sie in Callable verwendet werden können
            final int finalY0    = y0;
            final int finalH0    = h0;
            final int finalImgW  = imgW;
            final int finalImgH  = imgH;
            final int finalMax   = maxIter;
            final double finalXmin = xmin;
            final double finalXmax = xmax;
            final double finalYmin = ymin;
            final double finalYmax = ymax;

            // Asynchrones RMI‐Call an den Master: computeBlock(...)
            futures.add(executor.submit(() -> {

                return master.computeBlock(
                        finalMax,
                        0, finalY0,
                        finalImgW, finalH0,
                        finalImgW, finalImgH,
                        finalXmin, finalXmax,
                        finalYmin, finalYmax
                );
            }));
        }

        // 4) Auf Ergebnisse warten und in img schreiben
        for (int L = 0; L < futures.size(); L++) {
            try {
                Color[][] block = futures.get(L).get();
                int y0 = L * hLayer; // y-Offset für diesen Layer

                for (int dx = 0; dx < imgW; dx++) {
                    for (int dy = 0; dy < block[dx].length; dy++) {
                        img.setRGB(dx, y0 + dy, block[dx][dy].getRGB());
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("[MODEL] Fehler beim Einlesen von Layer " + L);
                e.printStackTrace();
            }
        }

        return img;
    }
}
