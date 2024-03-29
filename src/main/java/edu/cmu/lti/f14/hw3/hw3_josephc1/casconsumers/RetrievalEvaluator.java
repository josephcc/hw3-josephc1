package edu.cmu.lti.f14.hw3.hw3_josephc1.casconsumers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f14.hw3.hw3_josephc1.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_josephc1.utils.Similarity;
import edu.cmu.lti.f14.hw3.hw3_josephc1.utils.StaticDocument;
import edu.cmu.lti.f14.hw3.hw3_josephc1.utils.Utils;

public class RetrievalEvaluator extends CasConsumer_ImplBase {
  /**
   * Cache for all the queries.
   */
  private ArrayList<StaticDocument> queries;
  
  /**
   * Cache for all the corpora.
   */
  private HashMap<Integer, ArrayList<StaticDocument>> corpora;

  /**
   * Handle to the final output file
   */
  private PrintWriter outFile;

  /**
   * Path to the final output file
   */
  private final String filename = "report.txt";

  /**
   * Initializer
   * 
   * Initialize output file handle and the private variables that stores the queries and corpora.
   * 
   * @see org.apache.uima.collection.CasConsumer_ImplBase#initialize(org.apache.uima.resource.ResourceSpecifier,
   *      java.util.Map)
   */
  public void initialize() throws ResourceInitializationException {
    queries = new ArrayList<StaticDocument>();
    corpora = new HashMap<Integer, ArrayList<StaticDocument>>();
    try {
      outFile = new PrintWriter(filename, "UTF-8");
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Release resources
   * 
   * Release output file handle before class is destroyed
   * 
   * @see org.apache.uima.collection.CasConsumer_ImplBase#destroy()
   */
  @Override
  public void destroy() {
    outFile.close();
    super.destroy();
  }

  /**
   * Store the quries and corpus in the private variable. Global word frequency and sentence tokens
   * are processed in previous annotator.
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
      Integer queryId = doc.getQueryID();
      if (doc.getRelevanceValue() == 99) {
        queries.add(new StaticDocument(doc));
      } else {
        if (corpora.get(queryId) == null) {
          corpora.put(queryId, new ArrayList<StaticDocument>());
        }
        corpora.get(queryId).add(new StaticDocument(doc));
      }
    }

  }

  /**
   * Score, rank the documents based on the query, calculate the MRR score, and output to file.
   */
  @Override
  public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
          IOException {

    super.collectionProcessComplete(arg0);
    scoreAndRank();
    report();
  }

  /**
   * Write the results to the output file.
   */
  private void report() {
    double metric_mrr = compute_mrr();
    for (StaticDocument query : queries) {
      Integer queryId = query.queryId;
      System.out.printf("qid=%d\t%s\n", query.queryId, query.text);
      System.out.println("\t\t\t" + query.vector);
      int r = 1;
      for (StaticDocument candidate : corpora.get(queryId)) {
        String out = String.format("cosine=%.4f\trank=%d\tqid=%d\trel=%d\t%s", candidate.score, r,
                candidate.queryId, candidate.relevance, candidate.text);
        System.out.println(out);
        System.out.println("\t\t\t" + candidate.vector);
        if (candidate.relevance == 1) {
          outFile.println(out);
        }
        r += 1;
      }
    }
    String out = String.format("MRR=%.4f", metric_mrr);
    System.out.println(out);
    outFile.println(out);
  }

  /**
   * Score rank the documents.
   */
  private void scoreAndRank() {
    for (StaticDocument query : queries) {
      Integer queryId = query.queryId;
      Map<String, Double> queryVector = Utils.fromIntegerMapToDoubleMap(query.vector);
      for (StaticDocument candidate : corpora.get(queryId)) {
        Map<String, Double> docVector = Utils.fromIntegerMapToDoubleMap(candidate.vector);
        double cosineSimilarity = Similarity.computeCosineSimilarity(queryVector, docVector);
        double tfidfCosineSimilarity = Similarity.computeCosineSimilarity(
                Similarity.tfidf(queryVector, queryId), Similarity.tfidf(docVector, queryId));
        double okapiScore = Similarity.computeOkapiBM25Score(queryVector, docVector, queryId, 1.2,
                0.75); // k=1.2~2.0 b=0.75
        candidate.score = cosineSimilarity;
      }
      Collections.sort(corpora.get(queryId), Collections.reverseOrder());
    }
  }

  /**
   * Calculate the MRR score on the ranked documents.
   * 
   * @return mrr the MRR score
   */
  private double compute_mrr() {
    double metric_mrr = 0.0;
    System.out.printf("\n[");
    for (StaticDocument query : queries) {
      Integer queryId = query.queryId;
      int r = 1;
      for (StaticDocument candidate : corpora.get(queryId)) {
        if (candidate.relevance == 1) {
          break;
        }
        r += 1;
      }
      metric_mrr += 1.0 / r;
      System.out.printf((1.0 / r) + ", ");
    }
    System.out.printf("]\n");
    metric_mrr /= queries.size();

    return metric_mrr;
  }

}
