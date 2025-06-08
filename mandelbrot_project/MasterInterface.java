import java.awt.Color;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote-Interface des Masters: verteilt Blöcke an Worker.
 * Wir übergeben nun explizit xmin, xmax, ymin, ymax an den Worker.
 */
public interface MasterInterface extends Remote {
    /**
     * Meldet einen Worker beim Master an.
     */
    void registerWorker(WorkerInterface worker) throws RemoteException;

    /**
     * Berechnet einen Bildblock (w×h) mit folgenden Parametern:
     * @param maxIter   maximale Iterationsanzahl
     * @param x0        Start-X im Pixelbereich (0..imgW-1)
     * @param y0        Start-Y im Pixelbereich (0..imgH-1)
     * @param w         Breite des Blocks in Pixeln
     * @param h         Höhe des Blocks in Pixeln
     * @param imgW      Gesamtbreite des Bildes
     * @param imgH      Gesamthöhe des Bildes
     * @param xmin      Minimaler Real‐Wert (linke Kante)
     * @param xmax      Maximaler Real‐Wert (rechte Kante)
     * @param ymin      Minimaler Imaginär‐Wert (untere Kante)
     * @param ymax      Maximaler Imaginär‐Wert (obere Kante)
     * @return          2D‐Farbmatrix [w][h] für diesen Block
     */
    Color[][] computeBlock(
            int maxIter,
            int x0, int y0,
            int w, int h,
            int imgW, int imgH,
            double xmin, double xmax,
            double ymin, double ymax
    ) throws RemoteException;
}
