/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.ntnu.wifimanager;

import static no.ntnu.wifimanager.CommonUtilities.HTTP_CONTENT_TYPE_URL_ENCODED;
import static no.ntnu.wifimanager.CommonUtilities.LOG_TAG;
import static no.ntnu.wifimanager.CommonUtilities.URL_REGISTER_APP_FOR_GCM;
import static no.ntnu.wifimanager.CommonUtilities.URL_SERVER;
import static no.ntnu.wifimanager.CommonUtilities.displayMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;

/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {

	private static final int MAX_ATTEMPTS = 5;
	private static final int BACKOFF_MILLI_SECONDS = 2000;
	private static final Random random = new Random();

	public static final String EXTRA_CLIENT_NAME = "no.ntnu.wifimanager.CLIENT_NAME";
	public static final String EXTRA_CLIENT_MAC = "no.ntnu.wifimanager.CLIENT_MAC";

	/**
	 * Register this account/device pair within the server.
	 *
	 * @return whether the registration succeeded or not.
	 */
	public static boolean register(final Context context, final String regId) {
		Log.i(LOG_TAG, "registering device (regId = " + regId + ")");

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context); 
		String userId = preferences.getString("email", "NA");


		Map<String, String> params = new HashMap<String, String>();
		params.put("regId", regId);
		params.put("userId", userId);
		long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
		// Once GCM returns a registration id, we need to register it in the
		// demo server. As the server might be down, we will retry it a couple
		// times.
		for (int i = 1; i <= MAX_ATTEMPTS; i++) {
			Log.d(LOG_TAG, "Attempt #" + i + " to register");
			try {
				displayMessage(context, context.getString(
						R.string.server_registering, i, MAX_ATTEMPTS));
				HTTPpost(URL_REGISTER_APP_FOR_GCM, params, HTTP_CONTENT_TYPE_URL_ENCODED);
				GCMRegistrar.setRegisteredOnServer(context, true);
				String message = context.getString(R.string.server_registered);
				CommonUtilities.displayMessage(context, message);
				return true;
			} catch (IOException e) {
				// Here we are simplifying and retrying on any error; in a real
				// application, it should retry only on unrecoverable errors
				// (like HTTP error code 503).
				Log.e(LOG_TAG, "Failed to register on attempt " + i, e);
				if (i == MAX_ATTEMPTS) {
					break;
				}
				try {
					Log.d(LOG_TAG, "Sleeping for " + backoff + " ms before retry");
					Thread.sleep(backoff);
				} catch (InterruptedException e1) {
					// Activity finished before we complete - exit.
					Log.d(LOG_TAG, "Thread interrupted: abort remaining retries!");
					Thread.currentThread().interrupt();
					return false;
				}
				// increase backoff exponentially
				backoff *= 2;
			}
		}
		String message = context.getString(R.string.server_register_error,
				MAX_ATTEMPTS);
		CommonUtilities.displayMessage(context, message);
		return false;
	}

	/**
	 * Unregister this account/device pair within the server.
	 */
	public static void unregister(final Context context, final String regId) {
		Log.i(LOG_TAG, "unregistering device (regId = " + regId + ")");
		String serverUrl = URL_SERVER + "/unregister";
		Map<String, String> params = new HashMap<String, String>();
		params.put("regId", regId);
		try {
			HTTPpost(serverUrl, params, HTTP_CONTENT_TYPE_URL_ENCODED);
			GCMRegistrar.setRegisteredOnServer(context, false);
			String message = context.getString(R.string.server_unregistered);
			CommonUtilities.displayMessage(context, message);
		} catch (IOException e) {
			// At this point the device is unregistered from GCM, but still
			// registered in the server.
			// We could try to unregister again, but it is not necessary:
			// if the server tries to send a message to the device, it will get
			// a "NotRegistered" error message and should unregister the device.
			String message = context.getString(R.string.server_unregister_error,
					e.getMessage());
			CommonUtilities.displayMessage(context, message);
		}
	}

	/**
	 * Issue a POST request to server.
	 *
	 * @param serverUrl POST address.
	 * @param params request parameters.
	 *
	 * @throws IOException propagated from POST.
	 */
	public static void HTTPpost(String serverUrl, Map<String, String> params, String contentType)
			throws IOException {
		URL url;
		try {
			url = new URL(serverUrl);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid url: " + serverUrl);
		}
		StringBuilder bodyBuilder = new StringBuilder();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		// constructs the POST body using the parameters
		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();
			bodyBuilder.append(param.getKey()).append('=')
			.append(param.getValue());
			if (iterator.hasNext()) {
				bodyBuilder.append('&');
			}
		}
		String body = bodyBuilder.toString();
		Log.v(LOG_TAG, "Posting '" + body + "' to " + url);
		byte[] bytes = body.getBytes();
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setFixedLengthStreamingMode(bytes.length);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", contentType);
			// post the request
			OutputStream out = conn.getOutputStream();
			out.write(bytes);
			out.close();
			// handle the response
			int status = conn.getResponseCode();

			if (status != 200) {
				throw new IOException("Post failed with error code " + status);
			}
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}


	public static JSONArray getJSONArray(String url, String userId) {

		InputStream is = null;
		JSONArray jArray = null;
		String json = "";

		// Making HTTP request
		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("user_id", userId));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();          

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			json = sb.toString();
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		// try parse the string to a JSON object
		try {
			jArray = new JSONArray(json);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		// return JSON String
		return jArray;
	}

	
	public static void postJSON(String url, String JSONString){

		HttpPost httpPost = new HttpPost(url);
		StringEntity se;
		
		try {
			
			se = new StringEntity(JSONString);
			se.setContentType("application/json");
			se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
			//httpPost.setHeader("Accept", "application/json");
			httpPost.setEntity(se);
			HttpResponse resp = new DefaultHttpClient().execute(httpPost);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
