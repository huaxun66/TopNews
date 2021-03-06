package com.huaxun.chat;

import com.huaxun.R;
import com.huaxun.chat.util.Tools;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class SettingDialog extends Dialog implements android.view.View.OnClickListener{
	private Context context = null;
	private SpinAdapter adapter;
	private EditText nikeName;
	private Spinner spin;
	private Button okBtn;
	private Button cancelBtn;
	
	private int headIconPos = 0;

	private SharedPreferences sharedPreferences;
	private SharedPreferences.Editor editor;
	public static String chatSharePreferenceKey = "ChatKey";
	
	public SettingDialog(Context context) {
		super(context);
		this.context = context;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_dialog_layout);
		
		nikeName = (EditText)findViewById(R.id.nicke_name);
		spin = (Spinner)findViewById(R.id.spinner1);
		adapter = new SpinAdapter();		
		spin.setAdapter(adapter);		
		okBtn = (Button)findViewById(R.id.okBtn);
		okBtn.setOnClickListener(this);
		cancelBtn = (Button)findViewById(R.id.cancelBtn);
		cancelBtn.setOnClickListener(this);
		
		spin.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				headIconPos = position;
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}		
		});
		
		sharedPreferences = context.getSharedPreferences(chatSharePreferenceKey,Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();
		
		setTitle("修改信息");
		nikeName.setText(Tools.me.getName());
		headIconPos = Tools.me.getHeadIconPos();
		spin.setSelection(headIconPos);
	}

	private class SpinAdapter implements SpinnerAdapter{

		@Override
		public int getCount() {
			return Tools.headIconIds.length;
		}
		@Override
		public Object getItem(int pos) {
			return null;
		}
		@Override
		public long getItemId(int pos) {
			return 0;
		}
		@Override
		public int getItemViewType(int pos) {
			return 0;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		
			convertView = getLayoutInflater().inflate(R.layout.head_image_spinner_layout, null);
			ImageView hicon = (ImageView)convertView.findViewById(R.id.headericon);
			hicon.setImageResource(Tools.headIconIds[position]);
		
			TextView hnote = (TextView)convertView.findViewById(R.id.headernote);
			hnote.setText(position+"");
			hnote.setTextColor(Color.BLACK);
			
			return convertView;
		}

		@Override
		public int getViewTypeCount() {
			return 0;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public void registerDataSetObserver(DataSetObserver arg0) {			
		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver arg0) {			
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			return getView(position, convertView, parent);
		}		
	}



	@Override
	public void onClick(View btn) {
		switch(btn.getId()){
		case R.id.okBtn:
			saveSettings();
			break;
		case R.id.cancelBtn:
			dismiss();
			break;
		}		
	}
	
	private void saveSettings(){
		//发送广播通知更改信息
		if(nikeName.getText().toString().equals("")) {
			Tools.ChatTips(Tools.SHOW, "不能为空");			
		}else{
			editor.putString("nickeName", nikeName.getText().toString());
			editor.putInt("headIconPos", headIconPos);
			editor.commit();
			
			Tools.me.setName(nikeName.getText().toString());
			Tools.me.setHeadIconPos(headIconPos);
			Tools.ChatTips(Tools.SHOW, "设置成功");
			//发送给自己
			Tools.ChatTips(Tools.CMD_UPDATEINFORMATION, null);
			dismiss();
		}
	}
	
	
}
