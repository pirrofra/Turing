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
    END_EDIT(7),
    LOGOUT(8),
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
    INVALID_REQUEST(-15);
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

}
