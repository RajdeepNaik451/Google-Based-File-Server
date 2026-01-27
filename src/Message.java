import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Message implements Serializable {
    public RequestType type;
    public String fileName;
    public byte[] data;

    // Response fields
    public List<String> chunks;
    public Map<String, List<String>> chunkLocations;
}
