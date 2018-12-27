package RemoteUserTable;

import Message.Operation;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface defines method that can be called using RMI
 * This interface is implemented by ServerData.UserTable class
 *
 * @author Francesco Pirrò - Matr. 544539
 */
public interface RemoteUserTable extends Remote {

    Operation registerUser(String username, String password) throws RemoteException,IllegalArgumentException;
}
