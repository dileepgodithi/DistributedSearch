package cluster.management;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceRegistry implements Watcher {
    public static final String WORKERS_REGISTRY_ZNODE = "/workers_service_registry";
    public static final String COORDINATORS_REGISTRY_ZNODE = "/coordinators_service_registry";
    private final ZooKeeper zooKeeper;
    private String currentZnode = null;
    private List<String> allServiceAddresses = null;
    private final String serviceRegistryZnode;

    public ServiceRegistry(ZooKeeper zooKeeper, String serviceRegistryZnode){
        this.zooKeeper = zooKeeper;
        this.serviceRegistryZnode    = serviceRegistryZnode;
        createServiceRegistryZnode();
    }

    public void createServiceRegistryZnode(){
        try {
            if(zooKeeper.exists(serviceRegistryZnode, false) == null){
                zooKeeper.create(serviceRegistryZnode, new byte[]{},
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void registerForUpdates(){
        try {
            this.updateAddresses();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void registerToCluster(String serverAddress) throws KeeperException, InterruptedException, UnknownHostException {
        if(this.currentZnode != null){
            System.out.println("Already registered to service registy");
            return;
        }
        this.currentZnode  = zooKeeper.create(serviceRegistryZnode + "/n_", serverAddress.getBytes(),
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

    public List<String> getAllServiceAddresses() throws KeeperException, InterruptedException {
        if(allServiceAddresses == null)
            updateAddresses();

        return allServiceAddresses;
    }
    private synchronized void updateAddresses() throws KeeperException, InterruptedException {
        List<String> workerZnodes = zooKeeper.getChildren(serviceRegistryZnode, this);
        List<String> addresses = new ArrayList<>();
        for(String node : workerZnodes){
            String nodePath = serviceRegistryZnode + "/" + node;
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
