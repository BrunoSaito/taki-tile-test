package com.test.taqtile.takitiletest.data.account

import com.test.taqtile.takitiletest.core.config.Constants
import com.test.taqtile.takitiletest.models.*
import io.reactivex.Observable
import org.json.JSONObject
import retrofit2.adapter.rxjava2.Result
import retrofit2.http.*

interface AccountServices {
  @POST(Constants.USER_LIST)
  fun create(@Body user: UserCreate?) : Observable<Result<UserResponse>>

  @GET(Constants.USER_LIST)
  fun list(@Query("pagination") pagination: JSONObject?) : Observable<Result<ListUserResponse>>

  @GET(Constants.USER_DETAILS)
  fun getDetails(@Path("id") id: String?) : Observable<Result<UserResponse>>

  @POST(Constants.USER_LOGIN)
  fun login(@Body loginCredentials: LoginCredentials?) : Observable<Result<LoginResponse>>

  @PUT(Constants.USER_DETAILS)
  fun update(@Path("id") id: String?, @Body editUserData: EditUserData?) : Observable<Result<UserResponse>>

  @DELETE(Constants.USER_DETAILS)
  fun delete(@Path("id") id: String?) : Observable<Result<UserResponse>>
}