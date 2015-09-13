package com.astronaut_wannabe;

import com.astronaut_wannabe.model.PocketResponse;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.http.Body;
import retrofit.http.POST;

public class PocketClient {
    public static final String API_URL = "https://getpocket.com/v3";
    public static final String CONSUMER_KEY = "***REMOVED***";

    public static RequestInterceptor sRequestInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            request.addHeader("Content-Type", "application/json; charset=UTF-8");
            request.addHeader("X-Accept", "application/json");
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

    public interface Pocket {
        @POST("/oauth/request")
        public void obtainRequestToken(@Body TokenRequest request, Callback<TokenResponse> cb);

        @POST("/oauth/authorize")
        public void authorizeToken(@Body TokenRequest request, Callback<TokenResponse> cb);

        @POST("/get")
        public void get(@Body GetRequest request, Callback<PocketResponse> cb);

        @POST("/send")
        public void deleteMultipleItems();
    }
}
