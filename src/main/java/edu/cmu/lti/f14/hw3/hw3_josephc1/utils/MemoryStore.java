package edu.cmu.lti.f14.hw3.hw3_josephc1.utils;

import java.util.HashMap;
import java.util.Map;

public class MemoryStore {

  private static Map<String, MemoryStore> singletonInstances;

  public HashMap<String, Object> data;

  // SingletonExample prevents any other class from instantiating
  /**
   * Initializer
   * 
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
