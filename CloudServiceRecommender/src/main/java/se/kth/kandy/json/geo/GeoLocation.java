package se.kth.kandy.json.geo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author Hossein
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoLocation {

  private String country_name;
  private String city;
  private String ip;

  public GeoLocation() {
  }

  public GeoLocation(String country_name, String city, String ip) {
    this.country_name = country_name;
    this.city = city;
    this.ip = ip;
  }

  public String getCountry_name() {
    return country_name;
  }

  public void setCountry_name(String country_name) {
    this.country_name = country_name;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

}
