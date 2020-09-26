import cluster.management.OnElection;
import cluster.management.ServiceRegistry;
import networking.WebServer;
import org.apache.zookeeper.KeeperException;
import search.SearchWorker;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class OnElectionImpl implements OnElection {
    private ServiceRegistry workerServiceRegistry;
    private ServiceRegistry coordinatorServiceRegistry;
    private int port;
    private WebServer webServer;
    public OnElectionImpl(ServiceRegistry workerServiceRegistry, ServiceRegistry coordinatorServiceRegistry, int port) {
        this.workerServiceRegistry = workerServiceRegistry;
        this.coordinatorServiceRegistry = coordinatorServiceRegistry;
        this.port = port;
    }

    @Override
    public void onLeader() {
        workerServiceRegistry.unRegisterFromCluster();
        workerServiceRegistry.registerForUpdates();
    }

    @Override
    public void onWorker() {
        SearchWorker searchWorker = new SearchWorker();
        if(webServer == null){
            webServer = new WebServer(port, searchWorker);
        }
        try {
            String serverAddress = String.format("http://%s:%d/%s", InetAddress.getLocalHost().getCanonicalHostName(), this.port,
                    searchWorker.getEndPoint());
            workerServiceRegistry.registerToCluster(serverAddress);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
