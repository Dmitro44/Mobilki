package com.example.unitconverter.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Button
import android.widget.Toast
import com.example.unitconverter.R

/**
 * Premium version of DataFragment with additional features:
 * - Swap button to swap from/to units
 * - Copy buttons to copy input/output values to clipboard
 */
class DataFragment : DataFragmentBase() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupPremiumFeatures(view)
    }
    
    private fun setupPremiumFeatures(view: View) {
        // Swap button
        view.findViewById<Button>(R.id.btnSwap)?.setOnClickListener {
            viewModel.onSwapClick()
        }
        
        // Copy input button
        view.findViewById<ImageButton>(R.id.btnCopyInput)?.setOnClickListener {
            val inputText = binding.etInput.text.toString()
            copyToClipboard(inputText)
        }
        
        // Copy output button
        view.findViewById<ImageButton>(R.id.btnCopyOutput)?.setOnClickListener {
            val outputText = binding.etOutput.text.toString()
            copyToClipboard(outputText)
        }
    }
    
    private fun copyToClipboard(text: String) {
        if (text.isNotEmpty()) {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Converted Value", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
        }
    }
}
