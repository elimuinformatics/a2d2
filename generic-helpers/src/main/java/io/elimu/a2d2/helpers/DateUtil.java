// Copyright 2018-2020 Elimu Informatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.elimu.a2d2.helpers;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;

/**
 * Date utilities initially used by MME 
 */
public class DateUtil {

	private DateUtil() {
		throw new IllegalStateException("Utility class");
	}
	
	/**
	 * Returns, as a fraction, how much of the day passed after the beggining of the day
	 * i.e.
	 * <ul>
	 * <li>if you pass a date 2021-01-01 12:00:00, it will return 0.5</li>
	 * <li>if you pass a date 2021-10-05 18:00:00, it will return 0.75</li>
	 * <li>if you pass a date 2021-10-05 06:00:00, it will return 0.25</li>
	 * </ul>
	 * @param dt a given Date
	 * @return a double value between 0.0 and 1.0
	 */
	public static Double fractionOfDayBefore(Date dt) {
	    return Double.valueOf((dt.getTime() % 86400000) / 86400000.0);
	}

	/**
	 * Returns, as a fraction, how much of the day is left until the beggining of the next day
	 * i.e.
	 * <ul>
	 * <li>if you pass a date 2021-01-01 12:00:00, it will return 0.5</li>
	 * <li>if you pass a date 2021-10-05 18:00:00, it will return 0.25</li>
	 * <li>if you pass a date 2021-10-05 06:00:00, it will return 0.5</li>
	 * </ul>
	 * @param dt a given Date
	 * @return a double value between 0.0 and 1.0
	 */
	public static Double fractionOfDayAfter(Date dt) {
	    return Double.valueOf(1.0 - ((dt.getTime() % 86400000) / 86400000.0));
	}

	/**
	 * Returns a Date with the start of the day of the given parameter
	 * i.e.
	 * <ul>
	 * <li>if you pass a date 2021-01-01 12:00:00, it will return a date repesenting 2021-01-01 00:00:00</li>
	 * <li>if you pass a date 2021-10-05 18:00:00, it will return a date repesenting 2021-10-05 00:00:00</li>
	 * <li>if you pass a date 2021-10-06 06:00:00, it will return a date repesenting 2021-10-06 00:00:00</li>
	 * </ul>
	 * @param param a given Date
	 * @return the Date at the beggining of param's day
	 */
	public static Date dayStart(Date param) {
	    GregorianCalendar cal = new GregorianCalendar();
	    cal.setTime(param);
	    cal.clear(Calendar.HOUR_OF_DAY);
	    cal.clear(Calendar.MINUTE);
	    cal.clear(Calendar.SECOND);
	    cal.clear(Calendar.MILLISECOND);
	    cal.add(Calendar.MILLISECOND, cal.getTimeZone().getRawOffset());
	    return cal.getTime();
	}
}
