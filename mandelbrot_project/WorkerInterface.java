import java.awt.Color;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote-Interface des Workers: berechnet Mandelbrot für einen Block.
 * Wir erhalten alle vier Ecken‐Werte (xmin, xmax, ymin, ymax).
 */
public interface WorkerInterface extends Remote {
    Color[][] computeBlock(
            int maxIter,
            int x0, int y0,
            int w, int h,
            int imgW, int imgH,
            double xmin, double xmax,
            double ymin, double ymax
    ) throws RemoteException;
}
