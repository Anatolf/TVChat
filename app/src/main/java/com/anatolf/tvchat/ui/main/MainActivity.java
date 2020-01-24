package com.anatolf.tvchat.ui.main;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.anatolf.tvchat.R;
import com.anatolf.tvchat.ui.fragment.FragmentAboutProgram;
import com.anatolf.tvchat.ui.fragment.FragmentFeedback;
import com.anatolf.tvchat.ui.fragment.FragmentMain;
import com.anatolf.tvchat.ui.fragment.FragmentSettings;
import com.anatolf.tvchat.ui.fragment.FragmentUploadChannelsToFirebase;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    public static final String CHANNEL_OBJECT_EXTRA = "channel_object_extra";

    private FragmentMain fragMain;
    private FragmentAboutProgram fragAboutProgram;
    private FragmentFeedback fragFeedback;
    private FragmentSettings fragSettings;
    private FragmentTransaction fTrans;
    private LinearLayout leftDrawer;
    private DrawerLayout mDrawerLayout;
    private FragmentUploadChannelsToFirebase fragUploadChannelsToFirebase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        leftDrawer = findViewById(R.id.left_drawer);

        ImageView icon_toolbar = findViewById(R.id.image_tool_bar);
        TextView head_text_toolbar = findViewById(R.id.head_text_tool_bar);
        head_text_toolbar.setText(R.string.app_name);

        Picasso.get()
                .load(R.drawable.ic_tvchat_255)
                .into(icon_toolbar);

        fTrans = getFragmentManager().beginTransaction();

        fragMain = new FragmentMain();
        fragAboutProgram = new FragmentAboutProgram();
        fragFeedback = new FragmentFeedback();
        fragSettings = new FragmentSettings();
        fragUploadChannelsToFirebase = new FragmentUploadChannelsToFirebase();

        fTrans.add(R.id.frgmCont, fragMain);
        // Developers fragment
        //fTrans.add(R.id.frgmCont, fragUploadChannelsToFirebase);
        fTrans.commit();
    }


    public void onClick(View v) {
        fTrans = getFragmentManager().beginTransaction();
        switch (v.getId()) {
            case R.id.image_tool_bar:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)){
                    mDrawerLayout.closeDrawers();
                } else {
                    mDrawerLayout.openDrawer(leftDrawer);
                }
                break;
            case R.id.btnAboutProgram:
                fTrans.replace(R.id.frgmCont, fragAboutProgram);
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)){
                    mDrawerLayout.closeDrawers();
                }
                break;
            case R.id.btnFeedback:
                fTrans.replace(R.id.frgmCont, fragFeedback);
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)){
                    mDrawerLayout.closeDrawers();
                }
                break;
            case R.id.btnSettings:
                fTrans.replace(R.id.frgmCont, fragSettings);
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)){
                    mDrawerLayout.closeDrawers();
                }
            default:
                break;
        }
        fTrans.addToBackStack(null);
        fTrans.commit();
    }
}
