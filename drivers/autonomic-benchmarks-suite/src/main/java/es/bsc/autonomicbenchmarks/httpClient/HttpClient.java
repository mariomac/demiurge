package es.bsc.autonomicbenchmarks.httpClient;

import com.squareup.okhttp.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class HttpClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();

    public HttpClient() { }

    public String get(String url) throws IOException {

        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();

        return response.body().string();
    }

    public String getWithTimeout(String url, Integer timeout) throws IOException {
        OkHttpClient clientTimeout = new OkHttpClient();

        clientTimeout.setConnectTimeout(timeout, TimeUnit.SECONDS); // connect timeout
        clientTimeout.setReadTimeout(timeout, TimeUnit.SECONDS);    // socket timeoout

        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = clientTimeout.newCall(request).execute();

        return response.body().string();
    }
    
    public String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String delete(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }




}
