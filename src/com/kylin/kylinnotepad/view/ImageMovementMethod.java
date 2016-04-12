package com.kylin.kylinnotepad.view;

import com.kylin.kylinnotepad.activity.NoteActivity;

import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.BaseMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class ImageMovementMethod extends BaseMovementMethod {
	private static final String TAG = "ImageMovementMethod";
	private static BaseMovementMethod sInstance;
	private Handler handler = null;
	private Class spanClass = null;

	int x1;
	int x2;
	int y1;
	int y2;

	public static MovementMethod getInstance(Handler _handler, Class _spanClass) {
		sInstance = new ImageMovementMethod();
		((ImageMovementMethod) sInstance).handler = _handler;
		((ImageMovementMethod) sInstance).spanClass = _spanClass;
		return sInstance;
	}

	@Override
	public boolean onTouchEvent(TextView widget, Spannable buffer,
			MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			x1 = (int) event.getX();
			y1 = (int) event.getY();
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			x2 = (int) event.getX();
			y2 = (int) event.getY();
			if (Math.abs(x1 - x2) < 10 && Math.abs(y1 - y2) < 10) {
				x2 -= widget.getTotalPaddingLeft();
				y2 -= widget.getTotalPaddingTop();
				x2 += widget.getScrollX();
				y2 += widget.getScrollY();
				Layout layout = widget.getLayout();
				int line = layout.getLineForVertical(y2);
				int off = layout.getOffsetForHorizontal(line, x2);
				Log.d(TAG, "点击了图片" + buffer.toString());
				Log.d(TAG, "行：" + line + ";宽偏移：" + off);
				/** * get you interest span */
				@SuppressWarnings("unchecked")
				Object[] spans = buffer.getSpans(off, off, spanClass);
				if (spans.length != 0) {
					Selection.setSelection(buffer,
							buffer.getSpanStart(spans[0]),
							buffer.getSpanEnd(spans[0]));
					MessageSpan obj = new MessageSpan();
					obj.setObj(spans);
					obj.setView(widget);
					Message message = handler.obtainMessage();
					message.obj = obj;
					message.what = NoteActivity.MESSAGE_TYPE_IMAGESPAN;
					message.sendToTarget();
					return true;
				}
			}
		}
		return super.onTouchEvent(widget, buffer, event);
	}

	public class MessageSpan {
		private Object obj;
		private TextView view;

		public Object getObj() {
			return obj;
		}

		public void setObj(Object obj) {
			this.obj = obj;
		}

		public TextView getView() {
			return view;
		}

		public void setView(TextView view) {
			this.view = view;
		}
	}
}
