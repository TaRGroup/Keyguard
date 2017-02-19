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

package trumeet.keyguard.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

/**
 * Created by Administrator on 2017/2/19.
 * @author Trumeet
 */

public class ReceiverUtil {
    private Context mContext;
    private BroadcastReceiver mReceiver;

    public ReceiverUtil(Context context, BroadcastReceiver receiver) {
        mReceiver = receiver;
        mContext = context;
    }

    public void subscribe (IntentFilter filter) {
        mContext.registerReceiver(mReceiver, filter);
    }

    public boolean unsubscribe () {
        try {
            mContext.unregisterReceiver(mReceiver);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
