package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.databinding.ActivityFavouriteBinding
import com.example.musicplayer.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity(),ServiceConnection,MediaPlayer.OnCompletionListener {


    companion object{
        lateinit var musicListPA:ArrayList<Music>
        var songPosition:Int=0;
        var isPlaying:Boolean=false
        var musicService:MusicService ?=null
        @SuppressLint("StaticFieldLeak")
        lateinit var playerActivityBinding: ActivityPlayerBinding
        var repeat:Boolean=false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerActivityBinding=ActivityPlayerBinding.inflate(layoutInflater)
        setTheme(R.style.coolPink)
        setContentView(playerActivityBinding.root)

        // For Starting Service
        val intent= Intent(this,MusicService::class.java)
        bindService(intent,this, BIND_AUTO_CREATE)
        startService(intent)
        initializeLayout()

        playerActivityBinding.playPauseBtnPA.setOnClickListener {
            if(isPlaying) pauseMusic()
            else playMusic()
        }

        playerActivityBinding.nextBtnPA.setOnClickListener{
             prevNextSong(increment = true)
        }

        playerActivityBinding.previousBtnPA.setOnClickListener {
            prevNextSong(increment = false)
        }

        playerActivityBinding.seekBarPA.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) =Unit

            override fun onStopTrackingTouch(p0: SeekBar?) =Unit
        })

        playerActivityBinding.repeatBtnPA.setOnClickListener {
            if (!repeat) {
                repeat = true
                playerActivityBinding.repeatBtnPA.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.purple_500
                    )
                )
            } else {
                repeat = false
                playerActivityBinding.repeatBtnPA.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.cool_pink
                    )
                )

            }
        }

        playerActivityBinding.backBtnPA.setOnClickListener {
            finish()
        }

        playerActivityBinding.shareBtnPA.setOnClickListener {
            val shareIntent=Intent()
            shareIntent.action=Intent.ACTION_SEND
            shareIntent.type="audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent,"Sharing Music File !!"))
        }

        playerActivityBinding.equalizerBtnPA.setOnClickListener {
            try {
                val eqIntent=Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME,baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent,13)
            }catch(e:Exception){
                Toast.makeText(this,"Equaliser not supported", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun setLayout(){
        Glide.with(this@PlayerActivity)
            .load(musicListPA[songPosition].artURI)
            .apply(RequestOptions().placeholder(R.drawable.music_splash_screen).centerCrop())
            .into(playerActivityBinding.songImgPA)
        playerActivityBinding.songNamePA.text= musicListPA[songPosition].title
        if(repeat) playerActivityBinding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
    }

    private fun createMediaPlayer(){
        try{
            if(musicService!!.mediaPlayer==null) musicService!!.mediaPlayer= MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying=true
            playerActivityBinding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            musicService!!.showNotification(R.drawable.pause_icon)
            playerActivityBinding.tvSeekBarStart.text= formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            playerActivityBinding.tvSeekBarEnd.text= formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            playerActivityBinding.seekBarPA.progress=0
            playerActivityBinding.seekBarPA.max=musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
        }catch(e:Exception){
            return
        }
    }

    private fun initializeLayout(){
        songPosition=intent.getIntExtra("index",0)
        when(intent.getStringExtra("class")){
            "MusicAdapter"->{
                musicListPA= ArrayList()
                musicListPA.addAll(MainActivity.musicListMA)
                setLayout()
            }
            "MainActivity"->{
                musicListPA= ArrayList()
                musicListPA.addAll(MainActivity.musicListMA)
                musicListPA.shuffle()
                setLayout()
            }
            "MusicAdapterSearch" ->{
                musicListPA= ArrayList()
                musicListPA.addAll(MainActivity.musicListSearch)
                setLayout()
            }
        }
    }

    private fun playMusic(){
        isPlaying = true
        playerActivityBinding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        musicService!!.mediaPlayer!!.start()
        musicService!!.showNotification(R.drawable.pause_icon)
    }

    private fun pauseMusic(){
        isPlaying = false
        playerActivityBinding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
        musicService!!.mediaPlayer!!.pause()
        musicService!!.showNotification(R.drawable.play_icon)
    }

    private fun prevNextSong(increment: Boolean){
        if(increment)
        {
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
        }
        else{
            setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder= service as MusicService.MyBinder
        musicService=binder.currentService()
        createMediaPlayer()
        musicService!!.seekBarSetUp()


    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService=null
    }

    override fun onCompletion(p0: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        try{
            setLayout()

        }catch(ex:Exception){
             return
        }

    }


}