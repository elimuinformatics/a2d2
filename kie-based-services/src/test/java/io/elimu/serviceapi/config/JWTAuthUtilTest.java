package io.elimu.serviceapi.config;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import io.elimu.genericapi.service.GenericKieBasedService;
import io.elimu.genericapi.service.RunningServices;

public class JWTAuthUtilTest {

	@Test
	public void testTokenParsingExpired() {
		String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJ4eXphNzg5MWxoeE1W"
				+ "WUdDT043TGduS1paOEhRR0Q1SCIsInN1YiI6Ijk2MjZmNDQxMDU1MzQ5Y2U4Y2I3ZDdkNW"
				+ "E0ODNlYWEyIiwidCI6ImVwaWNfaWQiLCJzY29wZSI6ImJhc2ljX3Byb2ZpbGUgZnJpZW5k"
				+ "c19saXN0IHByZXNlbmNlIiwiYXBwaWQiOiJmZ2hpNDU2N08wM0hST3hFandibjdrZ1hwQm"
				+ "huaFd3diIsImlzcyI6Imh0dHBzOi8vYXBpLmVwaWNnYW1lcy5kZXYvZXBpYy9vYXV0aC92"
				+ "MSIsImRuIjoiS3JuYnJ5IiwiZXhwIjoxNTg4Mjg2MDgzLCJpYXQiOjE1ODgyNzg4ODMsIm"
				+ "5vbmNlIjoibi1CNXBjbEl2WkpCWkFNSkw1bDZHb1JyQ08zYkU9IiwianRpIjoiNjRjMzBk"
				+ "MDI5OGEzNDM3YzhhNzRlNTkwMWMzNDg2YjUifQ.yYA-UsOABhrXwxiuXhyFkC9HL8Zqh_e"
				+ "cJeSVJF7Jdjo";
		boolean value = JWTAuthUtil.isValidJwt(token, "test-service");
		Assert.assertFalse(value);
	}

	@Test
	public void testTokenParsingValid() {
		RunningServices.getInstance().register(new GenericKieBasedService() { 
			@Override
			public Properties getConfig() {
				Properties testProperties = new Properties();
				testProperties.setProperty("jwtValidIss", "https://api.epicgames.dev/epic/oauth/v1");
				testProperties.setProperty("jwtValidAud", "xyza7891lhxMVYGCON7LgnKZZ8HQGD5H");
				return testProperties;
			}
			@Override
			public String getId() {
				return "test-service";
			}
		});
		String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJ4eXphNzg5MWxoeE1W"
				+ "WUdDT043TGduS1paOEhRR0Q1SCIsInN1YiI6Ijk2MjZmNDQxMDU1MzQ5Y2U4Y2I3ZDdkNW"
				+ "E0ODNlYWEyIiwidCI6ImVwaWNfaWQiLCJzY29wZSI6ImJhc2ljX3Byb2ZpbGUgZnJpZW5k"
				+ "c19saXN0IHByZXNlbmNlIiwiYXBwaWQiOiJmZ2hpNDU2N08wM0hST3hFandibjdrZ1hwQm"
				+ "huaFd3diIsImlzcyI6Imh0dHBzOi8vYXBpLmVwaWNnYW1lcy5kZXYvZXBpYy9vYXV0aC92"
				+ "MSIsImRuIjoiS3JuYnJ5IiwiZXhwIjoyNTUxMTc5NzM1LCJpYXQiOjE1ODgyNzg4ODMsIm"
				+ "5vbmNlIjoibi1CNXBjbEl2WkpCWkFNSkw1bDZHb1JyQ08zYkU9IiwianRpIjoiNjRjMzBk"
				+ "MDI5OGEzNDM3YzhhNzRlNTkwMWMzNDg2YjUifQ.X-LIMeBy_uhaHlTvixM6dyPR9VVdz8A"
				+ "j2ulwgeOD3rY";
		boolean value = JWTAuthUtil.isValidJwt(token, "test-service");
		Assert.assertFalse(value);
	}

	@Test
	public void testTokenParsingTweakedToken() {
		String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJ4eXphNzg5MWxoeE1W"
				+ "WUdDT043TGduS1paOEhRR0Q1SCIsInN1YiI6Ijk2MjZmNDQxMDU1MzQ5Y2U4Y2I3ZDdkNW"
				+ "E0ODNlYWEyIiwidCI6ImVwaWNHELLOMYBABYHELLOMYDARLINGHELLOMYRAGTIMEGALW5k"
				+ "c19saXN0IHByZXNlbmNASDASDFDAFaWQiOiJmZ2hpNDU2N08wM0hST3hFandibjdrZ1wQm"
				+ "huaFd3diIsImlzcyI6Imh0dHBzOi8vYXBpLmVwaWNnYW1lcy5kZXYvZXBpYy9vYXV0aC92"
				+ "MSIsImRuIjoiS3JuYnJ5IiwiZXhwIjoxNTg4Mjg2MDgzLCJpYXQiOjE1ODgyNzg4ODMsIm"
				+ "5vbmNlIjoibi1CNXBjbEl2WkpCWkFNSkw1bDZHb1JyQ08zYkU9IiwianRpIjoiNjRjMzBk"
				+ "MDI5OGEzNDM3YzhhNzRlNTkwMWMzNDg2YjUifQ.yYA-UsOABhrXwxiuXhyFkC9HL8Zqh_e"
				+ "cJeSVJF7Jdjo";
		boolean value = JWTAuthUtil.isValidJwt(token, null);
		Assert.assertFalse(value);
	}


	@Test
	public void testTokenParsingNotJWTToken() {
		String token = "l6XI48ujknQQlsJgpGXg4l2i_DuUxuG2GXTzkOG7UtX4MqkVBCfW1t1JIIc8q0kCI"
				+ "nC2oBwhC599ZCmd-cOi0kS7Aquv68fjERIRK9oCUnF_lJg296jV8xcalFY0FOWX--qX3xG"
				+ "KL33VjJBMIrIu7ETjj06s-v4li22CnHmu2lDkrp_FPTVzFscn-XRIojqIFb7pKRFPt27m1"
				+ "2FNE_Rd9bqlVCkvMNuE7VTpTOrSfKk5B01M5IuXKXk0pTAWnelqaD9bHjAExe2I_183lp_"
				+ "uFhNN4hLTjOojxl-dK8Jy2OCPEAsg5rs9Lwttp3zZ--y0sM7UttN2dE0w3F2f352MNQ";
		boolean value = JWTAuthUtil.isValidJwt(token, null);
		Assert.assertFalse(value);
	}
}
