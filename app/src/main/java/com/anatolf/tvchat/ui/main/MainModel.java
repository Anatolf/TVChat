package com.anatolf.tvchat.ui.main;

import com.anatolf.tvchat.model.Channel;
import com.anatolf.tvchat.utils.FirebaseConstants;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

class MainModel {

    private DatabaseReference channelsRef;

    MainModel() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        channelsRef = database.getReference(FirebaseConstants.CHANNELS);
    }

    void downloadChannelsFromFireBase(final FireBaseListener listener) {  // слой модели в котором идёт загрузка

        channelsRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Channel channel = dataSnapshot.getValue(Channel.class);
                channel.setFirebaseChannelId(dataSnapshot.getKey());
                listener.onGetChannel(channel);   // оповещение презентера о завершении загрузки через листенер
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                listener.onGetUsersCountOnline(dataSnapshot);   // оповещение презентера о завершении загрузки через листенер
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
    }

    public interface FireBaseListener {
        void onGetChannel(Channel channel);
        void onGetUsersCountOnline(DataSnapshot dataSnapshot);
    }
}
