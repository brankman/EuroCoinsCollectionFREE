package moedas.euro.free;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import androidx.core.app.NavUtils;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || DataSettingsPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            final Preference pref_about = findPreference("pref_key_about");
            pref_about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), AboutActivity.class));
                    return false;
                }
            });

            final Preference pref_rate_it = findPreference("pref_key_rate_it");
            pref_rate_it.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {

                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=moedas.euro.free")));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=moedas.euro.free&feature=search_result#?t=W251bGwsMSwxLDEsIm1vZWRhcy5ldXJvLmZyZWUiXQ..")));
                    }

                    return false;
                }
            });

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                NavUtils.navigateUpTo(getActivity(), new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSettingsPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            setHasOptionsMenu(true);

            final Preference pref_csv_import_normal = findPreference("pref_key_csv_import_normal");
            pref_csv_import_normal.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {

                    final ProgressDialog progDialog = ProgressDialog.show(getActivity(), "", getString(R.string.dialog_loading_data_backup));

                    Thread loadThread = new Thread(){
                        public void run(){
                            try{
                                Functions classFunctions = new Functions(getActivity().getApplication().getApplicationContext());
                                String extStorageDirectory = Environment.getExternalStorageDirectory().toString();

                                File myFile = new File(extStorageDirectory+"/EuroCoinsCollection/coins.csv");
                                if(myFile.exists())
                                {
                                    classFunctions.readCSVNormal(extStorageDirectory+"/EuroCoinsCollection/coins.csv");
                                }

                                myFile = new File(extStorageDirectory + "/EuroCoinsCollection/coins_commecorative.csv");
                                if(myFile.exists())
                                {
                                    classFunctions.readCSVCommemorative(extStorageDirectory + "/EuroCoinsCollection/coins_commecorative.csv");
                                }

                                myFile = new File(extStorageDirectory + "/EuroCoinsCollection/coinsBackUp.csv");
                                if(myFile.exists())
                                {
                                    classFunctions.readFromCSV(extStorageDirectory + "/EuroCoinsCollection/coinsBackUp.csv");
                                }

                                //Toast.makeText(getActivity(), R.string.toast_collection_imported, Toast.LENGTH_SHORT).show();
                            }catch (Exception e) {
                                e.printStackTrace();
                            }finally{
                                progDialog.dismiss();
                            }
                        }
                    };
                    loadThread.start();


                    return false;
                }
            });

            final Preference pref_csv_export_normal = findPreference("pref_key_csv_export_normal");
            pref_csv_export_normal.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Functions classFunctions = new Functions(getActivity().getApplication().getApplicationContext());
                    classFunctions.writeToCSV();

                    Toast.makeText(getActivity(), R.string.toast_collection_exported, Toast.LENGTH_SHORT).show();

                    return false;
                }
            });

            final Preference pref_key_reset_data = findPreference("pref_key_reset_data");
            pref_key_reset_data.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {

                    final ProgressDialog progDialog = ProgressDialog.show(getActivity(), "", getString(R.string.dialog_loading_reset_data));

                    Thread loadThread = new Thread(){
                        public void run(){
                            try{
                                Functions classFunctions = new Functions(getActivity().getApplication().getApplicationContext());
                                classFunctions.importInitialDB();
                                //Toast.makeText(getActivity(), R.string.toast_reset_default_data, Toast.LENGTH_SHORT).show();
                            }catch (Exception e) {
                                e.printStackTrace();
                            }finally{
                                progDialog.dismiss();
                            }
                        }
                    };
                    loadThread.start();


                    return false;
                }
            });


            final Preference pref_clear_data = findPreference("pref_key_clear_data");
            pref_clear_data.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {

                    /**limpa todos os ficheiros da pasta da aplicação**/
                    String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
                    String moedas = "/EuroCoinsCollection";
                    final File moedasDir = new File(extStorageDirectory + moedas);

                    if(moedasDir.isDirectory())
                    {
                        String[] filesInFolder = moedasDir.list();
                        for (int i = 0; i < filesInFolder.length; i++) {
                            new File(moedasDir, filesInFolder[i]).delete();
                        }
                    }

                    Toast.makeText(getActivity(), R.string.toast_clear_data, Toast.LENGTH_SHORT).show();

                    return false;
                }
            });

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                NavUtils.navigateUpTo(getActivity(), new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

}
