package ServerData;

import RemoteUserTable.RemoteUserTable;

import java.io.Serializable;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to store all needed information for a correct execution of a Turing Server
 * This class stores the userTable, the DocumentTable and the table of connected users
 *
 * Its constructor is private, so it can be obtained only using the static method createServerData
 * This method initialize a new instance of serverData, and it starts the RMI service on the port given as a parameter
 *
 * UserTable,DocumentTable and connectedUsers can be returned with their respective getters
 * These getters are package-private so only classes in the ServerData packages can use them.
 *
 * @author Francesco Pirrò - Matr.544539
 */
public class ServerData implements Serializable {
    private UserTable users;
    private DocumentTable documents;
    private ConcurrentHashMap<SocketChannel,String> connectedUsers;

    /**
     * Private class constructor
     * @param path Path to use for storing documents
     * @throws RemoteException if an error occurs during a Remote Method Invocation
     */
    private ServerData(String path) throws RemoteException{
        users=new UserTable();
        documents=new DocumentTable(path);
        connectedUsers=new ConcurrentHashMap<>();
    }

    /**
     * Private class constructor
     * @param path Path to use for storing documents
     * @param initialCapacity hashTable initial capacity
     * @param loadFactor Hash Table load factor
     * @param concurrencyLevel max number of concurrent access
     * @throws RemoteException an exception thrown by the RMI-support if an error occurs
     */
    private ServerData(String path, int initialCapacity, float loadFactor, int concurrencyLevel) throws RemoteException {
        users=new UserTable(initialCapacity,loadFactor,concurrencyLevel);
        documents=new DocumentTable(path,initialCapacity,loadFactor,concurrencyLevel);
        connectedUsers=new ConcurrentHashMap<>();
    }

    /**
     * Getter for UserTable
     * @return users
     */
    /*package*/ UserTable getUserTable(){
        return users;
    }

    /**
     * Getter for DocumentTable
     * @return documents
     */
    /*package*/ DocumentTable getDocumentTable(){
        return documents;
    }

    /**
     * Getter for connectedUsers
     * @return connectedUsers
     */
    /*package*/ ConcurrentHashMap<SocketChannel,String> getConnectedUsers(){
        return connectedUsers;
    }

    /**
     * private method to activate the RMI support
     * @param port port to use for the RMI registry
     * @throws RemoteException an exception thrown by the RMI-support if an error occurs
     */
    private void activateRMI(int port) throws RemoteException{
        RemoteUserTable stub= (RemoteUserTable)UnicastRemoteObject.exportObject(users,0);
        LocateRegistry.createRegistry(port);
        Registry reg=LocateRegistry.getRegistry(port);
        reg.rebind("USERTABLE-TURING",stub);
    }

    /**
     * Static method used to retrieve a new ServerData instance
     * @param path Path to use for storing documents
     * @param RMIport port to use for the RMI registry
     * @return new ServerData instance
     * @throws RemoteException an exception thrown by the RMI-support if an error occurs
     */
    public static ServerData createServerData(String path,int RMIport) throws RemoteException{
        ServerData newdata=new ServerData(path);
        newdata.activateRMI(RMIport);
        return newdata;
    }

}
