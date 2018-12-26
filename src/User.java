import java.io.Serializable;
import java.util.Vector;

/**
 * This class is used to contain basic information about Turing's Users
 * such as password, username and list of document
 *
 * A boolean "loggedIn" keeps track if the user is currently using Turing
 * Method to notify a log in and a log out are implemented in this class too
 *
 * This class is thread-safe
 *
 * @author Francesco Pirrò - Matr. 544539
 */
public class User implements Serializable {

    private String username;
    private String password;
    private Vector<String> documentList;
    private boolean loggedIn;

    /**
     * Class constructor
     * @param usr new Username
     * @param  psw User's password
     * @throws IllegalArgumentException if username and/or password are null/empty string
     */
    public User(String usr,String psw) throws IllegalArgumentException{
        if(usr==null || psw==null|| usr.compareTo("")==0||psw.compareTo("")==0) throw new IllegalArgumentException();
        username=usr;
        password=psw;
        documentList=new Vector<>();
    }

    /**
     * Username getter
     * @return user.username
     */
    public String getUsername(){
        return username;
    }

    /**
     * Method to notify user has logged in if password is correct
     * @param psw password for login
     * @return Operation.Already_Logged_in if user is already logged in, Operation.Password_incorrect if psw!=user.password
     *         Operation.OK if login successful
     * @throws IllegalArgumentException if psw is null or an empty string
     */
    public synchronized Operation login(String psw) throws IllegalArgumentException{
        if (psw==null || psw.compareTo("")==0) throw new IllegalArgumentException();
        else if(loggedIn) return Operation.ALREADY_LOGGED_IN;
        else if(psw.compareTo(password)==0){
            loggedIn=true;
            return Operation.OK;
        }
        else return Operation.PASSWORD_INCORRECT;
    }

    /**
     * add a new document to list of available documents
     * @param Document identifier of the document
     * @return Operation.User_Already_invited if document is already in documentList, Operation.OK if successful
     * @throws IllegalArgumentException if document is null
     */
    public synchronized Operation addDocument (String Document) throws IllegalArgumentException{
        if(Document==null) throw new IllegalArgumentException();
        if(documentList.contains(Document)) return Operation.USER_ALREADY_INVITED;
        else{
            documentList.add(Document);
            return Operation.OK;
        }
    }

    /**
     * Method to notify user has logged off
     */
    public synchronized void logoff(){
        loggedIn=false;
    }
}
