package com.example.gestinfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;

public class Main {

    private static final String INSTANCE_URL = "https://playful-shark-5wyeir-dev-ed.my.salesforce.com";
    private static final String ACCESS_TOKEN = "6Cel800DQy0000062OKf888Qy0000001NdhAwKaXTvT0R0JaHpVVl4k3Cki9150XQH6ViC9fU3b2BylpT2F0DpWTx70z6PwQP8T0n7tyPFQ";

    public static void main(String[] args) throws IOException {

        String query = INSTANCE_URL + "/services/oauth2/authorize?response_type=code&client_id=3MVG9k02hQhyUgQBpXbI1aWPefGbWONGzuXxJNNBtFgPyTTfU8AQd43YUfoi2wdrQQWKtcrg2nAop8u4xEYuy&redirect_uri=http://localhost:8080/callback&scope=api";

        HttpGet httpGet = new HttpGet(query);
        httpGet.setHeader("Authorization", "Bearer " + ACCESS_TOKEN);

        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse response = httpClient.execute(httpGet);

        HttpEntity entity = response.getEntity();
        if (entity != null) {
            
            String responseString = EntityUtils.toString(entity);
            System.out.println("Response: " + responseString);
        }
    }
}
