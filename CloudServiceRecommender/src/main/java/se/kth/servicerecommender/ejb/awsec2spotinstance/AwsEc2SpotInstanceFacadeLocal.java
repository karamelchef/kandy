/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.servicerecommender.ejb.awsec2spotinstance;

import java.util.List;
import javax.ejb.Local;
import se.kth.servicerecommender.model.AwsEc2SpotInstance;

/**
 *
 * @author Hossein
 */
@Local
public interface AwsEc2SpotInstanceFacadeLocal {

  AwsEc2SpotInstance create(AwsEc2SpotInstance spotInstance);

  AwsEc2SpotInstance edit(AwsEc2SpotInstance spotInstance);

  void remove(AwsEc2SpotInstance spotInstance);

  AwsEc2SpotInstance find(Object id);

  List<AwsEc2SpotInstance> findAll();

  List<AwsEc2SpotInstance> findRange(int[] range);

  int count();

}
