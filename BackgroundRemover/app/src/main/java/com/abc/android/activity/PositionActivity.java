package com.abc.android.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.abc.android.selffie.view.HoverView;
import com.abc.android.selffie.view.TryonView;

public class PositionActivity extends Activity implements OnClickListener {
	private String imagePath;
	private Intent intent;
	private ContentResolver mContentResolver;
	private Bitmap headBitmap;
	private Bitmap bodyBitmap;
	
	HoverView mHoverView;
	double mDensity;
	
	int viewWidth;
	int viewheight;
	int bmWidth;
	int bmHeight;
	
	int actionBarHeight;
	int bottombarHeight;
	double bmRatio;
	double viewRatio; 
	RelativeLayout mLayout;
	
	ImageView bodyImageView;
	TryonView mTryOnView;

	Button saveButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_position);
		mContentResolver = getContentResolver();

		intent = getIntent();
		imagePath = intent.getStringExtra("imagePath");
		
		mLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        mDensity = getResources().getDisplayMetrics().density;
        actionBarHeight = (int)(110*mDensity);
        bottombarHeight = (int)(60*mDensity);
		
		viewWidth = getResources().getDisplayMetrics().widthPixels;
        viewheight = getResources().getDisplayMetrics().heightPixels - actionBarHeight - bottombarHeight;
		
		headBitmap = getBitmap(imagePath, viewWidth*2/3);
        
        mTryOnView = new TryonView(this, headBitmap, viewWidth, viewheight);
        mTryOnView.setLayoutParams(new LayoutParams(viewWidth, viewheight));
        
        mLayout.addView(mTryOnView);
        
        initButton();

	}
	
	public void initButton() {
		bodyImageView = (ImageView) findViewById(R.id.body);
		bodyImageView.setOnClickListener(this);
		
		bodyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.body);
		bodyImageView.setImageBitmap(bodyBitmap);
		
		saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(this);
		
	}

	private Uri getImageUri(String path) {
		return Uri.fromFile(new File(path));
	}

	private Bitmap getBitmap(String path, int maxWidth) {
		Uri uri = getImageUri(path);
		InputStream in = null;
		try {
			final int IMAGE_MAX_SIZE = maxWidth;
			in = mContentResolver.openInputStream(uri);

			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			BitmapFactory.decodeStream(in, null, o);
			in.close();

			int scale = 1;
			if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
				scale = (int) Math.pow(2, (int) Math
						.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
			}

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			in = mContentResolver.openInputStream(uri);
			Bitmap b = BitmapFactory.decodeStream(in, null, o2);
			in.close();
			
			return b;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.eraseButton:
				findViewById(R.id.eraser_layout).setVisibility(View.VISIBLE);
				break;
				
			case R.id.saveButton:
				Toast.makeText(getApplicationContext(), "Image is saved at " + imagePath, Toast.LENGTH_LONG).show();
				break;
				
		}

	}
	
}
