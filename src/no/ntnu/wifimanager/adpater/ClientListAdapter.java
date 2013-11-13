package no.ntnu.wifimanager.adpater;


import java.util.ArrayList;

import no.ntnu.wifimanager.R;
import no.ntnu.wifimanager.domain.Client;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ClientListAdapter extends ArrayAdapter<Client> {

	private LayoutInflater layoutInflater;
	private Context context;

	public ClientListAdapter(Context context){
		super(context, R.layout.list_item_client , new ArrayList<Client>());
		this.context = context;
		layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = layoutInflater.inflate(R.layout.list_item_client, null);
		}
		Client client = getItem(position);
		WifiClientListItemContainer.setOrCreateViewContainer(convertView, client, context);

		return convertView;
	}

	private static class WifiClientListItemContainer {

		private TextView nameView;
		private TextView statusView;

		static void setOrCreateViewContainer(View itemView, Client client, Context context) {
			WifiClientListItemContainer container = (WifiClientListItemContainer) itemView.getTag();
			if(container == null) {
				container = new WifiClientListItemContainer();
				container.nameView = (TextView) itemView.findViewById(R.id.client_name);
				container.statusView = (TextView) itemView.findViewById(R.id.client_status);


				itemView.setTag(container);
			}
			container.setFieldValues(client, context);
		}

		void setFieldValues(Client client, Context context) {

			Drawable top;

			if(!client.hasPermissons()){
				top = context.getResources().getDrawable(R.drawable.ic_access_req);
				statusView.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
				statusView.setText(context.getString(R.string.new_request));
			}

			if(client.hasAccessPermission()){
				if(client.getAccessPermission().isAccessAllowed()){
					top = context.getResources().getDrawable(R.drawable.ic_ball_green);
					statusView.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
					statusView.setText(context.getString(R.string.allowed_access));
				}else{
					top = context.getResources().getDrawable(R.drawable.ic_ball_red);
					statusView.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null);
					statusView.setText(context.getString(R.string.blocked_access));
				}
			}

			nameView.setText(client.getName());
		}
	}
}
