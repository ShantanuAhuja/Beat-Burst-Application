package com.example.musicplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.musicplayer.databinding.ActivityFavouriteBinding
import com.example.musicplayer.databinding.ActivityPlayListBinding

class PlayListActivity : AppCompatActivity() {
    lateinit var playListActivityBinding: ActivityPlayListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playListActivityBinding= ActivityPlayListBinding.inflate(layoutInflater)
        setTheme(R.style.coolPink)
        setContentView(playListActivityBinding.root)

        playListActivityBinding.backBtnFA.setOnClickListener {
            finish()
        }
    }
}