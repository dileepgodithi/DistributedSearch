package cluster.management;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceRegistry implements Watcher {
    private static final String REGISTRY_ZNODE = "/service_registry";
    private final ZooKeeper zooKeeper;
    private String currentZnode = null;
    private List<String> allServiceAddresses = null;
    private final int port;

    public ServiceRegistry(ZooKeeper zooKeeper, int port){
        this.zooKeeper = zooKeeper;
        this.port = port;
        createServiceRegistryZnode();
    }

    public void createServiceRegistryZnode(){
        try {
            if(zooKeeper.exists(REGISTRY_ZNODE, false) == null){
                zooKeeper.create(REGISTRY_ZNODE, new byte[]{},
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void registerToCluster() throws KeeperException, InterruptedException, UnknownHostException {
        String serverAddress = String.format("http://%s:%d", InetAddress.getLocalHost().getCanonicalHostName(), port);
        if(this.currentZnode != null){
            System.out.println("Already registered to service registy");
            return;
        }
        this.currentZnode  = zooKeeper.create(REGISTRY_ZNODE + "/n_", serverAddress.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Registered to service registry");
    }

    public void unRegisterFromCluster(){
        try {
            if(currentZnode != null && zooKeeper.exists(currentZnode, false) != null){
                zooKeeper.delete(currentZnode, -1);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void updateAddresses() throws KeeperException, InterruptedException {
        List<String> workerZnodes = zooKeeper.getChildren(REGISTRY_ZNODE, this);
        List<String> addresses = new ArrayList<>();
        for(String node : workerZnodes){
            String nodePath = REGISTRY_ZNODE + "/" + node;
            Stat nodeStat = zooKeeper.exists(nodePath,false);
            if(nodeStat == null)
                continue;

            byte[] addressBytes = zooKeeper.getData(nodePath, false, nodeStat);
            addresses.add(new String(addressBytes));
        }

        this.allServiceAddresses = Collections.unmodifiableList(addresses);
        System.out.println("Cluster addresses are: " + this.allServiceAddresses);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            updateAddresses();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
