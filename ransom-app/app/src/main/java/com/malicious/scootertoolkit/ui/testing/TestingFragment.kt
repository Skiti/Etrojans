package com.malicious.scootertoolkit.ui.testing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.malicious.scootertoolkit.databinding.FragmentTestingBinding

class TestingFragment : Fragment() {

    private var _binding: FragmentTestingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val testingViewModel =
                ViewModelProvider(this).get(TestingViewModel::class.java)

        _binding = FragmentTestingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSlideshow
        testingViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        testingViewModel.connected.observe(viewLifecycleOwner) {
            binding.registerButton.isEnabled = it
        }
        testingViewModel.registered.observe(viewLifecycleOwner) {
            binding.authButton.isEnabled = it
        }
        testingViewModel.authenticated.observe(viewLifecycleOwner) {
            binding.testButton.isEnabled = it
        }

        binding.registerButton.setOnClickListener{ testingViewModel.launchRegister() }
        binding.authButton.setOnClickListener{ testingViewModel.launchAuthentication() }
        binding.testButton.setOnClickListener{ testingViewModel.launchTestCommandSN() }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}