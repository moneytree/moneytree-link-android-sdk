package com.example.myawesomeapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myawesomeapp.databinding.FragmentVaultBinding
import com.getmoneytree.LinkRequestContext
import com.getmoneytree.MoneytreeLink
import com.getmoneytree.VaultOpenServicesOptions

class VaultFragment : BaseFragment() {

  private var _binding: FragmentVaultBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentVaultBinding.inflate(requireActivity().layoutInflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.vaultButton.setOnClickListener { sdkOpenVault() }

    setupServiceTypeDropdown()
    setupAccountGroupDropdown()
    binding.openServicesButton.setOnClickListener { sdkOpenService() }

    binding.connectServiceButton.setOnClickListener { sdkConnectService() }
    binding.connectionSettingsButton.setOnClickListener { sdkConnectionSettings() }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  private fun setupServiceTypeDropdown() {
    binding.typeSpinner.adapter = GenericSpinnerAdapter(
      context = requireContext(),
      data = listOf(
        getString(R.string.general_no_selection),
        "bank",
        "credit_card",
        "stock",
        "stored_value",
        "point",
        "corporate",
      ),
    ) { it }
  }

  private fun setupAccountGroupDropdown() {
    binding.groupSpinner.adapter = GenericSpinnerAdapter(
      context = requireContext(),
      data = listOf(
        getString(R.string.general_no_selection),
        "grouping_bank",
        "grouping_bank_credit_card",
        "grouping_bank_dc_card",
        "grouping_corporate_credit_card",
        "grouping_credit_card",
        "grouping_credit_coop",
        "grouping_credit_union",
        "grouping_dc_pension_plan",
        "grouping_debit_card",
        "grouping_digital_money",
        "grouping_ja_bank",
        "grouping_life_insurance",
        "grouping_point",
        "grouping_regional_bank",
        "grouping_stock",
        "grouping_testing",
      ),
    ) { it }
  }

  //region ------------------------------ SDK flows ------------------------------

  private fun sdkOpenVault() {
    MoneytreeLink.instance.openVault(
      activity = requireActivity(),
      requestContext = LinkRequestContext
        .Builder()
        .build()
    )
  }

  private fun sdkOpenService() {
    val options = VaultOpenServicesOptions.Builder()
      .type(
        binding.typeSpinner.selectedItem.toString()
          .takeUnless { binding.typeSpinner.selectedItemPosition == 0 }
      )
      .group(
        binding.groupSpinner.selectedItem.toString()
          .takeUnless { binding.groupSpinner.selectedItemPosition == 0 }
      )
      .search(binding.openServicesSearchInput.text.toString())
      .build()

    MoneytreeLink.instance.openVault(
      activity = requireActivity(),
      requestContext = LinkRequestContext
        .Builder()
        .vaultOpenServicesOptions(options)
        .build()
    )
  }

  private fun sdkConnectService() {
    // The `entityKey` can be found in
    // https://docs.link.getmoneytree.com/reference/institution-list
    val entityKey = binding.serviceEntityKeyInput.text.toString()

    if (entityKey.isBlank()) {
      showError(getString(R.string.services_output_entity_key_required))
      hideKeyboard()
      return
    }

    MoneytreeLink.instance.openVault(
      activity = requireActivity(),
      requestContext = LinkRequestContext
        .Builder()
        .path(MoneytreeLink.VAULT_SERVICE)
        .pathSuffix(entityKey)
        .build()
    )
  }

  private fun sdkConnectionSettings() {
    // The `accountGroup` can be found in
    // https://docs.link.getmoneytree.com/reference/get-link-v1-profile-account-groups
    val accountGroup = binding.accountGroupInput.text.toString()

    if (accountGroup.isBlank()) {
      showError(getString(R.string.services_output_account_group_required))
      hideKeyboard()
      return
    }

    MoneytreeLink.instance.openVault(
      activity = requireActivity(),
      requestContext = LinkRequestContext
        .Builder()
        .path(MoneytreeLink.VAULT_SERVICE_SETTINGS)
        .pathSuffix(accountGroup)
        .build()
    )
  }

  //endregion
}
