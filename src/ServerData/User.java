package ServerData;

import Message.Operation;
import RemoteClientNotifier.RemoteClientNotifier;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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
/*package*/ class User {

    /**
     * Username of the user
     */
    private String username;

    /**
     * password of the user
     */
    private String password;

    /**
     * List of document the user can edit
     */
    private Vector<String> documentList;

    /**
     * boolean that keeps track if the user is logged in or not
     */
    private boolean loggedIn;

    /**
     * Document the user is currently editing, null if he's not editing anything
     */
    private String editingDocument;

    /**
     *
     */
    private int RMIPort;

    private String address;

    /**
     * number of pending notification
     */
    private int pendingNotification;

    /**
     * Class constructor
     * @param usr new Username
     * @param  psw ServerData.User's password
     * @throws IllegalArgumentException if username and/or password are null/empty string
     */
    /*package*/ User(String usr,String psw) throws IllegalArgumentException{
        if(usr==null || psw==null|| usr.compareTo("")==0||psw.compareTo("")==0) throw new IllegalArgumentException();
        username=usr;
        password=psw;
        documentList=new Vector<>();
        editingDocument=null;
        RMIPort=-1;
        pendingNotification=0;
    }

    /**
     * Username getter
     * @return user.username
     */
    /*package*/ String getUsername(){
        return username;
    }

    /**
     * Method to notify user has logged in if password is correct
     * @param psw password for login
     * @param addr Address of the client the user is connected with
     * @param port Port used by the client for RMI
     * @return Message.Operation.Already_Logged_in if user is already logged in, Message.Operation.Password_incorrect if psw!=user.password
     *         Message.Operation.OK if login successful
     * @throws IllegalArgumentException if psw is null or an empty string
     */
    /*package*/ synchronized Operation login(String psw, String addr, int port) throws IllegalArgumentException{
        if (psw==null || psw.compareTo("")==0) throw new IllegalArgumentException();
        else if(loggedIn) return Operation.ALREADY_LOGGED_IN;
        else if(psw.compareTo(password)==0){
            loggedIn=true;
            RMIPort=port;
            address=addr;
            return Operation.OK;
        }
        else return Operation.PASSWORD_INCORRECT;
    }

    /**
     * add a new document to list of available documents
     * @param Document identifier of the document
     * @throws IllegalArgumentException if document is null
     */
    /*package*/ synchronized  void addDocument (String Document) throws IllegalArgumentException{
        if(Document==null) throw new IllegalArgumentException();
        if(!documentList.contains(Document)){
            documentList.add(Document);
        }
    }

    /**
     * Method to get the list of this user's document
     * @return a vector containing all user's document
     */
    /*package*/ synchronized Vector<String> documentList(){
        return new Vector<>(documentList);
    }

    /**
     * Method to notify which document the user is editing
     * @param document document currently editing
     * @return result of the operation
     */
    /*package*/ synchronized Operation edit(String document) throws IllegalArgumentException{
        if(document==null) throw new IllegalArgumentException();
        if (editingDocument!=null) return Operation.USER_ALREADY_EDITING;
        else{
            editingDocument=document;
            return Operation.OK;
        }
    }

    /**
     * Method to notify the user has stopped editing the document
     */
    /*package*/ synchronized void endEdit(){
        editingDocument=null;
    }

    /**
     * Method to notify user has logged off
     * @return the document the user is currently editing
     */
    /*package*/ synchronized String logoff(){
        loggedIn=false;
        String doc=editingDocument;
        editingDocument=null;
        RMIPort=-1;
        address=null;
        return doc;
    }

    /**
     * Method that notify an user if an invite has been received, or it increments the number of pending notification
     * @param msg message to be sent as a notification
     */
    /*package*/ synchronized void notify(String msg){
        if(!loggedIn) ++pendingNotification;
        else{
            try{
                Registry reg= LocateRegistry.getRegistry(address,RMIPort);
                RemoteClientNotifier notifier = (RemoteClientNotifier) reg.lookup("NOTIFIER-TURING");
                notifier.notify(msg);
            }
            catch (IOException | NotBoundException e){
                ++pendingNotification;
            }
        }
    }

    /**
     * Method that sends all pending notification to an user if the number of pending notification in more than 0
     */
    /*package*/ synchronized void sendPendingNotification(){
        if(loggedIn && pendingNotification>0){
            String msg="You had " + pendingNotification +" invite while you where away";
            try{
                Registry reg= LocateRegistry.getRegistry(address,RMIPort);
                RemoteClientNotifier notifier = (RemoteClientNotifier) reg.lookup("NOTIFIER-TURING");
                notifier.notify(msg);
                pendingNotification=0;
            }
            catch (IOException | NotBoundException e) {
                //do nothing
            }
        }
    }

}
