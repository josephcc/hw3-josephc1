package edu.cmu.lti.f14.hw3.hw3_josephc1.utils;

import java.util.Map;

import edu.cmu.lti.f14.hw3.hw3_josephc1.typesystems.Document;

/**
 * A simple class that contains the attributes that maps to the Document class for in memory
 * "serialization"
 * 
 * @author josephcc
 * 
 */
public class StaticDocument implements Comparable<StaticDocument> {

  /**
   * Correspond to the relevance attribute in Document
   */
  public int relevance;

  /**
   * Correspond to the queryId attribute in Document
   */
  public int queryId;

  /**
   * A sparse matrix constructed from the TokenList attribute in Document
   */
  public Map<String, Number> vector;

  /**
   * Correspond to the score attribute in Document
   */
  public Double score;

  /**
   * Correspond to the relevance attribute in Document
   */
  public String text;

  /**
   * Serialize the Document class
   * 
   * @param doc
   */
  public StaticDocument(Document doc) {
    relevance = doc.getRelevanceValue();
    queryId = doc.getQueryID();
    vector = Utils.fromDocumentToVector(doc);
    text = doc.getText();
  }

  /**
   * Compare StaticDocument instances using the score attribute for ranking
   */
  public int compareTo(StaticDocument arg0) {
    return score.compareTo(((arg0).score));
  }

  /**
   * pretty print
   */
  public String toString() {
    return "<doc:qid=" + queryId + ":rel=" + relevance + ":score=" + score + " " + text + ">";
  }

}
