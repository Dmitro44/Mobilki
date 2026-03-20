package com.example.unitconverter.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.unitconverter.databinding.FragmentKeyboardBinding
import com.example.unitconverter.viewmodel.ConverterViewModel

class KeyboardFragment : Fragment() {

    private var _binding: FragmentKeyboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConverterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKeyboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupKeyboard()
    }

    private fun setupKeyboard() {
        // Number buttons 0-9
        binding.btn0.setOnClickListener { viewModel.onDigitClick("0") }
        binding.btn1.setOnClickListener { viewModel.onDigitClick("1") }
        binding.btn2.setOnClickListener { viewModel.onDigitClick("2") }
        binding.btn3.setOnClickListener { viewModel.onDigitClick("3") }
        binding.btn4.setOnClickListener { viewModel.onDigitClick("4") }
        binding.btn5.setOnClickListener { viewModel.onDigitClick("5") }
        binding.btn6.setOnClickListener { viewModel.onDigitClick("6") }
        binding.btn7.setOnClickListener { viewModel.onDigitClick("7") }
        binding.btn8.setOnClickListener { viewModel.onDigitClick("8") }
        binding.btn9.setOnClickListener { viewModel.onDigitClick("9") }

        // Decimal point button
        binding.btnDot.setOnClickListener { viewModel.onDigitClick(".") }

        // Backspace/Delete button
        binding.btnBackspace.setOnClickListener { viewModel.onDeleteClick() }

        // Clear button
        binding.btnClear.setOnClickListener { viewModel.onClearClick() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
