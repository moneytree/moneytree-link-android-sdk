package com.example.myawesomeapp.fcm

/**
 * @author Moneytree KK
 */
interface TokenRegistrar {

  fun registerToken(token: String)

  fun deregisterToken(token: String)
}
