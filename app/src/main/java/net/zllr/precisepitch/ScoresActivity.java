package net.zllr.precisepitch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.zllr.precisepitch.helper.DataHisto;
import net.zllr.precisepitch.helper.LocalDatabaseHelper;
import net.zllr.precisepitch.helper.PdfExportHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ScoresActivity extends AppCompatActivity implements CompactCalendarView.CompactCalendarViewListener, View.OnClickListener {

    private RecyclerView dataList;
    private LocalDatabaseHelper databaseHelper = null;
    private boolean calendarIsHide;
    private Button dateSelector;
    private CompactCalendarView calendarView;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private FloatingActionButton sendScoresButton;

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
        dateSelector = (Button) findViewById(R.id.date_selector);
        dateSelector.setText(sdf.format(new Date()));
        dateSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (calendarIsHide){
                    calendarView.showCalendar();
                    calendarIsHide = false;
                }
                else {
                    calendarView.hideCalendar();
                    calendarIsHide = true;
                }
            }
        });
        calendarView = (CompactCalendarView) findViewById(R.id.calendarScores);
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);
        calendarView.addEvents(databaseHelper.getAllDate());
        calendarView.setCurrentDate(getFirstEvent(calendarView.getEventsForMonth(new Date())));
        calendarView.hideCalendar();
        calendarIsHide = true;
        calendarView.setListener(this);

        dataList = (RecyclerView) findViewById(R.id.data_entry_list);
        dataList.setHasFixedSize(true);
        List<DataHisto> listOfScore = databaseHelper.getHistoScoresFromDate(sdf.format(new Date()));
        dataList.setLayoutManager(new LinearLayoutManager(this));
        dataList.setAdapter(new AdapterScoreData(listOfScore));
        updateCasrdList(getFirstEvent(calendarView.getEventsForMonth(new Date())));

        sendScoresButton = (FloatingActionButton) findViewById(R.id.scoresFab);
        sendScoresButton.setImageDrawable(new IconicsDrawable(getApplicationContext())
                .icon(CommunityMaterial.Icon.cmd_file_send).color(Color.WHITE));
        sendScoresButton.setOnClickListener(this);
    }

    private Date getFirstEvent(List<Event> eventsForMonth) {
        long eventLong = 0;
        for (Event event : eventsForMonth){
            if (eventLong < event.getTimeInMillis()){
                eventLong = event.getTimeInMillis();
            }
        }
        Calendar calendar = Calendar.getInstance();
        if (eventLong != 0){
            calendar.setTimeInMillis(eventLong);
        }
        return calendar.getTime();
    }

    private void updateCasrdList(Date dateClicked){
        List<DataHisto> listOfScore = databaseHelper.getHistoScoresFromDate(sdf.format(dateClicked));
        dataList.setLayoutManager(new LinearLayoutManager(this));
        dataList.setAdapter(new AdapterScoreData(listOfScore));
        // Change date selected on Button text
        dateSelector.setText(sdf.format(dateClicked));
        // Hide calendar after date selected
        calendarView.hideCalendar();
        calendarIsHide = true;
    }

    @Override
    public void onClick(View view) {
        PdfExportHelper pdfExportHelper = new PdfExportHelper(view);
        pdfExportHelper.createDocument(view.getContext(), databaseHelper);
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
    public void onDayClick(Date dateClicked) {
        updateCasrdList(dateClicked);
    }

    @Override
    public void onMonthScroll(Date firstDayOfNewMonth) {
        // Nothing for now
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

    private class PersonViewHolder extends RecyclerView.ViewHolder {

        private TextView scaleSelected;
        private BarChart scaleChart;
        private TextView dataHistoNotes;

        public PersonViewHolder(View itemView) {
            super(itemView);
            scaleSelected = (TextView)itemView.findViewById(R.id.scale_selected);
            scaleChart = (BarChart)itemView.findViewById(R.id.chartScale);
            dataHistoNotes = (TextView)itemView.findViewById(R.id.notesCard);
        }

        public void bind(final DataHisto dataHisto){
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
            dataHistoNotes.setText(dataHisto.getNotes());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    final EditText noteInput = new EditText(view.getContext());
                    noteInput.setText(dataHisto.getNotes());
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
                            .setTitle(view.getContext().getString(R.string.notes_title_string))
                            .setView(noteInput)
                            .setPositiveButton(view.getContext().getString(R.string.notes_btn_string),
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Save new notes
                                    dataHisto.setNotes(noteInput.getText().toString());
                                    databaseHelper.updateDataHisto(dataHisto);
                                    dataHistoNotes.setText(dataHisto.getNotes());
                                }
                            });
                    builder.create().show();
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
