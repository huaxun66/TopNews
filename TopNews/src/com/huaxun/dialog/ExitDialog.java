package com.huaxun.dialog;

import com.huaxun.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ExitDialog extends Dialog implements android.view.View.OnClickListener {
	Handler handler;
	private Typeface fontFace;
    private TextView exit_tv;
	private Button cancelButton;
	private Button exitButton;
	
	public ExitDialog (Context context, Handler handler) {
		super(context,R.style.popuptoast_dialog);
		setContentView(R.layout.exit_dialog);
        exit_tv = (TextView) this.findViewById(R.id.exit_tv);
        cancelButton = (Button) this.findViewById(R.id.cancle);
        exitButton = (Button) this.findViewById(R.id.exit);
		fontFace = Typeface.createFromAsset(context.getAssets(),"fonts/klz.ttf");
		exit_tv.setTypeface(fontFace);
		this.handler = handler;
		cancelButton.setOnClickListener(this);
		exitButton.setOnClickListener(this);  
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cancle:
			this.dismiss();
			break;
		case R.id.exit:
			handler.sendEmptyMessage(0);
			break;
		}
		
	}
}
