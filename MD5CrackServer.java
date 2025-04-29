//MD5CrackServer.java
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class MD5CrackServer extends UnicastRemoteObject implements ServerInterface {

    private static final String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!\"#$%&'()*+,-./:;<=>?@[\\]^_{|}~";

    public MD5CrackServer() throws RemoteException {
        super();
    }

    @Override
    public Result startSearch(String targetHash, int passwordLength, int startChar, int endChar, int numThreads, AtomicBoolean globalIsFound, int serverId) throws RemoteException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
        long startTime = System.currentTimeMillis();
        System.out.println("Server " + serverId + " Start searching: " + sdf.format(new Date(startTime)));

        Thread[] threads = new Thread[numThreads];
        String[] foundPassword = new String[1];
        int[] foundThreadId = new int[1];

        for (int i = 0; i < numThreads; i++) {
            int logicalThreadId = i + 1;
            int threadStart = startChar + ((endChar - startChar + 1) / numThreads) * i;
            int threadEnd = (i == numThreads - 1) ? endChar : threadStart + ((endChar - startChar + 1) / numThreads) - 1;

            threads[i] = new Thread(() -> {
                try {
                    bruteForce(targetHash, passwordLength, threadStart, threadEnd, globalIsFound, foundPassword, foundThreadId, logicalThreadId, serverId);
                } catch (Exception e) {
                    System.err.println("Error in thread: " + e.getMessage());
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted: " + e.getMessage());
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Server " + serverId + " End searching: " + sdf.format(new Date(endTime)));

        if (globalIsFound.get()) {
            return new Result(foundPassword[0], endTime - startTime, foundThreadId[0], serverId, targetHash);
        }
        return null;
    }

    private void bruteForce(String targetHash, int length, int startChar, int endChar, AtomicBoolean globalIsFound, String[] foundPassword, int[] foundThreadId, int logicalThreadId, int serverId) {
        if (globalIsFound.get()) return;

        for (int i = startChar; i <= endChar; i++) {
            if (globalIsFound.get()) return;
            char ch = CHARSET.charAt(i);
            try {
                reportProgress(serverId, logicalThreadId, "" + ch, getMd5("" + ch)); // Call within try-catch
            } catch (RemoteException e) {
                System.err.println("Error reporting progress: " + e.getMessage());
            }
            bruteForceRecursive("" + ch, length - 1, startChar, endChar, targetHash, globalIsFound, foundPassword, foundThreadId, logicalThreadId, serverId);
        }
    }

    private void bruteForceRecursive(String current, int remaining, int startChar, int endChar, String targetHash, AtomicBoolean globalIsFound, String[] foundPassword, int[] foundThreadId, int logicalThreadId, int serverId) {
        if (globalIsFound.get()) return;

        if (remaining == 0) {
            if (getMd5(current).equals(targetHash)) {
                globalIsFound.set(true);
                foundPassword[0] = current;
                foundThreadId[0] = logicalThreadId;
            }
            return;
        }

        for (int i = startChar; i < CHARSET.length(); i++) {
            if (globalIsFound.get()) return;
            char ch = CHARSET.charAt(i);
            try {
                reportProgress(serverId, logicalThreadId, current + ch, getMd5(current + ch)); // Call within try-catch
            } catch (RemoteException e) {
                System.err.println("Error reporting progress: " + e.getMessage());
            }
            bruteForceRecursive(current + ch, remaining - 1, 0, CHARSET.length(), targetHash, globalIsFound, foundPassword, foundThreadId, logicalThreadId, serverId);
        }
    }

    private static String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Implement the reportProgress method as required by the ServerInterface
    @Override
    public void reportProgress(int serverId, int threadId, String currentString, String currentHash) throws RemoteException {
        System.out.println("Server " + serverId + " - Thread " + threadId + " is trying: " + currentString + " | Hash: " + currentHash);
    }

    public static void main(String[] args) {
        try {
            MD5CrackServer server = new MD5CrackServer();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("MD5CrackServer", server);
            System.out.println("MD5CrackServer is running...");
        } catch (RemoteException e) {
            System.err.println("Server setup error: " + e.getMessage());
        }
    }
}
