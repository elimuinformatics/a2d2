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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class LogTask implements Runnable {

	private static final LogTask INSTANCE = new LogTask();

	private BlockingQueue<LogEntry> queue = new LinkedBlockingQueue<>();
	private final Thread ourselves;

	private LogTask() {
		this.ourselves = Executors.privilegedThreadFactory().newThread(this);
		this.ourselves.start();
	}

	public static LogTask getInstance() {
		return INSTANCE;
	}

	@Override
	public void run() {
		try {
			int j = 0;
			while (true) {
				LogEntry entry = queue.poll();
				if (entry == null) {
					Thread.sleep(1000);
				} else {
					entry.execute();
				}
				j++;
				if (j == Integer.MIN_VALUE) {  // true at Integer.MAX_VALUE +1
					break;
				}
			}
		} catch (InterruptedException cont) {
			this.ourselves.interrupt();
		}
	}

	//log methods
	public void trace(String message) {
		queue.add(new LogEntry(LogEntry.LogLevel.TRACE, message));
	}

	public void trace(String message, Throwable error) {
		queue.add(new LogEntry(LogEntry.LogLevel.TRACE, message, error));
	}

	public void debug(String message) {
		queue.add(new LogEntry(LogEntry.LogLevel.DEBUG, message));
	}

	public void debug(String message, Throwable error) {
		queue.add(new LogEntry(LogEntry.LogLevel.DEBUG, message, error));
	}

	public void info(String message) {
		queue.add(new LogEntry(LogEntry.LogLevel.INFO, message));
	}

	public void info(String message, Throwable error) {
		queue.add(new LogEntry(LogEntry.LogLevel.INFO, message, error));
	}

	public void warn(String message) {
		queue.add(new LogEntry(LogEntry.LogLevel.WARN, message));
	}

	public void warn(String message, Throwable error) {
		queue.add(new LogEntry(LogEntry.LogLevel.WARN, message, error));
	}

	public void error(String message) {
		queue.add(new LogEntry(LogEntry.LogLevel.ERROR, message));
	}

	public void error(String message, Throwable error) {
		queue.add(new LogEntry(LogEntry.LogLevel.ERROR, message, error));
	}

}
