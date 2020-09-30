package search;

import cluster.management.ServiceRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import model.proto.SearchModel;
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
        try {
            SearchModel.Request request = SearchModel.Request.parseFrom(requestBytes);
            SearchModel.Response response = createResponse(request);

            return response.toByteArray();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return SearchModel.Response.getDefaultInstance().toByteArray();
    }

    @Override
    public String getEndPoint() {
        return ENDPOINT;
    }

    private SearchModel.Response createResponse(SearchModel.Request request){
        SearchModel.Response.Builder response = SearchModel.Response.newBuilder();

        //Implement building response


        return response.build();
    }


}
