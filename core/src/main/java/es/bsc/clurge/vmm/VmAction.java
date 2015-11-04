package es.bsc.clurge.vmm;

/**
 * Created by mmacias on 4/11/15.
 */
public enum VmAction {
	REBOOT_HARD("rebootHard"),
	REBOOT_SOFT("rebootSoft"),
	START("start"),
	STOP("stop"),
	SUSPEND("suspend"),
	RESUME("resume");

	private String camelCase;

	VmAction(String camelCase) {
		this.camelCase = camelCase;
	}

	public String getCamelCase() {
		return camelCase;
	}

	public static VmAction fromCamelCase(String camelCase) {
		for(VmAction action : values()) {
			if(action.camelCase.equals(camelCase)) {
				return action;
			}
		}
		throw new EnumConstantNotPresentException(VmAction.class, camelCase);
	}
}
