package com.huaxun.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;

import com.huaxun.app.AppApplication;
import com.huaxun.menuRight.bean.UserInfo;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.Constants;

public class FileUtil {

	// 判断News文件是否存在,如果不存在就创建临时文件，如果存在就删掉
	public static void createTempNewsFile(String name) {
		File file = new File(getNewsPath() + File.separator + name);
		if (file.exists()) {
			file.delete();
		} else {
			try {
				file = new File(getNewsPath() + File.separator + name + ".tmp");
				file.createNewFile();
			} catch (Exception e) {
			}
		}
	}

	public static String getNewsPath() {
		return Constants.NewsFolderPath;
	}

	public static boolean isNewsFileExist(String name) {
		File file = new File(getNewsPath() + File.separator + name);
		return file.exists();
	}

	public static void downloadNewsToSDcard(String newstitle, String newsurl) {
		boolean isContinueDownload;
		URL url = null;
		HttpURLConnection conn = null;
		InputStream inStream = null;
		try {
			url = new URL(newsurl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true); // 设置向程序输入数据
			conn.setRequestMethod("GET"); // 设置Get方法来请求
			conn.connect(); // 连接服务器
			// 返回200表示连接成功
			int responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				isContinueDownload = true;
				inStream = conn.getInputStream();
				// 如果SD卡上news文件不存在就创建它，如果存在就删掉它
				createTempNewsFile(newstitle);
				// 真正的下载过程
				File file = null;
				OutputStream output = null;
				try {
					// 在本地创建要下载的文件，然后读取数据放进去
					file = new File(FileUtil.getNewsPath() + File.separator + newstitle + ".tmp");
					// 把创建好的文件用输出流打开，准备往里面写数据
					output = new FileOutputStream(file);
					byte[] buffer = new byte[1024];
					int num = 0;
					do {
						num = inStream.read(buffer);
						if (num <= 0) {
							isContinueDownload = false;
							break;
						}
						output.write(buffer, 0, num);
					} while (isContinueDownload);

					output.flush();
					output.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				File f = new File(FileUtil.getNewsPath() + File.separator + newstitle);
				file.renameTo(f);
			} else {

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// finally无论有没有异常都会执行，在这里断开连接
			conn.disconnect();
		}
	}
	
	public static String ReadLocalNewsFile(String strFilePath) {
		String path = strFilePath;
		String content = ""; // 文件内容字符串
		// 打开文件
		File file = new File(path);
		// 如果path是传递过来的参数，可以做一个非目录的判断
		if (file.isDirectory()) {
			BaseTools.showlog("The file is a directory");
		} else {
			try {
				InputStream instream = new FileInputStream(file);
				if (instream != null) {
					InputStreamReader inputreader = new InputStreamReader(instream);
					BufferedReader buffreader = new BufferedReader(inputreader);
					String line;
					// 分行读取
					while ((line = buffreader.readLine()) != null) {
						content += line + "\n";
					}
					instream.close();
				}
			} catch (java.io.FileNotFoundException e) {
				BaseTools.showlog("The File doesn't not exist.");
			} catch (IOException e) {
				BaseTools.showlog(e.getMessage());
			}
		}
		return content;
	}

	// 判断Media文件是否存在,如果不存在就创建临时文件，如果存在就删掉
	public static void createTempMediaFile(String name) {
		File file = new File(getMediaPath() + File.separator + name);
		if (file.exists()) {
			file.delete();
		} else {
			try {
				file = new File(getMediaPath() + File.separator + name + ".tmp");
				file.createNewFile();
			} catch (Exception e) {
			}
		}
	}

	public static String getMediaPath() {
		return Constants.MediaFolderPath;
	}

	public static boolean isMediaFileExist(String name) {
		File file = new File(getMediaPath() + File.separator + name);
		return file.exists();
	}

	public static void downloadMediaToSDcard(String name, String newsurl) {
		boolean isContinueDownload;
		URL url = null;
		HttpURLConnection conn = null;
		InputStream inStream = null;
		try {
			url = new URL(newsurl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true); // 设置向程序输入数据
			conn.setRequestMethod("GET"); // 设置Get方法来请求
			conn.connect(); // 连接服务器
			// 返回200表示连接成功
			int responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				isContinueDownload = true;
				inStream = conn.getInputStream();
				// 如果SD卡上media文件不存在就创建它，如果存在就删掉它
				createTempMediaFile(name);
				// 真正的下载过程
				File file = null;
				OutputStream output = null;
				try {
					// 在本地创建要下载的文件，然后读取数据放进去
					file = new File(FileUtil.getMediaPath() + File.separator + name + ".tmp");
					// 把创建好的文件用输出流打开，准备往里面写数据
					output = new FileOutputStream(file);
					byte[] buffer = new byte[1024];
					int num = 0;
					do {
						num = inStream.read(buffer);
						if (num <= 0) {
							isContinueDownload = false;
							break;
						}
						output.write(buffer, 0, num);
					} while (isContinueDownload);

					output.flush();
					output.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				File f = new File(FileUtil.getMediaPath() + File.separator + name);
				file.renameTo(f);
			} else {

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// finally无论有没有异常都会执行，在这里断开连接
			conn.disconnect();
		}
	}

	// 判断Cache文件是否存在,如果不存在就创建，如果存在就删掉
	public static void createTempCacheFile(String name) {
		File file = new File(getCachePath() + File.separator + name);
		if (file.exists()) {
			file.delete();
		} else {
			try {
				file = new File(getCachePath() + File.separator + name + ".tmp");
				file.createNewFile();
			} catch (Exception e) {
			}
		}
	}

	public static String getCachePath() {
		return Constants.CacheFolderPath;
	}

	public static boolean isCacheFileExist(String name) {
		File file = new File(getCachePath() + File.separator + name);
		return file.exists();
	}
	
	public static String getCacheImagePath() {
		return Constants.ImageFolderPath;
	}
	
	public static String getCacheAudioPath() {
		return Constants.AudioFolderPath;
	}
	
	public static boolean isCacheImageFileExist(String name) {
		File file = new File(getCacheImagePath() + File.separator + name);
		return file.exists();
	}
	
	public static String getPicturePath() {
		return Constants.PictureFolderPath;
	}
	
	public static boolean isPictureFileExist(String name) {
		File file = new File(getPicturePath() + File.separator + name);
		return file.exists();
	}
	
	public static String getMusicPath() {
		return Constants.MusicFolderPath;
	}
	
	public static String getLrcPath() {
		return Constants.LrcFolderPath;
	}
	
	/**
	 * 获取文件的大小
	 * @param fileName
	 * @return
	 */
	public static long getPictureFileSize(String fileName) {
		return new File(getPicturePath() + File.separator + fileName).length();
	}
	
	/**
	 * 保存Image的方法，有sd卡存储到sd卡，没有就存储到手机目录
	 * @param fileName 
	 * @param bitmap   
	 * @throws IOException
	 */
	public static void saveBitmap(String fileName, Bitmap bitmap) throws IOException{
		if(bitmap == null){
			return;
		}
		String path = getPicturePath();
		File folderFile = new File(path);
		if(!folderFile.exists()){
			folderFile.mkdir();
		}
		File file = new File(path + File.separator + fileName);
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		bitmap.compress(CompressFormat.JPEG, 100, fos);
		fos.flush();
		fos.close();
	}
	
	/**
	 * 从手机或者sd卡获取Bitmap
	 * @param fileName
	 * @return
	 */
	public static Bitmap getBitmap(String fileName){
		return BitmapFactory.decodeFile(getPicturePath() + File.separator + fileName);
	}
	
	/**
	 * 获取文件的名称
	 * */
	public static String getFileName(String str) {
		int i = str.lastIndexOf('/');
		if (i != -1) {
			return str.substring(i + 1);
		}
		return str;
	}

	/**
	 * 获取文件的后缀名，返回大写
	 * */
	public static String getSuffix(String str) {
		int i = str.lastIndexOf('.');
		if (i != -1) {
			return str.substring(i + 1).toUpperCase();
		}
		return str;
	}

	/**
	 * 格式化文件大小 Byte->MB
	 * */
	public static String formatByteToMB(long l) {
		float mb = l / 1024f / 1024f;
		return String.format("%.2f", mb);
	}
	
	/**
	 * 调用此方法自动计算指定文件或指定文件夹的大小
	 * 
	 * @param filePath
	 *            文件路径
	 * @return 计算好的带B、KB、MB、GB的字符串
	 */
	public static String getFileOrFilesSize(String filePath) {
		File file = new File(filePath);
		long blockSize = 0;
		try {
			if (file.isDirectory()) {
				blockSize = getFileSizes(file);
			} else {
				blockSize = getFileSize(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("获取文件大小", "获取失败!");
		}
		return FormatFileSize(blockSize);
	}

	/**
	 * 获取指定文件大小
	 * 
	 * @param f
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public static long getFileSize(File file) {
		long size = 0;
		try {
			if (file.exists()) {
				FileInputStream fis = null;
				fis = new FileInputStream(file);
				size = fis.available();
			} else {
				file.createNewFile();
				Log.e("获取文件大小", "文件不存在!");
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return size;
	}

	/**
	 * 获取指定文件夹
	 * 
	 * @param f
	 * @return
	 * @throws Exception
	 */
	public static long getFileSizes(File f) {
		long size = 0;
		try {
			File flist[] = f.listFiles();
			for (int i = 0; i < flist.length; i++) {
				if (flist[i].isDirectory()) {
					size = size + getFileSizes(flist[i]);
				} else {
					size = size + getFileSize(flist[i]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return size;
	}

	/**
	 * 转换文件大小
	 * 
	 * @param fileS
	 * @return
	 */
	private static String FormatFileSize(long fileS) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		String wrongSize = "0B";
		if (fileS == 0) {
			return wrongSize;
		}
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "KB";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "MB";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "GB";
		}
		return fileSizeString;
	}

    /**
     * 删除单个文件
     * @param   filePath    被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String filePath) {
    File file = new File(filePath);
        if (file.isFile() && file.exists()) {
        return file.delete();
        }
        return false;
    }
	
    /**
     * 删除文件夹目录下的文件
     * @param   filePath 被删除目录的文件路径
     * @param   delete  true-也要删除空文件夹     false-只删除文件夹内容，不删除空文件夹
     * @return  目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String filePath, boolean delete) {
    boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
            //删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } else {
            //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath(),true);
                if (!flag) break;
            }
        }
        if (!flag) return false;
        if (delete == true) {
            //删除当前空目录
          return dirFile.delete();
        }
        return true;
    }

    
	public static String getUserInfoPath() {
		return Constants.UserInfoFolderPath;
	}
	
	/**
	 * @Description 将用户信息保存入内部文件,这里直接保存对象，其实也可以转化成字节数组保存，见SDCardHelper
	 */
	public static void saveUserInfo(Context context, UserInfo info) {
		ObjectOutputStream out = null;
		try {
			//用于指定文件名称，不能包含路径分隔符“/”，如果文件不存在，Android会自动创建它。创建的文件保存在/data/data/<package name>/files/目录中
			out = new ObjectOutputStream(context.openFileOutput(Constants.FILE_NMAE_USERINFO, Context.MODE_PRIVATE));
			out.writeObject(info);
			out.flush();
		} catch (Exception e) {
			BaseTools.showlog("Serialize playlist error: " + e.getMessage());
		} finally {
			try {
				if (out != null) {
					out.close();
					out = null;
				}
			} catch (Exception e2) {
				out = null;
				BaseTools.showlog("Serialize playlist (close stream) error: " + e2.getMessage());
			}
		}
	}

	/**
	 * @Description 将用户信息转化为实例
	 */
	public static UserInfo GetUserInfo(Context context) {
		ObjectInputStream in = null;
		UserInfo result = null;
		try {
			in = new ObjectInputStream(context.openFileInput(Constants.FILE_NMAE_USERINFO));
			result = (UserInfo) in.readObject();
		} catch (Exception e) {
			BaseTools.showlog("Unserialize playlist  error: " + e.getMessage());
		} finally {
			try {
				if (in != null) {
					in.close();
					in = null;
				}
			} catch (Exception e3) {
				in = null;
				BaseTools.showlog("Unserialize playlist (close stream) error: " + e3.getMessage());
			}
		}
		return result;
	}

    //读取assert路径下文件
	/**
	 * 读取asset目录下文件。
	 * 
	 * @return 二进制文件数据
	 */
	public static byte[] readAssetsFile(Context context, String filename) {
		try {
			InputStream ins = context.getAssets().open(filename);
			byte[] data = new byte[ins.available()];
			
			ins.read(data);
			ins.close();
			
			return data;
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	/**
	 * Try to return the absolute file path from the given Uri
	 *
	 * @param context
	 * @param uri
	 * @return the file path or null
	 */
	public static String uri2filePath(final Context context, final Uri uri) {
	    if (null == uri) 
	    	return null;
	    final String scheme = uri.getScheme();
	    String data = null;
	    if (scheme == null) {
	    	data = uri.getPath();
	    } else if (ContentResolver.SCHEME_FILE.equals( scheme )) {
	        data = uri.getPath();
	    } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
	        Cursor cursor = context.getContentResolver().query(uri, new String[] {ImageColumns.DATA}, null, null, null );
	        if (null != cursor) {
	            if (cursor.moveToFirst()) {
	                int index = cursor.getColumnIndex(ImageColumns.DATA);
	                if (index > -1) {
	                    data = cursor.getString(index);
	                }
	            }
	            cursor.close();
	        }
	    }
	    return data;
	}
	
}
