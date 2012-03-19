package de.janbusch.jhashpassword.net;

import java.net.InetSocketAddress;

public interface IJHPMsgHandler {
	void handleMessage(String msg, InetSocketAddress from);
	void handleAction(EActionCommand cmd);
}
