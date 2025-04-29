//ServerInterface.java
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicBoolean;

public interface ServerInterface extends Remote {
    Result startSearch(String targetHash, int passwordLength, int startChar, int endChar, int numThreads, AtomicBoolean globalIsFound, int serverId) throws RemoteException;
    void reportProgress(int serverId, int threadId, String currentString, String currentHash) throws RemoteException;
}
