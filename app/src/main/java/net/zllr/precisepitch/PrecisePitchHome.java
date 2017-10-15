package net.zllr.precisepitch;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.zllr.precisepitch.adapters.HomeArrayAdapter;

public class PrecisePitchHome extends Activity implements OnItemClickListener {
    private static String entries[][];

    private ListView homeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_precise_pitch_home);
        entries = new String[][]{
                {getString(R.string.home_entry_tuner), getString(R.string.home_entry_tuner_description), "net.zllr.precisepitch.TunerActivity", "tuner"},
                {getString(R.string.home_entry_practice), getString(R.string.home_entry_practice_description), "net.zllr.precisepitch.PracticeActivity", "practice"},
                {getString(R.string.home_entry_history), getString(R.string.home_entry_history_description), "net.zllr.precisepitch.ScoresActivity", "scores"},
                {getString(R.string.home_entry_settings), getString(R.string.home_entry_settings_description), "net.zllr.precisepitch.ScoresActivity", "settings"},
        };

        homeList = (ListView) findViewById(R.id.homeList);
        homeList.setOnItemClickListener(this);

        HomeArrayAdapter adapter = new HomeArrayAdapter(this, R.layout.home_list_entry, entries);

        homeList.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        Class<?> c = null;
        String name = entries[position][2];
        if (name != null) {
            try {
                c = Class.forName(name);
                Intent intent = new Intent(this, c);
                startActivity(intent);
            } catch (ClassNotFoundException e) {
            }
        }
    }

}
