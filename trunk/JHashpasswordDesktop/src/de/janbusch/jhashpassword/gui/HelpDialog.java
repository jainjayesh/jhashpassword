package de.janbusch.jhashpassword.gui;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.swtdesigner.SWTResourceManager;


public class HelpDialog extends Dialog {

	private static final String HTML_DIR = "html" + File.separatorChar
			+ "hilfe.html";
	protected Object result;
	protected Shell shlHilfe;
	private Browser browser;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public HelpDialog(Shell parent) {
		super(parent, SWT.DIALOG_TRIM);
		setText("Hilfe");
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlHilfe.open();
		shlHilfe.layout();

		Display display = getParent().getDisplay();
		while (!shlHilfe.isDisposed()) {
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
		shlHilfe = new Shell(getParent(), getStyle());
		shlHilfe.setImage(SWTResourceManager.getImage(HelpDialog.class, "/de/janbusch/jhashpassword/images/48px-Crystal_Clear_app_ktip.png"));
		shlHilfe.setSize(550, 350);
		shlHilfe.setText("Hilfe");
		shlHilfe.setLayout(new GridLayout(1, false));
		{
			browser = new Browser(shlHilfe, SWT.NONE);
			browser.setJavascriptEnabled(false);
			browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
					1, 1));
			browser.setUrl(System.getProperty("user.dir")
					+ File.separatorChar + HTML_DIR);
		}
	}
}
