package ChatRoom;

import java.util.HashSet;
import java.util.Random;

/**
 * This class is used to generate Multicast Address in a range specified by a Base Address and a bound
 * It keeps track of which address are currently in use, so getNewAddress() js never an address already in use
 *
 * Base address is stored as an integer value, so it can easily generate random address by adding a random integer between 0 and bound
 * The integer value is then converted in a String which contains the address in dotted notation
 *
 * This generator only works with IPv4 address, which are 32-bit address
 *
 * @author Francesco Pirrò - Matr.544539
 */
public class ChatOrganizer {

    /**
     * sets that keeps track of address in use
     */
    private final HashSet<String> usedAddress;

    /**
     * port number used for multicast chat room
     */
    private final int port;

    /**
     * base address, stored as int
     */
    private int base;

    /**
     * bound of the range
     */
    private final int bound;

    /**
     * Random number generator
     */
    private final Random randomGenerator;

    /**
     * class constructor
     * @param baseAddress baseAddress for the range of address to be generated
     * @param boundNum range of address to be generated
     * @param p port to be used for the multicast chat
     */
    public  ChatOrganizer(String baseAddress,int boundNum,int p){
        usedAddress=new HashSet<>();
        base=0;
        String[] ipAddressSplit=baseAddress.split("\\.");
        //Convert the address from dotted notation to integer
        for(int i=3;i>=0;i--){
            int value= Integer.parseInt(ipAddressSplit[3-i]);
            base|= value<<(i*8);
        }
        bound=boundNum;
        port=p;
        randomGenerator=new Random(System.currentTimeMillis());
    }

    /**
     * Method that returns a free address in the range
     * @return free address, null if the range is full
     */
    public synchronized String getNewAddress(){
        if(usedAddress.size()==bound &&bound!=0) return null;
        if(usedAddress.size()==1 && bound==0) return null;
        while(true){
            //Generate a new address, until it finds a free one
            String newAddress=getRandomAddress();
            if(!usedAddress.contains(newAddress)){
                usedAddress.add(newAddress);
                return newAddress;
            }
        }
    }

    /**
     * Method that sets an address as free
     * @param address address to be set as free
     */
    public synchronized void closeRoom(String address){
        usedAddress.remove(address);
    }

    /**
     * private method that generate a random address in rage
     * doesn't check if the generated address is free or not
     * @return random address
     */
    private String getRandomAddress() {
        int value= base+Math.abs(randomGenerator.nextInt(bound));
        //Converts the address form integer to dotted notation
        return((value >> 24 ) & 0xFF) + "." +
                ((value >> 16 ) & 0xFF) + "." +
                ((value >>  8 ) & 0xFF) + "." +
                ( value        & 0xFF);
    }

    /**
     * Getter for the port number
     * @return port number
     */
    public int getPort(){
        return port;
    }
}
