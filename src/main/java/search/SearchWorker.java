package search;

import model.DocumentData;
import model.Result;
import model.SerializationUtils;
import model.Task;
import networking.OnRequest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SearchWorker implements OnRequest {
    private final String ENDPOINT = "/task";
    @Override
    public byte[] handleRequest(byte[] requestBytes) {
        Task task = (Task) SerializationUtils.deSerialize(requestBytes);
        Result result = createResult(task);
        return SerializationUtils.serialize(result);
    }

    private Result createResult(Task task){
        List<String> documents = task.getDocuments();
        System.out.println(String.format("Received %d documents to process", documents.size()));
        Result result = new Result();

        for(String document : documents){
            List<String> wordsInDoc = parseDocument(document);
            DocumentData documentData = TFIDF.createSearchDataInDocument(wordsInDoc, task.getSearchTerms());
            result.addDocumentData(document, documentData);
        }

        return result;
    }

    private List<String> parseDocument(String document){
        try {
            FileReader fileReader = new FileReader(document);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            List<String> lines = bufferedReader.lines().collect(Collectors.toList());
            List<String> words = TFIDF.getWordsFromDocument(lines);
            return words;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public String getEndPoint() {
        return ENDPOINT;
    }
}
