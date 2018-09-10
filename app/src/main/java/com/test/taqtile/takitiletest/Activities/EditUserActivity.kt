package com.test.taqtile.takitiletest.Activities

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.test.taqtile.takitiletest.DataModels.EditUserData
import com.test.taqtile.takitiletest.DataModels.EditUserSuccess
import com.test.taqtile.takitiletest.DataModels.UserLoginError
import com.test.taqtile.takitiletest.Preferences
import com.test.taqtile.takitiletest.R
import com.test.taqtile.takitiletest.RetrofitInitializer
import kotlinx.android.synthetic.main.activity_edit_user.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class EditUserActivity : AppCompatActivity() {

  private var userName: String? = null
  private var userEmail: String? = null
  private var userRole: String? = null
  private var userId: String? = null

  private var token: String? = null
  private var preferences: HashMap<String?, String?>? = null

  private val spinnerItems = HashMap<String?, String?>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_edit_user)

    val actionBar = supportActionBar
    actionBar?.title = getString(R.string.edit_user_title)

    preferences = Preferences(this@EditUserActivity).getPreferences()
    token = preferences?.get("token")

    spinnerItems.put("Usuário", "user")
    spinnerItems.put("Administrador", "admin")

    userName = intent.getStringExtra("userName")
    userEmail = intent.getStringExtra("userEmail")
    userRole = intent.getStringExtra("userRole")
    userId = intent.getStringExtra("userId")

    textEditUserName.setText(userName, TextView.BufferType.EDITABLE)
    textEditUserEmail.setText(userEmail, TextView.BufferType.EDITABLE)

    val spinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerItems.keys.toTypedArray())
    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    spinnerEditUserRole.adapter = spinnerAdapter

    var i = 0
    for ((key, value) in spinnerItems) {
      if (value == userRole)
        spinnerEditUserRole.setSelection(i)
      i++
    }

    spinnerEditUserRole.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
      override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        var i = 0
        for ((key, value) in spinnerItems) {
          if (i == position)
            userRole = value
          i++
        }
      }

      override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
      }
    }

    progressBarEditUser.visibility = ProgressBar.GONE

    buttonEditUserSubmit.setOnClickListener {
      userName = textEditUserName.text.toString()
      userEmail = textEditUserEmail.text.toString()


      if (validate(userName, userEmail)) {
        lockSubmitButton()

        updateUserDataRequest(userId, userName, userEmail, userRole)
      }
    }
  }

  private fun updateUserDataRequest(id: String?, name: String?, email: String?, role: String?) {
    val updateUser = RetrofitInitializer(token).userServices().updateUserData(id, EditUserData(name, email, role))

    updateUser.enqueue(object: Callback<EditUserSuccess?> {
      override fun onResponse(call: Call<EditUserSuccess?>?, response: Response<EditUserSuccess?>?) {
        try {
          userName = response!!.body()!!.data!!.name
          userEmail = response.body()!!.data!!.email
          userRole = response.body()!!.data!!.role
          userId = response.body()!!.data!!.id.toString()

          val intent = Intent(this@EditUserActivity, UserDetailsActivity::class.java)
          intent.putExtra("userName", userName)
          intent.putExtra("userEmail", userEmail)
          intent.putExtra("userRole", userRole)
          intent.putExtra("userId", userId)
          intent.putExtra("message", "Usuário atualizado com sucesso!")

          startActivity(intent)
        }
        catch (e: Exception) {
          val jsonError = response!!.errorBody()!!.string()
          val userLoginError = Gson().fromJson(jsonError, UserLoginError::class.java)

          val builder = android.app.AlertDialog.Builder(this@EditUserActivity)

          builder.setTitle(getString(R.string.login_failure))
          builder.setMessage(userLoginError.errors!!.get(0).message)
          builder.setNeutralButton("OK") { _, _ -> }

          val dialog = builder.create()
          dialog.show()
        }

        unlockSubmitButton()
      }

      override fun onFailure(call: Call<EditUserSuccess?>?, failureResponse: Throwable) {
        val builder = AlertDialog.Builder(this@EditUserActivity)

        builder.setTitle("Erro ao atualizar usuário")
        builder.setMessage(failureResponse.message.toString())
        builder.setNeutralButton("OK") { _, _ -> }

        val dialog = builder.create()
        dialog.show()

        unlockSubmitButton()
      }
    })
  }

  private fun validate(name: String?, email: String?): Boolean {
    var validName = false
    var validEmail = false

    if (validateName(name)) {
      textErrorEditUserName?.visibility = TextView.GONE
      validName = true
    }
    if (validateEmail(email)) {
      textErrorEditUserEmail?.visibility = TextView.GONE
      validEmail = true
    }

    return (validName && validEmail)
  }

  private fun validateName(name: String?): Boolean {
    if (name?.isEmpty()!! || !name.matches("\\D+".toRegex())) {
      textEditUserName?.background?.mutate()?.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
      textErrorEditUserName?.text = getString(R.string.invalid_name)
      textErrorEditUserName?.visibility = TextView.VISIBLE
    }
    textEditUserName?.background?.mutate()?.setColorFilter(getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP)
    return true
  }

  private fun validateEmail(email: String?): Boolean {
    if (email?.isEmpty()!! || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      textEditUserEmail?.background?.mutate()?.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
      textErrorEditUserEmail?.text = getString(R.string.invalid_email)
      textErrorEditUserEmail?.visibility = TextView.VISIBLE

      return false
    }
    textEditUserEmail?.background?.mutate()?.setColorFilter(getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP)
    return true
  }

  private fun lockSubmitButton() {
    progressBarEditUser?.visibility = ProgressBar.VISIBLE
    buttonEditUserSubmit?.text = ""
    buttonEditUserSubmit.isEnabled = false
  }

  private fun unlockSubmitButton() {
    progressBarEditUser?.visibility = ProgressBar.GONE
    buttonEditUserSubmit?.text = getString(R.string.new_user_submit)
    buttonEditUserSubmit.isEnabled = true
  }
}