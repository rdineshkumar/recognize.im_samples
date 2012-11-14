package pl.itraff.TestApi.ItraffApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * AsyncTask used to get response from api. In PostExecute it
 * retruns:
 * 
 * <pre>
 * {@code
 * 
 * 	Message msg = new Message();
 * 	Bundle data = new Bundle();
 * 	data.putInt(ItraffApi.STATUS, status);
 * 
 * 	data.putString(ItraffApi.RESPONSE, response);
 * 
 * 	msg.setData(data);
 * 	itraffApiHandler.sendMessage(msg);
 * }
 * </pre>
 * <p>
 *  <pre>
 * {@code
 * Where STATUS is Integer:
 * < 0 application error
 * 0 status ok
 * > 0 api error
 * 
 * and RESPONSE is json containing whole response from api
 * }
 * </pre>
 */
public class ItraffApiPostPhoto extends AsyncTask<Void, Void, Integer> {

	private String TAG = "SimplePhotoRecognition";
	Boolean debug = false;

	String response;
	HttpPost httpPost;
	Handler itraffApiHandler;

	public ItraffApiPostPhoto(Handler itraffApiHandler, HttpPost httpPost, String debugTag, Boolean debug) {
		this.httpPost = httpPost;
		this.itraffApiHandler = itraffApiHandler;
		this.TAG = debugTag;
		this.debug = debug;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		Integer status = -1;

		// post photo and get json response
		response = post();
		if (response != null) {
			log("RESPONSE:");
			log(response);

			try {
				JSONObject jsonObject = new JSONObject(response);
				status = jsonObject.getInt(ItraffApi.STATUS);
				log("" + status);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return status;
	}

	@Override
	protected void onPostExecute(Integer status) {
		super.onPostExecute(status);
		// callback to method in MainActivity with api response
		if (itraffApiHandler != null) {
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putInt(ItraffApi.STATUS, status);
			data.putString(ItraffApi.RESPONSE, response);

			msg.setData(data);
			itraffApiHandler.sendMessage(msg);
		}
	}

	private String post() {
		HttpClient httpClient = ItraffApiHttpClient.getHttpClient();
		BufferedReader in = null;
		try {
			log(httpPost.getURI().toString());
			HttpResponse response = httpClient.execute(httpPost);

			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String jsonResponse = getResponseJson(in);

			return jsonResponse;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getResponseJson(BufferedReader in) throws IOException {
		StringBuffer sb = new StringBuffer("");
		String line = "";
		String NL = System.getProperty("line.separator");
		while ((line = in.readLine()) != null) {
			sb.append(line + NL);
		}
		in.close();
		return sb.toString();
	}

	public void log(String message) {
		if (debug) {
			Log.v(TAG, message);
		}
	}
}
