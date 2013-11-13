package no.ntnu.wifimanager.domain.permission;

public class AccessPermission extends WifiPermission{
	
	
	private boolean allowAccess;

	public void isAccessAllowed(boolean allowAccess) {
		this.allowAccess = allowAccess;
	}
	
	public boolean isAccessAllowed(){
		return allowAccess;
	}

}
