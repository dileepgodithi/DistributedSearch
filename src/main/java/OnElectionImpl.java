import cluster.management.OnElection;
import cluster.management.ServiceRegistry;
import networking.WebServer;
import org.apache.zookeeper.KeeperException;
import search.SearchCoordinator;
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

        if(webServer != null){
            webServer.stop();
        }
        SearchCoordinator searchCoordinator = new SearchCoordinator(workerServiceRegistry);
        webServer = new WebServer(port, searchCoordinator);
        webServer.startServer();
        this.registerToCluster(searchCoordinator.getEndPoint(), coordinatorServiceRegistry);

    }

    @Override
    public void onWorker() {
        SearchWorker searchWorker = new SearchWorker();
        if(webServer == null){
            webServer = new WebServer(port, searchWorker);
            webServer.startServer();
        }
        this.registerToCluster(searchWorker.getEndPoint(), workerServiceRegistry);
    }

    private void registerToCluster(String endPoint, ServiceRegistry serviceRegistry) {
        try {
            String serverAddress = String.format("http://%s:%d%s", InetAddress.getLocalHost().getCanonicalHostName(),
                    this.port, endPoint);
            serviceRegistry.registerToCluster(serverAddress);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
