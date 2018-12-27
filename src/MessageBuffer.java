import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Vector;

/**
 * This class is used to define the format of messages exchanged between server and clients
 * The Message is made of an header and a body.
 * The Header contains two integers: one identifies the correct Operation enum and the second one is the length of the body
 * The body is made of different arguments, depending on the type of message
 * Each argument is made of an integer which define the length of the argument, and the actual argument as a byte array.
 * Until all byte specified in the message header are read, new arguments are found.
 *
 * Method to read and write messages from and to the socket are implemented in this class too.
 * Constructor is private, and actual MessageBuffer instances are to be obtained from a socket or with the static Method CreateBufferMessage
 * Arguments are returned as byte array in a vector.
 *
 * @author Francesco Pirrò - Matr. 5445390
 */
public class MessageBuffer {

    private Operation OP;
    private int dimension;
    private ByteBuffer body;

    /**
     * Private class constructor
     * @param operation message type
     * @param buff ByteBuffer with message body
     */
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

    /**
     * Static Method to generate MessageBuffer
     * @param operation message type
     * @param Args multiple arguments passed as byte array
     * @return MessageBuffer ready to be sent
     */
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

    /**
     * Static Method to generate MessageBuffer
     * @param operation message type
     * @param file ByteBuffer used for the message body
     * @return a MessageBuffer ready to be sent if operation is Operation.OK or Operation.End_Edit, null otherwise
     */
    public static MessageBuffer createMessageBuffer(Operation operation, ByteBuffer file){
        if(operation!=Operation.OK && operation!=Operation.END_EDIT) return null;
        else return new MessageBuffer(operation,file);
    }

    /**
     * Static Method to read a Message from a socketChannel
     * @param socket SocketChannel from where the message has to be read
     * @return Message read
     * @throws IOException if an error occurs during i/o operations
     */
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

    /**
     * Method to send this message to a socket
     * @param socket SocketChannel to use to send this message
     * @throws IOException if an error occurs during i/o operations
     */
    public void sendMessage(SocketChannel socket) throws IOException{
        ByteBuffer header=ByteBuffer.allocate(8);
        header.putInt(OP.value);
        header.putInt(dimension);
        header.flip();
        write(socket,header,8);
        write(socket,body,dimension);
        body.flip();
    }

    /**
     * Static Method used to read data from a socketchanell
     * @param socket SocketChannel to read from
     * @param buff ByteBuffer to use to store new data
     * @param size number of byte to read from the socket
     * @throws IOException if an error occurs during i/o operations
     */
    private static void read(SocketChannel socket, ByteBuffer buff,int size) throws IOException {
        while(size>0){
            int byte_read= socket.read(buff);
            if(byte_read<0) throw new IOException();
            size -=byte_read;
        }
    }

    /**
     * Static Method used to write data in a socketchannel
     * @param socket socketchannel to write to
     * @param buff ByteBuffer containing data to write
     * @param size number of byte to write to the socket
     * @throws IOException if an error occurs during i/o operations
     */
    private static void write(SocketChannel socket, ByteBuffer buff, int size) throws IOException{
        while(size>0){
            int byte_wrote= socket.write(buff);
            if(byte_wrote<0) throw new IOException();
            size -=byte_wrote;
        }
    }

    /**
     * Getter for OP
     * @return MessageBuffer Operation
     */
    public Operation getOP(){
        return OP;
    }

    /**
     * Method which read from the buffer and store all data in buffer in a Vector
     * @return Vector of byte array
     */
    public Vector<byte[]> getArgs(){
        Vector<byte[]> argsVector=new Vector<>();
        if(body!=null){
            while(body.hasRemaining()){
                int dimension=body.getInt();
                byte[] arg=new byte[dimension];
                body.get(arg);
                argsVector.add(arg);
            }
            body.flip();
        }
        return argsVector;
    }

}

