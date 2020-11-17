package aashi.fiaxco.asquirefilip0x08;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import aashi.fiaxco.asquirefilip0x08.audioengine.AsqEngine;
import aashi.fiaxco.asquirefilip0x08.audioengine.AsqViewModel;
import aashi.fiaxco.asquirefilip0x08.audioengine.AudioService;
import aashi.fiaxco.asquirefilip0x08.audioengine.UploadService;

public class RecordActivity extends AppCompatActivity {

	private static final String TAG = RecordActivity.class.getName();
	private static final int AUDIO_EFFECT_REQUEST = 666;

	Button recordButton, nextButton, optionButton;

	//Service - Audio
	private AudioService mAudService;
	private AsqViewModel mAsqViewModel;

	//Service - Upload
	private UploadService mUploadService;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);


		recordButton = findViewById(R.id.control_button_record);
		nextButton = findViewById(R.id.control_button_next);
		optionButton = findViewById(R.id.control_button_options);

		setObservers();


		recordButton.setOnClickListener(view -> {
			if (mAudService != null) {
				if (!isRecordPermissionGranted()) {
					requestRecordPermission();
					return;
				}
				mAudService.function();
				mAsqViewModel.setIsRecording(mAudService.isRecording);
			} else {
				Log.d(TAG, "Service object is null");
			}
		});

		nextButton.setOnClickListener(view -> {
			if (mUploadService != null && mAudService != null) {
				Log.d(TAG, "onCreate: Uploading started");
				if (mAudService.recordDone) {
					mUploadService.uploadData(mAudService.RecFilePath, mAudService.RecFilename);
					mAudService.recordDone = false;
				} else {
					makeToast("Recording not finished!");
				}
			}
		});

		optionButton.setOnClickListener(view -> {
			Log.d(TAG, "onClick: TODO options");
			makeToast("Reset: record again");

			String modelFileName = "model.txt";
			AssetManager asqAssets = getAssets();
			File modelFile = new File(getCacheDir(), modelFileName);

			try {
				InputStream inputStream = asqAssets.open(modelFileName);
				int size = inputStream.available();
				byte[] buffer = new byte[size];
				int n = inputStream.read(buffer);
				inputStream.close();
				Log.d(TAG, "onCreate: asset ip stream - " + n);

				FileOutputStream fos = new FileOutputStream(modelFile);
				fos.write(buffer);
				fos.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

			AsqEngine.asqPredict(modelFile.getAbsolutePath());
			Log.d(TAG, "onCreate: prediction done?");
		});

	}

	private void setObservers() {
		// Aud Service stuff
		mAsqViewModel = ViewModelProviders.of(this).get(AsqViewModel.class);

		mAsqViewModel.getAudBinder().observe(this, audioBinder -> {
			if (audioBinder != null) {
				mAudService = audioBinder.getAudioService();
				Log.d(TAG, "onCreate: Connected to Audio Service");
			} else {
				mAudService = null;
				Log.d(TAG, "onCreate: Unbound from the service");
			}
		});
		mAsqViewModel.getIsRecording().observe(this, isRec -> {

			recordButton.setText(isRec ? R.string.stop : R.string.record);
			nextButton.setEnabled(!isRec);
			optionButton.setEnabled(!isRec);
			recordButton.setEnabled(false);
			recordButton.postDelayed(() -> recordButton.setEnabled(true), 1000);

		});

		// Up Service stuff
		mAsqViewModel.getUpBinder().observe(this, fireBinder -> {
			if (fireBinder != null) {
				mUploadService = fireBinder.getUploadService();
				Log.d(TAG, "onCreate: Connected to Upload Service");
			} else {
				mUploadService = null;
				Log.d(TAG, "onCreate: Unbound from the service");
			}
		});

	}



	@Override
	protected void onStart() {
		super.onStart();
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Clear cache folder
		File[] files = getCacheDir().listFiles();
		if (files != null) {
			for (File f : files) {
				boolean res = f.delete();
				if (res) Log.d(TAG, "onStart: deleted file " + f.getName());
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		AsqEngine.setDefaultStreamValues(this);
		startServices();
	}

	@Override
	protected void onPause() {

		unbindService(mAsqViewModel.getAudioServiceConnection());
		super.onPause();
	}

	private void startServices() {
		Intent audServiceIntent = new Intent(this, AudioService.class);
		startService(audServiceIntent);
		bindService();

		Intent upServiceIntent = new Intent(this, UploadService.class);
		startService(upServiceIntent);
		bindService(upServiceIntent, mAsqViewModel.getUploadServiceConnection(), Context.BIND_AUTO_CREATE);
	}

	private void bindService() {
		Intent serviceIntent = new Intent(this, AudioService.class);
		bindService(serviceIntent, mAsqViewModel.getAudioServiceConnection(), Context.BIND_AUTO_CREATE);
	}




	// Permissions
	private boolean isRecordPermissionGranted() {
		return (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
				PackageManager.PERMISSION_GRANTED);
	}

	private void requestRecordPermission() {
		ActivityCompat.requestPermissions(
				this,
				new String[]{Manifest.permission.RECORD_AUDIO},
				AUDIO_EFFECT_REQUEST);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (AUDIO_EFFECT_REQUEST != requestCode) {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
			return;
		}

		if (grantResults.length != 1 ||
				grantResults[0] != PackageManager.PERMISSION_GRANTED) {

			makeToast(getString(R.string.need_record_audio_permission));
		}
	}

	// Misc
	private void makeToast(String msg) {
		Toast.makeText(RecordActivity.this, msg, Toast.LENGTH_SHORT).show();
	}

}