package com.example.radarphone.viewModels

import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.radarphone.R
import com.google.firebase.auth.FirebaseAuth
import com.example.radarphone.dataStructures.Player
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

class RegLogViewModel : ViewModel() {

    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    fun isValidUsernameAndPassword(username: String, password: String): Boolean {
        val blackListCharsUsername = listOf('\'', '"', ' ', ',', ';', '(', ')', '[', ']', '{', '}', '<', '>', ':', '?', '!', '$', '&', '*', '#', '^', '/', '|', '-', '+', '~', '.')
        val blackListCharsPassword = listOf('\'', '"', ' ', ',')

        return username.none { it in blackListCharsUsername } && password.none { it in blackListCharsPassword }
    }
    fun isValidPassword(password: String): Boolean {
        val blackListCharsPassword = listOf('\'', '"', ' ', ',')

        return password.none { it in blackListCharsPassword }
    }

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

    suspend fun login(email : String,password : String): Pair<Boolean, String>{

        if(email.isEmpty() || password.isEmpty()){
            Log.d("SignIn", "Error fields empty")
            _authState.value = AuthState.Error("Email or password can't be empty")
            return Pair(false, "Email or password can't be empty")
        }
        if (password.length < 6) {
            Log.d("SignIn", "Error passw not long enough")
            _authState.value = AuthState.Error("Passw not long enough")
            return Pair(false, "Password needs at least 6 characters")
        }
        if (!email.matches(emailPattern.toRegex())) {
            Log.d("SignIn", "Error email not valid")
            _authState.value = AuthState.Error("Email not valid")
            return Pair(false, "Email format not valid")
        }
        //Check for sql injection with blacklist
        if (!isValidPassword(password)) {
            Log.d("SignIn", "Error chars not valid")
            _authState.value = AuthState.Error("Chars not valid")
            return Pair(false, "Cannot use special characters in password")
        }

        _authState.value = AuthState.Loading

        try {
            val authResult = auth.signInWithEmailAndPassword(email,password).await()

            _authState.value = AuthState.Authenticated
            return Pair(true, "SignUp successful")
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Something went wrong")
            return Pair(false, e.message ?: "Something went wrong")
        }
    }

    suspend fun signup(email: String, password: String, username: String, confirmPassword: String): Pair<Boolean, String> {
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
        if (!isValidUsernameAndPassword(username, password)) {
            Log.d("SignUp", "Error chars not valid")
            _authState.value = AuthState.Error("Chars not valid")
            return Pair(false, "Cannot use special characters in username or password")
        }

        Log.d("SignUp", "Starting signup process")
        _authState.value = AuthState.Loading

       try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid
            val database = FirebaseDatabase.getInstance().reference

            val user = Player(userId!!, username, email, password) // Create a User data class
            database.child("users").child(userId).setValue(user).await()
            _authState.value = AuthState.Authenticated
            return Pair(true, "SignUp successful")
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Something went wrong")
            return Pair(false, e.message ?: "Something went wrong")
        }
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