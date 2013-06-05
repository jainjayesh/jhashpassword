package de.janbusch.jhashpassword.net.common;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SSLSocketFactoryUtil {
	public static SSLSocketFactory GetSocketFactory(InputStream key)
			throws KeyManagementException, NoSuchAlgorithmException,
			KeyStoreException, UnrecoverableKeyException, CertificateException,
			IOException {
		final KeyStore keyStore = KeyStore.getInstance("BKS");
		keyStore.load(key, null);

		final KeyManagerFactory keyManager = KeyManagerFactory
				.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManager.init(keyStore, null);
		// keyManager.init(null, null);

		final TrustManagerFactory trustFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustFactory.init(keyStore);

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keyManager.getKeyManagers(),
				trustFactory.getTrustManagers(), new SecureRandom());

		final javax.net.ssl.SSLSocketFactory delegate = sslContext
				.getSocketFactory();
		SSLSocketFactory factory = new SSLSocketFactory() {
			@Override
			public Socket createSocket(String host, int port)
					throws IOException, UnknownHostException {
				InetAddress addr = InetAddress.getByName(host);
				injectHostname(addr, host);
				return delegate.createSocket(addr, port);
			}

			@Override
			public Socket createSocket(InetAddress host, int port)
					throws IOException {
				return delegate.createSocket(host, port);
			}

			@Override
			public Socket createSocket(String host, int port,
					InetAddress localHost, int localPort) throws IOException,
					UnknownHostException {
				return delegate.createSocket(host, port, localHost, localPort);
			}

			@Override
			public Socket createSocket(InetAddress address, int port,
					InetAddress localAddress, int localPort) throws IOException {
				return delegate.createSocket(address, port, localAddress,
						localPort);
			}

			private void injectHostname(InetAddress address, String host) {
				try {
					Field field = InetAddress.class
							.getDeclaredField("hostName");
					field.setAccessible(true);
					field.set(address, host);
				} catch (Exception ignored) {
				}
			}

			@Override
			public Socket createSocket(Socket s, String host, int port,
					boolean autoClose) throws IOException {
				injectHostname(s.getInetAddress(), host);
				return delegate.createSocket(s, host, port, autoClose);
			}

			@Override
			public String[] getDefaultCipherSuites() {
				return delegate.getDefaultCipherSuites();
			}

			@Override
			public String[] getSupportedCipherSuites() {
				return delegate.getSupportedCipherSuites();
			}
		};

		return factory;
	}
}
