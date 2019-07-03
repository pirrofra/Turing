
import ClientGui.MainForm;
import RequestExecutor.RequestExecutor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Properties;

public class TuringClient{

    /**
     * server TCP port, received from configuration file
     */
    private static int portTCP;

    /**
     * server RMI port, received from configuration file
     */
    private static int portRMI;

    private static int portNotifier;

    /**
     * directory path used to store files received from server, received from configuration file
     */
    private static String dir;

    /**
     * properties read from configuration file
     */
    private static Properties config;

    /**
     * default properties
     */
    private static final Properties defaultConfig=new Properties();

    /**
     * Main function
     * @param args main arguments
     */
    public static void main(String[] args){
        if(args.length!=1){
            System.out.println("Usage: Client serverAddress");
            System.exit(-1);
        }
        setDefault();
        config=new Properties(defaultConfig);
        String server=args[0]; //get server Address
        MainForm form;
        RequestExecutor exec;
        SocketChannel channel=null;
        try{
            setProperties(); //read properties
            channel=SocketChannel.open(); //open Socket
            InetAddress serverAddress= InetAddress.getByName(server);//set socket
            SocketAddress SocketAddress=new InetSocketAddress(serverAddress,portTCP);
            channel.connect(SocketAddress); //connect to the server
        }
        catch (IOException e){
            //An error occurs, the app is closed
            e.printStackTrace();
            System.exit(-1);
        }
        exec=new RequestExecutor(channel,server,portRMI,dir); //new Request Executor is created
        form=new MainForm(exec,portNotifier); //new MainForm is created
        form.initialize(); //mainForm is initialized
        try{
            form.open(); //MainForm is opened
        }
        catch (IOException e){
            //opening failed, app closed
            e.printStackTrace();
            System.exit(-1);
        }

    }

    /**
     * Method that sets defaultConfig with default values
     */
    private static void setDefault(){
        defaultConfig.setProperty("portTCP","55432");
        defaultConfig.setProperty("portRMI","55431");
        defaultConfig.setProperty("portNotifier","1099");
        defaultConfig.setProperty("dirFiles","clientFiles/");
    }

    /**
     * Method that read from clientConfig.ini and retrieves configuration options
     * @throws IOException while reading clientConfig.ini
     */
    private static void setProperties() throws IOException{
        try{
            FileInputStream input=new FileInputStream("clientConfig.ini");
            config.load(input);
            input.close();
        }
        catch (FileNotFoundException e){
            FileOutputStream output=new FileOutputStream("clientConfig.ini",false);
            defaultConfig.store(output,"DEFAULT VALUES");
            output.close();
        }
        portTCP=getIntegerProperty("portTCP");
        portRMI=getIntegerProperty("portRMI");
        portNotifier=getIntegerProperty("portNotifier");
        portNotifier=getIntegerProperty("portNotifier");
        dir=config.getProperty("dirFiles");
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
}
