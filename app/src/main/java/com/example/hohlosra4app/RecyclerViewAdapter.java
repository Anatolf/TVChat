package com.example.hohlosra4app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    // Create two empty arrayList and one context variable;
    Context mainActivityContext;
    ArrayList<String> channelsArrayList = new ArrayList<>();
    ArrayList<Integer> usersIntoChatArrayList = new ArrayList<>();

    private OnChannelClickListner onChannelClickListner;  // для передачи данных в MainActivity

//// конструктор для передачи данных из RecyclerView в MainActivity:
    public RecyclerViewAdapter(OnChannelClickListner onChannelClickListner) {
        this.onChannelClickListner = onChannelClickListner;
    }

//// Create one constructor with three parameters which will passed from MainActivity class
    public RecyclerViewAdapter(Context mainActivityContext, ArrayList<String> channelsArrayList, ArrayList<Integer> countUsersArrayList) {
        this.mainActivityContext = mainActivityContext;
      //  this.channelsArrayList = channelsArrayList;
      //  this.usersIntoChatArrayList = countUsersArrayList;
    }

//// Устанавливает список Каналов и список активных Юзеров в канале:
    public void setChannelAndUsersIntoList(ArrayList<String> channels, ArrayList<Integer> usersInto){
        channelsArrayList.addAll(channels);
        usersIntoChatArrayList.addAll(usersInto);
        notifyDataSetChanged();
    }

//// Очищает список Каналов и список активных Юзеров в канале:
    public void clearChannelAndUsersIntoList(){
        channelsArrayList.clear();
        usersIntoChatArrayList.clear();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Used to connect our custom UI to our recycler view // надувает представление одного элемента

        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);

        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {
        //Used to set data in each row of recycler view // обновляет при прокрутке

        String currentTvChannel = channelsArrayList.get(position);
        String countUsers = usersIntoChatArrayList.get(position).toString();

        holder.tvChannelName.setText(currentTvChannel);
        holder.tvUsersIntoChat.setText(countUsers);
    }


    @Override
    public int getItemCount() {
        //Returns total number of rows inside recycler view  // возвращает количество элементов списка
        return channelsArrayList.size();
    }


////////////// experiment ////////////////////
    public interface OnChannelClickListner{  // для передачи данных в майн создаем слушатель
        void onChannelClick(Integer channel, Integer usersIntoChat);
    }
//end////////////////////////////////////////


    public class ViewHolder extends RecyclerView.ViewHolder{
        //Used to work with the elements of our custom UI.

        LinearLayout llItemTvChannels;
        TextView tvChannelName;
        TextView tvUsersIntoChat;

        public ViewHolder(View itemView) {
            super(itemView);

            llItemTvChannels = itemView.findViewById(R.id.llItemTvChannels);
            tvChannelName = itemView.findViewById(R.id.tv_channel_name);
            tvUsersIntoChat =  itemView.findViewById(R.id.tvUsersIntoChat);

////////////experiment////////////
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int channelNumber = getAdapterPosition();  // передаём в MainActivity индекс элемента по которому нажали
                    int usersIntoChat = usersIntoChatArrayList.get(getAdapterPosition());
                    onChannelClickListner.onChannelClick(channelNumber, usersIntoChat);
                }
            });
//end//////////////////////////////
//            llItemTvChannels.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
////                    Toast.makeText(mainActivityContext,
////                            "You clicked item number: " + getAdapterPosition(),
////                            Toast.LENGTH_SHORT).show();
//
//                  //  Intent chatIntent = new Intent(mainActivityContext, ChatActivity.class);
//
//                  //  startActivity(chatIntent);
//
//                }
//            });

        }
    }
}