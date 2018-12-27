package ServerData;

import Message.Operation;
import RemoteUserTable.RemoteUserTable;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
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
public class UserTable extends RemoteServer implements RemoteUserTable, Serializable {

    //TODO:Serializzazione

    private ConcurrentHashMap<String,User> userMap;

    /**
     * Class Constructor with no parameters
     */
    public UserTable(){
        userMap=new ConcurrentHashMap<>();
    }

    /**
     * Class Constructor
     * @param initialCapacity userMap initial capacity
     * @param loadFactor userMap loadFactor
     * @param concurrencyLevel userMap max number of concurrent access
     */
    public UserTable(int initialCapacity, float loadFactor, int concurrencyLevel){
        userMap=new ConcurrentHashMap<>(initialCapacity,loadFactor,concurrencyLevel);
    }

    /**
     * Method for registering a new user
     * @param username new username
     * @param password new password
     * @return Message.Operation.Ok if successful, Message.Operation.Username_Not_availabe if the username is already taken
     * @throws RemoteException Exception thrown by rmi support
     * @throws IllegalArgumentException  if username and/or password are null
     */
    public Operation registerUser(String username, String password) throws RemoteException, IllegalArgumentException {
        User newUser=new User(username,password);
        if(userMap.putIfAbsent(username,newUser)==null) return Operation.OK;
        else return Operation.NAME_NOT_AVAILABLE;
    }

    /**
     * Method for logging in
     * @param username username
     * @param password password
     * @return Message.Operation.User_Not_Found if username is not an existing user,
     *         Message.Operation.Already_Logged_in if user is already logged in,
     *         Message.Operation.Password_incorrect if the password is incorrect,
     *         Message.Operation.OK if login successful
     * @throws IllegalArgumentException if password and/or username are null
     */
    /*package*/ Operation logIn(String username,String password) throws  IllegalArgumentException{
        if(username==null) throw new IllegalArgumentException();
        User user=userMap.get(username);
        if(user==null) return Operation.USER_NOT_FOUND;
        else return user.login(password);
    }

    /**
     * add a ServerData.Document to an user
     * @param username user
     * @param document document to add
     * @return Message.Operation.User_not_found if username is not an existing user,
     *          Message.Operation.User_Already_invited if document has already been added, Message.Operation.OK if successful
     * @throws IllegalArgumentException if username and/or document are null
     */
    /*package*/ Operation addDocument(String username,String document) throws IllegalArgumentException{
        if(username==null) throw new IllegalArgumentException();
        User user=userMap.get(username);
        if(user==null) return Operation.USER_NOT_FOUND;
        else return user.addDocument(document);
    }

    /**
     * Method to get list of Documents
     * @param username name of the user
     * @return string which contains all documents
     * @throws IllegalArgumentException if username is null
     */
    /*package*/ String getList(String username) throws IllegalArgumentException{
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
}
