package com.huaxun.dialog;

import com.huaxun.R;
import com.huaxun.tool.VolleyTool;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoadingDialog extends Dialog{
	private Typeface fontFace;
    private TextView loading_tv;
	
	public LoadingDialog (Context context) {
		super(context,R.style.loading_dialog);
		setContentView(R.layout.loading_dialog);
		loading_tv = (TextView) this.findViewById(R.id.loading_tv);
		fontFace = Typeface.createFromAsset(context.getAssets(),"fonts/klz.ttf");
		loading_tv.setTypeface(fontFace);
	}	
	
}
