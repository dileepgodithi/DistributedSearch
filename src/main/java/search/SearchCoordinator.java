package search;

import cluster.management.ServiceRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import model.Result;
import model.Task;
import model.proto.SearchModel;
import networking.OnRequest;
import networking.WebClient;
import org.apache.zookeeper.KeeperException;

import java.util.ArrayList;
import java.util.List;

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
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
        return SearchModel.Response.getDefaultInstance().toByteArray();
    }

    @Override
    public String getEndPoint() {
        return ENDPOINT;
    }

    private SearchModel.Response createResponse(SearchModel.Request request) throws KeeperException, InterruptedException {
        SearchModel.Response.Builder response = SearchModel.Response.newBuilder();

        System.out.println("Received search query: " + request.getSearchQuery());

        List<String> terms = TFIDF.getWordsFromLine(request.getSearchQuery());
        List<String> workers = workersServiceRegistry.getAllServiceAddresses();

        if(workers.isEmpty()){
            System.out.println("No workers are available");
            return response.build();
        }

        List<Task> tasks = createTasks(workers.size(), terms);
        List<Result> results = sendTasksToWorkers(workers, tasks);

        List<SearchModel.Response.DocumentStats> sortedDocuments = aggregateResults(results);
        response.addAllRelevantDocuments(sortedDocuments);

        return response.build();
    }

    private List<Task> createTasks(int numWorkers, List<String> searchTerms){
        List<String> documents = readDocumentList();
        List<List<String>> workerDocuments = spreadDocumentList(numWorkers, documents);
        List<Task> tasks = new ArrayList<>();
        //to be implemented

        return tasks;
    }

    private List<Result> sendTasksToWorkers(List<String> workers, List<Task> tasks){
        List<Result> results = new ArrayList<>();
        //to be implemented

        return results;

    }

    private List<SearchModel.Response.DocumentStats> aggregateResults(List<Result> results){
        //to be implemented

        return null;
    }

    private List<List<String>> spreadDocumentList(int numWorkers, List<String> documents){

        //to be implemented

        return null;
    }

    private List<String> readDocumentList(){

        //to be implemented

        return null;
    }

}
