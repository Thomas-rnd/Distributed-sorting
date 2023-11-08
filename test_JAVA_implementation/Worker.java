import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.List;

public class Worker {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Utilisation : java Worker <adresse IP du maître>");
            return;
        }

        String masterIP = args[0];
        int masterPort = 9999;
        int workerPort = 9998;

        try {
            Socket socket = new Socket(masterIP, masterPort);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            RegisterRequest request = new RegisterRequest();
            out.writeObject(request); // Envoie un message d'enregistrement

            bool isDone = false;
            while (!isDone) {
                Object receivedObject = in.readObject();

                if (receivedObject instanceof RegisterReply) {
                    RegisterReply registerReply = (RegisterReply) receivedObject;
                    System.out.println("Message reçu : " + registerReply.getMessage());

                } else if (receivedObject instanceof SamplingKeyRequest) {
                    List<String> myKeys = Arrays.asList("hello", "world", "red");
                    SamplingKeyReply reply = new SamplingKeyReply(myKeys);

                    sendObject(reply);
                    isDone = true;
                }

            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void sendObject(Serializable obj) {
        try {
            Socket socket = new Socket(masterIP, masterPort);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(obj);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
