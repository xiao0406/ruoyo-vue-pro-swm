package cn.iocoder.yudao.module.iot.config;

import okhttp3.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class OkHttpClientManager {

    private final OkHttpClient client;

    public OkHttpClientManager() {
        client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(
                        150, // 最大空闲连接数
                        5, TimeUnit.MINUTES // 空闲保持时间
                ))
                .retryOnConnectionFailure(true)
                .build();
    }

    public OkHttpClient getClient() {
        return client;
    }

    public String post(String url, String authorization, String body) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", authorization)
                .post(RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), body))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("HTTP请求失败: " + response.code() + " " + response.message());
            }
            return response.body() != null ? response.body().string() : null;
        }
    }
}
