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