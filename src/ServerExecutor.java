import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ServerExecutor implements Runnable {

    //TODO:Commenti

    private UserTable users;
    private DocTable documents;
    private SocketChannel socket;
    private ConcurrentHashMap<SocketChannel,String> connectedUsers;
    private BlockingQueue<SocketChannel> selectorKeys;

    public ServerExecutor(UserTable u, DocTable doc, SocketChannel sock,ConcurrentHashMap<SocketChannel,String>map,BlockingQueue<SocketChannel> queue){
        users=u;
        documents=doc;
        socket=sock;
        connectedUsers=map;
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

    private MessageBuffer create(Vector<byte[]> Args) throws IllegalArgumentException{
        if(Args.size()!=2) throw new IllegalArgumentException();
        String user=connectedUsers.get(socket);
        if(user==null) return MessageBuffer.createMessageBuffer(Operation.CLIENT_NOT_LOGGED_IN);
        String docName=new String(Args.get(0));
        int numSection= ByteBuffer.wrap(Args.get(1)).getInt();
        Operation result=documents.createDocument(docName,user,numSection);
        return MessageBuffer.createMessageBuffer(result);
    }

    private MessageBuffer invite( Vector<byte[]> Args) throws  IllegalArgumentException{
        if(Args.size()!=2) throw new IllegalArgumentException();
        String user=connectedUsers.get(socket);
        if(user==null) return  MessageBuffer.createMessageBuffer(Operation.CLIENT_NOT_LOGGED_IN);
        String docName=new String(Args.get(0));
        String invited=new String(Args.get(1));
        Operation result=documents.invite(docName,user,invited);
        if(result==Operation.OK) users.addDocument(user,docName);
        return MessageBuffer.createMessageBuffer(result);
    }

    private MessageBuffer list(Vector<byte[]> Args) throws IllegalArgumentException{
        if(Args.size()!=0) throw new IllegalArgumentException();
        String user=connectedUsers.get(socket);
        if(user==null) return  MessageBuffer.createMessageBuffer(Operation.CLIENT_NOT_LOGGED_IN);
        String docList=users.getList(user);
        return MessageBuffer.createMessageBuffer(Operation.OK,docList.getBytes());
    }

    private MessageBuffer show(Vector<byte[]>Args) throws IllegalArgumentException{
        if(Args.size()!=1) throw new IllegalArgumentException();
        String user=connectedUsers.get(socket);
        if(user==null) return MessageBuffer.createMessageBuffer(Operation.CLIENT_NOT_LOGGED_IN);
        String docName=new String(Args.get(0));
        return documents.show(docName,user);
    }

    private MessageBuffer edit(Vector<byte[]> Args) throws IllegalArgumentException{
        if(Args.size()!=2) throw new IllegalArgumentException();
        String user=connectedUsers.get(socket);
        if(user==null) return MessageBuffer.createMessageBuffer(Operation.CLIENT_NOT_LOGGED_IN);
        String docName=new String(Args.get(0));
        int section=ByteBuffer.wrap(Args.get(1)).getInt();
        return documents.edit(docName,user,section);
    }

    private MessageBuffer end_edit(Vector<byte[]> Args) throws IllegalArgumentException{
        if(Args.size()!=3) throw new IllegalArgumentException();
        String user=connectedUsers.get(socket);
        if(user==null) return MessageBuffer.createMessageBuffer(Operation.CLIENT_NOT_LOGGED_IN);
        String docName=new String(Args.get(0));
        int section=ByteBuffer.wrap(Args.get(1)).getInt();
        Operation result=documents.endEdit(docName,user,section,Args.get(2));
        return MessageBuffer.createMessageBuffer(result);
    }

    private void logout(){
        String user=connectedUsers.remove(socket);
        if(user!=null) users.logoff(user);
    }

    private MessageBuffer execute(MessageBuffer msg){
        Vector<byte[]> Args=msg.getArgs();
        MessageBuffer reply;
        try{
            switch (msg.getOP()){
                case LOGIN: reply=login(Args);
                break;
                case CREATE: reply=create(Args);
                break;
                case SHOW: reply=show(Args);
                break;
                case LIST: reply=list(Args);
                break;
                case INVITE: reply=invite(Args);
                break;
                case EDIT: reply=edit(Args);
                break;
                case END_EDIT: reply=end_edit(Args);
                break;
                default:reply=MessageBuffer.createMessageBuffer(Operation.INVALID_REQUEST);
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
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }
}
