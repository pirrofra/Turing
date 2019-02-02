package ServerData;

import ChatRoom.ChatOrganizer;
import Message.MessageBuffer;
import Message.Operation;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class implements a runnable object capable of executing a request from a socket
 *
 * The run method reads a message from a socket, execute the correct request and send the reply on the same socket
 * If an error occurs while reading/writing on a socket it closes it's connection
 * When the request is complete, if the connection is still up, the socket is added to a blockingQueue
 * This blockingQueue can be used to keep track of which socket needs to be registered to a selector again
 *
 * @author Francesco Pirrò - Matr. 544539
 */
public class ServerExecutor implements Runnable {

    private final UserTable users;
    private final DocumentTable documents;
    private final ChatOrganizer chat;
    private final SocketChannel socket;
    private final ConcurrentHashMap<SocketChannel,String> connectedUsers;
    private final BlockingQueue<SocketChannel> selectorKeys;
    private final Selector selector;
    private final DatagramChannel channel;

    /**
     * Public class constructor
     * @param data ServerData containing all server information
     * @param sock Socket from where the request is happening
     * @param queue queue containing all sockets needed to be register for the selector, again
     */
    public ServerExecutor(ServerData data, SocketChannel sock, BlockingQueue<SocketChannel> queue, Selector s, DatagramChannel c){
        users=data.getUserTable();
        documents=data.getDocumentTable();
        connectedUsers=data.getConnectedUsers();
        chat=data.getChatOrganizer();
        socket=sock;
        selectorKeys=queue;
        selector=s;
        channel=c;
    }

    /**
     * Method to correctly execute a login in the service
     * @param Args Message arguments
     * @return reply message
     * @throws IllegalArgumentException if the message is incomplete or invalid
     */
    private MessageBuffer login(Vector<byte[]> Args) throws IllegalArgumentException,IOException{
        if(Args.size()!=3) throw new IllegalArgumentException();
        String username=new String(Args.get(0));
        String password=new String(Args.get(1));
        int port= ByteBuffer.wrap(Args.get(2)).getInt();
        InetSocketAddress remoteSocketAddress=(InetSocketAddress) socket.getRemoteAddress();
        InetAddress address=remoteSocketAddress.getAddress();
        InetSocketAddress udpSocket=new InetSocketAddress(address,port);
        Operation result=users.logIn(username,password,udpSocket);
        if (result==Operation.OK) connectedUsers.put(socket,username);
        return MessageBuffer.createMessageBuffer(result);
    }

    /**
     * Method to create a new document
     * @param Args Message arguments
     * @param user user who sent the request
     * @return reply message
     * @throws IllegalArgumentException if the message is incomplete or invalid
     */
    private MessageBuffer create(Vector<byte[]> Args,String user) throws IllegalArgumentException{
        if(Args.size()!=2) throw new IllegalArgumentException();
        String docName=new String(Args.get(0));
        int numSection= ByteBuffer.wrap(Args.get(1)).getInt();
        Operation result=documents.createDocument(docName,user,numSection);
        if(result==Operation.OK) users.addDocument(user,user+"/"+docName);
        return MessageBuffer.createMessageBuffer(result);
    }

    /**
     * Method to correctly execute an invite request
     * @param Args Message arguments
     * @param user user who sent the request
     * @return reply message
     * @throws IllegalArgumentException if the message is incomplete or invalid
     */
    private MessageBuffer invite( Vector<byte[]> Args,String user) throws  IllegalArgumentException{
        if(Args.size()!=2) throw new IllegalArgumentException();
        String docName=new String(Args.get(0));
        String invited=new String(Args.get(1));
        Operation result;
        if(users.userExist(invited)) result=documents.invite(docName,user,invited);
        else result=Operation.USER_NOT_FOUND;
        if(result==Operation.OK) {
            users.addDocument(invited,docName);
            String msg=user +" has invited you to edit " +docName;
            users.sendNotification(invited,msg,channel);
        }
        return MessageBuffer.createMessageBuffer(result);
    }

    /**
     * Method to correctly send a list to the user
     * @param Args Message arguments
     * @param user user who sent the request
     * @return reply message
     * @throws IllegalArgumentException if the message is incomplete or invalid
     */
    private MessageBuffer list(Vector<byte[]> Args,String user) throws IllegalArgumentException{
        if(Args.size()!=0) throw new IllegalArgumentException();
        Vector<String> docList=users.getList(user);
        StringBuilder builder=new StringBuilder();
        for(String doc:docList){
            String info=documents.getInfo(doc);
            if(info!=null) builder.append(info);
        }
        if(builder.length()==0) builder.append("You can't edit any document");
        return MessageBuffer.createMessageBuffer(Operation.OK,builder.toString().getBytes());
    }

    /**
     * Method to correctly show the user the all document/only a section
     * @param Args Message arguments
     * @param user user who sent the request
     * @return reply message
     * @throws IllegalArgumentException if the message is incomplete or invalid
     */
    private MessageBuffer show(Vector<byte[]>Args,String user) throws IllegalArgumentException{
        if(Args.size()<1||Args.size()>2) throw new IllegalArgumentException();
        String docName=new String(Args.get(0));
        if(Args.size()==1) return documents.show(docName,user);
        else{
            int section=ByteBuffer.wrap(Args.get(1)).getInt();
            return documents.show(docName,user,section);
        }
    }

    /**
     * Method to execute an edit request for a document in the service
     * @param Args Message arguments
     * @param user user who sent the request
     * @return reply message
     * @throws IllegalArgumentException if the message is incomplete or invalid
     */
    private MessageBuffer edit(Vector<byte[]> Args,String user) throws IllegalArgumentException{
        if(Args.size()!=2) throw new IllegalArgumentException();
        String docName=new String(Args.get(0));
        int section=ByteBuffer.wrap(Args.get(1)).getInt();
        Operation result=users.edit(user,docName);
        MessageBuffer message;
        boolean resetEdit=false;
        if(result==Operation.OK) {
            try{
                message= documents.edit(docName,user,section);
                if(message.getOP()!=Operation.OK) resetEdit=true;
            }
            catch (IllegalArgumentException e){
                resetEdit=true;
                message= MessageBuffer.createMessageBuffer(Operation.FAIL);
            }

        }
        else{
            if(result!=Operation.USER_ALREADY_EDITING) resetEdit=true;
            message= MessageBuffer.createMessageBuffer(result);
        }
        if(resetEdit) users.endEdit(user);
        return message;
    }

    /**
     * Method to execute an end edit request in the service
     * @param Args Message arguments
     * @param user user who sent the request
     * @return reply message
     * @throws IllegalArgumentException if the message is incomplete or invalid
     */
    private MessageBuffer end_edit(Vector<byte[]> Args,String user) throws IllegalArgumentException{
        if(Args.size()!=3) throw new IllegalArgumentException();
        String docName=new String(Args.get(0));
        int section=ByteBuffer.wrap(Args.get(1)).getInt();
        Operation result=documents.endEdit(docName,user,section,Args.get(2),chat);
        users.endEdit(user);
        return MessageBuffer.createMessageBuffer(result);
    }

    private MessageBuffer chatRoom(Vector<byte[]> Args,String user) throws IllegalArgumentException{
        if(Args.size()!=1) throw new IllegalArgumentException();
        String docName=new String(Args.get(0));
        return documents.getChatAddress(docName,user,chat);
    }

    /**
     * Method to successfully log out an user
     * If a user is editing the section of a document, this section becomes available again
     */
    private void logout(){
        String user=connectedUsers.remove(socket);
        if(user!=null)  {
            String doc=users.logoff(user);
            if(doc!=null) documents.abruptStop(doc,user,chat);
        }
    }

    private void sendPendingNotification(String user){
        try{
            users.sendPendingNotifcation(user,channel);
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

    /**
     * Method to correctly execute a request received form a socket using a MessageBuffer
     * @param msg MessageBuffer containing the request
     * @return reply message
     * @throws IOException if an error occurs during Socket Operation
     */
    private MessageBuffer execute(MessageBuffer msg)throws IOException{
        Vector<byte[]> Args=msg.getArgs();
        MessageBuffer reply;
        String user=connectedUsers.get(socket);
        if(msg.getOP()!=Operation.LOGIN && user==null) return MessageBuffer.createMessageBuffer(Operation.CLIENT_NOT_LOGGED_IN);
        try{
            switch (msg.getOP()){
                case LOGIN: System.out.println("Login request received from "+socket.getRemoteAddress().toString());
                    reply=login(Args);
                    break;
                case CREATE: System.out.println("Create Document request received from "+user);
                    reply=create(Args,user);
                    break;
                case SHOW:  System.out.println("Show Document request received from "+user);
                    reply=show(Args,user);
                    break;
                case LIST:  System.out.println("List received from "+user);
                    reply=list(Args,user);
                    break;
                case INVITE:  System.out.println("Invite request received from "+user);
                    reply=invite(Args,user);
                    break;
                case EDIT:  System.out.println("Edit Document request received from "+user);
                    reply=edit(Args,user);
                    break;
                case CHAT_ROOM: System.out.println("Join Chat request received from "+user);
                    reply=chatRoom(Args,user);
                    break;
                case END_EDIT:  System.out.println("End Edit request received from "+user);
                    reply=end_edit(Args,user);
                    break;
                case LOGOUT:  System.out.println("Logout request received from "+user);
                    reply=MessageBuffer.createMessageBuffer(Operation.OK);
                    logout();
                    break;
                default: System.out.println("Invalid request received from "+user);
                    reply=MessageBuffer.createMessageBuffer(Operation.INVALID_REQUEST);
                    break;
            }
        }catch (IllegalArgumentException e){
            reply=MessageBuffer.createMessageBuffer(Operation.REQUEST_INCOMPLETE);
        }
        return reply;
    }

    /**
     * Implementation of the run method from the Runnable Interface
     * It reads from a socket, execute the request and send a reply
     * If an error occurs during I/O operations with the socket, the connection is lost
     * And the user is logged of from the service
     */
    public void run() {
        try {
            MessageBuffer request=MessageBuffer.readMessage(socket);
            MessageBuffer reply=execute(request);
            reply.sendMessage(socket);
            selectorKeys.put(socket);
            if(request.getOP()==Operation.LOGIN) {
                String user=connectedUsers.get(socket);
                sendPendingNotification(user);
            }
        }
        catch (IOException |InterruptedException e){
            logout();
            try {
                System.out.println("Connection ended with "+socket.getRemoteAddress().toString());
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        selector.wakeup();

    }
}
