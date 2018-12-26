import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class DocTable implements Serializable {

    private ConcurrentHashMap<String,Document> docMap;
    private String docPath;

    public DocTable(String path){
        docMap=new ConcurrentHashMap<>();
        docPath=path;
    }

    public DocTable(String path,int initialCapacity, float loadFactor, int concurrencyLevel){
        docMap=new ConcurrentHashMap<>(initialCapacity,loadFactor,concurrencyLevel);
        docPath=path;
    }

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

    public Operation invite(String document, String inviter,String invited) throws IllegalArgumentException{
        if(document==null) throw new IllegalArgumentException();
        Document doc=docMap.get(document);
        if(doc==null) return Operation.DOCUMENT_NOT_FOUND;
        else return doc.invite(inviter,invited);
    }

    public MessageBuffer Edit(String document,String user,int section) throws IllegalArgumentException,IOException{
        if(document==null) throw new IllegalArgumentException();
        Document doc=docMap.get(document);
        if(doc==null) return MessageBuffer.createMessageBuffer(Operation.DOCUMENT_NOT_FOUND);
        else return doc.edit(section,user);
    }

    public Operation endEdit(String document,String user,int section, byte[] file) throws IllegalArgumentException,IOException{
        if(document==null) throw new IllegalArgumentException();
        Document doc=docMap.get(document);
        if(doc==null) return Operation.DOCUMENT_NOT_FOUND;
        else return doc.endEdit(section,user,file);
    }


    /* TODO: Edit che restituisce il contenuto del file in un MappedByteBuffer
    *  TODO: Show che restituisce la concatenazione di tutti i file in un buffer
    * */


}
