package com.example.thunderbolt_108.shipperapp.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface IGeoCoordinates {
  //  @GET("maps/api/geocode/json")
  //  Call<String> getGeoCode(@Query("address") String address);

    @GET("maps/api/geocode/json")
    Call<String> getGeoCode(@Query("address") String address,@Query("key") String key);

    @GET
    Call<String> getAddressName(@Url String url);

    @GET
    Call<String> getLocationFromAddress(@Url String url);

    @GET("maps/api/directions/json")
    Call<String> getDirections(@Query("origin") String origin,@Query("destination") String destination,@Query("key") String key);
}
