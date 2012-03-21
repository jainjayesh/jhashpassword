package de.janbusch.jhashpassword.sync;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

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
import de.janbusch.jhashpassword.net.server.JHPServer;
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
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private boolean writeConfigXML() {
		try {
			SimpleXMLUtil.writeConfigXML(config, this.getApplicationContext());
			acceptedList = config.getSynchronization().getHosts();
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
				if (wl != null && wl.isHeld())
					wl.release();

				visibilityToggle.setEnabled(false);
				myJHPServer.killServer();
				txtSyncState.setText(R.string.txtNoConnection);
				return true;

			}
			return true;
		case R.id.toggleVisibility:
			// if (!visibilityToggle.isChecked()) {
			// myJHPServer.stopSolicitation();
			// } else {
			// myJHPServer.startSolicitation();
			// }
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
	public void handleMessage(ENetCommand command, final InetSocketAddress from) {
		switch (command) {
		case REQ:
			log("Request received from " + from.getAddress() + ", "
					+ command.getParam());
			if (!acceptedList.contains(from)) {
				showReqAckDialog(from, command);
			}
			break;
		case EST_TCP:
			if (acceptedList.contains(from)) {
				log("Creating secure tcp connection.");
			} else {
				log("Refused request from " + from.getAddress() + ", "
						+ command.getParam());
			}
			break;
		default:
			log("Unknown command received: " + command);
		}
	}

	@Override
	public void handleAction(EActionCommand cmd) {
		switch (cmd) {
		case SOLICITATION_LEFT:
			final int timeLeft = (Integer) cmd.getParam();
			log("Visible for: " + timeLeft + " sec.");
			break;
		case SOLICITATION_END:
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
								myJHPServer.sendMessage(from,
										ENetCommand.ACK.toString());
								
								Host host = new Host();
								host.setIpAddress(from.getAddress().toString());
								
								if (!acceptedList.contains(host)) {
									acceptedList.add(host);
								}
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
		if (wl != null && wl.isHeld())
			wl.release();

		if (myJHPServer != null) {
			myJHPServer.killServer();
		}
		
		writeConfigXML();

		super.onPause();
	}

}
