package com.example.gestinfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class SalesforceConnector {

    public static void main(String[] args) {
        String instanceUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com";
        String query = "SELECT Username FROM Users";
        String accessToken = "RMSpzpamc2jafuRRnC1XSIxk";

        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String apiUrl = instanceUrl + "/services/data/v60.0/query?q=" + encodedQuery;
            URI uri = new URI(apiUrl);

            HttpGet httpGet = new HttpGet(uri);
            httpGet.addHeader("Authorization", "Bearer " + accessToken);
            httpGet.addHeader("Content-Type", "application/json");

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity entity = response.getEntity();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()))) {
                    StringBuilder jsonResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonResponse.append(line);
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(jsonResponse.toString());

                    if (jsonNode.has("records")) {
                        for (JsonNode record : jsonNode.get("records")) {
                            String username = record.get("Username").asText();
                            System.out.println("Username: " + username);
                        }
                    } else {
                        System.out.println("No se encontraron registros.");
                    }
                } catch (JsonProcessingException e) {
                    System.err.println("Error parsing JSON response: " + e.getMessage());
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            System.err.println("Error executing Salesforce query: " + e.getMessage());
        }
    }
}
