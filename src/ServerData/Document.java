package ServerData;

import ChatRoom.ChatOrganizer;
import Message.MessageBuffer;
import Message.Operation;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.Vector;

/**
 * This class is used to store basics information about Turing's ServerData.Document
 * such as the creator, user invited, number of sections, path locations of all sections and user currently editing
 *
 * currentEdited keeps track of free sections - if currentEdited[i] is null, the section i+1 is free to be edited
 * sectionPath is used to store the paths of the various section of the file
 * Constructor is private, to create an instance of ServerData.Document is used the static method createDocument
 * private method initialize create one empty file for each section and it makes sure the path is complete
 *
 * This class uses NIO to manages files.
 *
 * This Class is Thread-Safe
 *
 * @author Francesco Pirrò - Matr. 544539
 */
/*package*/ class Document implements Serializable {


    private static final int maxSize=Integer.MAX_VALUE; //TODO:capire valore adatto
    private String documentName;
    private String creator;
    private int numSection;
    private Path[] sectionPath;
    private String[] currentEdited;
    private Vector<String> userInvited;
    private String chatAddress;

    /**
     * Private class constructor
     * @param docName document name
     * @param userCreator user who created the document
     * @param sections number of sections of the document
     * @throws IllegalArgumentException if docName and/or userCreator are null and if sections in 0 or less
     */
    private Document(String docName,String userCreator, int sections ) throws IllegalArgumentException{
        if(docName==null||docName.compareTo("")==0||userCreator==null||sections<1) throw new IllegalArgumentException();
        documentName=docName;
        creator=userCreator;
        numSection=sections;
        sectionPath=new Path[sections];
        currentEdited=new String[sections];
        userInvited=new Vector<>();
    }

    /**
     * Private method used to create an empty file for each section
     * @param path directory where all documents are stored
     * @throws IOException if an error occurs during CreateFile
     */
    /*package*/ void initialize(String path) throws IOException {
        Path dir=Paths.get(path,creator,documentName);
        Files.createDirectories(dir);
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
        for(int i=1;i<=numSection;i++){
            sectionPath[i-1]=dir.resolve("."+i);
            try{
                Files.createFile(sectionPath[i-1],attr);
            }
            catch (FileAlreadyExistsException e){
                String empty="";
                Files.write(sectionPath[i-1],empty.getBytes(),StandardOpenOption.TRUNCATE_EXISTING);
            }

        }
    }

    /**
     * Static Method used to generate new ServerData.Document
     * @param docName ServerData.Document name
     * @param userCreator user who created the document
     * @param sections number of sections
     * @return new ServerData.Document
     * @throws IllegalArgumentException if docName and/or userCreator are null and if sections in 0 or less
     */
    /*package*/ static Document createDocument(String docName,String userCreator,int sections) throws IllegalArgumentException {
        Document newDoc=new Document(docName,userCreator,sections);
        newDoc.addUser(userCreator);
        return newDoc;
    }

    /**
     * Private method used to add a new user in the list of invited users
     * @param user user to add
     * @return Message.Operation.User_Already_Invited if the user is already in the list or Message.Operation.OK if successful
     */
    private Operation addUser(String user){
        if(userInvited.contains(user)) return Operation.USER_ALREADY_INVITED;
        else{
            userInvited.add(user);
            return Operation.OK;
        }
    }


    /**
     * Method to approve an edit request by a user
     * @param section section wanted to be edited
     * @param username user who requested the edit
     * @throws IllegalArgumentException if section is 0 or more than numSection and/or if username is null
     * @throws IOException if an error occurs during I/O operations
     * @return Message.MessageBuffer containing result and file buffer
     */
    /*package*/ synchronized MessageBuffer edit(int section,String username) throws IllegalArgumentException,IOException{
        if(section>numSection||section<1||username==null) throw new IllegalArgumentException();
        else if(!userInvited.contains(username)) return MessageBuffer.createMessageBuffer(Operation.DOCUMENT_NOT_FOUND);
        else if(currentEdited[section-1]==null){
            currentEdited[section-1]=username;
            ByteBuffer file=openFile(sectionPath[section-1]);
            return MessageBuffer.createMessageBuffer(Operation.OK,file.array());
        }
        else return MessageBuffer.createMessageBuffer(Operation.SECTION_BUSY);
    }

    /**
     * Method to receive in a messageBuffer the entire document
     * @param username user who sent the request
     * @return messageBuffer containing the entire document
     * @throws IllegalArgumentException if username is null
     * @throws IOException if an error occurs during I/O operations
     */
    /*package*/ synchronized MessageBuffer show(String username) throws IllegalArgumentException,IOException{
        if(username==null) throw new IllegalArgumentException();
        else if(!userInvited.contains(username)) return MessageBuffer.createMessageBuffer(Operation.DOCUMENT_NOT_FOUND);
        int dimension=0;
        for(Path path:sectionPath){
            dimension+=Files.size(path);
        }
        StringBuilder info=new StringBuilder();
        info.append("Section currently edited: ");
        boolean allfree=true;
        for(int i=0;i<numSection;i++){
            if(currentEdited[i]!=null) {
                allfree=false;
                info.append(i);
                info.append("- ");
            }
        }
        if(allfree) info.append("none");
        ByteBuffer completeDocument=ByteBuffer.allocate(dimension);
        for(Path path:sectionPath){
            ByteBuffer section=openFile(path);
            completeDocument.put(section);
        }
        completeDocument.flip();
        return MessageBuffer.createMessageBuffer(Operation.OK,completeDocument.array(),info.toString().getBytes());
    }

    /**
     * Method to show a single section of a document
     * @param username user who requested the section
     * @param section section to be shown
     * @return messageBuffer containing the section if successful
     * @throws IllegalArgumentException if section is not a valid section number or username is null
     * @throws IOException if an error occurs while reading the file
     */
    /*package*/ synchronized MessageBuffer show(String username,int section) throws IllegalArgumentException,IOException{
        if(section>numSection||section<1||username==null) throw new IllegalArgumentException();
        else if(!userInvited.contains(username)) return MessageBuffer.createMessageBuffer(Operation.DOCUMENT_NOT_FOUND);
        ByteBuffer file=openFile(sectionPath[section-1]);
        String info;
        if(currentEdited[section-1]==null) info="Nobody is editing this section";
        else info=currentEdited[section-1]+" is editing this section";
        return MessageBuffer.createMessageBuffer(Operation.OK,file.array(),info.getBytes());
    }

    /**
     * Method to approve and end edit request by a user
     * the section is updated with new file
     * @param section section wanted to be edited
     * @param username user who requested the end edit
     * @param file  content of the file to be saved
     * @return Message.Operation.Document_Not_Found if the user has not been invited
     *         Message.Operation.Editing_Not_Requested if the user has not requested an editing of this section
     *         Message.Operation.OK if successful
     * @throws IllegalArgumentException if section is 0 or more than numSection and/or if username is null
     * @throws IOException if an error occurs during I/O operations
     */
    /*package*/ synchronized Operation endEdit(int section, String username, byte[] file, ChatOrganizer chat) throws IllegalArgumentException,IOException{
        if(section>numSection||section<1) throw new IllegalArgumentException();
        else if(!userInvited.contains(username)) return Operation.DOCUMENT_NOT_FOUND;
        else if(currentEdited[section-1]==null||currentEdited[section-1].compareTo(username)!=0) return Operation.EDITING_NOT_REQUESTED;
        else if(file.length>maxSize/numSection) return Operation.FILE_TOO_BIG;
        else{
            saveFile(sectionPath[section-1],file);
            currentEdited[section-1]=null;
            closeRoomIfEmpty(chat);
            return Operation.OK;
        }
    }

    /**
     * Method to approve an invite sent by an inviter to an invited
     * @param inviter user who sent the invite request
     * @param invited user who inviter wants to grant access to the file to
     * @return Message.Operation.Permission_Denied if inviter is not the creator
     *         Message.Operation.User_Already_Added if the user has been already invited
     *         Message.Operation.OK if successful
     * @throws IllegalArgumentException if inviter and/or invited are null
     */
    /*package*/ synchronized Operation invite(String inviter,String invited) throws IllegalArgumentException{
        if(invited==null||inviter==null) throw new IllegalArgumentException();
        else if(inviter.compareTo(creator)!=0) return Operation.PERMISSION_DENIED;
        else return addUser(invited);
    }

    /**
     * Method to return all document's info
     * @return String containing all document's info
     */
    /*package*/ synchronized String getInfo(){
        StringBuilder builder=new StringBuilder();
        builder.append(creator);
        builder.append("/");
        builder.append(documentName);
        builder.append("\n");
        builder.append("    Creator: ");
        builder.append(creator);
        builder.append("\n");
        builder.append("    Invited User: ");
        for(String user:userInvited){
            if(user.compareTo(creator)!=0)builder.append(user);
            builder.append(" ");
        }
        builder.append("\n");
        builder.append("    Number of Section: ");
        builder.append(numSection);
        builder.append("\n");
        return builder.toString();
    }

    /**
     * Method to notify the user has stopped editing without saving any content
     * @param user user who stopped editing
     */
    /*package*/ synchronized void abruptStop(String user,ChatOrganizer chat){
        if(user!=null){
            for(int i=0;i<numSection;i++){
                if(currentEdited[i]!=null && currentEdited[i].compareTo(user)==0) currentEdited[i]=null;
            }
        }
        closeRoomIfEmpty(chat);
    }

    /*package*/ synchronized MessageBuffer getRoomAddress(String user,ChatOrganizer chat)throws IllegalArgumentException{
        if(user==null) throw new IllegalArgumentException();
        boolean permission=false;
        for(int i=0;i<numSection;i++){
            if(currentEdited[i]!=null && currentEdited[i].compareTo(user)==0) permission=true;
        }
        if(permission){
            if(chatAddress==null) chatAddress=chat.getNewAddress();
            ByteBuffer buffer=ByteBuffer.allocate(4);
            buffer.putInt(chat.getPort());
            return MessageBuffer.createMessageBuffer(Operation.OK,chatAddress.getBytes(),buffer.array(),user.getBytes());
        }
        else return MessageBuffer.createMessageBuffer(Operation.EDITING_NOT_REQUESTED);
    }
    /**
     * Static method to save an array byte in a file
     * @param path path of the file where to save
     * @param file content to save
     * @throws IOException if an error occurs during I/O operation on the file
     */
    private static void saveFile(Path path,byte[] file) throws IOException{
        Files.write(path,file,StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Static method to open a file and copy its content in a buffer
     * @param path file to open
     * @return buffer containing the file
     * @throws IOException if an error occurs during I/O operation on the file
     */
    private static ByteBuffer openFile(Path path) throws IOException{
        FileChannel fileChannel=FileChannel.open(path,StandardOpenOption.READ);
        int size=(int)fileChannel.size();
        ByteBuffer buffer=ByteBuffer.allocate(size);
        while (size>0){
            int byteRead=fileChannel.read(buffer);
            if(byteRead<0) throw  new IOException();
            size-=byteRead;
        }
        buffer.flip();
        return buffer;
    }

    private void closeRoomIfEmpty(ChatOrganizer chat){
        boolean empty=true;
        for(int i=0;i<numSection;i++){
            if(currentEdited[i]!=null)
                empty=false;
        }
        if(empty) {
            chat.closeRoom(chatAddress);
            chatAddress=null;
        }
    }

}
