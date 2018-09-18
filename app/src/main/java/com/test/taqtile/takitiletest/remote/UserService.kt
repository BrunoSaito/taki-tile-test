package com.test.taqtile.takitiletest.remote

import com.test.taqtile.takitiletest.remote.model.UserDetailsModel
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path


interface UserService {
  @GET("/users/{id}")
  fun getDetails(@Path("id") id: String?) : Single<UserDetailsModel>
}