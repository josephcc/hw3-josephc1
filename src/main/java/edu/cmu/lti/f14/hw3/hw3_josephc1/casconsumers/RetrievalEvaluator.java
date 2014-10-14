package edu.cmu.lti.f14.hw3.hw3_josephc1.casconsumers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f14.hw3.hw3_josephc1.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_josephc1.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_josephc1.utils.MemoryStore;
import edu.cmu.lti.f14.hw3.hw3_josephc1.utils.Utils;

public class RetrievalEvaluator extends CasConsumer_ImplBase {

  /** query id number **/
  public ArrayList<Integer> qIdList;

  /** query and text relevant values **/
  public ArrayList<Integer> relList;

  public void initialize() throws ResourceInitializationException {

    qIdList = new ArrayList<Integer>();

    relList = new ArrayList<Integer>();

  }

  /**
   * TODO :: 1. construct the global word dictionary 2. keep the word frequency for each sentence
   */
  @Override
  public void processCas(CAS aCas) throws ResourceProcessException {

    JCas jcas;
    try {
      jcas = aCas.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }


    FSIterator<?> it = jcas.getAnnotationIndex(Document.type).iterator();

    if (it.hasNext()) {
      Document doc = (Document) it.next();

      // Make sure that your previous annotators have populated this in CAS
      FSList fsTokenList = doc.getTokenList();
      ArrayList<Token> tokenList = Utils.fromFSListToCollection(fsTokenList, Token.class);
      Map<String, Number> counter = Utils.fromTokenListToMap(tokenList);
      System.out.println(counter);
      System.out.println(unitVector(counter));
      System.out.println(MemoryStore.getSingletonInstance(Utils.fromQueryIdToKey(doc.getQueryID())).data);
      System.out.println(tfidf(counter, doc.getQueryID()));
      System.out.println(unitVector(tfidf(counter, doc.getQueryID())));
      System.out.println("-------------");


      qIdList.add(doc.getQueryID());
      relList.add(doc.getRelevanceValue());

      // Do something useful here

    }

  }

  /**
   * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2. Compute the MRR metric
   */
  @Override
  public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
          IOException {

    super.collectionProcessComplete(arg0);

    // TODO :: compute the cosine similarity measure

    // TODO :: compute the rank of retrieved sentences

    // TODO :: compute the metric:: mean reciprocal rank
    double metric_mrr = compute_mrr();
    System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
  }

  private Map<String, Double> unitVector(Map<String, Number> A){
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
  
  private Double idf(String term, Integer queryId) {
    HashMap<String, Object> IDF = MemoryStore.getSingletonInstance(Utils.fromQueryIdToKey(queryId)).data;
    Double N = ((Integer) IDF.get(Utils.NDOC_KEY)).doubleValue();
    Double n = ((Number)IDF.get(term)).doubleValue();
    //return Math.log( N - n + 0.5 / (n + 0.5) ); // IDF from Okapi BM25, can be negative and problematic
    return Math.log(N/n);
  }

  private Double tf(Map<String, Number> A, String term) {
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
  
  private Map<String, Number> tfidf(Map<String, Number> A, Integer queryId) {
    Map<String, Number> out = new HashMap<String, Number>();
    
    for (Map.Entry<String, Number> entry : A.entrySet()) {
      
      Double _tf = tf(A, entry.getKey());
      Double _idf = idf(entry.getKey(), queryId);
      out.put(entry.getKey(), _tf * _idf);
    }
    
    return out;
    
  }

  
  private double norm(Map<String, Double> A){
    double out = 0.0;      
    for (double prob: A.values()) {
      out += prob*prob;
    }
    return Math.sqrt(out);
  }
  
  private double dot(Map<String, Double> A, Map<String, Double> B) {
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
  private double computeCosineSimilarity(Map<String, Number> queryVector, Map<String, Number> docVector) {
    Map<String, Double> A = unitVector(queryVector);
    Map<String, Double> B = unitVector(docVector);
    return dot(A, B) / (norm(A) * norm(B));
  }

  /**
   * 
   * @return mrr
   */
  private double compute_mrr() {
    double metric_mrr = 0.0;

    // TODO :: compute Mean Reciprocal Rank (MRR) of the text collection

    return metric_mrr;
  }

}
