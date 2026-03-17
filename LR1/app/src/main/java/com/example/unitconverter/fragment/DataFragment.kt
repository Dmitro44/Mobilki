package com.example.unitconverter.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.unitconverter.R
import com.example.unitconverter.databinding.FragmentDataBinding
import com.example.unitconverter.viewmodel.ConverterViewModel

/**
 * Base Fragment displaying conversion data - input value, result, and unit selection.
 * Uses shared ViewModel from Activity.
 * This is the base class that can be extended for flavor-specific implementations.
 */
open class DataFragmentBase : Fragment() {

    private var _binding: FragmentDataBinding? = null
    protected val binding get() = _binding!!

    protected val viewModel: ConverterViewModel by activityViewModels()

    // Flag to prevent recursive updates
    private var isUpdatingFromViewModel = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Disable system keyboard for EditTexts
        binding.etInput.showSoftInputOnFocus = false
        
        setupClickListeners()
        setupSpinnerListeners()
        setupTextWatchers()
        setupObservers()
        
        // Set initial active field highlighting
        updateActiveFieldHighlighting(viewModel.activeField.value ?: ConverterViewModel.ActiveField.INPUT)
    }

    /**
     * Updates the visual highlighting based on the active field
     */
    private fun updateActiveFieldHighlighting(field: ConverterViewModel.ActiveField) {
        val activeDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.border_active)
        val defaultDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.border_default)
        
        when (field) {
            ConverterViewModel.ActiveField.INPUT -> {
                binding.inputContainer.background = activeDrawable
                binding.outputContainer.background = defaultDrawable
                binding.labelFrom.setTypeface(null, Typeface.BOLD)
                binding.labelTo.setTypeface(null, Typeface.NORMAL)
            }
            ConverterViewModel.ActiveField.OUTPUT -> {
                binding.inputContainer.background = defaultDrawable
                binding.outputContainer.background = activeDrawable
                binding.labelFrom.setTypeface(null, Typeface.NORMAL)
                binding.labelTo.setTypeface(null, Typeface.BOLD)
            }
        }
    }

    private fun setupClickListeners() {
        val disableActionMode = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean = false
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false
            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean = false
            override fun onDestroyActionMode(mode: ActionMode?) {}
        }

        // Set active field when clicking on input EditText
        binding.etInput.setOnClickListener {
            viewModel.setActiveField(ConverterViewModel.ActiveField.INPUT)
        }
        binding.etInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewModel.setActiveField(ConverterViewModel.ActiveField.INPUT)
            }
        }
        binding.etInput.setOnLongClickListener { true }
        binding.etInput.customSelectionActionModeCallback = disableActionMode
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            binding.etInput.customInsertionActionModeCallback = disableActionMode
        }
        
        // Disable context menu and selection on the read-only output field
        binding.etOutput.setOnLongClickListener { true }
        binding.etOutput.setOnClickListener { } // Consume clicks so they don't trigger anything
        
        // Allow horizontal scrolling but block focus/editing
        binding.etOutput.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                v.performClick()
            }
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    // Allow the EditText to handle down/move for scrolling
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    false 
                }
                else -> {
                    // Block other events like clicks or long presses from triggering menus
                    false
                }
            }
        }
        
        binding.etOutput.customSelectionActionModeCallback = disableActionMode
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            binding.etOutput.customInsertionActionModeCallback = disableActionMode
        }
    }

    private fun setupSpinnerListeners() {
        // Category spinner listener
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isUpdatingFromViewModel) return
                val categories = viewModel.categories.value ?: return
                if (position in categories.indices) {
                    val category = categories[position]
                    if (viewModel.selectedCategory.value?.id != category.id) {
                        viewModel.selectCategory(category)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // From unit spinner listener
        binding.spinnerFromUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isUpdatingFromViewModel) return
                val units = viewModel.availableUnits.value ?: return
                if (position in units.indices) {
                    val unit = units[position]
                    if (viewModel.fromUnit.value?.id != unit.id) {
                        viewModel.setFromUnit(unit)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // To unit spinner listener
        binding.spinnerToUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isUpdatingFromViewModel) return
                val units = viewModel.availableUnits.value ?: return
                if (position in units.indices) {
                    val unit = units[position]
                    if (viewModel.toUnit.value?.id != unit.id) {
                        viewModel.setToUnit(unit)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * Sets up TextWatchers for input and output EditTexts to handle paste and direct text input.
     */
    private fun setupTextWatchers() {
        // TextWatcher for input field
        binding.etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                // Skip if this change came from ViewModel update (not user action)
                if (isUpdatingFromViewModel) return
                
                val text = s?.toString() ?: ""
                // Set active field to INPUT when user types/pastes into input
                viewModel.setActiveField(ConverterViewModel.ActiveField.INPUT)
                // Update ViewModel with new input value
                viewModel.setInputValueString(text)
            }
        })
        
        // TextWatcher for output field
        // Read-only output field doesn't need TextWatcher since it won't receive input
        // and we disabled focus and touch events
    }

    private fun setupObservers() {
        // Observe categories and populate category spinner
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            isUpdatingFromViewModel = true
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories.map { it.name }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
            isUpdatingFromViewModel = false
        }

        // Observe selected category
        viewModel.selectedCategory.observe(viewLifecycleOwner) { category ->
            isUpdatingFromViewModel = true
            val categories = viewModel.categories.value ?: emptyList()
            val index = categories.indexOfFirst { it.id == category?.id }
            if (index >= 0 && binding.spinnerCategory.selectedItemPosition != index) {
                binding.spinnerCategory.setSelection(index)
            }
            isUpdatingFromViewModel = false
        }

        // Observe available units and populate unit spinners
        viewModel.availableUnits.observe(viewLifecycleOwner) { units ->
            isUpdatingFromViewModel = true
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                units.map { it.name }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerFromUnit.adapter = adapter
            binding.spinnerToUnit.adapter = adapter
            isUpdatingFromViewModel = false
        }

        // Observe input value
        viewModel.inputValue.observe(viewLifecycleOwner) { value ->
            isUpdatingFromViewModel = true
            val displayValue = value.ifEmpty { "" }
            if (binding.etInput.text.toString() != displayValue) {
                binding.etInput.setText(displayValue)
                binding.etInput.setSelection(displayValue.length)
            }
            isUpdatingFromViewModel = false
        }

        // Observe output value
        viewModel.outputValue.observe(viewLifecycleOwner) { value ->
            isUpdatingFromViewModel = true
            val displayValue = value.ifEmpty { "" }
            if (binding.etOutput.text.toString() != displayValue) {
                binding.etOutput.setText(displayValue)
                // Removed setSelection here because the output is read-only
                // and calling it can sometimes force the cursor to be visible
            }
            isUpdatingFromViewModel = false
        }

        // Observe conversion result to update output when converting from input
        viewModel.conversionResult.observe(viewLifecycleOwner) { result ->
            if (viewModel.activeField.value == ConverterViewModel.ActiveField.INPUT) {
                isUpdatingFromViewModel = true
                val outputText = if (result != null) {
                    // Delegate all formatting to the ViewModel's outputValue LiveData so
                    // that the fragment never applies its own (lower-precision) formatter.
                    // Reading result.outputValue here with "%.6f" would collapse values like
                    // 1e-11 to "0", which corrupts _outputValue via the TextWatcher.
                    viewModel.outputValue.value ?: ""
                } else {
                    ""
                }
                if (binding.etOutput.text.toString() != outputText) {
                    binding.etOutput.setText(outputText)
                }
                isUpdatingFromViewModel = false
            }
        }

        // Observe from unit selection
        viewModel.fromUnit.observe(viewLifecycleOwner) { unit ->
            isUpdatingFromViewModel = true
            val units = viewModel.availableUnits.value ?: emptyList()
            val index = units.indexOfFirst { it.id == unit?.id }
            if (index >= 0 && binding.spinnerFromUnit.selectedItemPosition != index) {
                binding.spinnerFromUnit.setSelection(index)
            }
            isUpdatingFromViewModel = false
        }

        // Observe to unit selection
        viewModel.toUnit.observe(viewLifecycleOwner) { unit ->
            isUpdatingFromViewModel = true
            val units = viewModel.availableUnits.value ?: emptyList()
            val index = units.indexOfFirst { it.id == unit?.id }
            if (index >= 0 && binding.spinnerToUnit.selectedItemPosition != index) {
                binding.spinnerToUnit.setSelection(index)
            }
            isUpdatingFromViewModel = false
        }

        // Observe active field to highlight the active EditText
        viewModel.activeField.observe(viewLifecycleOwner) { field ->
            updateActiveFieldHighlighting(field)
            when (field) {
                ConverterViewModel.ActiveField.INPUT -> {
                    binding.etInput.requestFocus()
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
