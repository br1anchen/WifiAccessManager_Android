package no.ntnu.wifimanager.domain;

import no.ntnu.wifimanager.domain.permission.AccessPermission;

import org.json.JSONException;
import org.json.JSONObject;

public class ClientMapper {

	private static final String TAG_CLIENT = "client";
	private static final String TAG_CLIENT_INFO = "client_info";
	private static final String TAG_PERMISSIONS = "permissions";
	private static final String TAG_TIME_OF_DAY = "timeofday";
	private static final String TAG_ACCESS = "access";


	private static final String TAG_NAME = "name";
	private static final String TAG_MAC = "mac";
	private static final String TAG_ALLOW = "allow";
	private static final String TAG_DAY = "day";
	private static final String TAG_START = "start";
	private static final String TAG_END = "end";

	public static Client toClient(JSONObject json){
		Client client = new Client();


		try {
			JSONObject jClient = json.getJSONObject(TAG_CLIENT);
			JSONObject jClientInfo = jClient.getJSONObject(TAG_CLIENT_INFO);

			//	JSONArray jTimeOfDay_perm = jPermissons.getJSONArray(TAG_TIME_OF_DAY);
			if(jClient.has(TAG_PERMISSIONS)){

				JSONObject jPermissons = jClient.getJSONObject(TAG_PERMISSIONS);
				JSONObject jAccess_perm = jPermissons.getJSONObject(TAG_ACCESS);

				boolean allowAccess = jAccess_perm.getBoolean(TAG_ALLOW);
				
				AccessPermission allowPerm = new AccessPermission();
				allowPerm.isAccessAllowed(allowAccess);
				client.setAccessPermission(allowPerm);
			}	
			
			String mac = jClientInfo.getString(TAG_MAC);
			String name = jClientInfo.getString(TAG_NAME);


			client.setName(name);
			client.setMac(mac);


		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return client;

	}

	public static JSONObject toJSONClient(Client client){
		JSONObject json = new JSONObject();


		try {

			JSONObject jClient = new JSONObject();
			json.put(TAG_CLIENT, jClient);
			JSONObject jClient_info = new JSONObject();
			jClient.put(TAG_CLIENT_INFO, jClient_info);

			jClient_info.put(TAG_MAC, client.getMac());
			jClient_info.put(TAG_NAME, client.getName());

			if(client.hasPermissons()){

				JSONObject jPermissons = new JSONObject();
				jClient.put(TAG_PERMISSIONS, jPermissons);

				if(client.hasAccessPermission()){
					AccessPermission accessPerm = client.getAccessPermission();
					JSONObject jAccess_perm = new JSONObject();
					jAccess_perm.put(TAG_ALLOW, accessPerm.isAccessAllowed());
					jPermissons.put(TAG_ACCESS, jAccess_perm);
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;

	}
}
