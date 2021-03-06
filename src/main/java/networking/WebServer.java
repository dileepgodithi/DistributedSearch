package networking;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebServer {
    private final String STATUS_ENDPOINT = "/status";

    private final int port;
    private HttpServer server;
    private OnRequest onRequest;

    public WebServer(int port, OnRequest onRequest){
        this.port = port;
        this.onRequest = onRequest;
    }

    public void startServer(){
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server started "+ server.getAddress());

        HttpContext taskContext = server.createContext(onRequest.getEndPoint());
        HttpContext statusContext = server.createContext(STATUS_ENDPOINT);

        taskContext.setHandler(this::handleTaskRequest);
        statusContext.setHandler(this::handleStatusRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }

    public void handleStatusRequest(HttpExchange exchange) throws IOException {
        if(!exchange.getRequestMethod().equalsIgnoreCase("get")){
            exchange.close();
            return;
        }

        String response = "I'm alive!";
        sendResponse(response.getBytes(), exchange);
    }

    public void handleTaskRequest(HttpExchange exchange) throws IOException {
        if(!exchange.getRequestMethod().equalsIgnoreCase("post")){
            exchange.close();
            return;
        }

        System.out.println("Handle task request");
        byte[] requestBytes = exchange.getRequestBody().readAllBytes();
        System.out.println(new String(requestBytes));
        byte[] responseBytes = onRequest.handleRequest(requestBytes);
        System.out.println(new String(responseBytes));

        sendResponse(responseBytes, exchange);
    }

    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
        exchange.close();
    }

    public void stop(){
        server.stop(100);
    }
}
