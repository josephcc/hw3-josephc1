package edu.cmu.lti.f14.hw3.hw3_josephc1.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * A Singleton Pattern class that stores global context for different query IDs, i.e., document
 * lengths, document frequencies, number of documents. The key for retrieving different attributes
 * are in the Utils class.
 * 
 * @author josephcc
 * 
 */
public class MemoryStore {

  private static Map<String, MemoryStore> singletonInstances;

  public HashMap<String, Object> data;

  /**
   * Initializer Initialize the singleton.
   * 
   */
  private MemoryStore() {
    data = new HashMap<String, Object>();
  }

  /**
   * Global point of entry to the singleton classes with unique models
   * 
   * @param tableName
   * @return
   */
  public static MemoryStore getSingletonInstance(String tableName) {
    if (null == singletonInstances) {
      singletonInstances = new HashMap<String, MemoryStore>();
    }
    if (!singletonInstances.containsKey(tableName)) {
      singletonInstances.put(tableName, new MemoryStore());
    }
    return singletonInstances.get(tableName);
  }

}
