package ServerData;

import RemoteUserTable.RemoteUserTable;

import java.io.Serializable;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class ServerData implements Serializable {
    private UserTable users;
    private DocumentTable documents;
    private ConcurrentHashMap<SocketChannel,String> connectedUsers;

    private ServerData(String path) throws RemoteException{
        users=new UserTable();
        documents=new DocumentTable(path);
        connectedUsers=new ConcurrentHashMap<>();
    }

    private ServerData(String path, int initialCapacity, float loadFactor, int concurrencyLevel) throws RemoteException {
        users=new UserTable(initialCapacity,loadFactor,concurrencyLevel);
        documents=new DocumentTable(path,initialCapacity,loadFactor,concurrencyLevel);
        connectedUsers=new ConcurrentHashMap<>();
    }
    /*package*/ UserTable getUserTable(){
        return users;
    }

    /*package*/ DocumentTable getDocumentTable(){
        return documents;
    }

    /*package*/ ConcurrentHashMap<SocketChannel,String> getConnectedUsers(){
        return connectedUsers;
    }

    private void activateRMI(int port) throws RemoteException{
        RemoteUserTable stub= (RemoteUserTable)UnicastRemoteObject.exportObject(users,0);
        LocateRegistry.createRegistry(port);
        Registry reg=LocateRegistry.getRegistry(port);
        reg.rebind("USERTABLE-TURING",stub);
    }

    public static ServerData createServerData(String path,int RMIport) throws RemoteException{
        ServerData newdata=new ServerData(path);
        newdata.activateRMI(RMIport);
        return newdata;
    }

}
