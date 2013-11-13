package no.ntnu.wifimanager.activity;

import static no.ntnu.wifimanager.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static no.ntnu.wifimanager.CommonUtilities.EXTRA_MESSAGE;
import static no.ntnu.wifimanager.CommonUtilities.LOG_TAG;
import static no.ntnu.wifimanager.CommonUtilities.SENDER_ID;
import static no.ntnu.wifimanager.CommonUtilities.URL_SERVER;
import static no.ntnu.wifimanager.CommonUtilities.URL_GET_CLIENTS;
import no.ntnu.wifimanager.GCMIntentService;
import no.ntnu.wifimanager.R;
import no.ntnu.wifimanager.ServerUtilities;
import no.ntnu.wifimanager.adpater.ClientListAdapter;
import no.ntnu.wifimanager.domain.Client;
import no.ntnu.wifimanager.domain.ClientMapper;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gcm.GCMRegistrar;

public class ClientListActivity extends Activity{

	private AsyncTask<Void, Void, Void> mRegisterTask;
	private ListView clientListView;
	private ClientListAdapter clientListAdapter;

	public static final String EXTRA_CLIENT_STRING = "no.ntnu.wifimanager.CLIENT_STRING";
	public static final int RESULT_CLIENT_UPDATE = 1;
	public static final int RESULT_NO_CLIENT_UPDATE = 2;
	
	private SharedPreferences preferences;
	private String userId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this); 
		userId = preferences.getString("email", "NA");
		
		if(userId =="NA"){
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
		}
		
		setContentView(R.layout.activity_client_list);

		configureGCM();
		clientListView = (ListView) findViewById(R.id.client_list_view);
		clientListAdapter = new ClientListAdapter(this);
		clientListView.setAdapter(clientListAdapter);
		
		clientListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos ,
					long id) {
					
				Client client = clientListAdapter.getItem(pos);
				
				if(client.hasPermissons()){
					Intent intent = new Intent(ClientListActivity.this, ManageClientActivity.class);
					String client_json = ClientMapper.toJSONClient(client).toString();
					intent.putExtra(EXTRA_CLIENT_STRING, client_json);
					startActivityForResult(intent, 0);
					
				}else{
					Intent intent = new Intent(ClientListActivity.this, NewClientActivity.class);
					intent.putExtra(GCMIntentService.EXTRA_CLIENT_NAME, client.getName());
					intent.putExtra(GCMIntentService.EXTRA_CLIENT_MAC, client.getMac());
					startActivityForResult(intent, 0);
				}
				
			}
		});
		
		new GetClientsTask().execute(URL_GET_CLIENTS, userId);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (resultCode) {
		case RESULT_CLIENT_UPDATE:
			
			clientListAdapter.clear();
			new GetClientsTask().execute(URL_GET_CLIENTS, userId);
			break;

		default:
			break;
		}
	}

	private void configureGCM() {
		checkNotNull(URL_SERVER, "SERVER_URL");
		checkNotNull(SENDER_ID, "SENDER_ID");
		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);
		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
		GCMRegistrar.checkManifest(this);
		registerReceiver(mHandleMessageReceiver,
				new IntentFilter(DISPLAY_MESSAGE_ACTION));
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			// Automatically registers application on startup.
			GCMRegistrar.register(this, SENDER_ID);
		} else {
			// Device is already registered on GCM, check server.
			if (GCMRegistrar.isRegisteredOnServer(this)) {
				// Skips registration.
				Log.i(LOG_TAG, getString(R.string.already_registered));
			} else {
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.
				final Context context = this;
				mRegisterTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						boolean registered =
								ServerUtilities.register(context, regId);
						// At this point all attempts to register with the app
						// server failed, so we need to unregister the device
						// from GCM - the app will try to register again when
						// it is restarted. Note that GCM will send an
						// unregistered callback upon completion, but
						// GCMIntentService.onUnregistered() will ignore it.
						if (!registered) {
							GCMRegistrar.unregister(context);
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						mRegisterTask = null;
					}

				};
				mRegisterTask.execute(null, null, null);
			}
		}
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		
		case R.id.options_update:
			clientListAdapter.clear();
			new GetClientsTask().execute(URL_GET_CLIENTS, userId);
			return true;
			
		case R.id.options_change_user:
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			Editor edit = preferences.edit();
			edit.remove("email");
			edit.commit(); 
			finish();
			return true;
			
		case R.id.options_exit:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	
	@Override
	protected void onDestroy() {
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}
		unregisterReceiver(mHandleMessageReceiver);
		GCMRegistrar.onDestroy(this);
		super.onDestroy();
	}

	
	private void checkNotNull(Object reference, String name) {
		if (reference == null) {
			throw new NullPointerException(
					getString(R.string.error_config, name));
		}
	}

	
	private final BroadcastReceiver mHandleMessageReceiver =
			new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
			Log.i(LOG_TAG, newMessage);
		}
	};
	
	
	private class GetClientsTask extends AsyncTask<String, Void, JSONArray> {


		@Override
		protected JSONArray doInBackground(String... params) {
			
			Log.i(LOG_TAG, "Requested Clients from Server");
			JSONArray clients = ServerUtilities.getJSONArray(URL_GET_CLIENTS, userId);
			Log.d(LOG_TAG, "clients json from server: "+clients.toString());
			
			return clients;
			
		}
		
		@Override
		protected void onPostExecute(JSONArray result) {
			super.onPostExecute(result);
			for(int i=0;i <result.length(); i++){
				try {
					Client client = ClientMapper.toClient(result.getJSONObject(i));
					clientListAdapter.add(client);
					
				} catch (JSONException e) {
					e.printStackTrace();
				}	
			}
		}
	}
}



