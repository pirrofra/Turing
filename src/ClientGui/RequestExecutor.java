package ClientGui;

import Message.MessageBuffer;
import Message.Operation;
import RemoteUserTable.RemoteUserTable;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/*package*/ class RequestExecutor {

    private SocketChannel channel;
    private String server;
    private int RMIport;
    private String filePath;

    /*package*/ RequestExecutor(SocketChannel socket,String hostname,int p,String path){
        channel=socket;
        server=hostname;
        RMIport=p;
        filePath=path;
    }

    /*package*/ MessageBuffer login(String username,String password) throws IOException{
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.LOGIN,username.getBytes(),password.getBytes());
        request.sendMessage(channel);
        return MessageBuffer.readMessage(channel);
    }

    /*package*/ Operation register(String username,String password) throws IOException{
        Registry reg= LocateRegistry.getRegistry(server,RMIport);
        try{
            RemoteUserTable userTable = (RemoteUserTable) reg.lookup("USERTABLE-TURING");
            return userTable.registerUser(username,password);
        }
        catch (NotBoundException e){
            throw new IOException();
        }
    }

    /*package*/ String getRemoteAddress() throws IOException,NullPointerException {
        return channel.getRemoteAddress().toString();
    }

    /*package*/ String getFilePath(){
        return filePath;
    }

}
