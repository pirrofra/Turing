import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteUserTable extends Remote {
    int registerUser(String username,String password) throws RemoteException,IllegalArgumentException;
}
