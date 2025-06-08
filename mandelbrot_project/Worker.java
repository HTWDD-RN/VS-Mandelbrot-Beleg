import java.awt.Color;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Worker‐Implementation: berechnet Mandelbrot‐Blöcke lokal.
 * Verwendet explizite xmin, xmax, ymin, ymax Parameter.
 */
public class Worker extends UnicastRemoteObject implements WorkerInterface {

    protected Worker() throws RemoteException {
        super();
    }

    @Override
    public Color[][] computeBlock(
            int maxIter,
            int x0, int y0,
            int w, int h,
            int imgW, int imgH,
            double xmin, double xmax,
            double ymin, double ymax
    ) throws RemoteException {

        System.out.printf("[WORKER] Computing block [%d,%d %dx%d] (imgW=%d,imgH=%d xmin=%.4f xmax=%.4f ymin=%.4f ymax=%.4f)%n",
                x0, y0, w, h, imgW, imgH, xmin, xmax, ymin, ymax);

        Color[][] block = new Color[w][h];

        for (int dx = 0; dx < w; dx++) {
            for (int dy = 0; dy < h; dy++) {
                // 1) Pixel-Koordinate → komplexe Ebene
                double cr = xmin + (x0 + dx) * (xmax - xmin) / (imgW - 1);
                double ci = ymin + ((imgH - 1) - (y0 + dy)) * (ymax - ymin) / (imgH - 1);

                // 2) Mandelbrot-Iteration (mit z0 = 0)
                double zr = 0.0, zi = 0.0;
                double zr2 = 0.0, zi2 = 0.0;
                int iter = 0;
                while (iter < maxIter && (zr2 + zi2) <= 4.0) {
                    // z = z^2 + c
                    zi = 2.0 * zr * zi + ci;
                    zr = zr2 - zi2 + cr;

                    zr2 = zr * zr;
                    zi2 = zi * zi;
                    iter++;
                }

                // 3) Farbvergabe: Smooth Coloring
                if (iter == maxIter) {
                    // Punkt „ist“ im Mandelbrot‐Set: schwarz
                    block[dx][dy] = Color.BLACK;
                } else {
                    // 3a) Betrag von z nach letzter Iteration
                    double modZ = Math.sqrt(zr2 + zi2);

                    // 3b) Logarithmische Smooth‐Iteration:
                    //     nu = iter + 1 - log₂( log|z| )
                    double logModZ = Math.log(modZ);
                    double nu = iter + 1 - Math.log(logModZ / Math.log(2)) / Math.log(2);

                    // 3c) Hue über [0..1) verteilen (Gesamt‐Farbenkreis):
                    //     Durch modulo‐1 erreichen wir zyklische Farbübergänge
                    float hue = (float) ((nu / maxIter) % 1.0);

                    // 3d) Sättigung und Helligkeit auf 1 → satte, leuchtende Farben
                    block[dx][dy] = Color.getHSBColor(hue, 1f, 1f);
                }
            }
        }

        return block;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java Worker <masterIP> <masterPort>");
            System.exit(1);
        }
        try {
            String masterIP   = args[0];
            int    masterPort = Integer.parseInt(args[1]);
            String url        = "rmi://" + masterIP + ":" + masterPort + "/MasterServer";

            // 1) Master-Stub holen
            MasterInterface master = (MasterInterface) Naming.lookup(url);

            // 2) Worker-Instanz erzeugen (wird durch super() exportiert)
            Worker w = new Worker();

            // 3) Worker beim Master anmelden
            master.registerWorker(w);
            System.out.println("[WORKER] Registered at " + url);
        } catch (Exception e) {
            System.err.println("[WORKER] Exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
