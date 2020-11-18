package aashi.fiaxco.asquirefilip0x08.webview;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SurveyWebView extends WebViewClient {

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		String hostname;

		// YOUR HOSTNAME
		hostname = "Asquire.com";

		Uri uri = Uri.parse(url);
		if (url.startsWith("file:") || uri.getHost() != null && uri.getHost().endsWith(hostname)) {
			return false;
		}
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		view.getContext().startActivity(intent);
		return true;
	}

}
