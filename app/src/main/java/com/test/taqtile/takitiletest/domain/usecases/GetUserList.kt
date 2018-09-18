//package com.test.taqtile.takitiletest.domain.usecases
//
//import com.test.taqtile.takitiletest.domain.UserRepository
//import com.test.taqtile.takitiletest.domain.common.Transformer
//import com.test.taqtile.takitiletest.domain.entities.UserEntity
//import io.reactivex.Observable
//import org.json.JSONObject
//
//
//class GetUserList (transformer: Transformer<List<UserEntity>>,
//                   private val userRepository: UserRepository) : UseCase<List<UserEntity>>(transformer) {
//
//  fun getList(pagination: JSONObject): Observable<List<UserEntity>> {
//    val data = HashMap<String, JSONObject>()
//    data["param"] = pagination
//
//    return observable(data)
//  }
//
//  override fun createObservable(data: Map<String, Any>?): Observable<List<UserEntity>> {
//    val pagination = data?.get("param")
//
//    pagination?.let {
//      return userRepository.getList(it as JSONObject)
//    } ?: return Observable.just(emptyList())
//  }
//}