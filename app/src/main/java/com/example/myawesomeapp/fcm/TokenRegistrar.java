package com.example.myawesomeapp.fcm;

import android.support.annotation.NonNull;

/**
 * @author Moneytree KK
 */
public interface TokenRegistrar {

  void registerToken(@NonNull String token);

  void deregisterToken(@NonNull String token);
}
