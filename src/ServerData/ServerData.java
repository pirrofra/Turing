package ServerData;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

public class ServerData {
    private UserTable users;
    private DocumentTable documents;
    private ConcurrentHashMap<SocketChannel,String> connectedUsers;

    public ServerData(){
        users=new UserTable();
        documents=new DocumentTable("files/");
        connectedUsers=new ConcurrentHashMap<>();
    }

    public ServerData(int initialCapacity, float loadFactor, int concurrencyLevel){
        users=new UserTable(initialCapacity,loadFactor,concurrencyLevel);
        documents=new DocumentTable("files/",initialCapacity,loadFactor,concurrencyLevel);
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

}
