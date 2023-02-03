package com.example.myawesomeapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myawesomeapp.databinding.FragmentMoneytreeIdBinding
import com.getmoneytree.AuthenticationMethod
import com.getmoneytree.LinkAuthFlow
import com.getmoneytree.LinkAuthOptions
import com.getmoneytree.MoneytreeLink
import com.getmoneytree.MoneytreeLinkConfiguration
import com.getmoneytree.linkkit.LinkKit
import com.google.android.material.chip.ChipGroup

class MoneytreeIdFragment : BaseFragment() {

  private var _binding: FragmentMoneytreeIdBinding? = null
  private val binding get() = _binding!!
  private var lastSelectedAuthnMethod: Int = 0

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding =
      FragmentMoneytreeIdBinding.inflate(requireActivity().layoutInflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    lastSelectedAuthnMethod = binding.chipGroupAuthnMethod.checkedChipId

    binding.buttonCreateMtId.setOnClickListener { sdkAuthorize(true) }
    binding.buttonLogIn.setOnClickListener { sdkAuthorize(false) }
    binding.buttonOnboarding.setOnClickListener { sdkOnboard() }
    binding.buttonLogOut.setOnClickListener { sdkLogout() }
    binding.chipGroupAuthnMethod.setOnCheckedStateChangeListener { _, checkedChips ->
      checkedChips.first().let {
        if (it == lastSelectedAuthnMethod) return@setOnCheckedStateChangeListener

        var selectedChipText: String
        val authnMethod = when(it) {
          binding.chipAuthnMethodCredentials.id -> AuthenticationMethod.Credentials.also {
            selectedChipText = binding.chipAuthnMethodCredentials.text.toString()
          }
          binding.chipAuthnMethodPasswordless.id -> AuthenticationMethod.Passwordless.also {
            selectedChipText = binding.chipAuthnMethodPasswordless.text.toString()
          }
          binding.chipAuthnMethodSso.id -> AuthenticationMethod.SingleSignOn.also {
            selectedChipText = binding.chipAuthnMethodSso.text.toString()
          }
          else -> error("Something has gone really wrong!!! Authentication Method UI is broken!")
        }

        lastSelectedAuthnMethod = it
        sdkUpdateLinkConfiguration(authnMethod)
        showMessage(
          getString(
            R.string.moneytree_id_output_selected_authentication_method,
            selectedChipText
          )
        )
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  //region ------------------------------ SDK flows ------------------------------

  private fun sdkAuthorize(isSignup: Boolean) {
    MoneytreeLink.instance.authorize(
      activity = requireActivity(),
      config = LinkAuthOptions.create(
        forceLogout = binding.switchForceLogout.isChecked,
        presentSignup = isSignup,
      ).buildAuthorize(binding.emailOptional.text?.toString() ?: "")
    )
  }

  // Onboarding is also know as Passwordless Sign up.
  private fun sdkOnboard() {
    val email = binding.emailRequired.editableText.toString()
    if (email.isEmpty()) {
      showError(getString(R.string.general_output_email_required))
      hideKeyboard()
      return
    }

    MoneytreeLink.instance.onboard(
      activity = requireActivity(),
      config = LinkAuthOptions.create().buildOnboarding(email)
    )
  }

  private fun sdkLogout() {
    MoneytreeLink.instance.logout(requireActivity())
  }

  private fun sdkUpdateLinkConfiguration(authnMethod: AuthenticationMethod) {
    val currentConfig = MoneytreeLink.instance.configuration
    val newConfig = MoneytreeLinkConfiguration.create(
      linkEnvironment = currentConfig.linkEnvironment,
      clientId = currentConfig.clientId,
      scopes = currentConfig.scope.split(" ").toSet(),
      authenticationMethod = authnMethod
    )
    MoneytreeLink.instance.updateConfiguration(newConfig)
    LinkKit.init(requireActivity().applicationContext, newConfig)
  }

  //endregion
}
