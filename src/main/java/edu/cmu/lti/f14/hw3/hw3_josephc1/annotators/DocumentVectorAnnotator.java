package edu.cmu.lti.f14.hw3.hw3_josephc1.annotators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f14.hw3.hw3_josephc1.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_josephc1.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_josephc1.utils.MemoryStore;
import edu.cmu.lti.f14.hw3.hw3_josephc1.utils.Utils;
import edu.cmu.lti.f14.hw3.hw3_josephc1.utils.Utils.MutableInteger;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
		if (iter.isValid()) {
			iter.moveToNext();
			Document doc = (Document) iter.get();
			createTermFreqVector(jcas, doc);
		}

	}

	
	/**
	 * 
	 * @param jcas
	 * @param doc
	 */
	private void createTermFreqVector(JCas jcas, Document doc) {
	  
	  List<String> tokens = Utils.stanfordTokenizer(doc.getText());

    HashMap<String, MutableInteger> counter = new HashMap<String, MutableInteger>();
    HashMap<String, Object> IDF = MemoryStore.getSingletonInstance(Utils.fromQueryIdToKey(doc.getQueryID())).data;
    Integer ndoc = (Integer) IDF.get(Utils.NDOC_KEY);
    if (ndoc == null) {
      IDF.put(Utils.NDOC_KEY, Integer.valueOf(1));
    } else {
      IDF.put(Utils.NDOC_KEY, Integer.valueOf(1 + ndoc));
    }
    int document_length = 0;
    for (String word : tokens) {
       if (word.length() < 4) {
         continue;
       }
       document_length += 1;
       MutableInteger initValue = new MutableInteger(1);
       MutableInteger oldValue = counter.put(word, initValue);
      
       if(oldValue != null) {
         initValue.set(oldValue.get() + 1);
       } else {
         Integer current = (Integer) IDF.get(word);
         if (current == null) {
           IDF.put(word, Integer.valueOf(1));
         } else {
           IDF.put(word, Integer.valueOf(1 + current));
         }
       }
    }
    Integer total_length = (Integer) IDF.get(Utils.TOTAL_LENGTH_KEY);
    if (total_length == null) {
      IDF.put(Utils.TOTAL_LENGTH_KEY, document_length);
    } else {
      IDF.put(Utils.TOTAL_LENGTH_KEY, document_length + total_length);
    }
    ArrayList<Token> tokenList = Utils.fromMapToTokenList(jcas, counter);
    FSList tokenFSList = Utils.fromCollectionToFSList(jcas, tokenList);
    doc.setTokenList(tokenFSList);
	}

}
