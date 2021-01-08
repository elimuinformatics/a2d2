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

public class DateUtil {

	private DateUtil() {
		throw new IllegalStateException("Utility class");
	}
	
	public static Double fractionOfDayBefore(Date dt) {
	    return Double.valueOf((dt.getTime() % 86400000) / 86400000.0);
	}

	public static Double fractionOfDayAfter(Date dt) {
	    return Double.valueOf(1.0 - ((dt.getTime() % 86400000) / 86400000.0));
	}

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
