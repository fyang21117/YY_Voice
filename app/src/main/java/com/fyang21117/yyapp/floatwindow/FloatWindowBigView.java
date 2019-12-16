package com.fyang21117.yyapp.floatwindow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fyang21117.yyapp.JsonParser;
import com.fyang21117.yyapp.R;
import com.fyang21117.yyapp.voice.IatSettings;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.sunflower.FlowerCollector;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static com.fyang21117.yyapp.MainActivity.answords;
import static com.fyang21117.yyapp.MainActivity.mInitListener;
import static com.fyang21117.yyapp.MainActivity.mPercentForBuffering;
import static com.fyang21117.yyapp.MainActivity.mPercentForPlaying;
import static com.fyang21117.yyapp.MainActivity.mSharedPreferences;
import static com.fyang21117.yyapp.MainActivity.mTtsInitListener;
import static com.fyang21117.yyapp.MainActivity.setParam;
import static com.fyang21117.yyapp.MainActivity.showTip;

/**
 * @author fyang21117
 * @since 2019/4/23 20:45
 */
public class FloatWindowBigView extends LinearLayout {

    /**
     * 记录大悬浮窗的宽度、高度
     */
    public static int viewWidth;
    public static int viewHeight;

    private static int statusBarHeight;//记录系统状态栏的高度
    private WindowManager windowManager;//用于更新小悬浮窗的位置
    private WindowManager.LayoutParams mParams;//小悬浮窗的参数

    private float xInScreen;//记录当前手指位置在屏幕上的横坐标值
    private float yInScreen;//记录当前手指位置在屏幕上的纵坐标值

    private float xDownInScreen;//记录手指按下时在屏幕上的横坐标的值
    private float yDownInScreen;//记录手指按下时在屏幕上的纵坐标的值

    private float xInView;//记录手指按下时在小悬浮窗的View上的横坐标的值
    private float yInView;//记录手指按下时在小悬浮窗的View上的纵坐标的值

    /**
     * 语音初始化
     */
    SpeechRecognizer        mYy;
    TextView                mResultText;
    PackageManager          packageManager;
    Context                 ct;
    SpeechSynthesizer       mTts;
    String                  keywords         = "";
    int                     ret              = 0;
    Intent                  actIntent;
    boolean                 mTranslateEnable = true;
    HashMap<String, String> mYyResults       = new LinkedHashMap<>();


    public FloatWindowBigView(final Context context) {
        super(context);
        Log.e("floatwindow","FloatWindowBigView start***********");

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.float_window_big, this);
        View view = findViewById(R.id.big_window_layout);
        viewWidth = view.getLayoutParams().width;
        viewHeight = view.getLayoutParams().height;
        Button yy_float = findViewById(R.id.yy_float);
        ImageButton close = findViewById(R.id.close);
        ImageButton back = findViewById(R.id.back);

        //******** 语音初始化********
        ct = context;
        mResultText =  LayoutInflater.from(context).
                inflate(R.layout.activity_main, this).findViewById(R.id.question_txt);

        mYy = SpeechRecognizer.createRecognizer(context, mInitListener);
        mTts = SpeechSynthesizer.createSynthesizer(context, mTtsInitListener);
        mSharedPreferences = ct.getSharedPreferences(IatSettings.PREFER_NAME,
                Activity.MODE_PRIVATE);
        packageManager = context.getPackageManager();

        if (null == mYy || null == mTts) {
            showTip("创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化");
            return;
        }
        setParam();
        yy_float.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                FlowerCollector.onEvent(context, "iat_recognize");
                mResultText.setText(null);
                mYyResults.clear();
                keywords = "";
                actIntent = null;
                ret = mYy.startListening(mRecognizerListener);
                if (ret != ErrorCode.SUCCESS)
                    showTip("听写失败,错误码：" + ret);
                else
                    showTip("Start speaking");
            }
        });
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击关闭悬浮窗的时候，移除所有悬浮窗，并停止Service
                MyWindowManager.removeBigWindow(context);
                MyWindowManager.removeSmallWindow(context);
                Intent intent = new Intent(getContext(), FloatWindowService.class);
                context.stopService(intent);
            }
        });
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击返回的时候，移除大悬浮窗，创建小悬浮窗
                MyWindowManager.removeBigWindow(context);
                MyWindowManager.createSmallWindow(context);
            }
        });
    }


    /**
     * 听写监听器。通过实现此接口，获取当前识别的状态和结果
     */
    public  RecognizerListener  mRecognizerListener = new RecognizerListener() {
        @Override
        public void onBeginOfSpeech() {showTip("开始说话");}
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
            printResult(results);
            keywords = mResultText.getText().toString();
            Intent appintent = new Intent(Intent.ACTION_MAIN, null);
            appintent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> applist = ct.getPackageManager().queryIntentActivities(appintent, 0);
            int code;

            if (isLast) {
                FlowerCollector.onEvent(ct, "iat_recognize");
                for (int i = 0; i < applist.size(); i++) {
                    ResolveInfo info = applist.get(i);
                    String packageName = info.activityInfo.packageName;
                    CharSequence Appname = info.activityInfo.loadLabel(ct.getPackageManager());
                    if (keywords.contains(Appname.toString())) {
                        code = mTts.startSpeaking(Appname.toString() + "已打开", mTtsListener);
                        if (code != ErrorCode.SUCCESS) {
                            showTip("语音合成失败,错误码: " + code);
                        }
                        actIntent = packageManager.getLaunchIntentForPackage(packageName);
                        ct.startActivity(actIntent);
                    }
                }
            } else {
                mTts.startSpeaking(answords[0], mTtsListener);

            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            //mVolume = volume;
            showTip("当前正在说话，音量大小：" + volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {}
    };


    /**
     * tts合成回调监听
     */
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
            mPercentForBuffering = percent;// 合成进度
            showTip(String.format(ct.getString(R.string.tts_toast_format), mPercentForBuffering,
                    mPercentForPlaying));
        }
        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            mPercentForPlaying = percent;// 播放进度
            showTip(String.format(ct.getString(R.string.tts_toast_format), mPercentForBuffering,
                    mPercentForPlaying));

            SpannableStringBuilder style = new SpannableStringBuilder(keywords);
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
    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mYyResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mYyResults.keySet()) {
            resultBuffer.append(mYyResults.get(key));
        }
        mResultText.setText(resultBuffer.toString());
    }

    /**
     * 悬浮窗触摸事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY() - getStatusBarHeight();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                // 手指移动的时候更新小悬浮窗的位置
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                // 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
                if (xDownInScreen == xInScreen && yDownInScreen == yInScreen) {
                    //openBigWindow();
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 将小悬浮窗的参数传入，用于更新小悬浮窗的位置。
     *
     * @param params 小悬浮窗的参数
     */
    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    /**
     * 更新小悬浮窗在屏幕中的位置。
     */
    private void updateViewPosition() {
        mParams.x = (int) (xInScreen - xInView);
        mParams.y = (int) (yInScreen - yInView);
        windowManager.updateViewLayout(this, mParams);
    }
    /**
     * 用于获取状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     */
    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }
}