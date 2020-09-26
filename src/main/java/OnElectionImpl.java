import cluster.management.OnElection;
import cluster.management.ServiceRegistry;
import networking.WebServer;
import org.apache.zookeeper.KeeperException;
import search.SearchWorker;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class OnElectionImpl implements OnElection {
    private ServiceRegistry serviceRegistry;
    private int port;
    private WebServer webServer;
    public OnElectionImpl(ServiceRegistry serviceRegistry, int port) {
        this.serviceRegistry = serviceRegistry;
        this.port = port;
    }

    @Override
    public void onLeader() {
        serviceRegistry.unRegisterFromCluster();
        serviceRegistry.registerForUpdates();
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
