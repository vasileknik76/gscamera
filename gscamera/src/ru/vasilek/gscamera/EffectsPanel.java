package ru.vasilek.gscamera;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.widget.AbsoluteLayout;

public class EffectsPanel extends AbsoluteLayout {

	public List<CameraEffect> effects;
	public Bitmap bmp;
	public EffectsPanel(Context context) {		
		super(context);
		effects = CameraEffect.CameraEffects();
		for (int i = 0; i < effects.size(); i++){
			CameraEffect e = effects.get(i);
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.effectpic);
			EffectView v = new EffectView(context, bmp, e);
			android.widget.AbsoluteLayout.LayoutParams params = new LayoutParams(40,40, i*50, 200);
			v.setLayoutParams(params);
			addView(v);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		
	}

}
