/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hackathon.openrice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.google.android.glass.app.Card;
import com.google.android.glass.app.Card.ImageLayout;
import com.google.android.glass.widget.CardScrollView;
import com.wikitude.samples.LocationProvider;

/**
 * Creates a card scroll view with examples of different image layout cards.
 */
public final class CardsActivity extends Activity {

	
	private boolean isReceiverRegistered;

	  public JSONArray getJSONFromUrl(String url) {
		   InputStream is = null;
		   JSONArray jObj = null;
		   String json = "";		  
		    // Making HTTP request
		    try {
		      // defaultHttpClient
		      DefaultHttpClient httpClient = new DefaultHttpClient();
		      HttpPost httpPost = new HttpPost(url);
		      HttpResponse httpResponse = httpClient.execute(httpPost);
		      HttpEntity httpEntity = httpResponse.getEntity();
		      is = httpEntity.getContent();
		    } catch (UnsupportedEncodingException e) {
		      e.printStackTrace();
		    } catch (ClientProtocolException e) {
		      e.printStackTrace();
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		    try {
		      BufferedReader reader = new BufferedReader(new InputStreamReader(
		          is, "iso-8859-1"), 8);
		      StringBuilder sb = new StringBuilder();
		      String line = null;
		      while ((line = reader.readLine()) != null) {
		        sb.append(line + "n");
		      }
		      is.close();
		      json = sb.toString();
		    } catch (Exception e) {
		      Log.e("Buffer Error", "Error converting result " + e.toString());
		    }
		    // try parse the string to a JSON object
		    try {
		      jObj = new JSONArray(json);
		    } catch (JSONException e) {
		      Log.e("JSON Parser", "Error parsing data " + e.toString());
		    }
		    // return JSON String
		    return jObj;
		  }
	
	  public static String getMapUrl(double latitude, double longitude, double currentLat, double currentLon, int width, int height) {
	        try {
	            String raw = "https://maps.googleapis.com/maps/api/staticmap?sensor=false&size=" + width + "x" + height +
	                "&style=feature:all|element:all|saturation:-100|lightness:-25|gamma:0.5|visibility:simplified" +
	                "&style=feature:roads|element:geometry&style=feature:landscape|element:geometry|lightness:-25" +
	                "&markers=icon:" + URLEncoder.encode("http://mirror-api.appspot.com/glass/images/map_dot.png",
	                "UTF-8") + "|shadow:false|" + currentLat + "," + "" + currentLon+"&markers=color:0xF7594A|" + latitude + "," + longitude;
	            return raw.replace("|", "%7C");
	        } catch (UnsupportedEncodingException e) {
	            return null;
	        }
	    }	  
	  
	
	class RetrieveImage extends AsyncTask<String, Void, Void> {

	    private Exception exception;

	    protected Void doInBackground(String... urls) {
	    	    	
	        try {
	            JSONArray jsonArr = getJSONFromUrl(urls[0]);
	            poiData = jsonArr;
	        	
	            for (int i = 0; i <  jsonArr.length(); i++) {
	            	JSONObject jsonObj = (JSONObject) jsonArr.get(i);
	            	double x,y,lat,lon;
	                if (lastKnownLocation != null) {
	                	x = lastKnownLocation.getLatitude();
	                	y = lastKnownLocation.getLongitude();
	                } else {
	                	x = 22.3049989;
	                	y = 114.17925600000001;
	                }
	                lat = Double.parseDouble(jsonObj.getString("x"));
	                lon = Double.parseDouble(jsonObj.getString("y"));
	                String mapUrl = getMapUrl(lat, lon, x, y, 240, 320);
		            cards.add(getImagesCard(ctx, (String) jsonObj.get("img"), mapUrl)
		                    .setImageLayout(ImageLayout.FULL)
		                    .setText(new String(jsonObj.getString("name").getBytes("ISO-8859-1"), "UTF-8"))); //+ "\n" + (String) jsonObj.get("address")));	
	            }
	        } catch (Exception e) {
	            this.exception = e;
	        }
			return null;

	    }
	    protected void onPostExecute(Void result) {
	        Intent intent = new Intent ("com.ours.asyncisover");            

	        CardsActivity.this.sendBroadcast(intent);
	 }

	}	
	
	private Context ctx = this;
	
    private CardScrollView mCardScroller;

    private ArrayList<Card> cards = new ArrayList<Card>();
    
    private JSONArray poiData;

	private LocationListener locationListener;
	private Location lastKnownLocation;
	private LocationProvider locationProvider;
    
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle extras = getIntent().getExtras();
        String keyword = null;
        if (extras != null) {
	        ArrayList<String> text = extras.getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
	        if (text != null) {
		        for (String t : text) {
		        	String temp = t.toLowerCase().trim();
		        	if (temp.startsWith("search")) {
		        		keyword = temp.substring(6).trim();
		        	}
		        }
	        }
        }
        Card info = new Card(this);
        info.setText("Loading data...");
        cards.add(info);
        RetrieveImage task = new RetrieveImage();

		// listener passed over to locationProvider, any location update is handled here
		this.locationListener = new LocationListener() {

			@Override
			public void onStatusChanged( String provider, int status, Bundle extras ) {
			}

			@Override
			public void onProviderEnabled( String provider ) {
			}

			@Override
			public void onProviderDisabled( String provider ) {
			}

			@Override
			public void onLocationChanged( final Location location ) {
				// forward location updates fired by LocationProvider to architectView, you can set lat/lon from any location-strategy
				if (location!=null) {
				// sore last location as member, in case it is needed somewhere (in e.g. your adjusted project)
				lastKnownLocation = location;
				if ( lastKnownLocation != null ) {
					// check if location has altitude at certain accuracy level & call right architect method (the one with altitude information)
					if ( location.hasAltitude() && location.hasAccuracy() && location.getAccuracy()<7) {
						lastKnownLocation = location;
					} else {
						lastKnownLocation = location;
					}
				}
				}
			}
		};        
        locationProvider = new LocationProvider(this, locationListener);
        double x, y;
        if (lastKnownLocation != null) {
        	x = lastKnownLocation.getLatitude();
        	y = lastKnownLocation.getLongitude();
        } else {
        	x = 22.3049989;
        	y = 114.17925600000001;
        }
        if (keyword != null) {
        	//Log.d("FFFF", keyword);
        	task.execute("http://openricescraper.herokuapp.com/?x=" + x + "&y=" + y + "&keyword=" + Uri.encode(keyword));
        } else {
        	task.execute("http://openricescraper.herokuapp.com/?x=" + x + "&y=" + y);

        }
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(new CardAdapter(cards));
        mCardScroller.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
	    		final String className = "com.wikitude.samples.SampleCamActivity";

	    		try {

	    			final Intent intent = new Intent(ctx, Class.forName(className));
	    			intent.putExtra(EXTRAS_KEY_ACTIVITY_TITLE_STRING,
	    					"5.2 Adding Radar");
	    			intent.putExtra(EXTRAS_KEY_ACTIVITY_ARCHITECT_WORLD_URL, "samples/5_Browsing$Pois_2_Adding$Radar/index.html");
	    			if (poiData != null) {
		    			JSONArray pass = new JSONArray();
		    			final String ATTR_ID = "id";
		    			final String ATTR_NAME = "name";
		    			final String ATTR_DESCRIPTION = "description";
		    			final String ATTR_LATITUDE = "latitude";
		    			final String ATTR_LONGITUDE = "longitude";
		    			final String ATTR_ALTITUDE = "altitude";
		    			int i = position - 1;
			            //for (int i = 0; i <  poiData.length(); i++) {
			            	JSONObject jsonObj = (JSONObject) poiData.get(i);
			            	final HashMap<String, String> poiInformation = new HashMap<String, String>();
							poiInformation.put(ATTR_ID, String.valueOf(i));
							poiInformation.put(ATTR_NAME, new String(jsonObj.getString("name").getBytes("ISO-8859-1"), "UTF-8"));
							poiInformation.put(ATTR_DESCRIPTION, "No info");
							poiInformation.put(ATTR_LATITUDE, String.valueOf(jsonObj.get("x")));
							poiInformation.put(ATTR_LONGITUDE, String.valueOf(jsonObj.get("y")));
							final float UNKNOWN_ALTITUDE = -32768f;  // equals "AR.CONST.UNKNOWN_ALTITUDE" in JavaScript (compare AR.GeoLocation specification)
							// Use "AR.CONST.UNKNOWN_ALTITUDE" to tell ARchitect that altitude of places should be on user level. Be aware to handle altitude properly in locationManager in case you use valid POI altitude value (e.g. pass altitude only if GPS accuracy is <7m).
							poiInformation.put(ATTR_ALTITUDE, String.valueOf(UNKNOWN_ALTITUDE));	
							pass.put(new JSONObject(poiInformation));
			            //}	
			            intent.putExtra("poiData", pass.toString());   
	    			}
	    			
	    			/* launch activity */
	    			ctx.startActivity(intent);

	    		} catch (Exception e) {
	    			/*
	    			 * may never occur, as long as all SampleActivities exist and are
	    			 * listed in manifest
	    			 */
	    			Toast.makeText(ctx, className + "\nnot defined/accessible",
	    					Toast.LENGTH_SHORT).show();
	    		}	
				
			}
		});
        
        setContentView(mCardScroller);
        IntentFilter filter = new IntentFilter(); 
        filter.addAction("com.ours.asyncisover");  
        filter.addCategory("android.intent.category.DEFAULT");        
        registerReceiver(myBroadcastReceiver, filter);
        isReceiverRegistered = true;

    }
    private BroadcastReceiver myBroadcastReceiver =
            new BroadcastReceiver() {

				@Override
				public void onReceive(Context arg0, Intent arg1) {
					if (poiData.length() == 0) {
						cards.get(0).setText("No data :(");
					} else {
						cards.get(0).setText("Side-scroll to browse the results, tap to view them in AR");
					}
			        mCardScroller.getAdapter().notifyDataSetChanged();
			        ctx.unregisterReceiver(myBroadcastReceiver);
					
				}
               
           };

           
           
    
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            //e.printStackTrace();
            return null;
        }
    }   
    
    private Card getImagesCard(Context context, String... urls) {
        Card card = new Card(context);
        for (String url : urls) {
	        Bitmap image = getBitmapFromURL(url);
	        if (image != null) {
	        	card.addImage(image);
	        }
        }
        /*String stars;
        switch (rating) {
        case 0: stars = "http://i616.photobucket.com/albums/tt248/lindanmc/starrating-halfstar.jpg";
        	break;
        case 1: stars = "http://i616.photobucket.com/albums/tt248/lindanmc/starrating-1star.jpg";
        	break;
        case 2: stars = "http://i616.photobucket.com/albums/tt248/lindanmc/starrating-2star.jpg";
    	break;        	
        case 3: stars = "http://i616.photobucket.com/albums/tt248/lindanmc/starrating-3star.jpg";
    	break; 
        case 4: stars = "http://i616.photobucket.com/albums/tt248/lindanmc/starrating-4star.jpg";
    	break;        	    	    	
        case 5: stars = "http://i616.photobucket.com/albums/tt248/lindanmc/starrating-5star.jpg";
    	break;        	    	    	
    	default: stars = "http://i616.photobucket.com/albums/tt248/lindanmc/starrating-halfstar.jpg";
        }
        Bitmap rate = getBitmapFromURL(stars);
        if (rate != null) {
        	card.addImage(rate);
        	
        }

        	
        	http://i984.photobucket.com/albums/ae328/hobster70/Stars/starrating-halfstar.jpg
        	http://i984.photobucket.com/albums/ae328/hobster70/Stars/starrating-1star.jpg
        	http://i984.photobucket.com/albums/ae328/hobster70/Stars/starrating-2stars.jpg
        	http://i984.photobucket.com/albums/ae328/hobster70/Stars/starrating-2andahalfstars.jpg
        	http://i984.photobucket.com/albums/ae328/hobster70/Stars/starrating-3stars.jpg
        	http://i984.photobucket.com/albums/ae328/hobster70/Stars/starrating-3andahalfstars.jpg
        	http://i984.photobucket.com/albums/ae328/hobster70/Stars/starrating-4stars.jpg
        	http://i984.photobucket.com/albums/ae328/hobster70/Stars/starrating-4andahalfstars.jpg
        	http://i984.photobucket.com/albums/ae328/hobster70/Stars/starrating-5stars.jpg
        	*/
        
        /*card.addImage(R.drawable.codemonkey1);
        card.addImage(R.drawable.codemonkey2);
        card.addImage(R.drawable.codemonkey3);
        card.addImage(R.drawable.codemonkey4);
        card.addImage(R.drawable.codemonkey5);*/
        return card;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter(); 
            filter.addAction("com.ours.asyncisover");  
            filter.addCategory("android.intent.category.DEFAULT");           	
            registerReceiver(myBroadcastReceiver, filter);
            isReceiverRegistered = true;
        }
        mCardScroller.activate();
    }

    @Override
    protected void onPause() {
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(myBroadcastReceiver);
            } catch (IllegalArgumentException e) {
                // Do nothing
            }
            isReceiverRegistered = false;
        }    	
        mCardScroller.deactivate();
        super.onPause();
    }

	public static final String EXTRAS_KEY_ACTIVITY_TITLE_STRING = "activityTitle";
	public static final String EXTRAS_KEY_ACTIVITY_ARCHITECT_WORLD_URL = "activityArchitectWorldUrl";
     
}
