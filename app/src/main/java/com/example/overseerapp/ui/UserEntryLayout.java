package com.example.overseerapp.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.overseerapp.R;

public class UserEntryLayout extends ConstraintLayout {

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

	public UserEntryLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	//...

	//constructor that is actually used in app code
	public UserEntryLayout(@NonNull Context context, @NonNull String userName, @NonNull int userId, @NonNull String location, @NonNull String dateRecordedLocation) {
		super(context);

		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		ConstraintSet constraintSet = new ConstraintSet();
		constraintSet.clone(this);

		//create user identifier label
		AppCompatTextView userIdentifierTV = new AppCompatTextView(context);
		userIdentifierTV.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		userIdentifierTV.setText(String.format(String.valueOf(R.string.user_identifier), userName, userId));
		userIdentifierTV.setTextColor(Color.RED);
		constraintSet.connect(userIdentifierTV.getId(), ConstraintSet.TOP, this.getId(), ConstraintSet.TOP);
		constraintSet.connect(userIdentifierTV.getId(), ConstraintSet.LEFT, this.getId(), ConstraintSet.LEFT);

		//create address label
		AppCompatTextView addressTV = new AppCompatTextView(context);
		addressTV.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addressTV.setText(location);
		addressTV.setTextColor(Color.RED);
		constraintSet.connect(addressTV.getId(), ConstraintSet.TOP, userIdentifierTV.getId(), ConstraintSet.BOTTOM);
		constraintSet.connect(addressTV.getId(), ConstraintSet.LEFT, this.getId(), ConstraintSet.LEFT);

		//create date label
		AppCompatTextView timeAgoTV = new AppCompatTextView(context);
		timeAgoTV.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		timeAgoTV.setText(dateRecordedLocation);
		timeAgoTV.setTextColor(Color.RED);
		constraintSet.connect(timeAgoTV.getId(), ConstraintSet.TOP, addressTV.getId(), ConstraintSet.BOTTOM);
		constraintSet.connect(timeAgoTV.getId(), ConstraintSet.LEFT, this.getId(), ConstraintSet.LEFT);

		//create settings button
		AppCompatButton settingsB = new AppCompatButton(context);
		settingsB.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		settingsB.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_settings_24, 0, 0, 0);
		constraintSet.connect(settingsB.getId(), ConstraintSet.RIGHT, this.getId(), ConstraintSet.RIGHT);
		constraintSet.connect(settingsB.getId(), ConstraintSet.TOP, this.getId(), ConstraintSet.TOP);
		constraintSet.connect(settingsB.getId(), ConstraintSet.BOTTOM, this.getId(), ConstraintSet.BOTTOM);

		//add everything to the layout
		this.addView(userIdentifierTV);
		this.addView(addressTV);
		this.addView(timeAgoTV);
		this.addView(settingsB);
		constraintSet.applyTo(this);
	}
}
