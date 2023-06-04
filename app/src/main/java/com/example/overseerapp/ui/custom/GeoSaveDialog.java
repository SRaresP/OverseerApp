package com.example.overseerapp.ui.custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import com.example.overseerapp.R;

public class GeoSaveDialog extends Dialog {
	private AppCompatButton saveB;
	private AppCompatEditText triggerMessageET;

	private void init() {
		setCancelable(false);
		setContentView(R.layout.dialog_save_geo_area);
		saveB = findViewById(R.id.savegSaveB);
		triggerMessageET = findViewById(R.id.savegMessageET);
	}

	public GeoSaveDialog(@NonNull Context context) {
		super(context);
		init();
	}

	public GeoSaveDialog(@NonNull Context context, int themeResId) {
		super(context, themeResId);
		init();
	}

	protected GeoSaveDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.WRAP_CONTENT);
		AppCompatButton cancelB = findViewById(R.id.savegCancelB);
		cancelB.setOnClickListener((view) -> {
			cancel();
		});
	}

	public void setSaveListener(View.OnClickListener listener) {
		saveB.setOnClickListener(listener);
	}

	public void show(String triggerMessage) {
		triggerMessageET.setText(triggerMessage);
		super.show();
	}
}
