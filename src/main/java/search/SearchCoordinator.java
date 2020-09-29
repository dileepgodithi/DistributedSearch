package search;

import cluster.management.ServiceRegistry;
import networking.OnRequest;
import networking.WebClient;

public class SearchCoordinator implements OnRequest {
    private final String ENDPOINT = "/search";
    private final ServiceRegistry workersServiceRegistry;
    private final WebClient webClient;

    public SearchCoordinator(ServiceRegistry workersServiceRegistry){
        this.workersServiceRegistry = workersServiceRegistry;
        this.webClient = new WebClient();
    }
    @Override
    public byte[] handleRequest(byte[] requestBytes) {
        return new byte[0];
    }

    @Override
    public String getEndPoint() {
        return ENDPOINT;
    }


}
