import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    public RequestType type;

    // File / directory info
    public String fileName;
    public boolean isDirectory;

    // Metadata
    public long fileSize;
    public String fileType;

    // Chunk info
    public String chunkId;
    public List<String> chunkList;
    public List<String> chunkServerList;

    // Data
    public byte[] data;
}
