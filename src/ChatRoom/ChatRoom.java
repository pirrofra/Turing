package ChatRoom;

import javax.swing.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ChatRoom implements Runnable {

    private DatagramChannel channel;
    private InetSocketAddress chatGroup;
    private JTextArea chatBox;

    public ChatRoom(String multicastGroup,int port,JTextArea box)throws IOException{
        chatBox=box;
        channel=DatagramChannel.open();
        NetworkInterface ni=NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        channel.setOption(StandardSocketOptions.IP_MULTICAST_IF,ni);
        chatGroup=new InetSocketAddress(multicastGroup,port);
    }

    public void run() {
        ByteBuffer buffer=ByteBuffer.allocate(2048);
        while(!Thread.interrupted()){
            buffer.clear();
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

    public void sendMessage(String user,String message) throws IOException{
        String msg="["+user+"]: "+message;
        byte[] buffer=msg.getBytes();
        send(ByteBuffer.wrap(buffer,0,2048));
    }

    private void send(ByteBuffer msg)throws IOException{
        while(msg.hasRemaining()){
            channel.send(msg,chatGroup);
        }
    }

}
