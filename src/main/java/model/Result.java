package model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Result implements Serializable {
    private Map<String, DocumentData> documentResults;

    public Result(){
        documentResults = new HashMap<>();
    }

    public void addDocumentData(String document, DocumentData documentData){
        documentResults.put(document, documentData);
    }

    public Map<String, DocumentData> getDocumentResults(){
        return Collections.unmodifiableMap(documentResults);
    }
}
