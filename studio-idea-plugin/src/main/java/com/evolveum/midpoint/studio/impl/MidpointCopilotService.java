package com.evolveum.midpoint.studio.impl;

import okhttp3.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dominik.
 *
 */
public class MidpointCopilotService {

    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)   // Time to connect
            .readTimeout(30, TimeUnit.SECONDS)      // Time to wait for server response
            .writeTimeout(30, TimeUnit.SECONDS)     // Time to send request body
            .build();

    String url = "http://127.0.0.1:5000";

    public String generate(String prompt, String context) {
        String jsonBody = "{ \"prompt\": \"" + prompt + "\"}";
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url + "/" + context)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                JsonReader reader = Json.createReader(new StringReader(response.body().string()));
                JsonObject jsonObject = reader.readObject();
                return jsonObject.getString("response");
            } else {
                return Integer.toString(response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
