package dev.ukanth.ufirewall.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import dev.ukanth.ufirewall.R;
import dev.ukanth.ufirewall.activity.AppDetailActivity;

/**
 * Created by Jabb0 on 09.11.2017.
 */

// ArrayAdapter for app based rules
public class AppBasedRulesListAdapter extends ArrayAdapter<AppBasedRule> {
    private Context ctx;

    public AppBasedRulesListAdapter(@NonNull Context context, @NonNull List<AppBasedRule> objects) {
        super(context,0, objects);
        this.ctx = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        AppBasedRule rule = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_app_based_rule, parent, false);
        }
        // Lookup view for data population
        TextView tvDest = (TextView) convertView.findViewById(R.id.tv_app_based_rule_dest);
        TextView tvPort = (TextView) convertView.findViewById(R.id.tv_app_based_rule_port);
        CheckBox cbEnabled = (CheckBox) convertView.findViewById(R.id.cb_app_based_rule_on);
        // Populate the data into the template view using the data object
        tvDest.setText(rule.dest);
        tvPort.setText(rule.port);
        cbEnabled.setChecked(rule.isEnabled);
        cbEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                rule.isEnabled = b;
            }
        });
        // Return the completed view to render on screen
        return convertView;
    }
}
