import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ChunkServer {

    private static final String STORAGE_DIR = "storage/";

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.out.println("Usage: java ChunkServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        new File(STORAGE_DIR).mkdirs();

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("ChunkServer running on port " + port);

        while (true) {
            Socket client = serverSocket.accept();
            new Thread(() -> handle(client)).start();
        }
    }

    private static void handle(Socket socket) {
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            Message msg = (Message) in.readObject();

            if (msg.type == RequestType.WRITE_CHUNK) {
                FileOutputStream fos =
                        new FileOutputStream(STORAGE_DIR + msg.fileName);
                fos.write(msg.data);
                fos.close();

                out.writeObject("WRITE_OK");
            }

            if (msg.type == RequestType.READ_CHUNK) {
                File file = new File(STORAGE_DIR + msg.fileName);
                byte[] data = new byte[(int) file.length()];
                new FileInputStream(file).read(data);

                out.writeObject(data);
            }

            out.flush();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
