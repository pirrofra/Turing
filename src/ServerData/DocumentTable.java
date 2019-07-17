package ServerData;

import ChatRoom.ChatOrganizer;
import Message.MessageBuffer;
import Message.Operation;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to store all documents' data
 * single ServerData.Document's data are stored in instances of ServerData.Document class
 *
 * Method for creating document, showing document, inviting new user and requesting an edit/end_edit are implemented
 *
 * Method show and edit returns a Message.MessageBuffer, ready to be sent using a SocketChannel
 * All others method simply return a Message.Operation type which describes the result of the request.
 *
 * @author Francesco Pirrò - Matr. 544539
 */
/*package*/ class DocumentTable {

    /**
     * HashMap that maps Document's name and Document instances that keeps track of document's info
     */
    private final ConcurrentHashMap<String,Document> docMap;

    /**
     * Path where document are stored
     */
    private final String docPath;

    /**
     * maximum size of a document. At the time of creation this size is divided equally between each section
     */
    private final int maxSize;

    /**
     * Class Constructor with no parameter for ConcurrentHashMap
     * @param path path where to store all documents' sections
     * @param max maximum size for a document
     */
    /*package*/ DocumentTable(String path,int max){
        docMap=new ConcurrentHashMap<>();
        docPath=path;
        maxSize=max;
    }

    /**
     * Class constructor
     * @param path path where to store all document's sections
     * @param max document maximum size
     * @param initialCapacity hash map initial capacity
     * @param loadFactor hash map load factor
     * @param concurrencyLevel number of max concurrent access in docMap
     */
    /*package*/ DocumentTable(String path,int max, int initialCapacity, float loadFactor, int concurrencyLevel){
        docMap=new ConcurrentHashMap<>(initialCapacity,loadFactor,concurrencyLevel);
        docPath=path;
        maxSize=max;
    }

    /**
     * Method to create a new document
     * the new document id is "creator/name"
     * @param name new document name
     * @param creator user who is creating new document
     * @param numSections number of sections this document has
     * @return Message.Operation.FAIL if an I/0 Error occurs
     *         Message.Operation.Name_not_available if the name is not available
     *         Message.Operation.OK if successful
     * @throws IllegalArgumentException if name and/or creator are null or numSections is zero or less
     */
    /*package*/ Operation createDocument(String name, String creator, int numSections) throws IllegalArgumentException{
        Document newDoc=Document.createDocument(name,creator,numSections,maxSize);
        if(name.contains("\\")||name.contains("/")||name.contains(" ")) return Operation.INVALID_CHARACTERS;
        else if(name.length()>128) return  Operation.NAME_TOO_LONG;
        if(docMap.putIfAbsent(creator+"/"+name,newDoc)==null) {
            try{
                newDoc.initialize(docPath);
                return Operation.OK;
            }
            catch (IOException e){
                return Operation.FAIL;
            }
        }
        else return Operation.NAME_NOT_AVAILABLE;
    }

    /**
     * Method to invite a new user to edit a document
     * @param document document the user invited should edit
     * @param inviter user who sent the invite request
     * @param invited user who need to be added
     * @return Message.Operation.Document_Not_Found if the document doesn't exist or the inviter doesn't have permission to add
     *         Message.Operation.User_already_invited if the user has already been invited
     *         Message.Operation.Ok if successful
     * @throws IllegalArgumentException if document, inviter and/or invited are null
     */
    /*package*/ Operation invite(String document, String inviter,String invited) throws IllegalArgumentException{
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
     * @return Message.MessageBuffer containing the result of the operation and the section content if successful
     *         a generic fail error is sent if an i/o error occurs
     * @throws IllegalArgumentException if document or user are null or section is not an existing section number
     */
    /*package*/ MessageBuffer edit(String document,String user,int section) throws IllegalArgumentException{
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
     * @param chat ChatOrganizer used for generating multicast address
     * @return Message.Operation.Document_Not_Found if the user has not been invited to edit document or if document doesn't exist
     *         Message.Operation.Editing_Not_Request if the user didn't make an edit request first
     *         Message.Operation.Fail if an I/O error occurs
     *         Message.Operation.OK if successful
     * @throws IllegalArgumentException if document or user are null, section is not an existing section number or file is null
     */
    /*package*/ Operation endEdit(String document,String user,int section, byte[] file,ChatOrganizer chat) throws IllegalArgumentException{
        if(document==null||file==null) throw new IllegalArgumentException();
        Document doc=docMap.get(document);
        if(doc==null) return Operation.DOCUMENT_NOT_FOUND;
        else {
            try{
                return doc.endEdit(section,user,file,chat);
            }
            catch (IOException e){
                return Operation.FAIL;
            }
        }
    }

    /**
     * Method to receive the entire document in a Message.MessageBuffer
     * @param document document wanted to be shown
     * @param user user who sent the request
     * @return Message.MessageBuffer containing the result and the entire document if successful
     *         a generic Fail error is sent if an I/0 error occurs
     * @throws IllegalArgumentException if document and/or user are null
     */
    /*package*/ MessageBuffer show(String document,String user) throws IllegalArgumentException{
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

    /**
     * Method to receive a section in a Message.MessageBuffer
     * @param document document wanted to be shown
     * @param user user who sent the request
     * @param section section wanted to be shown
     * @return Message.MessageBuffer containing the result and the entire document if successful
     *         a generic Fail error is sent if an I/0 error occurs
     * @throws IllegalArgumentException if document and/or user are null and section is not a valid section number
     */
    /*package*/ MessageBuffer show(String document,String user,int section) throws IllegalArgumentException{
        if(document==null) throw new IllegalArgumentException();
        Document doc=docMap.get(document);
        if(doc==null) return MessageBuffer.createMessageBuffer(Operation.DOCUMENT_NOT_FOUND);
        else {
            try{
                return doc.show(user,section);
            }
            catch (IOException e){
                return MessageBuffer.createMessageBuffer(Operation.FAIL);
            }
        }
    }

    /**
     *Method to return all document's info
     * @param document document whose information are needed
     * @return String containing all document's info
     * @throws IllegalArgumentException if document is null
     */
    /*package*/String getInfo(String document) throws IllegalArgumentException{
        if(document==null) throw new IllegalArgumentException();
        Document doc=docMap.get(document);
        if(doc==null) return null;
        else return doc.getInfo();
    }

    /**
     * Method to notify the user has stopped editing without saving any content
     * @param document document stopped to be edited
     * @param username username who stopped editing
     * @param chat ChatOrganizer used for generating multicast address
     */
    /*package*/ void abruptStop(String document, String username, ChatOrganizer chat){
        if(document!=null){
            Document doc=docMap.get(document);
            if(doc!=null){
                doc.abruptStop(username,chat);
            }
        }
    }

    /**
     * Method to get the Room Address of a document, if chatAddress is null it generates a new address with a ChatOrganizer
     * @param document document editing session
     * @param username user who wants to join the chat
     * @param chat ChatOrganizer that keeps track of free and used multicast address
     * @return MessageBuffer containing the result
     * @throws IllegalArgumentException if username or document are null
     */
    /*package*/ MessageBuffer getChatAddress(String document, String username,ChatOrganizer chat) throws IllegalArgumentException{
        if(document==null) throw new IllegalArgumentException();
        Document doc=docMap.get(document);
        if(doc==null) return MessageBuffer.createMessageBuffer(Operation.DOCUMENT_NOT_FOUND);
        else return doc.getRoomAddress(username,chat);
    }

}
