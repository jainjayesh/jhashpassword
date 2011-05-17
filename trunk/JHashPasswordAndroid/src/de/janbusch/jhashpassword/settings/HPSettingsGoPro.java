package de.janbusch.jhashpassword.settings;

import jhashpassword.gui.android.R;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import de.janbusch.hashpassword.core.DRMUtil;
import de.janbusch.jhashpassword.xml.simple.HashPassword;

public class HPSettingsGoPro extends Activity {
	private String actCode;
	private EditText etSerialNo;
	private Button btnActivate;
	private HashPassword hashPassword;
	private String imei;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_gopro);

		// Get the hashPassword object.
		this.hashPassword = (HashPassword) getIntent().getSerializableExtra(
				getString(R.string.hp));

		etSerialNo = (EditText) findViewById(R.id.etSerialNo);
		btnActivate = (Button) findViewById(R.id.btnActivate);
		EditText etGoPro = (EditText) findViewById(R.id.etActCode);
		TelephonyManager myTManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		imei = myTManager.getDeviceId();

		actCode = DRMUtil.getActivationCode(getString(R.string.app_name), imei);

		etGoPro.setText(actCode);
		setWatcher();
	}

	/**
	 * Sets all watchers for this activity.
	 */
	private void setWatcher() {
		TextWatcher charsetWatcher = new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				String serialNo = etSerialNo.getText().toString();
				if (serialNo.length() == 16 && serialNo.matches("[0-9]+")) {
					btnActivate.setEnabled(true);
				} else {
					btnActivate.setEnabled(false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// Nothing
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// Nothing

			}
		};
		etSerialNo.addTextChangedListener(charsetWatcher);
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
		case R.id.btnDonate:
			Intent donateIntent = new Intent(Intent.ACTION_VIEW, Uri
					.parse(getString(R.string.donateLink)));
			startActivity(donateIntent);
			return true;
		case R.id.btnSendActCode:
			final Intent emailIntent = new Intent(
					android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(Intent.EXTRA_EMAIL,
					new String[] { getString(R.string.myMailAddress) });
			emailIntent.putExtra(Intent.EXTRA_SUBJECT,
					getString(R.string.mailSubject));
			emailIntent.putExtra(Intent.EXTRA_TEXT,
					getString(R.string.mailBody) + "\n\nActivation-Code: "
							+ actCode);
			startActivity(Intent.createChooser(emailIntent,
					getString(R.string.btnSendActCode)));
			return true;
		case R.id.btnActivate:
			if (validateSerial()) {
				hashPassword.setSerialNo(etSerialNo.getText().toString().trim());
				setResult(Activity.RESULT_OK, new Intent().putExtra(
						getString(R.string.hp), hashPassword));
				finish();
			} else {
				Log.d(this.toString(), "Changes canceled!");
				setResult(Activity.RESULT_CANCELED);
				// Pop up an info dialog.
				Toast.makeText(getBaseContext(),
						getString(R.string.msgSerialRejected),
						Toast.LENGTH_LONG).show();
				finish();
			}
			return true;
		default:
			Log.d(this.toString(), "Clicked button has no case.");
			return false;
		}
	}

	private boolean validateSerial() {
		String pendingSerial = etSerialNo.getText().toString().trim();
		return DRMUtil.validateSerialNo(getString(R.string.app_name), imei,
				pendingSerial);
	}
	
}
