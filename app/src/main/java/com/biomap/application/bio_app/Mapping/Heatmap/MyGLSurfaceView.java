package com.biomap.application.bio_app.Mapping.Heatmap;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */
public class MyGLSurfaceView extends GLSurfaceView {

	private final MyGLRenderer mRenderer;

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public MyGLSurfaceView(Context context) {
		super(context);

		// Create an OpenGL ES 2.0 context.
		setEGLContextClientVersion(2);

		// Set the Renderer for drawing on the GLSurfaceView
		mRenderer = new MyGLRenderer();

		// Don't fucking change this unless you know what you're doing.
		setZOrderOnTop(false);
		setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		getHolder().setFormat(PixelFormat.RGBA_8888);
		setRenderer(mRenderer);

	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		switch (e.getAction()) {
			case MotionEvent.ACTION_DOWN:
				getRenderer().clear();
				postInvalidate();
				requestRender();
		}
		return true;
	}

	public MyGLRenderer getRenderer() {
		return mRenderer;
	}

}
