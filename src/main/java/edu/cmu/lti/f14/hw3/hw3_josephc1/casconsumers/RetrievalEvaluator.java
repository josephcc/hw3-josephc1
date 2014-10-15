package edu.cmu.lti.f14.hw3.hw3_josephc1.casconsumers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.impl.CASSerializer;
import org.apache.uima.cas.impl.Serialization;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f14.hw3.hw3_josephc1.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_josephc1.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_josephc1.utils.MemoryStore;
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

    // TODO :: compute the cosine similarity measure

    // TODO :: compute the rank of retrieved sentences

    // TODO :: compute the metric:: mean reciprocal rank

    for (StaticDocument query : queries) {
      /*
       * - - // Make sure that your previous annotators have populated this in CAS - FSList
       * fsTokenList = doc.getTokenList(); - ArrayList<Token> tokenList =
       * Utils.fromFSListToCollection(fsTokenList, Token.class); - Map<String, Number> counter =
       * Utils.fromTokenListToMap(tokenList); - System.out.println(counter); -
       * System.out.println(Similarity.unitVector(counter)); -
       * System.out.println(MemoryStore.getSingletonInstance
       * (Utils.fromQueryIdToKey(doc.getQueryID())).data); -
       * System.out.println(Similarity.tfidf(counter, doc.getQueryID())); -
       * System.out.println(Similarity.unitVector(Similarity.tfidf(counter, doc.getQueryID()))); -
       * System.out.println("-------------");
       */
      // System.out.println(query);
      System.out.println(query.queryId);

      Integer queryId = query.queryId;
      Map<String, Number> queryVector = query.vector;

      for (StaticDocument candidate : corpora.get(queryId)) {
        Map<String, Number> docVector = candidate.vector;
        System.out.println("---------------");
        System.out.println(candidate.relevance);
        System.out.println(Similarity.computeCosineSimilarity(queryVector, docVector));
        System.out.println(Similarity.computeCosineSimilarity(Similarity.tfidf(queryVector, queryId), Similarity.tfidf(docVector, queryId)));
        System.out.println(Similarity.computeOkapiBM25Score(queryVector, docVector, queryId, 1.2, 0.75)); // k=1.2~2.0 b=0.75
        System.out.println(Similarity.computeOkapiBM25Score(queryVector, docVector, queryId, 2.0, 0.75)); // k=1.2~2.0 b=0.75
        System.out.println("---------------");

      }

    }

    double metric_mrr = compute_mrr();
    System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
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
