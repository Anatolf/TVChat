package com.anatolf.tvchat;

import android.app.Application;
import android.content.SharedPreferences;

import com.anatolf.tvchat.net.OdnoklassnikiService;
import com.anatolf.tvchat.net.VkService;
import com.anatolf.tvchat.ui.main.MainPresenter;
import com.vk.sdk.VKSdk;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.ok.android.sdk.Odnoklassniki;

public class App extends Application {

    private static Retrofit retrofitVk;
    private static Retrofit retrofitOk;
    private static VkService service;
    private static OdnoklassnikiService odnoklassnikiService;
    private MainPresenter mainPresenter;

    private Odnoklassniki odnoklassniki;
    private SharedPreferences preferences;
    private static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
        Odnoklassniki.createInstance(this, BuildConfig.OK_APP_ID, BuildConfig.OK_APP_KEY);
        app = this;
    }

    public static App get() {
        return app;
    }

    public static Odnoklassniki getOdnoklassniki() {
        if (get().odnoklassniki == null){
            get().odnoklassniki = Odnoklassniki.Companion.of(app);
        }
        return get().odnoklassniki;
    }

    /// for VK
    public static Retrofit getRetrofitVk() {
        if (retrofitVk == null){
            retrofitVk = new Retrofit.Builder()
                    .baseUrl("https://oauth.vk.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitVk;
    }

    public static VkService getVkService() {
        if (service == null) {
            service = getRetrofitVk().create(VkService.class);
        }
        return service;
    }

    /// for OK
    public static Retrofit getRetrofitOk() {
        if (retrofitOk == null){
            retrofitOk = new Retrofit.Builder()
                    .baseUrl("https://api.ok.ru/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitOk;
    }

    public static OdnoklassnikiService getOdnoklassnikiService() {
        if (odnoklassnikiService == null) {
            odnoklassnikiService = getRetrofitOk().create(OdnoklassnikiService.class);
        }
        return odnoklassnikiService;
    }

    public MainPresenter getMainPresenter() {
        if (mainPresenter == null) {
            mainPresenter = new MainPresenter();
        }
        return mainPresenter;
    }

    public SharedPreferences getPrefs() {
        if (preferences == null) {
            preferences = get().getSharedPreferences("global_prefs", MODE_PRIVATE);
        }
        return preferences;
    }
}
