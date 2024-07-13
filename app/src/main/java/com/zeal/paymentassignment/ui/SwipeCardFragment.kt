package com.zeal.paymentassignment.ui

import android.content.ContentValues
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.zeal.paymentassignment.R
import com.zeal.paymentassignment.core.DialogHelper
import com.zeal.paymentassignment.core.FlowDataObject
import com.zeal.paymentassignment.core.ZealContentProvider
import com.zeal.paymentassignment.databinding.FragmentSwipeFragment2Binding

class SwipeCardFragment : Fragment() {
    private val binding by lazy {
        FragmentSwipeFragment2Binding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        DialogHelper.showPanDialog(requireContext(), { cardId ->
            Thread {
                DialogHelper.showLoadingDialog(requireActivity(), "Sending Transaction to The Bank")
                Thread.sleep(2000)
                if (cardId.isNotEmpty()) {
                    // Key-value object to add value in the database
                    val values = ContentValues().apply {
                        put(ZealContentProvider.amount, FlowDataObject.getInstance().amount.toString())
                        put(ZealContentProvider.cardNumber, cardId)
                        put(ZealContentProvider.time, System.currentTimeMillis())
                        put(ZealContentProvider.name,"Zeal")
                        put(ZealContentProvider.discountAmount,"0.0")
                    }

                    // Insert data using Content URI
                    val uri = requireActivity().contentResolver.insert(ZealContentProvider.CONTENT_URI, values)
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), uri.toString(), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                }
                DialogHelper.showLoadingDialog(requireActivity(), "Receiving Bank Response")
                Thread.sleep(1000)
                DialogHelper.hideLoading(requireActivity())
                requireActivity().runOnUiThread {
                    findNavController().navigate(R.id.action_swipeCardFragment_to_printReceiptFragment)
                }
            }.start()
        }) {
            findNavController().popBackStack()
        }

        return binding.root
    }
}
