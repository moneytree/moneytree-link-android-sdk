package com.example.myawesomeapp.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * @author Moneytree KK
 */
class MessagingService : FirebaseMessagingService() {

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    Log.i(TAG, "New FCM token: $token")
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    Log.i(TAG, "Got new message: ${remoteMessage.messageId}")
  }

  companion object {
    private const val TAG = "MessagingService"
  }
}
