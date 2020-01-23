package com.anatolf.tvchat.ui.uploadchannels;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anatolf.tvchat.R;
import com.anatolf.tvchat.net.model.Channel;

public class ChannelsAdapter extends ArrayAdapter<Channel> {
    private Activity activity;

    public ChannelsAdapter(Activity activity) {
        super(activity, R.layout.item_upload_to_firebase);
        this.activity = activity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final View view = activity.getLayoutInflater().inflate(R.layout.item_upload_to_firebase, null);
        final Channel channel = getItem(position);

        ((TextView) view.findViewById(R.id.channel_et)).setText(channel.name);
        ((TextView) view.findViewById(R.id.url_et)).setText(String.valueOf(channel.urlChannel));
        return view;
    }
}
