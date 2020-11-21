package aashi.fiaxco.asquirefilip0x08;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

import aashi.fiaxco.asquirefilip0x08.audioengine.AsqEngine;
import aashi.fiaxco.asquirefilip0x08.audioengine.AsqViewModel;
import aashi.fiaxco.asquirefilip0x08.audioengine.AudioService;
import aashi.fiaxco.asquirefilip0x08.audioengine.UploadService;
import aashi.fiaxco.asquirefilip0x08.stuff.Stimulus;
import aashi.fiaxco.asquirefilip0x08.stuff.Timer;

public class RecordActivity extends AppCompatActivity implements Timer.MessageConstants {

	private static final String TAG = RecordActivity.class.getName();
	private static final int AUDIO_EFFECT_REQUEST = 666;

	Button recordButton, nextButton, optionButton;
	ImageButton info;
	TextView stimulusTv, predictionTv;
	ProgressBar asthmaProgress;
	SwitchCompat contributeSwitch;

	private static final String[] mModels = {"cough.model", "cough_1.model", "feats_0.txt"};
	private final HashMap<String, String> mModelCacheFiles = new HashMap<>();
	private String[] mStimulus;
	private int mNStimuli = 0;
	private String mUserID;


	// Timer
	private Timer.TimerHandler mTimerHandler;

	//Service - Audio
	private AudioService mAudService;
	private AsqViewModel mAsqViewModel;

	//Service - Upload
	private UploadService mUploadService;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);

		Intent pIntent = getIntent();
		mUserID = pIntent.getStringExtra(UserIDActivity.USER_ID);

		// Buttons ID & Views
		{
			recordButton = findViewById(R.id.control_button_record);
			nextButton = findViewById(R.id.control_button_next);
			optionButton = findViewById(R.id.control_button_options);
			info = findViewById(R.id.button_info);
			stimulusTv = findViewById(R.id.task_frag_description_tv);
			predictionTv = findViewById(R.id.prediction_textView);
			asthmaProgress = findViewById(R.id.asthma_progressBar);
			contributeSwitch = findViewById(R.id.contribute);
		}

		// Timer
		Timer mTimer = new Timer();
		mTimerHandler = new Timer.TimerHandler(this, mTimer);

		// Stimulus
		mStimulus = Stimulus.getStimulus();
		stimulusTv.setText(mStimulus[0]);

		// Initialize stuff
		setObservers();

		// Button functions
		recordButton.setOnClickListener(view -> recordFunction());
		nextButton.setOnClickListener(view -> nextFunction());
		optionButton.setOnClickListener(view -> optionFunction());
		info.setOnClickListener(view -> showInfo());
		contributeSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
			int v = b ? View.INVISIBLE : View.VISIBLE;
			predictionTv.setVisibility(v);
			asthmaProgress.setVisibility(v);
			TextView tl = findViewById(R.id.low_pred_tv);
			TextView th = findViewById(R.id.high_pred_tv);
			tl.setVisibility(v);
			th.setVisibility(v);
		});

	}

	// onCreate Methods

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
			mTimerHandler.sendEmptyMessage(isRec ? MSG_START_TIMER : MSG_STOP_TIMER);
			if (isRec) {
				{
					nextButton.setEnabled(false);
					optionButton.setEnabled(false);
					recordButton.setEnabled(false);
					recordButton.postDelayed(() -> recordButton.setEnabled(true), 1000);
				}
			} else {
				recordButton.setEnabled(false);
				doPredict();
			}

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
	private void doPredict() {
		if (mAudService.recordDone && !mPredicted) {
			predictionTv.setText(R.string.analysing_msg);
			asthmaProgress.setIndeterminate(true);

			// Prediction result
			float op = AsqEngine.asqPredict(mModelCacheFiles.get(mModels[1]), mModelCacheFiles.get(mModels[2])); // TODO: temp param featfile

			predictionTv.postDelayed(() -> {
				predictionTv.setText(R.string.pred_result_message);
				Log.d(TAG, "doPredict: prediction " + (1 - op / 100)); // TODO: update progress bar
				asthmaProgress.setIndeterminate(false);
				asthmaProgress.setProgress((int) op);
				mPredicted = true;

				{
					recordButton.setEnabled(true);
					recordButton.setEnabled(true);
					nextButton.setEnabled(true);
					optionButton.setEnabled(true);
				}
			}, 1000);

		} else {
			makeToast("You have to record first");
		}
	}

	private void showInfo() {
	}

	private void cacheModelFiles() {

		AssetManager asquireAssets = getAssets();
		for (String model : mModels) {
			try {

				InputStream inputStream = asquireAssets.open(model);
				int size = inputStream.available();
				byte[] buffer = new byte[size];
				int n = inputStream.read(buffer);
				inputStream.close();
				Log.d(TAG, "onCreate: asset ip stream - " + n);

				File modelCacheFile = new File(getCacheDir(), model);
				FileOutputStream fos = new FileOutputStream(modelCacheFile);
				fos.write(buffer);
				fos.close();

				mModelCacheFiles.put(model, modelCacheFile.getAbsolutePath());

			} catch (IOException e) {
				e.printStackTrace();
			}
		}


	}


	private void recordFunction() {
		if (mAudService != null) {
			if (!isRecordPermissionGranted()) {
				requestRecordPermission();
				return;
			}
			mAudService.mUserId = mUserID;
			mAudService.function();
			mAsqViewModel.setIsRecording(mAudService.isRecording);
			mPredicted = false;
		} else {
			Log.d(TAG, "Service object is null");
		}
	}

	private void nextFunction() {
		if (mUploadService != null && mAudService != null) {
			Log.d(TAG, "onCreate: Uploading started");
			if (mAudService.recordDone) {
				mUploadService.uploadData(mAudService.RecFilePath, mAudService.RecFilename);

				mAudService.recordDone = false;
				mPredicted = false;

				predictionTv.setText("");
				asthmaProgress.setProgress(0);

				mTimerHandler.sendEmptyMessage(MSG_RESET_TIMER);

				stimulusTv.setText(mStimulus[++mNStimuli % mStimulus.length]);

			} else {
				makeToast("Recording not finished!");
			}
		}

	}
	private boolean mPredicted = false;

	private void optionFunction() {
		mNStimuli = --mNStimuli > 0 ? mNStimuli : mNStimuli + mStimulus.length;
		stimulusTv.setText(mStimulus[mNStimuli % mStimulus.length]);
	}

	private void clearCache() {
		File[] files = getCacheDir().listFiles();
		if (files != null) {
			for (File f : files) {
				boolean res = f.delete();
				if (res) Log.d(TAG, "onStart: deleted file " + f.getName());
			}
		}
		cacheModelFiles();
	}


	@Override
	protected void onResume() {
		super.onResume();
		AsqEngine.setDefaultStreamValues(this);
		startServices();
		if (mAudService != null)
			recordButton.setText(mAudService.isRecording ? R.string.stop : R.string.record);
	}

	private void startServices() {
		// Audio Service - Pass User ID here
		Intent audServiceIntent = new Intent(this, AudioService.class);
		startService(audServiceIntent);
		bindService(audServiceIntent, mAsqViewModel.getAudioServiceConnection(), Context.BIND_AUTO_CREATE);

		// Upload Service - Pass User ID here
		Intent upServiceIntent = new Intent(this, UploadService.class);
		startService(upServiceIntent);
		bindService(upServiceIntent, mAsqViewModel.getUploadServiceConnection(), Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
		unbindService(mAsqViewModel.getAudioServiceConnection());
		unbindService(mAsqViewModel.getUploadServiceConnection());
		super.onPause();
	}


	@Override
	protected void onStart() {
		super.onStart();
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		cacheModelFiles();
	}
	@Override
	protected void onStop() {
		// Clear cache folder
		File[] files = getCacheDir().listFiles();
		if (files != null) {
			for (File f : files) {
				boolean res = f.delete();
				if (res) Log.d(TAG, "onStart: deleted file " + f.getName());
			}
		}
		super.onStop();
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

