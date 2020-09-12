package commitware.ayia.covid19.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import commitware.ayia.covid19.Controllers.AppController;
import commitware.ayia.covid19.fragments.ListActivityFragment;
import commitware.ayia.covid19.interfaces.OnFragmentInteractionListener;
import commitware.ayia.covid19.models.Country;
import commitware.ayia.covid19.models.CountryServer;
import commitware.ayia.covid19.R;

import static commitware.ayia.covid19.Controllers.AppUtils.LIST_REQUEST;
import static commitware.ayia.covid19.Controllers.AppUtils.LIST_TYPE;
import static commitware.ayia.covid19.Controllers.AppUtils.LIST_TYPE_LOCAL;
import static commitware.ayia.covid19.Controllers.AppUtils.LOCATION_COUNTRY;
import static commitware.ayia.covid19.Controllers.AppUtils.LOCATION_STATE;

public class SettingsActivity extends AppCompatActivity implements OnFragmentInteractionListener{
    private static final String TITLE_TAG = "settingsActivityTitle";

    LinearLayout linearLayout;

    String state, country;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        linearLayout = findViewById(R.id.linearLayout);




        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }
        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                            setTitle(R.string.title_activity_settings);
                           // linearLayout.setVisibility(View.VISIBLE);
                        }
                    }
                });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if(AppController.getInstance().getAppType().equals("covidNg")) {
            SharedPreferences pref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
            state = pref.getString("state", null);
            country = pref.getString("country", null);
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }


    @Override
    public void listItemClickServer(CountryServer countryServer) {
    }

    @Override
    public void listItemClickSetting(Country country, String location, String listType) {

        SharedPreferences getSharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor e = getSharedPreferences.edit();
        String countryName;
        String continent;
        String state;
        String id ;

        switch (location){
            case LOCATION_STATE:
                state = country.getName();
                id = country.getContinent();
                e.putString("state", state);
                e.putString("id", id);
                AppController.getInstance().setId(id);
                AppController.getInstance().setState(state);
                break;
            case LOCATION_COUNTRY:
                countryName =  country.getName();
                continent = country.getContinent();
                e.putString("country", countryName);
                e.putString("continent",continent);
                AppController.getInstance().setCountry(countryName);
                AppController.getInstance().setContinent(continent);
                break;
        }
        e.apply();



        onBackPressed();

    }


    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {


            if(AppController.getInstance().getAppType().equals("covidNg"))
            {
                setPreferencesFromResource(R.xml.root_preferences, rootKey);

                Preference prefState = findPreference("state");
                if (prefState != null) {
                    prefState.setSummary(AppController.getInstance().getState());
                    prefState.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference preference) {

                            Intent it = new Intent(getActivity(), ListActivity.class);
                            it.putExtra(LIST_REQUEST, LOCATION_STATE);
                            it.putExtra(LIST_TYPE, LIST_TYPE_LOCAL);
                            startActivity(it);
                            getActivity().finish();

                            return true;
                        }
                    });
                }
            }

            Preference prefFeedBack = findPreference("feedback");
            if (prefFeedBack != null) {
                prefFeedBack.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        sendFeedback(getActivity());
                        return true;
                    }
                });
            }
            Preference prefRateApp = findPreference("rateApp");
            if (prefRateApp != null) {
                prefRateApp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        rateApp(getActivity());
                        return true;
                    }
                });
            }
            ListPreference themeListPreference = findPreference("theme");

            if (themeListPreference != null) {

                int currentMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (currentMode)
                {
                    case Configuration.UI_MODE_NIGHT_NO:
                        themeListPreference.setValueIndex(1);
                        break;
                    case Configuration.UI_MODE_NIGHT_YES:
                        themeListPreference.setValueIndex(0);
                        break;
                }
                themeListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {

                        if(newValue.toString().equals("DarkTheme"))
                        {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        }
                        else if(newValue.toString().equals("LightTheme")){

                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        }
                        return true;
                    }
                });
            }





        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    public static void sendFeedback(Context context) {
        String body = null;
        try {
            body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nPlease don't remove this information\nState: "+AppController.getInstance().getState()+"\nDevice OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
        } catch (PackageManager.NameNotFoundException e) {
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"commitware@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Query from android app");
        intent.putExtra(Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.choose_email_client)));
    }

    public static void rateApp(Context context)
    {
//        String url = "https://play.google.com/store/apps/details?id=YOUR PACKAGE NAME";
//        Intent i = new Intent(Intent.ACTION_VIEW);
//        i.setData(Uri.parse(url));
//        context.startActivity(i);
    }

}