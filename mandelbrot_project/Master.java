import java.awt.Color;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Master‐Implementation: hält Worker‐Stubs und verteilt computeBlock-Aufrufe.
 * Nun mit expliziten xmin, xmax, ymin, ymax Parametern.
 */
public class Master extends UnicastRemoteObject implements MasterInterface {
    private final List<WorkerInterface> workers = new CopyOnWriteArrayList<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    public Master() throws RemoteException {
        super();
    }

    @Override
    public void registerWorker(WorkerInterface worker) throws RemoteException {
        workers.add(worker);
        System.out.println("[MASTER] Worker registered. Total workers: " + workers.size());
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
        if (workers.isEmpty()) {
            System.out.println("[MASTER] No workers available!");
            return new Color[w][h];
        }
        int idx = counter.getAndIncrement() % workers.size();
        WorkerInterface worker = workers.get(idx);
        System.out.printf(
                "[MASTER] Dispatch block [%d,%d %dx%d] to worker %d (imgW=%d imgH=%d xmin=%.3f xmax=%.3f ymin=%.3f ymax=%.3f)%n",
                x0, y0, w, h, idx, imgW, imgH, xmin, xmax, ymin, ymax
        );
        return worker.computeBlock(
                maxIter, x0, y0, w, h, imgW, imgH,
                xmin, xmax, ymin, ymax
        );
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Master <port>");
            System.exit(1);
        }
        try {
            int port = Integer.parseInt(args[0]);
            String host = InetAddress.getLocalHost().getHostAddress();

            // 1) RMI‐Registry intern starten
            LocateRegistry.createRegistry(port);

            // 2) Master exportieren + binden
            String url = "rmi://" + host + ":" + port + "/MasterServer";
            Master master = new Master();
            Naming.rebind(url, master);
            System.out.println("[MASTER] Ready at " + url);
        } catch (Exception e) {
            System.err.println("[MASTER] Exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
