package com.test.taqtile.takitiletest.DataModels

import com.google.gson.annotations.SerializedName


data class EditUserData (
  @SerializedName("name") val name: String?,
  @SerializedName("email") val email: String?,
  @SerializedName("role") val role: String?
)