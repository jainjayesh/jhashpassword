package de.janbusch.jhashpassword.sync;

import java.io.IOException;
import java.io.NotSerializableException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

import org.xml.sax.SAXParseException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import de.janbusch.jhashpassword.R;
import de.janbusch.jhashpassword.net.client.JHPClient;
import de.janbusch.jhashpassword.net.common.EActionCommand;
import de.janbusch.jhashpassword.net.common.ENetCommand;
import de.janbusch.jhashpassword.net.common.IJHPMsgHandler;
import de.janbusch.jhashpassword.net.common.Partner;
import de.janbusch.jhashpassword.net.common.SecureMessage;
import de.janbusch.jhashpassword.net.common.SecureMessage.MessageType;
import de.janbusch.jhashpassword.net.server.JHPServer;
import de.janbusch.jhashpassword.net.server.JHPServer.ServerState;
import de.janbusch.jhashpassword.xml.SimpleXMLUtil;
import de.janbusch.jhashpassword.xml.simple.config.Host;
import de.janbusch.jhashpassword.xml.simple.config.JHPConfig;
import de.janbusch.jhashpassword.xml.simple.data.HashPassword;

public class HPSync extends Activity implements IJHPMsgHandler {
	private final String TAG = "de.janbusch.jhashpassword.sync.HPSync";
	private JHPConfig config;
	private HashPassword hashPassword;
	private JHPServer myJHPServer;
	private TextView txtSyncState;
	private Switch btnStartSync;
	private TextView txtLog;
	private ScrollView scroller;
	private ToggleButton visibilityToggle;
	private WakeLock wl;

	private List<Host> acceptedList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync);

		final WifiManager wifiM = (WifiManager) getSystemService(WIFI_SERVICE);
		if (!wifiM.isWifiEnabled()) {
			Builder inputDialog = new AlertDialog.Builder(HPSync.this);
			inputDialog.setTitle(R.string.app_name_sync);
			inputDialog.setMessage(R.string.msgWifiNotEnabled);
			inputDialog.setPositiveButton(getString(R.string.Yes),
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							wifiM.setWifiEnabled(true);
						}
					});
			inputDialog.setNegativeButton(R.string.No, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			inputDialog.show();
		}

		// Grab views
		txtSyncState = (TextView) findViewById(R.id.txtSyncState);
		btnStartSync = (Switch) findViewById(R.id.btnStartSync);
		txtLog = (TextView) findViewById(R.id.txtViewLog);
		scroller = (ScrollView) findViewById(R.id.scrollViewLog);
		visibilityToggle = (ToggleButton) findViewById(R.id.toggleVisibility);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);

		// Get the hashPassword object.
		this.hashPassword = (HashPassword) getIntent().getSerializableExtra(
				getString(R.string.hp));

		// Read config xml
		readConfigXML();
	}

	private boolean readConfigXML() {
		try {
			config = SimpleXMLUtil.getConfigXML(this.getApplicationContext());
			acceptedList = config.getSynchronization().getHosts();
		} catch (Exception spE) {
			config = new JHPConfig();
			acceptedList = config.getSynchronization().getHosts();
		}

		return true;
	}

	private boolean writeConfigXML() {
		try {
			SimpleXMLUtil.writeConfigXML(config, this.getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * This method specifies what to do if a button was clicked.
	 * 
	 * @param btn
	 *            Button that has been clicked as {@link View}.
	 * @return True if the button is known otherwise false.
	 */
	public boolean onButtonClicked(View btn) {
		switch (btn.getId()) {
		case R.id.btnStartSync:
			if (btnStartSync.isChecked()) {
				txtSyncState.setText(R.string.txtConnectionPending);
				wl.acquire();

				try {
					myJHPServer = new JHPServer(this,
							Util.getBroadcastAddress(getApplicationContext()),
							Util.getMacAddressAndroid(getApplicationContext()),
							Util.getOperatingSystemAndroid());
					myJHPServer.start();
					visibilityToggle.setEnabled(true);
					visibilityToggle.setChecked(true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				if (wl != null && wl.isHeld()) {
					wl.release();
				}

				visibilityToggle.setEnabled(false);
				myJHPServer.killServer();
				txtSyncState.setText(R.string.txtNoConnection);
				return true;

			}
			return true;
		case R.id.toggleVisibility:
			if (!visibilityToggle.isChecked()) {
				myJHPServer.stopAdvertising();
			} else {
				myJHPServer.startAdvertising();
			}
			return true;
		default:
			Log.d(this.TAG, "Clicked button has no case.");
			return false;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		default:
			return super.onCreateDialog(id);
		}
	}

	@Override
	public void handleMessage(SecureMessage recMsg, InetAddress remoteAddress) {
		if (acceptedList.contains(remoteAddress)) {
			log("Received secure message of type "
					+ recMsg.getType().toString());
			switch (recMsg.getType()) {
			case REQ_HASHPASSWORD_XML:
				SecureMessage msg = new SecureMessage(
						MessageType.REC_HASHPASSWORD_XML);
				try {
					msg.setPayload(hashPassword);
				} catch (NotSerializableException e) {
					e.printStackTrace();
				}
				myJHPServer.sendSecureMessage(msg);
				break;
			default:
				break;
			}
		} else {
			log("Refused secure message from because he's not allowed to talk to us. Disconnecting him! "
					+ remoteAddress.getAddress());
			myJHPServer.disconnectSecureConnection();
		}
	}

	@Override
	public void handleMessage(ENetCommand command, final InetSocketAddress from) {
		Host fromHost = null;
		fromHost = new Host();
		fromHost.setIpAddress(from.getAddress().getHostAddress());

		switch (command) {
		case REQ:
			log("Request received from " + from.getAddress() + ", "
					+ command.getParam());
			fromHost = new Host();
			fromHost.setIpAddress(from.getAddress().getHostAddress());

			if (!acceptedList.contains(fromHost)) {
				showReqAckDialog(from, command);
			} else {
				myJHPServer.sendMessage(from, ENetCommand.ACK.toString());
			}
			break;
		case REQ_PAIR:
			if (acceptedList.contains(fromHost)) {
				showPairDialog(from, command);
			}
			break;
		default:
			log("Unknown command received: " + command);
		}
	}

	@Override
	public void handleAction(EActionCommand cmd) {
		switch (cmd) {
		case ADVERTISEMENT_LEFT:
			final int timeLeft = (Integer) cmd.getParam();
			log("Visible for: " + timeLeft + " sec.");
			break;
		case ADVERTISEMENT_END:
			log("Visibility disabled.");

			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					visibilityToggle.setChecked(false);
				}
			});
			break;
		default:
			log("Unknown action command: " + cmd);
		}
	}

	private void showReqAckDialog(final InetSocketAddress from,
			final ENetCommand command) {
		myJHPServer.stopAdvertising();
		this.handleAction(EActionCommand.ADVERTISEMENT_END);

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String msgReqAckDialog = String.format(
						getString(R.string.msgReqAckDialog), from.getAddress()
								+ ", " + command.getParam());

				Builder inputDialog = new AlertDialog.Builder(HPSync.this);
				inputDialog.setTitle(R.string.titleReqAckDialog);
				inputDialog.setMessage(msgReqAckDialog);
				inputDialog.setPositiveButton(getString(R.string.Yes),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Host host = new Host();
								host.setIpAddress(from.getAddress().toString());
								host.setMacAddress(command.getParam());

								if (!acceptedList.contains(host)) {
									acceptedList.add(host);
								}

								myJHPServer.sendMessage(from,
										ENetCommand.ACK.toString());
							}
						});
				inputDialog.setNegativeButton(getString(R.string.No),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Toast.makeText(
										getBaseContext(),
										getString(R.string.toastReqAckDiscarded),
										Toast.LENGTH_SHORT).show();
								myJHPServer.sendMessage(from,
										ENetCommand.REF.toString());
							}
						});
				inputDialog.show();
			}
		});
	}

	private void showPairDialog(final InetSocketAddress from,
			final ENetCommand command) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String msgReqAckDialog = String.format(
						getString(R.string.msgPairDialog), command.getParam());

				Builder inputDialog = new AlertDialog.Builder(HPSync.this);
				inputDialog.setTitle(R.string.titleReqAckDialog);
				inputDialog.setMessage(msgReqAckDialog);
				inputDialog.setPositiveButton(getString(R.string.Yes),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								myJHPServer.sendMessage(from,
										ENetCommand.ACK_PAIR.toString());

								Host host = new Host();
								host.setIpAddress(from.getAddress().toString());

								if (acceptedList.contains(host)) {
									int index = config.getSynchronization()
											.getHosts().indexOf(host);
									host = config.getSynchronization()
											.getHosts().get(index);
									host.setCode(command.getParam());
									writeConfigXML();
								}
								myJHPServer
										.setState(ServerState.SERVER_LISTEN_CONNECTION_TCP);
							}
						});
				inputDialog.setNegativeButton(getString(R.string.No),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Toast.makeText(
										getBaseContext(),
										getString(R.string.toastReqAckDiscarded),
										Toast.LENGTH_SHORT).show();
							}
						});
				inputDialog.show();
			}
		});
	}

	private void log(final String msg) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				txtLog.append(msg + "\n");
				scroller.post(new Runnable() {
					public void run() {
						scroller.smoothScrollTo(0, txtLog.getBottom());
					}
				});
			}
		});
	}

	@Override
	protected void onPause() {
		if (wl != null && wl.isHeld()) {
			wl.release();
		}

		if (myJHPServer != null) {
			myJHPServer.killServer();
		}

		visibilityToggle.setEnabled(false);
		txtSyncState.setText(R.string.txtNoConnection);

		writeConfigXML();

		super.onPause();
	}

	@Override
	public boolean isClientAccepted(InetAddress inetAddress) {
		if (acceptedList.contains(inetAddress)) {
			log("Accepted secure connection from " + inetAddress.getAddress()
					+ ".");
			return true;
		} else {
			log("Refused secure connection request from "
					+ inetAddress.getAddress() + ".");
			return false;
		}
	}
}
