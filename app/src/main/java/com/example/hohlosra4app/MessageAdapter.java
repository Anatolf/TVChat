package com.example.hohlosra4app;

import android.app.Activity;
import android.content.Context;
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

import com.example.hohlosra4app.Model.Message;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.vk.sdk.VKSdk;

import java.util.ArrayList;
import java.util.List;

import ru.ok.android.sdk.Odnoklassniki;


public class MessageAdapter extends BaseAdapter {
    private static final String TAG = "myLogs";

    private Odnoklassniki odnoklassniki;

    List<Message> messages = new ArrayList<Message>();
    Context context;

    public MessageAdapter(Context context) {
        this.context = context;
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

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messages.get(i);

        odnoklassniki = Odnoklassniki.createInstance(context, "512000154078", "CPNKFHJGDIHBABABA");  // id "512000154078"

        // Если пользователь зарегестрировался через Вк, то отображаем сообщения с аватарками:
        if (VKSdk.isLoggedIn() || !TextUtils.isEmpty(odnoklassniki.getMAccessToken())) {

            if (message.isBelongsToCurrentUser()) {
                convertView = messageInflater.inflate(R.layout.my_message, null);
                holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                holder.time = (TextView) convertView.findViewById(R.id.time_et);
                convertView.setTag(holder);

                holder.time.setText(message.getTime());
                holder.messageBody.setText(message.getText());

            } else {
                convertView = messageInflater.inflate(R.layout.their_message, null);
                holder.avatar = (View) convertView.findViewById(R.id.avatar);
                holder.avatarPhoto = (ImageView) convertView.findViewById(R.id.avatarPhoto);
                holder.name = (TextView) convertView.findViewById(R.id.channel_et);
                holder.time = (TextView) convertView.findViewById(R.id.time_et);
                holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
                convertView.setTag(holder);

                holder.avatar.setVisibility(View.INVISIBLE);
                Picasso.get()
                        .load(message.getAvatar())
                        .transform(new CircularTransformation(0)) // 0 - радиус по умолчанию делает максимальный кроп углов от квадрата
                        .error(R.drawable.ic_launcher_foreground)
                        .into(holder.avatarPhoto);

                holder.name.setText(message.getName());
                holder.time.setText(message.getTime());
                holder.messageBody.setText(message.getText());
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
    public ImageView avatarPhoto;
    public TextView name;
    public TextView messageBody;
    public TextView time;
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