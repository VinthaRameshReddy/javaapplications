package com.medgo.virtualid.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import com.medgo.virtualid.domain.request.VirtualIdAuthJson;
import com.medgo.virtualid.endpoint.VirtualIdEndPoint;
import com.medgo.virtualid.utility.DateTypeDeserializer;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class VirtualIdRetrofitConfig {

    @Value("${virtual-id.base-url}")
    private String baseUrl;

    @Value("${virtual-id.username}")
    private String username;

    @Value("${virtual-id.password}")
    private String password;

    private String token = "";

    private int attemptCount = 0;

    @Bean("virtualIdOkHttpClient")
    public OkHttpClient okHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };
            // Install the all-trusting trust manager
            final SSLContext sslContext = createSslContext();
            sslContext.init(null, trustAllCerts, new SecureRandom());
            // Create a ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);

            builder.callTimeout(60, TimeUnit.SECONDS);
            builder.connectTimeout(60, TimeUnit.SECONDS);
            builder.readTimeout(60, TimeUnit.SECONDS);
            builder.writeTimeout(60, TimeUnit.SECONDS);

            builder.authenticator((route, response) -> {
                log.info("Authenticator statusCode {}", response.code());
                if (response.code() == 401 || response.code() == 403) {

                    log.info("Authenticator renewing token attempt #{}", attemptCount++);

                    // Create a temporary Retrofit instance for authentication (no token needed)
                    Retrofit authRetrofit = new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .client(new OkHttpClient.Builder()
                                    .connectTimeout(30, TimeUnit.SECONDS)
                                    .readTimeout(30, TimeUnit.SECONDS)
                                    .build())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    VirtualIdEndPoint authEndpoint = authRetrofit.create(VirtualIdEndPoint.class);
                    var respond = authEndpoint
                            .authenticate(new VirtualIdAuthJson(username, password))
                            .execute();

                    log.info("Authenticator Auth statusCode {}", respond.code());

                    if (respond.isSuccessful() && respond.body() != null) {
                        token = respond.body().getAccessToken();
                        attemptCount = 0;
                    }

                    return response.request()
                            .newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build();
                }

                return null;
            });

            builder.addInterceptor(chain -> {
                Request.Builder ongoing = chain.request().newBuilder();
                ongoing.addHeader("Accept", "application/json");

                if (null == chain.request().header("Authorization") && !token.isBlank()) {
                    ongoing.addHeader("Authorization", "Bearer " + token);
                }

                return chain.proceed(ongoing.build());
            });

            return builder.build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Extract SSLContext creation so tests can override/fail it to exercise the catch block
    protected SSLContext createSslContext() throws Exception {
        return SSLContext.getInstance("SSL");
    }

    @Bean("virtualIdRetrofit")
    public Retrofit retrofit(@Qualifier("virtualIdOkHttpClient") OkHttpClient client) {
        //Use date format used in Backend Web Services
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new DateTypeDeserializer());

        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(baseUrl)
                .client(client)
                .build();
    }

    @Bean("virtualIdGson")
    public Gson gson() {
        return new GsonBuilder().setLenient().create();
    }

    @Bean("virtualIdEndpoint")
    public VirtualIdEndPoint endpoint(@Qualifier("virtualIdRetrofit") Retrofit retrofit) {
        return retrofit.create(VirtualIdEndPoint.class);
    }
}
