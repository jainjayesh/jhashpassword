package de.janbusch.jhashpassword.net.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.rmi.Naming;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocket;

import de.janbusch.jhashpassword.net.client.JHPClient.ClientState;
import de.janbusch.jhashpassword.net.common.ENetCommand;
import de.janbusch.jhashpassword.net.common.IJHPMsgHandler;
import de.janbusch.jhashpassword.net.server.JHPServer;
import de.janbusch.jhashpassword.net.server.ProcessConnection;
import de.janbusch.jhashpassword.net.server.JHPRMIServer.IJHPServer;

public class JHPRMIClient {
	private static final int SOLICITATION_DELAY = 2;
	private ClientState myState;
	private DatagramSocket inputSocketUDP;
	private IJHPMsgHandler msgHandler;
	private InetAddress broadcast;
	private String macAddress;
	private String operatingSystem;
	private ScheduledExecutorService scheduledExecutor;
	private Thread receiverThread;

	public static final int CLIENT_PORT_UDP = 4833;
	private static final int CLIENT_TIMEOUT = 500;

	public enum ClientState {
		IDLE, SENDING_SOLICITATION, SHUTTING_DOWN
	};

	public JHPRMIClient(IJHPMsgHandler msgHandler,
			InetAddress broadcastAddress, String macAddress,
			String operatingSystem) throws IOException {
		this.msgHandler = msgHandler;

		inputSocketUDP = new DatagramSocket(CLIENT_PORT_UDP);
		inputSocketUDP.setSoTimeout(CLIENT_TIMEOUT);

		this.broadcast = broadcastAddress;
		this.operatingSystem = operatingSystem;
		this.macAddress = macAddress;
		this.myState = ClientState.IDLE;

		System.out.println("JHPRMIClient: ready!");
	}

	public IJHPServer GetServerConnection(String host) throws Exception {
		String url = "rmi://" + host + "/jhprmiserver";
		IJHPServer server = (IJHPServer) Naming.lookup(url);
		return server;
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

		receiverThread = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("JHPRMIClient: starts listening on port: "
						+ inputSocketUDP.getLocalPort());

				DatagramPacket packet;

				while (!Thread.interrupted()) {
					switch (myState) {
					case SENDING_SOLICITATION:
						packet = new DatagramPacket(
								new byte[ENetCommand.PACKET_SIZE],
								ENetCommand.PACKET_SIZE);
						try {
							inputSocketUDP.receive(packet);
							InetSocketAddress receivedFrom = (InetSocketAddress) packet
									.getSocketAddress();
							String msg = new String(packet.getData(), 0, packet
									.getLength()).trim();
							if (msg != null && msg.length() > 0) {
								handleMessage(msg, receivedFrom);
							}
						} catch (SocketTimeoutException e) {
							if (Thread.interrupted())
								break;
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
					case SHUTTING_DOWN:
						scheduledExecutor.shutdownNow();
						receiverThread.interrupt();
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
				inputSocketUDP.close();
				System.out.println("Solicitation: stopped!");
			}
		});

		receiverThread.start();
	}

	public void killClient() {
		myState = ClientState.SHUTTING_DOWN;
	}

	public void stopSolicitation() {
		if (myState != ClientState.SENDING_SOLICITATION)
			throw new IllegalStateException();

		this.scheduledExecutor.shutdownNow();

		if (receiverThread != null)
			receiverThread.interrupt();

		myState = ClientState.IDLE;
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
}
