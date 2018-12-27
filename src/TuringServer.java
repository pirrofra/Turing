import RemoteUserTable.RemoteUserTable;
import ServerData.ServerData;

import java.rmi.RemoteException;

public class TuringServer {
    //TODO: file config
    private static final int numThreads=8;
    private static final String dirPath="files/";
    private static final String bakPath="bak/";
    private static final int portRMI=55431;
    private static final int portTCP=55432;

    private static ServerData data;

    public static void main(String[] args){
        try {
            data=ServerData.createServerData(dirPath,portRMI);
        }
        catch (RemoteException e){
            e.printStackTrace();
        }
    }
}
