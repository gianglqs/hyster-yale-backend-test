package com.hysteryale.service;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.hysteryale.model.competitor.ScrapedProduct;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WebScrapingService {
    private static final String API_KEY = "7e28fe836c6e4584beab41f0f4d0c62a";

    public List<ScrapedProduct> scrapeData(String url) {
        Map<String, Object> parameters =
                ImmutableMap.of(
                        "url", url,
                        "httpResponseBody", true,
                        "productList", true,
                        "productListOptions", ImmutableMap.of("extractFrom","httpResponseBody")
                );
        String requestBody = new Gson().toJson(parameters);

        HttpPost request = new HttpPost("https://api.zyte.com/v1/extract");
        request.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON);
        request.setHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate");
        request.setHeader(HttpHeaders.AUTHORIZATION, buildAuthHeader());
        request.setEntity(new StringEntity(requestBody));

        List<ScrapedProduct> productArrayList = new ArrayList<>();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = client.execute(request)) {
                HttpEntity entity = response.getEntity();
                String apiResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                JsonObject jsonObject = JsonParser.parseString(apiResponse).getAsJsonObject();
                JsonArray productList = jsonObject.get("productList").getAsJsonObject().get("products").getAsJsonArray();
                int i = 0;

                for(JsonElement product : productList) {
                    i++;
                    String productName = product.getAsJsonObject().get("name") != null
                            ? trimSpecialCharacters(product.getAsJsonObject().get("name").toString())
                            : "";
                    double price = product.getAsJsonObject().get("price") !=null
                            ? Double.parseDouble(trimSpecialCharacters(product.getAsJsonObject().get("price").toString()))
                            : 0.0;
                    String currency = product.getAsJsonObject().get("currency") != null
                            ? trimSpecialCharacters(product.getAsJsonObject().get("currency").toString())
                            : "";
                    String image = product.getAsJsonObject().get("mainImage") != null
                            ? trimSpecialCharacters(product.getAsJsonObject().get("mainImage").getAsJsonObject().get("url").toString())
                            : "";

                    ScrapedProduct sp = new ScrapedProduct(i, productName, image, currency, price);
                    productArrayList.add(sp);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error on scraping data");
        }
        return productArrayList;
    }

    /**
     * Trim the character " out of String value
     */
    private String trimSpecialCharacters(String originalString) {
        return originalString.replaceAll("\"", "");
    }

    private static String buildAuthHeader() {
        String auth = API_KEY + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        return "Basic " + encodedAuth;
    }
}
