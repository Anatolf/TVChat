package com.example.hohlosra4app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hohlosra4app.Model.Message;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.vk.sdk.VKSdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ok.android.sdk.Odnoklassniki;

import static android.content.Context.MODE_PRIVATE;


public class MessageAdapter extends BaseAdapter {
    private static final String TAG = "myLogs";

    private OnLikeClickListener onLikeClickListener;  // для передачи сообщения которому поставили лайк в ChatActivity
    private OnCancelLikeClickListener onCancelLikeClickListener;  // для передачи сообщения которому отменили лайк в ChatActivity

    private Odnoklassniki odnoklassniki;

    List<Message> messages = new ArrayList<Message>();
    Context context;
    String current_user_id;

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
        Log.d(TAG, "Мы в Адаптере, мессадж = " + message.getText());
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

    public interface OnLikeClickListener {  // для передачи нажатого лайка в ChatActivity создаем слушатель
        void onLikeClick(Message message);
    }

    public interface OnCancelLikeClickListener {  // для передачи отмены лайка в ChatActivity создаем слушатель
        void onCancelLikeClick(Message message);
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        final MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        final Message message = messages.get(i);

        odnoklassniki = Odnoklassniki.createInstance(context, BuildConfig.OK_APP_ID, BuildConfig.OK_APP_KEY);

        // Если пользователь зарегестрировался через Вк или Ок, то отображаем сообщения с аватарками:
        if (VKSdk.isLoggedIn() || !TextUtils.isEmpty(odnoklassniki.getMAccessToken())) {

            if (message.isBelongsToCurrentUser()) {
                convertView = messageInflater.inflate(R.layout.my_message, null);
                holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                holder.time = (TextView) convertView.findViewById(R.id.time_et);
                holder.countHeart = (TextView) convertView.findViewById(R.id.count_heart);
                holder.messageBody.setText(message.getText());
                holder.time.setText(message.getTime());
                int count_likes = 0;
                HashMap<String, Boolean> liked_users = message.getLiked_users();
                for (Map.Entry entry : liked_users.entrySet()) {
                    if ((boolean) entry.getValue()) {
                        count_likes++;
                    }
                }
                holder.countHeart.setText(String.valueOf(count_likes));
                convertView.setTag(holder);


            } else {
                convertView = messageInflater.inflate(R.layout.their_message, null);
                holder.avatar = (View) convertView.findViewById(R.id.avatar);
                holder.avatarPhoto = (ImageView) convertView.findViewById(R.id.avatarPhoto);
                holder.name = (TextView) convertView.findViewById(R.id.channel_et);
                holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                holder.time = (TextView) convertView.findViewById(R.id.time_et);
                holder.countHeart = (TextView) convertView.findViewById(R.id.count_heart);


                holder.avatar.setVisibility(View.INVISIBLE);
                Picasso.get()
                        .load(message.getAvatar())
                        .transform(new CircularTransformation(0)) // 0 - радиус по умолчанию делает максимальный кроп углов от квадрата
                        .error(R.drawable.ic_launcher_foreground)
                        .into(holder.avatarPhoto);

                holder.name.setText(message.getName());
                holder.time.setText(message.getTime());
                holder.messageBody.setText(message.getText());

                int count_likes = 0;
                HashMap<String, Boolean> liked_users = message.getLiked_users();
                for (Map.Entry entry : liked_users.entrySet()) {
                    if ((boolean) entry.getValue()) {
                        count_likes++;
                    }
                }
                holder.countHeart.setText(String.valueOf(count_likes));
                convertView.setTag(holder);



            }

            // Если пользователь НЕ зарегестрировался через Вк или Ок, то отображаем сообщения цветными заглушками и рандомными именами:
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


        holder.whiteHeart = convertView.findViewById(R.id.image_heart_white);
        holder.redHeart = convertView.findViewById(R.id.image_heart_red);


        if (VKSdk.isLoggedIn() || !TextUtils.isEmpty(odnoklassniki.getMAccessToken())) {
            HashMap<String, Boolean> liked_users = message.getLiked_users();
            for (Map.Entry entry : liked_users.entrySet()) {

                if (entry.getKey().equals(current_user_id) && (boolean) entry.getValue()) {

                    holder.whiteHeart.setVisibility(View.INVISIBLE);
                    holder.redHeart.setVisibility(View.VISIBLE);
                    //iLikeThisMessage = true;
                } else {
                    holder.redHeart.setVisibility(View.INVISIBLE);
                    holder.whiteHeart.setVisibility(View.VISIBLE);
                }
            }
        }


        if (!TextUtils.isEmpty(current_user_id)) {

            holder.whiteHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Лайк: " + message.getText(), Toast.LENGTH_SHORT).show();
                    if (onLikeClickListener != null) {
                        onLikeClickListener.onLikeClick(message);
                    }
                    holder.whiteHeart.setVisibility(View.INVISIBLE);
                    holder.redHeart.setVisibility(View.VISIBLE);


                    //holder.countHeart.setText(String.valueOf(countLike + 1));

                }

            });

            holder.redHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Дизлайк: " + message.getText(), Toast.LENGTH_SHORT).show();
                    if (onCancelLikeClickListener != null) {
                        onCancelLikeClickListener.onCancelLikeClick(message);

                    }
                    holder.redHeart.setVisibility(View.INVISIBLE);
                    holder.whiteHeart.setVisibility(View.VISIBLE);

                    //holder.countHeart.setText(String.valueOf(countLike - 1));

                }

            });
        }

        return convertView;
    }
}

class MessageViewHolder {
    public View avatar;
    public ImageView avatarPhoto;
    public TextView name;
    public TextView messageBody;
    public TextView time;
    public ImageView whiteHeart;
    public ImageView redHeart;
    public TextView countHeart;

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