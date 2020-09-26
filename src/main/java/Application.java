import cluster.management.LeaderElection;
import cluster.management.OnElection;
import cluster.management.ServiceRegistry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class Application implements Watcher {
    private ZooKeeper zooKeeper;
    private String ZOOKEEPER_ADDRESS = "localhost:2181";
    private int SESSION_TIMEOUT = 5000;

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        int currentPort = args.length == 1 ? Integer.parseInt(args[0]) : 8080;
        Application application = new Application();
        ZooKeeper zooKeeper = application.connectToZookeeper();

        ServiceRegistry workersServiceRegistry = new ServiceRegistry(zooKeeper, ServiceRegistry.WORKERS_REGISTRY_ZNODE);
        ServiceRegistry coordinatorsServiceRegistry = new ServiceRegistry(zooKeeper, ServiceRegistry.COORDINATORS_REGISTRY_ZNODE);
        OnElection onElection = new OnElectionImpl(workersServiceRegistry, coordinatorsServiceRegistry, currentPort);

        LeaderElection leaderElection = new LeaderElection(zooKeeper, onElection);
        leaderElection.volunteerForLeaderShip();
        leaderElection.reElectLeader();

        application.run();
        application.close();
        System.out.println("Disconnected from zookeeper, exiting application");
    }

    public ZooKeeper connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT,this);
        return zooKeeper;
    }

    public void run() throws InterruptedException {
        synchronized (zooKeeper){
            zooKeeper.wait();
        }
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
    }
    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()){
            case None:
                if(watchedEvent.getState() == Event.KeeperState.SyncConnected){
                    System.out.println("Successfully connected to Zookeeper");
                }
                else {
                    synchronized (zooKeeper){
                        System.out.println("Disconnected from Zookeeper event");
                        zooKeeper.notifyAll();
                    }
                }
        }
    }
}
