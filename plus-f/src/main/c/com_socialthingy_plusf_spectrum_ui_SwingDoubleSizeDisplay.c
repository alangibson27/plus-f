#include "com_socialthingy_plusf_spectrum_ui_SwingDoubleSizeDisplay.h"

int xlimit(int x) {
    if (x < 0) {
        return 0;
    } else if (x > 255) {
        return 255;
    } else {
        return x;
    }
}

int ylimit(int y) {
    if (y < 0) {
        return 0;
    } else if (y > 191) {
        return 191;
    } else {
        return y;
    }
}

int sourcePixelAt(int x, int y) {
    return xlimit(x) + (ylimit(y) * 256);
}

int targetPixelAt(int mainx, int mainy, int subx, int suby) {
    return ((mainx * 2) + subx) + (((mainy * 2) + suby) * (256 * 2));
}

JNIEXPORT void JNICALL Java_com_socialthingy_plusf_spectrum_ui_SwingDoubleSizeDisplay_scale
  (JNIEnv *env, jobject obj, jintArray sourcePixels, jintArray targetPixels) {

    jint *src = (*env)->GetIntArrayElements(env, sourcePixels, 0);
    jint *tgt = (*env)->GetIntArrayElements(env, targetPixels, 0);
    for (int x = 0; x < 256; x++) {
        for (int y = 0; y < 192; y++) {
            int a = src[sourcePixelAt(x, y - 1)];
            int c = src[sourcePixelAt(x - 1, y)];
            int p = src[sourcePixelAt(x, y)];
            int b = src[sourcePixelAt(x + 1, y)];
            int d = src[sourcePixelAt(x, y + 1)];

            int e0 = (c == a && c != d && a != b) ? a : p;
            int e1 = (a == b && a != c && b != d) ? b : p;
            int e2 = (d == c && d != b && c != a) ? c : p;
            int e3 = (b == d && b != a && d != c) ? d : p;

            tgt[targetPixelAt(x, y, 0, 0)] = e0;
            tgt[targetPixelAt(x, y, 1, 0)] = e1;
            tgt[targetPixelAt(x, y, 0, 1)] = e2;
            tgt[targetPixelAt(x, y, 1, 1)] = e3;
        }
    }

    (*env)->ReleaseIntArrayElements(env, sourcePixels, src, 0);
    (*env)->ReleaseIntArrayElements(env, targetPixels, tgt, 0);
}

/*
                final int a = unsafe.getInt(sourcePixels, BASE + (sourcePixelAt(x, y - 1) * UnsafeUtil.SCALE));
                final int c = unsafe.getInt(sourcePixels, BASE + (sourcePixelAt(x - 1, y) * UnsafeUtil.SCALE));
                final int p = unsafe.getInt(sourcePixels, BASE + (sourcePixelAt(x, y) * UnsafeUtil.SCALE));
                final int b = unsafe.getInt(sourcePixels, BASE + (sourcePixelAt(x + 1, y) * UnsafeUtil.SCALE));
                final int d = unsafe.getInt(sourcePixels, BASE + (sourcePixelAt(x, y + 1) * UnsafeUtil.SCALE));

                final int e0 = (c == a && c != d && a != b) ? a : p;
                final int e1 = (a == b && a != c && b != d) ? b : p;
                final int e2 = (d == c && d != b && c != a) ? c : p;
                final int e3 = (b == d && b != a && d != c) ? d : p;

                unsafe.putInt(targetPixels, BASE + (targetPixelAt(x, y, 0, 0) * UnsafeUtil.SCALE), e0);
                unsafe.putInt(targetPixels, BASE + (targetPixelAt(x, y, 1, 0) * UnsafeUtil.SCALE), e1);
                unsafe.putInt(targetPixels, BASE + (targetPixelAt(x, y, 0, 1) * UnsafeUtil.SCALE), e2);
                unsafe.putInt(targetPixels, BASE + (targetPixelAt(x, y, 1, 1) * UnsafeUtil.SCALE), e3);
*/