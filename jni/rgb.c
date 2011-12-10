#include <jni.h>
#include <stdio.h>

void toRGB565(unsigned short *yuvs, int widthIn, int heightIn, unsigned int *rgbs, int widthOut, int heightOut) {  
int half_widthIn = widthIn >> 1;
 
  //the end of the luminance data
  int lumEnd = (widthIn * heightIn) >> 1;
  //points to the next luminance value pair
  int lumPtr = 0;
  //points to the next chromiance value pair
  int chrPtr = lumEnd;
  //the end of the current luminance scanline
  int lineEnd = half_widthIn;
 
  int x,y;
  for (y=0;y<heightIn;y++) {
    int yPosOut=(y*widthOut);// >> 1;
    for (x=0;x<half_widthIn;x++) {
      //read the luminance and chromiance values
      int Y1 = yuvs[lumPtr++];
      int Y2 = (Y1 >> 8) & 0xff;
      Y1 = Y1 & 0xff;
      int Cr = yuvs[chrPtr++];
      int Cb = ((Cr >> 8) & 0xff) - 128;
      Cr = (Cr & 0xff) - 128;
 
      int R, G, B;
      //generate first RGB components
      B = Y1 + ((454 * Cb) >> 8);
      if (B < 0) B = 0; if (B > 255) B = 255;
      G = Y1 - ((88 * Cb + 183 * Cr) >> 8);
      if (G < 0) G = 0; if (G > 255) G = 255;
      R = Y1 + ((359 * Cr) >> 8);
      if (R < 0) R = 0; if (R > 255) R = 255;
      int val = ((R & 0xf8) << 8) | ((G & 0xfc) << 3) | (B >> 3);
 
      //generate second RGB components
      B = Y1 + ((454 * Cb) >> 8);
      if (B < 0) B = 0; if (B > 255) B = 255;
      G = Y1 - ((88 * Cb + 183 * Cr) >> 8);
      if (G < 0) G = 0; if (G > 255) G = 255;
      R = Y1 + ((359 * Cr) >> 8);
      if (R < 0) R = 0; if (R > 255) R = 255;
      rgbs[yPosOut+x] = val | ((((R & 0xf8) << 8) | ((G & 0xfc) << 3) | (B >> 3)) << 16);
    }
    //skip back to the start of the chromiance values when necessary
    chrPtr = lumEnd + ((lumPtr  >> 1) / half_widthIn) * half_widthIn;
    lineEnd += half_widthIn;
  }
};

void toRGBBW(unsigned* rgb, unsigned short* yuv420sp, int width, int height) {
    int frameSize = width * height;

    int pix = 0;
    for (pix = 0; pix < frameSize; pix++)
    {
            unsigned pixVal = (0xff & ((unsigned) yuv420sp[pix])) - 16;
            if (pixVal < 0) pixVal = 0;
            if (pixVal > 255) pixVal = 255;
            rgb[pix] = 0xff000000 | (pixVal << 16) | (pixVal << 8) | pixVal;
    }
};


void decodeYUV420SP(unsigned* rgb, unsigned short* yuv420sp, int width, int height) {
    int frameSize = width * height;
    int j = 0;
    int yp = 0;
    int i = 0;
    for (j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (i = 0; i < width; i++, yp++) {
                    int y = (0xff & ((int) yuv420sp[yp])) - 16;
                    if (y < 0) y = 0;
                    if ((i & 1) == 0) {
                            v = (0xff & yuv420sp[uvp++]) - 128;
                            u = (0xff & yuv420sp[uvp++]) - 128;
                    }

                    int y1192 = 1192 * y;
                    int r = (y1192 + 1634 * v);
                    int g = (y1192 - 833 * v - 400 * u);
                    int b = (y1192 + 2066 * u);

                    if (r < 0) r = 0; else if (r > 262143) r = 262143;
                    if (g < 0) g = 0; else if (g > 262143) g = 262143;
                    if (b < 0) b = 0; else if (b > 262143) b = 262143;

                    rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
    }
}

/*
JNIEXPORT void JNICALL Java_pda_lupa_callbacks_MySurfaceCallback_NativeYuv2rgbbw
(JNIEnv *env, jclass clazz, jobject rgb, jbyteArray yuv, jint width, jint height) {

    jbyte *cYuv = (*env)->GetByteArrayElements(env, yuv, NULL);
    
    jint *out = (jint*)(*env)->GetDirectBufferAddress(env, rgb);
    
    toRGBBW((unsigned *) out, (unsigned short*)cYuv, width, height);
    
    
    (*env)->ReleaseByteArrayElements(env, yuv, cYuv, JNI_ABORT);
    

}*/

JNIEXPORT jintArray JNICALL Java_pda_lupa_callbacks_MySurfaceCallback_NativeYuv2rgbbw
(JNIEnv *env, jclass clazz, jbyteArray yuv, jint width, jint height) {

    jbyte *cYuv = (*env)->GetByteArrayElements(env, yuv, NULL);
    
    int size = width*height;
    unsigned result[size];
    jintArray ret = (*env)->NewIntArray(env, size);
    
    toRGBBW(result, (unsigned short*)cYuv, width, height);
    //decodeYUV420SP(result, (unsigned short*)cYuv, width, height);
    
    (*env)->SetIntArrayRegion(env, ret, 0, size, result);
    
    (*env)->ReleaseByteArrayElements(env, yuv, cYuv, JNI_ABORT);
    
    return ret;

}

/**
 * Converts the input image from YUV to a RGB 5_6_5 image.
 * The size of the output buffer must be at least the size of the input image.
 */


JNIEXPORT jintArray JNICALL Java_pda_lupa_callbacks_MySurfaceCallback_NativeYuv2rgb
  (JNIEnv *env, jclass clazz,
  jbyteArray imageIn, jint widthIn, jint heightIn,
  jint size, jint widthOut, jint heightOut) {

        
    jbyte *cImageIn = (*env)->GetByteArrayElements(env, imageIn, NULL);

    //jbyte *cImageOut = (jbyte*)(*env)->GetDirectBufferAddress(env, imageOut);
    
         unsigned result[size];
         jintArray ret = (*env)->NewIntArray(env, size);


        toRGB565((unsigned short*)cImageIn, widthIn, heightIn, result, widthOut, heightOut);
        
        (*env)->SetIntArrayRegion(env, ret, 0, size, result);
        
        (*env)->ReleaseByteArrayElements(env, imageIn, cImageIn, JNI_ABORT);
    
    
        return ret;
}