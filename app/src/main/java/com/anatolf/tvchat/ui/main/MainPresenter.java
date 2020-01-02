package com.anatolf.tvchat.ui.main;

import com.anatolf.tvchat.model.Channel;
import com.google.firebase.database.DataSnapshot;

/**
 * Связывает слой вью и слой модель
 */
public class MainPresenter {

    private MainContractView view;
    private final MainModel model;

    public MainPresenter() {
        this.model = new MainModel(); // создаём модель (получает данные из FB)
    }

    public void attachView(MainContractView view) {
        this.view = view;
    }

    public void detachView() {
        view = null;
    }


    public void autoDownloadChannels() {  // произошло пользовательское действие (типа нажал на лайк) // загружает каналы через модель
        model.downloadChannelsFromFireBase(new MainModel.FireBaseListener() {  // модель сообщает о завершении загрузки презентеру

            @Override
            public void onGetChannel(Channel channel) {
                if (view != null) {
                    view.showAddedChannel(channel);  // презентер оповещает вью о новых данных после завершения загрузки данных моделью
                }
            }

            @Override
            public void onGetUsersCountOnline(DataSnapshot dataSnapshot) {
                if (view != null) {
                    view.showUsersCountOnline(dataSnapshot);  // презентер оповещает вью о новых данных после завершения загрузки данных моделью
                }
            }
        });
    }
}
