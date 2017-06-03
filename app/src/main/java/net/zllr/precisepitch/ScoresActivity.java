package net.zllr.precisepitch;

import android.os.Bundle;
import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import net.zllr.precisepitch.helper.LocalDatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScoresActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private RecyclerView dataList;
    private LocalDatabaseHelper databaseHelper = null;
    private Spinner dateSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);
        //Get database helper for getting data
        try {
            databaseHelper = new LocalDatabaseHelper(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Spinner settings
        dateSelector = (Spinner) findViewById(R.id.date_selector);
        ArrayAdapter<String> spinerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, databaseHelper.getAllDate());
        spinerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSelector.setAdapter(spinerAdapter);
        dateSelector.setOnItemSelectedListener(this);

        dataList = (RecyclerView) findViewById(R.id.data_entry_list);
        dataList.setHasFixedSize(true);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Map<String,Object> listOfScore = (Map<String, Object>) databaseHelper.getHistoScoresFromDate(databaseHelper.getAllDate().get(i))
                .get(databaseHelper.getAllDate().get(i));
        dataList.setLayoutManager(new LinearLayoutManager(this));
        dataList.setAdapter(new AdapterScoreData(listOfScore));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private class AdapterScoreData extends RecyclerView.Adapter<PersonViewHolder> {

        private Map<String,Object> dataEntry;

        public AdapterScoreData(Map<String,Object> dataEntry) {
            this.dataEntry = dataEntry;
        }

        @Override
        public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.score_card_layout,parent,false);
            return new PersonViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PersonViewHolder personViewHolder, int position) {
            MyCardData cardData = new MyCardData(dataEntry.keySet().toArray(new String[dataEntry.keySet().size()])[position],
                    formatScore(dataEntry.get(dataEntry.keySet().toArray()[position])));
            personViewHolder.bind(cardData);
        }

        @Override
        public int getItemCount() {
            return dataEntry.size();
        }

        private String formatScore(Object scores){
            List<Double> roundList = new ArrayList<>();
            for (Double score : (List<Double>) scores){
                roundList.add(round(score,2));
            }
            return roundList.toString();
        }

        /**
         * Thanks StackOverFlow :
         * http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
         * @param value
         * @param places
         * @return
         */
        public double round(double value, int places) {
            if (places < 0) throw new IllegalArgumentException();
            long factor = (long) Math.pow(10, places);
            value = value * factor;
            long tmp = Math.round(value);
            return (double) tmp / factor;
        }
    }

    public static class PersonViewHolder extends RecyclerView.ViewHolder {

        private TextView scaleSelected;
        private TextView scaleScores;

        public PersonViewHolder(View itemView) {
            super(itemView);
            scaleSelected = (TextView)itemView.findViewById(R.id.scale_selected);
            scaleScores = (TextView)itemView.findViewById(R.id.scale_scores);
        }

        public void bind(MyCardData myCardData){
            scaleSelected.setText(myCardData.getScale());
            scaleScores.setText(myCardData.getScores());
        }
    }

    public class MyCardData{
        private String scale;
        private String scores;

        public MyCardData(String scale, String scores) {
            this.scale = scale;
            this.scores = scores;
        }

        public String getScale() {
            return scale;
        }

        public void setScale(String scale) {
            this.scale = scale;
        }

        public String getScores() {
            return scores;
        }

        public void setScores(String scores) {
            this.scores = scores;
        }
    }
}
