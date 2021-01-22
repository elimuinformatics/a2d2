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

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordDecryptUtil {

	private static final Logger LOG = LoggerFactory.getLogger(PasswordDecryptUtil.class);

	private PasswordDecryptUtil() {
		throw new IllegalStateException("Utility class");
	}
	
	protected static StringEncryptor stringEncryptor() {
		String salt = System.getenv("jasypt_encryptor_password");
		if (salt == null || "".equals(salt.trim())) {
			LOG.debug("Couldn't find environment variable jasypt_encryptor_password");
			throw new IllegalStateException("Couldn't find environment variable jasypt_encryptor_password.");
		}
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		SimpleStringPBEConfig config = new SimpleStringPBEConfig();
		config.setPassword(salt);
		config.setAlgorithm(System.getProperty("jasypt.encryptor.algorithm", "PBEWithMD5AndDES"));
		config.setIvGeneratorClassName(System.getProperty("jasypt.encryptor.iv-generator-classname", "org.jasypt.iv.NoIvGenerator"));
		config.setKeyObtentionIterations("1000");
		config.setPoolSize("1");
		config.setProviderName("SunJCE");
		config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
		config.setStringOutputType("base64");
		encryptor.setConfig(config);
		return encryptor;
	}

	public static String decrypt(String str) {
		return PasswordDecryptUtil.stringEncryptor().decrypt(str);
	}

	public static String encrypt(String str) {
		return PasswordDecryptUtil.stringEncryptor().encrypt(str);
	}
}
