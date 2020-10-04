package com.example.deltatask3.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.deltatask3.activities.PokemonsActivity
import com.example.deltatask3.databinding.FragmentRegionsBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class RegionsFragment : Fragment(), View.OnClickListener {

    companion object {
        private val regions: ArrayList<String> = arrayListOf(
                "Kanto",
                "Johto",
                "Hoenn",
                "Sinnoh",
                "Unova",
                "Kalos",
                "Alola",
        )
    }

    private var _binding: FragmentRegionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentRegionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButtons()
    }

    private fun initButtons() {
        binding.btA.setOnClickListener { v: View -> onClick(v) }
        binding.btH.setOnClickListener { v: View -> onClick(v) }
        binding.btJ.setOnClickListener { v: View -> onClick(v) }
        binding.btKl.setOnClickListener { v: View -> onClick(v) }
        binding.btKt.setOnClickListener { v: View -> onClick(v) }
        binding.btS.setOnClickListener { v: View -> onClick(v) }
        binding.btU.setOnClickListener { v: View -> onClick(v) }
    }

    override fun onClick(v: View) {
        val intent = Intent(activity, PokemonsActivity::class.java)
        val REGIONS = 45
        intent.putExtra("mode", REGIONS)
        intent.putExtra("regionID", v.tag.toString().toInt())
        intent.putExtra("regionName", regions[v.tag.toString().toInt()])
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}