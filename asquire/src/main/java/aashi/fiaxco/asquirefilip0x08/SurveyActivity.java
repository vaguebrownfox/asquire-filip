package aashi.fiaxco.asquirefilip0x08;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import aashi.fiaxco.asquirefilip0x08.webview.SurveyWebView;

public class SurveyActivity extends AppCompatActivity {

	private WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_survey);

		Button startButton = findViewById(R.id.next_button);
		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SurveyActivity.this, RecordActivity.class);
				// TODO: put required stuff into intent
				startActivity(intent);
			}

		});

		mWebView = findViewById(R.id.survey_activity_webview);
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new SurveyWebView());

		// REMOTE RESOURCE
		mWebView.loadUrl("https://docs.google.com/forms/d/e/1FAIpQLSeoU1W6Xh1wICI7hJA8ku01aHO2sPFQDqLyGVwUXA9NfEkdfg/viewform?usp=sf_link");

	}
}