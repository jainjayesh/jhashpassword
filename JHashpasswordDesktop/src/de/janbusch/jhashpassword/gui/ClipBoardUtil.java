package de.janbusch.jhashpassword.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class ClipBoardUtil {
	public static void addToClipboard(final String s) {
		Clipboard systemClipboard = Toolkit.getDefaultToolkit()
				.getSystemClipboard();
		Transferable contents = new Transferable() {
			public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException, IOException {
				return s;
			}

			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] { DataFlavor.stringFlavor };
			}

			public boolean isDataFlavorSupported(DataFlavor flavor) {
				for (DataFlavor df : getTransferDataFlavors())
					if (df.equals(flavor))
						return true;
				return false;
			}
		};
		systemClipboard.setContents(contents, null);
	}

	public static String getFromClipboard() {
		String result = null;
		Clipboard systemClipboard = Toolkit.getDefaultToolkit()
				.getSystemClipboard();

		for (DataFlavor dF : systemClipboard.getAvailableDataFlavors()) {
			if (dF.isFlavorTextType()) {
				try {
					Reader r = dF.getReaderForText(systemClipboard
							.getContents(dF));
					BufferedReader bR = new BufferedReader(r);
					result = bR.readLine();
					bR.close();
					r.close();
				} catch (UnsupportedFlavorException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (result == null) {
			return "";
		} else {
			return result;
		}
	}
}
