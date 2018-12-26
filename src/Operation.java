/**
 * This enum defines types of request/response between client and server
 *
 * @author Francesco Pirrò - Matr. 544539
 */
public enum Operation {
    LOGIN,
    CREATE,
    SHOW,
    LIST,
    INVITE,
    EDIT,
    END_EDIT,
    OK,
    FAIL,
    NAME_NOT_AVAILABLE,
    ALREADY_LOGGED_IN,
    PASSWORD_INCORRECT,
    DOCUMENT_NOT_FOUND,
    SECTION_BUSY,
    EDITING_NOT_REQUESTED,
    USER_NOT_FOUND,
    USER_ALREADY_INVITED,
    PERMISSION_DENIED,
}
