package com.huaxun.tool;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;


/**
 看到了注释，一切都水落石出了，原来，如果在服务器的返回数据的header中没有指定字符集那么就会默认使用 ISO-8859-1 字符集。 
ISO-8859-1的别名叫做Latin1。这个字符集支持部分是用于欧洲的语言，不支持中文~ 
很不能理解为什么将这个字符集作为默认的字符集。Volley这个框架可是要用在网络通信的环境中的。 
吐槽也没有用，我们来看一下如何来解决中文乱码的问题。有以下几种解决方式：
在服务器的返回的数据的header的中contentType加上charset=UTF-8的声明。
当你无法修改服务器程序的时候，可以定义一个新的子类。覆盖parseNetworkResponse这个方法，直接使用UTF-8对服务器的返回数据进行转码。 
 */
public class CharsetStringRequest extends StringRequest {

  public CharsetStringRequest(String url, Listener<String> listener,
		  ErrorListener errorListener) {
      super(url, listener, errorListener);
  }

  public CharsetStringRequest(int method, String url, Listener<String> listener,
          ErrorListener errorListener) {
      super(method, url, listener, errorListener);
  }

  @Override
  protected Response<String> parseNetworkResponse(NetworkResponse response) {
      // TODO Auto-generated method stub
      String str = null;
      try {
          str = new String(response.data,"utf-8");
      } catch (UnsupportedEncodingException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      }
      return Response.success(str, HttpHeaderParser.parseCacheHeaders(response));
  }

}