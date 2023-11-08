import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Master {
    private static final int port = 9999;
    private static final int maxWorkers = 1; // Définissez le nombre maximal de workers

    private static Map<String, WorkerInfo> workerInfoMap = new HashMap<>();
    public static Map<String, List<String>> samplingKeys = new HashMap<>();
    private static int connectedWorkers = 0;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Le maître écoute sur le port " + port);
            ExecutorService threadPool = Executors.newCachedThreadPool();

            while (connectedWorkers < maxWorkers) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouvelle connexion entrante");
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                Object receivedObject = in.readObject();
                if (receivedObject instanceof RegisterRequest) {
                    RegisterRequest registerRequest = (RegisterRequest) receivedObject;
                    handleRegisterRequest(clientSocket, registerRequest, threadPool);
                } else {
                    clientSocket.close();
                }

            }
            serverSocket.close();

            // send samplingkeyrequest
            sendSamplingKeyRequests();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void handleRegisterRequest(Socket clientSocket, RegisterRequest registerRequest,
            ExecutorService threadPool) {
        if (connectedWorkers < maxWorkers) {
            String clientIP = clientSocket.getInetAddress().getHostAddress();
            System.out.println("IP enregistrée : " + clientIP);

            // Crée un WorkerInfo avec le clientIP et la socket associée
            WorkerInfo workerInfo = new WorkerInfo(clientIP, clientSocket);
            workerInfoMap.put(clientIP, workerInfo);

            connectedWorkers++;
            RegisterReply reply = new RegisterReply();

            // Utilise la socket du client pour envoyer la RegisterReply
            sendObject(clientSocket, reply);

        }
    }

    private static void sendSamplingKeyRequests() {
        List<Thread> threads = new ArrayList<>();

        for (WorkerInfo workerInfo : workerInfoMap.values()) {
            Thread thread = new Thread(new SamplingKeyRequestThread(workerInfo, samplingKeys)); // Pass samplingKeys
            threads.add(thread);
            thread.start();
        }

        System.out.println("Started " + threads.size() + " threads for SamplingKeyRequests.");

        // Attend que tous les threads aient terminé
        for (Thread thread : threads) {
            try {
                thread.join();
                System.out.println("Thread joined: " + thread.getId());
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.err.println("Thread join interrupted: " + e.getMessage());
            }
        }

        System.out.println("All sampligs received : ");
        for (Map.Entry<String, List<String>> entry : samplingKeys.entrySet()) {
            String workerIP = entry.getKey();
            List<String> keys = entry.getValue();

            System.out.println("Worker IP: " + workerIP);
            System.out.println("Sampling Keys: " + keys);
        }
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
    private WorkerInfo workerInfo;
    private Map<String, List<String>> samplingKeys; // Add this field

    public SamplingKeyRequestThread(WorkerInfo workerInfo, Map<String, List<String>> samplingKeys) {
        this.workerInfo = workerInfo;
        this.samplingKeys = samplingKeys; // Initialize the field
    }

    @Override
    public void run() {
        try {
            Socket socket = workerInfo.getSocket();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            // Envoyer la requête SamplingKeyRequest
            SamplingKeyRequest request = new SamplingKeyRequest();
            out.writeObject(request);

            // Attendre la réponse SamplingKeyReply

            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Object receivedObject = in.readObject();
            if (receivedObject instanceof SamplingKeyReply) {
                SamplingKeyReply reply = (SamplingKeyReply) receivedObject;
                handleSamplingKeyReply(reply);
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handleSamplingKeyReply(SamplingKeyReply samplingKeyReply) {
        String workerIP = workerInfo.getWorkerIP();
        List<String> keys = samplingKeyReply.getSamplingKeys();

        System.out.println("Keys from worker " + workerIP + ": " + keys);
        samplingKeys.put(workerIP, keys);
    }
}

class WorkerInfo {
    private String workerIP;
    private Socket socket;

    public WorkerInfo(String workerIP, Socket socket) {
        this.workerIP = workerIP;
        this.socket = socket;
    }

    public String getWorkerIP() {
        return workerIP;
    }

    public Socket getSocket() {
        return socket;
    }
}
