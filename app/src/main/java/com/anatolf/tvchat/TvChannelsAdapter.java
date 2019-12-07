package com.anatolf.tvchat;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.anatolf.tvchat.Model.Channel;
import com.example.hohlosra4app.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class TvChannelsAdapter extends RecyclerView.Adapter<TvChannelsAdapter.ViewHolder> {
    public static final String TAG = "TvChannelsAdapter";

    // Create two empty arrayList and one context variable;
    private final Context mainActivityContext;

    ArrayList<Channel> tvChannelsList = new ArrayList<>();

    private OnChannelClickListener onChannelClickListener;  // для передачи данных в MainActivity

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    //// конструктор для передачи данных из RecyclerView в MainActivity:
    public TvChannelsAdapter(Context mainActivityContext, OnChannelClickListener onChannelClickListener) {
        this.onChannelClickListener = onChannelClickListener;
        this.mainActivityContext = mainActivityContext;
    }

    //// принимаем список ТВ-Каналов из Main (из FireBase):
    public void setChannelsList(Channel channel) {

        preferences = PreferenceManager.getDefaultSharedPreferences(mainActivityContext);
        String favorite_channel_id = preferences.getString(channel.channel_id, "");

        // проверка добавлен ли тв-канал в избранное, если да, то в начало списка:
        if (!TextUtils.isEmpty(favorite_channel_id)) {
            tvChannelsList.add(0, channel);
        } else {
            tvChannelsList.add(channel);
        }
        notifyDataSetChanged();
    }

    //// Очищает список ТВ-Каналов:
    public void clearChannelsList() {
        tvChannelsList.clear();
        notifyDataSetChanged();
    }

    @Override
    public TvChannelsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Used to connect our custom UI to our recycler view // формирует представление одного элемента
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_channel_recyclerview, parent, false);

        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(TvChannelsAdapter.ViewHolder holder, int position) {
        //Used to set data in each row of recycler view // обновляет при прокрутке

        String nameTvChannel = tvChannelsList.get(position).name;
        holder.tvChannelName.setText(nameTvChannel);

        int countUsersInChat = 0;
        if(tvChannelsList.get(position).getCountUsersInChat() != null) {
            HashMap<String, Boolean> count_users = tvChannelsList.get(position).getCountUsersInChat();
            for (Map.Entry entry : count_users.entrySet()) {
                if ((boolean) entry.getValue()) {
                    countUsersInChat++;
                }
            }
            holder.tvUsersIntoChat.setText(String.valueOf(countUsersInChat));
            holder.tvUsersIntoChat.setVisibility(View.VISIBLE);
            holder.onLine.setVisibility(View.VISIBLE);
        } else {
            holder.tvUsersIntoChat.setVisibility(View.INVISIBLE);
            holder.onLine.setVisibility(View.INVISIBLE);
        }

        String urlTvChannelLogo = tvChannelsList.get(position).urlChannel;
        Picasso.get()
                .load(urlTvChannelLogo)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.logoTvChannel);

        preferences = PreferenceManager.getDefaultSharedPreferences(mainActivityContext);
        String favorite_channel_id = preferences.getString(tvChannelsList.get(position).channel_id, "");
        if (!TextUtils.isEmpty(favorite_channel_id)) {
            holder.mStarWhite.setVisibility(View.GONE);
            holder.mStarYellow.setVisibility(View.VISIBLE);
        } else {
            holder.mStarYellow.setVisibility(View.GONE);
            holder.mStarWhite.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public int getItemCount() {
        //Returns total number of rows inside recycler view  // возвращает количество элементов списка
        return tvChannelsList.size();
    }


    public interface OnChannelClickListener {  // для передачи данных в майн создаем слушатель
        void onChannelClick(Channel channel);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //Used to work with the elements of our custom UI.

        LinearLayout llItemTvChannels;
        TextView tvChannelName;
        TextView tvUsersIntoChat;
        ImageView logoTvChannel;
        ImageView mStarWhite;
        ImageView mStarYellow;
        TextView onLine;

        public ViewHolder(View itemView) {
            super(itemView);

            llItemTvChannels = itemView.findViewById(R.id.llItemTvChannels);
            tvChannelName = itemView.findViewById(R.id.tv_channel_name);
            tvUsersIntoChat = itemView.findViewById(R.id.tvUsersIntoChat);
            logoTvChannel = itemView.findViewById(R.id.logoTvChannel);
            mStarWhite = itemView.findViewById(R.id.star_icon_white);
            mStarYellow = itemView.findViewById(R.id.star_icon_yellow);
            onLine = itemView.findViewById(R.id.tv_online);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // передаём в MainActivity:
                    Channel channel = tvChannelsList.get(getAdapterPosition()); // объект Channel по которому нажали
                    onChannelClickListener.onChannelClick(channel);
                }
            });


            // нажатие на звёздочку (тв канал в избранное) // скрываем белую и показываем жёлтую в методе setChannelsList()
            mStarWhite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Channel channel = tvChannelsList.get(getAdapterPosition()); // объект Channel, чья звёздочка

                    preferences = PreferenceManager.getDefaultSharedPreferences(mainActivityContext);
                    editor = preferences.edit();
                    editor.putString(channel.channel_id, channel.channel_id);
                    editor.apply();

                    notifyDataSetChanged();
                }
            });

            mStarYellow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Channel channel = tvChannelsList.get(getAdapterPosition()); // объект Channel, чья звёздочка

                    preferences = PreferenceManager.getDefaultSharedPreferences(mainActivityContext);
                    editor = preferences.edit();
                    editor.remove(channel.channel_id);
                    editor.apply();

                    mStarYellow.setVisibility(View.GONE);
                    mStarWhite.setVisibility(View.VISIBLE);
                    notifyDataSetChanged();
                }
            });


        }
    }
}