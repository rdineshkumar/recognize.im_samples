package pl.itraff.TestApi;

import pl.itraff.TestApi.ItraffApi.ItraffApi;
import pl.itraff.camera.TakePhoto;
import pl.itraff.camera.utils.CameraConstants;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	static Boolean debug = true;

	// only for demo to enable user to edit this values on screen
	private String CLIENT_API_KEY = null;
	private Integer CLIENT_API_ID = null;

	private final String ID = "id";
	private final String KEY = "key";

	// tag for debug
	private static final String TAG = "TestApi";
	public static final String PREFS_NAME = "iTraffTestApiPreferences";

	private TextView responseView;

	private EditText clientIdEdit;
	private EditText clientApiKeyEdit;

	// minimum size of picture to scale
	final int REQUIRED_SIZE = 400;

	ProgressDialog waitDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		responseView = (TextView) findViewById(R.id.response_text);

		clientIdEdit = (EditText) findViewById(R.id.client_id_edit);
		clientApiKeyEdit = (EditText) findViewById(R.id.client_api_key_edit);

		// Restore preferences
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		CLIENT_API_ID = settings.getInt(ID, 0);
		CLIENT_API_KEY = settings.getString(KEY, "");
		if (CLIENT_API_ID > 0) {
			clientIdEdit.setText(CLIENT_API_ID.toString());
		}
		if (CLIENT_API_KEY.length() > 0) {
			clientApiKeyEdit.setText(CLIENT_API_KEY);
		}
	}

	@Override
	protected void onStop() {
		getValuesFromEdit();
		savePrefs();
		super.onStop();
	}

	public void infoClickHandler(View v) {
		Intent intent = new Intent(this, InfoActivity.class);
		startActivity(intent);
	}

	private void getValuesFromEdit() {
		// get values from edti text fields
		String clientApiIdString = clientIdEdit.getText().toString();
		Integer clientApiId = 0;
		if (clientApiIdString.length() > 0) {
			try {
				clientApiId = Integer.parseInt(clientApiIdString);
				CLIENT_API_ID = clientApiId;
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "ID should be a correct number!", Toast.LENGTH_LONG).show();
			}
		}
		CLIENT_API_KEY = clientApiKeyEdit.getText().toString();
	}

	public void makePhotoClick(View v) {
		getValuesFromEdit();

		if (CLIENT_API_KEY != null && CLIENT_API_KEY.length() > 0 && CLIENT_API_ID != null && CLIENT_API_ID > 0) {
			savePrefs();

			// Intent to take a photo
			Intent intent = new Intent(this, TakePhoto.class);
			intent.putExtra(CameraConstants.CATEGORY_TYPE, 1);
			startActivityForResult(intent, 1234);
		} else {
			Toast.makeText(getApplicationContext(), "Fill in Your API Id and API Key", Toast.LENGTH_SHORT).show();
		}
	}

	private void savePrefs() {
		// Save prefs
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(ID, CLIENT_API_ID);
		editor.putString(KEY, CLIENT_API_KEY);
		// Commit the edits!
		editor.commit();
	}

	// handler that receives response from api
	private Handler itraffApiHandler = new Handler() {
		// callback from api
		@Override
		public void handleMessage(Message msg) {
			dismissWaitDialog();
			Bundle data = msg.getData();
			if (data != null) {
				Integer status = data.getInt(ItraffApi.STATUS, -1);
				String response = data.getString(ItraffApi.RESPONSE);
				// status ok
				if (status == 0) {
					responseView.setText(response);
					// application error (for example timeout)
				} else if (status == -1) {
					responseView.setText(getResources().getString(R.string.app_error));
					// error from api
				} else {
					responseView.setText(getResources().getString(R.string.error) + response);
				}
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		log("onActivityResult reqcode, resultcode: " + requestCode + "  " + resultCode);
		if (resultCode == Activity.RESULT_OK) {
			log("Activity.RESULT_OK");
			if (data != null) {
				log("data != null");
				Bundle bundle = data.getBundleExtra("data");
				if (bundle != null) {
					log("bundle != null");

					byte[] pictureData = bundle.getByteArray("pictureData");
					Bitmap image = null;
					if (pictureData != null) {
						log("pictureData != null");
						try {
							image = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (image != null) {
							log("image != null");

							// chceck internet connection
							if (ItraffApi.isOnline(getApplicationContext())) {
								showWaitDialog();
								// send photo
								ItraffApi api = new ItraffApi(CLIENT_API_ID, CLIENT_API_KEY, TAG, false);
								api.sendPhoto(pictureData, itraffApiHandler);
							} else {
								// show message: no internet connection
								// available.

								Toast.makeText(getApplicationContext(), getResources().getString(R.string.not_connected),
										Toast.LENGTH_LONG).show();
							}
						}
					} else {
						log("picture data == null");
					}
				}
			}
		} else if (resultCode == CameraConstants.RESULT_BMP_DAMAGED) {
			log("RESULT_BMP_DAMAGEDl");
		}
	}


	private void showWaitDialog() {
		if (waitDialog != null) {
			if (!waitDialog.isShowing()) {
				waitDialog.show();
			}
		} else {
			waitDialog = new ProgressDialog(this);
			waitDialog.setMessage(getResources().getString(R.string.wait_message));
			waitDialog.show();
		}
	}

	private void dismissWaitDialog() {
		try {
			if (waitDialog != null && waitDialog.isShowing()) {
				waitDialog.dismiss();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void log(String msg) {
		if (debug) {
			Log.v(TAG, msg);
		}
	}
}