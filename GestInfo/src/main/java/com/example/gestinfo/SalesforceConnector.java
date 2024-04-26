package com.example.gestinfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sforce.ws.ConnectorConfig;
import java.net.URLEncoder;
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

public class SalesforceConnector {

    public static void main(String[] args) {
        String instanceUrl = "https://solucionamideuda--devmiguel.my.salesforce.com";
        String query = "SELECT Username FROM User";
        String accessToken = "RMSpzpamc2jafuRRnC1XSIxk";

        try {
            // Codificar la consulta para evitar caracteres no permitidos en la URL
            String encodedQuery = URLEncoder.encode(query, "UTF-8");

            // Construir la URL de la consulta
            String apiUrl = instanceUrl + "/services/data/v60.0/query?q=" + encodedQuery;

            // Crear un objeto URI para manejar la URL
            URI uri = new URI(apiUrl);

            // Configurar la solicitud HTTP GET
            HttpGet httpGet = new HttpGet(uri);
            httpGet.addHeader("Authorization", "Bearer " + accessToken);
            httpGet.addHeader("Content-Type", "application/json");

            // Crear un cliente HTTP y ejecutar la solicitud
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(httpGet);

            // Leer la respuesta y procesarla
            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
            StringBuilder jsonResponse = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonResponse.append(line);
            }

            // Convertir la respuesta JSON a objetos Java
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse.toString());

            // Procesar los resultados de la consulta
            if (jsonNode.has("records")) {
                for (JsonNode record : jsonNode.get("records")) {
                    String username = record.get("Username").asText();
                    System.out.println("Username: " + username);
                }
            } else {
                System.out.println("No se encontraron registros.");
            }

            // Cerrar el cliente HTTP
            httpClient.close();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
