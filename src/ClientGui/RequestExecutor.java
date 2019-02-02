package ClientGui;

import Message.MessageBuffer;
import Message.Operation;
import RemoteUserTable.RemoteUserTable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RequestExecutor {

    private final SocketChannel channel;
    private final String server;
    private final int RMIport;
    private final String filePath;

public RequestExecutor(SocketChannel socket,String hostname,int p,String path){
        channel=socket;
        server=hostname;
        RMIport=p;
        filePath=path;
    }

    /*package*/ synchronized MessageBuffer login(String username,String password,int port) throws IOException{
        ByteBuffer buffer=ByteBuffer.allocate(4);
        buffer.putInt(port);
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.LOGIN,username.getBytes(),password.getBytes(),buffer.array());
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
        catch (IllegalArgumentException e){
            return Operation.REQUEST_INCOMPLETE;
        }
    }

    /*package*/synchronized MessageBuffer createDocument(String docName,int numSection) throws IOException{
        ByteBuffer buffer=ByteBuffer.allocate(4);
        buffer.putInt(numSection);
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.CREATE,docName.getBytes(),buffer.array());
        request.sendMessage(channel);
        return MessageBuffer.readMessage(channel);
    }

    /*package*/synchronized MessageBuffer show(String docName) throws IOException{
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.SHOW,docName.getBytes());
        request.sendMessage(channel);
        return MessageBuffer.readMessage(channel);
    }

    /*package*/ synchronized MessageBuffer show(String docName,int section) throws IOException{
        ByteBuffer buffer=ByteBuffer.allocate(4);
        buffer.putInt(section);
        buffer.flip();
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.SHOW,docName.getBytes(),buffer.array());
        request.sendMessage(channel);
        return MessageBuffer.readMessage(channel);
    }

    /*package*/synchronized MessageBuffer invite(String docName,String user) throws IOException{
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.INVITE,docName.getBytes(),user.getBytes());
        request.sendMessage(channel);
        return MessageBuffer.readMessage(channel);
    }

    /*package*/synchronized MessageBuffer edit(String docName,int section) throws IOException{
        ByteBuffer buffer=ByteBuffer.allocate(4);
        buffer.putInt(section);
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.EDIT,docName.getBytes(),buffer.array());
        request.sendMessage(channel);
        return MessageBuffer.readMessage(channel);
    }

    /*package*/ synchronized MessageBuffer list() throws IOException{
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.LIST);
        request.sendMessage(channel);
        return MessageBuffer.readMessage(channel);
    }

    /*package*/synchronized MessageBuffer logout() throws IOException{
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.LOGOUT);
        request.sendMessage(channel);
        return MessageBuffer.readMessage(channel);
    }

    /*package*/synchronized MessageBuffer endEdit(String docName,int section,byte[] file ) throws IOException{
        ByteBuffer buffer=ByteBuffer.allocate(4);
        buffer.putInt(section);
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.END_EDIT,docName.getBytes(),buffer.array(),file);
        request.sendMessage(channel);
        return  MessageBuffer.readMessage(channel);
    }

    /*package*/ synchronized MessageBuffer chatRoom(String docName) throws IOException{
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.CHAT_ROOM,docName.getBytes());
        request.sendMessage(channel);
        return MessageBuffer.readMessage(channel);
    }

    /*package*/ String getRemoteAddress() throws IOException,NullPointerException {
        return channel.getRemoteAddress().toString();
    }

    /*package*/ String getFilePath(){
        return filePath;
    }

}
