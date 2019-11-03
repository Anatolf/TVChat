package com.example.hohlosra4app;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OdnoklassnikiService {


    @GET("fb.do")
    Call<Object> fbDo(@Query("application_key") String applicationKey,
                           @Query("fields") String fields,
                           @Query("format") String format,
                           @Query("method") String method,
                           @Query("uids") String uids,
                           @Query("sig") String sig,
                           @Query("access_token") String accessToken);
}
