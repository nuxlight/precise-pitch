package net.zllr.precisepitch.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.zllr.precisepitch.PrecisePitchHome;
import net.zllr.precisepitch.R;

public class HomeArrayAdapter extends ArrayAdapter {

    private String data[][] = null;

    public HomeArrayAdapter(Context context, int resource, String data[][]) {
        super(context, resource, data);
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_home_adaptor, parent, false);
        }
        HomeEntryHolder holder = new HomeEntryHolder();
        holder.homeListEntryImage = (ImageView) convertView.findViewById(R.id.img_home);
        holder.homeListEntryText1 = (TextView) convertView.findViewById(R.id.title_home);
        holder.homeListEntryText2 = (TextView) convertView.findViewById(R.id.description_home);

        convertView.setTag(holder);
        IconicsDrawable iconicsDrawable = null;
        switch (data[position][3]){
            case "tuner":
                iconicsDrawable = new IconicsDrawable(getContext())
                        .icon(CommunityMaterial.Icon.cmd_microphone).color(Color.LTGRAY).sizeDp(70);
            break;
            case "practice":
                iconicsDrawable = new IconicsDrawable(getContext())
                        .icon(CommunityMaterial.Icon.cmd_music_note_eighth).color(Color.LTGRAY).sizeDp(70);
            break;
            case "scores":
                iconicsDrawable = new IconicsDrawable(getContext())
                        .icon(CommunityMaterial.Icon.cmd_chart_areaspline).color(Color.LTGRAY).sizeDp(70);
            break;
            default:
                iconicsDrawable = new IconicsDrawable(getContext())
                        .icon(CommunityMaterial.Icon.cmd_alert_octagon).color(Color.LTGRAY).sizeDp(70);
            break;
        }
        holder.homeListEntryImage.setImageDrawable(iconicsDrawable);
        holder.homeListEntryText1.setText(data[position][0]);
        holder.homeListEntryText2.setText(data[position][1]);
        
        return convertView;
    }

    static class HomeEntryHolder {
        ImageView homeListEntryImage;
        TextView homeListEntryText1;
        TextView homeListEntryText2;
    }
}
