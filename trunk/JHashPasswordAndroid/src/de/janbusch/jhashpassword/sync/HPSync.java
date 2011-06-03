package de.janbusch.jhashpassword.sync;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.Dialog;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import de.janbusch.jhashpassword.R;
import de.janbusch.jhashpassword.sync.SyncMessage.ObserverData;
import de.janbusch.jhashpassword.xml.simple.HashPassword;

public class HPSync extends Activity implements Observer {
	private Synchronization mySync;
	private HashPassword hashPassword;

	private TextView txtSyncState;
	private Button btnStartSync;
	private Button btnStopSync;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync);

		WifiManager wifiM = (WifiManager) getSystemService(WIFI_SERVICE);
		if (!wifiM.isWifiEnabled()) {
			// Pop up an info dialog.
			Toast.makeText(getBaseContext(),
					getString(R.string.msgWifiNotEnabled), Toast.LENGTH_LONG)
					.show();
			finish();
		}

		// Grab views
		txtSyncState = (TextView) findViewById(R.id.txtSyncState);
		btnStartSync = (Button) findViewById(R.id.btnStartSync);
		btnStopSync = (Button) findViewById(R.id.btnStopSync);

		// Get the hashPassword object.
		this.hashPassword = (HashPassword) getIntent().getSerializableExtra(
				getString(R.string.hp));
		this.mySync = new Synchronization(this, getBaseContext(), hashPassword);
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
			btnStartSync.setEnabled(false);
			mySync.startSync();
			btnStopSync.setEnabled(true);
			return true;
		case R.id.btnStopSync:
			btnStopSync.setEnabled(false);
			mySync.stopSync();
			btnStartSync.setEnabled(true);
			return true;
		default:
			Log.d(this.toString(), "Clicked button has no case.");
			return false;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case Synchronization.CONNECTION_ESTABLISHING_DIALOG:
			return mySync.getProgressDialog();
		default:
			return super.onCreateDialog(id);
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		ObserverData observerData = (ObserverData) data;

		switch (observerData) {
		case CONNECTION_ESTABLISHED:
			txtSyncState.setText(getString(R.string.txtConnectionEstablished));
			btnStartSync.setEnabled(false);
			btnStopSync.setEnabled(true);
			break;
		case CONNECTION_DISCONNECTED:
			txtSyncState.setText(getString(R.string.txtNoConnection));
			btnStartSync.setEnabled(true);
			btnStopSync.setEnabled(false);
			break;
		}
	}
}
