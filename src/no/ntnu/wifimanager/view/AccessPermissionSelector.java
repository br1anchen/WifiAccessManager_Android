package no.ntnu.wifimanager.view;

import no.ntnu.wifimanager.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

public class AccessPermissionSelector extends TextView{

	private Context context;
	private AccessPermissionStatus status;
	private Drawable ic_status_allow_access;
	private Drawable ic_status_block_access;

	public  AccessPermissionSelector(Context context, AttributeSet attrs) {
		super(context, attrs);

		ic_status_allow_access = context.getResources().getDrawable(R.drawable.ic_ball_green);
		ic_status_block_access = context.getResources().getDrawable(R.drawable.ic_ball_red);

		this.context = context;
		inflateLayout();
	}


	private void inflateLayout() {
		LayoutInflater.from(context).inflate(R.layout.view_access_perm_selector, null);
	}

	public void setStatus(AccessPermissionStatus status){

		this.status = status;

		switch (status) {

		case ACCESS_ALLOW:

			setCompoundDrawablesWithIntrinsicBounds(null, ic_status_allow_access , null, null);
			setText(context.getString(R.string.allow_access));
			break;

		case ACCESS_BLOCK:

			setCompoundDrawablesWithIntrinsicBounds(null, ic_status_block_access , null, null);
			setText(context.getString(R.string.block_access));
			break;
		}

	}

	
	public void showNextStatus(){

		switch (status) {

		case ACCESS_ALLOW:
			setStatus(AccessPermissionStatus.ACCESS_BLOCK);
			break;

		case ACCESS_BLOCK:

			setStatus(AccessPermissionStatus.ACCESS_ALLOW);
			break;
		}
	}

	public AccessPermissionStatus getStatus(){
		return status;
	}

	public enum AccessPermissionStatus{
		ACCESS_ALLOW, ACCESS_BLOCK;
	}

}
