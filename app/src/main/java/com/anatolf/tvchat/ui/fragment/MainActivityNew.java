package com.anatolf.tvchat.ui.fragment;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.anatolf.tvchat.R;
import com.squareup.picasso.Picasso;

public class MainActivityNew extends AppCompatActivity {
    // https://startandroid.ru/ru/uroki/vse-uroki-spiskom/175-urok-105-android-3-fragments-dinamicheskaja-rabota.html

    private FragmentMain fragMain;
    private FragmentUploadChannelsToFirebase fragUploadChannelsToFirebase;
    private FragmentTransaction fTrans;
    private LinearLayout leftDrawer;
    private DrawerLayout mDrawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        TextView head_text_toolbar = findViewById(R.id.head_text_tool_bar);
        ImageView icon_toolbar = findViewById(R.id.image_tool_bar);
        leftDrawer = findViewById(R.id.left_drawer);
        head_text_toolbar.setText(R.string.app_name);

        Picasso.get()
                .load(R.drawable.ic_tvchat_255)
                .into(icon_toolbar);

        fTrans = getFragmentManager().beginTransaction();

        fragMain = new FragmentMain();
        fragUploadChannelsToFirebase = new FragmentUploadChannelsToFirebase();
        fTrans.add(R.id.frgmCont, fragMain);
        fTrans.addToBackStack(null);
        fTrans.commit();
    }


    public void onClick(View v) {
        fTrans = getFragmentManager().beginTransaction();
        switch (v.getId()) {
            case R.id.btnAdd:
                fTrans.add(R.id.frgmCont, fragMain);
                break;
            case R.id.btnRemove:
                fTrans.remove(fragMain);
                break;
            case R.id.btnReplace:
                fTrans.replace(R.id.frgmCont, fragUploadChannelsToFirebase);
            case R.id.image_tool_bar:
                Toast.makeText(MainActivityNew.this,
                        "тут будет выпадающее меню!",
                        Toast.LENGTH_SHORT).show();

                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)){
                    mDrawerLayout.closeDrawers();
                } else {
                    mDrawerLayout.openDrawer(leftDrawer);
                }

                break;
            default:
                break;
        }
        //if (chbStack.isChecked())
        fTrans.addToBackStack(null);
        fTrans.commit();
    }
}
