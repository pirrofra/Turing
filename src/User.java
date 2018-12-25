import java.util.Vector;

public class User {
    private String username;
    private String password;
    private Vector<String> documentList;
    private boolean loggedIn;

    public User(String usr,String psw) throws IllegalArgumentException{
        if(usr==null || psw==null|| usr.compareTo("")==0||psw.compareTo("")==0) throw new IllegalArgumentException();
        username=usr;
        password=psw;
        documentList=new Vector<>();
    }

    public String getUsername(){
        return username;
    }

    public synchronized boolean login(String psw) throws IllegalArgumentException{
        if (psw==null || psw.compareTo("")==0) throw new IllegalArgumentException();
        else if(loggedIn) return false;
        else if(psw.compareTo(password)==0){
            return loggedIn=true;
        }
        else return false;
    }
    
    public synchronized void logoff(){
        loggedIn=false;
    }
}
