package com.anatolf.tvchat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anatolf.tvchat.Model.Message;
import com.example.hohlosra4app.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.vk.sdk.VKSdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.BaseViewHolder> {
    private static final String TAG = "MessageAdapter";
    private static final int ITEM_MY_MESSAGE = 0;
    private static final int ITEM_THEIR_MESSAGE = 1;
    private static final int ITEM_UNKNOW_MESSAGE = 2;

    private OnLikeClickListener onLikeClickListener;  // для передачи сообщения которому поставили лайк в ChatActivity
    private OnCancelLikeClickListener onCancelLikeClickListener;  // для передачи сообщения которому отменили лайк в ChatActivity

    List<Message> messages = new ArrayList<Message>();
    private Context context;
    private String current_user_id;

    public MessageAdapter(Context context,
                          OnLikeClickListener onLikeClickListener,
                          OnCancelLikeClickListener onCancelLikeClickListener,
                          String current_user_id) {

        this.context = context;
        this.onLikeClickListener = onLikeClickListener;
        this.onCancelLikeClickListener = onCancelLikeClickListener;
        this.current_user_id = current_user_id;
    }


    public void add(Message message) {
        // Log.d(TAG, "Мы в Адаптере, мессадж = " + message.getText());
        this.messages.add(message);
        notifyDataSetChanged();
    }

    // todo clear()

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Used to connect our custom UI to our recycler view // формирует представление одного элемента
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
        //Used to set data in each row of recycler view // обновляет при прокрутке
        holder.bind(messages.get(position));

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (!VKSdk.isLoggedIn() && TextUtils.isEmpty(App.getOdnoklassniki().getMAccessToken())) {
            return ITEM_UNKNOW_MESSAGE;
        }

        if (messages.get(position).isBelongsToCurrentUser()) {
            return ITEM_MY_MESSAGE;
        } else {
            return ITEM_THEIR_MESSAGE;
        }
    }

    public interface OnLikeClickListener {  // для передачи нажатого лайка в ChatActivity создаем слушатель
        void onLikeClick(Message message);
    }

    public interface OnCancelLikeClickListener {  // для передачи отмены лайка в ChatActivity создаем слушатель
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

        public TheirMessageViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        void bind(final Message message) {

            // Если пользователь зарегестрировался через Вк или Ок, то отображаем сообщения с аватарками:
            avatar.setVisibility(View.INVISIBLE);
            Picasso.get()
                    .load(message.getAvatar())
                    .transform(new CircularTransformation(0)) // 0 - радиус по умолчанию делает максимальный кроп углов от квадрата
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

        public UnknownMessageViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        void bind(final Message message) {
            // Если пользователь НЕ зарегестрировался через Вк или Ок, то отображаем сообщения цветными заглушками и рандомными именами:
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

        public View avatar;
        public ImageView avatarPhoto;

        public TextView name;
        public TextView messageBody;
        public TextView time;
        public ImageView whiteHeart;
        public ImageView redHeart;
        public TextView countHeart;

        public BaseViewHolder(View itemView) {
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

                if (liked_users.containsKey(current_user_id)) {
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

            if (!TextUtils.isEmpty(current_user_id)) {

                whiteHeart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(context, "Лайк: " + message.getText(), Toast.LENGTH_SHORT).show();
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


// for Picasso:
class CircularTransformation implements Transformation {
    private int mRadius = 10;

    public CircularTransformation() {
    }

    public CircularTransformation(final int radius) {
        this.mRadius = radius;
    }

    @Override
    public Bitmap transform(final Bitmap source) {


        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        final Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        if (mRadius == 0) {
            canvas.drawCircle(source.getWidth() / 2, source.getHeight() / 2, source.getWidth() / 2, paint);
        } else {
            canvas.drawCircle(source.getWidth() / 2, source.getHeight() / 2, mRadius, paint);
        }

        if (source != output)
            source.recycle();

        return output;
    }

    @Override
    public String key() {
        return "circle";
    }
}