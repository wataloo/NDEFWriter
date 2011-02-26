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

public class SmartPosterRTD extends Activity {
	private Tag mTag;
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smart_poster_rtd);
        
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
         
        final Button button = (Button) findViewById(R.id.WriteButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v("My Tag", "The write button is tapped.");
                writeLog("");
                EditText inputText = (EditText) findViewById(R.id.TextInput);
                EditText inputUri = (EditText) findViewById(R.id.UriInput);
                NdefRecord textRecord = newTextRecord(inputText.getText().toString(), Locale.ENGLISH, true);
                NdefRecord uriRecord = newUriRecord(inputUri.getText().toString());
                NdefRecord spRecord = newSpRecord(textRecord, uriRecord);
            	NdefMessage message = new NdefMessage(
            			new NdefRecord[] {spRecord});
            	
            	if (mTag == null) {
            		writeLog("Failed writing the text to the tag.");
            		return;
            	}
            	Ndef ndef = Ndef.get(mTag);
            	
            	try {
            		ndef.connect();
            		ndef.writeNdefMessage(message);
            		writeLog("Successfully wrote the text into the tag.");
            		ndef.close();
            	} catch (Exception e) {
            		writeLog("Failed writing the text to the tag.");
            	}
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
        mTag = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    }

    @Override
    public void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
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
    
    private static NdefRecord newUriRecord(String uri) {
    	byte[] uriBytes = uri.getBytes(Charset.forName("UTF-8"));
    	byte[] buf = new byte[1 + uriBytes.length];
    	buf[0] = 0; // unabridged URI
    	System.arraycopy(uriBytes, 0, buf, 1, uriBytes.length);
    	return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, new byte[0], buf);
    }
    
    private static NdefRecord newSpRecord(NdefRecord text, NdefRecord uri) {
    	byte[] textRecordBytes = text.toByteArray();
    	textRecordBytes[0] = (byte)((textRecordBytes[0] | 0x80) & ~0x40); // set MB=1, ME=0
    	byte[] uriRecordBytes = uri.toByteArray();
    	uriRecordBytes[0] = (byte)((uriRecordBytes[0] & ~0x80) | 0x40); // set MB=0, ME=1
    	byte[] buf = new byte[textRecordBytes.length + uriRecordBytes.length];
    	System.arraycopy(textRecordBytes, 0, buf, 0, textRecordBytes.length);
    	System.arraycopy(uriRecordBytes, 0, buf, textRecordBytes.length, uriRecordBytes.length);
    	return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_SMART_POSTER, new byte[0], buf);
    }
    
    
    private void writeLog(String result) {
    	TextView resultText = (TextView) findViewById(R.id.ResultText);
    	resultText.setText(result);
    }
}
