package com.huaxun.weather;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.huaxun.R;
import com.huaxun.db.WeatherDB;
import com.huaxun.dialog.LoadingDialog;
import com.huaxun.tool.BaseTools;
import com.huaxun.tool.CharsetStringRequest;
import com.huaxun.tool.VolleyTool;
import com.huaxun.utils.Util;

public class WeatherActivity extends Activity{
	private WeatherDB weatherDB;
	private ListView provice_list;
	private ListView city_list;
	private ListView county_list;
	private ViewPager viewpager;
	private ImageView weather_checked1, weather_checked2, weather_checked3;
	private TipsPagerAdapter vpadapter;
	private List<View> viewContainter = new ArrayList<View>();	

	private TextView title_text;
	private Button back;
	private Button location;
	private Button refresh_weather;
	//viewpager第一页显示
	private TextView city_name;
	private TextView publish_text;
	private TextView current_date;
	private TextView weather_desp;
	private TextView temp;
	private ImageView weather_icon;
	//viewpager第二页显示
	private TextView first_day_tv;
	private TextView first_day_week;
	private TextView first_tem_tv;
	private TextView first_weather_tv;
	private ImageView first_day_iv;
	private TextView second_day_tv;
	private TextView second_day_week;
	private TextView second_tem_tv;
	private TextView second_weather_tv;
	private ImageView second_day_iv;
	private TextView third_day_tv;
	private TextView third_day_week;
	private TextView third_tem_tv;
	private TextView third_weather_tv;
	private ImageView third_day_iv;
	//viewpager第二页显示
	private TextView fourth_day_tv;
	private TextView fourth_day_week;
	private TextView fourth_tem_tv;
	private TextView fourth_weather_tv;
	private ImageView fourth_day_iv;
	private TextView fiveth_day_tv;
	private TextView fiveth_day_week;
	private TextView fiveth_tem_tv;
	private TextView fiveth_weather_tv;
	private ImageView fiveth_day_iv;
	private TextView sixth_day_tv;
	private TextView sixth_day_week;
	private TextView sixth_tem_tv;
	private TextView sixth_weather_tv;
	private ImageView sixth_day_iv;
	
	private ProvinceAdapter provinceAdapter;
	private CityAdapter cityAdapter;
	private CountyAdapter countyAdapter;
	
	private LoadingDialog loadingDialog;
	
	// 省份集合
	private List<Province> provinces = new ArrayList<Province>();
	// 城市集合
	private List<City> cities = new ArrayList<City>();
	//县集合
	private List<County> counties = new ArrayList<County>();
	
	private Province selectedProvince;
	private City selectedCity;
	
	private final int updateWeatherDisplay = 1;
	
	Handler handler = new Handler() {
		public void handleMessage (Message msg) {
			switch (msg.what) {
			case updateWeatherDisplay:
				SharedPreferences pref = getSharedPreferences("weatherinfo", WeatherActivity.this.MODE_PRIVATE);
				try {
					//第一页
					publish_text.setText(pref.getString("date_y","")+"发布"); 
					current_date.setText(getDataStringDelay(0)+"/"+getWeekStringDelay(0));
					weather_desp.setText(pref.getString("weather1",""));
					temp.setText(pref.getString("temp1",""));
					city_name.setText(pref.getString("city",""));
					if (!pref.getString("img1","").isEmpty()){
						InputStream imageFile = getAssets().open("weather/" + pref.getString("img1","") + ".png");
						weather_icon.setImageBitmap(BitmapFactory.decodeStream(imageFile));
					}
					//第二页
					first_day_tv.setText(getDataStringDelay(0));
					first_day_week.setText(getWeekStringDelay(0));
					first_tem_tv.setText(pref.getString("temp1",""));
					first_weather_tv.setText(pref.getString("weather1",""));
					if (!pref.getString("img1","").isEmpty()){
						InputStream imageFile = getAssets().open("weather/" + pref.getString("img1","") + ".png");
						first_day_iv.setImageBitmap(BitmapFactory.decodeStream(imageFile));
					}
					second_day_tv.setText(getDataStringDelay(1));
					second_day_week.setText(getWeekStringDelay(1));
					second_tem_tv.setText(pref.getString("temp2",""));
					second_weather_tv.setText(pref.getString("weather2",""));
					if (!pref.getString("img3","").isEmpty()){
						InputStream imageFile = getAssets().open("weather/" + pref.getString("img3","") + ".png");
						second_day_iv.setImageBitmap(BitmapFactory.decodeStream(imageFile));
					}
					third_day_tv.setText(getDataStringDelay(2));
					third_day_week.setText(getWeekStringDelay(2));
					third_tem_tv.setText(pref.getString("temp3",""));
					third_weather_tv.setText(pref.getString("weather3",""));
					if (!pref.getString("img5","").isEmpty()){
						InputStream imageFile = getAssets().open("weather/" + pref.getString("img5","") + ".png");
						third_day_iv.setImageBitmap(BitmapFactory.decodeStream(imageFile));
					}
					//第三页
					fourth_day_tv.setText(getDataStringDelay(3));
					fourth_day_week.setText(getWeekStringDelay(3));
					fourth_tem_tv.setText(pref.getString("temp4",""));
					fourth_weather_tv.setText(pref.getString("weather4",""));
					if (!pref.getString("img7","").isEmpty()){
						InputStream imageFile = getAssets().open("weather/" + pref.getString("img7","") + ".png");
						fourth_day_iv.setImageBitmap(BitmapFactory.decodeStream(imageFile));
					}
					fiveth_day_tv.setText(getDataStringDelay(4));
					fiveth_day_week.setText(getWeekStringDelay(4));
					fiveth_tem_tv.setText(pref.getString("temp5",""));
					fiveth_weather_tv.setText(pref.getString("weather5",""));
					if (!pref.getString("img9","").isEmpty()){
						InputStream imageFile = getAssets().open("weather/" + pref.getString("img9","") + ".png");
						fiveth_day_iv.setImageBitmap(BitmapFactory.decodeStream(imageFile));
					}
					sixth_day_tv.setText(getDataStringDelay(5));
					sixth_day_week.setText(getWeekStringDelay(5));
					sixth_tem_tv.setText(pref.getString("temp6",""));
					sixth_weather_tv.setText(pref.getString("weather6",""));
					if (!pref.getString("img11","").isEmpty()){
						InputStream imageFile = getAssets().open("weather/" + pref.getString("img11","") + ".png");
						sixth_day_iv.setImageBitmap(BitmapFactory.decodeStream(imageFile));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}
	};
     
	 public void onCreate(Bundle savedInstanceState){
		  super.onCreate(savedInstanceState);
		  Util.setColorTranslucent(this, "#484e61");
		  
		  weatherDB = WeatherDB.getInstance();
		  setContentView(R.layout.weather_activity_layout);
		  initView();
		  provinceAdapter = new ProvinceAdapter();
		  provice_list.setAdapter(provinceAdapter);
		  cityAdapter = new CityAdapter();
		  city_list.setAdapter(cityAdapter);
		  countyAdapter = new CountyAdapter();
		  county_list.setAdapter(countyAdapter);
		  
		  provice_list.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				selectedProvince = provinces.get(position);
				provice_list.setVisibility(View.INVISIBLE);
				city_list.setVisibility(View.VISIBLE);
				county_list.setVisibility(View.INVISIBLE);
				title_text.setText(selectedProvince.getProvince_name());
				queryCities();
			}			  
		  });
		  
		  city_list.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				selectedCity = cities.get(position);
				provice_list.setVisibility(View.INVISIBLE);
				city_list.setVisibility(View.INVISIBLE);
				county_list.setVisibility(View.VISIBLE);
				title_text.setText(selectedCity.getCity_name());
				queryCounties();
			}			  
		  });
		  
		  county_list.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				title_text.setText(counties.get(position).getCounty_name());
				queryWeather(counties.get(position).getCounty_code());
			}			  
		  });
		  
		  refresh_weather.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				RotateAnimation anim = new RotateAnimation(0f,720f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
				anim.setDuration(1000);
				refresh_weather.startAnimation(anim);
				SharedPreferences pref = getSharedPreferences("weatherinfo", WeatherActivity.this.MODE_PRIVATE);
				//cityid其实就是County_code
				queryWeatherInfo(pref.getString("cityid",""));
			 }			  
		  });
		  
		  back.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					finish();
					WeatherActivity.this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
				 }			  
			  });
		  
		  location.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					loadingDialog = new LoadingDialog(WeatherActivity.this);
					loadingDialog.show();
					getLocation(WeatherActivity.this);  	
				 }			  
			  });
		  
		  title_text.setText("中国");
		  queryProvinces();
		  //每次进入这个activity就刷新一次天气
		  SharedPreferences prefrence = getSharedPreferences("weatherinfo", WeatherActivity.this.MODE_PRIVATE);
		  queryWeatherInfo(prefrence.getString("cityid","101020100"));
	 }  

	 
	 private void initView() {
		 provice_list = (ListView) this.findViewById(R.id.provice_list);
		 city_list = (ListView) this.findViewById(R.id.city_list);
		 county_list = (ListView) this.findViewById(R.id.county_list);
		 title_text = (TextView) this.findViewById(R.id.title_text);
		 back = (Button) this.findViewById(R.id.back);
		 location = (Button) this.findViewById(R.id.location);
		 refresh_weather = (Button) this.findViewById(R.id.refresh_weather);		 
		 viewpager = (ViewPager) this.findViewById(R.id.viewpager);
		 weather_checked1 = (ImageView) this.findViewById(R.id.weather_checked1);
		 weather_checked2 = (ImageView) this.findViewById(R.id.weather_checked2);
		 weather_checked3 = (ImageView) this.findViewById(R.id.weather_checked3);
     
	     View view1 = LayoutInflater.from(this).inflate(R.layout.weather_viewpager1_layout, null);
	     View view2 = LayoutInflater.from(this).inflate(R.layout.weather_viewpager2_layout, null);
	     View view3 = LayoutInflater.from(this).inflate(R.layout.weather_viewpager3_layout, null);
	      //viewpager开始添加view
	     viewContainter.add(view1);
	     viewContainter.add(view2);
	     viewContainter.add(view3);  
	     
	     vpadapter = new TipsPagerAdapter(viewContainter);
	     viewpager.setAdapter(vpadapter);
	     viewpager.setCurrentItem(0);
	     weather_checked1.setImageResource(R.drawable.weather_checked);
	     weather_checked2.setImageResource(R.drawable.weather_unchecked);
	     weather_checked3.setImageResource(R.drawable.weather_unchecked);
	     viewpager.setOnPageChangeListener(new OnPageChangeListener(){
			@Override
			public void onPageScrollStateChanged(int position) {	
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {				
			}

			@Override
			public void onPageSelected(int position) {	
				if (position == 0) {
					weather_checked1.setImageResource(R.drawable.weather_checked);
				    weather_checked2.setImageResource(R.drawable.weather_unchecked);
				    weather_checked3.setImageResource(R.drawable.weather_unchecked);
				} else if (position == 1) {
					weather_checked1.setImageResource(R.drawable.weather_unchecked);
				    weather_checked2.setImageResource(R.drawable.weather_checked);
				    weather_checked3.setImageResource(R.drawable.weather_unchecked);
				} else if (position == 2) {
					weather_checked1.setImageResource(R.drawable.weather_unchecked);
				    weather_checked2.setImageResource(R.drawable.weather_unchecked);
				    weather_checked3.setImageResource(R.drawable.weather_checked);
				}			
			}	    	 
	     });
	     
		 //第一页
		 publish_text = (TextView) view1.findViewById(R.id.publish_text);
		 current_date = (TextView) view1.findViewById(R.id.current_date);
		 weather_desp = (TextView) view1.findViewById(R.id.weather_desp);
		 temp = (TextView) view1.findViewById(R.id.temp);
		 city_name = (TextView) view1.findViewById(R.id.city_name);
		 weather_icon = (ImageView) view1.findViewById(R.id.weather_icon);
		 //第二页
		 first_day_tv = (TextView) view2.findViewById(R.id.first_day_tv);
		 first_day_week =(TextView) view2.findViewById(R.id.first_day_week);
		 first_tem_tv = (TextView) view2.findViewById(R.id.first_tem_tv);
		 first_weather_tv = (TextView) view2.findViewById(R.id.first_weather_tv);
		 first_day_iv = (ImageView) view2.findViewById(R.id.first_day_iv);
		 second_day_tv = (TextView) view2.findViewById(R.id.second_day_tv);
		 second_day_week =(TextView) view2.findViewById(R.id.second_day_week);
		 second_tem_tv = (TextView) view2.findViewById(R.id.second_tem_tv);
		 second_weather_tv = (TextView) view2.findViewById(R.id.second_weather_tv);
		 second_day_iv = (ImageView) view2.findViewById(R.id.second_day_iv);
		 third_day_tv = (TextView) view2.findViewById(R.id.third_day_tv);
		 third_day_week =(TextView) view2.findViewById(R.id.third_day_week);
		 third_tem_tv = (TextView) view2.findViewById(R.id.third_tem_tv);
		 third_weather_tv = (TextView) view2.findViewById(R.id.third_weather_tv);
		 third_day_iv = (ImageView) view2.findViewById(R.id.third_day_iv);
		 //第三页
		 fourth_day_tv = (TextView) view3.findViewById(R.id.fourth_day_tv);
		 fourth_day_week =(TextView) view3.findViewById(R.id.fourth_day_week);
		 fourth_tem_tv = (TextView) view3.findViewById(R.id.fourth_tem_tv);
		 fourth_weather_tv = (TextView) view3.findViewById(R.id.fourth_weather_tv);
		 fourth_day_iv = (ImageView) view3.findViewById(R.id.fourth_day_iv);
		 fiveth_day_tv = (TextView) view3.findViewById(R.id.fiveth_day_tv);
		 fiveth_day_week =(TextView) view3.findViewById(R.id.fiveth_day_week);
		 fiveth_tem_tv = (TextView) view3.findViewById(R.id.fiveth_tem_tv);
		 fiveth_weather_tv = (TextView) view3.findViewById(R.id.fiveth_weather_tv);
		 fiveth_day_iv = (ImageView) view3.findViewById(R.id.fiveth_day_iv);
		 sixth_day_tv = (TextView) view3.findViewById(R.id.sixth_day_tv);
		 sixth_day_week =(TextView) view3.findViewById(R.id.sixth_day_week);
		 sixth_tem_tv = (TextView) view3.findViewById(R.id.sixth_tem_tv);
		 sixth_weather_tv = (TextView) view3.findViewById(R.id.sixth_weather_tv);
		 sixth_day_iv = (ImageView) view3.findViewById(R.id.sixth_day_iv);
		 
	 }
	 
		/**
		 * 查询全国的省，优先从数据库查询，如果没有查询再到服务器上查询
		 */
		private void queryProvinces() {
			provinces = weatherDB.loadProvices();
			if (provinces.size() > 0) {
				provinceAdapter.notifyDataSetChanged();
				provice_list.setSelection(0);
				title_text.setText("中国");
			} else {
				queryFromServer(null, "province");
			}
		}
		
		/**
		 * 查询某省所有的市，优先从数据库查询，如果没有查询再到服务器上查询
		 */
		private void queryCities() {
			cities = weatherDB.loadCities(selectedProvince.getId());
			if (cities.size() > 0) {
				cityAdapter.notifyDataSetChanged();
				city_list.setSelection(0);
				title_text.setText(selectedProvince.getProvince_name());
			} else {
				queryFromServer(selectedProvince.getProvince_code(), "city");
			}
		}

		/**
		 * 查询某市所有的县，优先从数据库查询，如果没有查询再到服务器上查询
		 */
		private void queryCounties() {
			counties = weatherDB.loadCounties(selectedCity.getId());
			if (counties.size() > 0) {
				countyAdapter.notifyDataSetChanged();
				county_list.setSelection(0);
				title_text.setText(selectedCity.getCity_name());
			} else {
				queryFromServer(selectedCity.getCity_code(), "county");
			}
		}

		
		/**
		 * 根据传入的代号和数据查询省县市数据
		 * 
		 */
		private void queryFromServer(final String code, final String type) {
			if (loadingDialog==null || !loadingDialog.isShowing()) {
				loadingDialog = new LoadingDialog(this);
				loadingDialog.show();
		    }
			String address;
			if (!TextUtils.isEmpty(code)) {
				address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
			} else {
				address = "http://www.weather.com.cn/data/list3/city.xml";
			}
			CharsetStringRequest charsetStringRequest = new CharsetStringRequest(address,new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
							BaseTools.showlog("queryFromServer：response="+response);
							if ("province".equals(type)) {
								handleProvicesResponse(response);
							} else if ("city".equals(type)) {
							    handleCitiesResponse(response, selectedProvince);
							} else if ("county".equals(type)) {
							    handleCountiesResponse(response, selectedCity);
							}
						}
					}, new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							Toast.makeText(getApplicationContext(),"加载数据失败！", Toast.LENGTH_SHORT).show();
							if (loadingDialog!=null && loadingDialog.isShowing()) {
								loadingDialog.dismiss();
			    		    }
						}
					});
			VolleyTool.getInstance(this).getmRequestQueue().add(charsetStringRequest);
		}

		private void handleProvicesResponse(String response) {
			if (!TextUtils.isEmpty(response)) {
				String[] allProvices = response.split(",");
				if (allProvices != null && allProvices.length > 0) {
					for (String p : allProvices) {
						String[] array = p.split("\\|");
						Province province = new Province();
						province.setProvince_code(array[0]);				
						province.setProvince_name(array[1]);
						weatherDB.saveProvice(province);
						provinces.add(province);
						provinceAdapter.notifyDataSetChanged();
						if (loadingDialog!=null && loadingDialog.isShowing()) {
							loadingDialog.dismiss();
		    		    }
					}
				}
			}
		}

		/**
		 * 处理服务端返回的市级json数据
		 */
		private void handleCitiesResponse(String response, Province province) {

			if (!TextUtils.isEmpty(response)) {
				String[] allCities = response.split(",");
				if (allCities != null && allCities.length > 0) {
					for (String c : allCities) {
						String[] array = c.split("\\|");
						City city = new City();
						city.setCity_code(array[0]);
						city.setCity_name(array[1]);
						city.setProvince(province);
						weatherDB.saveCity(city);
						cities.add(city);
						cityAdapter.notifyDataSetChanged();
						if (loadingDialog!=null && loadingDialog.isShowing()) {
							loadingDialog.dismiss();
		    		    }
					}
				}
			}
		}

		/**
		 * 处理服务端返回的县级json数据
		 */
		private void handleCountiesResponse(String response, City city) {
			if (!TextUtils.isEmpty(response)) {
				String[] allCounties = response.split(",");
				if (allCounties != null && allCounties.length > 0) {
					for (String c : allCounties) {
						String[] array = c.split("\\|");
						County county = new County();
						county.setCounty_code(array[0]);
						county.setCounty_name(array[1]);
						county.setCity(city);
						weatherDB.saveCounty(county);
						counties.add(county);
						countyAdapter.notifyDataSetChanged();
						if (loadingDialog!=null && loadingDialog.isShowing()) {
							loadingDialog.dismiss();
		    		    }
					}
				}
			}
		}
		
		private void queryWeather(String county_code) {
			if (loadingDialog==null || !loadingDialog.isShowing()) {
				loadingDialog = new LoadingDialog(this);
				loadingDialog.show();
		    }
			BaseTools.showlog("queryWeather：county_code="+county_code);
			String address = "http://www.weather.com.cn/data/list3/city" + county_code + ".xml";
			CharsetStringRequest charsetStringRequest = new CharsetStringRequest(address,new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {					
					BaseTools.showlog("queryWeather：response="+response);
					String[] array = response.split("\\|");
					String city_id = array[1];
					queryWeatherInfo(city_id);
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					Toast.makeText(WeatherActivity.this,"获取天气代码失败！", Toast.LENGTH_SHORT).show();
					if (loadingDialog!=null && loadingDialog.isShowing()) {
						loadingDialog.dismiss();
	    		    }
				}
			});
	        VolleyTool.getInstance(this).getmRequestQueue().add(charsetStringRequest);
		}
		
		private void queryWeatherInfo(String city_id) {			
			if (loadingDialog==null || !loadingDialog.isShowing()) {
				loadingDialog = new LoadingDialog(this);
				loadingDialog.show();
		    }
			BaseTools.showlog("queryWeather：city_id="+city_id);
			//中央气象局台的天气数据API接口
			//String address = "http://www.weather.com.cn/data/cityinfo/" + city_id + ".html";
			//中央天气预报：
			String address = "http://weather.51wnl.com/weatherinfo/GetMoreWeather?cityCode=" + city_id + "&weatherType=0";		
			JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Method.GET,address,null,new Response.Listener<JSONObject>() {  
	            @Override
	            public void onResponse(JSONObject response) {
	            	BaseTools.showlog("queryWeatherInfo：response="+response);       	
	            	Weather weather = getWeatherDataByJson(response.toString());
	            	if (weather.weatherinfo!=null) {
		            	saveWeatherInfo(weather.weatherinfo);
		            	handler.sendEmptyMessage(updateWeatherDisplay);
		            	//发送广播通知左侧菜单天气更新
		            	Intent updateIntent = new Intent("update_weather");
		        		WeatherActivity.this.sendBroadcast(updateIntent);
	            	}	            	
					if (loadingDialog!=null && loadingDialog.isShowing()) {
						loadingDialog.dismiss();
	    		    }
	            }  
	        },new Response.ErrorListener() { 
	            public void onErrorResponse(VolleyError error) { 
	            	Toast.makeText(WeatherActivity.this,"加载天气失败！", Toast.LENGTH_SHORT).show();
					if (loadingDialog!=null && loadingDialog.isShowing()) {
						loadingDialog.dismiss();
	    		    }
	            }  
	        }); 
	        VolleyTool.getInstance(this).getmRequestQueue().add(jsonObjectRequest);
		}
		
		private Weather getWeatherDataByJson(String json) {
			Weather data = null;
			try {
				Gson g = new Gson();
				data = g.fromJson(json, Weather.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return data;
		}
		
		private void saveWeatherInfo(WeatherInfo weatherInfo){
			SharedPreferences sharedPreferences = getSharedPreferences("weatherinfo", this.MODE_PRIVATE);
			Editor editor =  sharedPreferences.edit();
			editor.putString("weather1", weatherInfo.weather1);
			editor.putString("weather2", weatherInfo.weather2);
			editor.putString("weather3", weatherInfo.weather3);
			editor.putString("weather4", weatherInfo.weather4);
			editor.putString("weather5", weatherInfo.weather5);
			editor.putString("weather6", weatherInfo.weather6);
			editor.putString("temp1", weatherInfo.temp1);
			editor.putString("temp2", weatherInfo.temp2);
			editor.putString("temp3", weatherInfo.temp3);
			editor.putString("temp4", weatherInfo.temp4);
			editor.putString("temp5", weatherInfo.temp5);
			editor.putString("temp6", weatherInfo.temp6);
			editor.putString("img1", weatherInfo.img1);
			editor.putString("img2", weatherInfo.img2);
			editor.putString("img3", weatherInfo.img3);
			editor.putString("img4", weatherInfo.img4);
			editor.putString("img5", weatherInfo.img5);
			editor.putString("img6", weatherInfo.img6);
			editor.putString("img7", weatherInfo.img7);
			editor.putString("img8", weatherInfo.img8);
			editor.putString("img9", weatherInfo.img9);
			editor.putString("img10", weatherInfo.img10);
			editor.putString("img11", weatherInfo.img11);
			editor.putString("img12", weatherInfo.img12);
			editor.putString("city", weatherInfo.city);
			editor.putString("cityid", weatherInfo.cityid);
			editor.putString("date_y", weatherInfo.date_y);
			editor.commit();
		}
		
		//得到当前时间推迟days天后的日期字符串
		private String getDataStringDelay(int days){
			SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日");       
			Date curDate = new Date(System.currentTimeMillis() + days*24*60*60*1000);//获取当前时间       
			String str=formatter.format(curDate);
			return str;
		}  
		
		//得到当前时间推迟days天后是星期几
		private String getWeekStringDelay(int days){
		    final Calendar c = Calendar.getInstance();  
		    c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		    int mCur = Integer.valueOf(c.get(Calendar.DAY_OF_WEEK)); 
		    int mCal = (mCur + days) % 7;		    
		    String mWay = "";
		    if(mCal == 1){
		         mWay ="天";  
		    }else if(mCal == 2){
		         mWay ="一";  
		    }else if(mCal == 3){  
		         mWay ="二";  
		    }else if(mCal == 4){  
		         mWay ="三";  
		    }else if(mCal == 5){  
		         mWay ="四";  
		    }else if(mCal == 6){  
		         mWay ="五";  
		    }else if(mCal == 0){
		         mWay ="六";  
		    }  
		    return "星期"+mWay;  
		}

		private LocationManager locationManager;
		private String provider;
		public void getLocation(Context context) {
			// 获取位置管理服务
			 locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		     if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
		          Toast.makeText(this, "请开启GPS定位", Toast.LENGTH_SHORT).show();
		          Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);   
		          startActivityForResult(intent,0);
		          if (loadingDialog!=null && loadingDialog.isShowing()) {
						loadingDialog.dismiss();
				    }
		          return;
		      }	
			// 查找到服务信息
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
			criteria.setAltitudeRequired(false);
			criteria.setBearingRequired(false);
			criteria.setCostAllowed(true);
			criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
			//打印出可用的提供商
			List<String> providerlist = locationManager.getProviders(true);
			 for(int i = 0 ; i < providerlist.size(); i ++){  
			       BaseTools.showlog("(" +i +")" + providerlist.get(i)); 
			    }

			provider = locationManager.getBestProvider(criteria, true);
			BaseTools.showlog("provider=" + provider);
			Location location = null;
			try {
				location = locationManager.getLastKnownLocation(provider);
				if(location==null){
	                location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
	            }
				if (location != null) {
//					location.getLatitude();
//					location.getLongitude();
					getCurrentCity(location);
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 设置监听器，自动更新的最小时间为间隔N秒或最小位移变化超过N米
			//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
			if (loadingDialog!=null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
		    }
        	Toast.makeText(WeatherActivity.this,"定位失败！", Toast.LENGTH_SHORT).show();
			return;
		}
		
	    private LocationListener locationListener = new LocationListener() {        
	        /**
	         * 位置信息变化时触发
	         */
	        public void onLocationChanged(Location location) {
	        	BaseTools.showlog("onLocationChanged"); 
	        	getCurrentCity(location);
	        	locationManager.removeUpdates(this);

	        }	        
	        /**
	         * GPS状态变化时触发
	         */
	        public void onStatusChanged(String provider, int status, Bundle extras) {
	        	BaseTools.showlog("onStatusChanged"); 
	        	switch (status) {
	            //GPS状态为可见时
	            case LocationProvider.AVAILABLE:
	                break;
	            //GPS状态为服务区外时
	            case LocationProvider.OUT_OF_SERVICE:
	                break;
	            //GPS状态为暂停服务时
	            case LocationProvider.TEMPORARILY_UNAVAILABLE:
	                break;
	            }
	        }	    
	        /**
	         * GPS开启时触发
	         */
	        public void onProviderEnabled(String provider) {
	        	BaseTools.showlog("onProviderEnabled"); 	        	
	        }	    
	        /**
	         * GPS禁用时触发
	         */
	        public void onProviderDisabled(String provider) {
	        	BaseTools.showlog("onProviderDisabled"); 
	        }
	    };
	
		public void getCurrentCity(Location location) {
			try {
				//BaseTools.showlog("location.getLatitude()="+location.getLatitude()+"location.getLongitude()" + location.getLongitude()); 
				String url = "http://api.map.baidu.com/geocoder?output=json&location=" + location.getLatitude() + ","+ location.getLongitude();
				JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null,new Response.Listener<JSONObject>() {  
		            @Override
		            public void onResponse(JSONObject response) {		    			
						try {
							//BaseTools.showlog("response="+response.toString());
							JSONObject result_json = response.getJSONObject("result");
							JSONObject address_json = result_json.getJSONObject("addressComponent");
				    		String temp = address_json.getString("city");
				    		String city = temp.substring(0, temp.length() - 1);
				    		//BaseTools.showlog("city="+city);
				    		Toast.makeText(WeatherActivity.this, response.toString(), Toast.LENGTH_LONG).show();
				    		//Toast.makeText(WeatherActivity.this, "当前定位城市："+city, Toast.LENGTH_SHORT).show();
				    		getCurrentCountyCodeByName(city);	    					    				            	
						} catch (JSONException e) {
							e.printStackTrace();
						} finally {
							if (loadingDialog!=null && loadingDialog.isShowing()) {
								loadingDialog.dismiss();
			    		    }
						}
		            }  
		        },new Response.ErrorListener() { 
		            public void onErrorResponse(VolleyError error) {
		            	if (loadingDialog!=null && loadingDialog.isShowing()) {
							loadingDialog.dismiss();
		    		    }
		            	Toast.makeText(WeatherActivity.this,"获取当前城市失败！", Toast.LENGTH_SHORT).show();
		            }  
		        }); 
		        VolleyTool.getInstance(this).getmRequestQueue().add(jsonObjectRequest);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private void getCurrentCountyCodeByName(String name) {
			List<County> countylist = WeatherDB.getInstance().loadAllCounties();
			for(County county : countylist) {
				//BaseTools.showlog("getCounty_name="+county.getCounty_name());	
					if (county.getCounty_name().equals(name)) {
						String currentCountyCode = county.getCounty_code();
						//BaseTools.showlog("currentCountyCode="+currentCountyCode);
						queryWeather(currentCountyCode);
						return;
					}
				}
			return;
		}
	 
	class ProvinceAdapter extends BaseAdapter {
		 LayoutInflater inflater = null;			
		 public ProvinceAdapter() {
			inflater = LayoutInflater.from(WeatherActivity.this);
		 }

		 public int getCount() {
			return provinces.size();
		 }

		 public Object getItem(int position) {
			return provinces.get(position);
		 }

		 public long getItemId(int position) {
			return position;
		 }

		 public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.provice_list_item, null);
				holder.provinceName = (TextView) convertView.findViewById(R.id.provice_name);
				holder.right_array = (ImageView) convertView.findViewById(R.id.provice_array_right);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.provinceName.setText(provinces.get(position).getProvince_name());
			holder.right_array.setImageResource(R.drawable.array_right_blue);			
			return convertView;
			}

			class ViewHolder {
				TextView provinceName = null;
				ImageView right_array = null;
			}
		}

	class CityAdapter extends BaseAdapter {
		 LayoutInflater inflater = null;			
		 public CityAdapter() {
			inflater = LayoutInflater.from(WeatherActivity.this);
		 }

		 public int getCount() {
			return cities.size();
		 }

		 public Object getItem(int position) {
			return cities.get(position);
		 }

		 public long getItemId(int position) {
			return position;
		 }

		 public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.city_list_item, null);
				holder.cityName = (TextView) convertView.findViewById(R.id.city_name);
				holder.left_array = (ImageView) convertView.findViewById(R.id.city_array_left);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.cityName.setText(cities.get(position).getCity_name());
			holder.left_array.setImageResource(R.drawable.array_left_blue);			
			holder.left_array.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					provice_list.setVisibility(View.VISIBLE);
					city_list.setVisibility(View.INVISIBLE);
					county_list.setVisibility(View.INVISIBLE);
					title_text.setText("中国");
				}
			});
			return convertView;
			}

			class ViewHolder {
				TextView cityName = null;
				ImageView left_array = null;
			}
		}		

	class CountyAdapter extends BaseAdapter {
		 LayoutInflater inflater = null;			
		 public CountyAdapter() {
			inflater = LayoutInflater.from(WeatherActivity.this);
		 }

		 public int getCount() {
			return counties.size();
		 }

		 public Object getItem(int position) {
			return counties.get(position);
		 }

		 public long getItemId(int position) {
			return position;
		 }

		 public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.county_list_item, null);
				holder.countyName = (TextView) convertView.findViewById(R.id.county_name);
				holder.left_array = (ImageView) convertView.findViewById(R.id.county_array_left);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.countyName.setText(counties.get(position).getCounty_name());
			holder.left_array.setImageResource(R.drawable.array_left_blue);			
			holder.left_array.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					provice_list.setVisibility(View.INVISIBLE);
					city_list.setVisibility(View.VISIBLE);
					county_list.setVisibility(View.INVISIBLE);
					title_text.setText(selectedProvince.getProvince_name());
				}
			});
			return convertView;
			}

			class ViewHolder {
				TextView countyName = null;
				ImageView left_array = null;
			}
		}
	
	public class TipsPagerAdapter extends PagerAdapter {

		private List<View> viewContainter = null;

		public TipsPagerAdapter(List<View> viewContainter) {
			this.viewContainter = viewContainter;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(viewContainter.get(position));
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return super.getPageTitle(position);
		}

		@Override
		public Object instantiateItem(View container, int position) {
			try {
				((ViewPager) container).addView(viewContainter.get(position % viewContainter.size()), 0);
			} catch (Exception e) {

			}
			// ((ViewPager) container).addView(listViews.get(position));
			// return listViews.get(position);
			return viewContainter.get(position % viewContainter.size());
		}

	}
	
}
