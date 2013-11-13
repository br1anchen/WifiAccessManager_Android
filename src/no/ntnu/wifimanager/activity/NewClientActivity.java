package no.ntnu.wifimanager.activity;

import no.ntnu.wifimanager.GCMIntentService;
import no.ntnu.wifimanager.view.AccessPermissionSelector.AccessPermissionStatus;
import android.content.Intent;
import android.os.Bundle;

public class NewClientActivity extends BaseClientActivity {
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		client.setName(intent.getStringExtra(GCMIntentService.EXTRA_CLIENT_NAME));
		client.setMac(intent.getStringExtra(GCMIntentService.EXTRA_CLIENT_MAC));
		String title = "Grant "+client.getName()+ " Wifi Permissions";
		actionBar.setTitle(title);
		
		configureViews();
		//Default value for new clients 
		accessPermSelector.setStatus(AccessPermissionStatus.ACCESS_ALLOW);
	}
}
