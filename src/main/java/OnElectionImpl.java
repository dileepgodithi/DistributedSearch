import cluster.management.OnElection;
import cluster.management.ServiceRegistry;
import org.apache.zookeeper.KeeperException;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class OnElectionImpl implements OnElection {
    private ServiceRegistry serviceRegistry;
    private int port;
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
        try {
            String serverAddress = String.format("http://%s:%d", InetAddress.getLocalHost().getCanonicalHostName(), this.port);
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
