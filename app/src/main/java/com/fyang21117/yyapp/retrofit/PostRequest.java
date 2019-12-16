//package com.fyang21117.yyapp.retrofit;
//
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.widget.TextView;
//import com.fyang21117.yyapp.R;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import retrofit2.Retrofit;
//import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
//import retrofit2.converter.gson.GsonConverterFactory;
//
///*
//        实现的功能：将 英文 翻译成 中文
//        实现方法：采用Post方法对 有道API 发送网络请求
//        采用 Gson 进行数据解析
//*/
//
//public class PostRequest extends AppCompatActivity {
//
//
//    public TextView resTxt;
//    @Override
//    protected void onCreate(final Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//         resTxt = findViewById(R.id.ans_txt);
//         Bundle bundle = this.getIntent().getExtras();
//         String keywords = bundle.getString("keywords");
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://fanyi.youdao.com/") // 设置 网络请求 Url
//                .addConverterFactory(GsonConverterFactory.create()) //设置使用Gson解析(记得加入依赖)
//                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()) // 支持RxJava平台
//                .build();
//
//        // 步骤5:创建 网络请求接口 的实例
//        final PostRequest_Interface request = retrofit.create(PostRequest_Interface.class);
//
//        //对 发送请求 进行封装(设置需要翻译的内容)
//        //subTxt.setText("I love you");
//        Call<Translation1> call = request.getCall(keywords);
//
//        //步骤6:发送网络请求(异步)
//        call.enqueue(new Callback<Translation1>() {
//            //请求成功时回调
//            @Override
//            public void onResponse(Call<Translation1> call, Response<Translation1> response) {
//                // 请求处理,输出结果
//                // 输出翻译的内容
//                System.out.println("请求成功");
//                System.out.println("翻译是："+ response.body().getTranslateResult().get(0).get(0).getTgt());
//                resTxt.setText("translation:"+response.body().getTranslateResult().get(0).get(0).getTgt());
//            }
//
//            //请求失败时回调
//            @Override
//            public void onFailure(Call<Translation1> call, Throwable throwable) {
//                System.out.println("请求失败");
//                resTxt.setText("请求失败");
//                System.out.println(throwable.getMessage());
//            }
//        });
//    }
//}