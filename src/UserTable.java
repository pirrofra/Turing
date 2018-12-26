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
 * UserTable extends RemoteServer to make possible the Remote Method Invocation of registerUser
 *
 * @author Francesco Pirrò - Matr. 544539
 */
public class UserTable extends RemoteServer implements RemoteUserTable, Serializable {


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
     * @return Operation.Ok if successful, Operation.Username_Not_availabe if the username is already taken
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
     * @return Operation.User_Not_Found if username is not an existing user,
     *         Operation.Already_Logged_in if user is already logged in,
     *         Operation.Password_incorrect if the password is incorrect,
     *         Operation.OK if login successful
     * @throws IllegalArgumentException if password and/or username are null
     */
    public Operation logIn(String username,String password) throws  IllegalArgumentException{
        if(username==null) throw new IllegalArgumentException();
        User user=userMap.get(username);
        if(user==null) return Operation.USER_NOT_FOUND;
        else return user.login(password);
    }

    /**
     * add a Document to an user
     * @param username user
     * @param document document to add
     * @return Operation.User_not_found if username is not an existing user,
     *          Operation.User_Already_invited if document has already been added, Operation.OK if successful
     * @throws IllegalArgumentException if username and/or document are null
     */
    public Operation addDocument(String username,String document) throws IllegalArgumentException{
        if(username==null) throw new IllegalArgumentException();
        User user=userMap.get(username);
        if(user==null) return Operation.USER_NOT_FOUND;
        else return user.addDocument(document);
    }
}
