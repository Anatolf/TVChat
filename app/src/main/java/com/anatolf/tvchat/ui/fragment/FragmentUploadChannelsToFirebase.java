package com.anatolf.tvchat.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anatolf.tvchat.R;
import com.anatolf.tvchat.net.model.Channel;
import com.anatolf.tvchat.ui.uploadchannels.ChannelsAdapter;
import com.anatolf.tvchat.ui.uploadchannels.UploadChannelsToFirebaseContractView;
import com.anatolf.tvchat.ui.uploadchannels.UploadChannelsToFirebasePresenter;

//import androidx.fragment.app.Fragment;

/**
 * Fragment for create new TV-channels
 */
public class FragmentUploadChannelsToFirebase extends Fragment implements UploadChannelsToFirebaseContractView {

    private UploadChannelsToFirebasePresenter presenter;
    private ChannelsAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_channels_to_firebase, container,false);

        presenter = new UploadChannelsToFirebasePresenter();
        presenter.attachView(this);

        final EditText id_Channel = view.findViewById(R.id.id_channel_et);
        final EditText nameChannel = view.findViewById(R.id.channel_et);
        final EditText numberChannel = view.findViewById(R.id.number_et);
        final EditText urlChannel = view.findViewById(R.id.url_et);
        final Button add = view.findViewById(R.id.add);
        final ListView channelsList = view.findViewById(R.id.items);

        adapter = new ChannelsAdapter(getActivity());
        channelsList.setAdapter(adapter);


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Channel channel = new Channel("9TV", "ch", "111"); // todo set real data
                presenter.createChannel(channel);
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    @Override
    public void showChannel(Channel channel) {
        adapter.add(channel);
    }


}
