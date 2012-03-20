package de.janbusch.jhashpassword.net.common;

import java.net.InetSocketAddress;

public interface IJHPMsgHandler {
	void handleMessage(ENetCommand command, InetSocketAddress from);
	void handleAction(EActionCommand cmd);
}
