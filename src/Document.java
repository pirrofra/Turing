import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;

/**
 * This class is used to store basics information about Turing's Document
 * such as the creator, user invited, number of sections, path locations of all sections and user currently editing
 *
 * currentEdited keeps track of free sections - if currentEdited[i] is null, the section i+1 is free to be edited
 * sectionPath is used to store the paths of the various section of the file
 * Constructor is private, to create an instance of Document is used the static method createDocument
 * private method initialize create one empty file for each section and it makes sure the path is complete
 *
 * This Class is Thread-Safe
 *
 * @author Francesco Pirrò - Matr. 544539
 */
public class Document implements Serializable {
    private String documentName;
    private String creator;
    private int numSection;
    private Path[] sectionPath;
    private String[] currentEdited;
    private Vector<String> userInvited;

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
    private void initialize(String path) throws IOException {
        Path dir=Paths.get(path,documentName);
        Files.createDirectories(dir);
        for(int i=1;i<=numSection;i++){
            sectionPath[i-1]=dir.resolve("."+i);
            Files.createFile(sectionPath[i-1]);
        }
    }

    /**
     * Static Method used to generate new Document
     * @param docName Document name
     * @param userCreator user who created the document
     * @param sections number of sections
     * @param Path directory where all documents are stored
     * @return new Document
     * @throws IllegalArgumentException if docName and/or userCreator are null and if sections in 0 or less
     * @throws IOException if an error occurs during newDoc.initialize
     */
    public static Document createDocument(String docName,String userCreator,int sections, String Path) throws IllegalArgumentException,IOException{
        Document newDoc=new Document(docName,userCreator,sections);
        newDoc.initialize(Path);
        newDoc.addUser(userCreator);
        return newDoc;
    }

    /**
     * Private method used to add a new user in the list of invited users
     * @param user user to add
     * @return Operation.User_Already_Invited if the user is already in the list or Operation.OK if successful
     */
    private Operation addUser(String user){
        if(userInvited.contains(user)) return Operation.USER_ALREADY_INVITED;
        else{
            userInvited.add(user);
            return Operation.OK;
        }
    }

    /**
     * Getter for a section path
     * @param section number of section needed
     * @throws IllegalArgumentException if section is 0 or more than numSection
     * @return Path of the section needed
     */
    public Path getPath(int section) throws IllegalArgumentException{
        if(section<1||section>numSection) throw new IllegalArgumentException();
        return sectionPath[section-1];
    }

    /**
     * Method to approve an edit request by a user
     * @param section section wanted to be edited
     * @param username user who requested the edit
     * @throws IllegalArgumentException if section is 0 or more than numSection and/or if username is null
     * @return Operation.Document_Not_Found if the user has not been invited,
     *         Operation.Section_Busy if someone else is already editing this section,
     *         Operation.OK if is successful
     */
    public synchronized Operation edit(int section,String username) throws IllegalArgumentException{
        if(section>numSection||section<1||username==null) throw new IllegalArgumentException();
        else if(!userInvited.contains(username)) return Operation.DOCUMENT_NOT_FOUND;
        else if(currentEdited[section-1]==null){
            currentEdited[section-1]=username;
            return Operation.OK;
        }
        else return Operation.SECTION_BUSY;
    }

    /**
     * Method to approve and end edit request by a user
     * @param section section wanted to be edited
     * @param username user who requested the end edit
     * @return Operation.Document_Not_Found if the user has not been invited
     *         Operation.Editing_Not_Requested if the user has not requested an editing of this section
     *         Operation.OK if successful
     * @throws IllegalArgumentException if section is 0 or more than numSection and/or if username is null
     */
    public synchronized Operation endEdit(int section,String username) throws IllegalArgumentException{
        if(section>numSection||section<1) throw new IllegalArgumentException();
        else if(!userInvited.contains(username)) return Operation.DOCUMENT_NOT_FOUND;
        else if(currentEdited[section-1]==null||currentEdited[section-1].compareTo(username)!=0) return Operation.EDITING_NOT_REQUESTED;
        else{
            currentEdited[section-1]=null;
            return Operation.OK;
        }
    }

    /**
     * Method to approve an invite sent by an inviter to an invited
     * @param inviter user who sent the invite request
     * @param invited user who inviter wants to grant access to the file to
     * @return Operation.Permission_Denied if inviter is not the creator
     *         Operation.User_Already_Added if the user has been already invited
     *         Operation.OK if successful
     * @throws IllegalArgumentException if inviter and/or invited are null
     */
    public synchronized Operation invite(String inviter,String invited) throws IllegalArgumentException{
        if(invited==null||inviter==null) throw new IllegalArgumentException();
        else if(inviter.compareTo(creator)!=0) return Operation.PERMISSION_DENIED;
        else return addUser(invited);
    }

    /**
     * Method to see if an user has been invited to see/edit this file
     * @param user user to check
     * @return true if the user has been invited, false otherwise
     * @throws IllegalArgumentException if user is null
     */
    public synchronized boolean isInvited(String user)throws IllegalArgumentException{
        if(user==null) throw new IllegalArgumentException();
        return userInvited.contains(user);
    }

}
