package net.zllr.precisepitch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import net.zllr.precisepitch.adapters.HomeArrayAdapter;

public class PrecisePitchHome extends AppCompatActivity implements OnItemClickListener {
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
                {getString(R.string.home_entry_settings), getString(R.string.home_entry_settings_description), "net.zllr.precisepitch.SettingsActivity", "settings"},
        };
        homeList = (ListView) findViewById(R.id.homeList);
        homeList.setOnItemClickListener(this);
        HomeArrayAdapter adapter = new HomeArrayAdapter(this, R.layout.home_list_entry, entries);
        homeList.setAdapter(adapter);
        // Check if it's the first launch, if it's yes go to intro part
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (!prefs.getBoolean("FIRST_LAUNCH", false)){
            Intent intent = new Intent(this, WizardActivity.class);
            startActivity(intent);
        }
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
