public enum RequestType {

    // Control
    REGISTER_CHUNKSERVER,
    HEARTBEAT,

    // Metadata
    CREATE_FILE,
    GET_CHUNKS,
    CREATE_DIRECTORY,

    // Data
    WRITE_CHUNK,
    READ_CHUNK,
    APPEND
}
