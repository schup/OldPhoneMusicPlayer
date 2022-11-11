package code.name.monkey.retromusic.activities

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.tech.NfcF
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.nio.charset.StandardCharsets

class NfcActivity : AppCompatActivity() {

    private lateinit var pendingIntent: PendingIntent

    protected var nfcAdapter: NfcAdapter? = null

    //private var nfcAdapter: NfcAdapter;
    // https://github.com/marc136/tonuino-nfc-tools/blob/main/app/src/main/java/de/mw136/tonuino/nfc/NfcIntentActivity.kt
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        initNfcTagIntents()
    }


    private fun initNfcTagIntents() {
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0 /* PendingIntent.FLAG_MUTABLE*/)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }


    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onResume() {
        super.onResume()
        setupForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null) {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
                val ndefMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                if (! ndefMessages.isNullOrEmpty()) {
                    val records = (ndefMessages[0] as NdefMessage).records
                    if (! records.isNullOrEmpty()) {
                        val payload = records[0].payload
                        val payloadString = payload.toString(StandardCharsets.UTF_8)
                        Log.d("NFC", "payload: $payloadString")
                    }
                }
            }
        }
    }

    private fun setupForegroundDispatch(activity: Activity) {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter != null) {
            val intent = Intent(
                activity.applicationContext,
                activity.javaClass
            ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0)
            val ndfFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
            ndfFilter.addDataType("audio/album")

            nfcAdapter.enableForegroundDispatch(
                activity,
                pendingIntent, arrayOf(ndfFilter), arrayOf(arrayOf("android.nfc.tech.NfcF"))
            )

        }
    }

    private fun stopForegroundDispatch(activity: Activity) {
        NfcAdapter.getDefaultAdapter(this)?.disableForegroundDispatch(activity)
    }
}