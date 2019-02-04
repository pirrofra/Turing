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

/**
 * This class implements client communication with the server via SocketChannel.
 * For each request that can be sent to the server, a method is implemented to communicate with the server
 * The result of each operation is given as a MessageBuffer
 *
 * Every method has the synchronized keyword so access to socket is in mutual exclusion
 *
 * @author  Francesco Pirrò - Matr.544539
 */
public class RequestExecutor {

    /**
     * SocketChannel used for the communication with the server
     */
    private final SocketChannel channel;

    /**
     * Server address
     */
    private final String server;

    /**
     * Port used by the server for RMI services
     */
    private final int RMIport;

    /**
     * Path for files to be downloaded in
     */
    private final String filePath;

    /**
     * class constructor
     * @param socket SocketChannel used for the communication with the server
     * @param hostname Server address
     * @param p Port used by the server for RMI services
     * @param path Path for files to be downloaded in
     */
    public RequestExecutor(SocketChannel socket,String hostname,int p,String path){
        channel=socket;
        server=hostname;
        RMIport=p;
        filePath=path;
    }

    /**
     * Method to send a login request to the server
     * @param username user who wants to login
     * @param password password for login
     * @param port port used by the client for UDP notifications
     * @return MessageBuffer with the result
     * @throws IOException an error occurs while communicating with the server
     */
    /*package*/ synchronized MessageBuffer login(String username,String password,int port) throws IOException{
        ByteBuffer buffer=ByteBuffer.allocate(4);
        buffer.putInt(port);
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.LOGIN,username.getBytes(),password.getBytes(),buffer.array());
        request.sendMessage(channel);
        return MessageBuffer.readMessage(channel);
    }

    /**
     * Method that uses RMI to create a new user
     * @param username new username
     * @param password new password
     * @return Operation with the result
     * @throws IOException an error occurs while communicating with the server
     */
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

    /**
     * Method that sends a request to create a new document to the server
     * @param docName name of the new document
     * @param numSection number of sections of the new document
     * @return MessageBuffer with the result
     * @throws IOException an error occurs while communicating with the server
     */
    /*package*/synchronized MessageBuffer createDocument(String docName,int numSection) throws IOException{
        ByteBuffer buffer=ByteBuffer.allocate(4);
        buffer.putInt(numSection);
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.CREATE,docName.getBytes(),buffer.array());
        request.sendMessage(channel);
        return MessageBuffer.readMessage(channel);
    }

    /**
     * Method that sends a request to send an entire document to the server
     * @param docName name of the document to be shown
     *@return MessageBuffer with the result
     * @throws IOException an error occurs while communicating with the server
     */
    /*package*/synchronized MessageBuffer show(String docName) throws IOException{
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.SHOW,docName.getBytes());
        request.sendMessage(channel);
        return MessageBuffer.readMessage(channel);
    }

    /**
     * Method that sends a request to send a document's section to the server
     * @param docName name of the document
     * @param section section to be shown
     * @return MessageBuffer with the result
     * @throws IOException an error occurs while communicating with the server
     */
    /*package*/ synchronized MessageBuffer show(String docName,int section) throws IOException{
        ByteBuffer buffer=ByteBuffer.allocate(4);
        buffer.putInt(section);
        buffer.flip();
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.SHOW,docName.getBytes(),buffer.array());
        request.sendMessage(channel);
        return MessageBuffer.readMessage(channel);
    }

    /**
     * Method that sends an invite request to the server
     * @param docName document the user should be invited
     * @param user user invited
     * @return MessageBuffer with the result
     * @throws IOException an error occurs while communicating with the server
     */
    /*package*/synchronized MessageBuffer invite(String docName,String user) throws IOException{
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.INVITE,docName.getBytes(),user.getBytes());
        request.sendMessage(channel);
        return MessageBuffer.readMessage(channel);
    }

    /**
     * Method that sends an edit request to the server
     * @param docName name of the document to be edited
     * @param section section to be edited
     * @return MessageBuffer with the result
     * @throws IOException an error occurs while communicating with the server
     */
    /*package*/synchronized MessageBuffer edit(String docName,int section) throws IOException{
        ByteBuffer buffer=ByteBuffer.allocate(4);
        buffer.putInt(section);
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.EDIT,docName.getBytes(),buffer.array());
        request.sendMessage(channel);
        return MessageBuffer.readMessage(channel);
    }

    /**
     *Method that sends a request for the document list to the server
     * @return MessageBuffer with the result
     * @throws IOException an error occurs while communicating with the server
     */
    /*package*/ synchronized MessageBuffer list() throws IOException{
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.LIST);
        request.sendMessage(channel);
        return MessageBuffer.readMessage(channel);
    }

    /**
     * Method to send a logout request to the server
     * @return MessageBuffer with the result
     * @throws IOException an error occurs while communicating with the server
     */
    /*package*/synchronized MessageBuffer logout() throws IOException{
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.LOGOUT);
        request.sendMessage(channel);
        return MessageBuffer.readMessage(channel);
    }

    /**
     * Method to send an end edit request to the server
     * @param docName document editing
     * @param section section editing
     * @param file byte array that contains updated file
     * @return MessageBuffer with the result
     * @throws IOException an error occurs while communicating with the server
     */
    /*package*/synchronized MessageBuffer endEdit(String docName,int section,byte[] file ) throws IOException{
        ByteBuffer buffer=ByteBuffer.allocate(4);
        buffer.putInt(section);
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.END_EDIT,docName.getBytes(),buffer.array(),file);
        request.sendMessage(channel);
        return  MessageBuffer.readMessage(channel);
    }

    /**
     * Method to get the MulticastAddress and port for the chatRoom
     * @param docName document the chat room belongs to
     * @return MessageBuffer with the result
     * @throws IOException an error occurs while communicating with the server
     */
    /*package*/ synchronized MessageBuffer chatRoom(String docName) throws IOException{
        MessageBuffer request=MessageBuffer.createMessageBuffer(Operation.CHAT_ROOM,docName.getBytes());
        request.sendMessage(channel);
        return MessageBuffer.readMessage(channel);
    }

    /**
     * Method to get the serverAddress
     * @return server remote address
     * @throws IOException if an error occurs while getting the Remote Address
     */
    /*package*/ String getRemoteAddress() throws IOException {
        return channel.getRemoteAddress().toString();
    }

    /**
     * Getter for the path files are stored in
     * @return path the files are stored in
     */
    /*package*/ String getFilePath(){
        return filePath;
    }

}
