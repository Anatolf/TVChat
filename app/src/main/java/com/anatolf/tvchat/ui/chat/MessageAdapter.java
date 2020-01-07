package com.anatolf.tvchat.ui.chat;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anatolf.tvchat.App;
import com.anatolf.tvchat.R;
import com.anatolf.tvchat.net.model.Message;
import com.anatolf.tvchat.utils.CircularTransformation;
import com.anatolf.tvchat.utils.PrefsConstants;
import com.squareup.picasso.Picasso;
import com.vk.sdk.VKSdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.BaseViewHolder> {
    private static final String TAG = "MessageAdapter";
    private static final int ITEM_MY_MESSAGE = 0;
    private static final int ITEM_THEIR_MESSAGE = 1;
    private static final int ITEM_UNKNOWN_MESSAGE = 2;

    private OnLikeClickListener onLikeClickListener;
    private OnCancelLikeClickListener onCancelLikeClickListener;

    List<Message> messages = new ArrayList<Message>();

    MessageAdapter(OnLikeClickListener onLikeClickListener,
                   OnCancelLikeClickListener onCancelLikeClickListener) {

        this.onLikeClickListener = onLikeClickListener;
        this.onCancelLikeClickListener = onCancelLikeClickListener;
    }


    void add(Message message) {
        this.messages.add(message);
        notifyDataSetChanged();
    }

    private String getCurrentUserId() {
        SharedPreferences sPref = App.get().getPrefs();
        final String current_user_id_vk = sPref.getString(PrefsConstants.USER_VK_ID, "");
        final String current_user_id_ok = sPref.getString(PrefsConstants.USER_Ok_ID, "");

        if (!TextUtils.isEmpty(current_user_id_vk)) {
            return current_user_id_vk;
        }
        if (!TextUtils.isEmpty(current_user_id_ok)) {
            return current_user_id_ok;
        }
        return "";
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v;

        switch (viewType) {
            case ITEM_MY_MESSAGE: {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_message, parent, false);
                return new MyMessageViewHolder(v);
            }
            case ITEM_THEIR_MESSAGE: {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_their_message, parent, false);
                return new TheirMessageViewHolder(v);
            }
            default: {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_their_message, parent, false);
                return new UnknownMessageViewHolder(v);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (!VKSdk.isLoggedIn() && TextUtils.isEmpty(App.getOdnoklassniki().getMAccessToken())) {
            return ITEM_UNKNOWN_MESSAGE;
        }

        if (messages.get(position).isBelongsToCurrentUser()) {
            return ITEM_MY_MESSAGE;
        } else {
            return ITEM_THEIR_MESSAGE;
        }
    }

    public interface OnLikeClickListener {
        void onLikeClick(Message message);
    }

    public interface OnCancelLikeClickListener {
        void onCancelLikeClick(Message message);
    }


    public class MyMessageViewHolder extends BaseViewHolder {

        public MyMessageViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        void bind(final Message message) {
            messageBody.setText(message.getText());
            time.setText(message.getTime());

            setMyLike(message);
            setOnLikeClick(message);
            setLikeCount(message);
        }
    }

    public class TheirMessageViewHolder extends BaseViewHolder {

        TheirMessageViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        void bind(final Message message) {

            avatar.setVisibility(View.INVISIBLE);
            Picasso.get()
                    .load(message.getAvatar())
                    .transform(new CircularTransformation(0))
                    .error(R.drawable.ic_launcher_foreground)
                    .into(avatarPhoto);

            name.setText(message.getName());
            time.setText(message.getTime());
            messageBody.setText(message.getText());

            setMyLike(message);
            setOnLikeClick(message);
            setLikeCount(message);
        }
    }

    public class UnknownMessageViewHolder extends BaseViewHolder {

        UnknownMessageViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        void bind(final Message message) {
            // Without registration - display messages with incognito avatars and names
            name.setText(message.getName());
            time.setText(message.getTime());
            messageBody.setText(message.getText());
            GradientDrawable drawable = (GradientDrawable) avatar.getBackground();
            drawable.setColor(Color.parseColor(message.getColor()));

            whiteHeart.setVisibility(View.VISIBLE);
            setOnLikeClick(message);
            setLikeCount(message);
        }
    }

    abstract class BaseViewHolder extends RecyclerView.ViewHolder {

        View avatar;
        ImageView avatarPhoto;
        TextView name;
        TextView messageBody;
        TextView time;
        ImageView whiteHeart;
        ImageView redHeart;
        TextView countHeart;

        BaseViewHolder(View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.avatar);
            avatarPhoto = itemView.findViewById(R.id.avatarPhoto);

            name = itemView.findViewById(R.id.channel_et);
            messageBody = itemView.findViewById(R.id.message_body);
            time = itemView.findViewById(R.id.time_et);
            whiteHeart = itemView.findViewById(R.id.image_heart_white);
            redHeart = itemView.findViewById(R.id.image_heart_red);
            countHeart = itemView.findViewById(R.id.count_heart);

        }

        abstract void bind(final Message message);


        protected void setMyLike(final Message message) {

            whiteHeart.setVisibility(View.VISIBLE);

            if (message.getLiked_users() != null) {
                HashMap<String, Boolean> liked_users = message.getLiked_users();

                if (liked_users.containsKey(getCurrentUserId())) {
                    whiteHeart.setVisibility(View.INVISIBLE);
                    redHeart.setVisibility(View.VISIBLE);
                } else {
                    redHeart.setVisibility(View.INVISIBLE);
                    whiteHeart.setVisibility(View.VISIBLE);
                }
            }
        }


        protected void setLikeCount(final Message message) {
            int count_likes = 0;
            if (message.getLiked_users() != null) {
                HashMap<String, Boolean> liked_users = message.getLiked_users();
                for (Map.Entry entry : liked_users.entrySet()) {
                    if ((boolean) entry.getValue()) {
                        count_likes++;
                    }
                }
            }
            countHeart.setText(String.valueOf(count_likes));
        }


        protected void setOnLikeClick(final Message message) {

            if (!TextUtils.isEmpty(getCurrentUserId())) {

                whiteHeart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onLikeClickListener != null) {
                            onLikeClickListener.onLikeClick(message);
                        }
                        whiteHeart.setVisibility(View.INVISIBLE);
                        redHeart.setVisibility(View.VISIBLE);
                    }

                });

                redHeart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onCancelLikeClickListener != null) {
                            onCancelLikeClickListener.onCancelLikeClick(message);
                        }
                        redHeart.setVisibility(View.INVISIBLE);
                        whiteHeart.setVisibility(View.VISIBLE);
                    }

                });
            }
        }
    }
}


