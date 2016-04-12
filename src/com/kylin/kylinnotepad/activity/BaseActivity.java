package com.kylin.kylinnotepad.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public abstract class BaseActivity extends FragmentActivity {

	@Deprecated
	@Override
	final protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActivity(savedInstanceState);
	}

	abstract protected void initActivity(Bundle savedInstanceState);

}
