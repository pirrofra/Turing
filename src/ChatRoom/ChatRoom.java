package ChatRoom;

import javax.swing.*;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ChatRoom extends Thread {

    private DatagramChannel channel;
    private InetSocketAddress chatGroup;
    private final JTextArea chatBox;
    private final String user;

    public ChatRoom(String multicastGroup,int port,JTextArea box,String username)throws IOException{
        super();
        chatBox=box;
        user=username;
        channel=DatagramChannel.open(StandardProtocolFamily.INET);
        System.setProperty("java.net.preferIPv4Stack", "true");
        NetworkInterface ni=NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        channel.setOption(StandardSocketOptions.IP_MULTICAST_IF,ni);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR,true);
        channel.setOption(StandardSocketOptions.IP_MULTICAST_TTL,1);
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
            ByteBuffer buffer=ByteBuffer.allocate(1024);
            try{
                channel.receive(buffer);
                buffer.flip();
                String msg=new String(buffer.array());
                chatBox.append(msg+"\n");
            }
            catch (IOException e){
                chatBox.append("--- CHAT CRASHED ---\n");
                break;
            }

            chatBox.setCaretPosition(chatBox.getText().length());
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
        msg=msg.substring(0,Math.min(1024,msg.length()));
        byte[] buffer=msg.getBytes();
        send(ByteBuffer.wrap(buffer));
    }

    private void send(ByteBuffer msg)throws IOException{
        while(msg.hasRemaining()){
            channel.send(msg,chatGroup);
        }
    }


}
