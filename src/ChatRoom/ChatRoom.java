package ChatRoom;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * This class implements a thread that listen on a DatagramChannel, and appends received message on a JTextArea
 * sendMessage uses the same DatagramChannel to send messages to a multicast address
 *
 * @author Francesco Pirrò - Matr.544539
 */
public class ChatRoom extends Thread {

    /**
     * DatagramChannel used to receive and send messages
     */
    private DatagramChannel channel;

    /**
     * Multicast address of the chat room
     */
    private InetSocketAddress chatGroup;

    /**
     * JTextArea used to store received messages
     */
    private final JTextArea chatBox;

    /**
     * username of the user sending messages
     */
    private final String user;

    /**
     * public class constructor
     * constructor opens DatagramChannel and sets Option
     * @param multicastGroup multicast address of the chat room
     * @param port port number of the chat room
     * @param box JTextArea to store messages in
     * @param username user sending messages
     * @throws IOException  if it fails to open datagramChannel
     */
    public ChatRoom(String multicastGroup,int port,JTextArea box,String username)throws IOException{
        super();
        chatBox=box;
        user=username;
        channel=DatagramChannel.open(StandardProtocolFamily.INET);
        System.setProperty("java.net.preferIPv4Stack", "true");//Force DatagramChannel to use IPv4 Socket
        NetworkInterface ni=NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        channel.setOption(StandardSocketOptions.IP_MULTICAST_IF,ni);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR,true);
        channel.setOption(StandardSocketOptions.IP_MULTICAST_TTL,1); //Set ttl to 1, so user datagram doesn't exit local network
        chatGroup=new InetSocketAddress(multicastGroup,port);
        InetAddress groupAddress=InetAddress.getByName(multicastGroup);
        channel.bind(new InetSocketAddress(port)); //bind socket
        try{
            channel.join(groupAddress,ni); //join group
        }
        catch (IllegalArgumentException e){
            chatBox.append("---NOT A MULTICAST ADDRESS---");
        }

    }

    /**
     * Thread main
     */
    public void run() {
        String welcome=user+" has joined the chat";
        try{
            send(ByteBuffer.wrap(welcome.getBytes())); //send welcome message
        }catch (IOException e){
            chatBox.append("--- CHAT CRASHED ---\n");
            interrupt();
        }
        while(!Thread.interrupted()){
            ByteBuffer buffer=ByteBuffer.allocate(1024);
            try{
                channel.receive(buffer);
                //DatagramChannel is in blocking mode, so it waits until it receive a message
                buffer.flip();
                String msg=new String(buffer.array());
                chatBox.append(msg+"\n");
            }
            catch (IOException e){
                chatBox.append("--- CHAT CRASHED ---\n");
                break;
            }

            chatBox.setCaretPosition(chatBox.getText().length()); //set Caret position to the  bottom of the JtextArea
        }
        try{
            channel.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Method that sends a message to the group
     * @param message message to be sent
     * @throws IOException if an error occurs while sending the message
     */
    public void sendMessage(String message) throws IOException{
        String msg="["+user+"]: "+message;
        msg=msg.substring(0,Math.min(1024,msg.length()));
        byte[] buffer=msg.getBytes();
        send(ByteBuffer.wrap(buffer));
    }

    /**
     * send content of a byteBuffer on a datagramChannel
     * @param msg byteBuffer to be sent
     * @throws IOException if an error occurs while sending the buffer content
     */
    private void send(ByteBuffer msg)throws IOException{
        while(msg.hasRemaining()){
            channel.send(msg,chatGroup);
        }
    }


}
