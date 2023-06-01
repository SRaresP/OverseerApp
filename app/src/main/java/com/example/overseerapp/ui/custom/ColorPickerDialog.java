package com.example.overseerapp.ui.custom;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.example.overseerapp.R;
import com.rarepebble.colorpicker.ColorPickerView;

public class ColorPickerDialog extends Dialog {
	private ColorPickerView colorPicker;

	private void init() {
		setContentView(R.layout.dialog_color);
		setCancelable(false);
		colorPicker = findViewById(R.id.pickColorPicker);
		AppCompatButton cancelB = findViewById(R.id.pickCancelB);
		cancelB.setOnClickListener(view -> cancel());
	}

	public ColorPickerDialog(@NonNull Context context) {
		super(context);
		init();
	}

	public ColorPickerDialog(@NonNull Context context, int themeResId) {
		super(context, themeResId);
		init();
	}

	protected ColorPickerDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init();
	}

	public void show(int color) {
		colorPicker.setColor(color);
		super.show();
	}

	public void setOnSaveListener(View.OnClickListener listener) {
		AppCompatButton saveB = findViewById(R.id.pickSaveB);
		saveB.setOnClickListener(listener);
	}

	public int getColor() {
		return colorPicker.getColor();
	}
}
