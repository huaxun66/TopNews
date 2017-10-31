package com.huaxun.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.huaxun.app.AppApplication;
import com.huaxun.tool.BaseTools;

import android.os.Environment;
import android.util.Log;

/**
 * @author zhouchangshi
 * @Create 2013-7-30
 * @Module net
 * @Description 网络操作的封装
 */
public final class HttpUtil {

	private static final String TAG = "HttpUtils";

	/**
	 * @Description
	 */
	private static final int PARAM_CONNECT_TIMEOUT = 6 * 1000;

	/**
	 * 禁止实例化
	 */
	private HttpUtil() {
	}

	/**
	 * @Description 获取网络资源
	 * @param urlStr
	 *            网络资源的地址
	 * @return 返回网络资源的内容 (去除空格的字符串)
	 */

	public static String requestContentWithGet11(String urlStr) {
		String content = null;
		if (urlStr != null && urlStr.startsWith("http://")) {
			URL url = null;
			HttpURLConnection conn = null;
			try {
				url = new URL(urlStr);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(PARAM_CONNECT_TIMEOUT);
				conn.setRequestProperty("accept", "*/*");
				conn.setRequestProperty("Charset", "UTF-8");
				conn.connect();
				int responseCode = conn.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					content = convertStream2String(conn.getInputStream());
				} else {
					content = "";
				}
			} catch (Exception e) {
				Log.d(TAG, "-> method=requestContentWithGet error-message: " + e.getMessage());
			} finally {
				if (conn != null) {
					conn.disconnect();
					conn = null;
				}
			}
		}
		return content;
	}

	/**
	 * @Description 获取网络资源(java内置版本)
	 * @param urlStr
	 *            网络资源的地址
	 * @return 返回网络资源的内容 (没有去除空格的字符串)
	 */
	public static String requestContentWithGet12(String urlStr) {
		String content = null;
		if (urlStr != null && urlStr.startsWith("http://")) {
			URL url = null;
			HttpURLConnection conn = null;
			try {
				url = new URL(urlStr);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(PARAM_CONNECT_TIMEOUT);
				conn.setRequestProperty("accept", "*/*");
				conn.setRequestProperty("Charset", "UTF-8");
				conn.connect();
				int responseCode = conn.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					content = convertStream2String1(conn.getInputStream());
				} else {
					content = "";
				}
			} catch (Exception e) {
				Log.d(TAG, "-> method=requestContentWithGet error-message: " + e.getMessage());
			} finally {
				if (conn != null) {
					conn.disconnect();
					conn = null;
				}
			}
		}
		return content;
	}
	
	/**
	 * @Description 获取网络资源(apache httpclient版本)
	 * @param urlStr
	 *            网络资源的地址
	 * @return 返回网络资源的内容 (去除空格的字符串)
	 */
	public static String requestContentWithGet21(String urlStr) {
		String content = null;
		if (urlStr != null && urlStr.startsWith("http://")) {
			HttpClient client = null;
			HttpGet get = null;
			HttpResponse response = null;
			try {
				BasicHttpParams params = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(params, 3000); //连接超时
				HttpConnectionParams.setSoTimeout(params, 3000); //等待数据超时
				client = new DefaultHttpClient(params);

				get = new HttpGet(urlStr);
				get.addHeader("Accept", "*/*");
				get.addHeader("Charset", "utf-8");

				response = client.execute(get);
				int responseCode = response.getStatusLine().getStatusCode();
				if (responseCode == HttpStatus.SC_OK) {
					content = convertStream2String(response.getEntity().getContent());
				} else {
					content = "";
				}
			} catch (Exception e) {
				Log.d(TAG, "-> method=requestContentWithGet error-message: " + e.getMessage());
			}
		}
		return content;
	}

	/**
	 * @Description 获取网络资源(apache httpclient版本)
	 * @param urlStr
	 *            网络资源的地址
	 * @return 返回网络资源的内容 (没有去除空格的字符串)
	 */
	public static String requestContentWithGet22(String urlStr) {
		String content = null;
		if (urlStr != null && urlStr.startsWith("http://")) {
			HttpClient client = null;
			HttpGet get = null;
			HttpResponse response = null;
			try {
				BasicHttpParams params = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(params, 3000); //连接超时
				HttpConnectionParams.setSoTimeout(params, 3000); //等待数据超时
				client = new DefaultHttpClient(params);

				get = new HttpGet(urlStr);
				get.addHeader("Accept", "*/*");
				get.addHeader("Charset", "utf-8");

				response = client.execute(get);
				int responseCode = response.getStatusLine().getStatusCode();
				if (responseCode == HttpStatus.SC_OK) {
					content = convertStream2String1(response.getEntity().getContent());
				} else {
					content = "";
				}
			} catch (Exception e) {
				Log.d(TAG, "-> method=requestContentWithGet error-message: " + e.getMessage());
			}
		}
		return content;
	}
	
	/**
	 * @Description Get获取网络资源
	 * @param urlPath
	 *            网络资源的地址
	 * @param params
	 *            Get请求参数
	 * @param encoding
	 *            发送内容编码方式
	 * @return 返回网络资源的内容(字符串)
	 */
	public static String sendGetRequest(String urlPath, Map<String, String> params, String encoding) 
			throws Exception {

		// 使用StringBuilder对象
		StringBuilder sb = new StringBuilder(urlPath);
		sb.append('?');

		// 迭代Map
		for (Map.Entry<String, String> entry : params.entrySet()) {
			//解决请求参数中含有中文导致乱码问题
			sb.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), encoding)).append('&');
		}
		sb.deleteCharAt(sb.length() - 1);
		
		// 打开链接
		URL url = new URL(sb.toString());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "text/xml");
		conn.setRequestProperty("Charset", "utf-8");
		conn.setConnectTimeout(PARAM_CONNECT_TIMEOUT);
		// 如果请求响应码是200，则表示成功
		if (conn.getResponseCode() == 200) {
			// 获得服务器响应的数据
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));
			// 数据
			String retData = null;
			String responseData = "";
			while ((retData = in.readLine()) != null) {
				responseData += retData;
			}
			in.close();
			return responseData;
		}
		return "sendGetRequest error!";

	}

	/**
	 * @Description Get获取网络资源
	 * @param urlPath
	 *            网络资源的地址
	 * @param params
	 *            Get请求参数
	 * @param encoding
	 *            发送内容编码方式
	 * @return 返回下载结果
	 */
	public static String downloadFileWithGet(String urlPath, String dirPath, String filename, String encoding) 
			throws Exception {
		// 使用StringBuilder对象
		StringBuilder sb = new StringBuilder(urlPath);
		sb.append('?');
		//解决请求参数中含有中文导致乱码问题
		sb.append("filename").append('=').append(URLEncoder.encode(filename, encoding));
		
		// 打开链接
		URL url = new URL(sb.toString());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "image/png");
		conn.setRequestProperty("Charset", "utf-8");
		conn.setConnectTimeout(PARAM_CONNECT_TIMEOUT);
		// 如果请求响应码是200，则表示成功
		if (conn.getResponseCode() == 200) {
			// 获得服务器响应的数据
			InputStream in = conn.getInputStream();	
			File resultFile = write2SDFromInput(dirPath, filename, in);
			in.close();
			if (resultFile == null) {
				return "下载失败";
			}
			return "下载成功";
		}
		return "下载失败";
	}
	
	/**
	 * 将一个InputStream里面的数据写入到SD卡中
	 */
	private static File write2SDFromInput(String directory, String fileName,InputStream input) {
		File file = null;
		FileOutputStream output = null;
		File dir = new File(directory);
		if (!dir.exists()) {
			dir.mkdir();
		}
		try {
			file = new File(dir + File.separator + fileName);
			file.createNewFile();
			output = new FileOutputStream(file);
			byte buffer[] = new byte[1024];
			while ((input.read(buffer)) != -1) {
				output.write(buffer);
			}
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	
	/**
	 * @Description 获取网络资源
	 * @param urlStr
	 *            网络资源的地址
	 * @return 返回网络资源的内容
	 */
	public static String requestContentWithPost(String urlStr) {
		String content = null;
		if (urlStr != null && urlStr.startsWith("http://")) {
			URL url = null;
			HttpURLConnection conn = null;
			try {
				url = new URL(urlStr);
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(PARAM_CONNECT_TIMEOUT);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("accept", "*/*");
				conn.setRequestProperty("Charset", "UTF-8");
				conn.connect();
				int responseCode = conn.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					content = convertStream2String(conn.getInputStream());
				} else {
					content = "";
				}
			} catch (Exception e) {
				Log.d(TAG, "-> method=requestContentWithGet error-message: " + e.getMessage());
			} finally {
				if (conn != null) {
					conn.disconnect();
					conn = null;
				}
			}
		}
		return content;
	}
	
	/**
	 * 通过Post方式提交参数给服务器,也可以用来传送json或xml文件
	 */
	public static String sendPostRequest(String urlPath,Map<String, String> params, String encoding)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		// 如果参数不为空
		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				// Post方式提交参数的话，不能省略内容类型与长度
				sb.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), encoding)).append('&');
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		// 得到实体的二进制数据
		byte[] entitydata = sb.toString().getBytes();
		URL url = new URL(urlPath);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setConnectTimeout(PARAM_CONNECT_TIMEOUT);
		// 如果通过post提交数据，必须设置允许对外输出数据
		conn.setDoOutput(true);
		// 这里只设置内容类型与内容长度的头字段
		conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		// conn.setRequestProperty("Content-Type", "text/xml");
		conn.setRequestProperty("Charset", encoding);
		conn.setRequestProperty("Content-Length", String.valueOf(entitydata.length));
		OutputStream outStream = conn.getOutputStream();
		// 把实体数据写入是输出流
		outStream.write(entitydata);
		// 内存中的数据刷入
		outStream.flush();
		outStream.close();
		// 如果请求响应码是200，则表示成功
		if (conn.getResponseCode() == 200) {
			// 获得服务器响应的数据
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));
			// 数据
			String retData = null;
			String responseData = "";
			while ((retData = in.readLine()) != null) {
				responseData += retData;
			}
			in.close();
			return responseData;
		}
		return "sendPostRequest error!";
	}
	
	/**
	 * 在遇上HTTPS安全模式或者操作cookie的时候使用HTTPclient会方便很多 使用HTTPClient（开源项目）向服务器提交参数
	 */
	public static String sendHttpClientWithPost(String urlPath,Map<String, String> params, String encoding)
			throws Exception {
		// 需要把参数放到NameValuePair
		List<NameValuePair> paramPairs = new ArrayList<NameValuePair>();
		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				paramPairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
		}
		// 对请求参数进行编码，得到实体数据
		UrlEncodedFormEntity entitydata = new UrlEncodedFormEntity(paramPairs, encoding);
		// 构造一个请求路径
		HttpPost post = new HttpPost(urlPath);
		// 设置请求实体
		post.setEntity(entitydata);
		// 浏览器对象
		DefaultHttpClient client = new DefaultHttpClient();
		// 执行post请求
		HttpResponse response = client.execute(post);
		// 从状态行中获取状态码，判断响应码是否符合要求
		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, encoding);
			BufferedReader reader = new BufferedReader(inputStreamReader);// 读字符串用的。
			String s;
			String responseData = "";
			while (((s = reader.readLine()) != null)) {
				responseData += s;
			}
			reader.close();// 关闭输入流
			return responseData;
		}
		return "sendHttpClientPost error!";
	}
	
	/**
	 * 传送文本,例如Json,xml等
	 */
	public static String sendTxtWithPost(String urlPath, String txt, String encoding)
			throws Exception {
		byte[] sendData = txt.getBytes();
		URL url = new URL(urlPath);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setConnectTimeout(PARAM_CONNECT_TIMEOUT);
		// 如果通过post提交数据，必须设置允许对外输出数据
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "text/xml");
		conn.setRequestProperty("Charset", encoding);
		conn.setRequestProperty("Content-Length", String.valueOf(sendData.length));
		OutputStream outStream = conn.getOutputStream();
		outStream.write(sendData);
		outStream.flush();
		outStream.close();
		if (conn.getResponseCode() == 200) {
			// 获得服务器响应的数据
			BufferedReader in = new BufferedReader(new InputStreamReader(conn
					.getInputStream(), encoding));
			// 数据
			String retData = null;
			String responseData = "";
			while ((retData = in.readLine()) != null) {
				responseData += retData;
			}
			in.close();
			return responseData;
		}
		return "sendTxtWithPost error!";
	}
	
	/**
	 * 上传文件,这里不支持进度显示和断点续传         
     * @param urlPath 
     *            上传的服务器的路径 
     * @param filePath 
     *            需要上传的文件路径
     * @param newName 
     *            上传的文件名称，不填写将为需要上传文件表单中的名字
     * @throws IOException 
     */  
	public static String sendFileWithPost (String urlPath, String filePath,String newName)
			throws Exception {
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		
		StringBuilder sb = new StringBuilder();
		/** 
         * 上传文件的头 
         */  
        sb.append(twoHyphens + boundary + end);  
        sb.append("Content-Disposition: form-data; " + "name=\"uploadFile\";filename=\"" + newName + "\"" + end);  
 //     sb.append("Content-Type: image/png" + "\r\n");// 如果服务器端有文件类型的校验，必须明确指定ContentType  
        sb.append(end);  
  
        byte[] headerInfo = sb.toString().getBytes("UTF-8");  
        byte[] endInfo = (end + twoHyphens + boundary + twoHyphens + end).getBytes("UTF-8");

		URL url = new URL(urlPath);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		/* 允许Input、Output，不使用Cache */
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false);
		/* 设置传送的method=POST */
		con.setRequestMethod("POST");
		/* setRequestProperty */

		con.setRequestProperty("Connection", "Keep-Alive");
		con.setRequestProperty("Charset", "UTF-8");
		con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
		/* 设置OutputStream */
		OutputStream out = con.getOutputStream();		
		out.write(headerInfo);

		/* 取得文件的FileInputStream */
		FileInputStream in = new FileInputStream(filePath);
		/* 设置每次写入1024bytes */
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		int length = -1;
		/* 从文件读取数据至缓冲区 */
		while ((length = in.read(buffer)) != -1) {
			/* 将资料写入DataOutputStream中 */
			out.write(buffer, 0, length);
		}
		out.write(endInfo);

		/* close streams */
        in.close();  
     // 内存中的数据刷入
        out.flush();
        out.close();

		/* 取得Response内容 */     
		// 如果请求响应码是200，则表示成功
		if (con.getResponseCode() == 200) {
			// 获得服务器响应的数据
			BufferedReader bf = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
			// 数据
			String retData = null;
			String responseData = "";
			while ((retData = bf.readLine()) != null) {
				responseData += retData;
			}
			bf.close();
			return responseData;
		}  
		return "sendFileWithPost error!";
	}
	
	/**
	 * 根据URL直接读文件内容，前提是这个文件当中的内容是文本，函数的返回值就是文件当中的内容
	 */
	public static String readTxtFile(String urlStr, String encoding)
			throws Exception {
		StringBuffer sb = new StringBuffer();
		String line = null;
		BufferedReader buffer = null;
		try {
			// 创建一个URL对象
			URL url = new URL(urlStr);
			// 创建一个Http连接
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			// 使用IO流读取数据
			buffer = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), encoding));
			while ((line = buffer.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				buffer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * @Description 将输入流转化为字符串(InputStream->String)
	 * @param 要转换的输入流
	 * @return 返回转化后的字符串 (去除了空格)
	 */
	public static String convertStream2String(InputStream in) {
		if (in == null)
			return null;

		BufferedReader reader = null;
		StringBuffer sb = new StringBuffer();
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String temp = null;
			while ((temp = reader.readLine()) != null) {
				sb.append(temp.trim()); //删除空格, 避免发生解析错误
			}
		} catch (Exception e) {
			Log.d(TAG, "-> method=convertStream2String error-message: " + e.getMessage());
		} finally {
			try {
				if (reader != null) {
					reader.close();
					reader = null;
				}
				if (in != null) {
					in.close();
					in = null;
				}
			} catch (Exception e2) {
				Log.d(TAG, "-> method=convertStream2String error-message: " + e2.getMessage());
				reader = null;
				in = null;
			}
		}
		return sb.toString();
	}

	/**
	 * @Description 将输入流转化为字符串(InputStream->String)
	 * @param 要转换的输入流
	 * @return 返回转化后的字符串 (没有去除空格)
	 */
	public static String convertStream2String1(InputStream in) {
		if (in == null)
			return null;
		BufferedReader reader = null;
		StringBuffer sb = new StringBuffer();
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			CharBuffer cb = CharBuffer.allocate(1024 * 10);
			int len = -1;
			while ((len = reader.read(cb)) != -1) {
				sb.append(cb.array(), 0, len);
				cb.clear();
			}
		} catch (Exception e) {
			Log.d(TAG, "-> method=convertStream2String error-message: " + e.getMessage());
		} finally {
			try {
				if (reader != null) {
					reader.close();
					reader = null;
				}
				if (in != null) {
					in.close();
					in = null;
				}
			} catch (Exception e2) {
				Log.d(TAG, "-> method=convertStream2String error-message: " + e2.getMessage());
				reader = null;
				in = null;
			}
		}
		return sb.toString();
	}

	/**
	 * @Description 获取文件存储到本地
	 * @param url
	 * @param path
	 */
	public static void saveFileToLocal(String urlStr, String path, String encoding) {
		if (urlStr != null && urlStr.startsWith("http://")) {
			HttpClient client = null;
			HttpGet get = null;
			HttpResponse response = null;
			InputStream in = null;
			OutputStream out = null;
			try {
				BasicHttpParams params = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(params, 3000); //连接超时
				HttpConnectionParams.setSoTimeout(params, 3000); //等待数据超时
				client = new DefaultHttpClient(params);

				get = new HttpGet(urlStr);
				get.addHeader("Accept", "*/*");
				get.addHeader("Charset", encoding);

				response = client.execute(get);
				int responseCode = response.getStatusLine().getStatusCode();
				if (responseCode == HttpStatus.SC_OK) {
					in = response.getEntity().getContent();
					out = new FileOutputStream(new File(path));
					int len = -1;
					byte[] buffer = new byte[1024 * 2];
					while ((len = in.read(buffer)) != -1) {
						out.write(buffer, 0, len);
					}
					out.flush();
				} else {
					Log.d(TAG, "saveFileToLocal(\"" + urlStr + "\", \"" + path + "\") error-code: " + responseCode);
				}
			} catch (Exception e) {
				Log.d(TAG, "saveFileToLocal(\"" + urlStr + "\", \"" + path + "\") error: " + e.getMessage());
			} finally {
				try {
					if (in != null) {
						in.close();
						in = null;
					}
					if (out != null) {
						out.close();
						out = null;
					}
				} catch (Exception e2) {
					out = null;
					in = null;
					Log.d(TAG, "saveFileToLocal(\"" + urlStr + "\", \"" + path + "\") (close stream) error: " + e2.getMessage());
				}
			}
		}
	}

	/**
	 * GET请求到流的形式
	 */
	public static InputStream connect_(String urlStr) {
		InputStream content = null;
		if (urlStr != null && urlStr.startsWith("http://")) {
			HttpClient client = null;
			HttpGet get = null;
			HttpResponse response = null;
			try {
				BasicHttpParams params = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(params, 3000); //连接超时
				HttpConnectionParams.setSoTimeout(params, 3000); //等待数据超时
				client = new DefaultHttpClient(params);

				get = new HttpGet(urlStr);
				get.addHeader("Accept", "*/*");
				get.addHeader("Charset", "utf-8");

				response = client.execute(get);
				int responseCode = response.getStatusLine().getStatusCode();
				if (responseCode == HttpStatus.SC_OK) {
					content = response.getEntity().getContent();
				}
			} catch (Exception e) {
				Log.d(TAG, "-> method=requestContentWithGet error-message: " + e.getMessage());
			}
		}
		return content;
	}

	/**
	 * POST 请求
	 * 
	 * @param url
	 * @param parameters
	 */
	public static String HTTP_POST(String url, List<String> parameters) {
		HttpPost httpPost = new HttpPost(url);
		String result = "";
		// 设置HTTP POST请求参数必须用NameValuePair对象 
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (int i = 0; i < parameters.size(); i++) {
			params.add(new BasicNameValuePair(parameters.get(i).substring(0, parameters.get(i).indexOf("=")), parameters.get(i).substring(
					parameters.get(i).indexOf("=") + 1)));
		}

		//        params.add(new BasicNameValuePair("action", "downloadAndroidApp")); 
		//        params.add(new BasicNameValuePair("packageId", "89dcb664-50a7-4bf2-aeed-49c08af6a58a")); 
		//        params.add(new BasicNameValuePair("uuid", "test_ok1")); 

		HttpResponse httpResponse = null;
		try {
			// 设置httpPost请求参数 
			httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			httpResponse = new DefaultHttpClient().execute(httpPost);
			//System.out.println(httpResponse.getStatusLine().getStatusCode()); 
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				// 第三步，使用getEntity方法活得返回结果 
				result = EntityUtils.toString(httpResponse.getEntity());
				//System.out.println("result:" + result);  
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @Deprecated 崩溃发送
	 */

	public static String crashReport(String url, List<NameValuePair> params) {
		if (url == null || !url.startsWith("http://"))
			return "-1";
		HttpClient client = null;
		HttpPost post = null;
		HttpResponse response = null;
		String result = "-1";
		try {
			client = new DefaultHttpClient();
			// 请求超时
			client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
			// 读取超时
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);

			post = new HttpPost(url);
			post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			response = client.execute(post);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				result = HttpUtil.convertStream2String1(response.getEntity().getContent());
			}
		} catch (Exception e) {
			Log.d("crashreport", "crash report error: " + e.getMessage());
		}
		return result;
	}

	/**
	 * @Deprecated 判断地址是否有效
	 */
	public static int getHttpStatus(String urlStr) {

		int responseCode = -1;
		if (urlStr != null && urlStr.startsWith("http://")) {
			HttpClient client = null;
			HttpGet get = null;
			HttpResponse response = null;
			try {
				BasicHttpParams params = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(params, 3000); //连接超时
				HttpConnectionParams.setSoTimeout(params, 3000); //等待数据超时
				client = new DefaultHttpClient(params);

				get = new HttpGet(urlStr);
				get.addHeader("Accept", "*/*");
				get.addHeader("Charset", "utf-8");

				response = client.execute(get);
				responseCode = response.getStatusLine().getStatusCode();
			} catch (Exception e) {
				//				responseCode=404;

				Log.d(TAG, "-> method=requestContentWithGet error-message: " + e.getMessage());
			}
		}
		return responseCode;
	}
}
