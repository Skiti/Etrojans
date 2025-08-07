package com.malicious.scootertoolkit.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.malicious.scootertoolkit.R
import com.malicious.scootertoolkit.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel.text.observe(viewLifecycleOwner) {
            binding.textHome.text = it
        }

        context?.let { homeViewModel.setDeviceAdapter(it) }
        binding.deviceList.adapter = homeViewModel.getdeviceList()

        binding.deviceList.onItemClickListener =
            AdapterView.OnItemClickListener { _, view, position, _ ->
                if (homeViewModel.selectDevice(view, position) == true){
                    this.childFragmentManager.findFragmentById(R.id.attacks_fragment)?.let {
                        this.childFragmentManager.beginTransaction().replace(R.id.attacks_fragment,
                            it
                        ).commit()
                    }
                }
            }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}