package ServerData;

import Message.Operation;
import RemoteUserTable.RemoteUserTable;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.rmi.server.RemoteServer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to store all users' data
 * Users' data are stored in instances of the UserClass in a Map
 *
 * Methods for logging in, logging out, registering a new user and add a add a new document to a new user are implemented
 *
 * ServerData.UserTable extends RemoteServer to make possible the Remote Method Invocation of registerUser
 *
 * @author Francesco Pirrò - Matr. 544539
 */
/*package*/ class UserTable extends RemoteServer implements RemoteUserTable, Serializable {

    private final ConcurrentHashMap<String,User> userMap;

    /**
     * Class Constructor with no parameters
     */
    /*package*/ UserTable() {
        userMap=new ConcurrentHashMap<>();
    }

    /**
     * Class Constructor
     * @param initialCapacity userMap initial capacity
     * @param loadFactor userMap loadFactor
     * @param concurrencyLevel userMap max number of concurrent access
     */
    /*package*/ UserTable(int initialCapacity, float loadFactor, int concurrencyLevel) {
        userMap=new ConcurrentHashMap<>(initialCapacity,loadFactor,concurrencyLevel);
    }

    /**
     * Method for registering a new user
     * @param username new username
     * @param password new password
     * @return Message.Operation.Ok if successful, Message.Operation.Username_Not_Available if the username is already taken
     * @throws IllegalArgumentException  if username and/or password are null
     */
    public Operation registerUser(String username, String password) throws IllegalArgumentException {
        User newUser=new User(username,password);
        if(username.contains("\\")||username.contains("/")||username.contains(" ")) return Operation.INVALID_CHARACTERS;
        if(userMap.putIfAbsent(username,newUser)==null) return Operation.OK;
        else return Operation.NAME_NOT_AVAILABLE;
    }

    /**
     * Method for logging in
     * @param username username
     * @param password password
     * @param address Address of the client the user is connected from
     * @return Message.Operation.User_Not_Found if username is not an existing user,
     *         Message.Operation.Already_Logged_in if user is already logged in,
     *         Message.Operation.Password_incorrect if the password is incorrect,
     *         Message.Operation.OK if login successful
     * @throws IllegalArgumentException if password and/or username are null
     */
    /*package*/ Operation logIn(String username, String password, InetSocketAddress address) throws  IllegalArgumentException{
        if(username==null) throw new IllegalArgumentException();
        User user=userMap.get(username);
        if(user==null) return Operation.USER_NOT_FOUND;
        else return user.login(password,address);
    }

    /**
     * add a ServerData.Document to an user
     * @param username user
     * @param document document to add
     * @throws IllegalArgumentException if username and/or document are null
     */
    /*package*/ void addDocument(String username, String document) throws IllegalArgumentException{
        if(username==null) throw new IllegalArgumentException();
        User user=userMap.get(username);
        if(user!=null){
            user.addDocument(document);
        }
    }

    /**
     * Method to get list of Documents
     * @param username name of the user
     * @return Vector which contains all documents
     * @throws IllegalArgumentException if username is null
     */
    /*package*/ Vector<String> getList(String username) throws IllegalArgumentException{
        if(username==null) throw new IllegalArgumentException();
        User user=userMap.get(username);
        if(user==null) return null;
        else return user.documentList();
    }

    /**
     * Method to notify the user is editing a document
     * @param username user who is currently editing the document
     * @param document document currently edited
     * @return Message.Operation.OK if successful, Message.Operation.User_already_editing if the user is currently editing another document
     * @throws IllegalArgumentException if username and/or document are null
     */
    /*package*/ Operation edit(String username,String document) throws IllegalArgumentException{
        if(username==null) throw new IllegalArgumentException();
        User user=userMap.get(username);
        if(user==null) return Operation.USER_NOT_FOUND;
        else return user.edit(document);
    }

    /**
     * Method to notify the user has stopped editing a document
     * @param username user who stopped editing
     */
    /*package*/ void endEdit(String username){
        if(username!=null){
            User user=userMap.get(username);
            if(user!=null) user.endEdit();
        }
    }

    /**
     * Method to log off an user
     * @param username user to log off
     * @return a string containing the document the user is editing, null if is not editing anything
     */
    /*package*/ String logoff(String username){
        if(username!=null){
            User user=userMap.get(username);
            if(user!=null) return user.logoff();
        }
        return null;
    }

    /*package*/ void sendNotification(String username, String msg, DatagramChannel channel){
        if(username!=null){
            User user=userMap.get(username);
            if(user!=null) user.notify(msg,channel);
        }
    }

    /*package*/ void sendPendingNotifcation(String username,DatagramChannel channel) throws IOException {
        if(username!=null){
            User user=userMap.get(username);
            if(user!=null) user.sendPendingNotification(channel);
        }
    }

    /*package*/ boolean userExist(String username){
        return userMap.containsKey(username);
    }
}
