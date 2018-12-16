package net.precise_team.cellocoach.helper;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

public class ScoreBarFormater implements com.github.mikephil.charting.formatter.IValueFormatter {

    private DecimalFormat mFormat;

    public ScoreBarFormater() {
        mFormat = new DecimalFormat("###,###,##0.00");
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return mFormat.format(value);
    }
}
