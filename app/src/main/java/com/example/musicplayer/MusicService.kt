package com.example.musicplayer

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat

class MusicService:Service() {
    private var myBinder=MyBinder()
    var mediaPlayer :MediaPlayer?=null
    private lateinit var mediaSession:MediaSessionCompat
    private lateinit var runnable: Runnable

    override fun onBind(p0: Intent?): IBinder? {
        mediaSession=MediaSessionCompat(baseContext,"My Music")
        return myBinder
    }

    inner class MyBinder: Binder(){
          fun currentService(): MusicService {
              return this@MusicService
          }
    }


    @SuppressLint("SuspiciousIndentation")
    fun showNotification(playPauseBtn:Int){

        val prevIntent=Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent= PendingIntent.getBroadcast(baseContext,0,prevIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent=Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent= PendingIntent.getBroadcast(baseContext,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val playIntent=Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent= PendingIntent.getBroadcast(baseContext,0,playIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val exitIntent=Intent(baseContext,NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent= PendingIntent.getBroadcast(baseContext,0,exitIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        // Render image in Notification
        val imageArt= getImgArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        val image=if(imageArt !=null){
            BitmapFactory.decodeByteArray(imageArt,0,imageArt.size)
        }
        else{
            BitmapFactory.decodeResource(resources,R.drawable.music_splash_screen)
        }
        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
            .setSmallIcon(R.drawable.playlist_icon)
            .setLargeIcon(image)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.previous_icon, "Previous", prevPendingIntent)
            .addAction(playPauseBtn, "Play", playPendingIntent)
            .addAction(R.drawable.next_icon, "Next", nextPendingIntent)
            .addAction(R.drawable.exit_icon, "Exit", exitPendingIntent)
            .build()
             startForeground(13,notification)
    }

    fun createMediaPlayer(){
        try{
            if(PlayerActivity.musicService!!.mediaPlayer==null) PlayerActivity.musicService!!.mediaPlayer= MediaPlayer()
            PlayerActivity.musicService!!.mediaPlayer!!.reset()
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()
            PlayerActivity.playerActivityBinding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
            PlayerActivity.playerActivityBinding.tvSeekBarStart.text= formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.playerActivityBinding.tvSeekBarEnd.text= formatDuration(mediaPlayer!!.duration.toLong())
            PlayerActivity.playerActivityBinding.seekBarPA.progress=0
            PlayerActivity.playerActivityBinding.seekBarPA.max= PlayerActivity.musicService!!.mediaPlayer!!.duration
        }catch(e:Exception){
            return
        }
    }

    fun seekBarSetUp(){
      runnable= Runnable {
          PlayerActivity.playerActivityBinding.tvSeekBarStart.text= formatDuration(mediaPlayer!!.currentPosition.toLong())
          PlayerActivity.playerActivityBinding.seekBarPA.progress=mediaPlayer!!.currentPosition
          Handler(Looper.getMainLooper()).postDelayed(runnable,200)
      }
          Handler(Looper.getMainLooper()).postDelayed(runnable,0)
    }

}