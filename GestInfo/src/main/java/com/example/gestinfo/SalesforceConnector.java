package com.example.gestinfo;

import com.force.api.ApiConfig;
import com.force.api.Auth;
import com.force.api.QueryResult;
import com.force.api.RequestException;
import com.force.api.RestConnection;

public class SalesforceConnector {
    public static void main(String[] args) {
        String username = "tu_usuario_de_salesforce";
        String password = "tu_contraseña_de_salesforce";
        String securityToken = "tu_token_de_seguridad";

        String clientId = "tu_client_id";
        String clientSecret = "tu_client_secret";

        try {
            // Autenticarse con Salesforce
            Auth auth = new Auth();
            String accessToken = auth.getAccessToken(username, password, clientId, clientSecret, securityToken);
            ApiConfig config = new ApiConfig().setAccessToken(accessToken);

            // Realizar una consulta SOQL para obtener datos
            RestConnection connection = new RestConnection(config);
            QueryResult<Object> result = connection.query("SELECT Id, Name FROM Account LIMIT 10");

            // Procesar los resultados
            if (result.isSuccess()) {
                for (Object record : result.getRecords()) {
                    System.out.println(record);
                }
            } else {
                System.err.println("Error al ejecutar la consulta: " + result.getStatusCode() + " " + result.getError());
            }
        } catch (RequestException e) {
            System.err.println("Error al conectarse a Salesforce: " + e.getMessage());
        }
    }
}
