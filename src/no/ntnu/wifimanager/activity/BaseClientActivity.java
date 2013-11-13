package no.ntnu.wifimanager.activity;

import static no.ntnu.wifimanager.CommonUtilities.HTTP_CONTENT_TYPE_JSON;
import static no.ntnu.wifimanager.CommonUtilities.LOG_TAG;
import static no.ntnu.wifimanager.CommonUtilities.URL_POST_CLIENT_PERMS;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import no.ntnu.wifimanager.R;
import no.ntnu.wifimanager.ServerUtilities;
import no.ntnu.wifimanager.domain.Client;
import no.ntnu.wifimanager.domain.ClientMapper;
import no.ntnu.wifimanager.domain.permission.AccessPermission;
import no.ntnu.wifimanager.view.AccessPermissionSelector;
import no.ntnu.wifimanager.view.AccessPermissionSelector.AccessPermissionStatus;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;


public abstract class BaseClientActivity extends Activity {


	
	protected Client client;
	protected ActionBar actionBar;
	protected TextView allowOrBlockView;
	protected AccessPermissionSelector accessPermSelector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_client_permissions);

		client = new Client();
		actionBar = getActionBar();
		configureViews();
	}

	protected void configureViews() {

		accessPermSelector = (AccessPermissionSelector) findViewById(R.id.access_perm_selector);
		allowOrBlockView = (TextView) findViewById(R.id.allow_or_block_text_view);

		accessPermSelector.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				accessPermSelector.showNextStatus();
			}
		});
	}



	public void confirmPermissions(View view){
		readUserInputToClient();
		String client_json = ClientMapper.toJSONClient(client).toString();
		new PostClientTask().execute(client_json);
	}


	public void cancelPermissions(View view){
		finishActivityWithResult(false);
	}


	private void finishActivityWithResult(boolean clientUpdated) {

		Intent intent = getIntent();

		if(clientUpdated){
			
			setResult(ClientListActivity.RESULT_CLIENT_UPDATE, intent);
	
		}else{
			setResult(ClientListActivity.RESULT_NO_CLIENT_UPDATE, intent);
		}
		
		super.finish();
	}

	
	private void readUserInputToClient() {
		readAccessPermissionInput();
	}

	private void readAccessPermissionInput() {

		AccessPermissionStatus status = accessPermSelector.getStatus();
		AccessPermission accssPerm = new AccessPermission();

		switch (status) {
		case ACCESS_ALLOW:
			accssPerm.isAccessAllowed(true);
			client.setAccessPermission(accssPerm);
			break;

		case ACCESS_BLOCK:
			accssPerm.isAccessAllowed(false);
			client.setAccessPermission(accssPerm);
			break;
		}
	}


	protected class PostClientTask  extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			
				String clientJSON = params[0];
				Map<String, String> postParams = new HashMap<String, String>();
				postParams.put("client_json", clientJSON);
				ServerUtilities.postJSON(URL_POST_CLIENT_PERMS, clientJSON);
				Log.d(LOG_TAG, "client sent to server: "+ params[0]);
			
				return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			BaseClientActivity.this.finishActivityWithResult(true);
		}
	}

}
