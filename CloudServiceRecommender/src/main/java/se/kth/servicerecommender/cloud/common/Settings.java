/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.servicerecommender.cloud.common;

import java.io.File;
import org.jclouds.aws.domain.Region;
import org.jclouds.ec2.domain.InstanceType;

/**
 *
 * @author Hossein
 */
public class Settings {

  //Providers
  public static final String PROVIDER_EC2_DEFAULT_TYPE = InstanceType.M1_MEDIUM;
  public static final String PROVIDER_EC2_DEFAULT_REGION = Region.EU_WEST_1;
  public static final String PROVIDER_EC2_DEFAULT_AMI = "ami-0307ce74"; //12.04  "ami-896c96fe"; // 14.04
  public static final String PROVIDER_EC2_DEFAULT_USERNAME = "ubuntu";
  public static final String PROVIDER_BAREMETAL_DEFAULT_USERNAME = "root";

  //Jcloud settings
  public static final int JCLOUDS_PROPERTY_MAX_RETRIES = 100;
  public static final int JCLOUDS_PROPERTY_RETRY_DELAY_START = 1000; //ms
  public static final int EC2_MAX_FORK_VMS_PER_REQUEST = 50;

  public static final String USER_HOME = System.getProperty("user.home");
  public static final String USER_NAME = System.getProperty("user.name");
  public static final String OS_NAME = System.getProperty("os.name");
  public static final boolean UNIX_OS = OS_NAME.toLowerCase().contains("mac")
      || OS_NAME.toLowerCase().contains("linux");
  public static final String DEFAULT_PUBKEY_PATH = UNIX_OS ? USER_HOME + "/.ssh/id_rsa.pub" : null;
  public static final String DEFAULT_PRIKEY_PATH = UNIX_OS ? USER_HOME + "/.ssh/id_rsa" : null;
  public static final String DEFAULT_CREDENTIALS_PATH = UNIX_OS ? "/opt/sina/.servicerecommender/.aws"
      : "c:\\opt\\sina\\.servicerecommender\\.aws";
  public static final String SSH_PUBKEY_PATH_KEY = "ssh.publickey.path";
  public static final String SSH_PRIVKEY_PATH_KEY = "ssh.privatekey.path";
  public static final String SSH_PRIVKEY_PASSPHRASE = "ssh.privatekey.passphrase";
  public static final String AWS_ACCESS_KEY = "aws.access.key";
  public static final String AWS_ACCESS_KEY_ENV_VAR = "aws_access_key_id";
  public static final String AWS_SECRET_KEY = "aws.secret.key";
  public static final String AWS_SECRET_KEY_ENV_VAR = "aws_secret_access_key";
  public static final String AWS_KEYPAIR_NAME_KEY = "aws.keypair.name";
  public static final String GCE_JSON_KEY_FILE_PATH = "gce.jsonkey.path";

  public static final String SERVICERECOMMENDER_ROOT_PATH = USER_HOME + File.separator + ".servicerecommender";
  public static final String YAML_FILE_NAME = "definition.yaml";
  public static final String SERVICERECOMMENDER_CONF_NAME = "conf";
  public static final String SERVICERECOMMENDER_CREDENTIALS_NAME = "credentials";
  public static final String SSH_FOLDER_NAME = ".ssh";
  public static final String TMP_FOLDER_NAME = "tmp";
  public static final String SYSTEM_TMP_FOLDER_PATH = "/" + TMP_FOLDER_NAME;
  public static final String SERVICERECOMMENDER_SSH_PATH = SERVICERECOMMENDER_ROOT_PATH + File.separator
      + SSH_FOLDER_NAME;
  public static final String SERVICERECOMMENDER_TMP_PATH = SERVICERECOMMENDER_ROOT_PATH + File.separator
      + TMP_FOLDER_NAME;
  public static final String SSH_PUBKEY_FILENAME = "ida_rsa.pub";
  public static final String SSH_PRIVKEY_FILENAME = "ida_rsa";
  public static final String RECIPE_RESULT_POSFIX = "__out.json";

}
