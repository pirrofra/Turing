package Message;

import java.util.HashMap;

/**
 * This enum defines types of request/response between client and server
 *
 * @author Francesco Pirrò - Matr. 544539
 */
public enum Operation {

    LOGIN(1),
    CREATE(2),
    SHOW(3),
    LIST(4),
    INVITE(5),
    EDIT(6),
    CHAT_ROOM(7),
    END_EDIT(8),
    LOGOUT(9),
    //--- REQUEST --- //
    OK(0),
    FAIL(-1),
    NAME_NOT_AVAILABLE(-2),
    ALREADY_LOGGED_IN(-3),
    PASSWORD_INCORRECT(-4),
    DOCUMENT_NOT_FOUND(-5),
    SECTION_BUSY(-6),
    EDITING_NOT_REQUESTED(-7),
    USER_ALREADY_EDITING(-8),
    USER_NOT_FOUND(-9),
    USER_ALREADY_INVITED(-10),
    PERMISSION_DENIED(-11),
    FILE_TOO_BIG(-12),
    REQUEST_INCOMPLETE(-13),
    CLIENT_NOT_LOGGED_IN(-14),
    INVALID_REQUEST(-15),
    INVALID_CHARACTERS(-16);
    // --- RESPONSE --- //

    int value;
    private static final HashMap<Integer,Operation> map=new HashMap<>();

    /**
     * Enum Constructor
     * @param v Integer Value
     */
    Operation(int v){
        value=v;
    }

   //Map between integer and enum
    static {
        for(Operation op: Operation.values()){
            map.put(op.value,op);
        }
    }

    /**
     * Method which return the correct enum given the integer value using a map
     * @param v integer value
     * @return correct enum
     */
    static Operation valueOf(int v){
        return map.get(v);
    }

    public static String getDescription(Operation OP){
        String result;
        switch (OP){
            case OK:
                result= "Operation Successful";
                break;
            case FAIL:
                result=  "Operation has failed";
                break;
            case NAME_NOT_AVAILABLE:
                result=  "This name is not available";
                break;
            case ALREADY_LOGGED_IN:
                result=  "This user is already logged in";
                break;
            case PASSWORD_INCORRECT:
                result=  "Username and/or password are incorrect";
                break;
            case DOCUMENT_NOT_FOUND:
                result=  "The document requested couldn't be found";
                break;
            case SECTION_BUSY:
                result=  "This section is already being edited";
                break;
            case EDITING_NOT_REQUESTED:
                result=  "Editing for this document not requested";
                break;
            case USER_ALREADY_EDITING:
                result=  "An editing request was already sent";
                break;
            case USER_NOT_FOUND:
                result=  "This username doesn't exist";
                break;
            case USER_ALREADY_INVITED:
                result=  "This user was already invited to edit this document";
                break;
            case PERMISSION_DENIED:
                result=  "You don't have permission to this operation";
                break;
            case FILE_TOO_BIG:
                result=  "This file is too big";
                break;
            case REQUEST_INCOMPLETE:
                result=  "The request was incomplete";
                break;
            case CLIENT_NOT_LOGGED_IN:
                result=  "This client isn't logged in yet";
                break;
            case INVALID_REQUEST:
                result=  "An invalid request was sent to the server";
                break;
            case INVALID_CHARACTERS:
                result= "Don't use any whitespaces, slash or backslash";
                break;
            default:
                result=  "An invalid response was sent by the server";
                break;
        }
        return result;
    }

}
