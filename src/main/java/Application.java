import model.DocumentData;
import search.TFIDF;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class Application {
    private static final String DIRECTORY = "resources/books";
    public static void main(String[] args) throws FileNotFoundException {
        File documentsDirectory = new File(DIRECTORY);

        System.out.println(documentsDirectory.list());
        System.out.println();
//        List<String> documents = Arrays.asList(documentsDirectory.list())
//                .stream()
//                .map(doc -> DIRECTORY + "/" + doc)
//                .collect(Collectors.toList());
//        System.out.println(documents);

        List<String> docs = new ArrayList<>();
        for(String doc : documentsDirectory.list()){
            docs.add(DIRECTORY + "/" + doc);
        }

        String searchTerm = "The best detective that catches many criminals using his detective methods";

        List<String> terms = Arrays.asList(searchTerm.split(" "));

        //System.out.println(docs);

        findRelevantDocuments(docs, terms);
    }

    private static void findRelevantDocuments(List<String> documents, List<String> terms) throws FileNotFoundException {
        Map<String, DocumentData> docInfo = new HashMap<>();
        //get words from documents
        for(String document : documents){
            FileReader fileReader = new FileReader(document);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            List<String> lines = bufferedReader.lines().collect(Collectors.toList());
            List<String> words = TFIDF.getWordsFromDocument(lines);
            var documentData = TFIDF.createSearchDataInDocument(words, terms);
            docInfo.put(document, documentData);
        }

        Map<Double , List<String>> docsByScore =  TFIDF.getDocumentsScore(docInfo, terms);

        printResults(docsByScore);

    }

    private static void printResults(Map<Double, List<String>> docsByScore){
        for(Map.Entry<Double, List<String>> pair : docsByScore.entrySet()){
            Double score = pair.getKey();
            List<String> docs = pair.getValue();
            for(String doc : docs){
                System.out.println(doc + " -- " + score);
            }
        }
    }
}
