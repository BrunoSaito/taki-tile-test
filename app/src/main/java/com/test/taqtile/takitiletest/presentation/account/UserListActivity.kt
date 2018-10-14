package com.test.taqtile.takitiletest.presentation.account

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.test.taqtile.takitiletest.R
import com.test.taqtile.takitiletest.domain.ListUsersUseCase
import com.test.taqtile.takitiletest.models.ListUserResponse
import com.test.taqtile.takitiletest.models.User
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_user_list.*
import org.json.JSONObject
import widgets.lists.user.UsersAdapter
import javax.inject.Inject
import androidx.recyclerview.widget.RecyclerView
import com.test.taqtile.takitiletest.core.config.Constants
import com.test.taqtile.takitiletest.domain.DeleteUserUseCase
import widgets.components.CustomSnackBarBuilder

class UserListActivity : AppCompatActivity(), UsersAdapter.Listener {

  @Inject
  lateinit var listUsersUseCase: ListUsersUseCase

  @Inject
  lateinit var deleteUserUseCase: DeleteUserUseCase

  private var disposables: MutableList<Disposable> = mutableListOf()
  private var adapter: UsersAdapter? = null

  private var users = ArrayList<User>()
  private var page = 0
  private var window = 10
  private var totalPages = 0
  private var total = 0

  private val CALLBACK_FROM_USER_FORM = 0
  private val CALLBACK_FROM_USER_DETAILS = 1
  private val jsonParams: JSONObject = JSONObject()

  // region listeners
  override fun onUserSelected(id: String?) {
//    val intent = Intent(this@UserListActivity, UserDetailsActivity::class.java)
//    intent.putExtra(Constants.USER_ID, id)
//
//    startActivity(intent)
//    finish()

    val intent = Intent(this@UserListActivity, UserDetailsActivity::class.java)
    intent.putExtra(Constants.USER_ID, id)

    startActivityForResult(intent, CALLBACK_FROM_USER_DETAILS)
  }

  override fun onEditUserClicked(id: String?) {
    val intent = Intent(this@UserListActivity, UserFormActivity::class.java)
    intent.putExtra(Constants.USER_ID, id)

    startActivity(intent)
    finish()
  }

  override fun onDeleteUserClicked(id: String?, name: String?) {
    val builder = AlertDialog.Builder(this@UserListActivity)
    builder.setMessage(getString(R.string.delete_user_alert_message, name))
        builder.setPositiveButton(R.string.delete_user_alert_positive_button) { _, _ ->
          val localId = id
          if (localId != null) delete(id)
        }
        builder.setNegativeButton(R.string.delete_user_alert_negative_button) { dialog, _ ->
          dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
  }
  // end region

  // region lifecycle
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_user_list)
    AndroidInjection.inject(this)

    jsonParams.put("page", page.toString())
    jsonParams.put("window", window.toString())

    progressBarListUsers.visibility = ProgressBar.VISIBLE
    list()

    setupActionBar()
    setupFabCreateButton()

    searchListView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(query: String?): Boolean {
        jsonParams.put("page", "0")
        jsonParams.put("window", totalPages.toString())

//        getUsersListRequest(jsonParams, query)

        return true
      }

      override fun onQueryTextChange(newText: String?): Boolean {
        jsonParams.put("page", "0")
        jsonParams.put("window", totalPages.toString())

//        getUsersListRequest(jsonParams, newText)

        return true
      }
    })
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == CALLBACK_FROM_USER_FORM && resultCode == Activity.RESULT_OK) {
      CustomSnackBarBuilder(this@UserListActivity)
              .withText(R.string.create_user_success)
              .show()
      page = 0
      jsonParams.put("page", page.toString())
      list(reset = true)
    }
    if (requestCode == CALLBACK_FROM_USER_DETAILS && resultCode == Activity.RESULT_OK) {
      CustomSnackBarBuilder(this@UserListActivity)
              .withText(R.string.delete_user_success)
              .show()
      page = 0
      jsonParams.put("page", page.toString())
      list(reset = true)
    }
  }
  // end region

  // region setup
  private fun setupActionBar() {
    val actionBar = supportActionBar
    actionBar?.title = getString(R.string.user_list_title)
  }

  private fun setupRecycler() {
    val manager = LinearLayoutManager(this@UserListActivity)
    user_list_recycler.setHasFixedSize(true)
    user_list_recycler.layoutManager = manager
    adapter = UsersAdapter(this, total)
    user_list_recycler.adapter = adapter
    adapter?.users = users
    adapter?.notifyDataSetChanged()

    setupRecyclerOnScrollListener()
  }

  private fun setupFabCreateButton() {
    fabCreateNewUser.setOnClickListener {
      val intent = Intent(this@UserListActivity, UserFormActivity::class.java)

      startActivityForResult(intent, CALLBACK_FROM_USER_FORM)
    }
  }

  private fun setupRecyclerOnScrollListener() {
    user_list_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)

        if (!recyclerView.canScrollVertically(1)) {
          page = page.plus(1)
          jsonParams.put("page", page.toString())

          list()
        }
      }
    })
  }
  // end region

  // region services
  private fun list(reset: Boolean? = false, query: String? = null) {
    disposables.add(
            listUsersUseCase.execute(jsonParams)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                      onListSuccess(it, reset)
                    }, {
                      onFailure(it.message)
                    })
    )
  }

  private fun delete(id: String) {
    progressBarListUsers.visibility = ProgressBar.VISIBLE
    disposables.add(
            deleteUserUseCase.execute(id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                      onDeleteSuccess(it.data)
                    }, {
                      onFailure(it.message)
                    })
    )
  }
  // end region

  // region private
  private fun onListSuccess(listUserResponse: ListUserResponse, reset: Boolean?) {
    if (reset == true) {
      users.clear()
    }
    listUserResponse.data.map { users.add(it) }

    totalPages = listUserResponse.pagination.totalPages
    total = listUserResponse.pagination.total

    if (user_list_recycler.adapter == null) {
      setupRecycler()
    }
    else {
      runOnUiThread {
        adapter?.users = users
        adapter?.notifyDataSetChanged()
      }
    }

    progressBarListUsers.visibility = ProgressBar.GONE
  }

  private fun onDeleteSuccess(user: User) {
    if (!user.active) {
      CustomSnackBarBuilder(this@UserListActivity)
              .withText(R.string.delete_user_success)
              .show()
      list(true)
    }

    progressBarListUsers.visibility = ProgressBar.GONE
  }

  private fun onFailure(message: String?) {
    CustomSnackBarBuilder(this@UserListActivity)
            .withText(message)
            .show()
    progressBarListUsers.visibility = ProgressBar.GONE
  }
  // end region
}