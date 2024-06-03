package com.example.gestinfo.GenericActions;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Miguel
 */
public class SalesforceTokenManager {
    
    private static final String TOKEN_URL = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/oauth2/token";
    private static final String CLIENT_ID = "3MVG9Iuu5rVmxszzMusfI_nxFFXvPdfFv0rbWhqg3GCyi4mOQX3tM5QiSz4_3UcRefw_SghMp_C5JH7LCgl9W";
    private static final String CLIENT_SECRET = "35C9C53EDD779118B04F053054F7A2E5CD2DCBBFCA5DEDBE758F00DD982E64E0";
    private static final String REFRESH_TOKEN = "5Aep8615IVbq48kZtUjUHzuqt.i5oKX4smXnOxiqB8FHfoLkKnFflfKVT9Ga3hV8DNgpuD0ku1t1JqZ9C9UoolI";

    public static String getNewAccessToken() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(TOKEN_URL);
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");

            String requestBody = String.format(
                "grant_type=refresh_token&client_id=%s&client_secret=%s&refresh_token=%s",
                CLIENT_ID, CLIENT_SECRET, REFRESH_TOKEN
            );

            httpPost.setEntity(new StringEntity(requestBody));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                return jsonNode.get("access_token").asText();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        SalesforceTokenManager manager = new SalesforceTokenManager();
        @SuppressWarnings("static-access")
        String newAccessToken = manager.getNewAccessToken();
        if (newAccessToken != null) {
            System.out.println("Nuevo token de acceso: " + newAccessToken);
        } else {
            System.out.println("Error al obtener el nuevo token de acceso.");
        }
    }
}