package com.astronaut_wannabe;

import com.astronaut_wannabe.model.PocketResponse;
import com.astronaut_wannabe.model.PocketSendAction;
import com.astronaut_wannabe.model.PocketSendResponse;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.POST;

public class PocketClient {
    public static final String API_URL = "https://getpocket.com";
    public static final String CONSUMER_KEY = "***REMOVED***";

    public static final Interceptor sRequestInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            final Request original_request = chain.request();
            final Request new_request = original_request.newBuilder()
                    .addHeader("X-Accept", "application/json")
                    .build();
            return chain.proceed(new_request);
        }
    };

    public static class TokenRequest {
        public String consumer_key;
        public String redirect_uri;
        public String state;
        public String code;
    }

    public static class TokenResponse {
        public String code;
        public String access_token;
        public String username;
    }

    public static class GetRequest {
        public String access_token;
        public String consumer_key;
        public String count;
    }

    public static class PostRequest {
        public String access_token;
        public String consumer_key;
        public PocketSendAction[] actions;
    }

    public interface Pocket {
        @POST("/v3/oauth/request")
        Call<TokenResponse> obtainRequestToken(@Body TokenRequest request);

        @POST("/v3/oauth/authorize")
        Call<TokenResponse> authorizeToken(@Body TokenRequest request);

        @POST("/v3/get")
        Call<PocketResponse> get(@Body GetRequest request);

        @POST("/v3/send")
        Call<PocketSendResponse> send(@Body PostRequest req);
    }
}
