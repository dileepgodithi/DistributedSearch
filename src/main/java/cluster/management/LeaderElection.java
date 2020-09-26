package cluster.management;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

public class LeaderElection implements Watcher{
    private final ZooKeeper zooKeeper;
    private static final String ELECTION_NAMESPACE = "/election";
    private String currentZnodeName;
    private OnElection onElection;

    public LeaderElection(ZooKeeper zooKeeper, OnElection onElection){
        this.zooKeeper = zooKeeper;
        this.onElection = onElection;
    }

    public void volunteerForLeaderShip() throws KeeperException, InterruptedException {
        String znodePrefix = ELECTION_NAMESPACE + "/c_";
        String znodePath = zooKeeper.create(znodePrefix, new byte[]{},
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        System.out.println("Znode name " + znodePath);
        this.currentZnodeName = znodePath.replace(ELECTION_NAMESPACE + "/", "");
    }

    public void reElectLeader() throws KeeperException, InterruptedException, UnknownHostException {
        Stat predStat = null;
        String predZnodeName = "";

        //avoid race condition and keep checking
        //until we get a predecessor stat populated
        while(predStat == null){
            List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
            Collections.sort(children);

            String smallestChild = children.get(0);
            if(currentZnodeName.equals(smallestChild)){
                System.out.println("I am the leader");
                //duties of leader node
                onElection.onLeader();
                return;
            }
            else{
                System.out.println("I am not the leader");
                int predIndex = Collections.binarySearch(children, currentZnodeName) - 1;
                predZnodeName = children.get(predIndex);
                predStat = zooKeeper.exists(ELECTION_NAMESPACE + "/" + predZnodeName, this);
            }
        }

        //duties of worker node
        onElection.onWorker();
        System.out.println("Watching znode " + predZnodeName);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()){
            case NodeDeleted:
                try {
                    reElectLeader();
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
        }
    }
}
