package com.malicious.scootertoolkit.ui.attacks

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.malicious.scootertoolkit.R
import com.malicious.scootertoolkit.databinding.FragmentAttacksBinding
import com.malicious.scootertoolkit.xiaomi.RansomPacketsCreator

class AttacksFragment : Fragment() {

    private var _binding: FragmentAttacksBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val attacksViewModel =
                ViewModelProvider(this).get(AttacksViewModel::class.java)

        _binding = FragmentAttacksBinding.inflate(inflater, container, false)
        val root: View = binding.root

        attacksViewModel.text.observe(viewLifecycleOwner) {
            binding.textGallery.text = it
        }
        attacksViewModel.connected.observe(viewLifecycleOwner) {
            binding.ransomwareButton.isEnabled = it
            binding.disconnectButton.isEnabled = it
            binding.disablechargeButton.isEnabled = it
            binding.ransomrestoreButton.isEnabled = it
            binding.restoreButton.isEnabled = it
        }
        binding.ransomwareButton.setOnClickListener{ attacksViewModel.launchAttack(RansomPacketsCreator.Companion.FW.RANSOMWARE) }
        binding.disconnectButton.setOnClickListener{ attacksViewModel.launchAttack(RansomPacketsCreator.Companion.FW.DISCONNECT) }
        binding.disablechargeButton.setOnClickListener{ attacksViewModel.launchAttack(RansomPacketsCreator.Companion.FW.DISABLECHARGE) }
        binding.ransomrestoreButton.setOnClickListener{ attacksViewModel.launchAttack(RansomPacketsCreator.Companion.FW.RANSOM_RECOVER) } //TODO(implement recover ransomware)
        binding.restoreButton.setOnClickListener{ attacksViewModel.launchAttack(RansomPacketsCreator.Companion.FW.RECOVER) }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}