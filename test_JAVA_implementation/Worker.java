import java.io.*;
import java.net.*;
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

        Thread t1 = new Thread(new WorkerThread(masterIP, workerPort));
        t1.start();

        try {
            Socket socket = new Socket(masterIP, masterPort);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            RegisterRequest request = new RegisterRequest();
            out.writeObject(request); // Envoie un message d'enregistrement
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class WorkerThread implements Runnable {
    private String masterIP;
    private int workerPort;

    public WorkerThread(String masterIP, int workerPort) {
        this.masterIP = masterIP;
        this.workerPort = workerPort;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(workerPort);
            System.out.println("Worker en attente sur le port " + workerPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                Object receivedObject = in.readObject();

                if (receivedObject instanceof SamplingKeyRequest) {
                    SamplingKeyReply reply = new SamplingKeyReply();
                    sendObject(reply);
                } else if (receivedObject instanceof RegisterReply) {
                    RegisterReply registerReply = (RegisterReply) receivedObject;
                    System.out.println("Message reçu : " + registerReply.getMessage());
                }

                clientSocket.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendObject(Serializable obj) {
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
