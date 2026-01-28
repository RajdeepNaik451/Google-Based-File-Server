import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ChunkServer {

    private static String STORAGE_DIR;

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.out.println("Usage: java ChunkServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        STORAGE_DIR = "storage_" + port;
        Files.createDirectories(Path.of(STORAGE_DIR));

        //REGISTER WITH MASTER (FIXED)
        try (Socket master = new Socket("localhost", 8080)) {

            ObjectOutputStream out = new ObjectOutputStream(master.getOutputStream());
            out.flush(); // IMPORTANT
            ObjectInputStream in = new ObjectInputStream(master.getInputStream());

            Message register = new Message();
            register.type = RequestType.REGISTER_CHUNKSERVER;
            register.chunkServerList = List.of("localhost:" + port);

            out.writeObject(register);
            out.flush();

            // READ MASTER RESPONSE (PREVENTS SOCKET ABORT)
            Message response = (Message) in.readObject();
            System.out.println("Registered with Master: " + response.type);
        }

        // START CHUNK SERVER
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("ChunkServer running on port " + port);

        while (true) {
            Socket client = serverSocket.accept();
            new Thread(() -> handle(client)).start();
        }
    }

    private static void handle(Socket socket) {
        try {
            // Output FIRST (CORRECT)
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            Message msg = (Message) in.readObject();

            Message response = new Message();
            response.type = msg.type;
            response.chunkId = msg.chunkId;

            if (msg.type == RequestType.WRITE_CHUNK) {

                Path chunkPath = Path.of(STORAGE_DIR, msg.chunkId);
                Files.write(chunkPath, msg.data);

            } else if (msg.type == RequestType.READ_CHUNK) {

                Path chunkPath = Path.of(STORAGE_DIR, msg.chunkId);
                response.data = Files.readAllBytes(chunkPath);
            }

            out.writeObject(response);
            out.flush();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
