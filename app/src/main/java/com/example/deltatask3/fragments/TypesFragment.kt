package com.example.deltatask3.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.deltatask3.activities.PokemonsActivity
import com.example.deltatask3.databinding.FragmentTypesBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class TypesFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentTypesBinding? = null
    private val binding get() = _binding!!

    private var types: ArrayList<String>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentTypesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButtons()
        initTypes()
    }

    private fun initButtons() {
        binding.button.setOnClickListener { v: View -> onClick(v) }
        binding.button2.setOnClickListener { v: View -> onClick(v) }
        binding.button3.setOnClickListener { v: View -> onClick(v) }
        binding.button4.setOnClickListener { v: View -> onClick(v) }
        binding.button5.setOnClickListener { v: View -> onClick(v) }
        binding.button6.setOnClickListener { v: View -> onClick(v) }
        binding.button7.setOnClickListener { v: View -> onClick(v) }
        binding.button8.setOnClickListener { v: View -> onClick(v) }
        binding.button9.setOnClickListener { v: View -> onClick(v) }
        binding.button10.setOnClickListener { v: View -> onClick(v) }
        binding.button11.setOnClickListener { v: View -> onClick(v) }
        binding.button12.setOnClickListener { v: View -> onClick(v) }
        binding.button13.setOnClickListener { v: View -> onClick(v) }
        binding.button14.setOnClickListener { v: View -> onClick(v) }
        binding.button15.setOnClickListener { v: View -> onClick(v) }
        binding.button16.setOnClickListener { v: View -> onClick(v) }
        binding.button17.setOnClickListener { v: View -> onClick(v) }
        binding.button18.setOnClickListener { v: View -> onClick(v) }
    }

    private fun initTypes() {
        types = ArrayList()
        types!!.add("Normal")
        types!!.add("Fighting")
        types!!.add("Flying")
        types!!.add("Poison")
        types!!.add("Ground")
        types!!.add("Rock")
        types!!.add("Bug")
        types!!.add("Ghost")
        types!!.add("Steel")
        types!!.add("Fire")
        types!!.add("Water")
        types!!.add("Grass")
        types!!.add("Electric")
        types!!.add("Psychic")
        types!!.add("Ice")
        types!!.add("Dragon")
        types!!.add("Dark")
        types!!.add("Fairy")
    }

    override fun onClick(v: View) {
        val intent = Intent(activity, PokemonsActivity::class.java)
        val TYPES = 23
        intent.putExtra("mode", TYPES)
        intent.putExtra("typeID", v.tag.toString().toInt())
        intent.putExtra("typeName", types!![v.tag.toString().toInt() - 1])
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}