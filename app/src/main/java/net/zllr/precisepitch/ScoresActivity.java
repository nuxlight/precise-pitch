package net.zllr.precisepitch;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import net.zllr.precisepitch.helper.DataHisto;
import net.zllr.precisepitch.helper.LocalDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

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
        List<DataHisto> listOfScore = databaseHelper.getHistoScoresFromDate(databaseHelper.getAllDate().get(i));
        dataList.setLayoutManager(new LinearLayoutManager(this));
        dataList.setAdapter(new AdapterScoreData(listOfScore));
    }

    @Override
    public void onBackPressed() {
        databaseHelper.close();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private class AdapterScoreData extends RecyclerView.Adapter<PersonViewHolder> {

        private List<DataHisto> dataEntryList;

        public AdapterScoreData(List<DataHisto> dataEntry) {
            this.dataEntryList = dataEntry;
        }

        @Override
        public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.score_card_layout,parent,false);
            return new PersonViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PersonViewHolder personViewHolder, int position) {
            personViewHolder.bind(dataEntryList.get(position));
        }

        @Override
        public int getItemCount() {
            return dataEntryList.size();
        }

    }

    public static class PersonViewHolder extends RecyclerView.ViewHolder {

        private TextView scaleSelected;
        private BarChart scaleChart;

        public PersonViewHolder(View itemView) {
            super(itemView);
            scaleSelected = (TextView)itemView.findViewById(R.id.scale_selected);
            scaleChart = (BarChart)itemView.findViewById(R.id.chartScale);
        }

        public void bind(DataHisto dataHisto){
            scaleSelected.setText(dataHisto.getScaleName());
            List<BarEntry> entries = new ArrayList<BarEntry>();
            int iterator = 0;
            for (Double value : dataHisto.getScores()){
                entries.add(new BarEntry(iterator, value.floatValue()));
                iterator++;
            }
            BarDataSet dataSet = new BarDataSet(entries, dataHisto.getScaleName());
            dataSet.setColor(Color.parseColor("#be9c91"));
            dataSet.setValueTextColor(Color.GRAY);
            dataSet.setValueTextSize(12f);
            BarData barData = new BarData(dataSet);
            scaleChart.setData(barData);
            scaleChart.getXAxis().setEnabled(false);
            scaleChart.getAxisLeft().setEnabled(false);
            scaleChart.getAxisRight().setEnabled(false);
            scaleChart.getLegend().setEnabled(false);
            scaleChart.animateXY(1000,1000);
            scaleChart.zoomOut();
            scaleChart.zoomOut();
            Description description = new Description();
            description.setText("");
            scaleChart.setDescription(description);
            scaleChart.setScaleEnabled(false);
            scaleChart.setTouchEnabled(false);
            scaleChart.invalidate(); // refresh

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
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
}
