package com.example.open_data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationListener
{
	private TextView tv_Location;
	private TextView tv_OpenData;
	private boolean getService =false;//是否已開啟定位服務
	private  LocationManager lms;
	private  Location location;
	private String bestProvider=LocationManager.GPS_PROVIDER;
	private Double longitude,latitude;
	String s1,s2,s4;
	int s3;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv_Location=(TextView)findViewById(R.id.tv_location);
		tv_OpenData=(TextView)findViewById(R.id.tv_json);
	//	new NewTaipeiHttpAsynoTask().execute("http://data.ntpc.gov.tw/NTPC/od/data/api/1040400255/?$format=json");//新北市
	//http://data.taipei.gov.tw/opendata/apply/query/MzVERDUyOTItNjI1NC00NjcyLUE3OEItNDY3ODhDMURFM0Yy?$format=json
		//new HttpAsynoTask().execute("http://data.taipei.gov.tw/opendata/apply/query/MzVERDUyOTItNjI1NC00NjcyLUE3OEItNDY3ODhDMURFM0Yy?$format=json");//台北市
		//取得系統定位服務
				LocationManager status=(LocationManager)(this.getSystemService(Context.LOCATION_SERVICE));
				if(status.isProviderEnabled(LocationManager.GPS_PROVIDER) || status.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
				{
					//如果GPS或網路定位開啟,呼叫locationServiceInitial()更新位置
					locationServiceInitial();
				}
				else
				{
					Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
					getService=true;//確定開啟定位服務
					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
				new HttpAsynoTask().execute("http://maps.googleapis.com/maps/api/geocode/json?latlng="+latitude+","+longitude+"&sensor=false&language=zh-tw");
//				new HttpAsynoTask().execute("http://maps.googleapis.com/maps/api/geocode/json?latlng=25.047908,121.517315&sensor=false&language=zh-tw");
				
				
	
	
		}
	private  class HttpAsynoTask extends AsyncTask<String, Void, String>
	{

		@Override
		protected void onPostExecute(String result)
		{
		     try 
		    {
		    	   
		    	 	JSONArray jsonObjs =  new  JSONObject(result).getJSONArray( "results" );  
		            //取出數組中第一個json對象(本示例數組中實際只包含一個元素)  
		            JSONObject jsonObj = jsonObjs.getJSONObject( 0 );  
		            //解析得formatted_address值  
		            String address = jsonObj.getString( "formatted_address" ); //247台灣新北市蘆洲區三民路42號
		            
		           // s1=address.substring(5, 8);//新北市
		          //  s2=address.substring(8, 11);//新北市
			        s3=address.indexOf("台");//3
			        
		            s1=address.substring(s3+2, s3+5);//新北市
		            s2=address.substring(s3+5, s3+8);//蘆洲區
		            
		            tv_Location.setText(s1+":"+s2+":"+s3);
		           // tv_Location.setText(address);//s1:新北市,s2:蘆洲區,s3抓取台這個位置
					if(s1.equals("新北市")) 
					{
						Toast.makeText(getBaseContext(), "你現在位置在新北市內", Toast.LENGTH_LONG).show();
							new NewTaipeiHttpAsynoTask().execute("http://data.ntpc.gov.tw/NTPC/od/data/api/1040400255/?$format=json");//新北市
						
					}
					else
					{
						Toast.makeText(getBaseContext(), "你現在位置在台北市內", Toast.LENGTH_LONG).show();
					}
		   
		    }
		     
		     catch (JSONException e) 
		     {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}

		@Override
		protected String doInBackground(String...urls) 
		{
			// TODO Auto-generated method stub
			return  GET(urls[0]);
		}

		private String GET(String url) 
		{
			InputStream inputStream=null;
			String result="";
			try 
			{
				HttpClient httpclient=new DefaultHttpClient();
				HttpResponse httpResponse=httpclient.execute(new HttpGet(url));
				inputStream=httpResponse.getEntity().getContent();
				if(inputStream!=null)
				{
					result=covertInputStreamToString(inputStream);
				}
				else
				{
					result="Did not work";
				}
			} 
			catch (ClientProtocolException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			
			return result;
		}

		private String covertInputStreamToString(InputStream inputStream) throws IOException 
		{
			BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
			String line="";
			String result="";
			// TODO Auto-generated method stub
			while((line=bufferedReader.readLine())!=null)
			{
				result+=line;
			}
			return result;
		}
		
		
	}
	
	@Override
	protected void onResume() 
	{
		// TODO Auto-generated method stub
		super.onResume();
		if(getService)
		{
			lms.requestLocationUpdates(bestProvider, 1000, 1, this);
		}
	}

	@Override
	protected void onPause() 
	{
		// TODO Auto-generated method stub
		super.onPause();
		if(getService)
		{
			lms.removeUpdates(this);
		}
	}

	private void locationServiceInitial() 
	{
		// TODO Auto-generated method stub
		lms=(LocationManager)getSystemService(LOCATION_SERVICE);//取得系統定位服務
		Criteria criteria=new Criteria();
		bestProvider=lms.getBestProvider(criteria, true);
		Location location=lms.getLastKnownLocation(bestProvider);
		getLocation(location);
		
	}
	private void getLocation(Location location) 
	{
		// TODO Auto-generated method stub
		if(location!=null)
		{
				longitude=location.getLongitude();
				latitude=location.getLatitude();
		}
		else
		{
			Toast.makeText(this, "無法定位服務",Toast.LENGTH_LONG).show();
		}
		
	}
	private  class NewTaipeiHttpAsynoTask extends AsyncTask<String, Void, String>
	{

		@Override
		protected String doInBackground(String... urls)
		{
			// TODO Auto-generated method stub
			return GET(urls[0]);
		}

		@Override
		protected void onPostExecute(String result) 
		{
		
			//int count=0;
			 try 
			 {
				JSONArray jsonArray = new JSONArray(result);
				String address="";
				String []All_add=new String [jsonArray.length()];
				for (int i = 0; i <jsonArray.length(); i++) 
				{
		            JSONObject jsonObject = jsonArray.getJSONObject(i);
		            String Add = jsonObject.getString("Add");
		            //All_add[i]=Add;
		            Log.i("Entry", "name: " + Add );
		         //   Toast.makeText(getApplicationContext(), Add,Toast.LENGTH_SHORT).show();
		            StringBuffer sb=new StringBuffer(Add);
		            All_add[i]=sb.substring(3, 6);
		            Toast.makeText(getApplicationContext(), All_add[i], Toast.LENGTH_SHORT).show();
		            // count++;
				
				}
			
				
				tv_OpenData.setText(address);
			
			 }
			 
			 catch (JSONException e)
			 {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
	
	          
			  
		}
		public String substring1(int i,int j)
		{
			return new String(this.substring(i,j));
		}

	

		private String GET(String url) 
		{
			InputStream inputStream=null;
			String result="";
			try 
			{
				HttpClient httpclient=new DefaultHttpClient();
				HttpResponse httpResponse=httpclient.execute(new HttpGet(url));
				inputStream=httpResponse.getEntity().getContent();
				if(inputStream!=null)
				{
					result=covertInputStreamToString(inputStream);
				}
				else
				{
					result="Did not work";
				}
			} 
			catch (ClientProtocolException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			
			return result;
		}

		private String covertInputStreamToString(InputStream inputStream) throws IOException 
		{
			BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
			String line="";
			String result="";
			// TODO Auto-generated method stub
			while((line=bufferedReader.readLine())!=null)
			{
				result+=line;
			}
			return result;
		}
		
	}
	private  class TaipeiHttpAsynoTask extends AsyncTask<String, Void, String>
	{

		@Override
		protected String doInBackground(String... urls)
		{
			// TODO Auto-generated method stub
			return GET(urls[0]);
		}

		@Override
		protected void onPostExecute(String result) 
		{
		
			//int count=0;
			 try 
			 {
				JSONArray jsonArray = new JSONArray(result);
			
			 }
			 
			 catch (JSONException e)
			 {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			          
			  
		}

	

		private String GET(String url) 
		{
			InputStream inputStream=null;
			String result="";
			try 
			{
				HttpClient httpclient=new DefaultHttpClient();
				HttpResponse httpResponse=httpclient.execute(new HttpGet(url));
				inputStream=httpResponse.getEntity().getContent();
				if(inputStream!=null)
				{
					result=covertInputStreamToString(inputStream);
				}
				else
				{
					result="Did not work";
				}
			} 
			catch (ClientProtocolException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			
			return result;
		}

		private String covertInputStreamToString(InputStream inputStream) throws IOException 
		{
			BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
			String line="";
			String result="";
			// TODO Auto-generated method stub
			while((line=bufferedReader.readLine())!=null)
			{
				result+=line;
			}
			return result;
		}
		
	}
	@Override
	public void onLocationChanged(Location location) 
	{
		// TODO Auto-generated method stub
		getLocation(location);
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) 
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderEnabled(String provider) 
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderDisabled(String provider)
	{
		// TODO Auto-generated method stub
		
	}



}
