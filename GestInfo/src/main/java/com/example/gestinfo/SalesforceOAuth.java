package com.example.gestinfo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class SalesforceOAuth {

    public static void main(String[] args) {
        // URL de la consulta
        String queryUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/query/?q=SELECT+Id+FROM+Account";

        // Token de portador
        String bearerToken = "00DUB000001QzdZ!AQEAQAZJWNZ6VM47HfJ_pI8gaFnskJRnzSHBqEgQdzyONN8BKxFR2T1q.1bjvTd0iamnpMU2R7vfJXO14judrXkMsoeC8GAj";

        // Realizar la consulta
        try {
            String response = executeQuery(queryUrl, bearerToken);
            System.out.println("Response from Salesforce: " + response);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static String executeQuery(String url, String bearerToken) throws IOException, URISyntaxException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(new URI(url));

        // Agregar el token de portador al encabezado de autorización
        httpGet.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);

        // Realizar la solicitud GET
        HttpResponse response = httpClient.execute(httpGet);

        // Leer la respuesta y convertir la entidad a una cadena
        String responseBody = EntityUtils.toString(response.getEntity());

        // Cerrar el cliente HTTP
        httpClient.close();

        return responseBody;
    }
}
