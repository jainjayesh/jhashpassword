package de.janbusch.jhashpassword.net.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
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

	public static final int CLIENT_PORT_UDP = 4833;
	public static final int CLIENT_PORT_TCP = 4835;
	private static final int CLIENT_TIMEOUT = 500;

	private static final int PARALLELISM_FACTOR = 2;

	public enum ClientState {
		LISTEN_ADVERTISEMENT, IDLE, SENDING_SOLICITATION, SHUTTING_DOWN, LISTEN_CONNECTION_UDP, SERVER_LISTEN_CONNECTION_TCP, CLIENT_LISTEN_CONNECTION_TCP, LISTEN_MSG_TCP
	};

	private ClientState myState;
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

	public JHPClient(IJHPMsgHandler msgHandler, InetAddress broadcastAddress,
			String macAddress, String operatingSystem) throws IOException {
		this.msgHandler = msgHandler;
		this.executor = Executors.newSingleThreadExecutor();
		this.taskQueueExecutor = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors() * PARALLELISM_FACTOR);
		this.taskQueue = new LinkedList<Runnable>();
		this.broadcast = broadcastAddress;
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

		executor.execute(new Runnable() {
			@Override
			public void run() {
				while (!Thread.interrupted()) {
					try {
						taskQueueExecutor.execute(taskQueue.remove());
					} catch (NoSuchElementException ne) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException ie) {
							// STOP
						}
					} catch (RejectedExecutionException re) {
						// STOP
					}
				}
			}
		});

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
				taskQueue.clear();
				executor.shutdown();
				taskQueueExecutor.shutdown();
				scheduledExecutor.shutdown();
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
		
		try {
			this.inputSocketUDP.close();
			this.serverSocketTCP.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
					ENetCommand req = ENetCommand.REP_OS;
					req.setParameter(macAddress + "|" + operatingSystem);

					sendMessage(new InetSocketAddress(broadcast,
							JHPServer.SERVER_PORT_UDP), req.toString());
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
