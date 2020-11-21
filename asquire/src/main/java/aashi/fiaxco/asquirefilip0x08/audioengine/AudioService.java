package aashi.fiaxco.asquirefilip0x08.audioengine;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Random;

public class AudioService extends Service {

	private static final String TAG = "AudioService";

	private final IBinder mBinder = new AudioBinder();

	enum STATE {
		START, RSTOP, PLAY, PSTOP;
	}
	private STATE mState;
	public Boolean isRecording;
	public boolean recordDone;
	public String RecFilePath;
	public String RecFilename;

	public String mUserId;

	public AudioService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
		boolean e = AsqEngine.create();
		if (!e) {
			Toast.makeText(this, "Audio engine error, restart App",
					Toast.LENGTH_LONG).show();
			stopSelf();
		}
		isRecording = false;
		recordDone = false;

		mState  = STATE.START;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	public class AudioBinder extends Binder {
		public AudioService getAudioService() {
			return AudioService.this;
		}

	}



	public void function() {
		switch (mState) {
			case START:
				isRecording = true;
				recordDone = false;
				startRecord();
				break;
			case RSTOP:
				isRecording = false;
				stopRecord();
				recordDone = true;
				break;
			case PLAY:
				startPlaying();
				break;
			case PSTOP:
				stopPlaying();
			default:
				break;
		}
	}


	private void startRecord() {
		Log.d(TAG, "Attempting to start recording");

		Random rand = new Random();
		int id = rand.nextInt(2000);
		RecFilename = mUserId + "_" + Build.DEVICE + "_"
								   + Build.PRODUCT + "_"
								   + Build.HARDWARE + "_"
								   + Build.MANUFACTURER+ "_"
								   + Build.BRAND + "_" + id + ".wav";

		File recFile = new File(getCacheDir(), RecFilename);
		RecFilePath = recFile.getAbsolutePath();
		AsqEngine.setRecFilePath(recFile.getAbsolutePath());

		Thread thread = new Thread(() -> AsqEngine.setRecOn(true));

		thread.start();
		mState = STATE.RSTOP;
	}

	private void stopRecord() {
		Log.d(TAG, "Attempting to stop recording");

		boolean res = AsqEngine.setRecOn(false);
		mState = STATE.START;
	}



	// TODO: playback recording
	private void startPlaying() {
	}
	private void stopPlaying() {
	}



	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);

		AsqEngine.setRecOn(false);
		AsqEngine.delete();
		stopSelf();
	}
}