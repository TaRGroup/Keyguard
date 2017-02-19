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

package trumeet.keyguard.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import trumeet.keyguard.utils.MaskWindowUtils;

/**
 * Created by Administrator on 2017/2/17.
 * @author Trumeet
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MaskWindowUtils.TimerPrefsUtil util = new MaskWindowUtils.TimerPrefsUtil(context);
        if ((util.getTotal() - util.getUsed()) > 0) {
            new MaskWindowUtils(context).tryStart(false);
        }
    }
}
