package search;

import networking.OnRequest;

public class SearchWorker implements OnRequest {
    @Override
    public byte[] handleRequest(byte[] requestBytes) {
        return new byte[0];
    }

    @Override
    public String getEndPoint() {
        return null;
    }
}
