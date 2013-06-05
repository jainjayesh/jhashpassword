package de.janbusch.jhashpassword.net.common;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public interface IJHPMsgHandler {
	void handleMessage(ENetCommand command, InetSocketAddress from);

	void handleAction(EActionCommand cmd);

	void handleMessage(SecureMessage recMsgTyped,
			InetAddress remoteSocketAddress);

	boolean isClientAccepted(InetAddress inetAddress);
}
