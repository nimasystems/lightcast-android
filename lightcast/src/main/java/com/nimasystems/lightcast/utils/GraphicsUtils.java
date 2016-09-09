package com.nimasystems.lightcast.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;

import com.nimasystems.lightcast.encoding.Base64;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class GraphicsUtils {
    public static Bitmap getRoundedShape(Bitmap scaleBitmapImage,
                                         int targetWidth, int targetHeight) {

        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight,
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        /*
         * Path path = new Path(); path.addCircle(((float) targetWidth - 1) / 2,
		 * ((float) targetHeight - 1) / 2, (Math.min(((float) targetWidth),
		 * ((float) targetHeight)) / 2), Path.Direction.CCW);
		 * 
		 * canvas.clipPath(path);
		 */

        final Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);

        canvas.drawCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth), ((float) targetHeight)) / 2),
                paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

        canvas.drawBitmap(scaleBitmapImage, new Rect(0, 0, scaleBitmapImage.getWidth(),
                scaleBitmapImage.getHeight()), new Rect(0, 0, targetWidth,
                targetHeight), paint);
        return targetBitmap;
    }

    public static Bitmap highlightImage(Bitmap src) {
        // create new bitmap, which will be painted and becomes result image
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth() + 96,
                src.getHeight() + 96, Bitmap.Config.ARGB_8888);
        // setup canvas for painting
        Canvas canvas = new Canvas(bmOut);
        // setup default color
        canvas.drawColor(0, Mode.CLEAR);
        // create a blur paint for capturing alpha
        Paint ptBlur = new Paint();
        ptBlur.setMaskFilter(new BlurMaskFilter(15, Blur.NORMAL));
        int[] offsetXY = new int[2];
        // capture alpha into a bitmap
        Bitmap bmAlpha = src.extractAlpha(ptBlur, offsetXY);
        // create a color paint
        Paint ptAlphaColor = new Paint();
        ptAlphaColor.setColor(0xFFFFFFFF);
        // paint color for captured alpha region (bitmap)
        canvas.drawBitmap(bmAlpha, offsetXY[0], offsetXY[1], ptAlphaColor);
        // free memory
        bmAlpha.recycle();

        // paint the image source
        canvas.drawBitmap(src, 0, 0, null);

        // return out final image
        return bmOut;
    }

    public static Bitmap drawShadow(Bitmap bitmap, int leftRightThk,
                                    int bottomThk, int padTop) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int newW = w - (leftRightThk * 2);
        int newH = h - (bottomThk + padTop);

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(w, h, conf);
        Bitmap sbmp = Bitmap.createScaledBitmap(bitmap, newW, newH, false);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Canvas c = new Canvas(bmp);

        // Left
        int leftMargin = (leftRightThk + 7) / 2;
        Shader lshader = new LinearGradient(0, 0, leftMargin, 0,
                Color.TRANSPARENT, Color.BLACK, TileMode.CLAMP);
        paint.setShader(lshader);
        c.drawRect(0, padTop, leftMargin, newH, paint);

        // Right
        Shader rshader = new LinearGradient(w - leftMargin, 0, w, 0,
                Color.BLACK, Color.TRANSPARENT, TileMode.CLAMP);
        paint.setShader(rshader);
        c.drawRect(newW, padTop, w, newH, paint);

        // Bottom
        Shader bshader = new LinearGradient(0, newH, 0, bitmap.getHeight(),
                Color.BLACK, Color.TRANSPARENT, TileMode.CLAMP);
        paint.setShader(bshader);
        c.drawRect(leftMargin - 3, newH, newW + leftMargin + 3,
                bitmap.getHeight(), paint);
        c.drawBitmap(sbmp, leftRightThk, 0, null);

        return bmp;
    }

    @SuppressWarnings("deprecation")
    public static void makeImageThumbnail(String pathOfInputImage,
                                          String pathOfOutputImage, int jpegCompression, int dstWidth,
                                          int dstHeight) throws FileNotFoundException {

        int inWidth;
        int inHeight;
        jpegCompression = (jpegCompression > 0) ? jpegCompression : 85;

        InputStream in = new FileInputStream(pathOfInputImage);

        BitmapFactory.Options options = new BitmapFactory.Options();

        // decode image size (decode metadata only, not the whole image)
        try {
            options.inJustDecodeBounds = true;
            options.inDither = false; // Disable Dithering mode

            if (android.os.Build.VERSION.SDK_INT < 21) {
                options.inPurgeable = true; // Tell to gc that whether it
                // needs free memory, the Bitmap
                // can be cleared
                options.inInputShareable = true; // Which kind of reference
                // will be used to
                // recover the Bitmap
                // data after being
                // clear, when it will
                // be used in the future
            }

            //options.inTempStorage = new byte[32 * 1024];
            BitmapFactory.decodeStream(in, null, options);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // save width and height
        inWidth = options.outWidth;
        inHeight = options.outHeight;

        // decode full image pre-resized
        in = new FileInputStream(pathOfInputImage);
        options = new BitmapFactory.Options();

        // calc rought re-size (this is no exact resize)
        options.inSampleSize = Math.max(inWidth / dstWidth, inHeight
                / dstHeight);
        // decode full image
        Bitmap roughBitmap = BitmapFactory.decodeStream(in, null, options);

        // calc exact destination size
        Matrix m = new Matrix();
        RectF inRect = new RectF(0, 0, roughBitmap.getWidth(),
                roughBitmap.getHeight());
        RectF outRect = new RectF(0, 0, dstWidth, dstHeight);
        m.setRectToRect(inRect, outRect, Matrix.ScaleToFit.CENTER);
        float[] values = new float[9];
        m.getValues(values);

        // resize bitmap
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(roughBitmap,
                (int) (roughBitmap.getWidth() * values[0]),
                (int) (roughBitmap.getHeight() * values[4]), true);

        // save image
        FileOutputStream out = new FileOutputStream(pathOfOutputImage);
        resizedBitmap
                .compress(Bitmap.CompressFormat.JPEG, jpegCompression, out);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        double inSampleSize = 1;

		/*
         * if (height > reqHeight || width > reqWidth) {
		 * 
		 * final int halfHeight = height / 2; final int halfWidth = width / 2;
		 * 
		 * // Calculate the largest inSampleSize value that is a power of 2 and
		 * // keeps both // height and width larger than the requested height
		 * and width. while ((halfHeight / inSampleSize) > reqHeight &&
		 * (halfWidth / inSampleSize) > reqWidth) { inSampleSize *= 2; } }
		 */

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.floor((float) width / (float) reqWidth);
            } else {
                inSampleSize = Math.floor((float) height / (float) reqHeight);
            }
        }

        return (int) inSampleSize;
    }

    @SuppressWarnings("deprecation")
    public static Bitmap decodeSampledBitmapFromResource(Resources res,
                                                         int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();

        if (android.os.Build.VERSION.SDK_INT < 21) {
            options.inPurgeable = true; // Tell to gc that whether
            // it
            // needs free memory, the
            // Bitmap
            // can be cleared
            options.inInputShareable = true; // Which kind of
            // reference
            // will be used to
            // recover the
            // Bitmap
            // data after being
            // clear, when it
            // will
            // be used in the
            // future
        }

        // options.inTempStorage = new byte[32 * 1024];

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap getScaledBitmap(Context context, int resourceId,
                                         int maxWidth, int maxHeight) {
        Bitmap bitmap = null;

        try {

            InputStream inp = context.getResources()
                    .openRawResource(resourceId);
            //noinspection TryFinallyCanBeTryWithResources
            try {
                byte[] imgBuffer = FileUtils.getBytesFromInputStream(inp);

                if (imgBuffer == null) {
                    return null;
                }

                bitmap = getScaledBitmap(imgBuffer, maxWidth, maxHeight);
            } finally {
                try {
                    inp.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Bitmap getScaledBitmap(String filename, int maxWidth,
                                         int maxHeight) {
        Bitmap bitmap = null;

        try {
            InputStream inp = new FileInputStream(filename);

            //noinspection TryFinallyCanBeTryWithResources
            try {
                byte[] imgBuffer = FileUtils.getBytesFromInputStream(inp);

                if (imgBuffer == null) {
                    return null;
                }

                bitmap = getScaledBitmap(imgBuffer, maxWidth, maxHeight);
            } finally {
                try {
                    inp.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Bitmap getScaledBitmap(String url, int connectTimeout,
                                         int readTimeout, int maxWidth, int maxHeight,
                                         boolean httpAuthEnabled,
                                         String httpAuthUsername,
                                         String httpAuthPassword) {
        Bitmap bitmap = null;

        try {
            URLConnection conn = new URL(url).openConnection();
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);

            if (httpAuthEnabled && !StringUtils.isNullOrEmpty(httpAuthUsername)) {
                String userPassword = httpAuthUsername + (!StringUtils.isNullOrEmpty(httpAuthPassword) ? ":" + httpAuthPassword : "");
                String encoding = Base64.encode(userPassword.getBytes());
                conn.setRequestProperty("Authorization", "Basic " + encoding);
            }

            InputStream inp = (InputStream) conn.getContent();

            if (inp == null) {
                throw new Exception("No input stream");
            }

            try {
                byte[] imgBuffer = FileUtils.getBytesFromInputStream(inp);

                if (imgBuffer == null) {
                    return null;
                }

                bitmap = getScaledBitmap(imgBuffer, maxWidth, maxHeight);
            } finally {
                try {
                    inp.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Bitmap getScaledBitmap(String url, int connectTimeout,
                                         int readTimeout, int maxWidth, int maxHeight) {
        return GraphicsUtils.getScaledBitmap(url, connectTimeout, readTimeout, maxWidth, maxHeight, false, null, null);
    }

    @SuppressWarnings("deprecation")
    public static Bitmap getScaledBitmap(final byte[] imageData, int maxWidth,
                                         int maxHeight) {

        if (imageData == null) {
            return null;
        }

        Bitmap bitmap = null;

        try {
            if (maxWidth > 0 && maxHeight > 0) {
                if (imageData.length > 0) {
                    // First decode with inJustDecodeBounds=true to check
                    // dimensions
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    options.inDither = false; // Disable Dithering mode

                    if (android.os.Build.VERSION.SDK_INT < 21) {
                        options.inPurgeable = true; // Tell to gc that whether
                        // it
                        // needs free memory, the
                        // Bitmap
                        // can be cleared
                        options.inInputShareable = true; // Which kind of
                        // reference
                        // will be used to
                        // recover the
                        // Bitmap
                        // data after being
                        // clear, when it
                        // will
                        // be used in the
                        // future
                    }

                    //options.inTempStorage = new byte[32 * 1024];

                    BitmapFactory.decodeByteArray(imageData, 0,
                            imageData.length, options);

                    // Calculate inSampleSize
                    options.inSampleSize = calculateInSampleSize(options,
                            maxWidth, maxHeight);

                    // Decode bitmap with inSampleSize set
                    options.inJustDecodeBounds = false;

                    bitmap = BitmapFactory.decodeByteArray(imageData, 0,
                            imageData.length, options);
                }
            } else {
                bitmap = BitmapFactory.decodeByteArray(imageData, 0,
                        imageData.length);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}
