package io.elimu.serviceapi.config;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import io.elimu.genericapi.service.GenericKieBasedService;
import io.elimu.genericapi.service.RunningServices;

public class JWTAuthUtilTest {

	@Test
	public void testTokenRS384() {
		RunningServices.getInstance().register(new GenericKieBasedService() { 
			@Override
			public Properties getConfig() {
				Properties testProperties = new Properties();
				testProperties.setProperty("jwtValidAud", "http://elimu.io/cds-services/cdc-gc-cds");
				testProperties.setProperty("jwtValidIss", "nextgen");
				testProperties.setProperty("jwtValidPresetKeys", "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUR0RENDQXB5Z0F3SUJBZ0lnT2ZvVzdwSjZHNjl"
						+ "Ma2xseWZDK0pUdHpOdnFhS1g1dFhOQllFTEVSdE5UMHcKRFFZSktvWklodmNOQVFFRkJRQXdkVEVKTUFjR0ExVUVCaE1BTVE0d0RBWURWUVFLREFWbGJHb"
						+ "HRkVEVKTUFjRwpBMVVFQ3d3QU1SMHdHd1lEVlFRRERCUmxiR2x0ZFdsdVptOXliV0YwYVdOekxtTnZiVEVQTUEwR0NTcUdTSWIzCkRRRUpBUllBTVIwd0d"
						+ "3WURWUVFEREJSbGJHbHRkV2x1Wm05eWJXRjBhV056TG1OdmJUQWVGdzB5TXpBNU1EUXgKTXpNeU1ESmFGdzB6TXpBNU1EUXhNek15TURKYU1GWXhDVEFIQ"
						+ "mdOVkJBWVRBREVPTUF3R0ExVUVDZ3dGWld4cApiWFV4Q1RBSEJnTlZCQXNNQURFZE1Cc0dBMVVFQXd3VVpXeHBiWFZwYm1admNtMWhkR2xqY3k1amIyMHh"
						+ "EekFOCkJna3Foa2lHOXcwQkNRRVdBRENDQVNJd0RRWUpLb1pJaHZjTkFRRUJCUUFEZ2dFUEFEQ0NBUW9DZ2dFQkFLTUkKTmJLSmN4cUNNK3RWek0vRlNBa"
						+ "W5NS204RlNoSUNMUkcyYWs2RWtvNHBYZkFMQ1lJUi9XU3lnZGVwMUtqblE0MApsQkFEV3hDWWRCdWhuVzFQUkZFZjlNTENWVnpvVzFBalNnTytxWkVLbzl"
						+ "BWTZLeHVody92VzBXZitLMmNJcVRzClIyaE5OTUFuUHIvNXVXbUpoVEJDNXVHVVk4OWZESzdxVmMzSDlUVHF3L01OUDJGUEJ2K2RPVXJvRmkyWDFYUWkKU"
						+ "k52UTlFMmRnd052dHQrZ0k2amc5dnZzdVF0Q1BlVFA3NUlVVGV1aVJ1WUlPR0NraFV5dXQ4by9MaTA1aWtBNApYM291OGw2Q0tXR0JGcVpKeGhPZ0RRYWV"
						+ "ZaXpFMVBNakJMczczTnFZa2o0ZjdCQWpkZnhwZVZTYjlZSUNWbWpUCnpwN2V4Q1ZhSUIzZFFudExwbFVDQXdFQUFhTlBNRTB3SFFZRFZSME9CQllFRkl0c"
						+ "VErWEpXODY4c3lNMmRwKy8KSVRZUm5HWjZNQjhHQTFVZEl3UVlNQmFBRkl0cVErWEpXODY4c3lNMmRwKy9JVFlSbkdaNk1Bc0dBMVVkRVFRRQpNQUtDQUR"
						+ "BTkJna3Foa2lHOXcwQkFRVUZBQU9DQVFFQW1FOEl2S1RuQit4U2MxZ3UwWkNOQ09RUTU1NXpEbXhiCjN6ZGlhSjVRNEFQWDVFdGV4QzNhdDczbUF5bjNqa"
						+ "0JiUVptUisxR0Z6dEFKSTZFYkU4UWl4NldaMGN0K0JROW8KRGs0cDB0eldTcnNweTVlNEVqRUw3V0JnaWxNdGdsRTVNYUw2ZHRuTGxQOTh5VmVrTkhYOGt"
						+ "hd0JBaThHOFpuQwozbWRtQmVWRzJSVkFSaklGb05hb0ZQUHYzSm5aWkZBT0J4djBabWRiWGpMRWhHZkVkUmVOelB1UlRka0xkYXJUCjgyQTM4OU5CUHpJb"
						+ "kI5ZlNPcXpaQzNqZlFCaVl5WjNTREI0MzVpNzk4SCtDZDFuZzRkQlRXNEd0S2ZsRytra3EKcUMxWHpoanVaT2lhREhIYjZMdGN0Wk5SNnBVNnRlZmlkbFR"
						+ "SRHA5VVRWWDlMd2NhN0ZDNGN3PT0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQ==");
				testProperties.setProperty("jwtValidJku", "https://auth.logicahealth.org/jwk,https://sandbox.cds-hooks.org/.well-known/jwks.json");
				return testProperties;
			}
			@Override
			public String getId() {
				return "test-service";
			}
		});
		String token = "eyJhbGciOiJSUzM4NCIsInR5cCI6IkpXVCIsImtpZCI6ImI5ODUyZTViLWJmZmItND"
				+ "U5Ni1iNzZjLWRmMmUzYmEwMjYyOCJ9.eyJpc3MiOiJuZXh0Z2VuIiwiYXVkIjoiaHR0cDov"
				+ "L2VsaW11LmlvL2Nkcy1zZXJ2aWNlcy9jZGMtZ2MtY2RzIiwiZXhwIjoxNzI1MTM5MTE0LCJ"
				+ "pYXQiOjE2OTM1ODY2MTksImp0aSI6IjUyNGE3ZTM1LWMzZjktNDlkNy1hZTA0LWRhMGU5YW"
				+ "U1ZTIyNSJ9.VpNgAKydmqP-eG9zk0r3_HShPHiEW6W-WOHybRYOVVbCch9Pmi5hThiaezKK"
				+ "XEGfosZ_od552KFVZIz-PXPldm7r3ZOhAxeReqbCTiMVF46POger_ibVKfivcTbbTj7l3ND"
				+ "UBYG3pDfZvXELmra6thhgj_PVdiGUxBuW6geqF653Pz9wulKE5zsPy1EDp_4_V0nU89XEHu"
				+ "hV5LFPu-rpQDWKQpB-xIsiauiwvrqGwi8GsIXL9beJBWeARbJMllAWsyi0ojz61W-NENWrx"
				+ "KfdeQRlWTDQO0ndEYSngkT-v61X2uzWCo_ZSBu6YC0bPJUEhODTgk8zlkOnfMzLRypGpxwv"
				+ "0bZYvANnAJuaxyEM6oQzDQkSVk7rN0_oOEoHGkXOD-be0q2Vv0PbVvdzPvzdKXfVlknYDWx"
				+ "nBbw8lvS6QMxSFXhdoM6fgdvAGPkA5LxsWOzroS_glLqqDlkQPHbCi8uyBhkqwfrApoQ2w9"
				+ "Q_1Hwp-Fquluf3omeLVIKIGR3PQj7hDERKmcAtSS6BWguiinS2FBrG6mYUHzSwT8akt0-BS"
				+ "R1DtsuTmtLhT5t5jyY1dqC6CCkpbATa543d9d2LyNnbXVFweIHV6ZijDKczHyNo8OVu1b1L"
				+ "p-X7rf-i1dxqCWufyXmMK4-FiiN3H68SkfKFtZaT3RTXJnPyuEcT8F8";
		boolean value = JWTAuthUtil.isValidJwt(token, "test-service");
		Assert.assertFalse(value);
	}
	
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
