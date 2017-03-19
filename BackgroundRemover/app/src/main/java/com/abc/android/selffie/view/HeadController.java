package com.abc.android.selffie.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public class HeadController {
	
	private boolean	mbApply;
	private Bitmap	OrgObjectBmp;
	
	
	private int mBoundaryType = 0;
//	public static final int RECT_ROI_BOUNDARY = 1;
	public static final int RECT_CENTER_BOUNDARY = 2;

	public static final int NONE_TOUCH_TYPE = 0;
	public static final int MOVE = 1;
	public static final int LEFT = 2;
	public static final int TOP = 3;
	public static final int RIGHT = 4;
	public static final int BOTTOM = 5;
	public static final int LEFT_TOP = 6;
	public static final int RIGHT_BOTTOM = 7;
	public static final int LEFT_BOTTOM = 8;
	public static final int RIGHT_TOP = 9;

	private int mFixedWidth = 0;
	private int mFixedHeight = 0;
	protected int mPreviewWidth = 0;
	protected int mPreviewHeight = 0;
	private boolean mFreeRect = false;
	private boolean mRightTopRotate = true;

	private Paint mDrawLinePaint = null;
	private Paint mDrawLinePaint1 = null;
	private Paint mRectPaint = null;
	private Paint mGreyPaint = null;

	protected HeadInfo mImageData = null;
	protected RectF mROI = null;
	private RectF mInitROI = null;
	protected RectF mOriginalROI = null;
	private RectF mDrawROI = null;
	protected int mTouchType;
	private float mPrevX;
	private float mPrevY;
	protected int mAngle;
	private PointF mStartPT = null, mEndPT = null, mOriginalCenterPT = null;
	protected PointF mDestPt1 = null, mDestPt2 = null, mDestPt3 = null,
			mDestPt4 = null;
	private PointF mCenterPT = null;
	private PointF mOriginalDestPt1 = null, mOriginalDestPt2 = null,
			mOriginalDestPt3 = null, mOriginalDestPt4 = null;
	private Bitmap mBitmapResource = null;
	private Bitmap mBitmapRotateResource = null;

	protected int minValueForClipboard = 100;


	public HeadController(Context context, Rect srcroi, HeadInfo imgData, int boundaryType, boolean freeRect) {
		initResource(context, imgData);
		mFreeRect = freeRect;
		mBoundaryType = boundaryType;
		
		OrgObjectBmp = imgData.getObjectBitmap();
		if( srcroi.left < 0 )
			srcroi.left = 0;
		if( srcroi.top < 0 )
			srcroi.top = 0;
		if( srcroi.right > ( imgData.getPreviewWidth() - 1 ) )
			srcroi.right = imgData.getPreviewWidth() - 1;
		if( srcroi.bottom > ( imgData.getPreviewHeight() - 1 ) )
			srcroi.bottom = imgData.getPreviewHeight() - 1;

		if( ( srcroi.left > srcroi.right ) || ( srcroi.top > srcroi.bottom ) )
			mbApply = false;
		else
			mbApply = true;

		if( mbApply == true ) {
			mROI = new RectF();

			Log.d("tri.dung", "mbApply == true ");
			int x_center = 0;
			int y_center = 0;
			x_center = imgData.getPreviewWidth() / 2;
			y_center = imgData.getPreviewHeight() / 3;

			int ObjectWidth  = srcroi.width();
			int ObjectHeight = srcroi.height();

			Rect roi = new Rect();
			roi.left 	= x_center - ( ObjectWidth / 2 );
			roi.right 	= roi.left + ObjectWidth - 1;
			roi.top 	= y_center - ( ObjectHeight / 2 );
			roi.bottom 	= roi.top + ObjectHeight - 1;
			setRectRoi(roi);
		}
	}

	
	public RectF getDrawBdry()
	{
		return getDrawROI();
	}

	public PointF getDrawCenterPt()
	{
		PointF ret = new PointF();
		ret.x = getCenterPT().x + mImageData.getPreviewPaddingX();
		ret.y = getCenterPT().y + mImageData.getPreviewPaddingY();
		return ret;
	}
	public PointF getCenterPt()
	{
		return getCenterPT();
	}

	public Bitmap GetObjectBitmap()
	{
		return OrgObjectBmp;
	}

	protected void setFreeRect(boolean freeRect) {
		mFreeRect = freeRect;
	}

	protected void setRectRoi(Rect roi) {
		mFixedWidth = roi.width();
		mFixedHeight = roi.height();
		mROI.set(roi);

		int rectWidth = (int) mROI.width();
		int rectHeight = (int) mROI.height();

		if (rectWidth > rectHeight) {
			int tempWidth = (int) (rectHeight * (mFixedWidth / (float) mFixedHeight));

			if (tempWidth > rectWidth) {
				int diff = tempWidth - rectWidth;
				mROI.right += diff;
			} else {
				int diff = rectWidth - tempWidth;
				mROI.right -= diff;
			}

			int absoluteMarginX = (int) ((mImageData.getDrawCanvasRoi().width() - mROI
					.width()) / 2);
			int tempRectWidth = (int) mROI.width();

			mROI.left = absoluteMarginX;
			mROI.right = mROI.left + tempRectWidth;
		} else {
			int tempHeight = (int) (rectWidth * (mFixedHeight / (float) mFixedWidth));

			if (tempHeight > rectHeight) {
				int diff = tempHeight - rectHeight;
				mROI.bottom += diff;
			} else {
				int diff = rectHeight - tempHeight;
				mROI.bottom -= diff;
			}

			int absoluteMarginY = (int) ((mImageData.getPreviewHeight() - mROI
					.height()) / 3);
			int tempRectHeight = (int) mROI.height();

			mROI.top = absoluteMarginY;
			mROI.bottom = mROI.top + tempRectHeight;
		}

		mDrawROI.left = (mROI.left + mImageData.getDrawCanvasRoi().left);
		mDrawROI.right = (mROI.right + mImageData.getDrawCanvasRoi().left);
		mDrawROI.top = (mROI.top + mImageData.getDrawCanvasRoi().top);
		mDrawROI.bottom = (mROI.bottom + mImageData.getDrawCanvasRoi().top);

		mDestPt1.set(mROI.left, mROI.top);
		mDestPt2.set(mROI.right, mROI.top);
		mDestPt3.set(mROI.right, mROI.bottom);
		mDestPt4.set(mROI.left, mROI.bottom);

		mCenterPT.x = (mROI.left + mROI.right) / 2;
		mCenterPT.y = (mROI.top + mROI.bottom) / 2;

		// int absoluteMarginX = (int) ((mImageData.getPreviewWidth() -
		// mROI.width())/2);
		mPreviewWidth = mImageData.getDrawCanvasRoi().width();// mImageData.getPreviewWidth();
		mPreviewHeight = mImageData.getDrawCanvasRoi().height();// mImageData.getPreviewHeight();
		calculateOriginal();
		mInitROI.set(mROI);
	}


	public void setAngle(int angle) {
		mAngle = angle;
	}

	public void destroy() {
		mROI = null;
		mDrawROI = null;
		mInitROI = null;
		mStartPT = null;
		mCenterPT = null;
		mEndPT = null;

		mDestPt1 = null;
		mDestPt2 = null;
		mDestPt3 = null;
		mDestPt4 = null;

		if (mBitmapResource != null) {
			mBitmapResource.recycle();
		}
		mBitmapResource = null;
		if (mBitmapRotateResource != null) {
			mBitmapRotateResource.recycle();
		}
		mBitmapRotateResource = null;
		mDrawLinePaint = null;
		mDrawLinePaint1 = null;
		mRectPaint = null;
		mGreyPaint = null;
	}

	public int getAngle() {
		return mAngle;
	}

	public boolean isFreeRect() {
		return mFreeRect;
	}

	protected RectF getDrawROI() {
		if (mDrawROI != null) {
			Matrix m = new Matrix();
			m.postTranslate(mImageData.getDrawCanvasRoi().left,
					mImageData.getDrawCanvasRoi().top);
			RectF src = new RectF();
			src.set(mROI);
			RectF dst = new RectF();
			m.mapRect(dst, src);
			mDrawROI.set(dst.left, dst.top, dst.right, dst.bottom);
			return mDrawROI;
		}

		return null;
	}

	public RectF getRoi() {
		return new RectF(mROI);
	}

	public void setOrgDestROI(RectF roi, PointF center, PointF mOrgDestPt1,
			PointF mOrgDestPt2, PointF mOrgDestPt3, PointF mOrgDestPt4,
			int angle) {
		mAngle = angle;
		mOriginalROI = roi;

		if (mImageData != null) {
			Matrix m = mImageData.getSupMatrix();
			float left, top;
			float scaleX, scaleY;
			float[] values = new float[9];
			Matrix applyMatrix = new Matrix();
			m.getValues(values);
			if (values[Matrix.MTRANS_X] > 0)
				left = 0;
			else
				left = values[Matrix.MTRANS_X];
			if (values[Matrix.MTRANS_Y] > 0)
				top = 0;
			else
				top = values[Matrix.MTRANS_Y];

			scaleX = values[Matrix.MSCALE_X];
			scaleY = values[Matrix.MSCALE_Y];
			applyMatrix.postScale(scaleX, scaleY);
			applyMatrix.postTranslate(left, top);

			RectF src = new RectF();
			src.set(roi);
			RectF dst = new RectF();
			applyMatrix.mapRect(dst, src);
			mROI.set((int) dst.left, (int) dst.top, (int) dst.right,
					(int) dst.bottom);

			float[] pts = new float[8];
			pts[0] = center.x;
			pts[1] = center.y;
			applyMatrix.mapPoints(pts);
			mCenterPT.x = (int) pts[0];
			mCenterPT.y = (int) pts[1];

			pts[0] = mOrgDestPt1.x;
			pts[1] = mOrgDestPt1.y;
			pts[2] = mOrgDestPt2.x;
			pts[3] = mOrgDestPt2.y;
			pts[4] = mOrgDestPt3.x;
			pts[5] = mOrgDestPt3.y;
			pts[6] = mOrgDestPt4.x;
			pts[7] = mOrgDestPt4.y;
			applyMatrix.mapPoints(pts);
			mDestPt1.x = (int) pts[0];
			mDestPt1.y = (int) pts[1];
			mDestPt2.x = (int) pts[2];
			mDestPt2.y = (int) pts[3];
			mDestPt3.x = (int) pts[4];
			mDestPt3.y = (int) pts[5];
			mDestPt4.x = (int) pts[6];
			mDestPt4.y = (int) pts[7];
		}
	}

	protected int InitMoveObject(float x, float y) {
		mTouchType = NONE_TOUCH_TYPE;

		int real_touch_area_offset = AVAILABLE_TOUCH_AREA_OFFSET; 

		if (mROI.width() < AVAILABLE_TOUCH_AREA_OFFSET * 4
				|| mROI.height() < AVAILABLE_TOUCH_AREA_OFFSET * 4) {
			if (mROI.width() > mROI.height()) {
				real_touch_area_offset = (int) (mROI.height() / 4);
			} else {
				real_touch_area_offset = (int) (mROI.width() / 4);
			}
			if (real_touch_area_offset < 1) {
				real_touch_area_offset = 1;
			}
		}

		float h_mid;
		float v_mid;

		if (Math.abs(x - mDestPt2.x) < real_touch_area_offset
				&& Math.abs(y - mDestPt2.y) < real_touch_area_offset) {

			h_mid = (mROI.left + mROI.right) / 2;
			v_mid = (mROI.top + mROI.bottom) / 2;

			mCenterPT.set(h_mid, v_mid);
			mStartPT.set(mROI.right, mROI.top);

			mTouchType = RIGHT_TOP;
		} else if (Math.abs(x - ((mDestPt1.x + mDestPt4.x) / 2)) < real_touch_area_offset
				&& Math.abs(y - ((mDestPt1.y + mDestPt4.y) / 2)) < real_touch_area_offset
				&& mFreeRect)
			mTouchType = LEFT;
		else if (Math.abs(x - ((mDestPt1.x + mDestPt2.x) / 2)) < real_touch_area_offset
				&& Math.abs(y - ((mDestPt1.y + mDestPt2.y) / 2)) < real_touch_area_offset
				&& mFreeRect)
			mTouchType = TOP;
		else if (Math.abs(x - ((mDestPt2.x + mDestPt3.x) / 2)) < real_touch_area_offset
				&& Math.abs(y - ((mDestPt2.y + mDestPt3.y) / 2)) < real_touch_area_offset
				&& mFreeRect)
			mTouchType = RIGHT;
		else if (Math.abs(x - ((mDestPt3.x + mDestPt4.x) / 2)) < real_touch_area_offset
				&& Math.abs(y - ((mDestPt3.y + mDestPt4.y) / 2)) < real_touch_area_offset
				&& mFreeRect)
			mTouchType = BOTTOM;
		else if (Math.abs(x - mDestPt1.x) < real_touch_area_offset
				&& Math.abs(y - mDestPt1.y) < real_touch_area_offset)
			mTouchType = LEFT_TOP;
		else if (Math.abs(x - mDestPt3.x) < real_touch_area_offset
				&& Math.abs(y - mDestPt3.y) < real_touch_area_offset)
			mTouchType = RIGHT_BOTTOM;
		else if (Math.abs(x - mDestPt4.x) < real_touch_area_offset
				&& Math.abs(y - mDestPt4.y) < real_touch_area_offset)
			mTouchType = LEFT_BOTTOM;
		else if (checkPointInRect(x, y, mDestPt1, mDestPt2, mDestPt3, mDestPt4)) {
			mTouchType = MOVE;
		}

		mPrevX = x;
		mPrevY = y;

		return mTouchType;
	}

	protected int limit = 150;

	protected void StartMoveObject(float x, float y) {
		int angle;

		RectF objectROI = new RectF();
		int width = 0;
		int height = 0;

		if (mImageData != null) {
			width = mImageData.getPreviewWidth();
			height = mImageData.getPreviewHeight();
		}

		if (mTouchType == RIGHT_TOP) {
			if (mRightTopRotate) {
				mEndPT.set(x, y);

				angle = getAngle(mCenterPT, mStartPT, mEndPT);
				PointF pt;
				pt = getRotationPoint(mROI.left, mROI.top, angle, mCenterPT.x,
						mCenterPT.y);
				mDestPt1.set(pt.x, pt.y);
				pt = null;

				pt = getRotationPoint(mROI.right, mROI.top, angle, mCenterPT.x,
						mCenterPT.y);
				mDestPt2.set(pt.x, pt.y);
				pt = null;

				pt = getRotationPoint(mROI.right, mROI.bottom, angle,
						mCenterPT.x, mCenterPT.y);
				mDestPt3.set(pt.x, pt.y);
				pt = null;

				pt = getRotationPoint(mROI.left, mROI.bottom, angle,
						mCenterPT.x, mCenterPT.y);
				mDestPt4.set(pt.x, pt.y);
				pt = null;

				if (mBoundaryType == RECT_CENTER_BOUNDARY) {
					mAngle = angle;
				}
			} else {
				resizeRectWithRotationAngle(x, y, width, height, mTouchType);
			}
		} else if (mTouchType > NONE_TOUCH_TYPE) {
			if (mTouchType == MOVE) {
				Log.d("tri.dung", "move");
				float x1, y1, x2, y2, x3, y3, x4, y4;

				float dx = x - mPrevX;
				float dy = y - mPrevY;

				x1 = mDestPt1.x + dx;
				y1 = mDestPt1.y + dy;
				x2 = mDestPt2.x + dx;
				y2 = mDestPt2.y + dy;
				x3 = mDestPt3.x + dx;
				y3 = mDestPt3.y + dy;
				x4 = mDestPt4.x + dx;
				y4 = mDestPt4.y + dy;

				if (mBoundaryType == RECT_CENTER_BOUNDARY) {
					float centerX = (x1 + x2 + x3 + x4) / 4;
					float centerY = (y1 + y2 + y3 + y4) / 4;

					if (centerX >= 0 && centerX <= width - 1) {
						mDestPt1.x = x1;
						mDestPt2.x = x2;
						mDestPt3.x = x3;
						mDestPt4.x = x4;
					}

					if (centerY >= 0 && centerY <= height - 1) {
						mDestPt1.y = y1;
						mDestPt2.y = y2;
						mDestPt3.y = y3;
						mDestPt4.y = y4;
					}
				}
				float objectW = mROI.right - mROI.left + 1;
				float objectH = mROI.bottom - mROI.top + 1;

				objectROI.left = Math.min(mDestPt4.x,
						Math.min(mDestPt3.x, Math.min(mDestPt1.x, mDestPt2.x)));
				objectROI.right = Math.max(mDestPt4.x,
						Math.max(mDestPt3.x, Math.max(mDestPt1.x, mDestPt2.x)));
				objectROI.top = Math.min(mDestPt4.y,
						Math.min(mDestPt3.y, Math.min(mDestPt1.y, mDestPt2.y)));
				objectROI.bottom = Math.max(mDestPt4.y,
						Math.max(mDestPt3.y, Math.max(mDestPt1.y, mDestPt2.y)));

				mCenterPT.x = (objectROI.left + objectROI.right) / 2;
				mCenterPT.y = (objectROI.top + objectROI.bottom) / 2;

				PointF pt;
				pt = getRotationPoint(mDestPt1, -mAngle, mCenterPT.x,
						mCenterPT.y);

				mROI.left = pt.x;
				mROI.top = pt.y;
				mROI.right = mROI.left + objectW - 1;
				mROI.bottom = mROI.top + objectH - 1;

				pt = null;
			} else if (mTouchType >= LEFT && mTouchType <= LEFT_BOTTOM) {
				resizeRectWithRotationAngle(x, y, width, height, mTouchType);
			}
		}

		objectROI = null;

		mPrevX = x;
		mPrevY = y;
//		calculateOriginal();
	}


	private void resizeRectWithRotationAngle(float x, float y, int width,
			int height, int mode) {

		PointF CurrPt1 = getRotationPoint(x, y, -mAngle, mCenterPT.x,
				mCenterPT.y);
		PointF CurrPt2 = getRotationPoint(mPrevX, mPrevY, -mAngle, mCenterPT.x,
				mCenterPT.y);

		float dx = CurrPt1.x - CurrPt2.x;
		float dy = CurrPt1.y - CurrPt2.y;
		RectF tempRoi = null;

		if (Math.abs(dx) > limit || Math.abs(dy) > limit)
			return;

		if (mode == LEFT || mode == RIGHT)
			tempRoi = getClipROI(mROI, mode, dx, 0, 0, 0, width, height);
		else if (mode == TOP || mode == BOTTOM)
			tempRoi = getClipROI(mROI, mode, 0, dy, 0, 0, width, height);
		else if (mode == LEFT_TOP || mode == RIGHT_TOP || mode == LEFT_BOTTOM
				|| mode == RIGHT_BOTTOM) {
			float[] diff = getDiffFromCropRatio(dx, dy, mode);
			tempRoi = getClipROI(mROI, mode, diff[0], diff[1], 0, 0, width,
					height);
		}

		CurrPt1 = null;
		CurrPt2 = null;

		if (mBoundaryType == RECT_CENTER_BOUNDARY) {
			if (tempRoi.width() > mInitROI.width() / 4
					&& tempRoi.width() < mInitROI.width() * 2
					&& tempRoi.height() > mInitROI.height() / 4
					&& tempRoi.height() < mInitROI.height() * 2) {
				mROI.set(tempRoi);
			}
		}
	}

	public void EndMoveObject() {
		if (mTouchType == RIGHT_TOP || mTouchType == MOVE) {
			// if(mMoveRectThread != null)
			// mMoveRectThread.threadStop();
			if (mBoundaryType == RECT_CENTER_BOUNDARY) {
				PointF pt;
				pt = getRotationPoint(mROI.left, mROI.top, mAngle, mCenterPT.x,
						mCenterPT.y);
				mDestPt1.set(pt.x, pt.y);
				pt = null;

				pt = getRotationPoint(mROI.right, mROI.top, mAngle,
						mCenterPT.x, mCenterPT.y);
				mDestPt2.set(pt.x, pt.y);
				pt = null;

				pt = getRotationPoint(mROI.right, mROI.bottom, mAngle,
						mCenterPT.x, mCenterPT.y);
				mDestPt3.set(pt.x, pt.y);
				pt = null;

				pt = getRotationPoint(mROI.left, mROI.bottom, mAngle,
						mCenterPT.x, mCenterPT.y);
				mDestPt4.set(pt.x, pt.y);
				pt = null;
			}
		} else if (mTouchType > 0) {
			if (mBoundaryType == RECT_CENTER_BOUNDARY) {
				PointF pt;
				pt = getRotationPoint(mROI.left, mROI.top, mAngle, mCenterPT.x,
						mCenterPT.y);
				mDestPt1.set(pt.x, pt.y);
				pt = null;

				pt = getRotationPoint(mROI.right, mROI.top, mAngle,
						mCenterPT.x, mCenterPT.y);
				mDestPt2.set(pt.x, pt.y);
				pt = null;

				pt = getRotationPoint(mROI.right, mROI.bottom, mAngle,
						mCenterPT.x, mCenterPT.y);
				mDestPt3.set(pt.x, pt.y);
				pt = null;

				pt = getRotationPoint(mROI.left, mROI.bottom, mAngle,
						mCenterPT.x, mCenterPT.y);
				mDestPt4.set(pt.x, pt.y);
				pt = null;

				RectF objectROI = new RectF();
				objectROI.left = Math.min(mDestPt4.x,
						Math.min(mDestPt3.x, Math.min(mDestPt1.x, mDestPt2.x)));
				objectROI.right = Math.max(mDestPt4.x,
						Math.max(mDestPt3.x, Math.max(mDestPt1.x, mDestPt2.x)));
				objectROI.top = Math.min(mDestPt4.y,
						Math.min(mDestPt3.y, Math.min(mDestPt1.y, mDestPt2.y)));
				objectROI.bottom = Math.max(mDestPt4.y,
						Math.max(mDestPt3.y, Math.max(mDestPt1.y, mDestPt2.y)));

				mCenterPT.x = (objectROI.left + objectROI.right) / 2;
				mCenterPT.y = (objectROI.top + objectROI.bottom) / 2;

				objectROI = null;
			}
		}
		mTouchType = 0;
//		calculateOriginal();
	}


	protected void calculateOriginal() {
		if (mImageData != null) {
			float left, top;
			float scaleX, scaleY;
			float[] values = new float[9];
			Matrix applyMatrix = new Matrix();
			Matrix m = null;
			m = mImageData.getSupMatrix();
			Matrix im = new Matrix();
			m.getValues(values);
			if (values[Matrix.MTRANS_X] > 0)
				left = 0;
			else
				left = values[Matrix.MTRANS_X];
			if (values[Matrix.MTRANS_Y] > 0)
				top = 0;
			else
				top = values[Matrix.MTRANS_Y];

			scaleX = values[Matrix.MSCALE_X];
			scaleY = values[Matrix.MSCALE_Y];

			applyMatrix.postScale(scaleX, scaleY);
			applyMatrix.postTranslate(left, top);

			applyMatrix.invert(im);
			RectF src = new RectF();
			src.set(mROI.left, mROI.top, mROI.right, mROI.bottom);
			RectF dst = new RectF();
			im.mapRect(dst, src);
			mOriginalROI.set(dst.left, dst.top, dst.right, dst.bottom);
			float[] pts = new float[8];
			pts[0] = mCenterPT.x;
			pts[1] = mCenterPT.y;
			im.mapPoints(pts);
			mOriginalCenterPT.x = (int) pts[0];
			mOriginalCenterPT.y = (int) pts[1];
			pts[0] = mDestPt1.x;
			pts[1] = mDestPt1.y;
			pts[2] = mDestPt2.x;
			pts[3] = mDestPt2.y;
			pts[4] = mDestPt3.x;
			pts[5] = mDestPt3.y;
			pts[6] = mDestPt4.x;
			pts[7] = mDestPt4.y;
			im.mapPoints(pts);
			mOriginalDestPt1.x = (int) pts[0];
			mOriginalDestPt1.y = (int) pts[1];
			mOriginalDestPt2.x = (int) pts[2];
			mOriginalDestPt2.y = (int) pts[3];
			mOriginalDestPt3.x = (int) pts[4];
			mOriginalDestPt3.y = (int) pts[5];
			mOriginalDestPt4.x = (int) pts[6];
			mOriginalDestPt4.y = (int) pts[7];
			pts = null;
			values = null;
		}
	}

	public PointF getPrevPT() {
		return new PointF(mPrevX, mPrevY);
	}

	public PointF getCenterPT() {
		PointF pt = new PointF();
		pt.set(mCenterPT.x, mCenterPT.y);
		return pt;
	}

	public PointF getOriginalDestPt1() {
		PointF pt = new PointF();
		pt.set(mOriginalDestPt1.x, mOriginalDestPt1.y);
		return pt;
	}

	public PointF getOriginalDestPt2() {
		PointF pt = new PointF();
		pt.set(mOriginalDestPt2.x, mOriginalDestPt2.y);
		return pt;
	}

	public PointF getOriginalDestPt3() {
		PointF pt = new PointF();
		pt.set(mOriginalDestPt3.x, mOriginalDestPt3.y);
		return pt;
	}

	public PointF getOriginalDestPt4() {
		PointF pt = new PointF();
		pt.set(mOriginalDestPt4.x, mOriginalDestPt4.y);
		return pt;
	}

	public void setDestROI(float left, float top, float right, float bottom) {
		mROI.set(left, top, right, bottom);

		int offset_x = (int) ((mROI.left + mROI.right) / 2);
		int offset_y = (int) ((mROI.top + mROI.bottom) / 2);

		PointF pt;
		pt = getRotationPoint(mROI.left, mROI.top, mAngle, offset_x, offset_y);
		mDestPt1.set(pt.x, pt.y);
		pt = null;

		pt = getRotationPoint(mROI.right, mROI.top, mAngle, offset_x, offset_y);
		mDestPt2.set(pt.x, pt.y);
		pt = null;

		pt = getRotationPoint(mROI.right, mROI.bottom, mAngle, offset_x,
				offset_y);
		mDestPt3.set(pt.x, pt.y);
		pt = null;

		pt = getRotationPoint(mROI.left, mROI.bottom, mAngle, offset_x,
				offset_y);
		mDestPt4.set(pt.x, pt.y);
		pt = null;

		mCenterPT.x = (mROI.left + mROI.right) / 2;
		mCenterPT.y = (mROI.top + mROI.bottom) / 2;
	}

	private void initResource(Context context, HeadInfo imgInfo) {
		mImageData = imgInfo;

		mOriginalROI = null;

		mStartPT = null;
		mCenterPT = null;
		mOriginalCenterPT = null;
		mEndPT = null;

		mAngle = 0;

		mStartPT = new PointF();
		mCenterPT = new PointF();
		mOriginalCenterPT = new PointF();
		mEndPT = new PointF();

		mDestPt1 = new PointF();
		mDestPt2 = new PointF();
		mDestPt3 = new PointF();
		mDestPt4 = new PointF();

		mOriginalDestPt1 = new PointF();
		mOriginalDestPt2 = new PointF();
		mOriginalDestPt3 = new PointF();
		mOriginalDestPt4 = new PointF();

		mROI = new RectF();
		mInitROI = new RectF();
		mOriginalROI = new RectF();
		mDrawROI = new RectF();

		mDrawLinePaint = new Paint();
		mDrawLinePaint.setColor(Color.BLACK);
		mDrawLinePaint
				.setPathEffect(new DashPathEffect(new float[] { 5, 5 }, 3));
		mDrawLinePaint.setStyle(Paint.Style.STROKE);
		mDrawLinePaint.setStrokeWidth(1f);

		mDrawLinePaint1 = new Paint();
		mDrawLinePaint1.setColor(Color.WHITE);
		mDrawLinePaint1.setStyle(Paint.Style.STROKE);
		mDrawLinePaint1.setStrokeWidth(1f);

		mRectPaint = new Paint();
		mRectPaint.setStrokeWidth(4f);
		mRectPaint.setStyle(Paint.Style.STROKE);
		mRectPaint.setColor(0xFF115f80);

		mGreyPaint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		mGreyPaint.setColorFilter(f);

		if (mImageData != null) {
			mPreviewWidth = mImageData.getDrawCanvasRoi().width();// mImageData.getPreviewWidth();
			mPreviewHeight = mImageData.getDrawCanvasRoi().height();// mImageData.getPreviewHeight();
		}

	}


	private float[] getDiffFromCropRatio(float dx, float dy, int type) {
		float[] ret = new float[2];
		float absDx = Math.abs(dx);
		float absDy = Math.abs(dy);

		if (absDx > absDy) {
			absDy = (absDx * mROI.height() / (float) mROI.width());
			switch (type) {
			case RIGHT_TOP:
			case LEFT_BOTTOM:
				if (dx < 0) {
					ret[0] = dx;
					ret[1] = absDy;
				} else {
					ret[0] = dx;
					ret[1] = -absDy;
				}
				break;
			case LEFT_TOP:
			case RIGHT_BOTTOM:
				if (dx < 0) {
					ret[0] = dx;
					ret[1] = -absDy;
				} else {
					ret[0] = dx;
					ret[1] = absDy;
				}
				break;
			}
		} else {
			absDx = (absDy * mROI.width() / (float) mROI.height());
			switch (type) {
			case RIGHT_TOP:
			case LEFT_BOTTOM:
				if (dy < 0) {
					ret[0] = absDx;
					ret[1] = dy;
				} else {
					ret[0] = -absDx;
					ret[1] = dy;
				}
				break;
			case LEFT_TOP:
			case RIGHT_BOTTOM:
				if (dy < 0) {
					ret[0] = -absDx;
					ret[1] = dy;
				} else {
					ret[0] = absDx;
					ret[1] = dy;
				}
				break;
			}
		}
		return ret;
	}

	private RectF getClipROI(RectF roi, int type, float dx, float dy,
			int boundaryLeft, int boundaryTop, int boundaryRight,
			int boundaryBottom) {
		RectF ret = new RectF();
		float left = 0, top = 0, right = 0, bottom = 0;
		left = roi.left;
		top = roi.top;
		right = roi.right;
		bottom = roi.bottom;
		if (mBoundaryType == RECT_CENTER_BOUNDARY) {
			switch (type) {
			case LEFT:
				left = roi.left + dx;
				right = roi.right - dx;
				break;
			case TOP:
				top = roi.top + dy;
				bottom = roi.bottom - dy;
				break;
			case RIGHT:
				left = roi.left - dx;
				right = roi.right + dx;
				break;
			case BOTTOM:
				top = roi.top - dy;
				bottom = roi.bottom + dy;
				break;
			case LEFT_TOP:
				left = roi.left + dx;
				right = roi.right - dx;
				top = roi.top + dy;
				bottom = roi.bottom - dy;
				break;
			case RIGHT_TOP:
				left = roi.left - dx;
				right = roi.right + dx;
				top = roi.top + dy;
				bottom = roi.bottom - dy;
				break;
			case RIGHT_BOTTOM:
				left = roi.left - dx;
				right = roi.right + dx;
				top = roi.top - dy;
				bottom = roi.bottom + dy;
				break;
			case LEFT_BOTTOM:
				left = roi.left + dx;
				right = roi.right - dx;
				top = roi.top - dy;
				bottom = roi.bottom + dy;
				break;
			case MOVE:
				left = roi.left;
				right = roi.right;
				top = roi.top;
				bottom = roi.bottom;
				break;
			}
		}
		ret.set(left, top, right, bottom);
		return ret;
	}

	public PointF getDestPt1() {
		return mDestPt1;
	}

	public PointF getDestPt2() {
		return mDestPt2;
	}

	public PointF getDestPt3() {
		return mDestPt3;
	}

	public PointF getDestPt4() {
		return mDestPt4;
	}
	
	static public PointF getRotationPoint(PointF srcPoint, int angle,
			float pivotX, float pivotY) {
		PointF retPt = new PointF();
		float x = srcPoint.x;
		float y = srcPoint.y;
		float radian = (float) Math.toRadians(angle);
		double cos = Math.cos(radian);
		double sin = Math.sin(radian);

		retPt.x = (int) Math.round(((x - pivotX) * cos - (y - pivotY) * sin));
		retPt.y = (int) Math.round(((x - pivotX) * sin + (y - pivotY) * cos));

		retPt.x = retPt.x + pivotX;
		retPt.y = retPt.y + pivotY;
		return retPt;
	}

	static public PointF getRotationPoint(float srcX, float srcY, float angle,
			float pivotX, float pivotY) {
		PointF retPt = new PointF();
		float x = srcX;
		float y = srcY;
		float radian = (float) Math.toRadians(angle);
		double cos = Math.cos(radian);
		double sin = Math.sin(radian);

		retPt.x = (int) Math.round(((x - pivotX) * cos - (y - pivotY) * sin));
		retPt.y = (int) Math.round(((x - pivotX) * sin + (y - pivotY) * cos));

		retPt.x = (int) (retPt.x + pivotX);
		retPt.y = (int) (retPt.y + pivotY);
		return retPt;
	}

	public static final int AVAILABLE_TOUCH_AREA_OFFSET = 40;

	static public boolean checkPointInRect(float x, float y, PointF P1,
			PointF P2, PointF P3, PointF P4) {
		PointF currentPoint = new PointF();
		currentPoint.set(x, y);

		if (ccw(P1, P2, currentPoint) > 0 && ccw(P2, P3, currentPoint) > 0
				&& ccw(P3, P4, currentPoint) > 0
				&& ccw(P4, P1, currentPoint) > 0) {
			currentPoint = null;
			return true;// the point in rect
		} else {
			currentPoint = null;
			return false;// the point out rect
		}
	}

	static public float ccw(PointF p1, PointF p2, PointF p3) {
		return (p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x);
	}

	static public int getDpToPixel(Context context, int dpVal) {
		float density = context.getResources().getDisplayMetrics().density;
		return (int) (dpVal * density + 0.5);
	}

	static public int getAngle(PointF centerPt, PointF startPt, PointF endPt) {
		int angle = 0;
		double fangle;
		PointF a = new PointF();
		PointF b = new PointF();

		a.x = startPt.x - centerPt.x;
		a.y = startPt.y - centerPt.y;

		b.x = endPt.x - centerPt.x;
		b.y = endPt.y - centerPt.y;

		double dist_a = Math.sqrt((double) (a.x * a.x + a.y * a.y));
		double dist_b = Math.sqrt((double) (b.x * b.x + b.y * b.y));
		float a_b = a.x * b.x + a.y * b.y;
		double value = (double) a_b / (double) (dist_a * dist_b);

		float temp = a.x * b.y - a.y * b.x;

		fangle = Math.toDegrees(Math.acos(value));
		angle = (int) (fangle);
		if (temp < 0)
			angle = 360 - angle;

		if (angle > 360)
			angle = angle - 360;

		a = null;
		b = null;

		return angle;
	}
}