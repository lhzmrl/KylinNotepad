package com.kylin.kylinnotepad.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

public class VariableActionBar extends FrameLayout {

	private boolean isInterimState;

	private OnInstantiateListener onInstantiateListener;

	private View mViewNormal;
	private View mViewInterim;

	public VariableActionBar(Context context) {
		super(context);
		initDefaultValues();
	}

	public VariableActionBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initDefaultValues();
	}

	private void initDefaultValues() {
		isInterimState = false;
		instantiateActionBar();
	}

	private void instantiateActionBar() {
		if (onInstantiateListener == null)
			return;
		View currActionBar;
		if (isInterimState) {
			if (mViewInterim == null)
				mViewInterim = onInstantiateListener
						.instantiateInterimActionBar();
			currActionBar = mViewInterim;
		} else {
			if (mViewNormal==null)
				mViewNormal = onInstantiateListener
						.instantiateNormalActionBar();
				currActionBar = mViewNormal;
		}
		removeAllViews();
		addView(currActionBar);
	}

	public void setActionBarState(boolean isInterimState) {
		this.isInterimState = isInterimState;
		instantiateActionBar();
	}

	/**
	 * 
	 * @return isInterimState
	 */
	public boolean getActionBarState() {
		return isInterimState;
	}

	public void setOnInstantiateListener(
			OnInstantiateListener onInstantiateListener) {
		this.onInstantiateListener = onInstantiateListener;
		instantiateActionBar();
	}

	public interface OnInstantiateListener {
		public View instantiateNormalActionBar();

		public View instantiateInterimActionBar();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

}
