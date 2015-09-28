/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.kandy.cloud.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;
import se.kth.kandy.cloud.common.exception.ServiceRecommanderException;

/**
 *
 * @author kamal
 * @param <K>
 * @param <V>
 */
public class Confs<K extends String, V extends String> extends Properties {

  private static final Logger logger = Logger.getLogger(Confs.class);

  public synchronized <K, V> void set(K k, V v) {
    if (v == null || v.toString().isEmpty()) {
      if (contains(k)) {
        remove(k);
      }
    } else {
      super.put(k, v);
    }
  }

  public void writeServiceRecommanderConfs() {
    File folder = new File(Settings.SERVICERECOMMENDER_ROOT_PATH);
    writeConfs(folder);
  }

  public void writeConfs(File folder) {
    FileOutputStream out = null;
    try {

      if (!folder.exists()) {
        folder.mkdirs();
      }
      File file = new File(folder, Settings.SERVICERECOMMENDER_CONF_NAME);
      out = new FileOutputStream(file);
      store(out, "ServiceRecommander configurations");
      logger.info("Conf file generated");
    } catch (IOException ex) {
      logger.error("", ex);
    } finally {
      try {
        out.close();
      } catch (IOException ex) {
        logger.error("", ex);
      }
    }
  }

  /**
   * Load credentials configuration from specified path
   *
   * @return
   */
  public static Confs loadServiceRecommanderCredentialsConf() {

    Confs prop = new Confs();
    File file = new File(Settings.DEFAULT_CREDENTIALS_PATH, Settings.SERVICERECOMMENDER_CREDENTIALS_NAME);
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      prop.load(fis);
    } catch (IOException ex) {
      logger.error("", new ServiceRecommanderException("Could not find credentials at: " + file));
    }
    return prop;
  }

  /**
   * Load configuration from default project path
   *
   * @return
   */
  public static Confs loadServiceRecommanderConfs() {
    return loadConfs(Settings.SERVICERECOMMENDER_ROOT_PATH);
  }

  public static Confs loadConfs(String folder) {
    Confs prop = new Confs();
    try {
      File folders = new File(folder);
      File file = new File(folders, Settings.SERVICERECOMMENDER_CONF_NAME);
      if (!folders.exists()) {
        logger.info(String.format("Created empty configuration folder cause didn't exist. %s'", folder));
        folders.mkdirs();
      } else {
        FileInputStream fis = new FileInputStream(file);
        prop.load(fis);
      }
    } catch (IOException e) {
      logger.warn(String.format("Couldn't find ServiceRecommander conf file in '%s'", folder));
    }
    return applyDefaults(prop);
  }

  public static Confs loadConfs() {
    Confs prop = new Confs();
    return applyDefaults(prop);
  }

  public static Confs applyDefaults(Confs prop) {
    String pubKeyPath = prop.getProperty(Settings.SSH_PUBKEY_PATH_KEY);
    String priKeyPath = prop.getProperty(Settings.SSH_PRIVKEY_PATH_KEY);
    if ((pubKeyPath == null || priKeyPath == null)) {
      if (Settings.DEFAULT_PRIKEY_PATH != null) { // In an unix operating system
        pubKeyPath = Settings.DEFAULT_PUBKEY_PATH;
        priKeyPath = Settings.DEFAULT_PRIKEY_PATH;
        prop.put(Settings.SSH_PUBKEY_PATH_KEY, pubKeyPath);
        prop.put(Settings.SSH_PRIVKEY_PATH_KEY, priKeyPath);
      }
    }
    return prop;
  }

  @Override
  public synchronized Confs clone() {
    Confs clone = new Confs();
    for (String prop : stringPropertyNames()) {
      clone.put(prop, getProperty(prop));
    }
    return clone;
  }

}
