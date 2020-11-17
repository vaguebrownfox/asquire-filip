package aashi.fiaxco.asquirefilip0x08.audioengine;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class UploadService extends Service {


	private static final String TAG = "UploadService";

	private final IBinder mUploadBinder = new FireBinder();

	FirebaseStorage storage;

	StorageReference data0x00Ref;



	public UploadService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();

		storage = FirebaseStorage.getInstance();
		StorageReference storageRef = storage.getReference();
		data0x00Ref = storageRef.child("data0x00");

	}

	@Override
	public IBinder onBind(Intent intent) {
		return mUploadBinder;
	}
	public class FireBinder extends Binder {
		public UploadService getUploadService() {
			return UploadService.this;
		}

	}


	public void uploadData(String filePath, String fileName) {
		File file = new File(filePath);
		StorageReference cloudFileRef = data0x00Ref.child(fileName);

		try {
			InputStream iStream = new FileInputStream(file);
			UploadTask upTask = cloudFileRef.putStream(iStream);

			upTask.addOnFailureListener(e -> {
				Log.d(TAG, "uploadData: Failed");

				try {
					iStream.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}).addOnSuccessListener(taskSnapshot -> {
				Log.d(TAG, "uploadData: Success");

				try {
					iStream.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			});

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}


	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		stopSelf();
		super.onTaskRemoved(rootIntent);
	}
}