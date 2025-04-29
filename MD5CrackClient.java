import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Date;

public class MD5CrackClient {

    private static final String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!\"#$%&'()*+,-./:;<=>?@[\\]^_{|}~";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss:SSS");

        try {
            Registry registry1 = LocateRegistry.getRegistry("192.168.122.101", 1099);
            ServerInterface server1 = (ServerInterface) registry1.lookup("MD5CrackServer");

            Registry registry2 = LocateRegistry.getRegistry("192.168.122.102", 1099);
            ServerInterface server2 = (ServerInterface) registry2.lookup("MD5CrackServer");

            AtomicBoolean globalIsFound = new AtomicBoolean(false);

            boolean continueSearching = true;

            while (continueSearching) {
                System.out.print("Enter target MD5 hash: ");
                String targetHash = scanner.nextLine().trim();

                if (!targetHash.matches("^[a-fA-F0-9]{32}$")) {
                    System.out.println("Error: Invalid MD5 hash. Must be 32 hexadecimal characters.");
                    continue;
                }

                System.out.print("Enter password length (3-6): ");
                int passwordLength = Integer.parseInt(scanner.nextLine().trim());
                if (passwordLength < 3 || passwordLength > 6) {
                    System.out.println("Error: Password length must be between 3 and 6.");
                    continue;
                }

                System.out.print("Enter number of threads (1-10): ");
                int numThreads = Integer.parseInt(scanner.nextLine().trim());
                if (numThreads < 1 || numThreads > 10) {
                    System.out.println("Error: Number of threads must be between 1 and 10.");
                    continue;
                }

                System.out.print("Enter number of servers (1-2): ");
                int numServers = Integer.parseInt(scanner.nextLine().trim());
                if (numServers < 1 || numServers > 2) {
                    System.out.println("Error: Number of servers must be 1 or 2.");
                    continue;
                }

                int startChar1 = 0;
                int endChar1 = (numServers == 1) ? CHARSET.length() - 1 : CHARSET.length() / 2 - 1;
                int startChar2 = CHARSET.length() / 2;
                int endChar2 = CHARSET.length() - 1;

                long clientStartTime = System.currentTimeMillis();

                AtomicReference<Result> result = new AtomicReference<>(null);

                if (numServers == 1) {
                    result.set(server1.startSearch(targetHash, passwordLength, startChar1, endChar1, numThreads, globalIsFound, 1));
                } else {
                    Thread thread1 = new Thread(() -> {
                        try {
                            result.set(server1.startSearch(targetHash, passwordLength, startChar1, endChar1, numThreads, globalIsFound, 1));
                        } catch (Exception e) {
                            System.err.println("Error with Server 1: " + e.getMessage());
                        }
                    });

                    Thread thread2 = new Thread(() -> {
                        try {
                            result.set(server2.startSearch(targetHash, passwordLength, startChar2, endChar2, numThreads, globalIsFound, 2));
                        } catch (Exception e) {
                            System.err.println("Error with Server 2: " + e.getMessage());
                        }
                    });

                    thread1.start();
                    thread2.start();

                    thread1.join();
                    thread2.join();
                }

                long clientEndTime = System.currentTimeMillis();
                System.out.println("Start Time: " + sdf.format(new Date(clientStartTime)));
                System.out.println("End Time: " + sdf.format(new Date(clientEndTime)));

                if (result.get() != null && result.get().getPassword() != null) {
                    System.out.println("Password found: " + result.get().getPassword());
                    System.out.println("Time taken: " + result.get().getTimeTaken() + " ms");
                    System.out.println("Thread ID: " + result.get().getThreadId());
                    System.out.println("Server ID: " + result.get().getServerId());
                } else {
                    System.out.println("Password not found.");
                }

                System.out.print("Do you want to search another password? (yes/no): ");
                String response = scanner.nextLine().trim().toLowerCase();
                continueSearching = response.equals("yes");
            }

        } catch (Exception e) {
            System.err.println("Client setup error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
