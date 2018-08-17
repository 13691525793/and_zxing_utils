package org.sinoac.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;

/**
 * @author lambert
 * date 2018/8/17 10:57
 * QQ  820306455
 * Email  fuxiaolei@sinoac.org
 * desc
 */
public class ZxUtils {


    /**
     * 在zxing扫码的DecodeHandler.java类中的decode方法解码成功后调用该方法
     *
     * @param data
     * @param width
     * @param height
     */
    public void renderColor(byte[] data, int width, int height) {

        int frameSize = width * height;
        int[] rgba = new int[frameSize];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int y = (0xff & ((int) data[i * width + j]));
                int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                y = y < 16 ? 16 : y;
                int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
                int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));
                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);
                rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
            }
        }
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0, width, 0, 0, width, height);

    }


    /**
     *裁剪zxing扫码截图，去除多余的白边
     * @param bmp
     * @param rawResult
     */
    public void cropBitmap(Bitmap bmp, Result rawResult) {
        bmp = rotatingImageView(bmp);
        bmp = rotatingImage(rawResult.getResultPoints(), bmp);
        compressBitmapWithSoundNoticeAndMultiDirection(bmp, rawResult);
    }

    private void compressBitmapWithSoundNoticeAndMultiDirection(Bitmap bitmap, Result rawResult) {
        //这里是获取扫码框的宽高
        // TODO: 2018/8/17 这里需要修改成自己获取扫码框的宽高
        Rect frame = new Rect();
        // 压缩Bitmap到对应尺寸
        Bitmap firstcutbmp = Bitmap.createBitmap((frame.right - frame.left), (frame.bottom - frame.top), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(firstcutbmp);
        Rect rect = new Rect(0, 0, (frame.right - frame.left), (frame.bottom - frame.top));
        Rect src = new Rect(frame.left, frame.top, frame.right, frame.bottom);
        canvas.drawBitmap(bitmap, src, rect, null);

        ResultPoint[] rstlist = rawResult.getResultPoints();
        int qrwh = (((Double) (Math.sqrt(Math.pow((rstlist[1].getX()
                - rstlist[0].getX()), 2) + Math.pow((rstlist[1].getY()
                - rstlist[0].getY()), 2)) + Math.sqrt(Math.pow((rstlist[2].getX()
                - rstlist[1].getX()), 2) + Math.pow((rstlist[2].getY()
                - rstlist[1].getY()), 2)))).intValue()) / 2;
//        _sx = _sy = _ex = _ey = -1.0;
        double vl = Math.sqrt(Math.pow((rstlist[1].getX() - rstlist[0].getX()), 2)
                + Math.pow((rstlist[1].getY() - rstlist[0].getY()), 2));
        int adjustvalue = ((Float) (((Double) (vl * 3.5 / 18)).floatValue())).intValue() + 2;
        Bitmap bmpout = Bitmap.createBitmap(qrwh + 2 * adjustvalue, qrwh + 2 * adjustvalue, Bitmap.Config.ARGB_8888);
        src = new Rect(((Float) rstlist[1].getX()).intValue()
                - adjustvalue, ((Float) rstlist[1].getY()).intValue()
                - adjustvalue, ((Float) rstlist[1].getX()).intValue()
                + qrwh + adjustvalue,
                ((Float) rstlist[1].getY()).intValue() + qrwh + adjustvalue);
        rect = new Rect(0, 0, qrwh + 2 * adjustvalue, qrwh + 2 * adjustvalue);
        Canvas canvas1 = new Canvas(bmpout);
        canvas1.drawBitmap(firstcutbmp, src, rect, null);
        firstcutbmp.recycle();

    }

    /**
     * 旋转图片
     *
     * @param bitmap
     * @return Bitmap
     */
    private Bitmap rotatingImageView(Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        // 创建新的图片
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    Bitmap bmpoutrotate = null;
    private float angletorotate = 0;
    Matrix matrix;

    private Bitmap rotatingImage(ResultPoint[] rstlist, Bitmap bitmap) {
        //旋转图片 动作
        if (rstlist.length > 2) {
            if (rstlist[0].getX() != rstlist[1].getX()) {
                float tanvl = (rstlist[1].getX() - rstlist[0].getX()) / Math.abs(rstlist[1].getY() - rstlist[0].getY());
                if (tanvl > 0) {
                    angletorotate = ((Double) (Math.atan(tanvl) / Math.PI * 180)).floatValue();
                } else {
                    angletorotate = ((Double) (360 + Math.atan(tanvl) / Math.PI * 180)).floatValue();
                }
                if (angletorotate > 0) {
                    //saveBitmap(bitmap, path);
                    matrix = new Matrix();
//                    matrix.postTranslate(bitmap.getWidth()/2,0);
                    matrix.setRotate(-angletorotate, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
                    bmpoutrotate = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bmpoutrotate);
                    canvas.drawBitmap(bitmap, matrix, null);
                    bitmap.recycle();
                    return bmpoutrotate;
                }
            }
            if (rstlist[0].getY() < rstlist[1].getY() || rstlist[0].getY() < rstlist[2].getY()) {
                if (bmpoutrotate != null) {
                    matrix = new Matrix();
                    matrix.setRotate(180, bmpoutrotate.getWidth() / 2, bmpoutrotate.getHeight() / 2);
                    Bitmap bmpout1 = Bitmap.createBitmap(bmpoutrotate.getWidth(), bmpoutrotate.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bmpout1);
                    canvas.drawBitmap(bmpoutrotate, matrix, null);
                    bmpoutrotate.recycle();
                    return bmpout1;

                }

            }
        }
        return bitmap;
    }


}
