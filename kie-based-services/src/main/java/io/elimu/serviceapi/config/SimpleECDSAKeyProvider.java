package io.elimu.serviceapi.config;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import com.auth0.jwt.interfaces.ECDSAKeyProvider;

public class SimpleECDSAKeyProvider implements ECDSAKeyProvider {

	private ECPublicKey key;

	public SimpleECDSAKeyProvider(ECPublicKey key) {
		this.key = key;
	}
	
	@Override
	public ECPublicKey getPublicKeyById(String keyId) {
		return key;
	}

	@Override
	public ECPrivateKey getPrivateKey() {
		return null;
	}

	@Override
	public String getPrivateKeyId() {
		return null;
	}

}
