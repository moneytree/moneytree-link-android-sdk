package com.example.myawesomeapp.fcm;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * @author Moneytree
 */
public class AppInstanceIDListenerService extends FirebaseInstanceIdService {

  @Nullable
  static TokenRegistrar registrar = null;

  private static final String TAG = "AppInstanceIDListener";

  @Override
  public void onTokenRefresh() {
    super.onTokenRefresh();
    if (registrar == null) {
      return;
    }

    final String token = FirebaseInstanceId.getInstance().getToken();
    if (token == null) {
      Log.e(TAG, "Token is null");
      return;
    }
    registrar.registerToken(token);
  }
}
