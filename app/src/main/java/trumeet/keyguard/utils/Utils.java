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

import android.support.annotation.NonNull;

import java.math.BigDecimal;

import trumeet.keyguard.App;
import trumeet.keyguard.R;

/**
 * Created by Administrator on 2017/2/17.
 * @author Trumeet
 */

public class Utils {
    @NonNull
    public static String formatSecToStr (BigDecimal sec) {
        int[] times = splitToComponentTimes(sec);
        return convertIntArrayToString(times);
    }
    public static int[] splitToComponentTimes(BigDecimal biggy)
    {
        long longVal = biggy.longValue();
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours , mins , secs};
        return ints;
    }

    public static String convertIntArrayToString (int[] times) {
        return App.getContext().getString(R.string.text_time
                , String.valueOf(times[0])
                , String.valueOf(times[1])
                , String.valueOf(times[2]));
    }
}
