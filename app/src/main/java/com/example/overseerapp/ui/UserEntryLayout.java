package com.example.overseerapp.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;

import com.example.overseerapp.R;
import com.google.android.material.button.MaterialButton;

public class UserEntryLayout extends LinearLayoutCompat {

	//mandatory constructors for Android to use for whatever it needs
	public UserEntryLayout(@NonNull Context context) {
		super(context);
	}

	public UserEntryLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public UserEntryLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	//constructor that is actually used in app code
	public UserEntryLayout(@NonNull Context context, @NonNull String userName, @NonNull int userId, @NonNull String location, @NonNull String dateRecordedLocation) {
		super(context);

		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		setOrientation(LinearLayoutCompat.HORIZONTAL);
		setGravity(Gravity.CENTER);

		//create user identifier label
		AppCompatTextView userIdentifierTV = new AppCompatTextView(context);
		userIdentifierTV.setId(ViewCompat.generateViewId());
		userIdentifierTV.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		userIdentifierTV.setText(context.getString(R.string.user_identifier, userName, userId));

		//create address label
		AppCompatTextView addressTV = new AppCompatTextView(context);
		addressTV.setId(ViewCompat.generateViewId());
		addressTV.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addressTV.setText(location);

		//create date label
		AppCompatTextView timeAgoTV = new AppCompatTextView(context);
		timeAgoTV.setId(ViewCompat.generateViewId());
		timeAgoTV.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		timeAgoTV.setText(dateRecordedLocation);

		//create the text wrapper
		LinearLayoutCompat detailsL = new LinearLayoutCompat(context);
		detailsL.setId(ViewCompat.generateViewId());
		detailsL.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
		detailsL.setOrientation(LinearLayoutCompat.VERTICAL);
		detailsL.addView(userIdentifierTV);
		detailsL.addView(addressTV);
		detailsL.addView(timeAgoTV);

		//create settings button
		AppCompatImageButton settingsB = new AppCompatImageButton(context);
		settingsB.setId(ViewCompat.generateViewId());
		settingsB.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		settingsB.setImageResource(R.drawable.ic_baseline_settings_24);
		settingsB.setPadding(15, 15, 15, 15);

		//add everything to the layout
		this.addView(detailsL);
		this.addView(settingsB);
	}
}
