import ClientGui.MainForm;
import ClientGui.RequestExecutor;

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


    private static int portTCP;
    private static int portRMI;
    private static String dir;

    private static Properties config;
    private static final Properties defaultConfig=new Properties();

    public static void main(String[] args){
        if(args.length!=1){
            System.out.println("Usage: Client serverAddress");
            System.exit(-1);
        }
        setDefault();
        config=new Properties(defaultConfig);
        String server=args[0];
        MainForm form;
        RequestExecutor exec;
        SocketChannel channel=null;
        try{
            setProperties();
            channel=SocketChannel.open();
            InetAddress serverAddress= InetAddress.getByName(server);
            SocketAddress SocketAddress=new InetSocketAddress(serverAddress,portTCP);
            channel.connect(SocketAddress);
        }
        catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
        exec=new RequestExecutor(channel,server,portRMI,dir);
        form=new MainForm(exec);
        form.initialize();
        form.open();
    }

    private static void setDefault(){
        defaultConfig.setProperty("portTCP","55432");
        defaultConfig.setProperty("portRMI","55431");
        defaultConfig.setProperty("dirFiles","clientFiles/");
    }

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
        dir=config.getProperty("dirFiles");

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
