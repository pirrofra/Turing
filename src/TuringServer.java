import ServerData.ServerData;
import ServerData.ServerExecutor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.*;

public class TuringServer {
    //TODO: file config
    private static final int numThreads=8;
    private static final String dirPath="files/";
    private static final String bakPath="bak/";
    private static final int portRMI=55431;
    private static final int portTCP=55432;
    private static final int timeout=1000;

    private static ServerData data;
    private static Selector selector=null;
    private static BlockingQueue<SocketChannel>queue;
    private static ThreadPoolExecutor pool;

    public static void main(String[] args){
        ServerSocketChannel dispatcher;
        queue= new LinkedBlockingQueue<>();
        pool=(ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);
        try {
            data=ServerData.createServerData(dirPath,portRMI);
            dispatcher= openDispatcher();
            selector= Selector.open();
            dispatcher.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Turing Server Started at " + InetAddress.getLocalHost().toString()+":"+portTCP);
        }
        catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
        while(!Thread.interrupted()){
            try{
                select();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

   }

   private static void retrieveSocketChannels() throws IOException{
        Vector<SocketChannel> list=new Vector<>();
        queue.drainTo(list);
        for(SocketChannel socket:list){
            socket.register(selector,SelectionKey.OP_READ);
        }
   }

   private static ServerSocketChannel openDispatcher()throws IOException {
       SocketAddress serverAddres=new InetSocketAddress(portTCP);
       ServerSocketChannel dispatcher=ServerSocketChannel.open();
       dispatcher.bind(serverAddres);
       dispatcher.configureBlocking(false);
       return dispatcher;
   }

   private static void acceptableKey(SelectionKey dispatcher)throws IOException{
        ServerSocketChannel socket=(ServerSocketChannel)dispatcher.channel();
        SocketChannel newSocket=socket.accept();
        newSocket.configureBlocking(false);
        newSocket.register(selector,SelectionKey.OP_READ);
        System.out.println("New Connection started with "+newSocket.getRemoteAddress().toString());
   }

   private static void readableKey(SelectionKey key)throws IOException{
        SocketChannel client=(SocketChannel) key.channel();
        key.cancel();
        ServerExecutor thread=new ServerExecutor(data,client,queue);
       System.out.println("New request received from "+client.getRemoteAddress().toString());
        pool.execute(thread);
   }

   private static void select() throws IOException{
       retrieveSocketChannels();
       selector.select(timeout);
       Set<SelectionKey> selectionKeySet=selector.selectedKeys();
       Iterator<SelectionKey> iterator=selectionKeySet.iterator();
       while(iterator.hasNext()){
           SelectionKey key=iterator.next();
           if(key.isAcceptable()) acceptableKey(key);
           else if(key.isReadable()) readableKey(key);
           iterator.remove();
       }
   }

}
