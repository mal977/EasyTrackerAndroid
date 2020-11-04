package com.ntu.staizen.EasyTracker.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.app.Activity;
import android.content.Context;
import android.util.Patterns;

import com.ntu.staizen.EasyTracker.R;
import com.ntu.staizen.EasyTracker.firebase.Authentication;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();

    MutableLiveData<AuthenticatedState> authenticated = new MutableLiveData<>();

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<AuthenticatedState> getAuthenticatedState() {
        return authenticated;
    }


    public void login(String username, String phoneNumber, Activity activity) {
        if (getLoginFormState().getValue() == null) {
            setAuthenticated(0, activity.getString(R.string.please_enter_details));
            return;
        }
        if (getLoginFormState().getValue().isDataValid()) {
            Authentication mAuthenciation = Authentication.getInstance(activity);
            mAuthenciation.signInAnonymously(activity, username, phoneNumber);
        } else {
            setAuthenticated(0, "Please fix the following errors.");
        }
    }

    public void loginDataChanged(String username, String phoneNumber) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPhoneNumberValid(phoneNumber)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_phone_number));
        }  else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    public void setAuthenticated(int authenticated, String errorMessage) {
        this.authenticated.setValue(new AuthenticatedState(authenticated, errorMessage));
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    private boolean isPhoneNumberValid(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        phoneNumber = phoneNumber.trim();

        if (phoneNumber.length() >= 8) {
            return Patterns.PHONE.matcher(phoneNumber).matches();
        } else {
            return false;
        }
    }
}