package com.artix.callnote.phone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.text.TextUtils

class PhoneStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
        val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incoming = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        if (stateStr == TelephonyManager.EXTRA_STATE_RINGING || stateStr == TelephonyManager.EXTRA_STATE_OFFHOOK) {
            if (!incoming.isNullOrEmpty()) {
                OverlayService.showOverlay(context, incoming)
            } else {
                // For outgoing or unknown numbers, stop overlay
                OverlayService.hideOverlay(context)
            }
        } else if (stateStr == TelephonyManager.EXTRA_STATE_IDLE) {
            OverlayService.hideOverlay(context)
        }
    }
}
