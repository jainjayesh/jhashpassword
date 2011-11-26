package de.janbusch.jhashpassword.gui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.swtdesigner.SWTResourceManager;

import de.janbusch.hashpassword.core.CoreInformation;
import de.janbusch.hashpassword.core.EHashType;
import de.janbusch.hashpassword.core.HashUtil;
import de.janbusch.jhashpassword.net.ENetCommand;
import de.janbusch.jhashpassword.net.IJHPMsgHandler;
import de.janbusch.jhashpassword.net.JHPServer;
import de.janbusch.jhashpassword.xml.SimpleXMLUtil;
import de.janbusch.jhashpassword.xml.simple.HashPassword;
import de.janbusch.jhashpassword.xml.simple.Host;
import de.janbusch.jhashpassword.xml.simple.Hosts;
import de.janbusch.jhashpassword.xml.simple.LoginName;

/**
 * An object of this class represents the main application window.
 * 
 * @author Jan Busch
 * 
 */
public class MainApplication implements IJHPMsgHandler {

	public static final String APPLICATION_TITLE = "JHashPassword";
	public static final String APPLICATION_VERSION = "1.6.41";
	private static final String XML_PATH = CoreInformation.HASH_PASSWORD_XML;
	protected Shell shlJhashpassword;
	private HashPassword hashPassword;
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
	protected JHPServer myServer;

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
		Display display = Display.getDefault();
		createContents();
		loadXMLFile();
		shlJhashpassword.open();
		shlJhashpassword.layout();
		while (!shlJhashpassword.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Load the XML-file into the program.
	 */
	private void loadXMLFile() {
		try {
			this.hashPassword = SimpleXMLUtil.getXML(XML_PATH);

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
					SimpleXMLUtil.writeXML(hashPassword, XML_PATH);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			} else {
				e1.printStackTrace();

				MessageBox messageBox = new MessageBox(shlJhashpassword,
						SWT.ICON_ERROR | SWT.OK);
				messageBox.setText("JHashPassword");
				messageBox.setMessage(Messages.MainApplication_25);
				messageBox.open();
				System.exit(1);
			}
		}
	}

	private void loadHostSettingsAndLogins() {
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
				passwordLengthText.setText(currentLogin.getPasswordLength()
						.replaceAll("[^0-9]", "")); //$NON-NLS-1$ //$NON-NLS-2$
				characterSetText.setText(currentLogin.getCharset());
				hashCombo.setText(currentLogin.getHashType());
			} catch (NullPointerException e) {
				// No settings found
			}
		}
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
			SimpleXMLUtil.writeXML(hashPassword, XML_PATH);
		} catch (Exception e1) {
			ErrorDialog.openError(shlJhashpassword,
					Messages.MainApplication_34, Messages.MainApplication_35,
					Status.OK_STATUS);
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlJhashpassword = new Shell(SWT.CLOSE | SWT.TITLE | SWT.MIN);
		shlJhashpassword.setMinimumSize(new Point(340, 480));
		shlJhashpassword
				.setImage(SWTResourceManager
						.getImage(MainApplication.class,
								"/de/janbusch/jhashpassword/images/32px-Crystal_Clear_action_lock-silver.png"));
		shlJhashpassword.setSize(300, 480);
		shlJhashpassword.setText(APPLICATION_TITLE); //$NON-NLS-1$
		shlJhashpassword.setLayout(new GridLayout(1, false));
		{
			Group group = new Group(shlJhashpassword, SWT.NONE);
			group.setLayout(new FillLayout(SWT.HORIZONTAL));
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
					2));
			{
				ExpandBar expandBar = new ExpandBar(group, SWT.V_SCROLL);
				expandBar.setBackground(SWTResourceManager
						.getColor(SWT.COLOR_WIDGET_BACKGROUND));
				{
					ExpandItem xpndtmHostRelationship = new ExpandItem(
							expandBar, SWT.NONE);
					xpndtmHostRelationship.setExpanded(true);
					xpndtmHostRelationship
							.setImage(SWTResourceManager
									.getImage(MainApplication.class,
											"/de/janbusch/jhashpassword/images/32px-Crystal_Clear_action_gohome.png"));
					xpndtmHostRelationship.setText(Messages.MainApplication_4);
					{
						Composite composite = new Composite(expandBar,
								SWT.BORDER);
						xpndtmHostRelationship.setControl(composite);
						composite.setLayout(new GridLayout(3, false));
						{
							Label lblHostNamenWhlen = new Label(composite,
									SWT.NONE);
							lblHostNamenWhlen
									.setText(Messages.MainApplication_5);
						}
						new Label(composite, SWT.NONE);
						new Label(composite, SWT.NONE);
						{
							hostCombo = new Combo(composite, SWT.READ_ONLY);
							hostCombo
									.addSelectionListener(new SelectionAdapter() {
										@Override
										public void widgetSelected(
												SelectionEvent e) {
											loadHostSettingsAndLogins();
											ClipBoardUtil
													.addToClipboard(hostCombo
															.getText());
										}
									});
							hostCombo.setLayoutData(new GridData(SWT.FILL,
									SWT.CENTER, true, false, 1, 1));
						}
						{
							btnDelHost = new Button(composite, SWT.NONE);
							btnDelHost
									.addSelectionListener(new SelectionAdapter() {
										public void widgetSelected(
												SelectionEvent e) {
											MessageBox messageBox = new MessageBox(
													shlJhashpassword,
													SWT.ICON_WARNING | SWT.YES
															| SWT.NO);
											messageBox
													.setText(Messages.MainApplication_29);
											messageBox
													.setMessage(Messages.MainApplication_38);
											int buttonID = messageBox.open();

											switch (buttonID) {
											case SWT.YES:
												Host host = hashPassword
														.getHosts()
														.getHostByName(
																hostCombo
																		.getText());
												if (host != null) {
													hashPassword.getHosts()
															.getHost()
															.remove(host);
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
													"/de/janbusch/jhashpassword/images/16px-Crystal_Clear_action_button_cancel.png"));
						}
						{
							btnAddHost = new Button(composite, SWT.NONE);
							btnAddHost
									.addSelectionListener(new SelectionAdapter() {
										@Override
										public void widgetSelected(
												SelectionEvent e) {
											InputDialog iD = new InputDialog(
													shlJhashpassword,
													Messages.MainApplication_0,
													Messages.MainApplication_1,
													Messages.MainApplication_2,
													null);
											iD.open();
											String hostName = iD.getValue();

											if (hostName == null
													|| hostName.trim()
															.isEmpty()) {
												return;
											}

											Host host = hashPassword.getHosts()
													.getHostByName(hostName);

											if (host != null) {
												return;
											}

											Host newHost = new Host();
											newHost.setName(hostName);
											newHost.setCharset(CoreInformation.DEFAULT_CHARACTERSET);
											newHost.setHashType(CoreInformation.DEFAULT_HASHTYPE);
											newHost.setPasswordLength(CoreInformation.DEFAULT_PASSWORD_LENGTH);
											Hosts hostList = hashPassword
													.getHosts();
											hostList.getHost().add(newHost);

											saveXMLFile();
											loadXMLFile();

										}
									});
							btnAddHost
									.setImage(SWTResourceManager
											.getImage(MainApplication.class,
													"/de/janbusch/jhashpassword/images/16px-Crystal_Clear_action_edit_add.png"));
						}
						{
							Label lblLoginNamenWhlen = new Label(composite,
									SWT.NONE);
							lblLoginNamenWhlen
									.setText(Messages.MainApplication_8);
						}
						new Label(composite, SWT.NONE);
						new Label(composite, SWT.NONE);
						{
							loginCombo = new Combo(composite, SWT.READ_ONLY);
							{
								GridData gridData = new GridData(SWT.FILL,
										SWT.CENTER, true, false, 1, 1);
								gridData.widthHint = 163;
								loginCombo.setLayoutData(gridData);
							}
							loginCombo
									.addSelectionListener(new SelectionAdapter() {
										@Override
										public void widgetSelected(
												SelectionEvent e) {
											Hosts hosts = hashPassword
													.getHosts();
											Host currentHost = hosts
													.getHostByName(hostCombo
															.getText());
											LoginName currentLogin = null;

											currentLogin = currentHost
													.getLoginNames()
													.getLoginNameByName(
															loginCombo
																	.getText());
											if (currentLogin == null) {
												loginCombo.select(0);
											} else {
												try {
													loginCombo
															.setText(currentLogin
																	.getName());
													passwordLengthText
															.setText(currentLogin
																	.getPasswordLength()
																	.replaceAll(
																			"[^0-9]", "")); //$NON-NLS-1$ //$NON-NLS-2$
													characterSetText
															.setText(currentLogin
																	.getCharset());
													hashCombo
															.setText(currentLogin
																	.getHashType());
												} catch (NullPointerException e1) {
													// No settings found
												}
											}

											ClipBoardUtil
													.addToClipboard(loginCombo
															.getText());
										}
									});
						}
						{
							Button btnDellLogin = new Button(composite,
									SWT.NONE);
							btnDellLogin
									.addSelectionListener(new SelectionAdapter() {
										@Override
										public void widgetSelected(
												SelectionEvent e) {
											MessageBox messageBox = new MessageBox(
													shlJhashpassword,
													SWT.ICON_WARNING | SWT.YES
															| SWT.NO);
											messageBox
													.setText(Messages.MainApplication_39);
											messageBox
													.setMessage(Messages.MainApplication_40);
											int buttonID = messageBox.open();

											switch (buttonID) {
											case SWT.YES:
												Host host = hashPassword
														.getHosts()
														.getHostByName(
																hostCombo
																		.getText());
												if (host != null) {
													LoginName loginName = host
															.getLoginNames()
															.getLoginNameByName(
																	loginCombo
																			.getText());
													if (loginName != null) {
														host.getLoginNames()
																.getLoginName()
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
													"/de/janbusch/jhashpassword/images/16px-Crystal_Clear_action_button_cancel.png"));
						}
						{
							Button btnAddLogin = new Button(composite, SWT.NONE);
							btnAddLogin
									.addSelectionListener(new SelectionAdapter() {
										@Override
										public void widgetSelected(
												SelectionEvent e) {
											Host host = hashPassword
													.getHosts()
													.getHostByName(
															hostCombo.getText());

											if (host == null) {
												return;
											}

											InputDialog iD = new InputDialog(
													shlJhashpassword,
													Messages.MainApplication_3,
													Messages.MainApplication_6,
													Messages.MainApplication_7,
													null);
											iD.open();
											String loginName = iD.getValue();

											if (loginName == null
													|| loginName.trim()
															.isEmpty()) {
												return;
											}

											if (host.getLoginNames()
													.getLoginNameByName(
															loginName) == null) {
												LoginName newLoginName = new LoginName();
												newLoginName.setName(loginName);
												host.getLoginNames()
														.getLoginName()
														.add(newLoginName);

												saveXMLFile();
												loadXMLFile();
											}

										}
									});
							btnAddLogin
									.setImage(SWTResourceManager
											.getImage(MainApplication.class,
													"/de/janbusch/jhashpassword/images/16px-Crystal_Clear_action_edit_add.png"));
						}
						composite.setTabList(new Control[] { hostCombo,
								loginCombo, btnDelHost, btnAddHost });
					}
					xpndtmHostRelationship
							.setHeight(xpndtmHostRelationship.getControl()
									.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
				}
				{
					ExpandItem xpndtmPassphrase = new ExpandItem(expandBar,
							SWT.NONE);
					xpndtmPassphrase.setExpanded(true);
					xpndtmPassphrase
							.setImage(SWTResourceManager
									.getImage(MainApplication.class,
											"/de/janbusch/jhashpassword/images/32px-Crystal_Clear_action_lock-silver.png"));
					xpndtmPassphrase.setText(Messages.MainApplication_9);
					{
						Composite composite = new Composite(expandBar,
								SWT.BORDER);
						composite.setBackground(SWTResourceManager
								.getColor(SWT.COLOR_WIDGET_BACKGROUND));
						xpndtmPassphrase.setControl(composite);
						composite.setLayout(new GridLayout(2, false));
						{
							Label lblPassphraseEingeben = new Label(composite,
									SWT.SHADOW_IN);
							lblPassphraseEingeben
									.setBackground(SWTResourceManager
											.getColor(SWT.COLOR_WIDGET_BACKGROUND));
							lblPassphraseEingeben.setLayoutData(new GridData(
									SWT.LEFT, SWT.CENTER, false, false, 2, 1));
							lblPassphraseEingeben
									.setText(Messages.MainApplication_10);
						}
						{
							txtPassphrase = new Text(composite, SWT.BORDER
									| SWT.PASSWORD);
							txtPassphrase
									.addModifyListener(new ModifyListener() {
										public void modifyText(ModifyEvent arg0) {
											if (!txtPassphrase.getText()
													.isEmpty()
													&& txtPassphrase
															.getText()
															.equals(txtPassphraseR
																	.getText())) {
												btnGeneratePassword
														.setEnabled(true);
											} else {
												btnGeneratePassword
														.setEnabled(false);
											}
										}
									});
							{
								GridData gridData = new GridData(SWT.FILL,
										SWT.CENTER, true, false, 2, 1);
								gridData.widthHint = 250;
								txtPassphrase.setLayoutData(gridData);
							}
						}
						{
							Label lblPassphraseWiederholen = new Label(
									composite, SWT.NONE);
							lblPassphraseWiederholen
									.setBackground(SWTResourceManager
											.getColor(SWT.COLOR_WIDGET_BACKGROUND));
							lblPassphraseWiederholen
									.setLayoutData(new GridData(SWT.LEFT,
											SWT.CENTER, false, false, 2, 1));
							lblPassphraseWiederholen
									.setText(Messages.MainApplication_11);
						}
						{
							txtPassphraseR = new Text(composite, SWT.BORDER
									| SWT.PASSWORD);
							{
								GridData gridData = new GridData(SWT.FILL,
										SWT.CENTER, true, false, 2, 1);
								gridData.widthHint = 250;
								txtPassphraseR.setLayoutData(gridData);
								txtPassphraseR
										.addKeyListener(new KeyListener() {

											@Override
											public void keyReleased(
													KeyEvent arg0) {
												if (arg0.keyCode == 13 || arg0.keyCode == 16777296) {
													btnGeneratePassword.notifyListeners(SWT.Selection, new Event());
												}
											}

											@Override
											public void keyPressed(KeyEvent arg0) {
												// Nothing
											}
										});
							}
							txtPassphraseR
									.addModifyListener(new ModifyListener() {
										public void modifyText(ModifyEvent arg0) {
											if (!txtPassphraseR.getText()
													.isEmpty()
													&& txtPassphraseR
															.getText()
															.equals(txtPassphrase
																	.getText())) {
												btnGeneratePassword
														.setEnabled(true);
											} else {
												btnGeneratePassword
														.setEnabled(false);
											}
										}
									});
						}
						{
							btnGeneratePassword = new Button(composite,
									SWT.NONE);
							btnGeneratePassword
									.addSelectionListener(new SelectionAdapter() {
										@Override
										public void widgetSelected(
												SelectionEvent e) {
											String hostname = hostCombo
													.getText();
											String loginname = loginCombo
													.getText();
											final String passphrase = txtPassphrase
													.getText();
											EHashType hashType = EHashType
													.valueOf(hashCombo
															.getText());
											String characterSet = characterSetText
													.getText();
											int maxPwLength = Integer
													.parseInt(passwordLengthText
															.getText());

											String password = HashUtil
													.generatePassword(hostname,
															loginname,
															passphrase,
															hashType,
															characterSet,
															maxPwLength);

											ClipBoardUtil
													.addToClipboard(password);

											txtPassphrase.setText("");
											txtPassphraseR.setText("");
										}
									});
							btnGeneratePassword.setLayoutData(new GridData(
									SWT.FILL, SWT.CENTER, false, false, 1, 1));
							btnGeneratePassword
									.setText(Messages.MainApplication_12);
							btnGeneratePassword.setEnabled(false);
						}
						{
							btnShowClipboard = new Button(composite, SWT.NONE);
							btnShowClipboard
									.addSelectionListener(new SelectionAdapter() {
										@Override
										public void widgetSelected(
												SelectionEvent e) {
											String clipboardString = ClipBoardUtil
													.getFromClipboard();

											MessageBox mB = new MessageBox(
													shlJhashpassword);
											mB.setText(Messages.MainApplication_48);
											mB.setMessage(clipboardString);
											mB.open();
										}
									});
							btnShowClipboard.setLayoutData(new GridData(
									SWT.FILL, SWT.CENTER, false, false, 1, 1));
							btnShowClipboard
									.setText(Messages.MainApplication_13);
						}
						composite.setTabList(new Control[] { txtPassphrase,
								txtPassphraseR, btnGeneratePassword,
								btnShowClipboard });
					}
					xpndtmPassphrase.setHeight(xpndtmPassphrase.getControl()
							.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
				}
				{
					ExpandItem xpndtmSettings = new ExpandItem(expandBar,
							SWT.NONE);
					xpndtmSettings
							.setImage(SWTResourceManager
									.getImage(MainApplication.class,
											"/de/janbusch/jhashpassword/images/32px-Crystal_Clear_app_kcontrol.png"));
					xpndtmSettings.setText(Messages.MainApplication_14);
					{
						Composite composite = new Composite(expandBar,
								SWT.BORDER);
						xpndtmSettings.setControl(composite);
						composite.setLayout(new GridLayout(4, false));
						{
							Label lblHashTypWhlen = new Label(composite,
									SWT.NONE);
							lblHashTypWhlen.setLayoutData(new GridData(
									SWT.LEFT, SWT.CENTER, false, false, 2, 1));
							lblHashTypWhlen
									.setText(Messages.MainApplication_15);
						}
						{
							hashCombo = new Combo(composite, SWT.READ_ONLY);
							hashCombo
									.addSelectionListener(new SelectionAdapter() {
										@Override
										public void widgetSelected(
												SelectionEvent e) {
											btnSave.setEnabled(true);
										}
									});
							hashCombo.setLayoutData(new GridData(SWT.FILL,
									SWT.CENTER, false, false, 2, 1));
							for (EHashType hashType : EHashType.values()) {
								hashCombo.add(hashType.toString());
							}
							hashCombo.select(1);
						}
						{
							Label lblMaximalePasswortLnge = new Label(
									composite, SWT.NONE);
							lblMaximalePasswortLnge.setLayoutData(new GridData(
									SWT.LEFT, SWT.CENTER, false, false, 2, 1));
							lblMaximalePasswortLnge
									.setText(Messages.MainApplication_16);
						}
						{
							passwordLengthText = new Text(composite, SWT.BORDER);
							passwordLengthText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
							passwordLengthText
									.addFocusListener(new FocusAdapter() {
										@Override
										public void focusLost(FocusEvent e) {
											if ((Boolean) passwordLengthText
													.getData("hasChanged")) {
												btnSave.setEnabled(true);
												passwordLengthText.setData(
														"hasChanged", false);
											}
										}
									});
							passwordLengthText
									.addModifyListener(new ModifyListener() {
										public void modifyText(ModifyEvent arg0) {
											passwordLengthText.setData(
													"hasChanged", true);
										}
									});
							passwordLengthText
									.setText(CoreInformation.DEFAULT_PASSWORD_LENGTH);
						}
						{
							Label lblCharacterSetDefinieren = new Label(
									composite, SWT.NONE);
							lblCharacterSetDefinieren
									.setText(Messages.MainApplication_18);
						}
						new Label(composite, SWT.NONE);
						new Label(composite, SWT.NONE);
						new Label(composite, SWT.NONE);
						{
							characterSetText = new Text(composite, SWT.BORDER
									| SWT.WRAP | SWT.MULTI);
							characterSetText
									.addFocusListener(new FocusAdapter() {
										@Override
										public void focusLost(FocusEvent e) {
											if ((Boolean) characterSetText
													.getData("hasChanged")) {
												btnSave.setEnabled(true);
												characterSetText.setData(
														"hasChanged", false);
											}
										}
									});
							characterSetText
									.addModifyListener(new ModifyListener() {
										public void modifyText(ModifyEvent arg0) {
											characterSetText.setData(
													"hasChanged", true);
										}
									});
							{
								GridData gridData = new GridData(SWT.FILL,
										SWT.CENTER, true, false, 3, 1);
								gridData.widthHint = 158;
								characterSetText.setLayoutData(gridData);
							}
							characterSetText.setSize(140,
									characterSetText.getSize().y);
							characterSetText
									.setText(CoreInformation.DEFAULT_CHARACTERSET);
						}
						{
							btnCharacterset = new Button(composite, SWT.NONE);
							btnCharacterset
									.addSelectionListener(new SelectionAdapter() {
										@Override
										public void widgetSelected(
												SelectionEvent e) {
											char[] currentCharacterSet = characterSetText
													.getText().toCharArray();
											char[] newCharacterSet = new char[currentCharacterSet.length];
											List<Character> charBlackList = new ArrayList<Character>();

											for (int i = 0; i < currentCharacterSet.length; i++) {
												char newChar = currentCharacterSet[(int) (Math
														.random() * currentCharacterSet.length)];
												if (charBlackList
														.contains(newChar)) {
													--i;
													continue;
												}
												newCharacterSet[i] = newChar;
												charBlackList.add(newChar);
											}
											characterSetText
													.setText(new String(
															newCharacterSet));
											characterSetText.notifyListeners(
													SWT.FocusOut, new Event());
										}
									});
							btnCharacterset
									.setImage(SWTResourceManager
											.getImage(MainApplication.class,
													"/de/janbusch/jhashpassword/images/16px-Crystal_Clear_action_edit.png"));
						}
						{
							Label lblZwischenablageLeer = new Label(composite,
									SWT.NONE);
							lblZwischenablageLeer.setEnabled(false);
							lblZwischenablageLeer
									.setText(Messages.MainApplication_20);
						}
						new Label(composite, SWT.NONE);
						new Label(composite, SWT.NONE);
						new Label(composite, SWT.NONE);
						{
							cacheCombo = new Combo(composite, SWT.READ_ONLY);
							cacheCombo
									.setToolTipText(Messages.MainApplication_cacheCombo_toolTipText);
							cacheCombo.setEnabled(false);
							cacheCombo.setItems(new String[] {
									Messages.MainApplication_21,
									Messages.MainApplication_22,
									Messages.MainApplication_23 });
							cacheCombo.setLayoutData(new GridData(SWT.LEFT,
									SWT.CENTER, false, false, 3, 1));
							cacheCombo.select(1);
						}
						new Label(composite, SWT.NONE);
					}
					xpndtmSettings.setHeight(200);
				}
			}
		}
		{
			Group grpButtons = new Group(shlJhashpassword, SWT.NONE);
			grpButtons.setLayout(new GridLayout(5, false));
			grpButtons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false,
					false, 1, 1));
			{
				btnSave = new Button(grpButtons, SWT.NONE);
				btnSave.setEnabled(false);
				btnSave.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						MessageBox messageBox = new MessageBox(
								shlJhashpassword, SWT.ICON_WARNING | SWT.YES
										| SWT.NO);
						messageBox.setText(Messages.MainApplication_57);
						messageBox.setMessage(Messages.MainApplication_58);
						int buttonID = messageBox.open();

						switch (buttonID) {
						case SWT.YES:
							try {
								Hosts hosts = hashPassword.getHosts();
								Host currentHost = hosts
										.getHostByName(hostCombo.getText());
								LoginName currentLogin = currentHost
										.getLoginNames().getLoginNameByName(
												loginCombo.getText());
								if (currentHost.getLoginNames().getLoginName()
										.size() > 1) {
									currentLogin.setCharset(characterSetText
											.getText());
									currentLogin.setHashType(hashCombo
											.getText());
									currentLogin
											.setPasswordLength(passwordLengthText
													.getText());
								} else {
									currentHost.setCharset(characterSetText
											.getText());
									currentHost.setHashType(hashCombo.getText());
									currentHost
											.setPasswordLength(passwordLengthText
													.getText());
								}

								saveXMLFile();
								MessageBox mB = new MessageBox(shlJhashpassword);
								mB.setText(Messages.MainApplication_32);
								mB.setMessage(Messages.MainApplication_33);
								mB.open();
							} catch (Exception exc) {
								ErrorDialog.openError(shlJhashpassword,
										Messages.MainApplication_34, Messages.MainApplication_35,
										Status.OK_STATUS);
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
				});
				btnSave.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
						false, 1, 1));
				btnSave.setText(Messages.MainApplication_36);
			}
			{
				Button btnSync = new Button(grpButtons, SWT.NONE);
				btnSync.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							myServer = new JHPServer(false, MainApplication.this);
							myServer.start();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
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
						ClipBoardUtil.addToClipboard("");
						saveXMLFile();
						System.exit(0);
					}
				});
				btnbeenden.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
						true, false, 1, 1));
				btnbeenden.setText(Messages.MainApplication_37);
			}
		}
	}

	@Override
	public void handleMessage(String msg, InetSocketAddress from) {
		ENetCommand command = ENetCommand.parse(msg);

		System.out.println("Received msg from " + from.getAddress()
				+ " with content: " + msg);

		switch (command) {
		case REQ:
			System.out.println("Request received! User " + command.getParam());
			break;
		case ADVERTISEMENT:
			System.out.println("Advertisement received from "
					+ from.getAddress());
			break;
		default:
			System.out.println("Unknown command received.");
		}
	}
}
