package de.janbusch.jhashpassword.net.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import de.janbusch.jhashpassword.net.common.ENetCommand;
import de.janbusch.jhashpassword.net.common.IJHPMsgHandler;
import de.janbusch.jhashpassword.net.common.SecureMessage;
import de.janbusch.jhashpassword.net.server.JHPServer;

public class JHPClient extends Thread {
	private static final int SOLICITATION_DELAY = 2;

	public static final int CLIENT_PORT_UDP = 4832;
	private static final int CLIENT_TIMEOUT = 1000;

	public enum ClientState {
		IDLE, SENDING_SOLICITATION, SHUTTING_DOWN, CONNECTED_SECURE
	};

	private ClientState myState;
	private ScheduledExecutorService scheduledExecutor;
	private DatagramSocket inputSocketUDP;
	private IJHPMsgHandler msgHandler;
	private InetAddress broadcast;
	private String macAddress;
	private String operatingSystem;
	private SSLSocket clientSocketTCP;
	private ObjectInputStream secureInputStream;
	private ObjectOutputStream secureOutputStream;

	public JHPClient(IJHPMsgHandler msgHandler, InetAddress broadcastAddress,
			String macAddress, String operatingSystem) throws IOException {
		this.msgHandler = msgHandler;
		this.broadcast = broadcastAddress;
		this.setName("JHashPassword Client");
		this.operatingSystem = operatingSystem;
		this.macAddress = macAddress;
		this.myState = ClientState.IDLE;

		// try {
		// SSLServerSocketFactory factory = (SSLServerSocketFactory)
		// SSLServerSocketFactory
		// .getDefault();
		// serverSocketTCP = (SSLServerSocket) factory
		// .createServerSocket(CLIENT_PORT_TCP);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		inputSocketUDP = new DatagramSocket(CLIENT_PORT_UDP);
		inputSocketUDP.setSoTimeout(CLIENT_TIMEOUT);

		startSolicitation();

		System.out.println(this.getName() + ": ready!");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void run() {
		System.out.println(this.getName() + ": starts listening on port: "
				+ inputSocketUDP.getLocalPort());

		DatagramPacket packet;

		while (!this.isInterrupted()) {
			switch (myState) {
			case SENDING_SOLICITATION:
				packet = new DatagramPacket(new byte[ENetCommand.PACKET_SIZE],
						ENetCommand.PACKET_SIZE);
				try {
					inputSocketUDP.receive(packet);
					InetSocketAddress receivedFrom = (InetSocketAddress) packet
							.getSocketAddress();
					String msg = new String(packet.getData(), 0,
							packet.getLength()).trim();
					if (msg != null && msg.length() > 0) {
						this.handleMessage(msg, receivedFrom);
					}
				} catch (SocketTimeoutException e) {
					if (this.isInterrupted())
						break;
				} catch (IOException e) {
					e.printStackTrace();
					this.interrupt();
				}
				break;
			case SHUTTING_DOWN:
				this.interrupt();
				scheduledExecutor.shutdown();
				break;
			case CONNECTED_SECURE:
				Object inObject;
				try {
					inObject = secureInputStream.readObject();

					if (inObject instanceof SecureMessage) {
						final SecureMessage recMsg = (SecureMessage) inObject;
						msgHandler.handleMessage(recMsg,
								clientSocketTCP.getInetAddress());
					} else {
						System.out
								.println("Unkown secure message received. Ignoring!");
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					this.interrupt();
				} catch (IOException e) {
					e.printStackTrace();
					this.interrupt();
				}
				break;
			case IDLE:
			default:
				try {
					JHPClient.sleep(50);
				} catch (InterruptedException e1) {
					// Interrupted
				}
				break;
			}
		}

		this.inputSocketUDP.close();
		System.out.println(this.getName() + ": stopped!");
	}

	private void handleMessage(final String msg,
			final InetSocketAddress receivedFrom) {
		final ENetCommand command = ENetCommand.parse(msg);

		switch (command) {
		case REQ_OS:
			ENetCommand req = ENetCommand.REP_OS;
			req.setParameter(macAddress + "|" + operatingSystem);

			sendMessage(new InetSocketAddress(broadcast,
					JHPServer.SERVER_PORT_UDP), req.toString());
			break;
		default:
			msgHandler.handleMessage(command, receivedFrom);
		}
	}

	public void sendMessage(final InetSocketAddress recipient, final String msg) {
		DatagramPacket packet = new DatagramPacket(msg.getBytes(),
				msg.getBytes().length);
		packet.setSocketAddress(recipient);
		try {
			inputSocketUDP.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendSecureMessage(final InetSocketAddress recipient,
			final SecureMessage message) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					secureOutputStream.writeObject(message);
					secureOutputStream.flush();
					secureOutputStream.reset();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void connectToServer(String serverHost, int port) {
		try {
			SocketFactory factory = SSLSocketFactory.getDefault();

			clientSocketTCP = (SSLSocket) factory
					.createSocket(serverHost, port);

			clientSocketTCP.setSoTimeout(CLIENT_TIMEOUT);
			clientSocketTCP.setUseClientMode(true);

			myState = ClientState.CONNECTED_SECURE;
			secureOutputStream = new ObjectOutputStream(
					new BufferedOutputStream(clientSocketTCP.getOutputStream()));
			secureInputStream = new ObjectInputStream(new BufferedInputStream(
					clientSocketTCP.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
			this.interrupt();
		}
	}

	public void killServer() {
		myState = ClientState.SHUTTING_DOWN;
	}

	public void startSolicitation() {
		this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		myState = ClientState.SENDING_SOLICITATION;

		scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				if (!Thread.interrupted()) {
					ENetCommand req = ENetCommand.SOLICITATION;

					sendMessage(new InetSocketAddress(broadcast,
							JHPServer.SERVER_PORT_UDP), req.toString());
				}
			}
		}, 0, SOLICITATION_DELAY, TimeUnit.SECONDS);
	}

	public void stopSolicitation() {
		this.scheduledExecutor.shutdownNow();
		myState = ClientState.IDLE;
	}

	public void setState(ClientState state) {
		this.myState = state;
	}

}
