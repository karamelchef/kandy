/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.servicerecommander.ejb.datacontroller;

import java.util.List;
import javax.ejb.Local;
import se.kth.servicerecommander.model.SpotInstance;

/**
 *
 * @author Hossein
 */
@Local
public interface SpotInstanceFacadeLocal {

  void create(SpotInstance spotInstance);

  void edit(SpotInstance spotInstance);

  void remove(SpotInstance spotInstance);

  SpotInstance find(Object id);

  List<SpotInstance> findAll();

  List<SpotInstance> findRange(int[] range);

  int count();
  
}
