package edu.cmu.lti.f14.hw3.hw3_josephc1.utils;

import java.util.Map;

import edu.cmu.lti.f14.hw3.hw3_josephc1.typesystems.Document;

public class StaticDocument implements Comparable<StaticDocument> {
  
  public int relevance;
  public int queryId;
  public Map<String, Number> vector;
  public Double score;
  public String text;
  
  public StaticDocument(Document doc) {
    relevance = doc.getRelevanceValue();
    queryId = doc.getQueryID();
    vector = Utils.fromDocumentToVector(doc);
    text = doc.getText();
  }

  public int compareTo(StaticDocument arg0) {
    return score.compareTo(((arg0).score));
  }
  
  public String toString() {
    return "<doc:qid=" + queryId + ":rel=" + relevance + ":score=" + score + " " + text + ">";
  }

}
