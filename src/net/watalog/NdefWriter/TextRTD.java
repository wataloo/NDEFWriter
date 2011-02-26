package net.watalog.NdefWriter;

import java.nio.charset.Charset;
import java.util.Locale;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

public class TextRTD extends Activity {
	private Tag mTag;
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private Boolean isPushing = false;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_rtd);
        
        mAdapter = NfcAdapter.getDefaultAdapter();
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Setup an intent filter for all MIME based dispatches
        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            filter.addDataType("*/*");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        mFilters = new IntentFilter[] {
                filter,
        };

        // Setup a tech list for all NfcF tags
        mTechLists = new String[][] { new String[] { NfcF.class.getName() } };
         
        final Button writeButton = (Button) findViewById(R.id.WriteButton);
        writeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v("My Tag", "The write button is tapped.");
                writeLog("");

               
            	if (mTag == null) {
            		writeLog("Failed writing the text to the tag.");
            		return;
            	}
            	Ndef ndef = Ndef.get(mTag);
            	
            	try {
            		ndef.connect();
            		ndef.writeNdefMessage(newMessage());
            		writeLog("Successfully wrote the text into the tag.");
            		ndef.close();
            	} catch (Exception e) {
            		writeLog("Failed writing the text to the tag.");
            	}
            }
        });

        final ToggleButton pushButton = (ToggleButton) findViewById(R.id.ToggleButton);
        pushButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		ToggleButton toggle = (ToggleButton)v;
        		Button writeButton = (Button) findViewById(R.id.WriteButton);
        		Activity activity = (Activity)v.getContext();
        		NfcAdapter adapter = NfcAdapter.getDefaultAdapter();
        		if (toggle.isChecked()) {
        			writeButton.setEnabled(false);
        			adapter.disableForegroundDispatch(activity);
        			adapter.enableForegroundNdefPush(activity, newMessage());
        		} else {
        			writeButton.setEnabled(true);
        			adapter.disableForegroundNdefPush(activity);
        			adapter.enableForegroundDispatch(activity, mPendingIntent, mFilters, mTechLists);
        		}
        	}
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!toggleIsChecked()) {
        	mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        } else {
        	mAdapter.enableForegroundNdefPush(this, newMessage());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
        mTag = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!toggleIsChecked()) {
        	mAdapter.disableForegroundDispatch(this);
        } else {
        	mAdapter.disableForegroundNdefPush(this);
        }
    }
    
    /*
     * This function is derived from ForgroundNDefPush.java in "ApiDemos" (Sample in Android SDK 10)
     */
    private static NdefRecord newTextRecord(String text, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));

        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = text.getBytes(utfEncoding);

        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length]; 
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }
    
    private NdefMessage newMessage() {
        EditText inputText = (EditText) findViewById(R.id.TextInput);
    	NdefMessage message = new NdefMessage(
    			new NdefRecord[] {newTextRecord(inputText.getText().toString(), Locale.ENGLISH, true)});
    	return message;
    }
    
    private void writeLog(String result) {
    	TextView resultText = (TextView) findViewById(R.id.ResultText);
    	resultText.setText(result);
    }
    
    private Boolean toggleIsChecked() {
        ToggleButton pushButton = (ToggleButton) findViewById(R.id.ToggleButton);
        return pushButton.isChecked();
    }
}
