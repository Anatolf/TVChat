package com.example.hohlosra4app;

import android.app.Application;

import com.vk.sdk.VKSdk;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
    }

    /// от сюда https://www.youtube.com/watch?v=0aS0S9Pk9yc

    //    VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
//        @Override
//        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
//            if (newToken == null) {
//// VKAccessToken is invalid
//                Intent intent = new Intent(ChatActivity.this,RegistrationActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//            }
//        }
//    };

    //        vkAccessTokenTracker.startTracking();
    //  VKSdk.initialize(this);

    // VK.login(activity, arrayListOf(VKScope.WALL, VKScope.PHOTOS));
}
