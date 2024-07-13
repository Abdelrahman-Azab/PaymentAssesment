package com.zeal.paymentassignment.ui

import android.content.ContentResolver
import android.database.Cursor
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

class ReceivingAfterDiscountFragment : Fragment() {
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
                DialogHelper.showLoadingDialog(requireActivity(), "Sending Transaction to The Loyalty application")
                Thread.sleep(2000)
                if (cardId.isNotEmpty()) {
                    val contentResolver: ContentResolver = requireActivity().contentResolver
                    val uri = ZealContentProvider.CONTENT_URI
                    val projection = null // null returns all columns
                    val selection = "${ZealContentProvider.cardNumber} = ?"
                    val selectionArgs = arrayOf(cardId)
                    val sortOrder = null

                    val cursor: Cursor? = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)

                    if (cursor != null && cursor.moveToFirst()) {
                        val amount = cursor.getString(cursor.getColumnIndexOrThrow(ZealContentProvider.amount))
                        val time = cursor.getLong(cursor.getColumnIndexOrThrow(ZealContentProvider.time))
                        val name = cursor.getString(cursor.getColumnIndexOrThrow(ZealContentProvider.name))
                        val discountAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(ZealContentProvider.discountAmount))

                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Amount: $amount, Time: $time, Name: $name, Discount Amount: $discountAmount", Toast.LENGTH_SHORT).show()
                            FlowDataObject.getInstance().amount = discountAmount.toFloat()
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "No data found for card number: $cardId", Toast.LENGTH_SHORT).show()
                        }
                    }

                    cursor?.close()
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                }

                DialogHelper.showLoadingDialog(requireActivity(), "Receiving Loyalty app Response")
                Thread.sleep(1000)
                DialogHelper.hideLoading(requireActivity())

                requireActivity().runOnUiThread {
                    findNavController().navigate(R.id.action_receivingAfterDiscountFragment_to_printReceiptFragment)
                }
            }.start()
        }) {
            findNavController().popBackStack()
        }

        return binding.root
    }
}
