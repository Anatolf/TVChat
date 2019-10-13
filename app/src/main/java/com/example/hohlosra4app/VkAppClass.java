package com.example.hohlosra4app;

import com.vk.sdk.VKSdk;

public class VkAppClass extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
    }
}
