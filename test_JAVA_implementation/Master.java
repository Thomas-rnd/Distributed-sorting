import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Master {
    private static final int port = 9999;
    private static final int maxWorkers = 1; // Définissez le nombre maximal de workers

    private static List<String> registeredIPs = new ArrayList<>();
    private static Map<String, List<String>> samplingKeys = new HashMap<>();
    private static int connectedWorkers = 0;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Le maître écoute sur le port " + port);
            ExecutorService threadPool = Executors.newCachedThreadPool();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouvelle connexion entrante");

                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                Object receivedObject = in.readObject();

                if (receivedObject instanceof RegisterRequest) {
                    RegisterRequest registerRequest = (RegisterRequest) receivedObject;
                    handleRegisterRequest(clientSocket, registerRequest, threadPool);
                } else if (receivedObject instanceof SamplingKeyReply) {
                    SamplingKeyReply samplingKeyReply = (SamplingKeyReply) receivedObject;
                    handleSamplingKeyReply(samplingKeyReply);
                }

                clientSocket.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void handleRegisterRequest(Socket clientSocket, RegisterRequest registerRequest,
            ExecutorService threadPool) {
        if (connectedWorkers < maxWorkers) {
            String clientIP = clientSocket.getInetAddress().getHostAddress();
            System.out.println("IP enregistrée : " + clientIP);
            registeredIPs.add(clientIP);
            connectedWorkers++;
            RegisterReply reply = new RegisterReply();
            Socket workerSocket = new Socket(workerIP, workerPort);
            sendObject(workerSocket, reply);
            workerSocket.close();
            if (connectedWorkers == maxWorkers) {
                sendSamplingKeyRequests();
            }
        }
    }

    private static void sendSamplingKeyRequests() {
        for (String workerIP : registeredIPs) {
            Thread thread = new Thread(new SamplingKeyRequestThread(workerIP));
            thread.start();
        }
    }

    private static void handleSamplingKeyReply(SamplingKeyReply samplingKeyReply) {
        String workerIP = samplingKeyReply.getWorkerIP();
        List<String> keys = samplingKeyReply.getSamplingKeys();

        System.out.println("Keys from worker " + workerIP + ": " + keys);
        samplingKeys.put(workerIP, keys);
    }

    private static void sendObject(Socket socket, Serializable obj) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class SamplingKeyRequestThread implements Runnable {
    private String workerIP;
    private int workerPort = 9998;

    public SamplingKeyRequestThread(String workerIP) {
        this.workerIP = workerIP;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(workerIP, workerPort);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            SamplingKeyRequest request = new SamplingKeyRequest();
            out.writeObject(request);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
