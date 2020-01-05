package com.anatolf.tvchat.ui.uploadchannels;

import com.anatolf.tvchat.model.Channel;


public class UploadChannelsToFirebasePresenter {

    private UploadChannelsToFirebaseContractView view;
    private UploadChannelsToFirebaseModel model;

    public UploadChannelsToFirebasePresenter() {
        this.model = new UploadChannelsToFirebaseModel(
                new UploadChannelsToFirebaseModel.FireBaseListener() {
                    @Override
                    public void onChannelAdded(Channel channel) {
                        view.showChannel(channel);
                    }
                });
    }

    public void attachView(UploadChannelsToFirebaseContractView view) {
        this.view = view;
    }

    public void detachView() {
        view = null;
    }

    public void createChannel(Channel channel) {
        model.createChannel(channel);
    }
}
