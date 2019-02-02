package ServerData;

import ChatRoom.ChatOrganizer;
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
    private final UserTable users;
    private final DocumentTable documents;
    private final ChatOrganizer chat;
    private final ConcurrentHashMap<SocketChannel,String> connectedUsers;

    /**
     * Private class constructor
     * @param path Path to use for storing documents
     * @param baseAddress address base for multicast range
     * @param bound Multicast address range dimension
     * @param port port used for the Multicast Chat
     * @param maxSize max Dimension of a document
     */
    private ServerData(String path,String baseAddress,int bound,int port,int maxSize) {
        users=new UserTable();
        documents=new DocumentTable(path,maxSize);
        connectedUsers=new ConcurrentHashMap<>();
        chat=new ChatOrganizer(baseAddress,bound,port);
    }

    /**
     * Private class constructor
     * @param path Path to use for storing documents
     * @param maxSize max Dimension of a document
     * @param baseAddress address base for multicast range
     * @param bound Multicast address range dimension
     * @param port port used for the Multicast Chat
     * @param initialCapacity hashTable initial capacity
     * @param loadFactor Hash Table load factor
     * @param concurrencyLevel max number of concurrent access
     */
    private ServerData(String path,int maxSize,String baseAddress,int bound,int port, int initialCapacity, float loadFactor, int concurrencyLevel) {
        users=new UserTable(initialCapacity,loadFactor,concurrencyLevel);
        documents=new DocumentTable(path,maxSize,initialCapacity,loadFactor,concurrencyLevel);
        connectedUsers=new ConcurrentHashMap<>();
        chat=new ChatOrganizer(baseAddress,bound,port);
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

    /*package*/ ChatOrganizer getChatOrganizer(){
        return chat;
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
     * @param maxSize max Dimension of a document
     * @param baseAddress address base for multicast range
     * @param bound Multicast address range dimension
     * @param chatPort Port used for the Multicast Chat
     * @param RMIport port to use for the RMI registry
     * @return new ServerData instance
     * @throws RemoteException an exception thrown by the RMI-support if an error occurs
     */
    public static ServerData createServerData(String path,int maxSize,String baseAddress,int bound,int chatPort,int RMIport) throws RemoteException{
        ServerData newData=new ServerData(path,baseAddress,bound,chatPort,maxSize);
        newData.activateRMI(RMIport);
        return newData;
    }




}
