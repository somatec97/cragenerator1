package fr.nazza.cragenerator1.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ApiGouvRestClient {

  public RestClient apiGouvRestClient(RestClient.Builder builder) {
    builder.baseUrl("https://calendrier.api.gouv.fr");
    return builder.build();
  }
}
