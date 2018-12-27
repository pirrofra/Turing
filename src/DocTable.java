import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to store all documents' data
 * single Document's data are stored in instances of Document class
 *
 * Method for creating document, showing document, inviting new user and requesting an edit/end_edit are implemented
 *
 * Method show and edit returns a MessageBuffer, ready to be sent using a SocketChannel
 * All others method simply return a Operation type which describes the result of the request.
 *
 * @author Francesco Pirrò - Matr. 544539
 */
public class DocTable implements Serializable {

    private ConcurrentHashMap<String,Document> docMap;
    private String docPath;

    //TODO:Show anche per la singola Sezione
    //TODO:Show deve anche indicare chi sta editando le sezioni
    //TODO:Lista e Show devono dire il numero di sezioni in cui il file è diviso

    /**
     * Class Constructor with no parameter for ConcurrentHashMap
     * @param path path where to store all documents' sections
     */
    public DocTable(String path){
        docMap=new ConcurrentHashMap<>();
        docPath=path;
    }

    /**
     * Class constructor
     * @param path path where to store all document's sections
     * @param initialCapacity hash map initial capacity
     * @param loadFactor hash map load factor
     * @param concurrencyLevel number of max concurrent access in docMap
     */
    public DocTable(String path,int initialCapacity, float loadFactor, int concurrencyLevel){
        docMap=new ConcurrentHashMap<>(initialCapacity,loadFactor,concurrencyLevel);
        docPath=path;
    }

    /**
     * Method to create a new document
     * @param name new document name
     * @param creator user who is creating new document
     * @param numSections number of sections this document has
     * @return Operation.FAIL if an I/0 Error occurs
     *         Operation.Name_not_available if the name is not available
     *         Operation.OK if successful
     * @throws IllegalArgumentException if name and/or creator are null or numsections is zero or less
     */
    public Operation createDocument(String name, String creator, int numSections) throws IllegalArgumentException{
        Document newDoc;
        try{
            newDoc=Document.createDocument(name,creator,numSections,docPath);
        }
        catch (IOException e){
            return Operation.FAIL;
        }
        if(docMap.putIfAbsent(name,newDoc)==null) return Operation.OK;
        else return Operation.NAME_NOT_AVAILABLE;
    }

    /**
     * Method to invite a new user to edit a document
     * @param document document the user invited should edit
     * @param inviter user who sent the invite request
     * @param invited user who need to be added
     * @return Operation.Document_Not_Found if the document doesn't exist or the inviter doesn't have permission to add
     *         Operation.User_already_invited if the user has already been invited
     *         Operation.Ok if successful
     * @throws IllegalArgumentException if document, inviter and/or invited are null
     */
    public Operation invite(String document, String inviter,String invited) throws IllegalArgumentException{
        if(document==null) throw new IllegalArgumentException();
        Document doc=docMap.get(document);
        if(doc==null) return Operation.DOCUMENT_NOT_FOUND;
        else return doc.invite(inviter,invited);
    }

    /**
     * Method to execute an edit request
     * @param document document wanted to be edited
     * @param user user who wants to edit document
     * @param section section wanted to be edited
     * @return MessageBuffer containing the result of the operation and the section content if successful
     *         a generic fail error is sent if an i/o error occurs
     * @throws IllegalArgumentException if document or user are null or section is not an existing section number
     */
    public MessageBuffer edit(String document,String user,int section) throws IllegalArgumentException{
        if(document==null) throw new IllegalArgumentException();
        Document doc=docMap.get(document);
        if(doc==null) return MessageBuffer.createMessageBuffer(Operation.DOCUMENT_NOT_FOUND);
        else {
            try{
                return doc.edit(section,user);
            }
            catch (IOException e){
                return MessageBuffer.createMessageBuffer(Operation.FAIL);
            }
        }
    }

    /**
     * Method to execute an end_edit request
     * @param document document wanted to be updated
     * @param user user who sent the request
     * @param section section modified
     * @param file byte array containing updated section
     * @return Operation.Document_Not_Found if the user has not been invited to edit document or if document doesn't exist
     *         Operation.Editing_Not_Request if the user didn't make an edit request first
     *         Operation.Fail if an I/O error occurs
     *         Operation.OK if successful
     * @throws IllegalArgumentException if document or user are null, section is not an existing section number or file is null
     */
    public Operation endEdit(String document,String user,int section, byte[] file) throws IllegalArgumentException{
        if(document==null||file==null) throw new IllegalArgumentException();
        Document doc=docMap.get(document);
        if(doc==null) return Operation.DOCUMENT_NOT_FOUND;
        else {
            try{
                return doc.endEdit(section,user,file);
            }
            catch (IOException e){
                return Operation.FAIL;
            }
        }
    }

    /**
     * Method to receive the entire document in a MessageBuffer
     * @param document document wanted to be shown
     * @param user user who sent the request
     * @return MessageBuffer containing the result and the entire document if successful
     *         a generic Fail error is sent if an I/0 error occurs
     * @throws IllegalArgumentException if document and/or user are null
     */
    public MessageBuffer show(String document,String user) throws IllegalArgumentException{
        if(document==null) throw new IllegalArgumentException();
        Document doc=docMap.get(document);
        if(doc==null) return MessageBuffer.createMessageBuffer(Operation.DOCUMENT_NOT_FOUND);
        else {
            try{
                return doc.show(user);
            }
            catch (IOException e){
                return MessageBuffer.createMessageBuffer(Operation.FAIL);
            }
        }
    }



}
