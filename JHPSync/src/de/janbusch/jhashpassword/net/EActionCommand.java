package de.janbusch.jhashpassword.net;

public enum EActionCommand {
	SOLICITATION_LEFT("SOLICITATION_LEFT"), SOLICITATION_END("SOLICITATION_END"), UNKNOWN("UNKNOWN");

	private String command;
	private Object parameter;

	/**
	 * Constructor
	 * 
	 * @param path
	 */
	private EActionCommand(String command) {
		this.command = command;
	}

	public void setParameter(Object param) {
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

	@Override
	public String toString() {
		return command;
	}

	public Object getParam() {
		return parameter;
	}
}
