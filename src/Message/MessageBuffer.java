package Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Vector;

/**
 * This class is used to define the format of messages exchanged between server and clients
 * The Message is made of an header and a body.
 * The Header contains two integers: one identifies the correct Message.Operation enum and the second one is the length of the body
 * The body is made of different arguments, depending on the type of message
 * Each argument is made of an integer which define the length of the argument, and the actual argument as a byte array.
 * Until all byte specified in the message header are read, new arguments are found.
 *
 * Method to read and write messages from and to the socket are implemented in this class too.
 * Constructor is private, and actual Message.MessageBuffer instances are to be obtained from a socket or with the static Method CreateBufferMessage
 * Arguments are returned as byte array in a vector.
 *
 * @author Francesco Pirrò - Matr. 5445390
 */
public class MessageBuffer {

    /**
     * Operation type
     */
    private final Operation OP;

    /**
     * dimension of the body
     */
    private final int dimension;

    /**
     * ByteBuffer that contains the body of the message
     */
    private final ByteBuffer body;

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
     * Static Method to generate Message.MessageBuffer
     * @param operation message type
     * @param Args multiple arguments passed as byte array
     * @return Message.MessageBuffer ready to be sent
     */
    public static MessageBuffer createMessageBuffer(Operation operation, byte[]... Args){
        int dim=0;
        ByteBuffer body=null;
        for(byte[] array:Args){
            dim+=array.length+4;//calculate dimension body
        }
        if(dim!=0) {
            body=ByteBuffer.allocate(dim);
            for(byte[] array:Args){
                body.putInt(array.length);
                body.put(array); //put all arguments in buffer
            }
            body.flip();
        }
        return new MessageBuffer(operation,body);
    }

    /**
     * Static Method to read a Message from a socketChannel
     * @param socket SocketChannel from where the message has to be read
     * @return Message read
     * @throws IOException if an description occurs during i/o operations
     */
    public static MessageBuffer readMessage(SocketChannel socket) throws IOException{
        ByteBuffer header=ByteBuffer.allocate(8);
        read(socket,header,8);
        header.flip();
        int value=header.getInt(); //read operation
        Operation op=Operation.valueOf(value);
        int dimension=header.getInt(); //read dimension of body
        ByteBuffer body=ByteBuffer.allocate(dimension);
        read(socket,body,dimension);//read body
        body.flip();
        return new MessageBuffer(op,body);
    }

    /**
     * Method to send this message to a socket
     * @param socket SocketChannel to use to send this message
     * @throws IOException if an description occurs during i/o operations
     */
    public void sendMessage(SocketChannel socket) throws IOException{
        ByteBuffer header=ByteBuffer.allocate(8);
        header.putInt(OP.value);
        header.putInt(dimension);
        header.flip();
        write(socket,header,8); //send header
        write(socket,body,dimension);//send body
    }

    /**
     * Static Method used to read data from a socketChannel
     * @param socket SocketChannel to read from
     * @param buff ByteBuffer to use to store new data
     * @param size number of byte to read from the socket
     * @throws IOException if an description occurs during i/o operations
     */
    private static void read(SocketChannel socket, ByteBuffer buff,int size) throws IOException {
        while(size>0){
            int byte_read= socket.read(buff);
            if(byte_read<0) throw new IOException();
            size -=byte_read;
        }
    }

    /**
     * Static Method used to write data in a socketChannel
     * @param socket socketChannel to write to
     * @param buff ByteBuffer containing data to write
     * @param size number of byte to write to the socket
     * @throws IOException if an description occurs during i/o operations
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
     * @return Message.MessageBuffer Message.Operation
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
                int dimension=body.getInt(); //dimension of the argument
                byte[] arg=new byte[dimension];
                body.get(arg);//actual argument
                argsVector.add(arg);
            }
            body.flip();
        }
        return argsVector;
    }

}

