package de.janbusch.jhashpassword.net.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import de.janbusch.jhashpassword.net.common.EActionCommand;
import de.janbusch.jhashpassword.net.common.ENetCommand;
import de.janbusch.jhashpassword.net.common.IJHPMsgHandler;
import de.janbusch.jhashpassword.net.server.JHPServer;
import de.janbusch.jhashpassword.net.server.ProcessConnection;

public class JHPClient extends Thread {
	private static final int SOLICITATION_DELAY = 2;
	private static final int MAX_SOL_COUNT = 5;

	public static final int CLIENT_PORT_UDP = 4833;
	public static final int CLIENT_PORT_TCP = 4835;
	private static final int CLIENT_TIMEOUT = 1000;

	public enum ClientState {
		LISTEN_SOLICITATION, IDLE, SENDING_SOLICITATION, SHUTTING_DOWN, LISTEN_CONNECTION_UDP, SERVER_LISTEN_CONNECTION_TCP, CLIENT_LISTEN_CONNECTION_TCP, LISTEN_MSG_TCP
	};

	private ClientState myState;
	private ExecutorService executor;
	private ScheduledExecutorService scheduledExecutor;
	private DatagramSocket inputSocketUDP;
	private boolean isServer;
	private IJHPMsgHandler msgHandler;
	private InetAddress broadcast;
	private String macAddress;
	private String operatingSystem;
	private int solCount;
	private SSLServerSocket serverSocketTCP;
	private SSLSocket clientSocketTCP;
	private SSLSocket connectedClient;
	private InputStream inputStream;
	private OutputStream outputStream;

	public JHPClient(IJHPMsgHandler msgHandler, InetAddress inetAddress,
			String macAddress, String operatingSystem) throws IOException {
		this.msgHandler = msgHandler;
		this.executor = Executors.newSingleThreadExecutor();
		this.broadcast = inetAddress;
		this.setName("JHashPassword Client");
		this.operatingSystem = operatingSystem;
		this.macAddress = macAddress;
		this.myState = ClientState.IDLE;

		try {
			SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory
					.getDefault();
			serverSocketTCP = (SSLServerSocket) factory
					.createServerSocket(CLIENT_PORT_TCP);
		} catch (Exception e) {
			e.printStackTrace();
		}

		inputSocketUDP = new DatagramSocket(CLIENT_PORT_UDP);
		inputSocketUDP.setSoTimeout(CLIENT_TIMEOUT);
		startSolicitation();

		System.out.println(this.getName() + ": ready!");
	}

	public void run() {
		System.out.println(this.getName() + ": starts listening on port: "
				+ inputSocketUDP.getLocalPort());

		DatagramPacket packet;

		while (!this.isInterrupted()) {
			switch (myState) {
			case LISTEN_CONNECTION_UDP:
			case LISTEN_SOLICITATION:
			case SENDING_SOLICITATION:
				packet = new DatagramPacket(new byte[ENetCommand.PACKET_SIZE], ENetCommand.PACKET_SIZE);
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
			case SERVER_LISTEN_CONNECTION_TCP:
				try {
					connectedClient = (SSLSocket) serverSocketTCP.accept();
					ProcessConnection pC = new ProcessConnection(
							connectedClient);
				} catch (IOException e) {
					e.printStackTrace();
					this.interrupt();
				}
				break;
			case SHUTTING_DOWN:
				this.interrupt();
				break;
			case IDLE:
			default:
				try {
					JHPClient.sleep(100);
				} catch (InterruptedException e1) {
					// Interrupted
				}
				break;
			}
		}

		this.inputSocketUDP.close();
		System.out.println(this.getName() + ": stopped!");
	}
	
	private void handleMessage(String msg, InetSocketAddress receivedFrom) {
		ENetCommand command = ENetCommand.parse(msg);

		switch (command) {
		case REQ_OS:
			ENetCommand req = ENetCommand.SOLICITATION;
			req.setParameter(macAddress + "|" + operatingSystem);

			sendMessage(new InetSocketAddress(broadcast, JHPServer.SERVER_PORT_UDP),
					req.toString());
			break;
		default:
			msgHandler.handleMessage(command, receivedFrom);
		}

	}

	public void sendMessage(final InetSocketAddress recipient, final String msg) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg
						.getBytes().length);
				packet.setSocketAddress(recipient);
				try {
					inputSocketUDP.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void connectToServer(String serverHost, int port) {
		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory
					.getDefault();
			clientSocketTCP = (SSLSocket) factory
					.createSocket(serverHost, port);
		} catch (IOException e) {
			e.printStackTrace();
			this.interrupt();
		}
	}

	public void killServer() {
		myState = ClientState.SHUTTING_DOWN;

		try {
			executor.shutdown();
			executor.awaitTermination(3, TimeUnit.SECONDS);

			if (scheduledExecutor != null) {
				scheduledExecutor.shutdown();
				scheduledExecutor.awaitTermination(3, TimeUnit.SECONDS);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void startSolicitation() {
		this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		myState = ClientState.SENDING_SOLICITATION;
		solCount = 0;
		scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				if (solCount < MAX_SOL_COUNT) {
					ENetCommand req = ENetCommand.SOLICITATION;
					req.setParameter(JHPClient.this.macAddress + "|"
							+ JHPClient.this.operatingSystem);

					sendMessage(new InetSocketAddress(broadcast,
							JHPServer.SERVER_PORT_UDP), req.toString());

					int sec = (MAX_SOL_COUNT - solCount) * SOLICITATION_DELAY;
					EActionCommand action = EActionCommand.SOLICITATION_LEFT;
					action.setParameter(sec);
					msgHandler.handleAction(action);

					System.out.println("Sending solicitation #" + solCount
							+ "/" + MAX_SOL_COUNT + "...");
				} else {
					stopSolicitation();
					msgHandler.handleAction(EActionCommand.SOLICITATION_END);
					myState = ClientState.LISTEN_CONNECTION_UDP;
				}
				solCount++;
			}
		}, 0, SOLICITATION_DELAY, TimeUnit.SECONDS);
	}

	public void stopSolicitation() {
		this.scheduledExecutor.shutdown();
	}

	public void setState(ClientState state) {
		this.myState = state;
	}

}
