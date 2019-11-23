package com.oliva.marc.smsverification

import android.app.Activity
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.phone.SmsRetriever
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val senderSms = null //You can change to an number
    private val SMS_CONSENT_REQUEST = 200  // Set to an unused request code
    lateinit var smsBroadcastReceiver :SmsBroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startSmsUserConsent()

    }
    private fun registerToSmsBroadcastReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver().also {
            it.smsBroadcastReceiverListener = object : SmsBroadcastReceiver.SmsBroadcastReceiverListener {
                override fun onSuccess(intent: Intent?) {
                    intent?.let { context -> startActivityForResult(context,SMS_CONSENT_REQUEST ) }
                }
                override fun onFailure() {
                    Toast.makeText(applicationContext,"Fail BroadcastReceiver",Toast.LENGTH_LONG).show()
                }
            }
        }

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsBroadcastReceiver, intentFilter)
    }
    private fun startSmsUserConsent() {
        SmsRetriever.getClient(this).also {
            //We can add sender phone number or leave it blank
            it.startSmsUserConsent(senderSms)
                .addOnSuccessListener {
                    Log.d("MainActivity", "LISTENING_SUCCESS")
                }
                .addOnFailureListener {
                    Log.d("MainActivity", "LISTENING_FAILURE")
                }
        }
    }

    override fun onStart() {
        super.onStart()
        registerToSmsBroadcastReceiver()
    }

    override fun onPause() {
        super.onPause()

        unregisterReceiver(smsBroadcastReceiver)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SMS_CONSENT_REQUEST -> {
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    //That gives all message to us. We need to get the code from inside with regex
                    val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                    val code = message?.let { fetchVerificationCode(it) }
                    /*The message should be for exmaple: Your code verification is : 123456
                    * Because you need to write letters and numbers */
                    sms_text.setText(code)
                }
            }
        }
    }

    private fun fetchVerificationCode(message: String): String {
        return Regex("(\\d{6})").find(message)?.value ?: ""
    }
}
