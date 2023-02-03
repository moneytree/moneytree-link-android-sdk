package com.example.myawesomeapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myawesomeapp.databinding.FragmentOtherBinding
import com.getmoneytree.LinkRequestContext
import com.getmoneytree.MoneytreeLink
import com.getmoneytree.MoneytreeLinkException
import com.getmoneytree.listener.Action

class OtherFragment : BaseFragment() {

  private var _binding: FragmentOtherBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentOtherBinding.inflate(requireActivity().layoutInflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Notifications
    binding.registerButton.setOnClickListener { sdkRegisterFcmToken() }
    binding.unregisterButton.setOnClickListener { sdkUnregisterFcmToken() }

    // Login Links
    setupLoginLinkDestinations()
    binding.loginLinkButton.setOnClickListener { sdkRequestLoginLink() }

    binding.settingsButton.setOnClickListener { sdkOpenSettings() }
    binding.customerSupportButton.setOnClickListener { sdkCustomerSupport() }

    binding.tokenButton.setOnClickListener { sdkGetToken() }
    binding.resetButton.setOnClickListener { sdkDeleteCredentials() }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  private fun setupLoginLinkDestinations() {
    binding.loginLinkDestination.adapter = GenericSpinnerAdapter(
      context = requireContext(),
      data = listOf(
        Pair(
          MoneytreeLink.ML_DESTINATION_SETTINGS,
          getString(R.string.other_functions_login_link_destination_settings)
        ),
        Pair(
          MoneytreeLink.ML_DESTINATION_LANGUAGE,
          getString(R.string.other_functions_login_link_destination_change_language)
        ),
        Pair(
          MoneytreeLink.ML_DESTINATION_AUTHORIZED_APPS,
          getString(R.string.other_functions_login_link_destination_authorized_apps)
        ),
        Pair(
          MoneytreeLink.ML_DESTINATION_DELETE_ACCOUNT,
          getString(R.string.other_functions_login_link_destination_delete_account)
        ),
        Pair(
          MoneytreeLink.ML_DESTINATION_EMAIL_PREFERENCES,
          getString(R.string.other_functions_login_link_destination_update_email_preferences)
        ),
        Pair(
          MoneytreeLink.ML_DESTINATION_UPDATE_EMAIL,
          getString(R.string.other_functions_login_link_destination_update_email_address)
        ),
        Pair(
          MoneytreeLink.ML_DESTINATION_UPDATE_PASSWORD,
          getString(R.string.other_functions_login_link_destination_update_password)
        ),
      ),
    ) { it?.second }
  }

  /**
   * You have to return a valid FCM token. Please refer to the FCM documentation at
   * https://firebase.google.com/docs/cloud-messaging  on how to set this up.
   */
  private fun getDeviceToken(): String = "__device_token__"

  //region ------------------------------ SDK flows ------------------------------

  /** Register a token to the Moneytree notifications server */
  private fun sdkRegisterFcmToken() {
    if (!MoneytreeLink.instance.isLoggedIn) {
      showError(getString(R.string.error_unauthorized))
      return
    }

    showMessage(getString(R.string.other_functions_notifications_registering_device))

    MoneytreeLink
      .instance
      .registerRemoteToken(
        getDeviceToken(),
        object : Action {
          override fun onSuccess() {
            showMessage(getString(R.string.other_functions_output_notification_register_success))
          }

          override fun onError(exception: MoneytreeLinkException) {
            showError(exception.message ?: return)
          }
        }
      )
  }

  /** Remove a token from the Moneytree notifications server */
  private fun sdkUnregisterFcmToken() {
    if (!MoneytreeLink.instance.isLoggedIn) {
      showError(getString(R.string.error_unauthorized))
      return
    }

    showMessage(getString(R.string.other_functions_notifications_unregistering_device))

    MoneytreeLink
      .instance
      .unregisterRemoteToken(
        getDeviceToken(),
        object : Action {
          override fun onSuccess() {
            showMessage(
              getString(R.string.other_functions_output_notification_unregister_success)
            )
          }

          override fun onError(exception: MoneytreeLinkException) {
            showError(exception.message ?: return)
          }
        }
      )
  }

  private fun sdkRequestLoginLink() {
    val email = binding.loginLinkEmail.text.toString()
    if (email.isEmpty()) {
      showError(getString(R.string.general_output_email_required))
      hideKeyboard()
      return
    }

    showMessage(getString(R.string.other_functions_login_link_requesting_login_link))
    hideKeyboard()

    @Suppress("UNCHECKED_CAST")
    val selectedItem = binding.loginLinkDestination.selectedItem as Pair<String, String>
    MoneytreeLink.instance.requestLoginLink(
      email = email,
      destination = selectedItem.first,
      listener = object : Action {
        override fun onSuccess() {
          showMessage(getString(R.string.other_functions_login_link_requested_login_link))
        }

        override fun onError(exception: MoneytreeLinkException) {
          showError(exception.message ?: return)
        }
      }
    )
  }

  private fun sdkOpenSettings() {
    MoneytreeLink.instance.openSettings(
      activity = requireActivity(),
    )
  }

  private fun sdkCustomerSupport() {
    MoneytreeLink.instance.openVault(
      activity = requireActivity(),
      requestContext = LinkRequestContext.Builder()
        .path(MoneytreeLink.VAULT_SUPPORT)
        .build()
    )
  }

  private fun sdkGetToken() {
    MoneytreeLink.instance.getToken(requireActivity())
  }

  private fun sdkDeleteCredentials() {
    MoneytreeLink.instance.deleteCredentials()
    showMessage(getString(R.string.other_functions_output_cleared_token_success))
  }

  //endregion
}
