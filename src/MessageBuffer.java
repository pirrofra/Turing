import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Vector;

public class MessageBuffer {

    //TODO: COMMENTI A MESSAGEBUFFER

    private Operation OP;
    private int dimension;
    private ByteBuffer body;

    private MessageBuffer(Operation operation, ByteBuffer buff){
        OP=operation;
        if(buff==null){
            dimension=0;
            body=null;
        }
        else{
            dimension=buff.limit();
            body=buff;
        }
    }

    public static MessageBuffer createMessageBuffer(Operation operation, byte[]... Args){
        int dim=0;
        ByteBuffer body=null;
        for(byte[] array:Args){
            dim+=array.length+4;
        }
        if(dim!=0) {
            body=ByteBuffer.allocate(dim);
            for(byte[] array:Args){
                body.putInt(array.length);
                body.put(array);
            }
            body.flip();
        }
        return new MessageBuffer(operation,body);
    }

    public static MessageBuffer createMessageBuffer(Operation operation, ByteBuffer file){
        if(operation!=Operation.OK && operation!=Operation.END_EDIT) return null;
        else return new MessageBuffer(operation,file);
    }

    public static MessageBuffer readMessage(SocketChannel socket) throws IOException{
        ByteBuffer header=ByteBuffer.allocate(8);
        read(socket,header,8);
        header.flip();
        int value=header.getInt();
        Operation op=Operation.valueOf(value);
        int dimension=header.getInt();
        ByteBuffer body=ByteBuffer.allocate(dimension);
        read(socket,body,dimension);
        body.flip();
        return new MessageBuffer(op,body);
    }


    public void sendMessage(SocketChannel socket) throws IOException{
        ByteBuffer header=ByteBuffer.allocate(8);
        header.putInt(OP.value);
        header.putInt(dimension);
        header.flip();
        write(socket,header,8);
        write(socket,body,dimension);
        body.flip();
    }

    private static void read(SocketChannel socket, ByteBuffer buff,int size) throws IOException {
        while(size>0){
            int byte_read= socket.read(buff);
            if(byte_read<0) throw new IOException();
            size -=byte_read;
        }
    }

    private static void write(SocketChannel socket, ByteBuffer buff, int size) throws IOException{
        while(size>0){
            int byte_wrote= socket.write(buff);
            if(byte_wrote<0) throw new IOException();
            size -=byte_wrote;
        }
    }

    public Operation getOP(){
        return OP;
    }

    public Vector<byte[]> getArgs(){
        if(body==null) return null;
        Vector<byte[]> argsVector=new Vector<>();
        while(body.hasRemaining()){
            int dimension=body.getInt();
            byte[] arg=new byte[dimension];
            body.get(arg);
            argsVector.add(arg);
        }
        body.flip();
        return argsVector;
    }

}

