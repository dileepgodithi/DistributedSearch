package networking;

public interface OnRequest {
    byte[] handleRequest(byte[] requestBytes);

    String getEndPoint();
}
