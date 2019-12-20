package com.anatolf.tvchat.net;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface VkService {

    @GET("users/{user}/repos")
    Call<String> listRepos(@Path("user") String user);


    @GET("access_token")
    Call<String> accsessToken(@Query("client_id") String clientId,
                              @Query("client_secret") String clientSecret,
                              @Query("v") String version,
                              @Query("grant_type") String grantType);

}


