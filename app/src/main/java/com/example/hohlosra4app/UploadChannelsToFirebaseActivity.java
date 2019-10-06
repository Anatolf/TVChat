package com.example.hohlosra4app;

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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Query;

import java.io.Serializable;

public class UploadChannelsToFirebaseActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_channels_to_firebase);

        // подключили базу
        database = FirebaseDatabase.getInstance();
//        database.setPersistenceEnabled(true);  // добавление элементов во время оффлайн

        myRef = database.getReference("Channels");
        //  myRef = database.getReference("items").child("users").child("newUser"); // многопользовательская ветка

        final EditText nameChannel = findViewById(R.id.channel_et);
        final EditText numberChannel = findViewById(R.id.number_et);
        final EditText urlChannel = findViewById(R.id.url_et);
        final Button add = findViewById(R.id.add);
        final ListView items = findViewById(R.id.items);

        final ItemsAdapter adapter = new ItemsAdapter();
        //   myRef.removeValue();  // удаление всех элементов из базы "Channels"

        items.setAdapter(adapter);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //создаем экземпляр одного канала
                Channel channel = new Channel(nameChannel.getText().toString(), Integer.valueOf(numberChannel.getText().toString()), urlChannel.getText().toString());
                //    adapter.add(item);
                // оправляем его в базу данных firebase
                myRef.push().setValue(channel);
            }
        });

        // делаем запрос в базу данных firebase
        Query myQuery = myRef;
        //  Query myQuery = myRef.orderByChild("numberChannel").equalTo(111);   // редактирование: сортирует ответ по "numberChannel" и 111
        myQuery.addChildEventListener(new ChildEventListener() {
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
            super(UploadChannelsToFirebaseActivity.this, R.layout.upload_to_firebase_item);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final View view = getLayoutInflater().inflate(R.layout.upload_to_firebase_item, null);
            final Channel channel = getItem(position);

            // показывает в upload_to_firebase_item из базы данных
            ((TextView) view.findViewById(R.id.channel_et)).setText(channel.name);
            ((TextView) view.findViewById(R.id.number_et)).setText(String.valueOf(channel.number));
            ((TextView) view.findViewById(R.id.url_et)).setText(String.valueOf(channel.urlChannel));

            return view;
        }
    }

    // Класс Channel - один элемент с тремя параметрами (для добавления в базу данных firebase)
    @IgnoreExtraProperties
    static class Channel implements Serializable {
        public String name;
        public int number;
        public String urlChannel;

        public Channel() {
        }

        Channel(String name, int number, String url) {
            this.name = name;
            this.number = number;
            this.urlChannel = url;
        }
    }

}
