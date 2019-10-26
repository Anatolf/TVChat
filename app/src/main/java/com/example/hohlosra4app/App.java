package com.example.hohlosra4app;

import android.app.Application;
import com.vk.sdk.VKSdk;
import ru.ok.android.sdk.Odnoklassniki;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
        Odnoklassniki.createInstance(this,"512000154078","CPNKFHJGDIHBABABA");
    }

}
