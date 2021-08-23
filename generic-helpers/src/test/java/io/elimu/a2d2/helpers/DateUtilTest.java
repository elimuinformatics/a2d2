package io.elimu.a2d2.helpers;

import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

public class DateUtilTest {

	@Test
	public void testMSDiffRightTZ() {
		String anytz = differentTz();
		int offset = DateUtil.millisDiffFromTimeZoneToLocal(anytz);
		Assert.assertNotEquals(0, offset);
	}

	private String differentTz() {
		String anytz = TimeZone.getAvailableIDs()[0];
		if (TimeZone.getDefault().getID().equalsIgnoreCase(anytz)) {
			anytz = TimeZone.getAvailableIDs()[TimeZone.getAvailableIDs().length - 1];
		}
		return anytz;
	}

	@Test
	public void testMSDiffWrongTZ() {
		String anytz = TimeZone.getAvailableIDs()[0] + "_Wrong_value";
		int offset = DateUtil.millisDiffFromTimeZoneToLocal(anytz);
		Assert.assertEquals(0, offset);
	}
	
	@Test 
	public void testAdjustDateRightTZ() {
		String tz = differentTz();
		Date now = new Date();
		Date dt = DateUtil.adjustDateByTimezone(now, tz);
		Assert.assertNotEquals(now.getTime(), dt.getTime());
	}
	
	@Test 
	public void testAdjustDateWrongTZ() {
		String tz = TimeZone.getAvailableIDs()[0] + "_Wrong_value";
		Date now = new Date();
		Date dt = DateUtil.adjustDateByTimezone(now, tz);
		Assert.assertEquals(now.getTime(), dt.getTime());
	}
	
	@Test 
	public void testAdjustCronRightTZ() {
		String tz = differentTz();
		String cron = "0 0 8 ? * * *";
		String newcron = DateUtil.adjustCronStringByTimezone(cron, tz);
		Assert.assertNotEquals(cron, newcron);
	}
	
	@Test 
	public void testAdjustCronWrongTZ() {
		String tz = TimeZone.getAvailableIDs()[0] + "_Wrong_value";
		String cron = "0 0 8 ? * * *";
		String newcron = DateUtil.adjustCronStringByTimezone(cron, tz);
		Assert.assertEquals(cron, newcron);
	}
	
	@Test 
	public void testAdjustCronWrongCron1() {
		String tz = differentTz();
		String cron = "this is not a cron expression";
		String newcron = DateUtil.adjustCronStringByTimezone(cron, tz);
		Assert.assertEquals(cron, newcron);
	}

	@Test 
	public void testAdjustCronWrongCron2() {
		String tz = differentTz();
		String cron = "ThisIsNotACronExpressionEither";
		String newcron = DateUtil.adjustCronStringByTimezone(cron, tz);
		Assert.assertEquals(cron, newcron);
	}
}
