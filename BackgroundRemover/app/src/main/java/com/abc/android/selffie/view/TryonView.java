package com.abc.android.selffie.view;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;

import com.abc.android.activity.R;

public class TryonView extends View {

	static Context mContext;
	public static int mode = 0;
	public static int magicTouch = 0;

	static Bitmap headBitmap;
	private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
	
    private final int ICON_WIDTH = 23;
	private final int ICON_HEIGHT = 23;
	private Bitmap mBm_lrtb = null, mBm_rotate = null;
    private HeadController mHeadRect = null;
    private HeadInfo mImageData;
    private boolean showRect = true;
    
	public TryonView(Context context, Bitmap head, int w, int h) {
		super(context);
		mContext = context;
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		init(head, w, h);
	}

	public static void save() {
		Bitmap bitmap =  headBitmap;

		String filename = "pippo.png";
		File sd = Environment.getExternalStorageDirectory();
		File dest = new File(sd, filename);

		try {
			FileOutputStream out = new FileOutputStream(dest);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void init(Bitmap head, int w, int h) {
		mBitmapPaint.setAntiAlias(true);
		
		headBitmap = head;
		mImageData = new HeadInfo(headBitmap, w, h);
		
		Rect objROI = new Rect();
		objROI.set(mImageData.getDrawCanvasRoi());
		mHeadRect = new HeadController(mContext, objROI, mImageData, HeadController.RECT_CENTER_BOUNDARY, false);
		
		Bitmap bm_lrtb = BitmapFactory.decodeResource( mContext.getResources(), R.drawable.photo_crop_handler);
		Bitmap bm_rotate = BitmapFactory.decodeResource( mContext.getResources(), R.drawable.photo_crop_handler_rotate);
		int dstWidth = (int) (ICON_WIDTH * mContext.getResources().getDisplayMetrics().density);
		int dstHeight = (int) (ICON_HEIGHT * mContext.getResources().getDisplayMetrics().density);
		mBm_lrtb = Bitmap.createScaledBitmap(bm_lrtb, dstWidth, dstHeight, true);
		mBm_rotate = Bitmap.createScaledBitmap(bm_rotate, dstWidth, dstHeight, true);
		bm_lrtb.recycle();
		bm_rotate.recycle();
	}

	
	@Override
	protected void onDraw(Canvas canvas) {
//		canvas.drawBitmap(bm, 0, 0, mBitmapPaint);
		drawRectBdry(canvas, new Matrix());
		
		super.onDraw(canvas);
	}

    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
		boolean ret = true;
		switch(event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
				mHeadRect.InitMoveObject(event.getX()-mImageData.getPreviewPaddingX(),
						event.getY()-mImageData.getPreviewPaddingY());
			break;
		case MotionEvent.ACTION_MOVE:
				mHeadRect.StartMoveObject(event.getX()-mImageData.getPreviewPaddingX(),
						event.getY()-mImageData.getPreviewPaddingY());
			break;
		case MotionEvent.ACTION_UP:
				mHeadRect.EndMoveObject();
				if(mHeadRect.getDrawBdry().contains(event.getX() , event.getY())) {
					showRect = true;
				} else showRect = false;
			break;
		}
		invalidate();
		return ret;
	}
    
    private void drawRectBdry(Canvas canvas, Matrix viewTransformMatrix) {
		if(mHeadRect == null)
			return;
		Paint paint = new Paint();

		paint.setColor(0xFF115f80);
		paint.setStyle(Paint.Style.FILL);

		float[] point = new float[2];
		point[0] = mHeadRect.getDrawCenterPt().x;
		point[1] = mHeadRect.getDrawCenterPt().y;
		viewTransformMatrix.mapPoints(point);
		
		RectF drawRoi = new RectF();
		drawRoi.set(mHeadRect.getDrawBdry());
		
		if( mHeadRect != null ) {
			if(mHeadRect.GetObjectBitmap() != null)
			{
				canvas.save();
				canvas.rotate( mHeadRect.getAngle(), point[0], point[1]);

				int width = mHeadRect.GetObjectBitmap().getWidth();
				int height = mHeadRect.GetObjectBitmap().getHeight();
				Rect s = new Rect();
				s.set(0, 0, width, height);
				if(!mHeadRect.GetObjectBitmap().isRecycled())
					canvas.drawBitmap( mHeadRect.GetObjectBitmap(), s, drawRoi, null);
				canvas.restore();
			}
			
			if(showRect) {
				canvas.save();
				canvas.rotate( mHeadRect.getAngle(), point[0], point[1]);

				canvas.drawRect( drawRoi.left - 2, drawRoi.top - 2, 
						drawRoi.right + 2, drawRoi.top + 2, paint );
				canvas.drawRect( drawRoi.right - 2, drawRoi.top - 2, 
						drawRoi.right + 2, drawRoi.bottom + 2, paint );
				canvas.drawRect( drawRoi.left - 2, drawRoi.bottom - 2, 
						drawRoi.right + 2, drawRoi.bottom + 2, paint );
				canvas.drawRect( drawRoi.left - 2, drawRoi.top - 2, 
						drawRoi.left + 2, drawRoi.bottom + 2, paint );

				canvas.drawBitmap(mBm_lrtb, drawRoi.left - mBm_lrtb.getWidth()/2, drawRoi.top - mBm_lrtb.getHeight()/2, null);
				canvas.drawBitmap(mBm_lrtb, drawRoi.left - mBm_lrtb.getWidth()/2, drawRoi.bottom - mBm_lrtb.getHeight()/2, null);
				canvas.drawBitmap(mBm_lrtb, drawRoi.right - mBm_lrtb.getWidth()/2, drawRoi.bottom - mBm_lrtb.getHeight()/2, null);
				canvas.drawBitmap(mBm_rotate, drawRoi.right - mBm_rotate.getWidth()/2, drawRoi.top - mBm_rotate.getHeight()/2, null);

				canvas.restore();
			}
			
		}

	}
    

}
