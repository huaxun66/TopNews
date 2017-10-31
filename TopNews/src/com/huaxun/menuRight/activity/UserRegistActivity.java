package com.huaxun.menuRight.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.NewsUrls;
import com.huaxun.utils.ChoosePhotoAndZoomUtil;
import com.huaxun.utils.FileUtil;
import com.huaxun.utils.HttpUtil;
import com.huaxun.utils.ImageUtil;
import com.huaxun.utils.NetUtil;
import com.huaxun.utils.Util;

/**
 * @author huaxun
 * @Create 2016-3-21
 * @Module core
 * @Description 用户注册界面
 */
public class UserRegistActivity extends Activity implements OnClickListener {
	private TextView topBack, topTitle;
	private ImageView usericon, icon_switch;
	private TextView icon_mode;
	private EditText regist_username_edit, regist_email_edit, regist_password_edit, regist_passwod_again_edit;
	private TextView regist_gender;
	private Button regist_finish_btn;
	private ChoosePhotoAndZoomUtil choosePhotoAndZoomUtil;// 进入相册，并剪切
	private String localIconPath = null;
	private boolean openCamera = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setTranslucent(this);
		setContentView(R.layout.userregist_activity_layout);
		
		topBack = (TextView) findViewById(R.id.topBack);
		topTitle = (TextView) findViewById(R.id.topTitle);
		usericon = (ImageView) findViewById(R.id.usericon);
		icon_mode = (TextView) findViewById(R.id.icon_mode);
		icon_switch = (ImageView) findViewById(R.id.icon_switch);
		regist_username_edit = (EditText) findViewById(R.id.regist_username_edit);
		regist_gender = (TextView) findViewById(R.id.regist_gender);
		regist_email_edit = (EditText) findViewById(R.id.regist_email_edit);
		regist_password_edit = (EditText) findViewById(R.id.regist_password_edit);
		regist_passwod_again_edit = (EditText) findViewById(R.id.regist_password_again_edit);
		regist_finish_btn = (Button) findViewById(R.id.regist_finish_btn);
		topTitle.setText("注册");
		topBack.setOnClickListener(this);
		usericon.setOnClickListener(this);
		icon_switch.setOnClickListener(this);
		regist_gender.setOnClickListener(this);
		regist_finish_btn.setOnClickListener(this);
		
		choosePhotoAndZoomUtil = new ChoosePhotoAndZoomUtil(this);
	}


	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.topBack:
			finish();
			overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
			break;
		case R.id.usericon:
			if (openCamera) {
				choosePhotoAndZoomUtil.takePicture();
			} else {
				choosePhotoAndZoomUtil.choosePhotoInAlbum();
			}
			break;
		case R.id.icon_switch:
			if (openCamera) {
				openCamera = false;
				icon_switch.setImageResource(R.drawable.choff1);
				icon_mode.setText("打开拍照模式");
			} else {
				openCamera = true;
				icon_switch.setImageResource(R.drawable.chon1);
				icon_mode.setText("关闭拍照模式");
			}
			break;	
		case R.id.regist_gender:
			showGerderDialog();
			break;
		case R.id.regist_finish_btn:
			if (regist_username_edit.getText().toString().trim().equals("")) {
				Toast.makeText(UserRegistActivity.this, "用户名不能为空！", Toast.LENGTH_SHORT).show();
				break;
			}			
			if (!regist_password_edit.getText().toString().trim().equals("")
					|| !regist_passwod_again_edit.getText().toString().trim().equals("")) {
				if (!regist_password_edit.getText().toString().equals(regist_passwod_again_edit.getText().toString())) {
					Toast.makeText(UserRegistActivity.this, "两次填写的密码不一致！", Toast.LENGTH_SHORT).show();
					break;
				} else if (regist_password_edit.getText().toString().length() < 6
						|| regist_passwod_again_edit.getText().toString().length() < 6) {
					Toast.makeText(UserRegistActivity.this, "密码长度请大于等于6位！", Toast.LENGTH_SHORT).show();
					break;
				}
			}
			if (NetUtil.isNetworkAvailable(this)) {
				new FinishRegistTask().execute();
			} else {
				Toast.makeText(UserRegistActivity.this, "请开启网络！", Toast.LENGTH_SHORT).show();
				break;
			}
			break;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		BaseTools.showlog("resultCode="+resultCode+"  data="+data);
		if (resultCode != RESULT_OK) {
			return;
		}		
		 // 拍照
		if (requestCode == ChoosePhotoAndZoomUtil.PHOTOHRAPH) {
			if (null == ChoosePhotoAndZoomUtil.mPictureFile) {
				Util.showToast(this, "拍照失败，请重试");
				return;
			}			
           choosePhotoAndZoomUtil.startPhotoZoom(Uri.fromFile(ChoosePhotoAndZoomUtil.mPictureFile));  
		}
		
		if (data == null) {
			return;
		}
		// 读取相册缩放图片
		if (requestCode == ChoosePhotoAndZoomUtil.PHOTOZOOM) {
			choosePhotoAndZoomUtil.startPhotoZoom(data.getData());
		}

		// 处理结果
		if (requestCode == ChoosePhotoAndZoomUtil.PHOTORESOULT) {
			String dirPath = FileUtil.getUserInfoPath() + "/localIcon";
			File mDir = new File(dirPath);
			if (!mDir.exists()) {
				mDir.mkdir();
			}
			Bundle extras = data.getExtras();
			if (extras != null) {
				Bitmap photo = extras.getParcelable("data");  //压缩之前bitmap
				// 三星手机返回空
				if (photo == null)
					return;
				
				localIconPath = dirPath + "/userIcon.png";
				Bitmap zoomBitmap = ImageUtil.zoomBimtap(photo, 100, 100); //压缩之后bitmap
				ImageUtil.saveImage(zoomBitmap, localIconPath, Bitmap.CompressFormat.PNG);
				
				// 部分手机会对图片做旋转，这里检测旋转角度
				int degree = ImageUtil.readPictureDegree(localIconPath);
				if (degree != 0) {
					// 把图片旋转为正的方向
					zoomBitmap = ImageUtil.rotateImage(degree, zoomBitmap);
					ImageUtil.saveImage(zoomBitmap, localIconPath, Bitmap.CompressFormat.PNG);
				}

				BitmapFactory.Options options = new BitmapFactory.Options();
				Bitmap bitmap = BitmapFactory.decodeFile(localIconPath, options);	//压缩之后的bitmap
				usericon.setImageBitmap(bitmap);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * gender select dialog,性别选择对话框
	 */
	private void showGerderDialog(){
		final Dialog dialog = new Dialog(this, R.style.WhiteDialog);
		dialog.setContentView(R.layout.tpl_gender_select_dialog);
		final ImageView ivBoy = (ImageView) dialog.findViewById(R.id.dialog_iv_boy);
		final ImageView ivGirl = (ImageView) dialog.findViewById(R.id.dialog_iv_girl);
        if (regist_gender.getText().toString() != null && regist_gender.getText().toString().equals("女")){
        	ivGirl.setVisibility(View.VISIBLE);
			ivBoy.setVisibility(View.GONE);
        }
		dialog.findViewById(R.id.rl_boy).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ivGirl.setVisibility(View.GONE);
				ivBoy.setVisibility(View.VISIBLE);
				regist_gender.setText("男");
				dialog.dismiss();
			}
		});
		dialog.findViewById(R.id.rl_girl).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ivGirl.setVisibility(View.VISIBLE);
				ivBoy.setVisibility(View.GONE);
				regist_gender.setText("女");
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	class FinishRegistTask extends AsyncTask<Void, Void, String> {
		String registerUrl = NewsUrls.USER_REGISTER;
		String uploadUrl = NewsUrls.UPLOAD_FILE;
		
		ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = ProgressDialog.show(UserRegistActivity.this, null, "请稍后...");
		}

		@Override
		protected String doInBackground(Void... arg0) {
			String result = null;
			String sendIconResult = null;
			
			String username = regist_username_edit.getText().toString();
			String password = regist_password_edit.getText().toString();
			String email = regist_email_edit.getText().toString();
			String gender = regist_gender.getText().toString();
			
			if (localIconPath != null) {
				try {		
					sendIconResult = HttpUtil.sendFileWithPost(uploadUrl, localIconPath, username + "_icon.png");
					BaseTools.showlog("sendIconResult="+sendIconResult);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}		
			
			Map<String, String> params = new HashMap<String, String>();
			params.put("username", username);
			params.put("password", password);
			params.put("email", email);
			params.put("gender", gender);
			if (sendIconResult !=null && sendIconResult.equals("上传成功")) {
				params.put("hasIcon", "true");
			} else {
				params.put("hasIcon", "false");
			}
			
			try {
				//get方式
				result = HttpUtil.sendGetRequest(registerUrl, params, "utf-8");
				//post方式
//				result = HttpUtil.sendPostRequest(registerUrl, params, "utf-8");
				//HttpClient方式
//				result = HttpUtil.sendHttpClientWithPost(registerUrl, params, "utf-8");
			} catch (Exception e) {
				e.printStackTrace();
			}

			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (progressDialog != null)
				progressDialog.dismiss();

			if (result != null && !result.equals("")) {				
				Util.showToast(UserRegistActivity.this, result);
				if (result.equals("注册成功")) {
					finish();
					overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
				}
			} else {
				Util.showToast(UserRegistActivity.this, "注册失败");
			}
		}
	}

}
