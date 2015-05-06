package ru.vasilek.gscamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class EffectView extends ImageView {
	
	private CameraEffect effect;
	private Bitmap bmp;
		
	public EffectView(Context context, Bitmap bmp, CameraEffect effect) {
		super(context);	
		this.bmp = bmp;
		this.effect = effect;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		effect.draw(canvas, bmp);		
		super.onDraw(canvas);
	}

}
