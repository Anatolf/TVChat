package com.anatolf.tvchat.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.anatolf.tvchat.App;
import com.anatolf.tvchat.R;
import com.anatolf.tvchat.net.model.Channel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class TvChannelsAdapter extends RecyclerView.Adapter<TvChannelsAdapter.ViewHolder> {
    public static final String TAG = "TvChannelsAdapter";

    private final Context context;

    public ArrayList<Channel> tvChannelsList = new ArrayList<>();

    private OnChannelClickListener onChannelClickListener;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public TvChannelsAdapter(Context context, OnChannelClickListener onChannelClickListener) {
        this.onChannelClickListener = onChannelClickListener;
        this.context = context;
        this.preferences = App.get().getPrefs();
    }

    public void setChannel(Channel channel) {

        String favorite_channel_id = preferences.getString(channel.channel_id, "");

        // favorite channels go to up
        if (!TextUtils.isEmpty(favorite_channel_id)) {
            tvChannelsList.add(0, channel);
        } else {
            tvChannelsList.add(channel);
        }
        notifyDataSetChanged();
    }

    public void clearChannelsList() {
        tvChannelsList.clear();
        notifyDataSetChanged();
    }

    @Override
    public TvChannelsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_channel_recyclerview, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TvChannelsAdapter.ViewHolder holder, int position) {

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
        return tvChannelsList.size();
    }

    public interface OnChannelClickListener {
        void onChannelClick(Channel channel);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

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
                    Channel channel = tvChannelsList.get(getAdapterPosition());
                    onChannelClickListener.onChannelClick(channel);
                }
            });

            mStarWhite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Channel channel = tvChannelsList.get(getAdapterPosition());

                    editor = preferences.edit();
                    editor.putString(channel.channel_id, channel.channel_id);
                    editor.apply();

                    notifyDataSetChanged();
                }
            });

            mStarYellow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Channel channel = tvChannelsList.get(getAdapterPosition());

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