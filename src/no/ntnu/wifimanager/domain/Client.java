package no.ntnu.wifimanager.domain;

import static no.ntnu.wifimanager.domain.permission.WifiPermission.PKEY_ACCESS;

import java.util.HashMap;

import no.ntnu.wifimanager.domain.permission.AccessPermission;
import no.ntnu.wifimanager.domain.permission.WifiPermission;


public class Client {


	private String name;
	private String mac;
	private HashMap<String, WifiPermission> permissions;
	
	public Client() {
		permissions = new HashMap<String, WifiPermission>();
	}

	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMac() {
		return mac;
	}
	
	
	public void setMac(String mac) {
		this.mac = mac;
	}

	
	public boolean hasPermissons() {
		if(permissions.size() > 0){
			return true;
		}
		return false;
	}

	
	public AccessPermission getAccessPermission() {
		return (AccessPermission) permissions.get(PKEY_ACCESS);
	}
	
	public void setAccessPermission(AccessPermission perm){
		permissions.put(PKEY_ACCESS, perm);
	}

	public boolean hasAccessPermission() {
		if(permissions.containsKey(PKEY_ACCESS)){
			return true;
		}
		return false;
	}

	public void removeAccessPermission() {
		permissions.remove(PKEY_ACCESS);
	}

}
