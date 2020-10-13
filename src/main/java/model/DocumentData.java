package model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DocumentData implements Serializable {
    private Map<String, Double> termToTermFrequencyMap;

    public DocumentData(){
        termToTermFrequencyMap = new HashMap<>();
    }

    public void putFrequency(String term, Double frequency){
        termToTermFrequencyMap.put(term, frequency);
    }

    public Double getFrequency(String term){
        return termToTermFrequencyMap.get(term);
    }
}
