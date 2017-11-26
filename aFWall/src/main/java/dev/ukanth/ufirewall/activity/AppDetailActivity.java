package dev.ukanth.ufirewall.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.TrafficStats;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import dev.ukanth.ufirewall.Api;
import dev.ukanth.ufirewall.R;
import dev.ukanth.ufirewall.log.Log;
import dev.ukanth.ufirewall.util.AppBasedRule;
import dev.ukanth.ufirewall.util.AppBasedRulesListAdapter;

public class AppDetailActivity extends AppCompatActivity {
    public static final String TAG = "AFWall";
    private static String packageName = "";

    ArrayList<AppBasedRule> rules;
    AppBasedRulesListAdapter adapter;
    Switch allow_selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.traffic_detail_title));
        setContentView(R.layout.app_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Context ctx = getApplicationContext();

        ImageView image = (ImageView) findViewById(R.id.app_icon);
        TextView textView = (TextView) findViewById(R.id.app_title);
        TextView textView2 = (TextView) findViewById(R.id.app_package);
        TextView up = (TextView) findViewById(R.id.up);
        TextView down = (TextView) findViewById(R.id.down);
        
        /**/

        int appid = getIntent().getIntExtra("appid", -1);
        if (appid > 0) {

            final PackageManager packageManager = getApplicationContext().getPackageManager();
            final String[] packageNameList = ctx.getPackageManager().getPackagesForUid(appid);

            if (packageNameList != null) {
                packageName = packageNameList.length > 0 ? packageNameList[0] : ctx.getPackageManager().getNameForUid(appid);
            } else {
                packageName = ctx.getPackageManager().getNameForUid(appid);
            }


            Button button = (Button) findViewById(R.id.app_settings);
            button.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Api.showInstalledAppDetails(getApplicationContext(), packageName);
                }
            });
            ApplicationInfo applicationInfo;

            try {
                applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                image.setImageDrawable(applicationInfo.loadIcon(packageManager));
                textView.setText(packageManager.getApplicationLabel(applicationInfo));
                if (packageNameList.length > 1) {
                    textView2.setText(Arrays.toString(packageNameList));
                    button.setEnabled(false);
                } else {
                    textView2.setText(packageName);
                }
                setTotalBytesManual(down, up, applicationInfo.uid);
            } catch (final NameNotFoundException e) {
                down.setText(" : " + humanReadableByteCount(0, false));
                up.setText(" : " + humanReadableByteCount(0, false));
                button.setEnabled(false);
            }
        }

        Button add_firewall_rule = (Button) findViewById(R.id.add_app_based_rule);
        add_firewall_rule.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context ctx = AppDetailActivity.this;
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle("Add Firewall rule");

                final EditText ip = new EditText(ctx);
                ip.setHint("Ip/Domain:port/from-to");
                ip.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                builder.setView(ip);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        addFireWallRule(ip.getText().toString());
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.show();
            }
        });

        allow_selected = (Switch) findViewById(R.id.app_based_allow_mode);
        allow_selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setRestrictMode(compoundButton.isChecked());
            }
        });

        rules = new ArrayList<>();
        adapter = new AppBasedRulesListAdapter(this, rules);
        ListView lvRules = (ListView) findViewById(R.id.app_based_rules);
        lvRules.setAdapter(adapter);

    }

    private void setRestrictMode(boolean whitelist){
        if(whitelist) {
            allow_selected.setText(R.string.mode_whitelist);
        } else {
            allow_selected.setText(R.string.mode_blacklist);
        }
        for(AppBasedRule rule : rules) {
            Log.d("rule test", rule.toString());
        }
    }

    private void addFireWallRule(String dest) {

        // FIXME: correct parsing
        String[] parsed = dest.split(":");
        String ip = parsed[0];
        String port = "";
        if (parsed.length == 2)
            port = parsed[1];
        adapter.add(new AppBasedRule(ip, port, false));
    }

    private void setTotalBytesManual(TextView down, TextView up, int localUid) {
        File dir = new File("/proc/uid_stat/");
        down.setText(" : " + humanReadableByteCount(Long.parseLong("0"), false));
        up.setText(" : " + humanReadableByteCount(Long.parseLong("0"), false));
        if(dir.exists()) {
            String[] children = dir.list();
            if (!Arrays.asList(children).contains(String.valueOf(localUid))) {
                down.setText(" : " + humanReadableByteCount(Long.parseLong("0"), false));
                up.setText(" : " + humanReadableByteCount(Long.parseLong("0"), false));
                return;
            }
            File uidFileDir = new File("/proc/uid_stat/" + String.valueOf(localUid));
            if(uidFileDir.exists()) {
                File uidActualFileReceived = new File(uidFileDir, "tcp_rcv");
                File uidActualFileSent = new File(uidFileDir, "tcp_snd");
                String textReceived = "0";
                String textSent = "0";
                try {
                    if(uidActualFileReceived.exists() && uidActualFileSent.exists() ) {
                        BufferedReader brReceived = new BufferedReader(new FileReader(uidActualFileReceived));
                        BufferedReader brSent = new BufferedReader(new FileReader(uidActualFileSent));
                        String receivedLine;
                        String sentLine;
                        if ((receivedLine = brReceived.readLine()) != null) {
                            textReceived = receivedLine;
                        }
                        if ((sentLine = brSent.readLine()) != null) {
                            textSent = sentLine;
                        }
                        down.setText(" : " + humanReadableByteCount(Long.parseLong(textReceived), false));
                        up.setText(" : " + humanReadableByteCount(Long.parseLong(textSent), false));
                        brReceived.close();
                        brSent.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception while reading tx bytes: " + e.getLocalizedMessage());
                }
            }
        }
       // return Long.valueOf(textReceived).longValue() + Long.valueOf(textReceived).longValue();
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < 0) return "0 B";
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
