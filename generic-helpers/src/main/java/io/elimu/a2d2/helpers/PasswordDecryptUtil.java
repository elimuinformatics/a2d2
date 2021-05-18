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
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this helper to encrypt or decrypt stored variables in your service DSL or passed as JVM parameters.
 * It uses the underlying encryption mechanisms to decrypt or encrypt a String using the {@link #decrypt(String)}
 * and {@link #encrypt(String)} methods respectively<br>
 *<br>
 * Here's two examples of how to use these methods:<br>
 * <code>
 * String encryptedValue = io.elimu.a2d2.helpers.PasswordDecryptUtil.encrypt("value-to-encrypt");<br>
 * String unencryptedValue = io.elimu.a2d2.helpers.PasswordDecryptUtil.decrypt(encryptedValue);<br>
 * </code>
 */
public class PasswordDecryptUtil {

	private static final Logger LOG = LoggerFactory.getLogger(PasswordDecryptUtil.class);

	private PasswordDecryptUtil() {
		throw new IllegalStateException("Utility class");
	}
	
	protected static StringEncryptor stringEncryptor() {
		String salt = System.getenv("jasypt_encryptor_password");
		if (salt == null || "".equals(salt.trim())) {
			LOG.warn("Couldn't find environment variable jasypt_encryptor_password");
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

	/**
	 * Decrypts a string
	 * @param str the encrypted string
	 * @return the unencrypted string
	 * @throws EncryptionOperationNotPossibleException if the passed value cannot be decrypted with the configured salt. This means str was encrypted using a different salt, and maybe this value is specific of another environment
	 * @throws IllegalStateException if the jasypt_encryptor_password Environment variable (the salt) is not present or empty. This means the environment is not properly configured
	 */
	public static String decrypt(String str) {
		return PasswordDecryptUtil.stringEncryptor().decrypt(str);
	}

	/**
	 * Encrypts a string
	 * @param str the unencrypted string
	 * @return the encrypted string
	 * @throws IllegalStateException if the jasypt_encryptor_password Environment variable (the salt) is not present or empty. This means the environment is not properly configured
	 */
	public static String encrypt(String str) {
		return PasswordDecryptUtil.stringEncryptor().encrypt(str);
	}
}
