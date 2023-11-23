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
				testProperties.setProperty("jwtValidPresetKeys", 
						  "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUR1RENDQXFDZ0F3SUJBZ0loQU54Q3FLRkJu"
						+ "SHJXdnkyY0MwZzRDZklhS0Q1VVJlSHM0bzZtdXBQUHZxazcKTUEwR0NTcUdTSWIzRFFFQkJRVUFN"
						+ "SE14Q1RBSEJnTlZCQVlUQURFYU1CZ0dBMVVFQ2d3UlJXeHBiWFVnU1c1bQpiM0p0WVhScFkzTXhD"
						+ "VEFIQmdOVkJBc01BREVXTUJRR0ExVUVBd3dOWkdWdGJ5NWxiR2x0ZFM1cGJ6RVBNQTBHCkNTcUdT"
						+ "SWIzRFFFSkFSWUFNUll3RkFZRFZRUUREQTFrWlcxdkxtVnNhVzExTG1sdk1CNFhEVEl6TURrd05q"
						+ "RXkKTkRjME1Wb1hEVE16TURrd05qRXlORGMwTVZvd1d6RUpNQWNHQTFVRUJoTUFNUm93R0FZRFZR"
						+ "UUtEQkZGYkdsdApkU0JKYm1admNtMWhkR2xqY3pFSk1BY0dBMVVFQ3d3QU1SWXdGQVlEVlFRRERB"
						+ "MWtaVzF2TG1Wc2FXMTFMbWx2Ck1ROHdEUVlKS29aSWh2Y05BUWtCRmdBd2dnRWlNQTBHQ1NxR1NJ"
						+ "YjNEUUVCQVFVQUE0SUJEd0F3Z2dFS0FvSUIKQVFDbW55RngrUk5CT2M5eU5RdkRid2RPK0N2eExu"
						+ "NkZhUjJTc3dvNDdJTUFvTkdwYm5yNnVQUFZvbnl6RDJzOQpPbXF4UkkyQ1FvWUxSaWJ0SUZDcndL"
						+ "OGpsa1hUSTNZdjVITy83bFRxVnRxMmdlcDY3bkJPdDdCN28xcVk2bHU0CnU5Mk02WEFDOFViV2Zs"
						+ "WUIwSzhvRTZOVkhHUTdPZEJRUnpGbU5oL09MSnRLZ3VPWER4T2VkV1d3YVZ2MUx6Q0wKUFJzUGRq"
						+ "MnRaWHNFQXFlSTJMWXh3SWwxN0FtVTQrempNZ3RnYzNleFZ4aWV2eGJONmh0eDlzb3RZcVo3aXNS"
						+ "VwppTXZQK2xIblgvbEJLRitZZmgzemFmQS94NzJMRHA5aXVvUXMvZ1lucmZlUGJJUm1udjZKWi9z"
						+ "cE5hRTVlUE1ZCmU5YkVwVzJqYi9pNjRjRVRHRnc5dmM5VkFnTUJBQUdqVHpCTk1CMEdBMVVkRGdR"
						+ "V0JCUjRxMmhsdk9XNGZGWDEKS1MzSEZ3eTJOVWQyeVRBZkJnTlZIU01FR0RBV2dCUjRxMmhsdk9X"
						+ "NGZGWDFLUzNIRnd5Mk5VZDJ5VEFMQmdOVgpIUkVFQkRBQ2dnQXdEUVlKS29aSWh2Y05BUUVGQlFB"
						+ "RGdnRUJBR2E1MzFVR28rY0Z2T3VBdWIvVllTOEE1UzhECkI4bzRDeEk5eEF5STRBaUwwaGNFdnZZ"
						+ "Y0hWU0JhZnVOUG9LUFc1blMyMEttbEdzak0xU1RMaW41TzhRdE9wTlMKWlZJRFVBZExMcStEaEhx"
						+ "UkkvejJqbjB6YU9Sczc1QmlzZ2lmbktPTkd6d3hENHBwRXgxa0tjTE81ODFPMlpiSgpzMHpjKzZM"
						+ "WngvYnkySHZYUWREREtJNy9ObVdCTitxaW4ydmkyQ2Jma1gzQm9LdVBzbUd5d2w5YnY3ZEwySkNW"
						+ "CkpGWWh5R0dWWHA3TWJDclYzbU9nbmxoZzBhVVAwc0orRnBWMEtIR2pSQnJ6bkJQV3pxRDRkWmJ1"
						+ "bzdLSFZDSW4KVWNZSTNkRG5uWHlWejh2NXNudU94cjVqUnl0RDNVQWY5eG9kMmhwN0FoeGJ5N1JJ"
						+ "NHk2L0ZTZ0lUZ3M9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K");
				testProperties.setProperty("jwtValidIss", "https://elimu.io");
				testProperties.setProperty("jwtValidAud", "https://elimu.io/cds");
				return testProperties;
			}
			@Override
			public String getId() {
				return "test-service";
			}
		});
		String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzM4NCJ9.eyJhdWQiOiJodHRwczovL2VsaW11"
				+ "LmlvL2NkcyIsImlzcyI6Imh0dHBzOi8vZWxpbXUuaW8iLCJleHAiOjkyMjMzNzIwMzY4NT"
				+ "Q3NzUsImlhdCI6MTY5NDAwOTI2OH0.BCPo3pnSMD3a0-wIxVZPZR3hrHaDRhtw2CHoCMB7"
				+ "JmdqmLfghVOOtFahjQMlzuClI4K47Mevp2K2ddcjgDWrFrXl1xaRKM3FR6eTpURxGkAym8"
				+ "Xbfp--vw3d5ecqsphKjMTi0_uQtp8GvoHmKv5eEh4iL6A4gBLSsiFEYU-K4e0_2OKoJacJ"
				+ "XE16-Za6fg0pZTp_gChZX3KVhvRC6wcX7J3x7RYFkYwvgKCf76wdZPpsf7rFKhghlklB5F"
				+ "yJvSseWYEuXQyYmFxWSFgrkH3TqfgZKRz9JjjB9u6xn296YBSIR3JbZzbnHUAFQBPdDgI4"
				+ "gK3efCX_JPmYUSxOfbROwQ";
		boolean value = JWTAuthUtil.isValidJwt(token, "test-service");
		Assert.assertTrue(value);
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
