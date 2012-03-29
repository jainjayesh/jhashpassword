package de.janbusch.jhashpassword.net.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import de.janbusch.jhashpassword.net.common.EActionCommand;
import de.janbusch.jhashpassword.net.common.ENetCommand;
import de.janbusch.jhashpassword.net.common.IJHPMsgHandler;

public class JHPServer extends Thread {
	private static final int PARALLELISM_FACTOR = 2;
	public static final int SERVER_PORT_UDP = 4832;
	public static final int SERVER_PORT_TCP = 4834;
	private static final int SERVER_TIMEOUT = 500;
	private static final int ADVERTISEMENT_TIMEOUT = 100;

	public enum ServerState {
		LISTEN_SOLICITATION, IDLE, SHUTTING_DOWN, LISTEN_CONNECTION_UDP, SERVER_LISTEN_CONNECTION_TCP, LISTEN_MSG_TCP
	};

	private ServerState myState;
	private Queue<Runnable> taskQueue;
	private ExecutorService executor;
	private ExecutorService taskQueueExecutor;
	private ScheduledExecutorService scheduledExecutor;
	private DatagramSocket inputSocketUDP;
	private IJHPMsgHandler msgHandler;
	private InetAddress broadcast;
	private String macAddress;
	private String operatingSystem;
	private SSLServerSocket serverSocketTCP;
	private SSLSocket clientSocketTCP;
	private SSLSocket connectedClient;
	private InputStream inputStream;
	private OutputStream outputStream;
	private int advertisementTime;

	public JHPServer(IJHPMsgHandler msgHandler, InetAddress inetAddress,
			String macAddress, String operatingSystem) throws IOException {
		this.msgHandler = msgHandler;
		this.executor = Executors.newSingleThreadExecutor();
		this.taskQueueExecutor = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors() * PARALLELISM_FACTOR);
		this.taskQueue = new LinkedList<Runnable>();
		this.broadcast = inetAddress;
		this.setName("JHashPassword Server");
		this.operatingSystem = operatingSystem;
		this.macAddress = macAddress;
		this.myState = ServerState.IDLE;

		try {
			SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory
					.getDefault();
			serverSocketTCP = (SSLServerSocket) factory
					.createServerSocket(SERVER_PORT_TCP);
		} catch (Exception e) {
			e.printStackTrace();
		}

		inputSocketUDP = new DatagramSocket(SERVER_PORT_UDP);
		inputSocketUDP.setSoTimeout(SERVER_TIMEOUT);

		executor.execute(new Runnable() {
			@Override
			public void run() {
				while (!Thread.interrupted()) {
					if (!taskQueue.isEmpty()) {
						taskQueueExecutor.execute(taskQueue.remove());
					} else {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		
		startAdvertising();

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
					JHPServer.sleep(100);
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
		taskQueue.add(new Runnable() {
			@Override
			public void run() {
				final ENetCommand command = ENetCommand.parse(msg);
				switch (command) {
				case REQ_OS:
					ENetCommand req = ENetCommand.SOLICITATION;
					req.setParameter(macAddress + "|" + operatingSystem);

					sendMessage(new InetSocketAddress(broadcast,
							SERVER_PORT_UDP), req.toString());
					break;
				case SOLICITATION:
					if (myState.equals(ServerState.LISTEN_SOLICITATION)) {
						ENetCommand advertisement = ENetCommand.ADVERTISEMENT;
						advertisement.setParameter(macAddress + "|"
								+ operatingSystem);
						sendMessage(receivedFrom, advertisement.toString());
					}
					break;
				default:
					taskQueue.add(new Runnable() {
						@Override
						public void run() {
							msgHandler.handleMessage(command, receivedFrom);
						}
					});
				}
			}
		});
	}

	public void sendMessage(final InetSocketAddress recipient, final String msg) {
		taskQueue.add(new Runnable() {
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

	public void killServer() {
		myState = ServerState.SHUTTING_DOWN;

		try {
			executor.shutdown();
			executor.awaitTermination(3, TimeUnit.SECONDS);
			taskQueueExecutor.shutdown();
			taskQueueExecutor.awaitTermination(3, TimeUnit.SECONDS);
			scheduledExecutor.shutdown();
			scheduledExecutor.awaitTermination(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void setState(ServerState state) {
		this.myState = state;
	}

	public void startAdvertising() {
		this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		myState = ServerState.LISTEN_SOLICITATION;
		advertisementTime = 0;

		scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				if (advertisementTime < ADVERTISEMENT_TIMEOUT) {
					advertisementTime++;

					int sec = (ADVERTISEMENT_TIMEOUT - advertisementTime);
					EActionCommand cmd = EActionCommand.ADVERTISEMENT_LEFT;
					cmd.setParameter(sec);
					msgHandler.handleAction(cmd);
				} else {
					stopAdvertising();
					EActionCommand cmd = EActionCommand.ADVERTISEMENT_END;
					msgHandler.handleAction(cmd);
				}
			}
		}, 0, 1, TimeUnit.SECONDS);
	}

	public void stopAdvertising() {
		myState = ServerState.LISTEN_CONNECTION_UDP;
		scheduledExecutor.shutdown();
	}
}
