package com.example.radarphone

import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.radarphone.ui.theme.User

class RegLogViewModel : ViewModel() {

    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    //Firebase authentication
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    //Tells us if auth or not
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    //Checks auth status
    init {
        checkAuthStatus()
    }

    fun checkAuthStatus(){
        if(auth.currentUser==null){
            _authState.value = AuthState.Unauthenticated
        }else{
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email : String,password : String): Pair<Boolean, String>{

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or password can't be empty")
            return Pair(false, "Email or password can't be empty")
        }
        var failure=false
        var success=true
        var msgErr="Something went wrong, try again"
        var msgSucc="You logged in"

        //check why login does not work

        var myPair: Pair<Boolean, String>  = Pair(failure,msgErr)
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if (task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                    myPair=Pair(success,msgSucc)
                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                    myPair=Pair(failure,msgErr)
                }
            }
        return myPair
    }

    fun signup(email: String, password: String, username: String, confirmPassword: String): Pair<Boolean, String> {
        if (email.isEmpty() || password.isEmpty() || username.isEmpty() || confirmPassword.isEmpty()) {
            Log.d("SignUp", "Error fields empty")
            _authState.value = AuthState.Error("Fields cannot be empty")
            return Pair(false, "Fields cannot be empty")
        }
        if (password != confirmPassword) {
            Log.d("SignUp", "Error passw not coinciding")
            _authState.value = AuthState.Error("Passw != Confirm")
            return Pair(false, "Wrong confirm password")
        }
        if (password.length < 6) {
            Log.d("SignUp", "Error passw not long enough")
            _authState.value = AuthState.Error("Passw not long enough")
            return Pair(false, "Password needs at least 6 characters")
        }
        if (!email.matches(emailPattern.toRegex())) {
            Log.d("SignUp", "Error email not valid")
            _authState.value = AuthState.Error("Email not valid")
            return Pair(false, "Email format not valid")
        }
        //Check for sql injection with blacklist

        Log.d("SignUp", "Starting signup process")
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                Log.d("SignUp", "Task signup process")
                if (task.isSuccessful) {
                    Log.d("SignUp", "Task signup successful")
                    val userId = auth.currentUser?.uid
                    val database = com.google.firebase.database.FirebaseDatabase.getInstance().reference

                    val user = User(userId!!, username, email, password) // Create a User data class

                    database.child("users").child(userId).setValue(user)
                        .addOnSuccessListener {
                            _authState.value = AuthState.Authenticated
                        }
                        .addOnFailureListener {
                            _authState.value = AuthState.Error(it.message ?: "Something went wrong")
                        }
                } else {
                    Log.d("SignUp", "Task signup failure")
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
        return Pair(true, "Success in registration")
    }

    fun signout(){
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }


}


sealed class AuthState{
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message : String) : AuthState()
}