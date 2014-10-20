package edu.cmu.lti.f14.hw3.hw3_josephc1.casconsumers;

import java.io.IOException;
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

  private ArrayList<StaticDocument> queries;

  private HashMap<Integer, ArrayList<StaticDocument>> corpora;

  public void initialize() throws ResourceInitializationException {
    queries = new ArrayList<StaticDocument>();
    corpora = new HashMap<Integer, ArrayList<StaticDocument>>();
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
   * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2. Compute the MRR metric
   */
  @Override
  public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
          IOException {

    super.collectionProcessComplete(arg0);
    scoreAndRank();
    report();
  }

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
        r += 1;
      }
    }
    String out = String.format("MRR=%.4f", metric_mrr);
    System.out.println(out);
  }

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
        candidate.score = tfidfCosineSimilarity;
      }
      Collections.sort(corpora.get(queryId), Collections.reverseOrder());
    }
  }

  /**
   * 
   * @return mrr
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
