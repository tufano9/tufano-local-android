package com.tufano.tufanomovil.global;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * This class defines an OnTouchListener that you can attach to any view so you
 * can support panning and zooming.
 *
 * <p> This code has been adapted from the work described here:
 * "Java Pan/Zoom Listener" (http://code.cheesydesign.com/?p=723)
 *
 */
@SuppressWarnings("SameParameterValue")
class PanAndZoomListener implements View.OnTouchListener {

    private float downX;
    private float downY;

    public static class Anchor
    {
        public static final int CENTER = 0;
        public static final int TOPLEFT = 1;
    }

    private static final String TAG = "PanAndZoomListener";
    // We can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    // Remember some things for zooming
    private final PointF start = new PointF();
    private final PointF mid = new PointF();
    private float oldDist = 1f;
    private float zoomActual =1f;
    private final PanZoomCalculator panZoomCalculator;

    public PanAndZoomListener(FrameLayout container, View view, int anchor)
    {
        panZoomCalculator = new PanZoomCalculator(container, view, anchor);
    }

    public boolean onTouch(View view, MotionEvent event)
    {
        // Handle touch events here...
        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
                start.set(event.getX(), event.getY());
                //int h = FragmentGaleria.imageView.getHeight();
                Log.d(TAG, "mode=DRAG");
                mode = DRAG;
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 10f)
                {
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;
            case MotionEvent.ACTION_UP:
                float upX = event.getX();
                float upY = event.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;
                //int h =FragmentGaleria.imageView.getHeight();
                // swipe horizontal?
                if(Math.abs(deltaX) > Math.abs(deltaY))
                {
                    if(Math.abs(deltaX) > 100 && zoomActual==1f)
                    {
                        // left or right
                        if(deltaX < 0)
                        {
                            //Log.d(TAG, "RIGHT SWIPE (ATRASAR) posicion:"+FragmentGaleria.posicion);
                            /*if (FragmentGaleria.posicion>0)
                            {
                                FragmentGaleria.imageView.setImageBitmap(FragmentGaleria.imageIDs[FragmentGaleria.posicion-1]);
                                //FragmentGaleria.gallery.setSelection(FragmentGaleria.posicion-1);
                                FragmentGaleria.posicion--;
                            }*/
                            return true;
                        }
                        if(deltaX > 0)
                        {
                            //Log.d(TAG, "LEFT SWIPE (ADELANTAR) posicion:"+FragmentGaleria.posicion);
                            /*if (FragmentGaleria.posicion<4)
                            {
                                FragmentGaleria.imageView.setImageBitmap(FragmentGaleria.imageIDs[FragmentGaleria.posicion+1]);
                                //FragmentGaleria.gallery.setSelection(FragmentGaleria.posicion+1);
                                FragmentGaleria.posicion++;
                            }*/
                            return true;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG)
                {
                    panZoomCalculator.doPan(event.getX() - start.x, event.getY() - start.y);
                    start.set(event.getX(), event.getY());
                }
                else if (mode == ZOOM)
                {
                    float newDist = spacing(event);
                    //Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 10f)
                    {
                        float scale = newDist / oldDist;
                        oldDist = newDist;
                        panZoomCalculator.doZoom(scale, mid);
                    }
                }
                break;
        }
        return true; // indicate event was handled
    }

    // Determine the space between the first two fingers
    private float spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return ((float) Math.sqrt(x * x + y * y));
    }

    // Calculate the mid point of the first two fingers
    private void midPoint(PointF point, MotionEvent event)
    {
        // ...
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    public class PanZoomCalculator
    {
        /// The current pan position
        PointF currentPan;
        /// The current zoom position
        float currentZoom;
        /// The windows dimensions that we are zooming/panning in
        final View window;
        final View child;
        final Matrix matrix;
        // Pan jitter is a workaround to get the video view to update its layout properly when zoom is changed
        int panJitter = 0;
        final int anchor;

        PanZoomCalculator(View container, View child, int anchor)
        {
            // Initialize class fields
            currentPan = new PointF(0, 0);
            currentZoom = 1f;
            this.window = container;
            this.child = child;
            matrix = new Matrix();
            this.anchor = anchor;
            onPanZoomChanged();
      /* IS THIS COMPATIBLE WITH 2.3.3?
      this.child.addOnLayoutChangeListener(new OnLayoutChangeListener() {
        // This catches when the image bitmap changes, for some reason it doesn't recurse

        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
          onPanZoomChanged();
        }
      });
      */
        }

        public void doZoom(float scale, PointF zoomCenter)
        {

            float oldZoom = currentZoom;

            // multiply in the zoom change
            currentZoom *= scale;

            // this limits the zoom
            currentZoom = Math.max(getMinimumZoom(), currentZoom);
            currentZoom = Math.min(8f, currentZoom);
            //Log.d(TAG,"MinimumZoom: "+getMinimumZoom()+", currentZoom: "+currentZoom);
            zoomActual = currentZoom;
            // Adjust the pan accordingly
            // Need to make it such that the point under the zoomCenter remains under the zoom center after the zoom

            // calculate in fractions of the image so:

            float width = window.getWidth();
            float height = window.getHeight();
            float oldScaledWidth = width * oldZoom;
            float oldScaledHeight = height * oldZoom;
            float newScaledWidth = width * currentZoom;
            float newScaledHeight = height * currentZoom;

            if (anchor == Anchor.CENTER) {

                float reqXPos = ((oldScaledWidth - width) * 0.5f + zoomCenter.x - currentPan.x) / oldScaledWidth;
                float reqYPos = ((oldScaledHeight - height) * 0.5f + zoomCenter.y - currentPan.y) / oldScaledHeight;
                float actualXPos = ((newScaledWidth - width) * 0.5f + zoomCenter.x - currentPan.x) / newScaledWidth;
                float actualYPos = ((newScaledHeight - height) * 0.5f + zoomCenter.y - currentPan.y) / newScaledHeight;

                currentPan.x += (actualXPos - reqXPos) * newScaledWidth;
                currentPan.y += (actualYPos - reqYPos) * newScaledHeight;
            } else {
                // assuming top left
                float reqXPos = (zoomCenter.x - currentPan.x) / oldScaledWidth;
                float reqYPos = (zoomCenter.y - currentPan.y) / oldScaledHeight;
                float actualXPos = (zoomCenter.x - currentPan.x) / newScaledWidth;
                float actualYPos = (zoomCenter.y - currentPan.y) / newScaledHeight;
                currentPan.x += (actualXPos - reqXPos) * newScaledWidth;
                currentPan.y += (actualYPos - reqYPos) * newScaledHeight;
            }

            onPanZoomChanged();
        }

        public void doPan(float panX, float panY)
        {
            currentPan.x += panX;
            currentPan.y += panY;
            //Log.d(TAG, "doPan ("+currentPan.x+","+currentPan.y+")");
            onPanZoomChanged();
        }

        @SuppressWarnings("SameReturnValue")
        private float getMinimumZoom() {
            return 1f;
        }

        /// Call this to reset the Pan/Zoom state machine
        @SuppressWarnings("unused")
        public void reset()
        {
            // Reset zoom and pan
            currentZoom = getMinimumZoom();
            currentPan = new PointF(0f, 0f);
            onPanZoomChanged();
        }

        public void onPanZoomChanged()
        {

            // Things to try: use a scroll view and set the pan from the scrollview
            // when panning, and set the pan of the scroll view when zooming

            float winWidth = window.getWidth();
            float winHeight = window.getHeight();

            if (currentZoom <= 1f)
            {
                //Log.d(TAG, "if 1");
                currentPan.x = 0;
                currentPan.y = 0;
            }
            else if (anchor == Anchor.CENTER)
            {
                //Log.d(TAG, "elseif 1");
                float maxPanX = (currentZoom - 1f) * window.getWidth() * 0.5f;
                float maxPanY = (currentZoom - 1f) * window.getHeight() * 0.5f;
                currentPan.x = Math.max(-maxPanX, Math.min(maxPanX, currentPan.x));
                currentPan.y = Math.max(-maxPanY, Math.min(maxPanY, currentPan.y));
            }
            else
            {
                // assume top left
                //Log.d(TAG, "else 1");
                float maxPanX = (currentZoom - 1f) * window.getWidth();
                float maxPanY = (currentZoom - 1f) * window.getHeight();
                currentPan.x = Math.max(-maxPanX, Math.min(0, currentPan.x));
                currentPan.y = Math.max(-maxPanY, Math.min(0, currentPan.y));
            }

            if (child instanceof ImageView && ((ImageView) child).getScaleType()== ImageView.ScaleType.MATRIX)
            {
                //Log.d(TAG, "if 2");
                ImageView view = (ImageView) child;
                Drawable drawable = view.getDrawable();
                if (drawable != null)
                {
                    //Log.d(TAG, "if 3");
                    Bitmap bm = ((BitmapDrawable) drawable).getBitmap();
                    if (bm != null)
                    {
                        // Limit Pan
                        //Log.d(TAG, "if 4");
                        float bmWidth = bm.getWidth();
                        float bmHeight = bm.getHeight();

                        float fitToWindow = Math.min(winWidth / bmWidth, winHeight / bmHeight);
                        float xOffset = (winWidth - bmWidth * fitToWindow) * 0.5f * currentZoom;
                        float yOffset = (winHeight - bmHeight * fitToWindow) * 0.5f * currentZoom;

                        matrix.reset();
                        matrix.postScale(currentZoom * fitToWindow, currentZoom * fitToWindow);
                        matrix.postTranslate(currentPan.x + xOffset, currentPan.y + yOffset);
                        ((ImageView) child).setImageMatrix(matrix);
                    }
                }
            }
            else
            {
                //Log.d(TAG, "else 2");
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                lp.leftMargin = (int) currentPan.x + panJitter;
                lp.topMargin = (int) currentPan.y;
                lp.width = (int) (window.getWidth() * currentZoom);
                lp.height = (int) (window.getHeight() * currentZoom);
                panJitter ^= 1;

                child.setLayoutParams(lp);
            }
        }
    }
}
