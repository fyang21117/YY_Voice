package com.fyang21117.yyapp;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;
import java.io.File;
import java.io.IOException;
import static com.fyang21117.yyapp.MainActivity.showTip;

public class MediaActivity extends AppCompatActivity implements View.OnClickListener{

    public static void actionStart(Context context){
        Intent intent = new Intent(context, MediaActivity.class);
        context.startActivity(intent);
    }

    public static String[] musics = new String[]{
            "dreamitpossible.mp3",
            "variations.mp3"
    };

    public static String[] videos = new String[]{
            "ifloveyou.mp4",
            "waitingforu.mp4"
    };

    public static int musicNum = 0;
    public static int videoNum = 0;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private VideoView videoView;
    private int currentVolume;
    private int maxVolume;
    private int minVolume;
    private AudioManager audioManager;
    Context context;
    File file;
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        setTitle("媒体播放器");
        Button musicPlay = findViewById(R.id.music_play);
        Button musicStop = findViewById(R.id.music_stop);
        Button musicNext = findViewById(R.id.music_next);

        videoView = findViewById(R.id.videoView);
        Button addVolume = findViewById(R.id.media_addVolume);
        Button reduceVolume = findViewById(R.id.media_reduceVolume);

        Button videoPlay = findViewById(R.id.movie_play);
        Button videoStop = findViewById(R.id.movie_stop);
        Button videoNext = findViewById(R.id.movie_next);

        musicPlay.setOnClickListener(this);
        musicStop.setOnClickListener(this);
        musicNext.setOnClickListener(this);
        addVolume.setOnClickListener(this);
        reduceVolume.setOnClickListener(this);

        videoPlay.setOnClickListener(this);
        videoStop.setOnClickListener(this);
        videoNext.setOnClickListener(this);

//        Bundle bundle = this.getIntent().getExtras();
//        String keyword = bundle.getString("keywords");
//        if(keyword.contains("打开音乐"))
//            initMediaPlayer();
//        else if(keyword.contains("打开视频"))
//            initVideoPlayer();

            initMediaPlayer();
            initVideoPlayer();


        context = getApplicationContext();
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//0-15
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        minVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC);
    }

    private void initVideoPlayer() {
        if(videoNum>1)videoNum=0;
        File file = new File(Environment.getExternalStorageDirectory(),videos[videoNum++]);
        videoView.setVideoPath(file.getPath());
        videoView.setMediaController(new android.widget.MediaController(this));
        videoView.start();
    }

    private void initMediaPlayer() {
        try{
            file = new File(Environment.getExternalStorageDirectory(),musics[musicNum]);
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.music_play: {
                if (!mediaPlayer.isPlaying())
                    mediaPlayer.start();
                else
                    mediaPlayer.pause();
                break;
            }

            case R.id.music_stop: {
                mediaPlayer.seekTo(0);
                mediaPlayer.pause();
//                mediaPlayer.reset();
//                mediaPlayer.release();
                break;
            }
            case R.id.music_next: {
                if(++musicNum>1)
                    musicNum=0;
                file = new File(Environment.getExternalStorageDirectory(),musics[musicNum]);
                try {
                    mediaPlayer.setDataSource(file.getPath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            }

            case R.id.movie_play: {
                if (!videoView.isPlaying())
                    videoView.start();
                else
                    videoView.pause();
                break;
            }

            case R.id.movie_stop: {
                    videoView.seekTo(0);
                    videoView.pause();
                    //videoView.resume();
                break;
            }
            case R.id.movie_next: {
                    if (videoView.isPlaying()) {
                        videoView.stopPlayback();
                    }
                    initVideoPlayer();
                    videoView.start();
                    break;
                }
            case R.id.media_addVolume:{
                if(currentVolume <maxVolume) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume++, AudioManager.FLAG_PLAY_SOUND);
                    showTip("当前音量："+currentVolume);
                } else
                    showTip("音量已经调到最大");
                break;
            }
            case R.id.media_reduceVolume:{
                if(currentVolume >minVolume) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume--, AudioManager.FLAG_PLAY_SOUND);
                    showTip("当前音量："+currentVolume);
                }else
                    showTip("已经调到静音");
                break;
            }
            default:break;
        }
    }


}
