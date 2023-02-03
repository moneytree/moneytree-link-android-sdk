package com.example.myawesomeapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.myawesomeapp.databinding.FragmentLinkKitBinding
import com.getmoneytree.MoneytreeLinkException
import com.getmoneytree.linkkit.LinkKit

class LinkKitFragment : BaseFragment() {

  private var _binding: FragmentLinkKitBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentLinkKitBinding.inflate(requireActivity().layoutInflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.linkKitButton.setOnClickListener { sdkStartLinkKit() }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  //region ------------------------------ SDK flows ------------------------------

  private fun sdkStartLinkKit() {
    showMessage(getString(R.string.link_kit_output_opening_link_kit))

    LinkKit.getInstance().launch(
      activity = requireActivity() as AppCompatActivity,
      listener = object : LinkKit.LinkKitListener {
        override fun onLaunched() {
          showMessage(getString(R.string.link_kit_output_opened_link_kit))
        }

        override fun onError(exception: MoneytreeLinkException) {
          showError(exception.message ?: return)
        }
      }
    )
  }

  //endregion
}
