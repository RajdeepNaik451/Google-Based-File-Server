import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Message implements Serializable {
    public RequestType type;

    //Metadata
    public String fileName;
    public String chunkId;

    //Response
    public List<String> chunkList;
    public List<String> chunkServerList;

    //Actual Data
    public byte[] data;

    // Response fields
    public List<String> chunks;
    public Map<String, List<String>> chunkLocations;
}
