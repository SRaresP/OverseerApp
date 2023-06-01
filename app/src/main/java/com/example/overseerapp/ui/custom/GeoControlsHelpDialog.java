package com.example.overseerapp.ui.custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.example.overseerapp.R;

public class GeoControlsHelpDialog extends Dialog {
	private void init() {
		AppCompatButton exitB = findViewById(R.id.geohExitB);
		exitB.setOnClickListener((view) -> {
			cancel();
		});
		getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT);
	}

	public GeoControlsHelpDialog(@NonNull Context context) {
		super(context);
	}

	public GeoControlsHelpDialog(@NonNull Context context, int themeResId) {
		super(context, themeResId);
	}

	protected GeoControlsHelpDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}
}
