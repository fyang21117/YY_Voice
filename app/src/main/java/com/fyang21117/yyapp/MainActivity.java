package com.fyang21117.yyapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.fyang21117.yyapp.floatwindow.FloatWindowService;
import com.fyang21117.yyapp.lbs.BNaviMainActivity;
import com.fyang21117.yyapp.lbs.LocationApplication;
import com.fyang21117.yyapp.lbs.LocationService;
import com.fyang21117.yyapp.movie.Movie;
import com.fyang21117.yyapp.movie.MovieActivity;
import com.fyang21117.yyapp.movie.MovieAdapter;
import com.fyang21117.yyapp.movie.MovieInterface;
import com.fyang21117.yyapp.voice.FucUtil;
import com.fyang21117.yyapp.voice.IatSettings;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ContactManager;
import com.iflytek.sunflower.FlowerCollector;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.functions.Action1;

import com.fyang21117.yyapp.retrofit.PostRequest_Interface;
import com.fyang21117.yyapp.retrofit.Translation1;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static String TAG = MainActivity.class.getSimpleName();
    public static SpeechRecognizer mAsr;
    public static HashMap<String, String> hashMap = new LinkedHashMap<>();
    public static String mEngineType = SpeechConstant.TYPE_CLOUD;

    public  TextView quesTxt;
    public  TextView ansTxt;
    public static Toast mToast;
    public static SharedPreferences mSharedPreferences;
    public static boolean mTranslateEnable = true;

    // 语音合成对象
    public static SpeechSynthesizer mTts;
    public static String voicer = "vixy";           // 默认发音人
    public static String keywords = "";               //关键词指令
    public static int mPercentForBuffering = 0;
    public static int mPercentForPlaying = 0;
    public static int ret = 0;
    public static int mVolume = 0;                    //音量设置
    public static int flag = 0;                    //0,1

//    //获取系统时间
//    public static Calendar calendar = Calendar.getInstance();
//    public static int hour = calendar.get(Calendar.HOUR_OF_DAY);//0--12
//    public static long time = System.currentTimeMillis();
//    public static Date date = new Date(time);
//    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日EEEE");
    public static Random random = new Random();
    public static ListAdapter listAdapter;
    public  ListView appsView;
    public static Intent actIntent;
    public static PackageManager packageManager;

    //获取系统位置
    public static LocationService locationService;
    public  TextView initResult;
    public static double startLat,startLoc;
    public static List<String> phonenamelist = new ArrayList<>();
    public static List<String> phonenumList = new ArrayList<>();

    public static String[] answords = new String[]{
            "麻烦您慢点说，我再确认一遍指令"};
    public static String[] words = new String[]{
            "请问有什么需要帮忙呢？\n您可以说：“翻译一下”",
            "请问有什么需要帮忙呢？\n您可以说：“发送短信”",
            "请问有什么需要帮忙呢？\n您可以说：“拨打电话”",
            "请问有什么需要帮忙呢？\n您可以说：“搜索电影”",
            "请问有什么需要帮忙呢？\n您可以说：“导航到公司”",
            "请问有什么需要帮忙呢？\n您可以说：“打开手机微信”"};

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
    private RecyclerView movieView;
    private int currentVolume;
    private int maxVolume;
    private int minVolume;
    private  AudioManager audioManager;
     Context context;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @SuppressLint("ShowToast")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5c0881d0");
        packageManager = this.getPackageManager();

        mAsr = SpeechRecognizer.createRecognizer(MainActivity.this, mInitListener);
        mTts = SpeechSynthesizer.createSynthesizer(MainActivity.this, mTtsInitListener);
        mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME, Activity.MODE_PRIVATE);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        //******** 页面初始化********
        quesTxt = findViewById(R.id.question_txt);
        ansTxt = findViewById(R.id.ans_txt);
        appsView = findViewById(R.id.appsView);
        findViewById(R.id.yy_start).setOnClickListener(MainActivity.this);
        findViewById(R.id.yy_help).setOnClickListener(MainActivity.this);
        findViewById(R.id.media_actions).setOnClickListener(MainActivity.this);
        findViewById(R.id.movie_actions).setOnClickListener(MainActivity.this);
        findViewById(R.id.Navi_actions).setOnClickListener(MainActivity.this);

        movieView = findViewById(R.id.movieView);
        videoView = findViewById(R.id.videoView);
        movieView.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);
        appsView.setVisibility(View.GONE);

        //******** 定位初始化********
        initResult = findViewById(R.id.location_txt);
        initResult.setMovementMethod(ScrollingMovementMethod.getInstance());

        setParam();
        requestPermissions();
        contactUpload();
        welcome_words();
        loadApps();

        //MediaPlayer
//        initMediaPlayer();
//        initVideoPlayer();
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
            File file = new File(Environment.getExternalStorageDirectory(),musics[musicNum]);
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if (null == mAsr || null == mTts) {
            showTip("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            return;
        }
        setParam();
        switch (view.getId()) {
            case R.id.yy_start: {
                FlowerCollector.onEvent(MainActivity.this, "iat_recognize");
                quesTxt.setText(null);
                hashMap.clear();
                keywords = "";
                actIntent = null;
                ret = mAsr.startListening(mRecognizerListener);
                if (ret != ErrorCode.SUCCESS)
                    showTip("听写失败,错误码：" + ret);
                else
                    showTip(getString(R.string.text_begin));
            }
            break;

            case R.id.yy_help: {
                flag++;
                if(flag%2 == 1){
                    initResult.setVisibility(View.GONE);
                    quesTxt.setVisibility(View.GONE);
                    ansTxt.setVisibility(View.GONE);
                    movieView.setVisibility(View.GONE);
                    videoView.setVisibility(View.GONE);
                    appsView.setVisibility(View.VISIBLE);
                }
                else {
                    initResult.setVisibility(View.VISIBLE);
                    quesTxt.setVisibility(View.VISIBLE);
                    ansTxt.setVisibility(View.VISIBLE);
                    movieView.setVisibility(View.GONE);
                    videoView.setVisibility(View.GONE);
                    appsView.setVisibility(View.GONE);
                    welcome_words();
                }
            }
            break;

            case R.id.media_actions:{
                MediaActivity.actionStart(this);
                break;
            }
            case R.id.Navi_actions:{
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putDouble("startLat",startLat);
                bundle.putDouble("startLon",startLoc);
//                bundle.putString("endPt",keywords.substring(3));
                intent.setClass(MainActivity.this,BNaviMainActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            }
            case R.id.movie_actions:{
                MovieActivity.actionStart(MainActivity.this);
                break;
            }
            default:
                break;
        }
    }

    private void contactUpload() {
        ContactManager mgr = ContactManager.createManager(MainActivity.this, mContactListener);
        mgr.asyncQueryAllContactsName();
        String contents = FucUtil.readFile(MainActivity.this, "userwords", "utf-8");
        mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        ret = mAsr.updateLexicon("userword", contents, mLexiconListener);
        if (ret != ErrorCode.SUCCESS)
            showTip("上传热词失败,错误码：" + ret);

        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String num = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phonenamelist.add(name);
                phonenumList.add(num);
            }
            cursor.close();
        }
    }

    private void welcome_words() {
        int r = random.nextInt(6);
        String string = words[r];
        quesTxt.setText(string);
    }

    final List<HelpItem> itemList = new ArrayList<>();

    private void help_page() {
        appsView = findViewById(R.id.appsView);
        appsView.setDivider(new ColorDrawable(Color.BLACK));
        appsView.setDividerHeight(5);
        new Thread() {//new Runnable()
            public void run() {
                try {
                    //192.168.10.226;www.fyang21117.com"
                    String path = "http://192.168.10.226/yy_voice/items.xml";
                    HttpURLConnection conn;
                    URL url = new URL(path);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(false);
                    conn.setDoInput(true);
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.setRequestProperty("Content-type", "application/json");
                    conn.setInstanceFollowRedirects(false);//必须设置false，否则会自动redirect到重定向后的地址
                    conn.connect();
                    if (conn.getResponseCode() == 200) {
                        InputStream is = conn.getInputStream();//获取输入流
                        XmlPullParser xmlPullParser = Xml.newPullParser();//获取解析器
                        xmlPullParser.setInput(is, "utf-8");//设置输入流和编码
                        int eventType = xmlPullParser.getEventType();
                        String name = "";
                        String info = "";
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            String nodeName = xmlPullParser.getName();
                            switch (eventType) {
                                case XmlPullParser.START_TAG: {
                                    if ("name".equals(nodeName)) {
                                        name = xmlPullParser.nextText();
                                    } else if ("info".equals(nodeName)) {
                                        info = xmlPullParser.nextText();
                                    }
                                    break;
                                }

                                case XmlPullParser.END_TAG: {
                                    if ("item".equals(nodeName)) {
                                        HelpItem item = new HelpItem();
                                        item.setName(name);
                                        item.setInfo(info);
                                        item.setImage_photo(R.mipmap.ic_launcher);
                                        itemList.add(item);
                                    }
                                    break;
                                }
                                default:
                                    break;
                            }
                            eventType = xmlPullParser.next();
                        }
                        is.close();
                    } else
                        Log.e(TAG, "***********conn.getResponseCode() is" + conn.getResponseCode());

                    listAdapter = new MyAdapter(itemList, MainActivity.this);
                    appsView.setAdapter(listAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.e(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    private LexiconListener mLexiconListener = new LexiconListener() {
        /**上传联系人/词表监听器。*/
        @Override
        public void onLexiconUpdated(String lexiconId, SpeechError error) {
            if (error != null) {
                showTip(error.toString());
            }
        }
    };

    public RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onBeginOfSpeech() {
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            if (mTranslateEnable && error.getErrorCode() == 14002) {
                showTip(error.getPlainDescription(true) + "\n请确认是否已开通翻译功能");
            } else {
                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEndOfSpeech() {
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.e(TAG, results.getResultString());
            if (mTranslateEnable) {
                printTransResult(results);
            } else {
                printResult(results);
            }
            keywords = quesTxt.getText().toString();
            Intent appintent = new Intent(Intent.ACTION_MAIN, null);
            appintent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> applist = getPackageManager().queryIntentActivities(appintent, 0);
            int code;

            if (isLast) {
                FlowerCollector.onEvent(MainActivity.this, "iat_recognize");
                for (int i = 0; i < applist.size(); i++) {
                    ResolveInfo info = applist.get(i);
                    String packageName = info.activityInfo.packageName;
                    CharSequence Appname = info.activityInfo.loadLabel(getPackageManager());
                    if (keywords.contains(Appname.toString())) {
                        code = mTts.startSpeaking(Appname.toString() + "已打开", mTtsListener);
                        if (code != ErrorCode.SUCCESS) {
                            showTip("语音合成失败,错误码: " + code);
                        }
                        actIntent = packageManager.getLaunchIntentForPackage(packageName);
                        startActivity(actIntent);
                    }
                }

                for (int j = 0; j < phonenamelist.size(); j++) {
                    String s = phonenamelist.get(j);
                    if (keywords.contains(s) && keywords.contains("打电话")) {
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        Uri data = Uri.parse("tel:" + phonenumList.get(j));
                        intent.setData(data);
                        startActivity(intent);
                    } else if (keywords.contains(s) && keywords.contains("发短信")) {
                        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                        sendIntent.setData(Uri.parse("smsto:" + phonenumList.get(j)));
                        sendIntent.putExtra("sms_body", "【语音短信】：" + keywords);
                        startActivity(sendIntent);
                    }
                }

                if(keywords.contains("调大音量")){
                    if(currentVolume <maxVolume) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume++, AudioManager.FLAG_PLAY_SOUND);
                        showTip("当前音量："+currentVolume);
                    } else
                        showTip("音量已经调到最大");
                }

                if(keywords.contains("调小音量")){
                    if(currentVolume >minVolume) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume--, AudioManager.FLAG_PLAY_SOUND);
                        showTip("当前音量："+currentVolume);
                    }else
                        showTip("已经调到静音");
                }

                if (keywords.contains("翻译")) {
                    //                   postRequest(keywords);
                    postRequest(keywords.substring(2));
                }
                if (keywords.contains("电影")) {
                    //getTopMovies();
//                    MovieActivity.actionStart(MainActivity.this);
                    Intent intent = new Intent(MainActivity.this,MovieActivity.class);
                    startActivity(intent);
                }
                if (keywords.contains("导航到")) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putDouble("startLat",startLat);
                    bundle.putDouble("startLon",startLoc);
                    bundle.putString("endPt",keywords.substring(3));
                    intent.setClass(MainActivity.this,BNaviMainActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    //BNaviMainActivity.ActionStart(MainActivity.this);
                }
                if(keywords.contains("打开音乐"))
                    initMediaPlayer();
                else if(keywords.contains("打开视频"))
                    initVideoPlayer();

//                if(keywords.contains("打开音乐") || keywords.contains("打开视频")){
//                    Intent intent = new Intent(MainActivity.this, MediaActivity.class);
//                    Bundle bundle = new Bundle();
//                    bundle.putString("keywords",keywords);
//                    intent.putExtras(bundle);
//                    startActivity(intent);
//                    //MediaActivity.actionStart(MainActivity.this);
//                }
                if(keywords.contains("音乐")){
                    if(keywords.contains("播放")){
                        if(!mediaPlayer.isPlaying())
                            mediaPlayer.start();
                    }
                    if(keywords.contains("暂停")){
                        if(mediaPlayer.isPlaying())
                            mediaPlayer.pause();
                    }
                    if(keywords.contains("停止")){
                        if(mediaPlayer.isPlaying()){
                            mediaPlayer.seekTo(0);
                            mediaPlayer.pause();
//                            mediaPlayer.reset();
//                            mediaPlayer.release();
                        }
                    }
                    if(keywords.contains("下一首")){
                        if(++musicNum>1)
                            musicNum=0;
                        File file = new File(Environment.getExternalStorageDirectory(),musics[musicNum]);
                        try {
                            mediaPlayer.setDataSource(file.getPath());
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(keywords.contains("视频")){
                    initResult.setVisibility(View.GONE);
                    quesTxt.setVisibility(View.GONE);
                    ansTxt.setVisibility(View.GONE);
                    movieView.setVisibility(View.GONE);
                    videoView.setVisibility(View.VISIBLE);
                    appsView.setVisibility(View.GONE);
                    if(keywords.contains("播放")){
                        if(!videoView.isPlaying())
                            videoView.start();
                    }
                    if(keywords.contains("暂停")){
                        if(videoView.isPlaying())
                            videoView.pause();
                    }
                    if(keywords.contains("重播")){
                        if(videoView.isPlaying()){
                            videoView.seekTo(0);
                            videoView.pause();
                            videoView.resume();
                        }
                    }
                    if(keywords.contains("下一个")){
                        if (videoView.isPlaying()) {
                            videoView.stopPlayback();
                        }
                        initVideoPlayer();
                        videoView.start();
                    }
                }

            } else {
                mTts.startSpeaking(answords[0], mTtsListener);
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            mVolume = volume;
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        hashMap.put(sn, text);

        StringBuilder resultBuffer = new StringBuilder();
        for (String key : hashMap.keySet()) {
            resultBuffer.append(hashMap.get(key));
        }
        quesTxt.setText(resultBuffer.toString());
    }

    private ContactManager.ContactListener mContactListener = new ContactManager.ContactListener() {
        @Override
        public void onContactQueryFinish(final String contactInfos, boolean changeFlag) {
            mAsr.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
            ret = mAsr.updateLexicon("contact", contactInfos, mLexiconListener);
            if (ret != ErrorCode.SUCCESS) {
                showTip("上传联系人失败：" + ret);
            }
        }
    };

    public static void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    public static void setParam() {
        mAsr.setParameter(SpeechConstant.PARAMS, null);
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");
        mTranslateEnable = mSharedPreferences.getBoolean(String.valueOf(R.string.pref_key_translate), false);
        if (mTranslateEnable) {
            Log.i(TAG, "translate enable");
            mAsr.setParameter(SpeechConstant.ASR_SCH, "1");
            mAsr.setParameter(SpeechConstant.ADD_CAP, "translate");
            mAsr.setParameter(SpeechConstant.TRS_SRC, "its");
        }
        String lag = mSharedPreferences.getString("iat_language_preference", "mandarin");
        assert lag != null;
        if (lag.equals("en_us")) {
            mAsr.setParameter(SpeechConstant.LANGUAGE, "en_us");
            mAsr.setParameter(SpeechConstant.ACCENT, null);
            if (mTranslateEnable) {
                mAsr.setParameter(SpeechConstant.ORI_LANG, "en");
                mAsr.setParameter(SpeechConstant.TRANS_LANG, "cn");
            }
        } else {
            mAsr.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            mAsr.setParameter(SpeechConstant.ACCENT, lag);
            if (mTranslateEnable) {
                mAsr.setParameter(SpeechConstant.ORI_LANG, "cn");
                mAsr.setParameter(SpeechConstant.TRANS_LANG, "en");
            }
        }
        mAsr.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
        mAsr.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
        mAsr.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "0"));
        mAsr.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mAsr.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/yyapp.wav");

        /*TTS参数设置***/
        mTts.setParameter(SpeechConstant.PARAMS, null);
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY, "1");
            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
            mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString("speed_preference", "50"));
            mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString("pitch_preference", "50"));
            mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString("volume_preference", "50"));
        } else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
        }
        mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString("stream_preference", "3"));
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "pcm");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.pcm");
    }

    @SuppressLint("SetTextI18n")
    private void printTransResult(RecognizerResult results) {
        String trans = JsonParser.parseTransResult(results.getResultString(), "dst");
        String oris = JsonParser.parseTransResult(results.getResultString(), "src");
        if (TextUtils.isEmpty(trans) || TextUtils.isEmpty(oris)) {
            showTip("解析结果失败，请确认是否已开通翻译功能。");
        } else {
            quesTxt.setText("原始语言:\n" + oris + "\n目标语言:\n" + trans);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mAsr) {
            mAsr.cancel();
            mAsr.destroy();
        }
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if(videoView != null){
            videoView.suspend();
        }
    }

    @Override
    protected void onResume() {
        FlowerCollector.onResume(MainActivity.this);
        FlowerCollector.onPageStart(TAG);
        super.onResume();
    }

    @Override
    protected void onPause() {
        FlowerCollector.onPageEnd(TAG);
        FlowerCollector.onPause(MainActivity.this);
        super.onPause();
    }

    //******** 初始化监听。********
    public static InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码：" + code);
            }
        }
    };

    //******** tts合成回调监听。********
    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            showTip("开始播放");
        }

        @Override
        public void onSpeakPaused() {
            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            mPercentForBuffering = percent;
            showTip(String.format(getString(R.string.tts_toast_format), mPercentForBuffering,
                    mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            mPercentForPlaying = percent;
            showTip(String.format(getString(R.string.tts_toast_format), mPercentForBuffering,
                    mPercentForPlaying));

            SpannableStringBuilder style = new SpannableStringBuilder(keywords);
            Log.e(TAG, "beginPos = " + beginPos + "  endPos = " + endPos);
            style.setSpan(new BackgroundColorSpan(Color.RED), beginPos, endPos,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ((TextView) findViewById(R.id.question_txt)).setText(style);
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                showTip("播放完成");
            } else if (error != null) {
                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
                byte[] buf = obj.getByteArray(SpeechEvent.KEY_EVENT_TTS_BUFFER);
                Log.e("MscSpeechLog", "buf is =" + buf);
            }
        }
    };

    @TargetApi(23)
    private void requestPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.CAMERA,
                Manifest.permission.INTERNET,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                Manifest.permission.VIBRATE,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        List<String> mPermissionList = new ArrayList<>();
        mPermissionList.clear();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        }
        if (mPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(this, permissions, 10000);
        } else {
            Log.e("getPermissions() >>>", "已经授权");
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onStop() {
        locationService.unregisterListener(mListener);
        locationService.stop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationService = ((LocationApplication) getApplication()).locationService;
        locationService.registerListener(mListener);
        int type = getIntent().getIntExtra("from", 0);
        if (type == 0) {
            locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        } else if (type == 1) {
            locationService.setLocationOption(locationService.getOption());
        }
        locationService.start();// 定位SDK
    }

    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                StringBuilder sb = new StringBuilder(200);
                sb.append("time : ");
                sb.append(location.getTime());
                sb.append("\nlatitude : ");// 纬度
                sb.append(location.getLatitude());
                sb.append("\nlontitude : ");// 经度
                sb.append(location.getLongitude());

                startLat = location.getLatitude();
                startLoc = location.getLongitude();
/*              sb.append("\nprovince:");
                sb.append(location.getProvince());
                sb.append("\ncity : ");// 城市
                sb.append(location.getCity());
                sb.append("\ndistrict : ");// 区
                sb.append(location.getDistrict());
                sb.append("\nstreet : ");// 街道
                sb.append(location.getStreet());*/
                sb.append("\nlocation: ");// 地址信息
                sb.append(location.getAddrStr());
                sb.append("\nlocationdescribe: ");
                sb.append(location.getLocationDescribe());// 位置语义化信息
                logMsg(sb.toString());
                //initResult.setText(sb.toString());

            }
        }
    };

    public void logMsg(String str) {
        final String s = str;
        try {
            if (initResult != null){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        initResult.post(new Runnable() {
                            @Override
                            public void run() {
                                initResult.setText(s);
                            }
                        });

                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    final List<HelpItem> appNamesList = new ArrayList<>();

    private void loadApps() {
        appsView = findViewById(R.id.appsView);
        appsView.setDivider(new ColorDrawable(Color.BLACK));
        appsView.setDividerHeight(5);
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = getPackageManager().queryIntentActivities(intent, 0);
        for (int i = 0; i < apps.size(); i++) {
            HelpItem helpItem = new HelpItem();
            ResolveInfo info = apps.get(i);
            String packageName = info.activityInfo.packageName;
            CharSequence Actname = info.activityInfo.name;
            CharSequence Appname = info.activityInfo.loadLabel(getPackageManager());

            helpItem.setPackagename(packageName);
            helpItem.setActname(Actname);
            helpItem.setAppname(Appname);
            helpItem.setImage_photo(R.mipmap.ic_launcher);
            appNamesList.add(helpItem);
        }
        listAdapter = new MyAdapter(appNamesList, MainActivity.this);
        appsView.setAdapter(listAdapter);
    }

    //floatwindow
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_float: {
                Intent intent = new Intent(MainActivity.this, FloatWindowService.class);
                startService(intent);
                finish();
            }
            return true;
            case R.id.action_about:
                Toast.makeText(this, "页面完善中", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void postRequest(String questionwords) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fanyi.youdao.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        final PostRequest_Interface request = retrofit.create(PostRequest_Interface.class);

        Call<Translation1> call = request.getCall(questionwords);

        call.enqueue(new Callback<Translation1>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<Translation1> call, @NonNull Response<Translation1> response) {
                if (response.body() != null) {
                    System.out.println("翻译是：" + response.body().getTranslateResult().get(0).get(0).getTgt());
                    ansTxt.setText("翻译结果：" + response.body().getTranslateResult().get(0).get(0).getTgt());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Translation1> call, @NonNull Throwable throwable) {
                ansTxt.setText("请求失败");
                System.out.println(throwable.getMessage());
            }
        });
    }


    private void getTopMovies() {
        MovieInterface mMovieInterface = new MovieInterface();
        movieView.addItemDecoration(new MovieDecoration());

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        movieView.setLayoutManager(manager);

        final MovieAdapter mMovieAdapter = new MovieAdapter();
        movieView.setAdapter(mMovieAdapter);

        findViewById(R.id.location_txt).setVisibility(View.GONE);
        findViewById(R.id.question_txt).setVisibility(View.GONE);
        findViewById(R.id.ans_txt).setVisibility(View.GONE);
        movieView.setVisibility(View.VISIBLE);

        mMovieInterface.getMovie(0, 25).subscribe(new Action1<List<Movie>>() {
            @Override
            public void call(List<Movie> movies) {
                mMovieAdapter.setMovies(movies);
                mMovieAdapter.notifyDataSetChanged();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.e("TAG", "error message:" + throwable.getMessage());
            }
        });
    }

    public static class MovieDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.set(0, 0, 0, 20);
        }
    }
}
