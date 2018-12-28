package ServerData;

import Message.Operation;

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
/*package*/ class User implements Serializable {

    private String username;
    private String password;
    private Vector<String> documentList;
    private boolean loggedIn;
    private String editingDocument;

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
     * @return Message.Operation.Already_Logged_in if user is already logged in, Message.Operation.Password_incorrect if psw!=user.password
     *         Message.Operation.OK if login successful
     * @throws IllegalArgumentException if psw is null or an empty string
     */
    /*package*/ synchronized Operation login(String psw) throws IllegalArgumentException{
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
        return doc;
    }
}
