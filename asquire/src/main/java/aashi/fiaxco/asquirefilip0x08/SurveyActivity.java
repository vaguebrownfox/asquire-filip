package aashi.fiaxco.asquirefilip0x08;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SurveyActivity extends AppCompatActivity {

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

	}
}