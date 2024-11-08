package smsresponder;

import java.util.*;
import java.util.Properties;

public class InstanceConfig {
   Properties configFile;
   public InstanceConfig()
   {
    configFile = new java.util.Properties();
    try {
      configFile.load(this.getClass().getClassLoader().
      // getResourceAsStream("myapp/config.cfg"));
      getResourceAsStream("META-INF/instance.conf"));
    }
    catch(Exception eta){
        eta.printStackTrace();
    }
   }
 
   public String getProperty(String key)
   {
    String value = this.configFile.getProperty(key);
    return value;
   }
}