package com.fyang21117.yyapp.movie;


import java.util.List;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MovieInterface  {
    private MovieService mMovieService;

    public MovieInterface(){
        mMovieService = RetrofitServiceManager.getInstance().create(MovieService.class);
    }

    protected  <T> Observable<T> observe(Observable<T> observable){
        return observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<Movie>> getMovie(int start, int count){
        return observe(mMovieService.getTop250(start,count))
                .map(new Func1<MovieSubject, List<Movie>>() {
            @Override
            public List<Movie> call(MovieSubject movieSubject) {
                return movieSubject.subjects;
            }
        });
    }

    public interface MovieService{
        //获取豆瓣Top250 榜单
       // @GET("top250")
        @GET("top250?apikey=0b2bdeda43b5688921839c8ecb20399b")
        Observable<MovieSubject> getTop250(@Query("start") int start, @Query("count") int count);
    }

    public static class MovieSubject {// static 20191126
        public int count;
        public int start;
        public int total;
        public List<Movie>  subjects;
        public String title;

    }
}
