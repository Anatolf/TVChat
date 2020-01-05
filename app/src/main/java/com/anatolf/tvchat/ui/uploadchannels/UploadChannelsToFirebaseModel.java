package com.anatolf.tvchat.ui.uploadchannels;

import com.anatolf.tvchat.model.Channel;
import com.anatolf.tvchat.utils.FirebaseConstants;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UploadChannelsToFirebaseModel {

    private DatabaseReference channelsRef;

    UploadChannelsToFirebaseModel(final FireBaseListener listener) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        channelsRef = database.getReference(FirebaseConstants.CHANNELS);


        channelsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Channel channel = dataSnapshot.getValue(Channel.class);
                listener.onChannelAdded(channel);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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

    public void createChannel(Channel channel) {
        channelsRef.push().setValue(channel);
    }

    public void removeAllChannels() {
        channelsRef.removeValue();
    }

    public interface FireBaseListener {
        void onChannelAdded(Channel channel);
    }
}
