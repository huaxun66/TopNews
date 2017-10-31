package com.huaxun.utils;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.huaxun.app.AppApplication;
import com.huaxun.radio.activity.AddNewRadioActivity;

/**
 * @author panyan
 * @Create 2014-7-23 下午3:39:21
 * @Module core
 * @Description 调用系统camera/系统相册并进入系统缩放
 */
public class ChoosePhotoAndZoomUtil {
	private Context context;
	public static final String IMAGE_UNSPECIFIED = "image/*";
    public static final int NONE = 90;  
    public static final int PHOTOHRAPH = 91;// 拍照  
    public static final int PHOTOZOOM = 92; // 缩放  
    public static final int PHOTORESOULT = 93;// 结果 
    //设置相机拍照后照片保存路径
    public static File mPictureFile;


	public ChoosePhotoAndZoomUtil(Context context) {
		super();
		this.context = context;
	}

	public void takePicture() {
		// 设置相机拍照后照片保存路径
		mPictureFile = new File(FileUtil.getCacheImagePath(),
				"picture" + System.currentTimeMillis()/1000 + ".png");
		// 启动拍照,并保存到临时文件
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPictureFile));
		intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
		((Activity)context).startActivityForResult(intent, PHOTOHRAPH);
	}

	public void choosePhotoInAlbum() {
		Intent intent = new Intent(Intent.ACTION_PICK, null);
		intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_UNSPECIFIED);
		((Activity)context).startActivityForResult(intent, PHOTOZOOM);
	}
	
	public void chooseVedioInAlbum() {
		Intent intent = new Intent(Intent.ACTION_PICK, null);
		intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_UNSPECIFIED);
		((Activity)context).startActivityForResult(intent, PHOTOZOOM);
	}

	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 64);
		intent.putExtra("outputY", 64);
		intent.putExtra("return-data", true);
		((Activity)context).startActivityForResult(intent, PHOTORESOULT);
	}
}
