/*
 * Copyright (C) 2017 TaRGroup
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package trumeet.keyguard.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.math.BigDecimal;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;
import trumeet.keyguard.R;
import trumeet.keyguard.utils.MaskWindowUtils;
import trumeet.keyguard.utils.Utils;

/**
 * Created by Trumeet on 2017/2/17.
 * @author Trumeet
 */

public class SettingsFragment extends PreferenceFragment {
    private MaskWindowUtils.TimerPrefsUtil mPrefsUtil;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefsUtil = new MaskWindowUtils.TimerPrefsUtil(getActivity());
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onStart () {
        super.onStart();
        final Preference timerPreference = findPreference(getString(R.string.settings_timer));
        timerPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                TimePickerDialog dialog = new TimePickerDialog();
                int[] timers = Utils.splitToComponentTimes(BigDecimal.valueOf(mPrefsUtil.getTotal()));
                dialog.initialize(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePickerDialog view
                            , int hourOfDay, int minute, int second) {
                        mPrefsUtil.updateTotal(
                                hourOfDay*60*60 +
                                        minute*60 +
                                        second
                        );
                        timerPreference.setSummary(mPrefsUtil.getTotalStr());
                        mPrefsUtil.updateUsed(0);
                    }
                }, timers[0], timers[1], timers[2], true);
                dialog.enableSeconds(true);
                dialog.show(getFragmentManager(), "TimePick");
                return false;
            }
        });
        timerPreference.setSummary(mPrefsUtil.getTotalStr());

        findPreference(getString(R.string.action_about))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(Intent.ACTION_VIEW
                                , Uri.parse("https://github.com/TaRGroup/Keyguard")));
                        return false;
                    }
                });

        findPreference(getString(de.psdev.licensesdialog.R.string.notices_title))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Notice materialDateTimePicker = new Notice("MaterialDateTimePicker"
                                , "https://github.com/wdullaer/MaterialDateTimePicker"
                                , "Copyright (c) 2015 Wouter Dullaert"
                                , new ApacheSoftwareLicense20());

                        Notices notices = new Notices();
                        notices.addNotice(materialDateTimePicker);
                        LicensesDialog dialog = new LicensesDialog.Builder(getActivity())
                                .setNotices(notices)
                                .setIncludeOwnLicense(true)
                                .build();
                        dialog.showAppCompat();
                        return false;
                    }
                });

        findPreference(getString(R.string.settings_history))
                .setSummary(getString(R.string.notification_finish_text,
                        Utils.formatSecToStr(BigDecimal.valueOf(mPrefsUtil.getHistoryTime())),
                        String.valueOf(mPrefsUtil.getHistoryScreenOn())));
    }
}
