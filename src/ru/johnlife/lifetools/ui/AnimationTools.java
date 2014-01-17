package ru.johnlife.lifetools.ui;

import ru.johnlife.lifetools.ui.listener.AnimationFinishedListener;
import android.animation.Animator;
import android.view.View;
import android.view.ViewGroup;

public class AnimationTools {

	public static final int DEFAULT_DELAY = 800;
	private static final float HALF = 0.5f;

	public static void animatePopup(final View v) {
		animatePopup(v, DEFAULT_DELAY);
	}
	
	public static void animatePopup(final View v, int customDelay) {
		animatePopup(v, customDelay, new AnimationFinishedListener() {
			@Override
			public void onAnimationEnd(Animator animation) {
				v.animate().setStartDelay(0);
			}
		});
	}
	
	public static void animatePopup(final View v, int customDelay, AnimationFinishedListener listener) {
		v.setAlpha(0);
		v.setScaleX(0);
		v.setScaleY(0);
		v.animate()
			.setStartDelay(customDelay)
			.alpha(1)
			.scaleX(1)
			.scaleY(1)
			.setListener(listener)
			.start();		
	}
	
	
	private static class Counter {
		private int i = 1;
		private int limit = 3;
		
		public Counter(int limit) {
			this.limit = limit;
		}

		public boolean inc() {
			if (++i <= limit) {
				return true;
			} else {
				i = 1;
				return false;
			}
		}
	}
	
	public static void animateBlink(final View v) {
		if (null == v) return;
		final Counter c = new Counter(2);
		AnimationFinishedListener blinker = new AnimationFinishedListener(){
			AnimationFinishedListener fadeOut = this;
			AnimationFinishedListener fadeIn = new AnimationFinishedListener() {
				@Override
				public void onAnimationEnd(Animator animation) {
					AnimationFinishedListener listener = c.inc() ? fadeOut : null;
					v.animate().alpha(1).scaleX(1).scaleY(1).setDuration(300).setListener(listener).start();				
				}
			};
			
			@Override
			public void onAnimationEnd(Animator animation) {
				v.animate().alpha(HALF).scaleX(HALF).scaleY(HALF).setListener(fadeIn).setDuration(300).start();
			}
		};
		v.animate().setDuration(0).setListener(blinker).start();
	}
	
	public static void animateFadeout(final View v) {
		v.animate()
			.alpha(0)
			.setListener(new AnimationFinishedListener() {
				@Override
				public void onAnimationEnd(Animator animation) {
					v.setVisibility(View.GONE);
				}
			})
			.start();		
	}
	
	public static void animateVerticalAddition(final ViewGroup parent, final View[] add, View[] remove, int addingHeight) {
		int ph = parent.getHeight();
		float h = ph + addingHeight;
		int rh = 0;
		if (null != remove) {
			for (View v : remove) {
				rh += v.getHeight();
				v.setVisibility(View.GONE);
			}
			h -= rh;
		}
		float f = h/(ph - rh);
		ViewGroup pp = (ViewGroup) parent.getParent();
		if (pp != null) {
			boolean found = false;
			for (int i=0; i<pp.getChildCount(); i++) {
				final View kid = pp.getChildAt(i);
				if (found) {
					kid.animate().translationY(h).setListener(new AnimationFinishedListener() {						
						@Override
						public void onAnimationEnd(Animator animation) {
							kid.setTranslationY(0);
						}
					}).start();
				} else if (kid.equals(parent)) {
					found = true;
				}
			}
		}
		parent.animate().scaleY(f).translationY(h/2).setListener(new AnimationFinishedListener() {
			@Override
			public void onAnimationEnd(Animator animation) {
				parent.setScaleY(1);
				parent.setTranslationY(0);
				if (null != add) {
					for (View v : add) {
						v.setVisibility(View.VISIBLE);
						v.setAlpha(0);
						v.animate().alpha(1).setListener(null).start();
					}
				}
			}
		}).start();
	}
	
}
