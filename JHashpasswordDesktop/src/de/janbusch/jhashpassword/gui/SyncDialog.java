package de.janbusch.jhashpassword.gui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.IllegalSelectorException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

import de.janbusch.jhashpassword.net.client.JHPClient;
import de.janbusch.jhashpassword.net.common.EActionCommand;
import de.janbusch.jhashpassword.net.common.ENetCommand;
import de.janbusch.jhashpassword.net.common.IJHPMsgHandler;
import de.janbusch.jhashpassword.net.common.Partner;
import de.janbusch.jhashpassword.net.common.SecureMessage;
import de.janbusch.jhashpassword.net.common.SecureMessage.MessageType;
import de.janbusch.jhashpassword.net.common.Util;
import de.janbusch.jhashpassword.net.server.JHPServer;
import de.janbusch.jhashpassword.xml.SimpleXMLUtil;
import de.janbusch.jhashpassword.xml.simple.data.HashPassword;
import de.janbusch.jhashpassword.xml.simple.data.Host;
import de.janbusch.jhashpassword.xml.simple.data.Hosts;
import de.janbusch.jhashpassword.xml.simple.data.LoginName;

public class SyncDialog extends Dialog implements IJHPMsgHandler {

	private JHPClient myClient;
	private Object result;
	private Shell shlSynchronisation;
	private Map<String, Partner> availablePartners;
	private Table tblAvailableClients;
	private TableColumn tblclmnIpaddress;
	private TableColumn tblclmnMacaddress;
	private TableColumn tblclmnOperatingSystem;
	private Label imgMyOS;
	private ProgressBar progressBar;
	private Button btnAutorefresh;
	private MessageDialog messageDialog;
	private MessageBox mB;
	private TabItem tbtmConnection;
	private TabItem tbtmSynchronisation;
	private TabFolder tabFolder;
	private Tree treeLocal;
	private Tree treeRemote;
	private HashPassword remoteHashPassword;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public SyncDialog(Shell parent, int style) {
		super(parent, style | SWT.CLOSE | SWT.APPLICATION_MODAL);
		setText("Synchronisation");

		try {
			availablePartners = new HashMap<String, Partner>();
			myClient = new JHPClient(SyncDialog.this,
					Util.getBroadcastAddress(InetAddress.getLocalHost()),
					Util.getMacAddress(InetAddress.getLocalHost()),
					Util.getOperatingSystem());
			myClient.start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlSynchronisation.open();
		shlSynchronisation.layout();
		shlSynchronisation.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				myClient.killServer();
			}
		});

		Display display = getParent().getDisplay();
		while (!shlSynchronisation.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlSynchronisation = new Shell(getParent(), getStyle());
		shlSynchronisation.setSize(764, 507);
		shlSynchronisation.setText("Synchronisation");
		shlSynchronisation.setLayout(new GridLayout(3, false));

		Group grpConnection = new Group(shlSynchronisation, SWT.NONE);
		grpConnection.setText("Connection");
		grpConnection.setLayout(new GridLayout(5, false));
		grpConnection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 1, 2));

		imgMyOS = new Label(grpConnection, SWT.NONE);
		imgMyOS.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1,
				1));
		imgMyOS.setImage(SWTResourceManager
				.getImage(SyncDialog.class,
						"/de/janbusch/jhashpassword/images/Crystal_Clear_app_kblackbox.png"));

		Label label = new Label(grpConnection, SWT.NONE);
		label.setText("----");

		Label imgArrow = new Label(grpConnection, SWT.NONE);
		imgArrow.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false,
				1, 1));
		imgArrow.setImage(com.swtdesigner.SWTResourceManager
				.getImage(SyncDialog.class,
						"/de/janbusch/jhashpassword/images/Crystal_Clear_app_package_network.png"));

		Label label_1 = new Label(grpConnection, SWT.NONE);
		label_1.setText("----");

		Label imgOtherOS = new Label(grpConnection, SWT.NONE);
		imgOtherOS.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false,
				1, 1));
		imgOtherOS
				.setImage(com.swtdesigner.SWTResourceManager
						.getImage(SyncDialog.class,
								"/de/janbusch/jhashpassword/images/Crystal_Clear_app_kblackbox.png"));

		Group grpStatus = new Group(shlSynchronisation, SWT.NONE);
		grpStatus.setText("Status");
		grpStatus.setLayout(new GridLayout(2, false));
		grpStatus.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				2, 2));

		Label lblState = new Label(grpStatus, SWT.NONE);
		lblState.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false,
				1, 1));
		lblState.setText("State:");

		Label lblConnection = new Label(grpStatus, SWT.NONE);
		lblConnection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		lblConnection.setText("No connection.");

		Composite group = new Composite(shlSynchronisation, SWT.NONE);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

		tabFolder = new TabFolder(group, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));

		tbtmConnection = new TabItem(tabFolder, SWT.NONE);
		tbtmConnection.setText("Connection");

		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		tbtmConnection.setControl(composite_1);
		composite_1.setLayout(new GridLayout(2, false));

		Group grpAvailableSyncpartners = new Group(composite_1, SWT.NONE);
		grpAvailableSyncpartners.setText("Available Syncpartners");
		grpAvailableSyncpartners.setLayout(new GridLayout(2, false));
		grpAvailableSyncpartners.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true, 1, 1));

		tblAvailableClients = new Table(grpAvailableSyncpartners, SWT.BORDER
				| SWT.FULL_SELECTION);
		tblAvailableClients.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true, 2, 1));
		tblAvailableClients.setHeaderVisible(true);
		tblAvailableClients.setLinesVisible(true);
		tblAvailableClients.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
				sendConnectionReq();
			}

			public void widgetSelected(SelectionEvent arg0) {
			}
		});

		tblclmnIpaddress = new TableColumn(tblAvailableClients, SWT.NONE);
		tblclmnIpaddress.setWidth(176);
		tblclmnIpaddress.setText("IP-Address");

		tblclmnMacaddress = new TableColumn(tblAvailableClients, SWT.NONE);
		tblclmnMacaddress.setWidth(176);
		tblclmnMacaddress.setText("MAC-Address");

		tblclmnOperatingSystem = new TableColumn(tblAvailableClients, SWT.NONE);
		tblclmnOperatingSystem.setWidth(231);
		tblclmnOperatingSystem.setText("Operating System");

		progressBar = new ProgressBar(grpAvailableSyncpartners,
				SWT.INDETERMINATE);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 2, 1));

		Group grpControls_1 = new Group(composite_1, SWT.NONE);
		grpControls_1.setText("Controls");
		grpControls_1.setLayout(new GridLayout(1, false));
		grpControls_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				true, 1, 1));

		btnAutorefresh = new Button(grpControls_1, SWT.TOGGLE);
		btnAutorefresh.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		btnAutorefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Object sender = arg0.getSource();

				if (sender instanceof Button) {
					Button toggle = (Button) sender;

					if (toggle.getSelection()) {
						myClient.startSolicitation();
						progressBar.setState(SWT.NORMAL);
					} else {
						myClient.stopSolicitation();
						progressBar.setState(SWT.PAUSED);
					}
				}
			}
		});
		btnAutorefresh
				.setToolTipText("Autosearch for JHashPassword in sync mode on Android devices or other computers.");
		btnAutorefresh.setSelection(true);
		btnAutorefresh.setText("Autorefresh");

		Button btnConnect = new Button(grpControls_1, SWT.NONE);
		btnConnect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				sendConnectionReq();
			}
		});
		btnConnect.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false,
				1, 1));
		btnConnect.setText("&Connect");

		tbtmSynchronisation = new TabItem(tabFolder, SWT.NONE);
		tbtmSynchronisation.setText("Synchronisation");

		Composite composite = new Composite(tabFolder, SWT.NONE);
		tbtmSynchronisation.setControl(composite);
		composite.setLayout(new GridLayout(3, false));

		Group grpYourSettings = new Group(composite, SWT.NONE);
		grpYourSettings.setText("Your Settings");
		grpYourSettings.setLayout(new FillLayout(SWT.HORIZONTAL));
		grpYourSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));
		grpYourSettings.setSize(332, 196);

		TreeViewer treeViewer = new TreeViewer(grpYourSettings, SWT.BORDER);
		treeLocal = treeViewer.getTree();

		Group grpControls = new Group(composite, SWT.NONE);
		grpControls.setText("Controls");
		grpControls.setLayout(new FillLayout(SWT.VERTICAL));
		grpControls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true,
				1, 1));

		Button btnLoadReset = new Button(grpControls, SWT.NONE);
		btnLoadReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				LoadLocalTree();
				LoadRemoteTree();
			}
		});
		btnLoadReset.setText("Load/Reset");

		Button btnSingleLR = new Button(grpControls, SWT.NONE);
		btnSingleLR.setText(">");

		Button btnSingleRL = new Button(grpControls, SWT.NONE);
		btnSingleRL.setText("<");

		Button btnAllLR = new Button(grpControls, SWT.NONE);
		btnAllLR.setText(">>");

		Button btnAllRL = new Button(grpControls, SWT.NONE);
		btnAllRL.setText("<<");

		Group grpRemoteSettings = new Group(composite, SWT.NONE);
		grpRemoteSettings.setText("Remote Settings");
		grpRemoteSettings.setLayout(new FillLayout(SWT.HORIZONTAL));
		grpRemoteSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));

		TreeViewer treeViewer_1 = new TreeViewer(grpRemoteSettings, SWT.BORDER);
		treeRemote = treeViewer_1.getTree();

		String os = Util.getOperatingSystem();

		if (os.contains("Windows")) {
			imgMyOS.setImage(SWTResourceManager.getImage(SyncDialog.class,
					"/de/janbusch/jhashpassword/images/windows-logo.png"));
		} else if (os.contains("Linux")) {
			imgMyOS.setImage(SWTResourceManager.getImage(SyncDialog.class,
					"/de/janbusch/jhashpassword/images/Linux_tux.png"));
		}
	}

	protected void LoadRemoteTree() {
	}

	protected void LoadLocalTree() {
		try {
			HashPassword hashPassword = SimpleXMLUtil.getXML();

			BuildTree(hashPassword, treeLocal);
		} catch (Exception e1) {
			MessageBox messageBox = new MessageBox(shlSynchronisation,
					SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("JHashPassword"); //$NON-NLS-1$
			messageBox.setMessage("Could not load XML-File");
			messageBox.open();
		}

	}

	private void BuildTree(HashPassword hashPassword, Tree tree) {
		tree.clearAll(true);

		for (Host host : hashPassword.getHosts().getHost()) {
			TreeItem itemHost = new TreeItem(tree, 0);
			itemHost.setText(host.getName());
			for (LoginName login : host.getLoginNames().getLoginName()) {
				TreeItem itemLogin = new TreeItem(itemHost, 0);
				itemLogin.setText(login.getName());

				TreeItem itemHashType = new TreeItem(itemLogin, 0);
				itemHashType.setText(login.getHashType());

				TreeItem itemCH = new TreeItem(itemLogin, 0);
				itemCH.setText(login.getCharset());

				TreeItem itemPWLength = new TreeItem(itemLogin, 0);
				itemPWLength.setText(login.getPasswordLength());
			}
		}
	}

	@Override
	public void handleMessage(ENetCommand command, final InetSocketAddress from) {

		System.out.println("Received msg from " + from.getAddress()
				+ " with content: " + command);

		switch (command) {
		case ACK:
			System.out.println("Ack received from " + from.getAddress());
			ENetCommand pair = ENetCommand.REQ_PAIR;

			String pariCode = "";
			for (int i = 0; i < 4; i++) {
				pariCode += ((int) (Math.random() * 9)) + "";
			}

			pair.setParameter(pariCode);

			closeMessageDialog(messageDialog);
			messageDialog = messageDialog("Pairing", "Paringcode: " + pariCode
					+ "?", MessageDialog.INFORMATION, new String[] { "OK" }, 0);
			myClient.sendMessage(from, pair.toString());
			break;
		case REF:
			closeMessageDialog(messageDialog);
			messageDialog = messageDialog("Connection",
					"The other device refused the request.",
					MessageDialog.INFORMATION, new String[] { "OK" }, 0);
			break;
		case ADVERTISEMENT:
			String[] params = command.getParam().split("[|]");
			Partner p = new Partner(from, params[0], params[1]);

			if (!availablePartners.containsKey(from.getHostName())) {
				availablePartners.put(from.getHostName(), p);
			}
			break;
		case ACK_PAIR:
			System.out.println("Ackpair received from " + from.getAddress());
			closeMessageDialog(messageDialog);
			new Thread(new Runnable() {
				@Override
				public void run() {
					myClient.connectToServer(from.getHostName(),
							JHPServer.SERVER_PORT_TCP);
				}
			}).start();
			
			shlSynchronisation.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					tabFolder.setSelection(tbtmSynchronisation);
				}
			});
			
			myClient.sendSecureMessage(from, new SecureMessage(
					MessageType.REQ_HASHPASSWORD_XML));
			break;
		default:
			System.out.println("Unknown command received.");
		}

		updateUI();
	}

	@Override
	public void handleMessage(SecureMessage recMsg, InetAddress remoteAddress) {
		switch (recMsg.getType()) {
		case REC_HASHPASSWORD_XML:
			System.out.println("Received HashPassword.xml from remote.");
			this.remoteHashPassword = (HashPassword) recMsg.getPayload();
			BuildTree(remoteHashPassword, treeRemote);
			break;
		default:
			break;
		}

		updateUI();
	}

	private MessageDialog messageDialog(String text, String message, int img,
			String[] buttonLabels, int defIndex) {
		final MessageDialog mD = new MessageDialog(shlSynchronisation, text,
				null, message, img, buttonLabels, defIndex);
		shlSynchronisation.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				mD.open();
			}
		});
		return mD;
	}

	private int closeMessageDialog(final org.eclipse.jface.dialogs.Dialog mD) {
		shlSynchronisation.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				mD.close();
			}
		});
		return mD.getReturnCode();
	}

	private void updateUI() {
		shlSynchronisation.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				rebuildTable();
			}

			private void rebuildTable() {
				int index = tblAvailableClients.getSelectionIndex();
				Partner[] addresses = availablePartners.values().toArray(
						new Partner[availablePartners.values().size()]);
				final TableItem[] items = new TableItem[availablePartners
						.size()];

				tblAvailableClients.removeAll();

				for (int i = 0; i < items.length; i++) {
					items[i] = new TableItem(tblAvailableClients, SWT.NONE);
					items[i].setText(new String[] {
							addresses[i].getAddress().getHostName(),
							addresses[i].getMacAddress(),
							addresses[i].getOperatingSystem() });
				}
				tblAvailableClients.setSelection(index);
			}
		});
	}

	private void sendConnectionReq() {
		int index = tblAvailableClients.getSelectionIndex();
		if (index == -1)
			return;

		messageDialog = messageDialog("Connecting", "Connecting to client...",
				MessageDialog.INFORMATION, new String[] { "Cancel" }, 0);

		myClient.startSolicitation();
		progressBar.setState(SWT.PAUSED);
		btnAutorefresh.setSelection(false);

		ENetCommand command = ENetCommand.REQ;
		try {
			command.setParameter(Util.getMacAddress(InetAddress.getLocalHost()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		Partner[] p = availablePartners.values().toArray(
				new Partner[availablePartners.size()]);

		myClient.sendMessage(p[index].getAddress(), command.toString());
	}

	@Override
	public void handleAction(EActionCommand cmd) {
		throw new IllegalSelectorException();
	}

	@Override
	public boolean isClientAccepted(InetAddress inetAddress) {
		throw new IllegalSelectorException();
	}
}
