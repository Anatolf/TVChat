package com.example.hohlosra4app;

import android.app.Application;
import android.content.SharedPreferences;

import com.vk.sdk.VKSdk;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.ok.android.sdk.Odnoklassniki;

public class App extends Application {

    private static Retrofit retrofit;
    private static VkService service;

    private Odnoklassniki odnoklassniki;
    private SharedPreferences preferences;
    private static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
        Odnoklassniki.createInstance(this,"512000154078","CPNKFHJGDIHBABABA");
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

    public static Retrofit getRetrofit() {
        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://oauth.vk.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static VkService getService() {
        if (service == null) {
            service = getRetrofit().create(VkService.class);
        }
        return service;
    }

    public SharedPreferences getPrefs() {
        if (preferences == null) {
            preferences = get().getSharedPreferences("global_prefs", MODE_PRIVATE);
        }
        return preferences;
    }
}
