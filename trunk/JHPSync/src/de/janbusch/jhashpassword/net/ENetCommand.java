package de.janbusch.jhashpassword.net;

public enum ENetCommand {
	SOLICITATION("SOLICITATION"), ADVERTISEMENT("ADVERTISEMENT"), REQ("REQ"), ACK(
			"ACK"), PAS_REQ("PAS_REQ"), PAS_REP("PAS_REP"), UNKNOWN("UNKNOWN"), REQ_XML(
			"REQ_XML"), EST_TCP("EST_TCP");

	private String command;
	private String parameter;
	private static String PRE = "<";
	private static String SEP = ":";
	private static String POST = ">";
	private static String ID_TAG = "JHP_";

	/**
	 * Constructor
	 * 
	 * @param path
	 */
	private ENetCommand(String command) {
		this.command = command;
	}

	public void setParameter(String param) {
		this.parameter = param;
	}

	/**
	 * Get the command
	 * 
	 * @return command as String
	 */
	public String value() {
		return command;
	}

	public static ENetCommand parse(String msg) {
		ENetCommand genCommand = null;
		String command = null;
		String parameter = null;

		try {
			command = msg.substring(msg.indexOf(PRE) + 1, msg.indexOf(SEP))
					.replaceFirst(ID_TAG, "");
			genCommand = ENetCommand.valueOf(command);
			parameter = msg.substring(msg.indexOf(SEP) + 1, msg.indexOf(POST));
			genCommand.setParameter(parameter);
		} catch (Exception e1) {
			try {
				command = msg
						.substring(msg.indexOf(PRE) + 1, msg.indexOf(POST))
						.replaceFirst(ID_TAG, "");
				genCommand = ENetCommand.valueOf(command);
			} catch (Exception e2) {
				genCommand = ENetCommand.UNKNOWN;
			}
		}

		return genCommand;
	}

	@Override
	public String toString() {
		if (parameter != null)
			return PRE + ID_TAG + command + SEP + parameter + POST;
		else
			return PRE + ID_TAG + command + POST;
	}

	public String getParam() {
		return parameter;
	}
}
