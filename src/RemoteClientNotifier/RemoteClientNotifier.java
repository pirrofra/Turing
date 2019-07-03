package RemoteClientNotifier;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface defines method that can be called using RMI
 * This interface is implemented by ClientGui.ClientNotifier class
 * This is used by the server to notify a client that a new invite has been received
 *
 * @author Francesco Pirrò - Matr. 544539
 */
public interface RemoteClientNotifier extends Remote {

    void notify(String msg) throws RemoteException;
}
