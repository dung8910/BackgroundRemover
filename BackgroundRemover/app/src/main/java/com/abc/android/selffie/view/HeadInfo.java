package com.abc.android.selffie.view;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;

public class HeadInfo {
	
	public Bitmap bm;
	public int viewWidth;
	public int viewHeight;
	public Rect mRoi;
	public Matrix mMatrix;
	

	
	public HeadInfo(Bitmap bitmap, int width, int height ) {
		bm = bitmap;
		viewWidth = width;
		viewHeight = height;
		mRoi = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		mMatrix = new Matrix();
	}

	public Bitmap getObjectBitmap() {
		return bm;
	}

	public int getPreviewWidth() {
		return viewWidth;
	}

	
	public int getPreviewHeight() {
		return viewHeight;
	}
	
	public Rect getDrawCanvasRoi() {
		return mRoi;
	}
	
	public int getPreviewPaddingX() {
		return 0;
	}

	
	public int getPreviewPaddingY() {
		return 0;
	}
	
	public Matrix getSupMatrix() {
		return mMatrix;
	}
	
	public Matrix getViewTransformMatrix() {
		return mMatrix;
	}

}
