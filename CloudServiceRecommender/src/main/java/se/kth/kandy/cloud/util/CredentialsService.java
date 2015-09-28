/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.kandy.cloud.util;

import se.kth.kandy.cloud.common.Confs;
import se.kth.kandy.cloud.common.Ec2Credentials;
import se.kth.kandy.cloud.common.Settings;
import se.kth.kandy.cloud.common.SshKeyPair;
import se.kth.kandy.cloud.common.SshKeyService;
import se.kth.kandy.cloud.common.exception.ServiceRecommanderException;

/**
 *
 * @author Hossein
 */
public class CredentialsService {

  public SshKeyPair loadSshKeysIfExist() throws ServiceRecommanderException {
    Confs confs = Confs.loadServiceRecommanderConfs();
    SshKeyPair sshKeys = SshKeyService.loadSshKeys(confs);
    return sshKeys;
  }

  public SshKeyPair generateSshKeysAndUpdateConf() throws ServiceRecommanderException {
    SshKeyPair sshkeys = SshKeyService.generateAndStoreSshKeys();
    Confs confs = Confs.loadConfs();
    confs.put(Settings.SSH_PRIVKEY_PATH_KEY, sshkeys.getPrivateKeyPath());
    confs.put(Settings.SSH_PUBKEY_PATH_KEY, sshkeys.getPublicKeyPath());
    if (sshkeys.getPassphrase() != null && sshkeys.getPassphrase().isEmpty() == false) {
      confs.put(Settings.SSH_PRIVKEY_PASSPHRASE, sshkeys.getPassphrase());
    }
    confs.writeServiceRecommanderConfs();
    return sshkeys;
  }

  public Ec2Credentials loadEc2Credentials() {
    Confs confs = Confs.loadServiceRecommanderCredentialsConf();
    return readCredentials(confs);
  }

  private Ec2Credentials readCredentials(Confs confs) {

    String accessKey = confs.getProperty(Settings.AWS_ACCESS_KEY_ENV_VAR);
    String secretKey = confs.getProperty(Settings.AWS_SECRET_KEY_ENV_VAR);
    Ec2Credentials credentials = null;
    if (accessKey != null && !accessKey.isEmpty() && secretKey != null && !accessKey.isEmpty()) {
      credentials = new Ec2Credentials();
      credentials.setAccessKey(accessKey);
      credentials.setSecretKey(secretKey);
    }
    return credentials;
  }
}
