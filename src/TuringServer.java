import Message.Operation;
import ServerData.ServerData;
import ServerData.ServerExecutor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.*;

public class TuringServer {
    //TODO: Commenti

    private static int numThreads;
    private static String dirPath;
    private static  String bakPath;
    private static int portRMI;
    private static int portTCP;
    private static int timeout;

    private static ServerData data;
    private static Selector selector=null;
    private static BlockingQueue<SocketChannel>queue;
    private static ThreadPoolExecutor pool;
    private static Properties config;
    private static Properties defaultConfig=new Properties();

    public static void main(String[] args){
        ServerSocketChannel dispatcher;
        queue= new LinkedBlockingQueue<>();
        setDefault();
        config=new Properties(defaultConfig);
        try {
            setProperties();
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
        pool=(ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);
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

   private static void setProperties() throws IOException{
        try{
            FileInputStream input=new FileInputStream("serverConfig.ini");
            config.load(input);
            input.close();
        }
        catch (FileNotFoundException e){
            FileOutputStream output=new FileOutputStream("severConfig.ini",false);
            defaultConfig.store(output,"DEFAULT VALUES");
            output.close();
        }
       dirPath=config.getProperty("dirPath");
       bakPath=config.getProperty("bakPath");
       numThreads=getIntegerProperty("numThreads");
       portTCP=getIntegerProperty("portTCP");
       portRMI=getIntegerProperty("portRMI");
       timeout=getIntegerProperty("timeout");
   }

   private static void setDefault(){
       defaultConfig.setProperty("dirPath","files/");
       defaultConfig.setProperty("bakPath","bak/");
       defaultConfig.setProperty("numThreads","8");
       defaultConfig.setProperty("portTCP","55432");
       defaultConfig.setProperty("portRMI","55431");
       defaultConfig.setProperty("timeout","1000");
   }

   private static int getIntegerProperty(String key){
       try{
           return Integer.parseInt(config.getProperty(key));
       }
       catch (NumberFormatException e){
           return Integer.parseInt(defaultConfig.getProperty(key));
       }
   }

}
