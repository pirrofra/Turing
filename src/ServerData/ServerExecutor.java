package ServerData;

import Message.MessageBuffer;
import Message.Operation;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ServerExecutor implements Runnable {

    //TODO:Commenti

    private UserTable users;
    private DocumentTable documents;
    private SocketChannel socket;
    private ConcurrentHashMap<SocketChannel,String> connectedUsers;
    private BlockingQueue<SocketChannel> selectorKeys;

    public ServerExecutor(ServerData data, SocketChannel sock, BlockingQueue<SocketChannel> queue){
        users=data.getUserTable();
        documents=data.getDocumentTable();
        connectedUsers=data.getConnectedUsers();
        socket=sock;
        selectorKeys=queue;
    }

    private MessageBuffer login(Vector<byte[]> Args) throws IllegalArgumentException{
       if(Args.size()!=2) throw new IllegalArgumentException();
       String username=new String(Args.get(0));
       String password=new String(Args.get(1));
       Operation result=users.logIn(username,password);
       if (result==Operation.OK) connectedUsers.put(socket,username);
       return MessageBuffer.createMessageBuffer(result);
    }

    private MessageBuffer create(Vector<byte[]> Args,String user) throws IllegalArgumentException{
        if(Args.size()!=2) throw new IllegalArgumentException();
        String docName=new String(Args.get(0));
        int numSection= ByteBuffer.wrap(Args.get(1)).getInt();
        Operation result=documents.createDocument(docName,user,numSection);
        return MessageBuffer.createMessageBuffer(result);
    }

    private MessageBuffer invite( Vector<byte[]> Args,String user) throws  IllegalArgumentException{
        if(Args.size()!=2) throw new IllegalArgumentException();
        String docName=new String(Args.get(0));
        String invited=new String(Args.get(1));
        Operation result=documents.invite(docName,user,invited);
        if(result==Operation.OK) users.addDocument(user,docName);
        return MessageBuffer.createMessageBuffer(result);
    }

    private MessageBuffer list(Vector<byte[]> Args,String user) throws IllegalArgumentException{
        if(Args.size()!=0) throw new IllegalArgumentException();
        String docList=users.getList(user);
        return MessageBuffer.createMessageBuffer(Operation.OK,docList.getBytes());
    }

    private MessageBuffer show(Vector<byte[]>Args,String user) throws IllegalArgumentException{
        if(Args.size()<1||Args.size()>2) throw new IllegalArgumentException();
        String docName=new String(Args.get(0));
        if(Args.size()==1) return documents.show(docName,user);
        else{
            int section=ByteBuffer.wrap(Args.get(1)).getInt();
            return documents.show(docName,user,section);
        }
    }

    private MessageBuffer edit(Vector<byte[]> Args,String user) throws IllegalArgumentException{
        if(Args.size()!=2) throw new IllegalArgumentException();
        String docName=new String(Args.get(0));
        int section=ByteBuffer.wrap(Args.get(1)).getInt();
        Operation result=users.edit(user,docName);
        if(result==Operation.OK) return documents.edit(docName,user,section);
        else{
            users.endEdit(user);
            return MessageBuffer.createMessageBuffer(result);
        }
    }

    private MessageBuffer end_edit(Vector<byte[]> Args,String user) throws IllegalArgumentException{
        if(Args.size()!=3) throw new IllegalArgumentException();
        String docName=new String(Args.get(0));
        int section=ByteBuffer.wrap(Args.get(1)).getInt();
        Operation result=documents.endEdit(docName,user,section,Args.get(2));
        users.endEdit(user);
        return MessageBuffer.createMessageBuffer(result);
    }

    private void logout(){
        String user=connectedUsers.remove(socket);
        if(user!=null)  {
            String doc=users.logoff(user);
            if(doc!=null) documents.abruptStop(doc,user);
        }
    }

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
                case END_EDIT:  System.out.println("End EDit Document request received from "+user);
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

    public void run() {
        try {
            MessageBuffer request=MessageBuffer.readMessage(socket);
            MessageBuffer reply=execute(request);
            reply.sendMessage(socket);
            selectorKeys.put(socket);
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

    }
}
