package com.medgo.virtualid.endpoint;

import com.medgo.virtualid.domain.request.VirtualIdAuthJson;
import com.medgo.virtualid.domain.response.VirtualIdAuthDto;
import com.medgo.virtualid.domain.response.VirtualIdResponseDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface VirtualIdEndPoint {

    @POST("/virtual-id/api/authenticate")
    Call<VirtualIdAuthDto> authenticate(@Body VirtualIdAuthJson json);

    @GET("/virtual-id/api/generate-link/{memberCode}")
    Call<VirtualIdResponseDto> getGeneratedLink(
            @Path("memberCode") String memberCode,
            @Query("skipMedgoValidation") boolean skipMedgoValidation
    );

}
