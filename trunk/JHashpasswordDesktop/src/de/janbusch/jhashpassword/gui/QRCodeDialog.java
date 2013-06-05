package de.janbusch.jhashpassword.gui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import de.janbusch.hashpassword.core.CoreInformation;

public class QRCodeDialog extends Dialog {

	protected Object result;
	protected Shell shlQRCode;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public QRCodeDialog(Shell parent) {
		super(parent, SWT.CENTER | SWT.DIALOG_TRIM);
		setText("QR-Code");
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlQRCode.open();
		shlQRCode.layout();
		shlQRCode.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				File f = new File(CoreInformation.QRCODEFILE);
				f.delete();
			}
		});

		Display display = getParent().getDisplay();
		while (!shlQRCode.isDisposed()) {
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
		shlQRCode = new Shell(getParent(), SWT.CENTER | SWT.DIALOG_TRIM
				| SWT.APPLICATION_MODAL);
		shlQRCode.setText("QR-Code");
		shlQRCode.setSize(310, 315);
		shlQRCode.setLayout(new FillLayout(SWT.HORIZONTAL));

		Label lblNewLabel = new Label(shlQRCode, SWT.NONE);
		lblNewLabel.setAlignment(SWT.CENTER);
		lblNewLabel.setImage(org.eclipse.wb.swt.SWTResourceManager
				.getImage(CoreInformation.QRCODEFILE));
		lblNewLabel.setToolTipText("Scan with your Android device.");
		{
		}
	}
}
