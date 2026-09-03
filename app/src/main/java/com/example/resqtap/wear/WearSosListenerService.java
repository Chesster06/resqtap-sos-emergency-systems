package com.example.resqtap.wear;

import com.example.resqtap.home.MainActivity;

import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;


/**
 * WearSosListenerService
 * Background listener kat phone: terima signal SOS dari smartwatch dan terus trigger phone.
 */
public class WearSosListenerService extends WearableListenerService {
    /** Fungsi untuk onMessageReceived. */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent == null || !WearSosBridge.SOS_MESSAGE_PATH.equals(messageEvent.getPath())) return;
        openMainForWatchSos();
    }

    /** Fungsi untuk onDataChanged. */
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        if (dataEvents == null) return;
        for (DataEvent event : dataEvents) {
            if (event == null || event.getType() != DataEvent.TYPE_CHANGED || event.getDataItem() == null) continue;
            Uri uri = event.getDataItem().getUri();
            if (uri != null && WearSosBridge.SOS_DATA_PATH.equals(uri.getPath())) {
                openMainForWatchSos();
                return;
            }
        }
    }

    /** Fungsi untuk openMainForWatchSos. */
    private void openMainForWatchSos() {
        WearSosBridge.markPending(this);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(WearSosBridge.triggerExtra(), true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
