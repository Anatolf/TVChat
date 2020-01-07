package com.anatolf.tvchat.ui.uploadchannels;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.anatolf.tvchat.R;
import com.anatolf.tvchat.net.model.Channel;

/**
 * Activity for create new TV-channels
 */
public class UploadChannelsToFirebaseActivity extends AppCompatActivity
        implements UploadChannelsToFirebaseContractView {

    private UploadChannelsToFirebasePresenter presenter;
    private ChannelsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_channels_to_firebase);

        presenter = new UploadChannelsToFirebasePresenter();
        presenter.attachView(this);

        final EditText id_Channel = findViewById(R.id.id_channel_et);
        final EditText nameChannel = findViewById(R.id.channel_et);
        final EditText numberChannel = findViewById(R.id.number_et);
        final EditText urlChannel = findViewById(R.id.url_et);
        final Button add = findViewById(R.id.add);
        final ListView channelsList = findViewById(R.id.items);

        adapter = new ChannelsAdapter(this);
        channelsList.setAdapter(adapter);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Channel channel = new Channel("9TV", "ch", "111"); // todo set real data
                presenter.createChannel(channel);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    @Override
    public void showChannel(Channel channel) {
        adapter.add(channel);
    }
}
