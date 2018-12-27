import java.util.HashMap;

/**
 * This enum defines types of request/response between client and server
 *
 * @author Francesco Pirrò - Matr. 544539
 */
public enum Operation {

    //TODO: Commenti a Operation

    LOGIN(1),
    CREATE(2),
    SHOW(3),
    LIST(4),
    INVITE(5),
    EDIT(6),
    END_EDIT(7),
    //--- REQUEST --- //
    OK(0),
    FAIL(-1),
    NAME_NOT_AVAILABLE(-2),
    ALREADY_LOGGED_IN(-3),
    PASSWORD_INCORRECT(-4),
    DOCUMENT_NOT_FOUND(-5),
    SECTION_BUSY(-6),
    EDITING_NOT_REQUESTED(-7),
    USER_NOT_FOUND(-8),
    USER_ALREADY_INVITED(-9),
    PERMISSION_DENIED(-10),
    FILE_TOO_BIG(-11),
    REQUEST_INCOMPLETE(-12);
    // --- RESPONSE --- //

    int value;
    private static HashMap<Integer,Operation> map=new HashMap<>();

    Operation(int v){
        value=v;
    }

    static {
        for(Operation op: Operation.values()){
            map.put(op.value,op);
        }
    }

    static Operation valueOf(int v){
        return map.get(v);
    }

}
