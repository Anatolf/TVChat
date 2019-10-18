package com.example.hohlosra4app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.vk.sdk.VKSdk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class MessageAdapter extends BaseAdapter {
    private static final String TAG = "myLogs";

    List<Message> messages = new ArrayList<Message>();
    Context context;

    public MessageAdapter(Context context) {
        this.context = context;
    }


    public void add(Message message) {
            this.messages.add(message);
            notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messages.get(i);


        // Если пользователь зарегестрировался через Вк, то отображаем сообщения с аватарками:
        if (VKSdk.isLoggedIn()) {
            // Picasso.with(context).load()


            if (message.isBelongsToCurrentUser()) {
                convertView = messageInflater.inflate(R.layout.my_message, null);
                holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                holder.time = (TextView) convertView.findViewById(R.id.time_et);
                convertView.setTag(holder);

                holder.time.setText(message.getTime());
                holder.messageBody.setText(message.getText());
               // Log.d(TAG, "inAdapter, Me: message= " + message.getText() + " , messages.size= " + messages.size());

            } else {
                convertView = messageInflater.inflate(R.layout.their_message, null);
                holder.avatar = (View) convertView.findViewById(R.id.avatar);
                holder.name = (TextView) convertView.findViewById(R.id.channel_et);
                holder.time = (TextView) convertView.findViewById(R.id.time_et);
                holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                convertView.setTag(holder);

                holder.name.setText(message.getName());
                holder.time.setText(message.getTime());
                holder.messageBody.setText(message.getText());
               // GradientDrawable drawable = (GradientDrawable) holder.avatar.getBackground();
               // drawable.setColor(Color.parseColor(message.getColor()));
               // Log.d(TAG, "inAdapter, Their: message= " + message.getText() + " , messages.size= " + messages.size());
            }

        // Если пользователь НЕ зарегестрировался через Вк, то отображаем сообщения цветными заглушками и рандомными именами:
        } else {
            if (message.isBelongsToCurrentUser()) {
                convertView = messageInflater.inflate(R.layout.my_message, null);
                holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                holder.time = (TextView) convertView.findViewById(R.id.time_et);
                convertView.setTag(holder);

                holder.time.setText(message.getTime());
                holder.messageBody.setText(message.getText());
              //  Log.d(TAG, "inAdapter, Me: message= " + message.getText() + " , messages.size= " + messages.size());

            } else {
                convertView = messageInflater.inflate(R.layout.their_message, null);
                holder.avatar = (View) convertView.findViewById(R.id.avatar);
                holder.name = (TextView) convertView.findViewById(R.id.channel_et);
                holder.time = (TextView) convertView.findViewById(R.id.time_et);
                holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                convertView.setTag(holder);

                holder.name.setText(message.getName());
                holder.time.setText(message.getTime());
                holder.messageBody.setText(message.getText());
                GradientDrawable drawable = (GradientDrawable) holder.avatar.getBackground();
                drawable.setColor(Color.parseColor(message.getColor()));
              //  Log.d(TAG, "inAdapter, Their: message= " + message.getText() + " , messages.size= " + messages.size());
            }
        }

        return convertView;
    }
}

class MessageViewHolder {
    public View avatar;
    public TextView name;
    public TextView messageBody;
    public TextView time;
}