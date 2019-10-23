package craftslab.werobot.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import craftslab.werobot.R;
import craftslab.werobot.activities.WebViewActivity;

public class GeneralSettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.general_preferences);
        setPrefListeners();
    }

    private void setPrefListeners() {
        Preference issuePref = findPreference("pref_etc_issue");
        issuePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent webViewIntent = new Intent(getActivity(), WebViewActivity.class);
                webViewIntent.putExtra("title", "GitHub Issues");
                webViewIntent.putExtra("url", getString(R.string.url_github_issues));
                webViewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(webViewIntent);
                return false;
            }
        });
    }
}
