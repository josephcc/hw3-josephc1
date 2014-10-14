package edu.cmu.lti.f14.hw3.hw3_josephc1.annotators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f14.hw3.hw3_josephc1.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_josephc1.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_josephc1.utils.Utils;
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
	
	class MutableInteger {
	  
	  private int val;
	 
	  public MutableInteger(int val) {
	    this.val = val;
	  }
	 
	  public int get() {
	    return val;
	  }
	 
	  public void set(int val) {
	    this.val = val;
	  }
	 
	  public String toString(){
	    return Integer.toString(val);
	  }
	}
	
	/**
	 * 
	 * @param jcas
	 * @param doc
	 */
	private void createTermFreqVector(JCas jcas, Document doc) {
	  
	  //joseph TODO: init this in cls initzr
    Properties props = new Properties();
    props.put("annotators", "tokenize");
    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		String docText = doc.getText();
		edu.stanford.nlp.pipeline.Annotation document = new edu.stanford.nlp.pipeline.Annotation(docText);
    pipeline.annotate(document);

    HashMap<String, MutableInteger> counter = new HashMap<String, MutableInteger>();
    for (CoreLabel token : document.get(TokensAnnotation.class)) {
       String word = token.get(TextAnnotation.class).toLowerCase();
       MutableInteger initValue = new MutableInteger(1);
       MutableInteger oldValue = counter.put(word, initValue);
      
       if(oldValue != null){
         initValue.set(oldValue.get() + 1);
       }
    }
    
    ArrayList<Token> tokenList = new ArrayList<Token>(counter.size());
    for (Entry<String, MutableInteger> entry : counter.entrySet()) {
      String text = entry.getKey();
      MutableInteger freq = entry.getValue();
      Token token = new Token(jcas);
      token.setText(text);
      token.setFrequency(freq.get());
      tokenList.add(token);
    }
    FSList tokenFSList = Utils.fromCollectionToFSList(jcas, tokenList);
    doc.setTokenList(tokenFSList);
	}

}
