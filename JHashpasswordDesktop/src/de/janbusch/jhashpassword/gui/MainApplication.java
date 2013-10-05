package de.janbusch.jhashpassword.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.swtdesigner.SWTResourceManager;

import de.janbusch.hashpassword.core.CoreInformation;
import de.janbusch.hashpassword.core.EHashType;
import de.janbusch.hashpassword.core.HashUtil;
import de.janbusch.jhashpassword.xml.SimpleXMLUtil;
import de.janbusch.jhashpassword.xml.simple.data.HashPassword;
import de.janbusch.jhashpassword.xml.simple.data.Host;
import de.janbusch.jhashpassword.xml.simple.data.Hosts;
import de.janbusch.jhashpassword.xml.simple.data.LoginName;

/**
 * An object of this class represents the main application window.
 * 
 * @author Jan Busch
 * 
 */
public class MainApplication {

	private static final String HAS_CHANGED = "hasChanged"; //$NON-NLS-1$
	public static final String APPLICATION_TITLE = "JHashPassword"; //$NON-NLS-1$
	public static final String APPLICATION_VERSION = "1.7.0"; //$NON-NLS-1$
	// private static final String XML_PATH = CoreInformation.HASH_PASSWORD_XML;
	private static final int TIMEOUT = 1000 * 60;
	protected Shell shlJhashpassword;
	private HashPassword hashPassword;
	private Timer timer;
	private Text passwordLengthText;
	private Text characterSetText;
	private Text txtPassphrase;
	private Text txtPassphraseR;
	private Combo hostCombo;
	private Combo loginCombo;
	private Combo hashCombo;
	private Button btnGeneratePassword;
	private Button btnDelHost;
	private Button btnAddHost;
	private Button btnShowClipboard;
	private Button btnCharacterset;
	private Combo cacheCombo;
	private Button btnSave;
	private ClipboardTimerTask timerTask;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(CoreInformation.HASHPASSWORD_COPYRIGHT + "\n"); //$NON-NLS-1$
		System.out.println(CoreInformation.JHASHPASSWORD_COPYRIGHT);

		try {
			MainApplication window = new MainApplication();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		final Display display = Display.getDefault();
		createContents();
		loadXMLFile();
		createListeners();
		timer = new Timer();

		shlJhashpassword.open();
		shlJhashpassword.layout();
		shlJhashpassword.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				switch (cacheCombo.getSelectionIndex()) {
				case 0:
				case 1:
					ClipBoardUtil.addToClipboard(new String());
					break;
				default:
					break;
				}
				if (btnSave.isEnabled()) {
					saveSettings();
				}
				System.exit(0);
				// saveXMLFile();
			}
		});

		while (!shlJhashpassword.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void createListeners() {
		characterSetText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				characterSetText.setData(HAS_CHANGED, true);
				btnSave.setEnabled(true);
			}
		});
	}

	/**
	 * Load the XML-file into the program.
	 */
	private void loadXMLFile() {
		try {
			this.hashPassword = SimpleXMLUtil.getXML();

			Hosts hosts = hashPassword.getHosts();
			Host currentHost = null;
			hostCombo.removeAll();
			for (Host host : hosts.getHost()) {
				hostCombo.add(host.getName());
				if (hashPassword.getLastHost().equals(host.getName())) {
					currentHost = host;
				}
			}
			hostCombo.setText(hashPassword.getLastHost());
			if (currentHost == null) {
				if (!hashPassword.getHosts().getHost().isEmpty()) {
					currentHost = hashPassword.getHosts().getHost().get(0);
					hostCombo.setText(currentHost.getName());
				} else {
					loginCombo.removeAll();
					return;
				}
			}

			loadHostSettingsAndLogins();
		} catch (Exception e1) {
			if (e1 instanceof FileNotFoundException) {
				hashPassword = new HashPassword();
				hashPassword.setDefaultCharset(characterSetText.getText());
				hashPassword.setDefaultHashType(hashCombo.getText());
				hashPassword.setDefaultPasswordLength(passwordLengthText
						.getText());
				try {
					SimpleXMLUtil.writeXML(hashPassword);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			} else {
				e1.printStackTrace();

				MessageBox messageBox = new MessageBox(shlJhashpassword,
						SWT.ICON_ERROR | SWT.OK);
				messageBox.setText("JHashPassword"); //$NON-NLS-1$
				messageBox.setMessage(Messages.MainApplication_25);
				messageBox.open();
				System.exit(1);
			}
		}
	}

	private void loadHostSettingsAndLogins() {
		boolean enabled = btnSave.getEnabled();
		Hosts hosts = hashPassword.getHosts();
		Host currentHost = hosts.getHostByName(hostCombo.getText());
		LoginName currentLogin = null;

		passwordLengthText.setText(currentHost.getPasswordLength().replaceAll(
				"[^0-9]", "")); //$NON-NLS-1$ //$NON-NLS-2$
		characterSetText.setText(currentHost.getCharset());
		hashCombo.setText(currentHost.getHashType());

		loginCombo.removeAll();
		for (LoginName loginName : currentHost.getLoginNames().getLoginName()) {
			loginCombo.add(loginName.getName());
		}
		currentLogin = currentHost.getLoginNames().getLoginNameByName(
				currentHost.getLastLogin());

		if (currentLogin == null) {
			loginCombo.select(0);
		} else {
			try {
				loginCombo.setText(currentLogin.getName());
				if (currentLogin.getPasswordLength() != null)
					passwordLengthText.setText(currentLogin.getPasswordLength()
							.replaceAll("[^0-9]", "")); //$NON-NLS-1$ //$NON-NLS-2$
				if (currentLogin.getCharset() != null)
					characterSetText.setText(currentLogin.getCharset());
				if (currentLogin.getHashType() != null)
					hashCombo.setText(currentLogin.getHashType());
			} catch (NullPointerException e) {
				// No settings found
			}
		}
		
		btnSave.setEnabled(enabled);
	}

	/**
	 * Save the XML-file.
	 */
	private void saveXMLFile() {
		String lastHostName = hostCombo.getText();
		String lastLoginName = loginCombo.getText();

		if (lastHostName != null && !lastHostName.isEmpty()) {
			hashPassword.setLastHost(hostCombo.getText());
		}
		if (lastLoginName != null && !lastLoginName.isEmpty()) {
			Host lastHost = hashPassword.getHosts().getHostByName(
					hostCombo.getText());
			if (lastHost != null) {
				lastHost.setLastLogin(loginCombo.getText());
			}
		}

		try {
			SimpleXMLUtil.writeXML(hashPassword);
		} catch (Exception e1) {
			ErrorDialog.openError(shlJhashpassword,
					Messages.MainApplication_34, Messages.MainApplication_35,
					Status.OK_STATUS);
		}
	}

	private void checkPassphrases() {
		if (txtPassphrase.getText().equals(txtPassphraseR.getText())) {
			btnGeneratePassword.setEnabled(true);
			btnGeneratePassword.setBackground(SWTResourceManager.getColor(0,
					255, 0));
		} else {
			btnGeneratePassword.setEnabled(false);
			btnGeneratePassword.setBackground(SWTResourceManager.getColor(255,
					0, 0));
		}

		if (!txtPassphrase.getText().isEmpty()
				&& txtPassphraseR.getText().isEmpty()) {
			btnGeneratePassword.setEnabled(true);
			btnGeneratePassword.setBackground(SWTResourceManager.getColor(255,
					0, 0));
		}

		if (txtPassphrase.getText().isEmpty()
				&& txtPassphraseR.getText().isEmpty()
				|| txtPassphrase.getText().isEmpty()) {
			btnGeneratePassword.setEnabled(false);
			btnGeneratePassword.setBackground(null);
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlJhashpassword = new Shell(SWT.CLOSE | SWT.TITLE | SWT.MIN);
		shlJhashpassword.setSize(585, 383);
		shlJhashpassword
				.setImage(SWTResourceManager
						.getImage(MainApplication.class,
								"/de/janbusch/jhashpassword/images/32px-Crystal_Clear_action_lock-silver.png")); //$NON-NLS-1$
		shlJhashpassword.setText(APPLICATION_TITLE);
		shlJhashpassword.setLayout(new GridLayout(2, true));
		{
			Group grpHome = new Group(shlJhashpassword, SWT.NONE);
			grpHome.setLayout(new GridLayout(5, false));
			GridData gd_grpHome = new GridData(SWT.FILL, SWT.FILL, true, true,
					1, 1);
			gd_grpHome.widthHint = 264;
			grpHome.setLayoutData(gd_grpHome);
			grpHome.setText(Messages.MainApplication_grpHome_text);
			{
				Label lblHostNamenWhlen = new Label(grpHome, SWT.NONE);
				lblHostNamenWhlen.setText(Messages.MainApplication_5);
			}
			new Label(grpHome, SWT.NONE);
			new Label(grpHome, SWT.NONE);
			new Label(grpHome, SWT.NONE);
			new Label(grpHome, SWT.NONE);
			{
				hostCombo = new Combo(grpHome, SWT.READ_ONLY);
				hostCombo
						.setToolTipText(Messages.MainApplication_hostCombo_toolTipText);
				hostCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
						true, false, 2, 1));
				hostCombo.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						loadHostSettingsAndLogins();
						if (!btnSave.getEnabled()) {
							saveXMLFile();
						}
						ClipBoardUtil.addToClipboard(hostCombo.getText());
					}
				});
			}
			{
				btnDelHost = new Button(grpHome, SWT.NONE);
				btnDelHost.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						MessageBox messageBox = new MessageBox(
								shlJhashpassword, SWT.ICON_WARNING | SWT.YES
										| SWT.NO);
						messageBox.setText(Messages.MainApplication_29);
						messageBox.setMessage(Messages.MainApplication_38);
						int buttonID = messageBox.open();

						switch (buttonID) {
						case SWT.YES:
							Host host = hashPassword.getHosts().getHostByName(
									hostCombo.getText());
							if (host != null) {
								hashPassword.getHosts().getHost().remove(host);
								saveXMLFile();
								loadXMLFile();
							}
							break;
						case SWT.NO:
							break;
						default:
							break;
						}
					}
				});
				btnDelHost
						.setImage(SWTResourceManager
								.getImage(MainApplication.class,
										"/de/janbusch/jhashpassword/images/16px-Crystal_Clear_action_button_cancel.png")); //$NON-NLS-1$
			}
			{
				btnAddHost = new Button(grpHome, SWT.NONE);
				btnAddHost.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						InputDialog iD = new InputDialog(shlJhashpassword,
								Messages.MainApplication_0,
								Messages.MainApplication_1,
								Messages.MainApplication_2, null);
						iD.open();
						String hostName = iD.getValue();

						if (hostName == null || hostName.trim().isEmpty()) {
							return;
						}

						Host host = hashPassword.getHosts().getHostByName(
								hostName);

						if (host != null) {
							return;
						}

						Host newHost = new Host();
						newHost.setName(hostName);
						newHost.setCharset(CoreInformation.DEFAULT_CHARACTERSET);
						newHost.setHashType(CoreInformation.DEFAULT_HASHTYPE);
						newHost.setPasswordLength(CoreInformation.DEFAULT_PASSWORD_LENGTH);
						Hosts hostList = hashPassword.getHosts();
						hostList.getHost().add(newHost);

						saveXMLFile();
						loadXMLFile();

					}
				});
				btnAddHost
						.setImage(SWTResourceManager
								.getImage(MainApplication.class,
										"/de/janbusch/jhashpassword/images/16px-Crystal_Clear_action_edit_add.png")); //$NON-NLS-1$
			}
			new Label(grpHome, SWT.NONE);
			{
				Label lblLoginNamenWhlen = new Label(grpHome, SWT.NONE);
				lblLoginNamenWhlen.setText(Messages.MainApplication_8);
			}
			new Label(grpHome, SWT.NONE);
			new Label(grpHome, SWT.NONE);
			new Label(grpHome, SWT.NONE);
			new Label(grpHome, SWT.NONE);
			{
				loginCombo = new Combo(grpHome, SWT.READ_ONLY);
				loginCombo
						.setToolTipText(Messages.MainApplication_loginCombo_toolTipText);
				loginCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
						true, false, 2, 1));
				loginCombo.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Hosts hosts = hashPassword.getHosts();
						Host currentHost = hosts.getHostByName(hostCombo
								.getText());
						LoginName currentLogin = null;

						currentLogin = currentHost.getLoginNames()
								.getLoginNameByName(loginCombo.getText());
						if (currentLogin == null) {
							loginCombo.select(0);
						} else {
							try {
								loginCombo.setText(currentLogin.getName());
								passwordLengthText.setText(currentLogin
										.getPasswordLength().replaceAll(
												"[^0-9]", "")); //$NON-NLS-1$ //$NON-NLS-2$
								characterSetText.setText(currentLogin
										.getCharset());
								hashCombo.setText(currentLogin.getHashType());
							} catch (NullPointerException e1) {
								// No settings found
							}
						}

						if (!btnSave.getEnabled()) {
							saveXMLFile();
						}

						ClipBoardUtil.addToClipboard(loginCombo.getText());
					}
				});
			}
			{
				Button btnDellLogin = new Button(grpHome, SWT.NONE);
				btnDellLogin.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						MessageBox messageBox = new MessageBox(
								shlJhashpassword, SWT.ICON_WARNING | SWT.YES
										| SWT.NO);
						messageBox.setText(Messages.MainApplication_39);
						messageBox.setMessage(Messages.MainApplication_40);
						int buttonID = messageBox.open();

						switch (buttonID) {
						case SWT.YES:
							Host host = hashPassword.getHosts().getHostByName(
									hostCombo.getText());
							if (host != null) {
								LoginName loginName = host.getLoginNames()
										.getLoginNameByName(
												loginCombo.getText());
								if (loginName != null) {
									host.getLoginNames().getLoginName()
											.remove(loginName);
									saveXMLFile();
									loadXMLFile();
								}
							}
							break;
						case SWT.NO:
							break;
						default:
							break;
						}
					}
				});
				btnDellLogin
						.setImage(SWTResourceManager
								.getImage(MainApplication.class,
										"/de/janbusch/jhashpassword/images/16px-Crystal_Clear_action_button_cancel.png")); //$NON-NLS-1$
			}
			{
				Button btnAddLogin = new Button(grpHome, SWT.NONE);
				btnAddLogin.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Host host = hashPassword.getHosts().getHostByName(
								hostCombo.getText());

						if (host == null) {
							return;
						}

						InputDialog iD = new InputDialog(shlJhashpassword,
								Messages.MainApplication_3,
								Messages.MainApplication_6,
								Messages.MainApplication_7, null);
						iD.open();
						String loginName = iD.getValue();

						if (loginName == null || loginName.trim().isEmpty()) {
							return;
						}

						if (host.getLoginNames().getLoginNameByName(loginName) == null) {
							LoginName newLoginName = new LoginName();
							newLoginName.setName(loginName);
							host.getLoginNames().getLoginName()
									.add(newLoginName);

							saveXMLFile();
							loadXMLFile();
						}

					}
				});
				btnAddLogin
						.setImage(SWTResourceManager
								.getImage(MainApplication.class,
										"/de/janbusch/jhashpassword/images/16px-Crystal_Clear_action_edit_add.png")); //$NON-NLS-1$
			}
			new Label(grpHome, SWT.NONE);
			{
				Label lblPassphraseEingeben = new Label(grpHome, SWT.SHADOW_IN);
				lblPassphraseEingeben.setBackground(SWTResourceManager
						.getColor(SWT.COLOR_WIDGET_BACKGROUND));
				lblPassphraseEingeben.setText(Messages.MainApplication_10);
			}
			new Label(grpHome, SWT.NONE);
			new Label(grpHome, SWT.NONE);
			new Label(grpHome, SWT.NONE);
			new Label(grpHome, SWT.NONE);
			{
				txtPassphrase = new Text(grpHome, SWT.BORDER | SWT.PASSWORD);
				txtPassphrase.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
						false, false, 4, 1));
				txtPassphrase.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent arg0) {
						checkPassphrases();
					}
				});
			}
			new Label(grpHome, SWT.NONE);
			{
				Label lblPassphraseWiederholen = new Label(grpHome, SWT.NONE);
				lblPassphraseWiederholen.setBackground(SWTResourceManager
						.getColor(SWT.COLOR_WIDGET_BACKGROUND));
				lblPassphraseWiederholen.setText(Messages.MainApplication_11);
			}
			new Label(grpHome, SWT.NONE);
			new Label(grpHome, SWT.NONE);
			new Label(grpHome, SWT.NONE);
			new Label(grpHome, SWT.NONE);
			txtPassphraseR = new Text(grpHome, SWT.BORDER | SWT.PASSWORD);
			txtPassphraseR.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
					false, false, 4, 1));
			{
				txtPassphraseR.addKeyListener(new KeyListener() {

					@Override
					public void keyReleased(KeyEvent arg0) {
						if (arg0.keyCode == 13 || arg0.keyCode == 16777296) {
							btnGeneratePassword.notifyListeners(SWT.Selection,
									new Event());
						}
					}

					@Override
					public void keyPressed(KeyEvent arg0) {
						// Nothing
					}
				});
			}
			txtPassphraseR.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent arg0) {
					checkPassphrases();
				}
			});
			new Label(grpHome, SWT.NONE);
			{
				Composite composite = new Composite(grpHome, SWT.NONE);
				composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
						false, 4, 1));
				composite.setLayout(new GridLayout(2, false));
				{
					btnGeneratePassword = new Button(composite, SWT.NONE);
					btnGeneratePassword
							.setToolTipText(Messages.MainApplication_btnGeneratePassword_toolTipText);
					btnGeneratePassword.setLayoutData(new GridData(SWT.FILL,
							SWT.CENTER, true, false, 2, 1));
					btnGeneratePassword
							.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									String hostname = hostCombo.getText();
									String loginname = loginCombo.getText();
									final String passphrase = txtPassphrase
											.getText();
									EHashType hashType = EHashType
											.valueOf(hashCombo.getText());
									String characterSet = characterSetText
											.getText();
									int maxPwLength = Integer
											.parseInt(passwordLengthText
													.getText());

									String password = HashUtil
											.generatePassword(hostname,
													loginname, passphrase,
													hashType, characterSet,
													maxPwLength);

									ClipBoardUtil.addToClipboard(password);

									switch (cacheCombo.getSelectionIndex()) {
									case 1:
										if (timerTask != null) {
											timerTask.cancel();
										}
										timerTask = new ClipboardTimerTask();
										timer.schedule(timerTask, TIMEOUT);
										break;
									default:
										break;
									}
								}
							});
					btnGeneratePassword.setText(Messages.MainApplication_12);
					btnGeneratePassword.setEnabled(false);
				}
				{
					btnShowClipboard = new Button(composite, SWT.NONE);
					btnShowClipboard
							.setToolTipText(Messages.MainApplication_btnShowClipboard_toolTipText);
					btnShowClipboard.setLayoutData(new GridData(SWT.FILL,
							SWT.CENTER, true, false, 1, 1));
					btnShowClipboard
							.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent e) {
									String clipboardString = ClipBoardUtil
											.getFromClipboard();

									MessageBox mB = new MessageBox(
											shlJhashpassword);
									mB.setText(Messages.MainApplication_48);
									mB.setMessage(clipboardString);
									mB.open();
								}
							});
					btnShowClipboard.setText(Messages.MainApplication_13);
				}
				{
					Button btnShowQRcode = new Button(composite, SWT.NONE);
					btnShowQRcode
							.setToolTipText(Messages.MainApplication_btnShowQRcode_toolTipText);
					btnShowQRcode.setLayoutData(new GridData(SWT.FILL,
							SWT.CENTER, true, false, 1, 1));
					btnShowQRcode.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							String clipboardString = ClipBoardUtil
									.getFromClipboard();

							try {
								MultiFormatWriter writer = new MultiFormatWriter();
								Hashtable<EncodeHintType, ErrorCorrectionLevel> hints = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
								hints.put(EncodeHintType.ERROR_CORRECTION,
										ErrorCorrectionLevel.Q);
								MatrixToImageWriter.writeToFile(writer.encode(
										clipboardString, BarcodeFormat.QR_CODE,
										300, 300, hints), "png", new File( //$NON-NLS-1$
										CoreInformation.QRCODEFILE));
								new QRCodeDialog(shlJhashpassword).open();
							} catch (Exception e1) {
								MessageBox messageBox = new MessageBox(
										shlJhashpassword, SWT.ICON_INFORMATION
												| SWT.OK);
								messageBox.setText("QR-Code"); //$NON-NLS-1$
								messageBox
										.setMessage(Messages.MainApplication_50);
								messageBox.open();
							}
						}
					});
					btnShowQRcode.setText("QR-Code"); //$NON-NLS-1$
				}
			}
			new Label(grpHome, SWT.NONE);
		}

		Group grpSettings = new Group(shlJhashpassword, SWT.NONE);
		grpSettings.setLayout(new GridLayout(2, false));
		grpSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));
		grpSettings.setText(Messages.MainApplication_grpSettings_text);
		{
			Label lblHashTypWhlen = new Label(grpSettings, SWT.NONE);
			lblHashTypWhlen.setText(Messages.MainApplication_15);
		}
		hashCombo = new Combo(grpSettings, SWT.READ_ONLY);
		hashCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		for (EHashType hashType : EHashType.values()) {
			hashCombo.add(hashType.toString());
		}
		hashCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnSave.setEnabled(true);
			}
		});
		hashCombo.select(1);
		{
			Label lblMaximalePasswortLnge = new Label(grpSettings, SWT.NONE);
			lblMaximalePasswortLnge.setText(Messages.MainApplication_16);
		}
		{
			passwordLengthText = new Text(grpSettings, SWT.BORDER);
			passwordLengthText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
					true, false, 1, 1));
			passwordLengthText.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					if ((Boolean) passwordLengthText.getData(HAS_CHANGED)) {
						btnSave.setEnabled(true);
						passwordLengthText.setData(HAS_CHANGED, false);
					}
				}
			});
			passwordLengthText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent arg0) {
					passwordLengthText.setData(HAS_CHANGED, true);
				}
			});
			passwordLengthText.setText(CoreInformation.DEFAULT_PASSWORD_LENGTH);
		}
		{
			Label lblCharacterSetDefinieren = new Label(grpSettings, SWT.NONE);
			lblCharacterSetDefinieren.setText(Messages.MainApplication_18);
		}
		{
			btnCharacterset = new Button(grpSettings, SWT.NONE);
			btnCharacterset
					.setToolTipText(Messages.MainApplication_btnCharacterset_toolTipText);
			btnCharacterset.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					char[] currentCharacterSet = characterSetText.getText()
							.toCharArray();
					List<Character> charList = new ArrayList<Character>();
					StringBuilder sB = new StringBuilder();

					for (char c : currentCharacterSet) {
						charList.add(c);
					}

					while (charList.size() > 0) {
						sB.append(charList.remove((int) (Math.random() * charList
								.size())));
					}

					characterSetText.setText(sB.toString());
					characterSetText.notifyListeners(SWT.FocusOut, new Event());
				}
			});
			btnCharacterset
					.setImage(SWTResourceManager
							.getImage(MainApplication.class,
									"/de/janbusch/jhashpassword/images/16px-Crystal_Clear_action_edit.png")); //$NON-NLS-1$
		}
		{
			characterSetText = new Text(grpSettings, SWT.BORDER | SWT.WRAP
					| SWT.MULTI);
			GridData gd_characterSetText = new GridData(SWT.FILL, SWT.TOP,
					true, true, 2, 1);
			gd_characterSetText.heightHint = 106;
			gd_characterSetText.widthHint = 232;
			characterSetText.setLayoutData(gd_characterSetText);
			characterSetText.setSize(100, characterSetText.getSize().y);
			characterSetText.setText(CoreInformation.DEFAULT_CHARACTERSET);
		}
		{
			Label lblZwischenablageLeer = new Label(grpSettings, SWT.NONE);
			lblZwischenablageLeer.setText(Messages.MainApplication_20);
		}
		new Label(grpSettings, SWT.NONE);
		{
			cacheCombo = new Combo(grpSettings, SWT.READ_ONLY);
			cacheCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
					false, 2, 1));
			cacheCombo.setVisibleItemCount(3);
			cacheCombo
					.setToolTipText(Messages.MainApplication_cacheCombo_toolTipText_1); //$NON-NLS-1$
			cacheCombo.setItems(new String[] { Messages.MainApplication_21,
					Messages.MainApplication_22, Messages.MainApplication_23 });
			cacheCombo.select(1);
		}
		{
			Group grpButtons = new Group(shlJhashpassword, SWT.NONE);
			grpButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					false, 2, 1));
			grpButtons.setLayout(new GridLayout(5, false));
			{
				btnSave = new Button(grpButtons, SWT.NONE);
				btnSave.setEnabled(false);
				btnSave.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						saveSettings();
					}
				});
				btnSave.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
						false, 1, 1));
				btnSave.setText(Messages.MainApplication_36);
			}
			{
				Button btnSync = new Button(grpButtons, SWT.NONE);
				btnSync.setEnabled(false);
				btnSync.setToolTipText(Messages.MainApplication_btnSync_toolTipText);
				btnSync.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
						false, 1, 1));
				btnSync.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						new SyncDialog(shlJhashpassword, SWT.DIALOG_TRIM)
								.open();
					}
				});
				btnSync.setText(Messages.MainApplication_btnSync_text);
			}
			{
				Button btnHilfef = new Button(grpButtons, SWT.NONE);
				btnHilfef.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						new HelpDialog(shlJhashpassword).open();
					}
				});
				btnHilfef.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
						true, false, 1, 1));
				btnHilfef.setText(Messages.MainApplication_26);
			}
			{
				Button btnberJhp = new Button(grpButtons, SWT.NONE);
				btnberJhp.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						new AboutDialog(shlJhashpassword).open();
					}
				});
				btnberJhp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
						true, false, 1, 1));
				btnberJhp.setText(Messages.MainApplication_31);
			}
			{
				Button btnbeenden = new Button(grpButtons, SWT.NONE);
				btnbeenden.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (btnSave.isEnabled()) {
							saveSettings();
						}
						switch (cacheCombo.getSelectionIndex()) {
						case 0:
						case 1:
							ClipBoardUtil.addToClipboard(new String());
							break;
						default:
							break;
						}
						// saveXMLFile();
						System.exit(0);
					}
				});
				btnbeenden.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
						true, false, 1, 1));
				btnbeenden.setText(Messages.MainApplication_37);
			}
		}
	}

	private void saveSettings() {
		MessageBox messageBox = new MessageBox(shlJhashpassword,
				SWT.ICON_WARNING | SWT.YES | SWT.NO);
		messageBox.setText(Messages.MainApplication_57);
		messageBox.setMessage(Messages.MainApplication_59);
		int buttonID = messageBox.open();

		switch (buttonID) {
		case SWT.YES:
			try {
				Hosts hosts = hashPassword.getHosts();
				Host currentHost = hosts.getHostByName(hostCombo.getText());
				LoginName currentLogin = currentHost.getLoginNames()
						.getLoginNameByName(loginCombo.getText());
				if (currentHost.getLoginNames().getLoginName().size() > 1) {
					currentLogin.setCharset(characterSetText.getText());
					currentLogin.setHashType(hashCombo.getText());
					currentLogin
							.setPasswordLength(passwordLengthText.getText());
				} else {
					currentHost.setCharset(characterSetText.getText());
					currentHost.setHashType(hashCombo.getText());
					currentHost.setPasswordLength(passwordLengthText.getText());
				}

				saveXMLFile();
				MessageBox mB = new MessageBox(shlJhashpassword);
				mB.setText(Messages.MainApplication_32);
				mB.setMessage(Messages.MainApplication_33);
				mB.open();
			} catch (Exception exc) {
				ErrorDialog.openError(shlJhashpassword,
						Messages.MainApplication_34,
						Messages.MainApplication_35, Status.OK_STATUS);
			}
			break;
		case SWT.NO:
			loadXMLFile();
			break;
		default:
			loadXMLFile();
			break;
		}

		btnSave.setEnabled(false);
	}

	class ClipboardTimerTask extends TimerTask {
		@Override
		public void run() {
			ClipBoardUtil.addToClipboard(new String());
		}
	};
}
