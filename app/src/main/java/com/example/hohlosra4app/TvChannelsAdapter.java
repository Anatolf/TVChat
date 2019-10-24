package com.example.hohlosra4app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.hohlosra4app.Model.Channel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class TvChannelsAdapter extends RecyclerView.Adapter<TvChannelsAdapter.ViewHolder> {
    // Create two empty arrayList and one context variable;
    Context mainActivityContext;

    ArrayList<Channel> tvChannelsList = new ArrayList<>();

    private OnChannelClickListener onChannelClickListener;  // для передачи данных в MainActivity

//// конструктор для передачи данных из RecyclerView в MainActivity:
    public TvChannelsAdapter(Context mainActivityContext, OnChannelClickListener onChannelClickListener) {
        this.onChannelClickListener = onChannelClickListener;
        this.mainActivityContext = mainActivityContext;
    }

//// принимаем список ТВ-Каналов из Main (из FireBase):
    public void setChannelsList(Channel channel){
        tvChannelsList.add(channel);
        notifyDataSetChanged();
    }

//// Очищает список ТВ-Каналов:
    public void clearChannelsList(){
        tvChannelsList.clear();
        notifyDataSetChanged();
    }

    @Override
    public TvChannelsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Used to connect our custom UI to our recycler view // формирует представление одного элемента
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);

        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(TvChannelsAdapter.ViewHolder holder, int position) {
        //Used to set data in each row of recycler view // обновляет при прокрутке

        String nameTvChannel = tvChannelsList.get(position).name;
        String countUsers = String.valueOf(tvChannelsList.get(position).number);
        String urlTvChannelLogo = tvChannelsList.get(position).urlChannel;

        holder.tvChannelName.setText(nameTvChannel);
        holder.tvUsersIntoChat.setText(countUsers);

        Picasso.with(mainActivityContext)
                .load(urlTvChannelLogo)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.logoTvChannel);
    }


    @Override
    public int getItemCount() {
        //Returns total number of rows inside recycler view  // возвращает количество элементов списка
        return tvChannelsList.size();
    }


    public interface OnChannelClickListener {  // для передачи данных в майн создаем слушатель
        void onChannelClick(Channel channel, Integer usersIntoChat);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        //Used to work with the elements of our custom UI.

        LinearLayout llItemTvChannels;
        TextView tvChannelName;
        TextView tvUsersIntoChat;
        ImageView logoTvChannel;

        public ViewHolder(View itemView) {
            super(itemView);

            llItemTvChannels = itemView.findViewById(R.id.llItemTvChannels);
            tvChannelName = itemView.findViewById(R.id.tv_channel_name);
            tvUsersIntoChat =  itemView.findViewById(R.id.tvUsersIntoChat);
            logoTvChannel =  itemView.findViewById(R.id.logoTvChannel);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // передаём в MainActivity:
                    Channel channel = tvChannelsList.get(getAdapterPosition()); // объект Channel по которому нажали
                    int usersIntoChat = tvChannelsList.get(getAdapterPosition()).number; // количество Юзеров в чате (пока что фейковое из firebase)
                    onChannelClickListener.onChannelClick(channel, usersIntoChat);
                }
            });

        }
    }
}