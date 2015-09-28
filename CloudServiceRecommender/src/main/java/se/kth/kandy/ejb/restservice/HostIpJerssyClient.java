package se.kth.kandy.ejb.restservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import se.kth.kandy.json.geo.GeoLocation;

/**
 * Jersy rest client to connect to hostip.info and get location for an specified ip
 *
 * @author Hossein
 */
public class HostIpJerssyClient {

  public HostIpJerssyClient() {
  }

  public static GeoLocation mapToLocation(String ip) throws IOException {

    Client client = ClientBuilder.newBuilder().build();
    //.register(JacksonFeature.class)
    WebTarget target = client.target(getBaseURI(ip)).path("get_json.php").queryParam("ip", ip);
    Response response = target.request().get();
    ObjectMapper mapper = new ObjectMapper();
    String json = response.readEntity(String.class);
    return mapper.readValue(json, GeoLocation.class);
  }

  private static URI getBaseURI(String ip) {
    return UriBuilder.fromUri("http://api.hostip.info").build();

  }

}
