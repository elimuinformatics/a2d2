package io.elimu.serviceapi.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import com.auth0.jwt.interfaces.RSAKeyProvider;

public class SimpleRSAKeyProvider implements RSAKeyProvider {

	private RSAPublicKey key;

	public SimpleRSAKeyProvider(RSAPublicKey key) {
		this.key = key;
	}
	
	@Override
	public RSAPublicKey getPublicKeyById(String keyId) {
		return key;
	}

	@Override
	public RSAPrivateKey getPrivateKey() {
		return null;
	}

	@Override
	public String getPrivateKeyId() {
		return null;
	}

}
