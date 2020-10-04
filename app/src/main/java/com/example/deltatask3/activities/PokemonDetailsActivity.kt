package com.example.deltatask3.activities

import android.os.Bundle
import android.transition.Explode
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.deltatask3.R
import com.example.deltatask3.api.PokemonApi
import com.example.deltatask3.databinding.ActivityPokemonDetailsBinding
import com.example.deltatask3.firstLetterToUppercase
import com.example.deltatask3.models.EvolutionChain
import com.example.deltatask3.models.Pokemon
import com.example.deltatask3.models.PokemonId
import com.example.deltatask3.models.PokemonSpecies
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PokemonDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPokemonDetailsBinding

    @Inject
    lateinit var pokemonApi: PokemonApi

    private var id = 0
    private lateinit var evolutionChainURL: String
    private lateinit var pokemon: Pokemon
    private val names = ArrayList<String>()
    private val urls = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPokemonDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        window.sharedElementEnterTransition.duration = 500
        window.enterTransition = Explode()
        window.enterTransition.duration = 500
        val intent = intent
        val pokemonJson = intent.getStringExtra("pokemonJson")
        pokemon = Gson().fromJson(pokemonJson, Pokemon::class.java)
        id = pokemon.id
        val name = pokemon.name
        val url = pokemon.sprites.front_default
        getTypes()
        getStats()
        getSpecies()
        binding.tvDetailsId.text = "ID:$id"
        binding.tvDetailsName.text = firstLetterToUppercase(name)
        Picasso.get()
                .load(url)
                .into(binding.ivDetailsPokemon)
    }

    private fun getTypes() {
        binding.tvType1.text = firstLetterToUppercase(pokemon.types[0].type.name)
        if (pokemon.types.size == 1) {
            binding.tvTypes.text = "Type"
            binding.tvType2.visibility = View.GONE
        } else binding.tvType2.text = firstLetterToUppercase(pokemon.types[1].type.name)
    }

    private fun getStats() {
        binding.tvSpeed.text = "Speed : " + pokemon.stats[0].base_stat
        binding.tvHp.text = "Hp : " + pokemon.stats[5].base_stat
        binding.tvAttack.text = "Attack : " + pokemon.stats[4].base_stat
        binding.tvDefense.text = "Defense : " + pokemon.stats[3].base_stat
        binding.tvSpAttack.text = "Sp. Attack : " + pokemon.stats[2].base_stat
        binding.tvSpDefense.text = "Sp. Defense : " + pokemon.stats[1].base_stat
    }

    private fun getSpecies() {
        val call = pokemonApi.getSpecies(id)
        call.enqueue(object : Callback<PokemonSpecies> {
            override fun onResponse(call: Call<PokemonSpecies>, response: Response<PokemonSpecies>) {
                if (!response.isSuccessful) {
                    Log.i(TAG, "onResponse: $response")
                    return
                }
                evolutionChainURL = response.body()!!.evolution_chain.url
                getEvolutionChain()
            }

            override fun onFailure(call: Call<PokemonSpecies>, t: Throwable) {
                Log.i(TAG, "getSpecies failed")
            }
        })
    }

    private fun getEvolutionChain() {
        val call = pokemonApi.getEvolutionChain(evolutionChainURL.substring(42))
        call.enqueue(object : Callback<EvolutionChain?> {
            override fun onResponse(call: Call<EvolutionChain?>, response: Response<EvolutionChain?>) {
                if (!response.isSuccessful) {
                    Log.i(TAG, "onResponse: $response")
                    return
                }
                val evolutionChain = response.body()
                val chainLink = evolutionChain!!.chain
                val name1 = chainLink.species.name
                names.add(name1)
                urls.add("")
                if (chainLink.evolves_to.size != 0) {
                    val name2 = chainLink.evolves_to[0].species.name
                    names.add(name2)
                    urls.add("")
                    if (chainLink.evolves_to[0].evolves_to.size != 0) {
                        val name3 = chainLink.evolves_to[0].evolves_to[0].species.name
                        names.add(name3)
                        urls.add("")
                    }
                }
                when (names.size) {
                    3 -> binding.evolutionCard3.visibility = View.VISIBLE
                    2 -> binding.evolutionCard2.visibility = View.VISIBLE
                    1 -> binding.evolutionCard1.visibility = View.VISIBLE
                }
                loadEvolutionChain()
            }

            override fun onFailure(call: Call<EvolutionChain?>, t: Throwable) {
                Log.i(TAG, "t=" + t.localizedMessage)
            }
        })
    }

    private fun loadEvolutionChain() {
        for (string in names) {
            val call = pokemonApi.getPokemonId(string)
            call.enqueue(object : Callback<PokemonId> {
                override fun onResponse(call: Call<PokemonId>, response: Response<PokemonId>) {
                    if (!response.isSuccessful) {
                        Log.i(TAG, "onResponse: $response")
                        return
                    }
                    urls[names.indexOf(string)] = getSpriteURLFromID(response.body()!!.id)
                    loadSprites(names.indexOf(string))
                }

                override fun onFailure(call: Call<PokemonId>, t: Throwable) {
                    Log.i(TAG, "t=" + t.localizedMessage)
                }
            })
        }
    }

    private fun loadSprites(index: Int) {
        when (names.size) {
            3 -> when (index) {
                0 -> {
                    Picasso.get().load(urls[0]).placeholder(R.drawable.placeholder_image).into(binding.iv31)
                    binding.tv31.text = firstLetterToUppercase(names[0])
                }
                1 -> {
                    Picasso.get().load(urls[1]).placeholder(R.drawable.placeholder_image).into(binding.iv32)
                    binding.tv32.text = firstLetterToUppercase(names[1])
                }
                2 -> {
                    Picasso.get().load(urls[2]).placeholder(R.drawable.placeholder_image).into(binding.iv33)
                    binding.tv33.text = firstLetterToUppercase(names[2])
                }
            }
            2 -> when (index) {
                0 -> {
                    Picasso.get().load(urls[0]).placeholder(R.drawable.placeholder_image).into(binding.iv21)
                    binding.tv21.text = firstLetterToUppercase(names[0])
                }
                1 -> {
                    Picasso.get().load(urls[1]).placeholder(R.drawable.placeholder_image).into(binding.iv22)
                    binding.tv22.text = firstLetterToUppercase(names[1])
                }
            }
            1 -> {
                Picasso.get().load(urls[0]).placeholder(R.drawable.placeholder_image).into(binding.iv11)
                binding.tv11.text = firstLetterToUppercase(names[0])
            }
        }
    }

    private fun getSpriteURLFromID(id: Int): String {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
    }

    companion object {
        private const val TAG = "PokemonDetailsActivity"
    }
}