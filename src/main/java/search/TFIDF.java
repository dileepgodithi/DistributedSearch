package search;

import model.DocumentData;

import java.util.*;

public class TFIDF {

    public static double calculateTF(List<String> wordsInDoc, String term){
        int count = 0;
        for(String word : wordsInDoc){
            if(term.equalsIgnoreCase(word))
                count++;
        }
        return (double) count/wordsInDoc.size();
    }

    public static double calculateIDF(Map<String, DocumentData> docInfo, String term){
        int nt = 0;
        for(String document : docInfo.keySet()){
            if(docInfo.get(document).getFrequency(term) > 0)
                nt++;
        }
        return nt == 0 ? 0 : Math.log10((double)docInfo.size()/nt);
    }

    public static List<String> getWordsFromDocument(List<String> lines){
        List<String> words = new ArrayList<>();
        for(String line : lines){
            words.addAll(Arrays.asList(line.split("(\\.)+|(,)+|( )+|(-)+|(\\?)+|(!)+|(;)+|(:)+|(/d)+|(/n)+")));
        }
        return words;
    }

    //calculate all term TFs and store in map
    public static DocumentData createSearchDataInDocument(List<String> wordsInDoc, List<String> searchTerms){
        DocumentData documentData = new DocumentData();
        for(String term : searchTerms){
            double tf = calculateTF(wordsInDoc, term);
            documentData.putFrequency(term, tf);
        }
        return documentData;
    }

    //input docInfo - docs term to TFs map
    public static Map<Double, List<String>> getDocumentsScore(Map<String, DocumentData> docInfo, List<String> terms) {

        TreeMap<Double, List<String>> scores = new TreeMap<>();

        Map<String, Double> termIDFScores = new HashMap<>();

        //calculate IDF for all terms in search query
        for(String term : terms){
            double idf = calculateIDF(docInfo, term);
            termIDFScores.put(term, idf);
        }

        //calculate document score
        for(String document : docInfo.keySet()){
            Double score = 0.0;
            for(String term : terms){
                score += docInfo.get(document).getFrequency(term) * termIDFScores.get(term);
            }

            scores.putIfAbsent(score, new ArrayList<>());
            scores.get(score).add(document);
        }

        return scores.descendingMap();
    }
}
