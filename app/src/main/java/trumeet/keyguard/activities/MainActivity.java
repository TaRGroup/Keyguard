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

package trumeet.keyguard.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import trumeet.keyguard.R;
import trumeet.keyguard.fragments.SettingsFragment;
import trumeet.keyguard.utils.MaskWindowUtils;

/**
 * @author Trumeet
 */

public class MainActivity extends AppCompatActivity {
    private MaskWindowUtils mMaskUtil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMaskUtil = new MaskWindowUtils(this);

        getFragmentManager().beginTransaction().add(R.id.frame, new SettingsFragment()).commitAllowingStateLoss();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMaskUtil.tryStart();
            }
        });
    }
}
