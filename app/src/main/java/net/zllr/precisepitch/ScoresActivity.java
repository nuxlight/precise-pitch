package net.zllr.precisepitch;

import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;

import com.couchbase.lite.CouchbaseLiteException;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import net.zllr.precisepitch.helper.LocalDatabaseHelper;

import java.io.IOException;

public class ScoresActivity extends Activity {

    private ListView dataEntry;
    private LocalDatabaseHelper databaseHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        try {
            databaseHelper = new LocalDatabaseHelper(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        dataEntry = (ListView) findViewById(R.id.data_entry_list);
        Spinner dateSelector = (Spinner) findViewById(R.id.date_selector);
        ArrayAdapter<String> spinerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, databaseHelper.getAllDate());
        spinerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSelector.setAdapter(spinerAdapter);
    }
}
