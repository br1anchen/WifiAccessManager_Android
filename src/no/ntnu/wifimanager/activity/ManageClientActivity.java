package no.ntnu.wifimanager.activity;
import static no.ntnu.wifimanager.CommonUtilities.LOG_TAG;
import no.ntnu.wifimanager.R;
import no.ntnu.wifimanager.domain.ClientMapper;
import no.ntnu.wifimanager.view.AccessPermissionSelector.AccessPermissionStatus;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

public class ManageClientActivity extends BaseClientActivity {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		String client_json = intent.getStringExtra(ClientListActivity.EXTRA_CLIENT_STRING);

		try {
			client = ClientMapper.toClient(new JSONObject(client_json));
			Log.d(LOG_TAG, "client name :" +client.getName());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		String title = client.getName()+"'s"+" Wifi Permissions";
		actionBar.setTitle(title);
		
		writeClientPermissionsToInterface();
	}
	
	
	private void writeClientPermissionsToInterface(){
		
		if(client.hasAccessPermission()){				

			if(client.getAccessPermission().isAccessAllowed()){
				accessPermSelector.setStatus(AccessPermissionStatus.ACCESS_ALLOW);
			
			}else{
				accessPermSelector.setStatus(AccessPermissionStatus.ACCESS_BLOCK);
			}
		}
	}
}
