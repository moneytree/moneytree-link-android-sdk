package com.example.myawesomeapp.fcm;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * @author Moneytree
 */
public class MessagingService extends FirebaseMessagingService {

  private final static String TAG = "MessagingService";

  @Override
  public void onMessageReceived(final RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);
    Log.i(TAG, "Got new message: " + remoteMessage.getMessageId());
  }
}
