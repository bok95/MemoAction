package fingo.plugin.action.memo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import fingo.plugin.action.AbstractActionReceiver;
import fingo.plugin.action.FingoApplication;

public class MemoActionReceiver extends AbstractActionReceiver {

	private boolean isRecording;
	private ImageView pointerView;
	private MarginLayoutParams pointerMargin;
	private int pointerOffsetX;
	private int pointerOffsetY;

	private WritableView writableView;
	private Paint mPaint;
	private MaskFilter mEmboss;
	private MaskFilter mBlur;

	@Override
	public void action1() {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final LinearLayout container = (LinearLayout) inflater.inflate(
				R.layout.activity_memo_action, null);
		FingoApplication.getInstance().setContainer(container);

		// container.setBackgroundColor(Color.YELLOW); // for debugging
		container.addOnLayoutChangeListener(new OnLayoutChangeListener() {

			@Override
			public void onLayoutChange(View arg0, int left, int top, int right,
					int bottom, int oldLeft, int oldTop, int oldRight,
					int oldBottom) {
				window.updateViewLayout(container, makeLayoutParams(context));
			}
		});

		window.addView(container, makeLayoutParams(context));
		isRecording = true;

		LinearLayout touchContainer = (LinearLayout) container
				.findViewById(R.id.fingersContainer);

		isRecording = true;
		createWritableView(container);
	}

	private android.view.WindowManager.LayoutParams makeLayoutParams(
			Context context) {
		int w = getWindowWidth(context)
				- context.getResources().getDimensionPixelSize(
						R.dimen.edge_detection_width);
		android.view.WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
				w, LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
						| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSLUCENT);
		return layoutParams;
	}

	void createWritableView(LinearLayout view) {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(0x99e32921);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(12);

		LinearLayout memoContainer = (LinearLayout) FingoApplication
				.getInstance().getContainer()
				.findViewById(R.id.fingersContainer);
		writableView = new WritableView(context, mPaint);
		memoContainer.addView(writableView);
		// writableView.setBackgroundColor(Color.GREEN);

		mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);

		mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);

	}

	private void save(String fileName) {
		LinearLayout container = FingoApplication.getInstance().getContainer();
		if (container == null)
			return;
		container.getRootView().buildDrawingCache();
		Bitmap srcimg = container.getRootView().getDrawingCache();
		String root = Environment.getExternalStorageDirectory().toString();
		File file = new File(root + "/" + fileName);
		if (file.exists())
			file.delete();
		try {
			FileOutputStream out = new FileOutputStream(file);
			srcimg.compress(Bitmap.CompressFormat.PNG, 100, out);
		} catch (FileNotFoundException e) {
			Log.d("FileNotFoundException:", e.getMessage());
		}
		container.getRootView().setDrawingCacheEnabled(false);
	}

	@Override
	public void action2() {
		unblock();
		isRecording = true;

	}

	@Override
	public void action3() {
		save("memo.png");
		unblock();
		isRecording = false;
	}

	@Override
	protected String getClassName() {
		return MemoAction.class.getName();
	}

	private void unblock() {
		LinearLayout container = FingoApplication.getInstance().getContainer();
		try {
			if (container != null) {
				window.removeView(container);
			}
		} catch (IllegalArgumentException e) {
			Log.e("monkey", e.toString());
		} finally {
			FingoApplication.getInstance().setContainer(null);
		}
	}

	private int getWindowWidth(Context context) {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		window.getDefaultDisplay().getMetrics(displaymetrics);
		return displaymetrics.widthPixels;
	}
}
