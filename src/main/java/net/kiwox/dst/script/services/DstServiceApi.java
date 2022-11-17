package net.kiwox.dst.script.services;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface DstServiceApi {

    @Headers("Content-Type: application/json")
    @GET(".")
    public Call<String> requestGetVerificationCodePhone(
            @Query("do") String doParam,
            @Query("rType") String rTypeParam,
            @Query("MSISDN") String msisdnParam,
            @Query("token") String tokenParam
    );
}
