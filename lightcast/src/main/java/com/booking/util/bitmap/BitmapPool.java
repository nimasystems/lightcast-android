package com.booking.util.bitmap;

import android.graphics.Bitmap;
import android.os.Handler;

import java.util.Stack;

/**
 * A pool of fixed-size Bitmaps. Leases a managed Bitmap object which is tied to
 * this pool. Bitmaps are put back to the pool instead of actual recycling.
 * <p/>
 * WARNING: This class is NOT thread safe, intended for use from the main thread
 * only.
 */
public class BitmapPool {
    private final int width;
    private final int height;
    private final Bitmap.Config config;
    private final Stack<Bitmap> bitmaps = new Stack<>();
    private final Handler handler = new Handler();
    private boolean isRecycled;

    /**
     * Construct a Bitmap pool with desired Bitmap parameters
     */
    public BitmapPool(int bitmapWidth, int bitmapHeight, Bitmap.Config config) {
        this.width = bitmapWidth;
        this.height = bitmapHeight;
        this.config = config;
    }

    /**
     * Destroy the pool. Any leased IManagedBitmap items remain valid until they
     * are recycled.
     */
    public void recycle() {
        isRecycled = true;
        for (Bitmap bitmap : bitmaps) {
            bitmap.recycle();
        }
        bitmaps.clear();
    }

    /**
     * Get a Bitmap from the pool or create a new one.
     *
     * @return a managed Bitmap tied to this pool
     */
    public IManagedBitmap getBitmap() {
        return new LeasedBitmap(bitmaps.isEmpty() ? Bitmap.createBitmap(width,
                height, config) : bitmaps.pop());
    }

    private class LeasedBitmap implements IManagedBitmap {
        private final Bitmap bitmap;
        private int referenceCounter = 1;

        private LeasedBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        public Bitmap getBitmap() {
            return bitmap;
        }

        @Override
        public void recycle() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (--referenceCounter == 0) {
                        if (isRecycled) {
                            bitmap.recycle();
                        } else {
                            bitmaps.push(bitmap);
                        }
                    }
                }
            });
        }

        @Override
        public IManagedBitmap retain() {
            ++referenceCounter;
            return this;
        }
    }
}