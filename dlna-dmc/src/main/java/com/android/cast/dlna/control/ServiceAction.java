package com.android.cast.dlna.control;

import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.TransportInfo;

interface ServiceAction {

    interface IServiceActionCallback<T> {
        void onSuccess(T result);

        void onFailed(String errMsg);
    }

    // --------------------------------------------------------------------------------
    // ---- AvService
    // --------------------------------------------------------------------------------
    interface IAVServiceAction {

        void cast(IServiceActionCallback<String> listener, String uri, String metadata);

        void play(IServiceActionCallback<Void> listener);

        void pause(IServiceActionCallback<Void> listener);

        void stop(IServiceActionCallback<Void> listener);

        void seek(IServiceActionCallback<Long> listener, final long position);

        void getPositionInfo(IServiceActionCallback<PositionInfo> listener);

        void getMediaInfo(IServiceActionCallback<MediaInfo> listener);

        void getTransportInfo(IServiceActionCallback<TransportInfo> listener);
    }

    // --------------------------------------------------------------------------------
    // ---- RendererService
    // --------------------------------------------------------------------------------
    interface IRendererServiceAction {
        void setVolume(IServiceActionCallback<Integer> listener, final int volume);

        void getVolume(IServiceActionCallback<Integer> listener);

        void setMute(IServiceActionCallback<Boolean> listener, boolean mute);

        void isMute(IServiceActionCallback<Boolean> listener);

        void setBrightness(IServiceActionCallback<Integer> listener, final int percent);

        void getBrightness(IServiceActionCallback<Integer> listener);
    }
}