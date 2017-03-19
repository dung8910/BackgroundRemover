package com.abc.android.selffie.view;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.abc.android.activity.EraserActivity;
import com.abc.android.activity.R;

public class HoverView extends View {

	public Context mContext;
	public static int mode = 0;

	Bitmap bm;
	Bitmap clippedBitmap;
	Bitmap magicPointer;
	int[] saveBitmapData;
	int[] lastBitmapData;
	int viewWidth, viewHeight;
	int bmWidth, bmHeight;
	
	
	static Canvas newCanvas;
	static Paint eraser, uneraser;
	private Paint mBitmapPaint;
	private Paint mMaskPaint;
	
	private static Path mPath, mPathErase;
	public static int ERASE_MODE = 0;
	public static int UNERASE_MODE = 1;
	public static int MAGIC_MODE = 2;
	public static int MAGIC_MODE_RESTORE = 3;
	public static int MOVING_MODE = 4;
	public static int MIRROR_MODE = 5;
	
	public PointF touchPoint;
	public PointF drawingPoint;
	
	public int magicTouchRange = 200;
	public int magicThreshold = 15;
	private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    private int strokeWidth = 40;

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int touchMode = NONE;
    String TAG = "tri.dung";
    
    ArrayList<int[]> stackChange;
    ArrayList<Boolean> checkMirrorStep;
    int currentIndex = -1;
    final int STACKSIZE = 10;
    private String filename;
    
    public static int POINTER_DISTANCE;
    
	public HoverView(Context context, Bitmap bm, int w, int h, int viewwidth, int viewheight) {
		super(context);
		mContext = context;
		viewWidth = viewwidth;
		viewHeight = viewheight;
		bmWidth = w;
		bmHeight = h;
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		init(bm, w, h);
	}

	public void switchMode(int _mode) {
		mode = _mode;
		resetPath();
		saveLastMaskData();
		if(mode == MAGIC_MODE || mode == MAGIC_MODE_RESTORE) {
			magicPointer = BitmapFactory.decodeResource(getResources(), R.drawable.color_select);
		} else if(mode == ERASE_MODE || mode == UNERASE_MODE) {
			magicPointer = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.color_select), strokeWidth + 5, strokeWidth + 5, false);
		}
		invalidate();
		
	}
	
	public int getMode() {
		return mode;
	}

	public void setMagicThreshold(int value) {
		magicThreshold = value;
	}
	
	public void mirrorImage(){
		
		Matrix matrix = new Matrix();
	    matrix.preScale(-1.0f, 1.0f);
	    Bitmap tempBm = bm;
		bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
		tempBm.recycle();
		
		Bitmap tempMask = clippedBitmap;
		clippedBitmap = Bitmap.createBitmap(clippedBitmap, 0, 0, clippedBitmap.getWidth(), clippedBitmap.getHeight(), matrix, true);
		tempMask.recycle();
		
		bm.getPixels(saveBitmapData, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());
		saveLastMaskData();
		
		newCanvas = new Canvas(clippedBitmap);
		newCanvas.save();
		((EraserActivity)mContext).updateUndoButton();
		invalidate();
		addToStack(true);
	}
	
	public String save() {
		Bitmap bitmap =  saveDrawnBitmap();

		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath() + "/BackgroundRemover");
		dir.mkdirs();	
		
		File dest = new File(dir, filename);

		try {
			FileOutputStream out = new FileOutputStream(dest);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		refreshGallery(dest);
		
		return (sdCard + "/BackgroundRemover/" + filename);
	}
	
	private void refreshGallery(File file) {
		Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		mediaScanIntent.setData(Uri.fromFile(file));
		mContext.sendBroadcast(mediaScanIntent);
	}
	
	
	public void setEraseOffset(int offSet) {
		strokeWidth = offSet;
		eraser.setStrokeWidth(offSet);
		uneraser.setStrokeWidth(offSet);
		magicPointer = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.color_select), offSet + 5, offSet + 5, false);
		mPath.reset();
		resetPath();
		invalidate();
	}

	void init(Bitmap bitmap, int w, int h) {
		mPath = new Path();
		mPathErase = new Path();
		
		eraser = new Paint();
		eraser.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		eraser.setAntiAlias(true);
		eraser.setStyle(Paint.Style.STROKE);
		eraser.setStrokeJoin(Paint.Join.ROUND);
		eraser.setStrokeCap(Paint.Cap.ROUND);
		eraser.setStrokeWidth(strokeWidth);
		
		uneraser = new Paint();
		uneraser.setXfermode(new PorterDuffXfermode(Mode.SRC_ATOP));
		uneraser.setAntiAlias(true);
		uneraser.setStyle(Paint.Style.STROKE);
		uneraser.setStrokeJoin(Paint.Join.ROUND);
		uneraser.setStrokeCap(Paint.Cap.ROUND);
		uneraser.setStrokeWidth(strokeWidth);
		
		matrix.postTranslate((viewWidth - w)/2, (viewHeight - h)/2);
		
		mBitmapPaint = new Paint();
		mBitmapPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		mBitmapPaint.setAntiAlias(true);
		
		mMaskPaint = new Paint();
		mMaskPaint.setAntiAlias(true);
		
		bm = bitmap;
		bm = bm.copy(Bitmap.Config.ARGB_8888, true);
		
		clippedBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		
		newCanvas = new Canvas(clippedBitmap);
		newCanvas.save();
		// Draws the mask photo.
		newCanvas.drawARGB(255, 255, 255, 255);
//		newCanvas.drawBitmap(bm, 0, 0, mMaskPaint);
		
		magicTouchRange = w > h ? h/2:w/2;
		
		saveBitmapData = new int[w * h];
		bm.getPixels(saveBitmapData, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());
		
		lastBitmapData = new int[w * h];
		
		magicPointer = BitmapFactory.decodeResource(getResources(), R.drawable.color_select);
		touchPoint = new PointF(w/2, h/2);
		drawingPoint = new PointF(w/2, h/2);
		
		saveLastMaskData();
		stackChange = new ArrayList<int[]>();
		checkMirrorStep = new ArrayList<Boolean>();
		addToStack(false);
		
		filename = "img_" + String.format("%d.jpg", System.currentTimeMillis());
		
		POINTER_DISTANCE = (int) (50*mContext.getResources().getDisplayMetrics().density);
	}
	
	void addToStack(boolean isMirror){
		if(stackChange.size() >= STACKSIZE) {
			stackChange.remove(0);
			if(currentIndex > 0) currentIndex--;
		}
		if(stackChange != null) {
			
			if(currentIndex == 0) {
				int size = stackChange.size();
				for(int i = size -1; i > 0; i--) {
					stackChange.remove(i);
					checkMirrorStep.remove(i);
				}
			}
			
			int[] pix = new int[clippedBitmap.getWidth() * clippedBitmap.getHeight()];
			clippedBitmap.getPixels(pix, 0, clippedBitmap.getWidth(), 0, 0, clippedBitmap.getWidth(), clippedBitmap.getHeight());
			stackChange.add(pix);
			checkMirrorStep.add(isMirror);
			currentIndex = stackChange.size()-1;
		}
	}
	
	public void redo(){
		Log.d(TAG, "Redo");
		resetPath();
		
		if(stackChange != null && stackChange.size() > 0 && currentIndex < stackChange.size() - 1) {
			currentIndex++;
			if(checkMirrorStep.get(currentIndex)) {
				Matrix matrix = new Matrix();
			    matrix.preScale(-1.0f, 1.0f);
				bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
				bm.getPixels(saveBitmapData, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());
			}
			
			int[] pix = stackChange.get(currentIndex);
			clippedBitmap.setPixels(pix, 0, bmWidth, 0, 0, bmWidth, bmHeight);
			invalidate();
			
		}
	}
	
	public void undo(){
		Log.d(TAG, "Undo");
		resetPath();
		
		if(stackChange != null && stackChange.size() > 0 && currentIndex > 0) {
			currentIndex--;
			if(checkMirrorStep.get(currentIndex+1)) {
				Matrix matrix = new Matrix();
			    matrix.preScale(-1.0f, 1.0f);
				bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
				bm.getPixels(saveBitmapData, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());
			}
			
			int[] pix = stackChange.get(currentIndex);
			clippedBitmap.setPixels(pix, 0, bmWidth, 0, 0, bmWidth, bmHeight);
			invalidate();
			
		}
	}
	
	public boolean checkUndoEnable() {
		if(stackChange != null && stackChange.size() > 0 && currentIndex > 0) return true;
		return false;
	}
	
	public boolean checkRedoEnable() {
		if(stackChange != null && stackChange.size() > 0 && currentIndex < stackChange.size() - 1) return true;
		return false;
	}

	public Bitmap drawBitmap(Bitmap bmpDraw) {

		if(mode == ERASE_MODE || mode == UNERASE_MODE) {
			if(mode == ERASE_MODE)  {
				uneraser.setXfermode(new PorterDuffXfermode(Mode.SRC_ATOP));
			}
			else {
				uneraser.setXfermode(new PorterDuffXfermode(Mode.SRC));
			}
			
			float strokeRatio = 1;
			
			if(scale > 1) strokeRatio = scale;
			
			eraser.setStrokeWidth(strokeWidth/strokeRatio);
			uneraser.setStrokeWidth(strokeWidth/strokeRatio);
			
			newCanvas.drawPath(mPath, eraser);
			newCanvas.drawPath(mPathErase, uneraser);
		} 

		return clippedBitmap;
	}
	
	public Bitmap saveDrawnBitmap() {

		Bitmap saveBitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Config.ARGB_8888);
		Paint paint = new Paint();
		
		Canvas cv = new Canvas(saveBitmap);
		cv.save();
		
		// Draws the photo.
		cv.drawBitmap(bm, 0, 0, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		cv.drawBitmap(clippedBitmap, 0, 0, paint);
		
		return saveBitmap;
	}


	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(bm, matrix, mMaskPaint);
		canvas.drawBitmap(drawBitmap(bm), matrix, mBitmapPaint);
		
		if(mode == MAGIC_MODE || mode == MAGIC_MODE_RESTORE || mode == ERASE_MODE || mode == UNERASE_MODE) {
			canvas.drawBitmap(magicPointer, drawingPoint.x-magicPointer.getWidth()/2, drawingPoint.y-magicPointer.getHeight()/2, mMaskPaint);
		}
		 
		super.onDraw(canvas);
	}

	public Bitmap magicEraseBitmap() {
		int mWidth = clippedBitmap.getWidth();
		int mHeight = clippedBitmap.getHeight();

		if(touchPoint != null) {
			int[] pix = new int[clippedBitmap.getWidth() * clippedBitmap.getHeight()];
			clippedBitmap.getPixels(pix, 0, clippedBitmap.getWidth(), 0, 0, clippedBitmap.getWidth(), clippedBitmap.getHeight());
			
			int left, right, top, bottom;
			
			int xTouch = (int)touchPoint.x;
			int yTouch = (int)touchPoint.y;
			
			if(xTouch > mWidth || xTouch < 0 || yTouch > mHeight || yTouch < 0) return clippedBitmap;
			
			int aT = (pix[yTouch * mWidth + xTouch] >> 24) & 0xff;
			
			if(true) {
				int rT = (saveBitmapData[yTouch * mWidth + xTouch] >> 16) & 0xff;
				int gT = (saveBitmapData[yTouch * mWidth + xTouch] >> 8) & 0xff;
				int bT = saveBitmapData[yTouch * mWidth + xTouch] & 0xff;
				
				/*if(xTouch - magicTouchRange < 0) {
					left = 0;
				} else left = xTouch - magicTouchRange;
				
				if(xTouch + magicTouchRange > mWidth) {
					right = mWidth;
				} else right = xTouch + magicTouchRange;
				
				if(yTouch - magicTouchRange < 0) {
					top = 0;
				} else top = yTouch - magicTouchRange;
				
				if(yTouch + magicTouchRange > mHeight) {
					bottom = mHeight;
				} else bottom = yTouch + magicTouchRange;
				*/
//				Log.d("tri.dung", "  x = " + touchPoint.x  + "  y = " + touchPoint.y + " bm width = " + bm.getWidth() + "  bm height = " + bm.getHeight()
//									+ "  left = " + left +"  right = " + right +"  top = " + top +"  bottom = " + bottom + " saveBitmapData " + saveBitmapData.length);
				
				left = 0;
				right = mWidth;
				top = 0;
				bottom = mHeight;
				
				for (int y = top; y < bottom ; y++) {
					for (int x = left; x < right; x++) {
						int index = y * mWidth + x;
						
						int aMask = (pix[index] >> 24) & 0xff;
						int a = (saveBitmapData[index] >> 24) & 0xff;
						int r = (saveBitmapData[index] >> 16) & 0xff;
						int g = (saveBitmapData[index] >> 8) & 0xff;
						int b = saveBitmapData[index] & 0xff;
						
						int lastAlphaMask = (lastBitmapData[index] >> 24) & 0xff;
						
						if(aMask > 0 && Math.abs(r - rT) < magicThreshold && Math.abs(g - gT) < magicThreshold && Math.abs(b - bT) < magicThreshold ){
							pix[index] = (0 << 16) | (0 << 8) | 0 | (0 << 24);
						} else if(lastAlphaMask > 0 && aMask == 0 && (Math.abs(r - rT) >= magicThreshold || Math.abs(g - gT) >= magicThreshold || Math.abs(b - bT) >= magicThreshold )) {
							pix[index] = (r << 16) | (g << 8) | b | (a << 24);
						}
					}

				}

				clippedBitmap.setPixels(pix, 0, mWidth, 0, 0, mWidth, mHeight);
				return clippedBitmap;
			}
			
		}
		

		return clippedBitmap;
	}
	
	
	public Bitmap magicRestoreBitmap() {
		int mWidth = clippedBitmap.getWidth();
		int mHeight = clippedBitmap.getHeight();

		if(touchPoint != null) {
			int[] pix = new int[clippedBitmap.getWidth() * clippedBitmap.getHeight()];
			clippedBitmap.getPixels(pix, 0, clippedBitmap.getWidth(), 0, 0, clippedBitmap.getWidth(), clippedBitmap.getHeight());
			
			int left, right, top, bottom;
			
			int xTouch = (int)touchPoint.x;
			int yTouch = (int)touchPoint.y;
			
			if(xTouch > mWidth || xTouch < 0 || yTouch > mHeight || yTouch < 0) 
				return clippedBitmap;
			
//			int aT = (pix[yTouch * mWidth + xTouch] >> 24) & 0xff;
			
			if(true) {
				int rT = (saveBitmapData[yTouch * mWidth + xTouch] >> 16) & 0xff;
				int gT = (saveBitmapData[yTouch * mWidth + xTouch] >> 8) & 0xff;
				int bT = saveBitmapData[yTouch * mWidth + xTouch] & 0xff;
				
				/*if(xTouch - magicTouchRange < 0) {
					left = 0;
				} else left = xTouch - magicTouchRange;
				
				if(xTouch + magicTouchRange > mWidth) {
					right = mWidth;
				} else right = xTouch + magicTouchRange;
				
				if(yTouch - magicTouchRange < 0) {
					top = 0;
				} else top = yTouch - magicTouchRange;
				
				if(yTouch + magicTouchRange > mHeight) {
					bottom = mHeight;
				} else bottom = yTouch + magicTouchRange;*/
				
				left = 0;
				right = mWidth;
				top = 0;
				bottom = mHeight;
				
				for (int y = top; y < bottom ; y++) {
					for (int x = left; x < right; x++) {
						int index = y * mWidth + x;
						
						
						int aMask = (pix[index] >> 24) & 0xff;
						int lastAlphaValue = (lastBitmapData[index] >> 24) & 0xff;
						
						if(aMask == 0) {
							int a = (saveBitmapData[index] >> 24) & 0xff;
							int r = (saveBitmapData[index] >> 16) & 0xff;
							int g = (saveBitmapData[index] >> 8) & 0xff;
							int b = saveBitmapData[index] & 0xff;
							if(Math.abs(r - rT) < magicThreshold && Math.abs(g - gT) < magicThreshold && Math.abs(b - bT) < magicThreshold){
								pix[index] = (r << 16) | (g << 8) | b | (a << 24);
							}
						} else if(aMask > 0 && lastAlphaValue == 0 ) {
							int a = (saveBitmapData[index] >> 24) & 0xff;
							int r = (saveBitmapData[index] >> 16) & 0xff;
							int g = (saveBitmapData[index] >> 8) & 0xff;
							int b = saveBitmapData[index] & 0xff;
							if(Math.abs(r - rT) >= magicThreshold || Math.abs(g - gT) >= magicThreshold || Math.abs(b - bT) >= magicThreshold){
								pix[index] = (0 << 16) | (0 << 8) | 0 | (0 << 24);
							}
						}
						
						
					}

				}

				clippedBitmap.setPixels(pix, 0, mWidth, 0, 0, mWidth, mHeight);
				return clippedBitmap;
			}
			
		}
		

		return clippedBitmap;
	}
	
	public void saveLastMaskData() {
		clippedBitmap.getPixels(lastBitmapData, 0, clippedBitmap.getWidth(), 0, 0, clippedBitmap.getWidth(), clippedBitmap.getHeight());
	}
	
	public void resetPath() {
		mPath.reset();
		mPathErase.reset();
	}
	
	
	public void invalidateView() {
		invalidate();
	}
	
    private void touch_start(float x, float y) {
        mPath.reset();
        mPathErase.reset();
        
        if(mode == ERASE_MODE) mPath.moveTo(x, y);
        else mPathErase.moveTo(x, y);
        mX = x;
        mY = y;
    }
    
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
        	if(mode == ERASE_MODE) mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
        	else mPathErase.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
    }
    
    private void touch_up() {
    	if(mode == ERASE_MODE) mPath.lineTo(mX, mY);
    	else mPathErase.lineTo(mX, mY);
    }
    
    PointF DownPT = new PointF(); // Record Mouse Position When Pressed Down
    
 // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    float scale = 1.0f;
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        if(mode == ERASE_MODE || mode == UNERASE_MODE) {
        	y = y - POINTER_DISTANCE;
        }
        
        if(mode == MAGIC_MODE || mode == MAGIC_MODE_RESTORE || mode == ERASE_MODE || mode == UNERASE_MODE) {
        	drawingPoint.x = x;
        	drawingPoint.y = y;
        }
        
        if(mode != MOVING_MODE) {
        	float[] v = new float[9];
        	matrix.getValues(v);
            // translation 
            float mScalingFactor = v[Matrix.MSCALE_X];
            
            RectF r = new RectF(); 
            matrix.mapRect(r);
            
         // mScalingFactor shall contain the scale/zoom factor
            float scaledX = (x - r.left);
            float scaledY = (y - r.top);

            scaledX /= mScalingFactor;
            scaledY /= mScalingFactor;
            
            x = scaledX;
            y = scaledY;
        }
        
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {
            case MotionEvent.ACTION_DOWN:
            	
            	savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                
                touchMode = DRAG;
            	
                if(mode == ERASE_MODE || mode == UNERASE_MODE) {
                	touch_start(x, y);
                }
                else if(mode == MOVING_MODE) {
                	DownPT.x = event.getX();
                    DownPT.y = event.getY();
                	
                }
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
            	if(touchMode == DRAG) {
            		if(mode == ERASE_MODE || mode == UNERASE_MODE) touch_move(x, y);
                	else if(mode == MOVING_MODE) {
                    	PointF mv = new PointF( event.getX() - DownPT.x, event.getY() - DownPT.y);
                    	matrix.postTranslate(mv.x, mv.y);
                        DownPT.x = event.getX();
                        DownPT.y = event.getY();
                    } else if(mode == MAGIC_MODE || mode == MAGIC_MODE_RESTORE) {
                		touchPoint.x = x;
                		touchPoint.y = y;
                	} 
                    invalidate();
            	} else if (touchMode == ZOOM && mode == MOVING_MODE) 
                { 
                    // pinch zooming
                    float newDist = spacing(event);
//                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 5f) 
                    {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist; // setting the scaling of the
                                                    // matrix...if scale > 1 means
                                                    // zoom in...if scale < 1 means
                                                    // zoom out
                        matrix.postScale(scale, scale, mid.x, mid.y);
                        Log.d(TAG, "scale =" + scale);
                    }
                    
                    invalidate();
                }
            	
                break;
            case MotionEvent.ACTION_UP:
            	if(mode == ERASE_MODE || mode == UNERASE_MODE) {
            		touch_up();
            		Log.d(TAG, "add to stack"); 
                	addToStack(false);
            	}
            	else if(mode == MAGIC_MODE || mode == MAGIC_MODE_RESTORE) {
            		touchPoint.x = x;
            		touchPoint.y = y;
            		saveLastMaskData();
            		((EraserActivity)mContext).resetSeekBar();
            	}
            	((EraserActivity)mContext).updateUndoButton();
            	((EraserActivity)mContext).updateRedoButton();
                invalidate();
                resetPath();
                break;
                
            case MotionEvent.ACTION_POINTER_UP: // second finger lifted

                touchMode = NONE;
                Log.d(TAG, "mode=NONE");
                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                oldDist = spacing(event);
//                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    touchMode = ZOOM;
//                    Log.d(TAG, "mode=ZOOM");
                }
                break;
        }
        return true;
    }
    
    
    /*
     * --------------------------------------------------------------------------
     * Method: spacing Parameters: MotionEvent Returns: float Description:
     * checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */

    private float spacing(MotionEvent event) 
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint(PointF point, MotionEvent event) 
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


}
