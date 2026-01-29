import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.List;

public class ChunkServer {

    private static String STORAGE;

    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(args[0]);
        STORAGE = "storage_" + port;
        Files.createDirectories(Path.of(STORAGE));

        // REGISTER
        registerWithMaster(port);

        // HEARTBEAT
        startHeartbeat(port);

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("ChunkServer running on " + port);

        while (true) {
            Socket client = serverSocket.accept();
            new Thread(() -> handle(client)).start();
        }
    }

    private static void handle(Socket socket) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            Message msg = (Message) in.readObject();
            Message res = new Message();
            res.type = msg.type;
            res.chunkId = msg.chunkId;

            Path path = Path.of(STORAGE, msg.chunkId);

            if (msg.type == RequestType.WRITE_CHUNK)
                Files.write(path, msg.data);

            if (msg.type == RequestType.READ_CHUNK)
                res.data = Files.readAllBytes(path);

            if (msg.type == RequestType.APPEND)
                Files.write(path, msg.data, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            out.writeObject(res);
            out.flush();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void registerWithMaster(int port) throws Exception {
        Socket s = new Socket("localhost", 8080);
        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
        out.flush();

        Message m = new Message();
        m.type = RequestType.REGISTER_CHUNKSERVER;
        m.chunkServerList = List.of("localhost:" + port);

        out.writeObject(m);
        out.flush();
        s.close();
    }

    private static void startHeartbeat(int port) {
        new Thread(() -> {
            try {
                while (true) {
                    Socket s = new Socket("localhost", 8080);
                    ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                    out.flush();

                    Message hb = new Message();
                    hb.type = RequestType.HEARTBEAT;
                    hb.chunkServerList = List.of("localhost:" + port);

                    out.writeObject(hb);
                    out.flush();
                    s.close();

                    Thread.sleep(3000);
                }
            } catch (Exception ignored) {}
        }).start();
    }
}
