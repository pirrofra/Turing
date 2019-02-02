import ServerData.ServerData;
import ServerData.ServerExecutor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Turing Server implementation
 *
 * This server uses Non-Blocking NIO Channels and a selector for TCP communication
 * When a socket is ready to be read, it's de-registered from the selector and it's given to a
 * FixedThreadPool where a thread will read his message, execute his request and send a reply
 * After the request is completed the notificationChannel is added in a blocking queue, where all channels who
 * needs to be registered again in the selector are
 *
 * In this way one thread, and one thread only, can have access to the notificationChannel and no interference between read and write can happen
 *
 * This server reads a serverConfig.ini file to get its properties.
 * If no serverConfig.ini file is found or it's incomplete, default properties will be chosen
 *
 * @author Francesco Pirrò - Matr.544539
 */
public class TuringServer {

    private static final double max=Integer.MAX_VALUE*0.9;
    private static int numThreads;
    private static String dirPath;
    private static String baseAddress;
    private static int bound;
    private static int portRMI;
    private static int portTCP;
    private static int UDPPort;
    private static int timeout;
    private static int portChat;
    private static int documentSize;

    private static ServerData data;
    private static Selector selector=null;
    private static BlockingQueue<SocketChannel>queue;
    private static ThreadPoolExecutor pool;
    private static Properties config;
    private static final Properties defaultConfig=new Properties();
    private static DatagramChannel notificationChannel;

    /**
     * Server Main
     * @param args No arguments needed in this main
     */
    public static void main(String[] args){
        ServerSocketChannel dispatcher;
        queue= new LinkedBlockingQueue<>();
        setDefault();
        config=new Properties(defaultConfig);
        try {
            setProperties();
            clear();
            data=ServerData.createServerData(dirPath,documentSize,baseAddress,bound,portChat,portRMI);
            notificationChannel =DatagramChannel.open();
            notificationChannel.socket().bind(new InetSocketAddress(UDPPort));
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

    /**
     * Static Method that retrieves all notificationChannel in queue and register them to the selector again
     * @throws IOException if an error occurs during the registering process
     */
   private static void retrieveSocketChannels() throws IOException{
        Vector<SocketChannel> list=new Vector<>();
        queue.drainTo(list);
        for(SocketChannel socket:list){
            socket.register(selector,SelectionKey.OP_READ);
        }
   }

    /**
     * Static Method that open the ServerSocketChannel which can accept new connections
     * @return new ServerSocketChannel
     * @throws IOException if an error occurs during the opening process
     */
   private static ServerSocketChannel openDispatcher()throws IOException {
       SocketAddress serverAddress=new InetSocketAddress(portTCP);
       ServerSocketChannel dispatcher=ServerSocketChannel.open();
       dispatcher.bind(serverAddress);
       dispatcher.configureBlocking(false);
       return dispatcher;
   }

    /**
     * Static Method that deals with an acceptable Key given by the selected Keys from the selector
     * @param dispatcher key which result acceptable
     * @throws IOException if an error occurs during the acceptance process
     */
   private static void acceptableKey(SelectionKey dispatcher)throws IOException{
        ServerSocketChannel socket=(ServerSocketChannel)dispatcher.channel();
        SocketChannel newSocket=socket.accept();
        newSocket.configureBlocking(false);
        newSocket.register(selector,SelectionKey.OP_READ);
        System.out.println("New Connection started with "+newSocket.getRemoteAddress().toString());
   }

    /**
     * Static Method that deals with a readable key given by the selected Keys from the selector
     * @param key key which result readable
     * @throws IOException if an error occurs
     */
   private static void readableKey(SelectionKey key)throws IOException{
        SocketChannel client=(SocketChannel) key.channel();
        key.cancel();
        ServerExecutor thread=new ServerExecutor(data,client,queue,selector, notificationChannel);
       System.out.println("New request received from "+client.getRemoteAddress().toString());
        pool.execute(thread);
   }

    /**
     * Static method which select ready keys and deals with them
     * @throws IOException if an i/o error occurs
     */
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

    /**
     * Static Method which read from serverConfig.ini and set global variables with the correct values
     * @throws IOException if an error occurs while reading the files
     */
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
       baseAddress=config.getProperty("multicastBaseAddress");
       bound=getIntegerProperty("multicastBound");
       numThreads=getIntegerProperty("numThreads");
       portTCP=getIntegerProperty("portTCP");
       portRMI=getIntegerProperty("portRMI");
       timeout=getIntegerProperty("timeout");
       portChat=getIntegerProperty("portChat");
       UDPPort=getIntegerProperty("portUDP");
       documentSize=getIntegerProperty("documentSize");
       if(documentSize>max||documentSize<1023) documentSize=(int) max;
   }

    /**
     * Static Method which initialize defaultConfig with some default configuration for the server
     */
   private static void setDefault(){
       defaultConfig.setProperty("dirPath","files/");
       defaultConfig.setProperty("multicastBaseAddress","239.0.0.0");
       defaultConfig.setProperty("multicastBound","10000");
       defaultConfig.setProperty("numThreads","8");
       defaultConfig.setProperty("portUDP","55433");
       defaultConfig.setProperty("portTCP","55432");
       defaultConfig.setProperty("portRMI","55431");
       defaultConfig.setProperty("portChat","56127");
       defaultConfig.setProperty("timeout","10000");
       defaultConfig.setProperty("documentSize",Integer.toString((int)max));
   }

    /**
     * Method which return the integer corresponding to the value in serverConfig, or a default value if the value is not an integer
     * @param key Property name which should have an integer value
     * @return integer value
     */
   private static int getIntegerProperty(String key){
       try{
           return Integer.parseInt(config.getProperty(key));
       }
       catch (NumberFormatException e){
           return Integer.parseInt(defaultConfig.getProperty(key));
       }
   }


   private static void clear()throws IOException{
       Scanner in=new Scanner(System.in);
       System.out.println("Attention, the entire content of "+dirPath +" is going to be deleted.\nPress Y to continue");
       char c=in.next().charAt(0);
       if(c!='y'&& c!='Y'){
           System.out.println("Aborting operation");
           System.exit(-1);
       }
       deleteDir(Paths.get(dirPath));
   }


    private static void deleteDir(Path path) throws IOException{
        if(Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)){
            DirectoryStream<Path> files=Files.newDirectoryStream(path);
            for(Path entry:files){
                deleteDir(entry);
            }
        }
        if(Files.exists(path))
            Files.delete(path);

    }

}
