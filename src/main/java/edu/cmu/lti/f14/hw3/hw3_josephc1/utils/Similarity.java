package edu.cmu.lti.f14.hw3.hw3_josephc1.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Similarity {

  public static Map<String, Double> unitVector(Map<String, Number> A){
    double total = 0;
    for (Number count: A.values()) {
      total += count.doubleValue();
    }
    HashMap<String, Double> out = new HashMap<String, Double>();
    for (Entry<String, Number> entry : A.entrySet()) {
      String text = entry.getKey();
      double count = entry.getValue().doubleValue();
      out.put(text, count / total);
    }
    return out;
  }
  
  public static Double idf(String term, Integer queryId) {
    HashMap<String, Object> IDF = MemoryStore.getSingletonInstance(Utils.fromQueryIdToKey(queryId)).data;
    Double N = ((Integer) IDF.get(Utils.NDOC_KEY)).doubleValue();
    Double n = ((Number)IDF.get(term)).doubleValue();
    //return Math.log( N - n + 0.5 / (n + 0.5) ); // IDF from Okapi BM25, can be negative and problematic
    return Math.log(N/n);
  }

  public static Double tf(Map<String, Number> A, String term) {
    Integer n = (Integer) A.get(term);
    
    Map.Entry<String, Number> maxEntry = null;
    for (Map.Entry<String, Number> entry : A.entrySet()) {
        if (maxEntry == null || ((Integer) entry.getValue()).compareTo((Integer) maxEntry.getValue()) > 0) {
            maxEntry = entry;
        }
    } 
    
    Double N = maxEntry.getValue().doubleValue();
    
    return 0.5 + ((0.5 * n) / N);
  }
  
  public static Map<String, Number> tfidf(Map<String, Number> A, Integer queryId) {
    Map<String, Number> out = new HashMap<String, Number>();
    
    for (Map.Entry<String, Number> entry : A.entrySet()) {
      
      Double _tf = tf(A, entry.getKey());
      Double _idf = idf(entry.getKey(), queryId);
      out.put(entry.getKey(), _tf * _idf);
    }
    
    return out;
    
  }

  
  public static double norm(Map<String, Double> A){
    double out = 0.0;      
    for (double prob: A.values()) {
      out += prob*prob;
    }
    return Math.sqrt(out);
  }
  
  public static double dot(Map<String, Double> A, Map<String, Double> B) {
    double out = 0.0;
    for (String word: A.keySet()) {
      if (B.containsKey(word)) {
        out += A.get(word) * B.get(word);
      }
    }
    return out;
  }
  
  /**
   * 
   * @return cosine_similarity
   */
  public static double computeCosineSimilarity(Map<String, Number> queryVector, Map<String, Number> docVector) {
    Map<String, Double> A = unitVector(queryVector);
    Map<String, Double> B = unitVector(docVector);
    return dot(A, B) / (norm(A) * norm(B));
  }

  
  public static double computeOkapiBM25Score(Map<String, Number> queryVector, Map<String, Number> docVector, Integer queryId, double k, double b) {
    
    Map<String, Double> A = unitVector(queryVector);
    Map<String, Double> B = unitVector(docVector);
    Map<String, Object> IDF = MemoryStore.getSingletonInstance(Utils.fromQueryIdToKey(queryId)).data;
    double D = ((Number)IDF.get(Utils.TOTAL_LENGTH_KEY)).doubleValue() / ((Number)IDF.get(Utils.NDOC_KEY)).doubleValue();
    double d = 0.0;
    for (Number value: docVector.values()) {
      d += value.doubleValue();
    }
    double out = 0.0;
    for (Entry<String, Double> entry : A.entrySet()) {
      String term = entry.getKey();
      if(! B.containsKey(term)) {
        continue;
      }
      double _tf = B.get(term);
      double _idf = idf(term, queryId);
      
      out += _idf * (_tf * (k + 1.0)) / (_tf + (k * (1 - b + (b*d/D))));
    
    }
    
    return out;
  }
  
}
