package edu.cmu.lti.f14.hw3.hw3_josephc1.utils;

import java.util.Map;

import edu.cmu.lti.f14.hw3.hw3_josephc1.typesystems.Document;

public class StaticDocument {
  
  public int relevance;
  public int queryId;
  public Map<String, Number> vector;
  
  public StaticDocument(Document doc) {
    relevance = doc.getRelevanceValue();
    queryId = doc.getQueryID();
    vector = Utils.fromDocumentToVector(doc);
  }

}
