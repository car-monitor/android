package android.vic;



import android.vic.RetrofitEntity.DepartmentRetro;
import android.vic.RetrofitEntity.LoginRetro;
import android.vic.RetrofitEntity.UnitRetro;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by wujy on 2017/6/26.
 * 网络请求retrofit接口
 */

public interface RetrofitInterface {

    @FormUrlEncoded
    @POST("login")
    Call<LoginRetro> login(@Field("username") String username, @Field("password") String password);

    @GET("getunit")
    Call<UnitRetro> getUnit(@Query("id")int id);

    @GET("getdepartment")
    Call<DepartmentRetro> getDepartment(@Query("id")int id);
}
