package search;

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

    public static double calculateIDF(Map<String, Map<String, Double>> docInfo, String term){
        int nt = 0;
        for(String document : docInfo.keySet()){
            if(docInfo.get(document).get(term) > 0)
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
    public static Map<String, Double> createSearchDataInDocument(List<String> wordsInDoc, List<String> searchTerms){
        Map<String, Double> searchData = new HashMap<>();
        for(String term : searchTerms){
            double tf = calculateTF(wordsInDoc, term);
            searchData.put(term, tf);
        }
        return searchData;
    }

    //input docInfo - docs term to TFs map
    public static Map<Double, List<String>> getDocumentsScore(Map<String, Map<String, Double>> docInfo, List<String> terms) {

        TreeMap<Double, List<String>> scores = new TreeMap<>();

        Map<String, Double> termIDFScores = new HashMap<>();

        for(String term : terms){
            double idf = calculateIDF(docInfo, term);
            termIDFScores.put(term, idf);
        }

        for(String document : docInfo.keySet()){
            Double score = 0.0;
            for(String term : terms){
                score += docInfo.get(document).get(term) * termIDFScores.get(term);
            }

            scores.putIfAbsent(score, new ArrayList<>());
            scores.get(score).add(document);
        }

        return scores.descendingMap();
    }
}
