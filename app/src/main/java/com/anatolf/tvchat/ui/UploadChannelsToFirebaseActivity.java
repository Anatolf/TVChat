package com.anatolf.tvchat.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.anatolf.tvchat.R;
import com.anatolf.tvchat.model.Channel;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

/**
 * Activity for create new TV-channels
 */
public class UploadChannelsToFirebaseActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference fbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_channels_to_firebase);

        database = FirebaseDatabase.getInstance();
        fbRef = database.getReference("Channels");
        // fbRef = database.getReference("items").child("users").child("newUser");

        final EditText id_Channel = findViewById(R.id.id_channel_et);
        final EditText nameChannel = findViewById(R.id.channel_et);
        final EditText numberChannel = findViewById(R.id.number_et);
        final EditText urlChannel = findViewById(R.id.url_et);
        final Button add = findViewById(R.id.add);
        final ListView items = findViewById(R.id.items);

        final ItemsAdapter adapter = new ItemsAdapter();
        // remove all elements "Channels"
        // fbRef.removeValue();

        items.setAdapter(adapter);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Channel channel = new Channel("9TV", "ch", "111");
                fbRef.push().setValue(channel);
            }
        });

        fbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Channel channel = dataSnapshot.getValue(Channel.class);
                adapter.add(channel);

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

    private class ItemsAdapter extends ArrayAdapter<Channel> {
        ItemsAdapter() {
            super(UploadChannelsToFirebaseActivity.this, R.layout.item_upload_to_firebase);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final View view = getLayoutInflater().inflate(R.layout.item_upload_to_firebase, null);
            final Channel channel = getItem(position);

            ((TextView) view.findViewById(R.id.channel_et)).setText(channel.name);
            ((TextView) view.findViewById(R.id.url_et)).setText(String.valueOf(channel.urlChannel));
            return view;
        }
    }

}
