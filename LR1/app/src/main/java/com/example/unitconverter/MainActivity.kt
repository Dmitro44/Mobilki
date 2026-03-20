package com.example.unitconverter

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.unitconverter.databinding.ActivityMainBinding
import com.example.unitconverter.fragment.DataFragment
import com.example.unitconverter.fragment.KeyboardFragment
import com.example.unitconverter.viewmodel.ConverterViewModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    private val viewModel: ConverterViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        if (savedInstanceState == null) {
            setupFragments()
            
            viewModel.categories.value?.firstOrNull()?.let { category ->
                viewModel.selectCategory(category)
            }
        }
        
        setupObservers()
    }
    
    private fun setupFragments() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        
        if (supportFragmentManager.findFragmentById(R.id.fragmentData) == null) {
            fragmentTransaction.add(R.id.fragmentData, DataFragment())
        }
        
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
