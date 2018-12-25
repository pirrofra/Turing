import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;

public class UserTable extends RemoteServer implements RemoteUserTable {


    public int registerUser(String username, String password) throws RemoteException, IllegalArgumentException {
        return 0;
    }
}
