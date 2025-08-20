package com.artix.callnote.phone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class OutgoingCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
        if (!number.isNullOrEmpty()) {
            OverlayService.showOverlay(context, number)
        }
    }
}


