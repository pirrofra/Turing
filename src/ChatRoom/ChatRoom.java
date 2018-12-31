package ChatRoom;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ChatRoom extends Thread {

    private DatagramChannel channel;
    private InetSocketAddress chatGroup;
    private final JTextArea chatBox;
    private String user;

    public ChatRoom(String multicastGroup,int port,JTextArea box,String username)throws IOException{
        super();
        chatBox=box;
        user=username;
        channel=DatagramChannel.open();
        System.setProperty("java.net.preferIPv4Stack", "true");
        NetworkInterface ni=NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        channel.setOption(StandardSocketOptions.IP_MULTICAST_IF,ni);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR,true);
        chatGroup=new InetSocketAddress(multicastGroup,port);
        InetAddress groupAddress=InetAddress.getByName(multicastGroup);
        channel.bind(new InetSocketAddress(port));
        try{
            channel.join(groupAddress,ni);
        }
        catch (IllegalArgumentException e){
            chatBox.append("---NOT A MULTICAST ADDRESS---");
        }

    }

    public void run() {
        String welcome=user+" has joined the chat";
        try{
            send(ByteBuffer.wrap(welcome.getBytes()));
        }catch (IOException e){
            chatBox.append("--- CHAT CRASHED ---\n");
            interrupt();
        }
        while(!Thread.interrupted()){
            ByteBuffer buffer=ByteBuffer.allocate(2048);
            try{
                channel.receive(buffer);
            }
            catch (IOException e){
                chatBox.append("--- CHAT CRASHED ---\n");
            }
            buffer.flip();
            String msg=new String(buffer.array());
            chatBox.append(msg+"\n");
        }
        try{
            channel.close();
        }
        catch (IOException e){
            chatBox.append("--- CHAT GROUP ---\n");
        }
    }

    public void sendMessage(String message) throws IOException{
        String msg="["+user+"]: "+message;
        byte[] buffer=msg.getBytes();
        send(ByteBuffer.wrap(buffer));
    }

    private void send(ByteBuffer msg)throws IOException{
        while(msg.hasRemaining()){
            channel.send(msg,chatGroup);
        }
    }


}
