package ChatRoom;

import java.util.HashSet;
import java.util.Random;

public class ChatOrganizer {

    private final HashSet<String> usedAddress;
    private final int port;
    private int base;
    private final int bound;
    private final Random randomGenerator;

    public  ChatOrganizer(String baseAddress,int boundNum,int p){
        usedAddress=new HashSet<>();
        base=0;
        String[] ipAddressSplit=baseAddress.split("\\.");
        for(int i=3;i>=0;i--){
            int value= Integer.parseInt(ipAddressSplit[3-i]);
            base|= value<<(i*8);
        }
        bound=boundNum;
        port=p;
        randomGenerator=new Random(System.currentTimeMillis());
    }

    public synchronized String getNewAddress(){
        if(usedAddress.size()==bound &&bound!=0) return null;
        if(usedAddress.size()==1 && bound==0) return null;
        while(true){
            String newAddress=getRandomAddress(base,bound);
            if(!usedAddress.contains(newAddress)){
                usedAddress.add(newAddress);
                return newAddress;
            }
        }
    }

    public synchronized void closeRoom(String address){
        usedAddress.remove(address);
    }


    private String getRandomAddress(int base,int bound) {
        int value= base+Math.abs(randomGenerator.nextInt(bound));
        return((value >> 24 ) & 0xFF) + "." +
                ((value >> 16 ) & 0xFF) + "." +
                ((value >>  8 ) & 0xFF) + "." +
                ( value        & 0xFF);
    }

    public int getPort(){
        return port;
    }
}
