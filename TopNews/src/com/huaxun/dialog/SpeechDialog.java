package com.huaxun.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.ImageView;

import com.huaxun.R;

public class SpeechDialog extends Dialog {
	
	private ImageView image;
	public SpeechDialog (Context context) {
		super(context,R.style.popuptoast_dialog);
		setContentView(R.layout.image_toast);
		image = (ImageView) findViewById(R.id.imageToast);
	}
	
	public void setImageResource(int resource) {
		image.setImageResource(resource);
	}
}