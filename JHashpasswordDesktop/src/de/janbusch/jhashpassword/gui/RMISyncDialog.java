package de.janbusch.jhashpassword.gui;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.wb.swt.SWTResourceManager;

import de.janbusch.jhashpassword.net.client.JHPRMIClient;
import de.janbusch.jhashpassword.net.common.EActionCommand;
import de.janbusch.jhashpassword.net.common.ENetCommand;
import de.janbusch.jhashpassword.net.common.IJHPMsgHandler;
import de.janbusch.jhashpassword.net.common.Partner;
import de.janbusch.jhashpassword.net.common.Util;
import de.janbusch.jhashpassword.net.server.JHPRMIServer.IJHPServer;

public class RMISyncDialog extends Dialog implements IJHPMsgHandler {

	private JHPRMIClient myClient;
	private IJHPServer myRemoteHost;
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
	private MessageDialog connectionMsg;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public RMISyncDialog(Shell parent, int style) {
		super(parent, style | SWT.CLOSE | SWT.APPLICATION_MODAL);
		setText("Synchronisation");
		availablePartners = new HashMap<String, Partner>();
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
				if (myRemoteHost != null)
					myRemoteHost.disconnect();

				if (myClient != null)
					myClient.killClient();
			}
		});

		try {
			myClient = new JHPRMIClient(this,
					Util.getBroadcastAddress(InetAddress.getLocalHost()),
					Util.getMacAddress(InetAddress.getLocalHost()),
					Util.getOperatingSystem());
			myClient.startSolicitation();
		} catch (SocketException e) {
			messageBox("Error", e.getMessage());
		} catch (UnknownHostException e) {
			messageBox("Error", e.getMessage());
		} catch (IOException e) {
			messageBox("Error", e.getMessage());
		}

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
				.getImage(RMISyncDialog.class,
						"/de/janbusch/jhashpassword/images/Crystal_Clear_app_kblackbox.png"));

		Label label = new Label(grpConnection, SWT.NONE);
		label.setText("----");

		Label imgArrow = new Label(grpConnection, SWT.NONE);
		imgArrow.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false,
				1, 1));
		imgArrow.setImage(com.swtdesigner.SWTResourceManager
				.getImage(RMISyncDialog.class,
						"/de/janbusch/jhashpassword/images/Crystal_Clear_app_package_network.png"));

		Label label_1 = new Label(grpConnection, SWT.NONE);
		label_1.setText("----");

		Label imgOtherOS = new Label(grpConnection, SWT.NONE);
		imgOtherOS.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false,
				1, 1));
		imgOtherOS
				.setImage(com.swtdesigner.SWTResourceManager
						.getImage(RMISyncDialog.class,
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

		TabFolder tabFolder = new TabFolder(group, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));

		TabItem tbtmConnection = new TabItem(tabFolder, SWT.NONE);
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

		TabItem tbtmSynchronisation = new TabItem(tabFolder, SWT.NONE);
		tbtmSynchronisation.setText("Synchronisation");

		Composite composite = new Composite(tabFolder, SWT.NONE);
		tbtmSynchronisation.setControl(composite);
		composite.setLayout(new GridLayout(3, false));

		Group grpYourSettings = new Group(composite, SWT.NONE);
		grpYourSettings.setText("Your Settings");
		grpYourSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));
		grpYourSettings.setSize(332, 196);

		Group grpControls = new Group(composite, SWT.NONE);
		grpControls.setText("Controls");
		grpControls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));

		Group grpRemoteSettings = new Group(composite, SWT.NONE);
		grpRemoteSettings.setText("Remote Settings");
		grpRemoteSettings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));

		String os = Util.getOperatingSystem();

		if (os.contains("Windows")) {
			imgMyOS.setImage(SWTResourceManager.getImage(RMISyncDialog.class,
					"/de/janbusch/jhashpassword/images/windows-logo.png"));
		} else if (os.contains("Linux")) {
			imgMyOS.setImage(SWTResourceManager.getImage(RMISyncDialog.class,
					"/de/janbusch/jhashpassword/images/Linux_tux.png"));
		}
	}

	private void messageBox(final String text, final String message) {
		shlSynchronisation.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageBox mB = new MessageBox(shlSynchronisation);
				mB.setText(text);
				mB.setMessage(message);
				mB.open();
			}
		});
	}

	private MessageDialog messageDialog(String text, String message, int img,
			String[] buttonLabels, int defIndex, final Runnable[] actions) {
		final MessageDialog mD = new MessageDialog(shlSynchronisation, text,
				null, message, img, buttonLabels, defIndex);
		shlSynchronisation.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				int idx = mD.open();

				if (actions != null && idx < actions.length) {
					new Thread(actions[idx]).start();
				}
			}
		});
		return mD;
	}

	private int closeMessageDialog(final MessageDialog mD) {
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

		connectionMsg = messageDialog("Connecting", "Connecting to client...",
				MessageDialog.INFORMATION, new String[] { "Cancel" }, 0, null);

		myClient.stopSolicitation();
		progressBar.setState(SWT.PAUSED);
		btnAutorefresh.setSelection(false);

		Partner[] p = availablePartners.values().toArray(
				new Partner[availablePartners.size()]);

		try {
			myRemoteHost = myClient.GetServerConnection(p[index].getAddress()
					.toString());
			closeMessageDialog(connectionMsg);
			myRemoteHost.authenticate("jan", "1234");
			messageBox("Success", "Connection established, authenticated!");
		} catch (Exception e) {
			messageBox("Error", e.getMessage());
		}
	}

	@Override
	public void handleMessage(ENetCommand command, InetSocketAddress from) {
		System.out.println("Received msg from " + from.getAddress()
				+ " with content: " + command);

		switch (command) {
		case ADVERTISEMENT:
			String[] params = command.getParam().split("[|]");
			Partner p = new Partner(from, params[0], params[1]);

			if (!availablePartners.containsKey(from.getHostName())) {
				availablePartners.put(from.getHostName(), p);
			}
			break;
		default:
			System.out.println("Unknown command received.");
		}

		updateUI();
	}

	@Override
	public void handleAction(EActionCommand cmd) {
		// TODO Auto-generated method stub

	}
}
