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

package io.elimu.a2d2.serviceapi.performance;

import java.util.HashMap;
import java.util.Map;

public class PerformanceHelper {

	private static final LogTask LOGGER = LogTask.getInstance();
    private static final PerformanceHelper INSTANCE = new PerformanceHelper();

    private ThreadLocal<Map<String, Long>> timeTable = new ThreadLocal<>();
    private ThreadLocal<StringBuilder> prefixes = new ThreadLocal<>();
    private static boolean perfomanceDebugEnabled;

    public static PerformanceHelper getInstance() {
            return INSTANCE;
    }

    private PerformanceHelper() {
            perfomanceDebugEnabled = Boolean.valueOf(System.getProperty("performance.log.enabled", "true"));
    }

    public void beginClock(String moduleName, String methodCalled) {
    	try {
    		if(perfomanceDebugEnabled){
    			long start = System.currentTimeMillis();
    			getMap().put(getKey(moduleName, methodCalled), start);
    			appendPrefix();
    		}
    	} catch (Throwable e) { /* make sure nothing fails because of Performance measurements*/ }
    }

    public long endClock(String moduleName, String methodCalled) {
    	long delta = -1;
    	try {
    		if(perfomanceDebugEnabled){
    			long end = System.currentTimeMillis();
    			//String prefix = getPrefix().toString();
    			String threadName = Thread.currentThread().getName();
    			prependPrefix();
    			String key = getKey(moduleName, methodCalled);
    			Long start = getMap().remove(key);
    			if (start == null) {
    				//beginClock was not used in this case. Get a stack trace into the log to be able
    				//to determine where the error might be in the code
    				addTrace(moduleName, methodCalled);
    			} else {
    				delta = end - start.longValue();
    				if(delta > 5000){
    					LOGGER.info("!!MORE THAN 5 SECONDS " + threadName + ": " + key + " - " + delta + "ms");
    				}else{
    					LOGGER.info(" " + threadName + ": " + key + " - " + delta + "ms");
    				}
    			}
    		}
        } catch (Throwable e) { /* make sure nothing fails because of Performance measurements*/ }
    	return delta;
    }

	private void addTrace(String moduleName, String methodCalled) {
		try {
			throw new IllegalArgumentException("beginClock() was never called for module name "
					+ moduleName + " and method " + methodCalled);
		} catch (IllegalArgumentException e) {
			LOGGER.error("Incorrect use of PerformaceHelper", e);
		}
	}

    private StringBuilder getPrefix() {
    	StringBuilder sb = prefixes.get();
    	if (sb == null) {
    		sb = new StringBuilder();
    		prefixes.set(sb);
    	}
    	return sb;
    }

    private void appendPrefix() {
    	StringBuilder sb = getPrefix();
    	sb.append("-");
    }

    private void prependPrefix() {
    	StringBuilder sb = getPrefix();
    	if (sb.length() > 0) {
    		sb.setLength(sb.length() - 1);
    	}
    }

	private String getKey(String moduleName, String methodCalled) {
		return moduleName + "-" + methodCalled;
	}

	private Map<String, Long> getMap() {
		Map<String, Long> timeMap = timeTable.get();
        if (timeMap == null) {
        	timeMap = new HashMap<String, Long>();
        	timeTable.set(timeMap);
        }
        return timeMap;
	}
}
