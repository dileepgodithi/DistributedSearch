package search;

import cluster.management.ServiceRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import model.DocumentData;
import model.Result;
import model.SerializationUtils;
import model.Task;
import model.proto.SearchModel;
import networking.OnRequest;
import networking.WebClient;
import org.apache.zookeeper.KeeperException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SearchCoordinator implements OnRequest {
    private static final String DIRECTORY = "resources/books";
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

        System.out.println("search terms: "+terms);
        System.out.println("workers: "+workers);
        if(workers.isEmpty()){
            System.out.println("No workers are available");
            return response.build();
        }

        List<Task> tasks = createTasks(workers.size(), terms);
        System.out.println("tasks: "+tasks);
        List<Result> results = sendTasksToWorkers(workers, tasks);
        System.out.println("results: "+results);

        List<SearchModel.Response.DocumentStats> sortedDocuments = aggregateResults(results, terms);
        response.addAllRelevantDocuments(sortedDocuments);

        return response.build();
    }

    private List<Task> createTasks(int numWorkers, List<String> searchTerms){
        System.out.println("Enter createTasks");
        List<String> documents = readDocumentList();
        System.out.println("Read documents: "+ documents);
        List<List<String>> workerDocuments = spreadDocumentList(numWorkers, documents);
        System.out.println("Worker documents: " + workerDocuments);
        List<Task> tasks = new ArrayList<>();

        for(List<String> singleWorkerDocs : workerDocuments){
            tasks.add(new Task(searchTerms, singleWorkerDocs));
        }

        return tasks;
    }

    private List<Result> sendTasksToWorkers(List<String> workers, List<Task> tasks){
        List<Result> results = new ArrayList<>();
        CompletableFuture<Result>[] futures = new CompletableFuture[tasks.size()];

        for(int i = 0; i < workers.size(); i++){
            futures[i] = webClient.sendTask(workers.get(i),
                    SerializationUtils.serialize(tasks.get(i)));
        }

        for(CompletableFuture<Result> future : futures){
            try {
                results.add(future.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        return results;
    }

    private List<SearchModel.Response.DocumentStats> aggregateResults(List<Result> results, List<String> searchTerms){
        Map<String, DocumentData> allDocumentsResults = new HashMap<>();

        //combine all results from nodes into one result
        for(Result result : results)
            allDocumentsResults.putAll(result.getDocumentResults());

        Map<Double, List<String>> scoredDocuments = TFIDF.getDocumentsScore(allDocumentsResults, searchTerms);

        return generateSortedDocumentStats(scoredDocuments);
    }

    private List<SearchModel.Response.DocumentStats> generateSortedDocumentStats(Map<Double, List<String>> scoredDocuments){
        List<SearchModel.Response.DocumentStats> sortedDocumentStats = new ArrayList<>();

        for(var scoreAndDocs : scoredDocuments.entrySet()){
            double score = scoreAndDocs.getKey();
            for(String doc : scoreAndDocs.getValue()){
                SearchModel.Response.DocumentStats documentStats = SearchModel.Response.DocumentStats
                        .newBuilder()
                        .setScore(score)
                        .setDocumentName(doc)
                        .build();

                sortedDocumentStats.add(documentStats);
            }
        }
        return sortedDocumentStats;
    }

    private List<List<String>> spreadDocumentList(int numWorkers, List<String> documents){
        int docsPerWorker = documents.size() / numWorkers;
        int additionalDocs = documents.size() % numWorkers;
        List<List<String>> spread = new ArrayList<>();
        System.out.println("docs/worker, additional " + docsPerWorker + " " +additionalDocs);
        int startPosition = 0;
        for(int i = 0; i < numWorkers; i++){
            int totalDocs = docsPerWorker + (additionalDocs > 0 ? 1 : 0);
            additionalDocs--;

            List<String> currentSet = documents.subList(startPosition, startPosition + totalDocs);
            spread.add(new ArrayList<>(currentSet));

            startPosition += totalDocs;
        }

        return spread;
    }

    private List<String> readDocumentList(){
        List<String> documents = new ArrayList<>();
        File documentsDirectory = new File(DIRECTORY);

        System.out.println("Enter read document list");
        for(String doc : documentsDirectory.list()){
            documents.add(DIRECTORY + "/" + doc);
        }

        System.out.println(documents);
        return documents;
    }
}
