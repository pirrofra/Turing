import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class DocTable implements Serializable {

    private ConcurrentHashMap<String,Document> docMap;

    public DocTable(){
        docMap=new ConcurrentHashMap<>();
    }

    public DocTable(int initialCapacity, float loadFactor, int concurrencyLevel){
        docMap=new ConcurrentHashMap<>(initialCapacity,loadFactor,concurrencyLevel);
    }


}
