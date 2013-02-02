package fingo.plugin.action;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import fingo.plugin.IExternalFingoAction;
import fingo.plugin.IExternalFingoAction.State;

public abstract class AbstractActionReceiver extends BroadcastReceiver
		implements IExternalAction {
	protected Context context;
	protected WindowManager window;

	private ImageView pointerView;
	private MarginLayoutParams pointerMargin;
	private int pointerOffsetX;
	private int pointerOffsetY;
	private LayoutInflater inflater;

	private boolean isRecording;

	protected abstract String getClassName();

	@Override
	public void onReceive(Context context, Intent intent) {
		String actionKey = getClassName() + '@' + context.getPackageName();
		this.context = context;
		int iState = intent.getIntExtra(actionKey, -1);
		if (iState < 0)
			return;

		window = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);

		IExternalFingoAction.State state = IExternalFingoAction.State.values()[iState];
		State currentState;
		currentState = FingoApplication.getInstance().getCurrentState();

		if (currentState == state)
			return;

		switch (state) {
		case DEFAULT:
		case TOGGLE_FIRST:
			action1();
			break;
		case TOGGLE_SECOND:
			action2();
			break;
		case TOGGLE_THIRD:
			action3();
			break;
		}
		
		FingoApplication.getInstance().setCurrentState(state);
	}

}
