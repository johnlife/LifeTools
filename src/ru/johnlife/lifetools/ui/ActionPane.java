package ru.johnlife.lifetools.ui;

import ru.johnlife.lifetools.R;
import ru.johnlife.lifetools.ui.listener.AnimationFinishedListener;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewPropertyAnimator;
import android.widget.Button;
import android.widget.TextView;

import com.android.ics.swipedismiss.SwipeDismissTouchListener;
import com.android.ics.swipedismiss.SwipeDismissTouchListener.DismissCallbacks;

public class ActionPane extends ViewStub {

	private TextView message;
	private Button actionButton;
	private TextView details;

	private final DismissCallbacks dismissCallback = new DismissCallbacks() {
		@Override
		public void onDismiss(View view, Object token) {
			view.setVisibility(View.GONE);
			if (dismissListener != null) {
				dismissListener.onDismiss();
			}
		}

		@Override
		public boolean canDismiss(Object token) {
			return true;
		}
	};

	public interface OnDismissListener {
		public void onDismiss();
	}

	private boolean setDefaultMargins = false;
	private View inflated;
	private int popupDelay = AnimationTools.DEFAULT_DELAY;
	private OnDismissListener dismissListener = null;

	//stock constructors

	public ActionPane(Context context) {
		super(context, R.layout.action_pane);
		setDefaultMargins = true;
	}

	public ActionPane(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setLayoutResource(R.layout.action_pane);
	}

	public ActionPane(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutResource(R.layout.action_pane);
	}

	// handy constructors

	/** Warning! This constructor invokes inflate() */
	protected ActionPane(View anySibling) {
		this(anySibling, 0);
	}

	private ActionPane(View anySibling, int popupDelay) {
		this(anySibling.getContext());
		this.popupDelay = popupDelay;
		if (anySibling == null || !(anySibling.getParent() instanceof ViewGroup)) {
			throw new IllegalArgumentException("Pass any sibling that is child of ViewGroup already");
		}
		ViewGroup parent = (ViewGroup) anySibling.getParent();
		parent.addView(this, 0);
		inflate();
	}

	public ActionPane(View anySibling, StringDescriptor title, StringDescriptor details, StringDescriptor button, final OnClickListener actionListener) {
		this(anySibling, title, details, button, actionListener, 0);
	}

	public ActionPane(View anySibling, StringDescriptor title, StringDescriptor details, StringDescriptor button, final OnClickListener actionListener, int popupDelay) {
		this(anySibling, popupDelay);
		title.apply(this.message);
		details.apply(this.details);
		button.apply(this.actionButton);
		actionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				actionListener.onClick(v);
				AnimationTools.animateFadeout(inflated);
			}
		});
	}


	// routines of handy

	@Override
	public View inflate() {
		inflated = super.inflate();
		inflated.setBackgroundResource(R.color.background_accent);
		if (setDefaultMargins ) {
			LayoutParams params = inflated.getLayoutParams();
			if (params instanceof ViewGroup.MarginLayoutParams) {
				ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
				marginParams.setMargins(0, 0, 0, getContext().getResources().getDimensionPixelSize(R.dimen.inset_margin));
			}
		}
		message = (TextView)inflated.findViewById(R.id.message);
		details = (TextView)inflated.findViewById(R.id.details);
		actionButton = (Button)inflated.findViewById(R.id.actionButton);
		onAfterInflate();
		return inflated;
	}

	protected void onAfterInflate() {
		AnimationTools.animatePopup(inflated, popupDelay);
		inflated.setOnTouchListener(new SwipeDismissTouchListener(inflated, null, dismissCallback));
	}

	public TextView getMessage() {
		return message;
	}

	public Button getActionButton() {
		return actionButton;
	}

	public TextView getDetails() {
		return details;
	}

	protected View getInflated() {
		return inflated;
	}

	@SuppressLint("NewApi")
	public void dismiss() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
			setVisibility(View.GONE);
		} else {
			ViewPropertyAnimator animation = inflated.animate()
				.translationX(inflated.getWidth())
				.alpha(0)
				.setListener(new AnimationFinishedListener(){
					@Override
					public void onAnimationEnd(Animator animation) {
						setVisibility(View.GONE);
					}
				});
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				animation.start();
			}
		}
		if (dismissListener != null) dismissListener.onDismiss();
	}

	public void setOnDismissListener(OnDismissListener dismissListener) {
		this.dismissListener = dismissListener;
	}

}
