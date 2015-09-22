/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.servicerecommender.batch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import javax.batch.api.chunk.AbstractItemReader;
import javax.inject.Named;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.apache.log4j.Logger;

/**
 *
 * @author Hossein
 */
@Named
public class AwsEc2InstanceItemReader extends AbstractItemReader {

  private static final Logger logger = Logger.getLogger(AwsEc2InstanceItemReader.class);

  private int index = 0;

  /**
   *
   * @param checkpoint
   */
  @Override
  public void open(Serializable checkpoint) {
    logger.info("Start fetching amazon Ondemand/Reserved Instances");
  }

  /**
   *
   * @return @throws Exception
   */
  @Override
  public AwsEc2InstancesJsonPerOS readItem() throws Exception {
    while (index < AwsEc2InstancesPriceLinks.values().length) {
      URL url = new URL(AwsEc2InstancesPriceLinks.values()[index].getLink());
      BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
      String inputLine;
      String jsonP = "";
      while ((inputLine = in.readLine()) != null) {
        jsonP = jsonP.concat(inputLine);
      }
      jsonP = jsonP.substring(jsonP.indexOf("(") + 1, jsonP.lastIndexOf(")"));
      logger.debug("Aws Ec2 instance prices downloaded from : [ " + url.toString() + " ]");
      ScriptEngineManager manager = new ScriptEngineManager();
      ScriptEngine engine = manager.getEngineByName("JavaScript");
      String script = "var jsonObject = JSON.stringify(" + jsonP + ");";
      engine.eval(script);
      return new AwsEc2InstancesJsonPerOS((String) engine.get("jsonObject"),
          AwsEc2InstancesPriceLinks.values()[index++].
          getOSType());
    }
    return null;
  }

}
