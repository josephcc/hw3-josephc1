package edu.cmu.lti.f14.hw3.hw3_josephc1.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A class with a private constructor and only static public functions that ranks the documents using different strategies.
 * @author josephcc
 *
 */
public class Similarity {

  private Similarity() {
    
  }
  
  /**
   * For a given sparse matrix, normalize it to unit length.
   * 
   * @param queryVector
   * @return
   */
  public static Map<String, Double> unitVector(Map<String, Double> queryVector) {
    double norm = norm(queryVector);
    HashMap<String, Double> out = new HashMap<String, Double>();
    for (Entry<String, Double> entry : queryVector.entrySet()) {
      String text = entry.getKey();
      double count = entry.getValue().doubleValue();
      out.put(text, count / norm);
    }
    return out;
  }

  /**
   * For a given sparse matrix of count, normalize it to a sparse pdf distribution.
   * 
   * @param queryVector
   * @return
   */
  public static Map<String, Double> pdfVector(Map<String, Double> A) {
    double total = 0.0;
    for (Double count : A.values()) {
      total += count.doubleValue();
    }
    HashMap<String, Double> out = new HashMap<String, Double>();
    for (Entry<String, Double> entry : A.entrySet()) {
      String text = entry.getKey();
      double count = entry.getValue().doubleValue();
      out.put(text, count / total);
    }
    return out;
  }

  /**
   * Calculate the IDF score for a given term and query id, this is the original version from the
   * TFIDF formula
   * 
   * @param term the given term
   * @param queryId the queryId used to refer to a corpus in the input documents.txt
   * @return IDF score of the given term
   */
  public static Double idf(String term, Integer queryId) {
    HashMap<String, Object> IDF = MemoryStore.getSingletonInstance(Utils.fromQueryIdToKey(queryId)).data;
    Double N = ((Number) IDF.get(Utils.NDOC_KEY)).doubleValue();
    Double n = ((Number) IDF.get(term)).doubleValue();
    // return Math.log( N - n + 0.5 / (n + 0.5) ); // IDF from Okapi BM25, can be negative and
    // problematic
    return Math.log(N / n);
  }
  /**
   * Calculate the IDF score for a given term and query id, this is the variant from the
   * Okapi BM25 formula
   * 
   * @param term the given term
   * @param queryId the queryId used to refer to a corpus in the input documents.txt
   * @return IDF score of the given term
   */
  public static Double idf2(String term, Integer queryId) {
    HashMap<String, Object> IDF = MemoryStore.getSingletonInstance(Utils.fromQueryIdToKey(queryId)).data;
    Double N = ((Number) IDF.get(Utils.NDOC_KEY)).doubleValue();
    Double n = ((Number) IDF.get(term)).doubleValue();
    // return Math.log( N - n + 0.5 / (n + 0.5) ); // IDF from Okapi BM25, can be negative and
    // problematic
    return Math.log((N - n + 0.5) / (n + 0.5));
  }

  /**
   * Return the Term Frequency of a given term
   * @param A the sparse count matrix of the document.
   * @param term the given term
   * @return the TF score of the given term
   */
  public static Double tf(Map<String, Double> A, String term) {
    Double n = A.get(term);

    Map.Entry<String, Double> maxEntry = null;
    for (Map.Entry<String, Double> entry : A.entrySet()) {
      if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
        maxEntry = entry;
      }
    }

    Double N = maxEntry.getValue().doubleValue();

    return 0.5 + ((0.5 * n) / N);
  }

  /**
   * For a given sparse count matrix and its queryId, return the TFIDF weighted version of the matrix.
   * @param A the sparse count matrix of the document.
   * @param term the given term
   * @return the TFIDF weighted version of the given matrix
   */
  public static Map<String, Double> tfidf(Map<String, Double> A, Integer queryId) {
    Map<String, Double> out = new HashMap<String, Double>();

    for (Map.Entry<String, Double> entry : A.entrySet()) {

      Double _tf = tf(A, entry.getKey());
      Double _idf = idf2(entry.getKey(), queryId);
      out.put(entry.getKey(), _tf * _idf);
    }

    return out;

  }

  /**
   * Calculate the norm of a given sparse matrix
   * @param queryVector the given matrix
   * @return the norm of the given matrix
   */
  public static double norm(Map<String, Double> queryVector) {
    double out = 0.0;
    for (Number prob : queryVector.values()) {
      out += prob.doubleValue() * prob.doubleValue();
    }
    return Math.sqrt(out);
  }

  /**
   * Calculate the dot product of two given sparse matrixes
   * @param A the first sparse matrix
   * @param B the second sparse matrix
   * @return the dot product of two given sparse matrixes
   */
  public static double dot(Map<String, Double> A, Map<String, Double> B) {
    double out = 0.0;
    for (String word : A.keySet()) {
      if (B.containsKey(word)) {
        out += A.get(word) * B.get(word);
      }
    }
    return out;
  }



  /**
   * Given two sparse matrixes, calculate the cosine similarity
   * @param queryVector the first matrix
   * @param docVector the second matrix
   * @return the cosine similarity
   */
  public static double computeCosineSimilarity(Map<String, Double> queryVector,
          Map<String, Double> docVector) {
    Map<String, Double> A = unitVector(queryVector);
    Map<String, Double> B = unitVector(docVector);

    return dot(A, B);// / (norm(A) * norm(B)); unit vectors has norm=1
  }
  
  /**
   * Given two sparse matrixes, calculate the cosine similarity
   * @param queryVector the first matrix
   * @param docVector the second matrix
   * @param queryId the queryId for finding the corresponding corpus
   * @param k the k parameter in the BM25 formula
   * @param b the b parameter in the BM25 formula
   * @return the Okapi BM25 score
   */
  public static double computeOkapiBM25Score(Map<String, Double> queryVector,
          Map<String, Double> docVector, Integer queryId, double k, double b) {

    Map<String, Double> A = unitVector(queryVector);
    Map<String, Double> B = unitVector(docVector);
    Map<String, Object> IDF = MemoryStore.getSingletonInstance(Utils.fromQueryIdToKey(queryId)).data;
    double D = ((Number) IDF.get(Utils.TOTAL_LENGTH_KEY)).doubleValue()
            / ((Number) IDF.get(Utils.NDOC_KEY)).doubleValue();
    double d = 0.0;
    for (Number value : docVector.values()) {
      d += value.doubleValue();
    }
    double out = 0.0;
    for (Entry<String, Double> entry : A.entrySet()) {
      String term = entry.getKey();
      if (!B.containsKey(term)) {
        continue;
      }
      double _tf = tf(B, entry.getKey());
      double _idf = idf2(term, queryId);

      // out += _idf * (_tf * (k + 1.0)) / (_tf + (k * (1 - b + (b * d / D))));
      out += _idf * (_tf * (k + 1.0)) / (_tf + (k * (1 - b + (0))));

    }

    return out;
  }

}
