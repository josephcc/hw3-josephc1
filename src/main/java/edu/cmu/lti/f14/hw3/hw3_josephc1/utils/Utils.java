package edu.cmu.lti.f14.hw3.hw3_josephc1.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.lucene.analysis.en.EnglishMinimalStemmer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.EmptyFSList;
import org.apache.uima.jcas.cas.EmptyStringList;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.jcas.cas.NonEmptyStringList;
import org.apache.uima.jcas.cas.StringList;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.tartarus.snowball.ext.PorterStemmer;
import org.uimafit.util.JCasUtil;

import edu.cmu.lti.f14.hw3.hw3_josephc1.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_josephc1.typesystems.Token;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * A class with a private constructor and only static public functions that stores the different
 * components
 * 
 * @author josephcc
 * 
 */
public class Utils {
  private Utils() {
  }

  /**
   * The key for retrieving number of documents in a corpus in the MemoryStore Singleton
   */
  public static final String NDOC_KEY = "::NDOC::";

  /**
   * The key for retrieving total length of documents in a corpus in the MemoryStore Singleton
   */
  public static final String TOTAL_LENGTH_KEY = "::TOTAL_LENGTH::";

  /**
   * The key for retrieving the memory hash of queryId corpus in the MemoryStore Singleton
   */
  public static String fromQueryIdToKey(Integer queryId) {
    return "IDF:QID" + queryId;
  }

  /**
   * Reference to the Properties parameter for StanfordCoreNlp
   */
  private static Properties props;

  /**
   * Reference to the StanfordCoreNLP pipeline
   */
  private static StanfordCoreNLP pipeline;

  /**
   * Reference to the EnglishMinimalStemmer from lucene
   */
  private static EnglishMinimalStemmer stemmer;
  
  /**
   * static initialization blocks to load all the models from external libraries
   */
  static {
    props = new Properties();
    props.put("annotators", "tokenize");
    pipeline = new StanfordCoreNLP(props);
    stemmer = new EnglishMinimalStemmer();
  }

  public static ArrayList<Token> fromMapToTokenList(JCas jcas, Map<String, MutableInteger> counter) {
    ArrayList<Token> tokenList = new ArrayList<Token>(counter.size());
    for (Entry<String, MutableInteger> entry : counter.entrySet()) {
      String text = entry.getKey();
      MutableInteger freq = entry.getValue();
      Token token = new Token(jcas);
      token.setText(text);
      token.setFrequency(freq.get());
      tokenList.add(token);
    }
    return tokenList;
  }

  public static Map<String, Double> fromIntegerMapToDoubleMap(Map<String, Number> vector) {
    Map<String, Double> out = new HashMap<String, Double>();
    for (Entry<String, Number> entry : vector.entrySet()) {
      out.put(entry.getKey(), entry.getValue().doubleValue());
    }
    return out;
  }

  public static Map<String, Number> fromTokenListToMap(ArrayList<Token> tokenList) {
    Map<String, Number> counter = new HashMap<String, Number>();
    for (Token token : tokenList) {
      counter.put(token.getText(), token.getFrequency());
    }
    return counter;
  }

  public static Map<String, Number> fromDocumentToVector(Document doc) {
    FSList fsTokenList = doc.getTokenList();
    ArrayList<Token> tokenList = Utils.fromFSListToCollection(fsTokenList, Token.class);
    Map<String, Number> counter = Utils.fromTokenListToMap(tokenList);
    return counter;
  }

  public static <T extends TOP> ArrayList<T> fromFSListToCollection(FSList list, Class<T> classType) {

    Collection<T> myCollection = JCasUtil.select(list, classType);
    /*
     * for(T element:myCollection){ System.out.println(.getText()); }
     */

    return new ArrayList<T>(myCollection);
  }

  public static StringList createStringList(JCas aJCas, Collection<String> aCollection) {
    if (aCollection.size() == 0) {
      return new EmptyStringList(aJCas);
    }

    NonEmptyStringList head = new NonEmptyStringList(aJCas);
    NonEmptyStringList list = head;
    Iterator<String> i = aCollection.iterator();
    while (i.hasNext()) {
      head.setHead(i.next());
      if (i.hasNext()) {
        head.setTail(new NonEmptyStringList(aJCas));
        head = (NonEmptyStringList) head.getTail();
      } else {
        head.setTail(new EmptyStringList(aJCas));
      }
    }

    return list;
  }

  public static <T extends Annotation> FSList fromCollectionToFSList(JCas aJCas,
          Collection<T> aCollection) {
    if (aCollection.size() == 0) {
      return new EmptyFSList(aJCas);
    }

    NonEmptyFSList head = new NonEmptyFSList(aJCas);
    NonEmptyFSList list = head;
    Iterator<T> i = aCollection.iterator();
    while (i.hasNext()) {
      head.setHead(i.next());
      if (i.hasNext()) {
        head.setTail(new NonEmptyFSList(aJCas));
        head = (NonEmptyFSList) head.getTail();
      } else {
        head.setTail(new EmptyFSList(aJCas));
      }
    }

    return list;
  }

  public static List<String> spaceTokenizer(String doc) {
    List<String> res = new ArrayList<String>();

    for (String s : doc.split("\\s+")) {
      res.add(s);
    }
    return res;
  }

  public static String minimalStem(String word) {
    char[] _word = word.toCharArray();
    int _length = stemmer.stem(_word, word.length());
    return new String(_word, 0, _length);
  }

  public static String porterStem(String word) {

    PorterStemmer obj = new PorterStemmer();
    obj.setCurrent(word);
    obj.stem();
    String out = obj.getCurrent();
    return out;
  }

  public static List<String> stanfordTokenizer(String doc) {
    List<String> res = new ArrayList<String>();

    edu.stanford.nlp.pipeline.Annotation document = new edu.stanford.nlp.pipeline.Annotation(doc);
    Utils.pipeline.annotate(document);

    for (CoreLabel token : document.get(TokensAnnotation.class)) {
      String word = token.get(TextAnnotation.class).toLowerCase();
      res.add(word);
    }
    return res;
  }

  /**
   * Mutable version of the Integer class for fast counting
   * @author josephcc
   *
   */
  public static class MutableInteger {

    /**
     * the actual value
     */
    private int val;

    /**
     * initializer
     * @param val
     */
    public MutableInteger(int val) {
      this.val = val;
    }

    /**
     * Retrieve the value
     * @return the integer
     */
    public int get() {
      return val;
    }

    /**
     * Set the value
     * @param val
     */
    public void set(int val) {
      this.val = val;
    }

    /**
     * pretty print
     */
    public String toString() {
      return Integer.toString(val);
    }
  }
}
