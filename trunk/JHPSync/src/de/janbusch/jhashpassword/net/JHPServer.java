package de.janbusch.jhashpassword.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JHPServer extends Thread {
	public enum ServerState {
		LISTEN_SOLICITATION, IDLE, SENDING_SOLICITATION, SHUTTING_DOWN, LISTEN_CONNECTION
	};

	private ServerState myState;
	private ExecutorService executor;
	private ScheduledExecutorService scheduledExecutor;
	private DatagramSocket serverSocket;
	private boolean isServer;
	private IJHPMsgHandler msgHandler;
	public static final int SERVER_PORT = 4832;
	public static final int CLIENT_PORT = 4833;
	private static final int SERVER_TIMEOUT = 1000;
	private static final int CLIENT_TIMEOUT = 1000;
	private static final int MAX_SOL_COUNT = 3;
	private static int PACKET_SIZE = 512;
	private InetAddress broadcast;
	private String macAddress;
	private String operatingSystem;
	private int solCount;

	public JHPServer(boolean isServer, IJHPMsgHandler msgHandler,
			InetAddress inetAddress, String macAddress, String operatingSystem)
			throws IOException {
		this.isServer = isServer;
		this.msgHandler = msgHandler;
		this.executor = Executors.newSingleThreadExecutor();
		this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		this.broadcast = inetAddress;
		this.setName("JHashPassword Server");
		this.operatingSystem = operatingSystem;
		this.macAddress = macAddress;
		this.myState = ServerState.IDLE;

		if (this.isServer) {
			serverSocket = new DatagramSocket(SERVER_PORT);
			serverSocket.setSoTimeout(SERVER_TIMEOUT);
			myState = ServerState.LISTEN_SOLICITATION;
		} else {
			serverSocket = new DatagramSocket(CLIENT_PORT);
			serverSocket.setSoTimeout(CLIENT_TIMEOUT);
			startSolicitation();
		}

		System.out.println(this.getName() + ": ready! Running as server: "
				+ isServer);
	}

	private void startSolicitation() {
		myState = ServerState.SENDING_SOLICITATION;
		solCount = 0;
		scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				if (solCount++ < MAX_SOL_COUNT) {
					ENetCommand req = ENetCommand.SOLICITATION;
					req.setParameter(JHPServer.this.macAddress + "|"
							+ JHPServer.this.operatingSystem);

					sendMessage(new InetSocketAddress(broadcast,
							JHPServer.SERVER_PORT), req.toString());

					System.out.println("Sending solicitation...");
				} else {
					stopSolicitation();
					myState = ServerState.LISTEN_CONNECTION;
				}
			}
		}, 0, 3, TimeUnit.SECONDS);
	}

	public void run() {
		System.out.println(this.getName() + ": starts listening on port: "
				+ serverSocket.getLocalPort());

		DatagramPacket packet;

		while (!this.isInterrupted()) {
			switch (myState) {
			case LISTEN_CONNECTION:
			case LISTEN_SOLICITATION:
			case SENDING_SOLICITATION:
				packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
				try {
					serverSocket.receive(packet);
					InetSocketAddress receivedFrom = (InetSocketAddress) packet
							.getSocketAddress();
					String msg = new String(packet.getData(), 0,
							packet.getLength()).trim();
					if (msg != null && msg.length() > 0) {
						msgHandler.handleMessage(msg, receivedFrom);
					}
				} catch (SocketTimeoutException e) {
					if(this.isInterrupted()) break;
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case SHUTTING_DOWN:
				this.interrupt();
				break;
			case IDLE:
			default:
				try {
					JHPServer.sleep(100);
				} catch (InterruptedException e1) {
					// Interrupted
				}
				break;
			}
		}

		this.serverSocket.close();
		System.out.println(this.getName() + ": stopped!");
	}

	public void sendMessage(final InetSocketAddress recipient, final String msg) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg
						.getBytes().length);
				packet.setSocketAddress(recipient);
				try {
					serverSocket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void killServer() {
		myState = ServerState.SHUTTING_DOWN;
		executor.shutdown();
		scheduledExecutor.shutdown();

		try {
			executor.awaitTermination(3, TimeUnit.SECONDS);
			scheduledExecutor.awaitTermination(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void stopSolicitation() {
		this.scheduledExecutor.shutdown();
	}

	public void setState(ServerState state) {
		this.myState = state;
	}

}
