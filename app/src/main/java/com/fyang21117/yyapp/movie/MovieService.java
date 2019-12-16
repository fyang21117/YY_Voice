package com.fyang21117.yyapp.movie;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by zhouwei on 16/11/16.
 */

public interface MovieService {
    //获取豆瓣Top250 榜单
    // @GET("top250?apikey=0b2bdeda43b5688921839c8ecb20399b")
    @GET("top250")
    Observable<MovieSubject> getTop250(@Query("start") int start, @Query("count") int count);

}
