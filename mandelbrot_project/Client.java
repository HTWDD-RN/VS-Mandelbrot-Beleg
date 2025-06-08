import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Client-Startpunkt: verbindet Presenter mit Master via RMI.
 */
public class Client {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java Client <masterIP> <masterPort>");
            return;
        }
        String masterIP   = args[0];
        int    masterPort = Integer.parseInt(args[1]);

        try {
            Registry registry = LocateRegistry.getRegistry(masterIP, masterPort);
            MasterInterface master = (MasterInterface) registry.lookup("MasterServer");
            System.out.printf("[CLIENT] Connected to Master %s:%d%n", masterIP, masterPort);
            // Presenter starten (öffnet GUI)
            new ApfelPresenter(master);
        } catch (Exception e) {
            System.err.println("[CLIENT] Exception:");
            e.printStackTrace();
        }
    }
}
