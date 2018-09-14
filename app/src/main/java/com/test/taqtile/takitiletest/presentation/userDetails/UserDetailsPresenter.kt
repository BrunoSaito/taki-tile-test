package com.test.taqtile.takitiletest.presentation.userDetails

import com.test.taqtile.takitiletest.domain.entities.UserDetailsEntity
import com.test.taqtile.takitiletest.domain.usecases.SingleUseCase
import com.test.taqtile.takitiletest.presentation.mapper.UserDetailsEntityViewMapper
import io.reactivex.observers.DisposableSingleObserver


class UserDetailsPresenter(val userDetailsView: UserDetailsContract.View,
                           val getUserDetailsUseCase: SingleUseCase<UserDetailsEntity, Void>,
                           val mapper: UserDetailsEntityViewMapper,
                           val userId: String) : UserDetailsContract.Presenter {

  init {
    userDetailsView.setPresenter(this)
  }

  override fun start() {
    userDetailsView.showProgressBar()
    getDetails(userId)
  }

  override fun stop() {
    getUserDetailsUseCase.dispose()
  }

  override fun getDetails(userId: String) {
    getUserDetailsUseCase.execute(UserSubscriber())
  }

  internal fun handleGetDetailsSuccess(userDetails: UserDetailsEntity?) {
    userDetailsView.hideError()

    userDetails?.data?.let {
      userDetailsView.hideProgressBar()
      userDetailsView.setUserName(it.name)
      userDetailsView.setUserEmail(it.email)
      userDetailsView.setUserRole(it.role)
    } ?: userDetailsView.showError("Algo deu errado :(")
  }

  inner class UserSubscriber: DisposableSingleObserver<UserDetailsEntity>() {

    override fun onSuccess(successResponse: UserDetailsEntity?) {
      handleGetDetailsSuccess(successResponse)
    }

    override fun onError(errorResponse: Throwable?) {
      userDetailsView.showError(errorResponse?.message.toString())
      userDetailsView.hideProgressBar()
    }

  }
}