package com.example.gestinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class SalesforceAuthenticator {

    private static final String AUTH_URL = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/oauth2/authorize";
    private static final String TOKEN_URL = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/oauth2/token";
    private static final String CLIENT_ID = "3MVG9Iuu5rVmxszzMusfI_nxFFXvPdfFv0rbWhqg3GCyi4mOQX3tM5QiSz4_3UcRefw_SghMp_C5JH7LCgl9W";
    private static final String CLIENT_SECRET = "35C9C53EDD779118B04F053054F7A2E5CD2DCBBFCA5DEDBE758F00DD982E64E0";
    private static final String CALLBACK_URL = "https://oauth.pstmn.io/v1/callback";
    private static final String CODE_CHALLENGE_METHOD = "S256";
    private static final String SCOPE = "api full chatter_api refresh_token offline_access wave_api eclair_api user_registration_api sfap_api interaction_api cdp_api";

    public static void main(String[] args) throws IOException {
        String authorizationCode = getAuthorizationCode();
        String accessToken = getAccessToken(authorizationCode);
        System.out.println("Access Token: " + accessToken);
    }

    private static String getAuthorizationCode() throws IOException {
        // Aquí puedes implementar el proceso para obtener el código de autorización manualmente
        // En este ejemplo, se devuelve un código de autorización ficticio
        return "YOUR_AUTHORIZATION_CODE";
    }

    private static String getAccessToken(String authorizationCode) throws IOException {
        String credentials = CLIENT_ID + ":" + CLIENT_SECRET;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        URL url = new URL(TOKEN_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
        connection.setDoOutput(true);

        String requestBody = "grant_type=authorization_code"
                + "&code=" + authorizationCode
                + "&redirect_uri=" + CALLBACK_URL
                + "&code_verifier=" + generateCodeVerifier()
                + "&scope=" + SCOPE;

        connection.getOutputStream().write(requestBody.getBytes());

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Parse the JSON response to extract the access token
        // Assuming the response is in the format {"access_token":"YOUR_ACCESS_TOKEN","token_type":"Bearer","expires_in":3600,"refresh_token":"YOUR_REFRESH_TOKEN","scope":"api"}
        String jsonResponse = response.toString();
        String accessToken = jsonResponse.substring(jsonResponse.indexOf(":\"") + 2, jsonResponse.indexOf("\",\"token_type\""));
        return accessToken;
    }

    private static String generateCodeVerifier() {
        // Implementa la generación del código verificador aquí (cadena alfanumérica aleatoria de 43-128 caracteres)
        // Por simplicidad, puedes usar una cadena estática o implementar un algoritmo de generación aleatoria
        return "YOUR_CODE_VERIFIER";
    }
}
