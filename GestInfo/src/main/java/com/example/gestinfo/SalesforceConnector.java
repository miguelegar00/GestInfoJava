package com.example.gestinfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SalesforceConnector {

    public static void main(String[] args) {
        String apiUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com";
        String query = "SELECT Id, Name FROM Users LIMIT 10";
        String accessToken = "pqJi2BuYtqw62wELmZaTwhUH";

        try {
            URL url = new URL(apiUrl + "/services/data/v60.0/query?q=" + query);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println("Response from Salesforce API:");
                System.out.println(response.toString());
            } else {
                System.out.println("Failed to fetch data from Salesforce API. Response code: " + responseCode);
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
