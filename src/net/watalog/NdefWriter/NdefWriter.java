package net.watalog.NdefWriter;

import java.nio.charset.Charset;
import java.util.Locale;

import android.app.Activity;
import android.app.ListActivity;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class NdefWriter extends ListActivity {

    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);

	  setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getTitleList()));

	  ListView lv = getListView();
	  lv.setTextFilterEnabled(true);

	  lv.setOnItemClickListener(new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View view,
	        int position, long id) {
	    	Intent intent = new Intent(table[position][1]);
	    	startActivity(intent);
	    }
	  });
	}
	
	private String[] getTitleList() {
		String[] titles = new String[table.length];
		for (int i = 0; i < table.length; i++) {
			titles[i] = table[i][0];
		}
		return titles;
	}
	
	static final String[][] table = new String[][] {
		{"Text RTD", "net.watalog.NdefWriter.TEXT_RTD"},
		{"SmartPoster RTD", "net.watalog.NdefWriter.SMART_POSTER_RTD"}
	};
}