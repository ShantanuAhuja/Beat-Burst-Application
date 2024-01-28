package com.example.musicplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.musicplayer.databinding.ActivityFavouriteBinding

class FavouriteActivity : AppCompatActivity() {
      lateinit var favouriteActivityBinding:ActivityFavouriteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favouriteActivityBinding= ActivityFavouriteBinding.inflate(layoutInflater)
        setTheme(R.style.coolPink)
        setContentView(favouriteActivityBinding.root)

        favouriteActivityBinding.backBtnFA.setOnClickListener {
            finish()
        }
    }
}