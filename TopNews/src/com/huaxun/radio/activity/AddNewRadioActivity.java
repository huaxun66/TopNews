package com.huaxun.radio.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huaxun.R;
import com.huaxun.app.AppApplication;
import com.huaxun.base.BaseActivity;
import com.huaxun.db.DataDB;
import com.huaxun.fragment.NewsRadioFragment;
import com.huaxun.radio.bean.RadioAudioURLDetail;
import com.huaxun.radio.bean.RadioItem;
import com.huaxun.radio.provider.MusicUtils;
import com.huaxun.tool.Constants;
import com.huaxun.utils.ChoosePhotoAndZoomUtil;

/***
 * Add New Radio Fragment
 * 
 * @author huaxun 2015.10.19
 *
 */
@SuppressLint("NewApi")
public class AddNewRadioActivity extends BaseActivity implements OnClickListener {
	private ImageButton topBack;
	private TextView topTitle;
	private EditText radioname, radiourl, radiodescription;
	private ImageView checkbutton, conservebutton, checkresultbutton, addiconbutton, addpicbutton;
	private DataDB dataDB;
	public ChoosePhotoAndZoomUtil choosePhotoAndZoomUtil;// 进入相册，并剪切
	private Handler handler;
	public AudioManager audioManager;
	// 标示点击的是Icon还是Pic图标 0 未点击 1 Icon 2 Pic
	private int flag = 0;
	// 标示检测状态 0 检测失败 1 检测成功
	private int checkstatus = 0;
	// 标示Icon和Pic是否从本地加载了图片
	private boolean isIconload = false;
	private boolean isPicload = false;
	// 上次检测OK的radio Url
	private String checkurl;
	private String radioListTitle = "";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dataDB = DataDB.getInstance(this);
		setContentView(R.layout.add_radio_layout);
		topBack = (ImageButton) findViewById(R.id.topBack);
		topBack.setOnClickListener(this);
		topTitle = (TextView) findViewById(R.id.topTitle);
		radioname = (EditText) findViewById(R.id.radio_name);
		radiourl = (EditText) findViewById(R.id.radio_url);
		radiourl.setText("http://42.96.249.166/live/24035.m3u8");
		//radiourl.setText("http://listen.eastday.com/media/auto/2015-12-09/257ba696-19ec-4db0-978b-34aefcd23f1d.mp3");
		radiodescription = (EditText) findViewById(R.id.radio_description);
		checkbutton = (ImageView) findViewById(R.id.check_button);
		conservebutton = (ImageView) findViewById(R.id.conserve_button);
		checkresultbutton = (ImageView) findViewById(R.id.check_result_button);
		addiconbutton = (ImageView) findViewById(R.id.add_icon_button);
		addpicbutton = (ImageView) findViewById(R.id.add_pic_button);
		checkbutton.setOnClickListener(this);
		conservebutton.setOnClickListener(this);
		checkresultbutton.setOnClickListener(this);
		addiconbutton.setOnClickListener(this);
		addpicbutton.setOnClickListener(this);
		choosePhotoAndZoomUtil = new ChoosePhotoAndZoomUtil(this);
		audioManager = (AudioManager) this.getApplicationContext().getSystemService(this.AUDIO_SERVICE);
		handler = new Handler();
		
		Bundle bundle = getIntent().getExtras();
		radioListTitle = bundle.getString("radioListTitle");	
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.check_button:
			checkRadioUrl();
			break;
		case R.id.conserve_button:
			if (checkRadioStatus() == true) {
				dataDB.updateRadioItem(editradioitem,radioListTitle);
				Toast.makeText(this, "保存成功！", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent("ListChanged");
				intent.putExtra("listName", radioListTitle);
				intent.putExtra("item",editradioitem);
				intent.putExtra("addOrdelete", "add");
				sendBroadcast(intent);
				finish();
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			}
			break;
		case R.id.check_result_button:
			if (checkRadioStatus() == true) {
				String url = radiourl.getText().toString();
				OpenRadioDetail(editradioitem);				
				StartRadioPlay(url);
			}
		    break;
		case R.id.add_icon_button:
			flag = 1;
			choosePhotoAndZoomUtil.choosePhotoInAlbum();
			break;
		case R.id.add_pic_button:
			flag = 2;
			choosePhotoAndZoomUtil.choosePhotoInAlbum();
			break;
		case R.id.topBack:
			finish();
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			break;
		default:
			break;
		}
	}
	
	public void onBackPressed() {
		finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	private RadioItem editradioitem = new RadioItem();
	private boolean checkRadioStatus() {
		if (TextUtils.isEmpty(radioname.getText().toString())) {
			Toast.makeText(this, "电台名称不能为空！", Toast.LENGTH_SHORT).show();
			return false;
		} else if (TextUtils.isEmpty(radiourl.getText().toString())) {
			Toast.makeText(this, "电台URL不能为空！", Toast.LENGTH_SHORT).show();
			return false;
		} else if (checkstatus == 0) {
			Toast.makeText(this, "请先校验电台URL！", Toast.LENGTH_SHORT).show();
			return false;
		} else if (checkstatus == 2) {
			Toast.makeText(this, "请输入正确的电台URL！", Toast.LENGTH_SHORT).show();
			return false;
		} else if (checkstatus == 1 && !checkurl.isEmpty()) {
			if (checkurl.equals(radiourl.getText().toString())) {
				String url = radiourl.getText().toString();
				String newstitle = radioname.getText().toString();
				RadioAudioURLDetail radioaudiourldetail = new RadioAudioURLDetail(url);
				ArrayList<RadioAudioURLDetail> audiourl = new ArrayList<RadioAudioURLDetail>();
				audiourl.add(radioaudiourldetail);
				String newsintroduce = "";
				String imgurl1 = "";
				String audioiconimage = "";

				if (!radiodescription.getText().toString().isEmpty()) {
					newsintroduce = radiodescription.getText().toString();
				}
				if (isIconload == true) {
					audioiconimage = Constants.RadioFolderPath + "/radio_icon_" + newstitle + ".png";
				}
				if (isPicload == true) {
					imgurl1 = Constants.RadioFolderPath + "/radio_pic_" + newstitle + ".png";
				}
				editradioitem = new RadioItem(newstitle, newsintroduce, audiourl, imgurl1, audioiconimage);
				return true;
			} else {
				Toast.makeText(this, "请先校验电台URL！", Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		return false;
	}
	
	private void OpenRadioDetail(RadioItem radioItem) {
		Intent intent = new Intent(this,RadioDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("tag_activity", radioItem);
		bundle.putString("resource", "check_result");
		intent.putExtras(bundle);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	private void StartRadioPlay(String string) {
		Intent detailIntent = new Intent("RADIO_CONTROL");
		detailIntent.putExtra("url", string);
		detailIntent.putExtra("control", "start");
		sendBroadcast(detailIntent);
	}

//	public void popRadioManagerFragment() {
//		int num = getActivity().getSupportFragmentManager().getBackStackEntryCount();
//		String numString = "++++++++++++++++++++++++++++++++++Fragment回退栈数量：" + num;
//		NewsConstants.showLog(numString);
//
//		for (int i = 0; i < num; i++) {
//			BackStackEntry backstatck = getActivity().getSupportFragmentManager().getBackStackEntryAt(i);
//			NewsConstants.showLog(backstatck.getName());
//		}
//		// BaseFragment fragment = mainAct.mBackStack.pop();
//		// FragmentManager fragmentManager = getFragmentManager();
//		// FragmentTransaction trans = fragmentManager.beginTransaction();
//		// // this.onHide();
//		// // trans.hide(this);
//		// trans.remove(fragment);
//		// trans.commitAllowingStateLoss();		
//		Intent intent = new Intent(mainAct, UserSpaceActivity.class);
//		Bundle mBundle = new Bundle();
//		mBundle.putString("radioListTitle", radioListTitle);
//		mBundle.putString("to", "RadioManager");
//		intent.putExtras(mBundle);
//		mainAct.startActivity(intent);
//		final boolean isAnimationsOrNot = mainAct.needAnimations;
//		handler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				mainAct.needAnimations = false;
//				mainAct.popFragment();
//				mainAct.needAnimations = isAnimationsOrNot;
//			}
//		}, 500);
//	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null) {
			return;
		}

		if (requestCode == ChoosePhotoAndZoomUtil.PHOTOZOOM) {
			choosePhotoAndZoomUtil.startPhotoZoom(data.getData());
		}
		// 处理结果
		if (requestCode == ChoosePhotoAndZoomUtil.PHOTORESOULT) {
			String dirPath = Constants.RadioFolderPath;
			File mDir = new File(dirPath);
			if (!mDir.exists()) {
				mDir.mkdir();
			}
			String name = radioname.getText().toString();
			String path = "";
			Bundle extras = data.getExtras();
			if (extras != null) {
				Bitmap photo = extras.getParcelable("data");
				if (photo == null)
					return;// 三星手机返回空
				// ByteArrayOutputStream stream = new ByteArrayOutputStream();
				FileOutputStream out = null;
				try {
					if (flag == 1) {
						out = new FileOutputStream(new File(Constants.RadioFolderPath + "/radio_icon_" + name + ".png"));
						photo.compress(Bitmap.CompressFormat.PNG, 75, out);
						path = Constants.RadioFolderPath + "/radio_icon_" + name + ".png";
					} else if (flag == 2) {
						out = new FileOutputStream(new File(Constants.RadioFolderPath + "/radio_pic_" + name + ".png"));
						photo.compress(Bitmap.CompressFormat.PNG, 75, out);
						path = Constants.RadioFolderPath + "/radio_pic_" + name + ".png";
					} else {
					}

				} catch (FileNotFoundException e) {
				} finally {
					FileInputStream fis = null;
					try {
						fis = new FileInputStream(path);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Bitmap bitmap = BitmapFactory.decodeStream(fis);
					if (flag == 1) {
						addiconbutton.setImageBitmap(bitmap);
						isIconload = true;
					} else if (flag == 2) {
						addpicbutton.setImageBitmap(bitmap);
						isPicload = true;
					} else {
					}
					try {
						out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private ProgressDialog MyDialog;
	private String str;

	public void checkRadioUrl() {
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(checkbutton.getWindowToken(), 0);
		}
		final int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		MyDialog = ProgressDialog.show(this, "检测中", "请稍后... ", true);
		final String url = radiourl.getText().toString();
		try {
			long[] list = new long[1];
			list[0] = MusicUtils.insert(this, url);
			NewsRadioFragment.mService.open(list, 0);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					boolean isplay = false;
					try {
						isplay = NewsRadioFragment.mService.isPlaying();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (isplay) {
						str = "有效URL";
						checkstatus = 1;
						checkurl = url;
					} else {
						str = "无效URL";
						checkstatus = 2;
						checkurl = "";
					}
					Toast.makeText(AddNewRadioActivity.this, str, Toast.LENGTH_SHORT).show();
					MyDialog.dismiss();
					try {
						NewsRadioFragment.mService.stop();
						audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}, 3000);
		}
	}
}
