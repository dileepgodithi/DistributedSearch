package cluster.management;

public interface OnElection {
    void onLeader();
    void onWorker();
}
