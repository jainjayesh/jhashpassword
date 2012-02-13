package de.janbusch.jhashpassword.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import de.janbusch.jhashpassword.R;
import de.janbusch.jhashpassword.xml.simple.HashPassword;
import de.janbusch.jhashpassword.xml.simple.Host;
import de.janbusch.jhashpassword.xml.simple.LoginName;

public class HPSettingsHost extends Activity {
	private static final String PWLEN_REGEXP = "[^0-9]";
	private HashPassword hashPassword;
	private Host currentHost;
	private EditText etCharset;
	private EditText etPWLen;
	private Spinner sprHashtype;
	private LoginName currentLogin;
	private boolean hasChanged;
	private int iCurrentSelection;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_host);
		hasChanged = false;

		// Get the hashPassword object.
		this.hashPassword = (HashPassword) getIntent().getSerializableExtra(
				getString(R.string.hp));
		this.currentHost = hashPassword.getHosts().getHostByName(
				((Host) getIntent().getSerializableExtra(
						getString(R.string.host))).getName());
		if (getIntent().getSerializableExtra(getString(R.string.login)) != null) {
			this.currentLogin = currentHost.getLoginNames().getLoginNameByName(
					((LoginName) getIntent().getSerializableExtra(
							getString(R.string.login))).getName());
		}

		etPWLen = (EditText) findViewById(R.id.etPasswordLength);
		etCharset = (EditText) findViewById(R.id.etCharset);
		sprHashtype = (Spinner) findViewById(R.id.sprHashtype);

		TextWatcher watcher = new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				hasChanged = true;
			}
		};

		etCharset.addTextChangedListener(watcher);
		etPWLen.addTextChangedListener(watcher);

		sprHashtype.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int i, long arg3) {
				if (iCurrentSelection != i) {
					hasChanged = true;
				}
				iCurrentSelection = i;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		int hashTypePos = -1;
		etPWLen.setText(currentHost.getPasswordLength().replaceAll(
				PWLEN_REGEXP, ""));
		etCharset.setText(currentHost.getCharset());
		hashTypePos = ((ArrayAdapter<CharSequence>) sprHashtype.getAdapter())
				.getPosition(currentHost.getHashType());
		if (currentLogin != null) {
			try {
				etPWLen.setText(currentLogin.getPasswordLength().replaceAll(
						PWLEN_REGEXP, ""));
				etCharset.setText(currentLogin.getCharset());
				hashTypePos = ((ArrayAdapter<CharSequence>) sprHashtype
						.getAdapter()).getPosition(currentLogin.getHashType());
			} catch (NullPointerException e) {
				// No logininfos
			}
		}

		if (hashTypePos != -1) {
			sprHashtype.setSelection(hashTypePos);
		}
		iCurrentSelection = sprHashtype.getSelectedItemPosition();
	}

	/**
	 * This method specifies what to do if a button was clicked.
	 * 
	 * @param btn
	 *            Button that has been clicked as {@link View}.
	 * @return True if the button is known otherwise false.
	 */
	@SuppressWarnings("unchecked")
	public boolean onButtonClicked(View btn) {
		switch (btn.getId()) {
		case R.id.btnReset:
			etCharset.setText(hashPassword.getDefaultCharset());
			etPWLen.setText(hashPassword.getDefaultPasswordLength().replaceAll(
					PWLEN_REGEXP, ""));
			int hashTypePos = ((ArrayAdapter<CharSequence>) sprHashtype
					.getAdapter()).getPosition(currentHost.getHashType());
			if (hashTypePos != -1) {
				sprHashtype.setSelection(hashTypePos);
			}
			hasChanged = true;
			return true;
		default:
			Log.d(this.toString(), "Clicked button has no case.");
			return false;
		}
	}

	@Override
	public void onBackPressed() {
		if (currentLogin == null) {
			currentHost.setCharset(etCharset.getText().toString()
					.replaceAll("[\r\n]+", ""));
			currentHost.setPasswordLength(etPWLen.getText().toString());
			currentHost.setHashType(sprHashtype.getSelectedItem().toString());
		} else {
			currentLogin.setCharset(etCharset.getText().toString()
					.replaceAll("[\r\n]+", ""));
			currentLogin.setPasswordLength(etPWLen.getText().toString());
			currentLogin.setHashType(sprHashtype.getSelectedItem().toString());
		}
		Intent resultIntent = new Intent();
		resultIntent.putExtra(getString(R.string.hp), hashPassword);
		resultIntent.putExtra(getString(R.string.settingsChanged), hasChanged);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}

}
