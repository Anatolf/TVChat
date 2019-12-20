package com.anatolf.tvchat.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

/**
 * Circular avatar for Picasso
 */
public class CircularTransformation implements Transformation {
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
