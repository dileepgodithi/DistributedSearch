package search;

import networking.OnRequest;

public class SearchCoordinator implements OnRequest {
    private final String ENDPOINT = "/search";
    @Override
    public byte[] handleRequest(byte[] requestBytes) {
        return new byte[0];
    }

    @Override
    public String getEndPoint() {
        return ENDPOINT;
    }
}
