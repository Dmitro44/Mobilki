package com.example.unitconverter

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.unitconverter.databinding.ActivityMainBinding
import com.example.unitconverter.fragment.DataFragment
import com.example.unitconverter.fragment.KeyboardFragment
import com.example.unitconverter.viewmodel.ConverterViewModel

/**
 * Main Activity for the Unit Converter application.
 * Uses ViewBinding and shared ViewModel for fragment communication.
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    // Shared ViewModel - will be accessed by fragments using activityViewModels()
    private val viewModel: ConverterViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Add fragments programmatically if not already added
        if (savedInstanceState == null) {
            setupFragments()
            
            // Select default category on first launch
            viewModel.categories.value?.firstOrNull()?.let { category ->
                viewModel.selectCategory(category)
            }
        }
        
        // Observe ViewModel for any Activity-level UI updates
        setupObservers()
    }
    
    private fun setupFragments() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        
        // Check if DataFragment is already in the container
        if (supportFragmentManager.findFragmentById(R.id.fragmentData) == null) {
            fragmentTransaction.add(R.id.fragmentData, DataFragment())
        }
        
        // Check if KeyboardFragment is already in the container
        if (supportFragmentManager.findFragmentById(R.id.fragmentKeyboard) == null) {
            fragmentTransaction.add(R.id.fragmentKeyboard, KeyboardFragment())
        }
        
        fragmentTransaction.commit()
    }
    
    private fun setupObservers() {
        // Observe conversion result for potential Activity-level actions
        viewModel.conversionResult.observe(this) { result ->
            // Fragments handle UI updates, but Activity can react if needed
        }
        
        viewModel.error.observe(this) { error ->
            // Handle errors at Activity level if needed
        }
    }
}
