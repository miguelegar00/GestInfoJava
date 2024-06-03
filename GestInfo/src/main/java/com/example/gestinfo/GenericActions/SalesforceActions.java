package com.example.gestinfo.GenericActions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.example.gestinfo.AccountActions.ShowAccounts;
import com.example.gestinfo.AccountActions.ShowAccounts.Account;
import com.example.gestinfo.UsersActions.ShowUsers;
import com.example.gestinfo.UsersActions.ShowUsers.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

/**
 *
 * @author Miguel
 */
public class SalesforceActions {

    private static SalesforceTokenManager tokenManager = new SalesforceTokenManager();

    public static void executePatchRequest(String url, String data) throws IOException {
        
        @SuppressWarnings("static-access")
        String bearerToken = tokenManager.getNewAccessToken();

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPatch httpPatch = new HttpPatch(url);
        httpPatch.addHeader("Content-Type", "application/json");
        httpPatch.addHeader("Authorization", "Bearer " + bearerToken);

        StringEntity entity = new StringEntity(data);
        httpPatch.setEntity(entity);

        CloseableHttpResponse response = httpClient.execute(httpPatch);
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 200 || statusCode == 204) {
        } else {
            throw new IOException("Error al actualizar el usuario. Código de respuesta HTTP: " + statusCode);
        }
    }

    public static String executeQuery(String url, String bearerToken) throws IOException {
        URL queryUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) queryUrl.openConnection();
        connection.setRequestMethod("GET");

        connection.setRequestProperty("Authorization", "Bearer " + bearerToken);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                return response.toString();
            }
        } else {
            throw new IOException("HTTP error code: " + responseCode);
        }
    }

    public static void executeAndDisplayResultsUsers(Stage primaryStage) {
        try {
            
            String queryUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/query/?q=SELECT+Id,FirstName,LastName,UserRoleId,IsActive+FROM+User+WHERE+UserRoleId+!=+null+AND+ProfileId+!=+null+AND+UserRoleId+!=+null";
    
            @SuppressWarnings("static-access")
            String bearerToken = tokenManager.getNewAccessToken();
    
            String response = executeQuery(queryUrl, bearerToken);
    
            ObservableList<User> userList = FXCollections.observableArrayList();
    
            Pattern pattern = Pattern.compile("\"Id\"\\s*:\\s*\"(\\w+)\",\"FirstName\"\\s*:\\s*\"(.*?)\",\"LastName\"\\s*:\\s*\"(.*?)\",\"UserRoleId\"\\s*:\\s*\"(.*?)\",\"IsActive\"\\s*:\\s*(true|false)");
            Matcher matcher = pattern.matcher(response);
            while (matcher.find()) {
                String id = matcher.group(1);
                String firstName = decodeString(matcher.group(2));
                String lastName = decodeString(matcher.group(3));
                String userRoleId = decodeString(matcher.group(4));
                Boolean isActive = Boolean.parseBoolean(matcher.group(5));
                userList.add(new User(id, firstName, lastName, userRoleId, isActive));
            }
    
            ShowUsers.userTable.setItems(userList);
            ShowUsers.originalUserList = userList;
        } catch (IOException e){
            ShowMessages.mostrarMensajeError("No se puede conectar a Salesforce.");
        }
    }

    public static void executeAndDisplayResultsAccounts() {
        try {
            // URL de la consulta
            String queryUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/query/?q=SELECT+Id,FirstName,LastName,Cliente_de__c,Phone+FROM+Account+WHERE+Id+!=+null";
    
            // Token de portador
            @SuppressWarnings("static-access")
            String bearerToken = tokenManager.getNewAccessToken();
    
            // Realizar la consulta
            String response = executeQuery(queryUrl, bearerToken);
    
            // Procesar la respuesta y mostrar los IDs y los Names en la tabla
            ObservableList<Account> accountList = FXCollections.observableArrayList();
    
            Pattern pattern = Pattern.compile("\"Id\"\\s*:\\s*\"(\\w+)\",\"FirstName\"\\s*:\\s*\"(.*?)\",\"LastName\"\\s*:\\s*\"(.*?)\",\"Cliente_de__c\"\\s*:\\s*\"(.*?)\",\"Phone\"\\s*:\\s*\"(.*?)\"");
            Matcher matcher = pattern.matcher(response);
            while (matcher.find()) {
                String id = matcher.group(1);
                String name = SalesforceActions.decodeString(matcher.group(2));
                String lastName = SalesforceActions.decodeString(matcher.group(3));
                String clienteDe = SalesforceActions.decodeString(matcher.group(4));
                String phone = SalesforceActions.decodeString(matcher.group(5));
                accountList.add(new Account(id, name, lastName, clienteDe, phone));
            }
    
            ShowAccounts.accountTable.setItems(accountList);
        } catch (IOException e) {
            ShowMessages.mostrarMensajeError("No se puede conectar a Salesforce.");
        }
    }

    public static void executePostRequest(String url, String data) throws IOException {
        @SuppressWarnings("static-access")
        String bearerToken = tokenManager.getNewAccessToken();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("Authorization", "Bearer " + bearerToken);

        // Configurar el cuerpo de la solicitud
        StringEntity entity = new StringEntity(data);
        httpPost.setEntity(entity);

        // Ejecutar la solicitud y obtener la respuesta
        HttpResponse response = httpClient.execute(httpPost);
        int statusCode = response.getStatusLine().getStatusCode();

        // Verificar el código de estado de la respuesta
        if (statusCode == 200 || statusCode == 201) {
            // La operación POST se realizó correctamente
            // Aquí puedes manejar la respuesta si es necesario
        } else {
            throw new IOException("Error al crear la cuenta. Código de respuesta HTTP: " + statusCode);
        }
    }

    public static String decodeString(String encodedString) {
        try {
            return java.net.URLDecoder.decode(encodedString, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return encodedString;
        }
    }

    public static void executePostRequest(String url, String data, String accessToken) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Authorization", "Bearer " + accessToken);

            StringEntity entity = new StringEntity(data);
            httpPost.setEntity(entity);

            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200 && statusCode != 201) {
                throw new IOException("Error al crear la cuenta. Código de respuesta HTTP: " + statusCode);
            }
        }
    }
}
