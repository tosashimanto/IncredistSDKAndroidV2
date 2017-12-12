//
// Created by ss990011 on 16/03/30.
//

#include "DukptJni.h"
#include <jni.h>

//﻿#include "stdafx.h"
#include <stdio.h>
#include <string.h>
#include <openssl/des.h>
#include <openssl/aes.h>
//#include "openssl/des.h"
//#include "openssl/aes.h"
#include "Dukpt.h"
#include "jp_co_nttdocomo_incredist_docomoif_main_Incredist_DocomoIF.h"

#include <android/log.h>

typedef unsigned char BYTE;
typedef BYTE *LPBYTE;
typedef unsigned short WORD;

//static BYTE bdk1[16] = {0x74, 0x8F, 0x25, 0xA2, 0xF6, 0xD1, 0xA6, 0x52, 0xDF, 0x78, 0xB9, 0x1A,
//                        0x6B, 0xAC, 0x18, 0x3D};
static unsigned char bdk1[16] = {0x74, 0x8F, 0x25, 0xA2, 0xF6, 0xD1, 0xA6, 0x52, 0xDF, 0x78, 0xB9, 0x1A,
                        0x6B, 0xAC, 0x18, 0x3D};
static unsigned char ksi1[5] = {0x1F, 0x70, 0xF3, 0x6D, 0x8A};
static unsigned char aeskey[16] = {0xE9, 0xB5, 0x6D, 0xD1, 0xC9, 0x87, 0x11, 0x49, 0xB0, 0x10, 0xAE, 0xAE,
                          0xC1, 0xF2, 0x69, 0xB3};

static BYTE ksn[10];
static BYTE t1enc[128];
static unsigned char t2enc[64];
static BYTE t3enc[128];
static BYTE t1dec[128];
static BYTE t2dec[64];
static BYTE t3dec[128];

extern "C"{

    JNIEXPORT jobject JNICALL Java_jp_co_nttdocomo_incredist_1docomoif_main_Incredist_1DocomoIF_decrypt2
            (JNIEnv *env, jobject me, jbyteArray ksn, jlong size, const jbyteArray encdata, jbyteArray decdata){
        jobject ret;
        jboolean b;
        jbyte* arrEncData = (*env).GetByteArrayElements(encdata,&b);
        jbyte* arrDecDeta = (*env).GetByteArrayElements(decdata,&b);
        jbyte* arrKsnDeta = (*env).GetByteArrayElements(ksn,&b);
        int encSize = (*env).GetArrayLength(encdata);
        int decSize = (*env).GetArrayLength(decdata);
        int ksnSize = (*env).GetArrayLength(ksn);
        __android_log_print(ANDROID_LOG_DEBUG,"Tag","Decrypt2 : Start");
        __android_log_print(ANDROID_LOG_DEBUG,"Tag","Decrypt2 : encSize : %d", encSize);
        __android_log_print(ANDROID_LOG_DEBUG,"Tag","Decrypt2 : decSize : %d", decSize);

        unsigned char *szEncData = createCharArray(env,encdata);
        unsigned char *szDecData = createCharArray(env,decdata);
        unsigned char *szKsn = createCharArray(env,ksn);

        unsigned char CurrentKey[16];

        DES_key_schedule ks1, ks2;
        DES_cblock k1, k2, iv;

__android_log_print(ANDROID_LOG_DEBUG,"Tag","Decrypt2 : Call 1");
__android_log_print(ANDROID_LOG_DEBUG,"Tag","Decrypt2 : ksn size : %d" , sizeof(ksn));
__android_log_print(ANDROID_LOG_DEBUG,"Tag","Decrypt2 : ksn size : %d" , sizeof(ksnSize));

__android_log_print(ANDROID_LOG_DEBUG,"Tag","Decrypt2 : Call 2");
__android_log_print(ANDROID_LOG_DEBUG,"Tag","Decrypt2 : bdk1 size : %d" , sizeof(bdk1));
        unsigned char tmpbdk[16];
        memcpy(tmpbdk,bdk1,sizeof(bdk1));
        obtainIPEK(tmpbdk, szKsn);
        getNewCurrentKey(szKsn, CurrentKey);

__android_log_print(ANDROID_LOG_DEBUG,"Tag","Decrypt2 : encdata size : %d", sizeof(encdata));
__android_log_print(ANDROID_LOG_DEBUG,"Tag","Decrypt2 : decdata size : %d", sizeof(decdata));
        memcpy(k1, &CurrentKey[0], sizeof(k1));
        memcpy(k2, &CurrentKey[8], sizeof(k2));
        DES_set_key_unchecked(&k1, &ks1);
        DES_set_key_unchecked(&k2, &ks2);


        memset(iv, 0, sizeof(iv));
__android_log_print(ANDROID_LOG_DEBUG,"Tag","Decrypt2 : Call 3");
        DES_ede2_cbc_encrypt(szEncData, szDecData, size, &ks1, &ks2, &iv, DES_DECRYPT);

        //Return Object
        jclass c = env->FindClass("jp/co/nttdocomo/incredist_docomoif/domain/Decrypto");
        jmethodID getStatusMethod,setStatusMethod,getTrack1SizeMethod,setTrack1SizeMethod,
            getTrack1Method,setTrack1Method,getTrack2SizeMethod,
            setTrack2SizeMethod,getTrack2Method,setTrack2Method,paramConstractor;

        getStatusMethod = env->GetMethodID(c,"getStatus","()I");
        setStatusMethod = env->GetMethodID(c,"setStatus","(I)V");
        getTrack1SizeMethod = env->GetMethodID(c,"getTrack1Size","()I");
        setTrack1SizeMethod = env->GetMethodID(c,"setTrack1Size","(I)V");
        getTrack1Method = env->GetMethodID(c,"getTrack1","()[B");
        setTrack1Method = env->GetMethodID(c,"setTrack1","([B)V");
        getTrack2SizeMethod = env->GetMethodID(c,"getTrack2Size","()I");
        setTrack2SizeMethod = env->GetMethodID(c,"setTrack2Size","(I)V");
        getTrack2Method = env->GetMethodID(c,"getTrack2","()[B");
        setTrack2Method = env->GetMethodID(c,"setTrack2","([B)V");
        paramConstractor = env->GetMethodID(c,"<init>","(II[BI[B)V");

__android_log_print(ANDROID_LOG_DEBUG,"Tag","Decrypt2 : jobject Make Start");

        jbyteArray retDecData = createByteArray(env,szDecData,decSize);
        jbyteArray retEncData = createByteArray(env,szEncData,encSize);

        ret = env->NewObject(c,
                             paramConstractor,
                             1,
                             sizeof(szDecData),
                             retDecData,
                             sizeof(szEncData),
                             retEncData
        );

__android_log_print(ANDROID_LOG_DEBUG,"Tag","Decrypt2 : jobject Make End");
        (*env).ReleaseByteArrayElements(encdata,arrEncData,0);
        (*env).ReleaseByteArrayElements(decdata,arrDecDeta,0);
        (*env).ReleaseByteArrayElements(ksn,arrKsnDeta,0);
        return ret;
}

JNIEXPORT jobject JNICALL Java_jp_co_nttdocomo_incredist_1docomoif_main_Incredist_1DocomoIF_aesDecrypt2
        (JNIEnv *env, jobject me, jlong wSize, jbyteArray encdata, jbyteArray decdata){
    __android_log_print(ANDROID_LOG_DEBUG,"Tag","aesDecrypt2 : Call 1");
    jobject ret;
    __android_log_print(ANDROID_LOG_DEBUG,"Tag","aesDecrypt2 : Call 2");
    AES_KEY deckey;
    BYTE iv[AES_BLOCK_SIZE];

    __android_log_print(ANDROID_LOG_DEBUG,"Tag","aesDecrypt2 : Call 3");
    //xxx aeskey(固定:16byte = 128bit) を deckey に設定
    AES_set_decrypt_key(aeskey, 128, &deckey);

    __android_log_print(ANDROID_LOG_DEBUG,"Tag","aesDecrypt2 : Call 4");
    __android_log_print(ANDROID_LOG_DEBUG,"Tag","aesDecrypt2 : encdata size : %d", sizeof(encdata));
    jboolean b;
    jbyte* arrEncData = (*env).GetByteArrayElements(encdata,&b);
    jbyte* arrDecDeta = (*env).GetByteArrayElements(decdata,&b);
    int encSize = (*env).GetArrayLength(encdata);
    int decSize = (*env).GetArrayLength(decdata);
    __android_log_print(ANDROID_LOG_DEBUG,"Tag","aesDecrypt2 : encSize : %d", encSize);
    __android_log_print(ANDROID_LOG_DEBUG,"Tag","aesDecrypt2 : decSize : %d", decSize);
    unsigned char *szEncData = createCharArray(env,encdata);
    unsigned char *szDecData = createCharArray(env,decdata);
    memset(iv, 0, sizeof(iv));



    //xxx 復号
    //xxx  szEncData: encdata から作成
    //xxx  szDecData: decdata から作成←これはまずそう？ 現状は32byte固定の引数を渡している
    //xxx  wSize: パラメータそのもの
    //xxx  deckey: AES_set_decrypt_key で初期化済み
    //xxx  iv: memset で 0初期化済み
    AES_cbc_encrypt(szEncData, szDecData, wSize, &deckey, iv, AES_DECRYPT);

__android_log_print(ANDROID_LOG_DEBUG,"Tag","aesDecrypt2 : szEncData size : %d", sizeof(szEncData));

    //Return Object
    jclass c = env->FindClass("jp/co/nttdocomo/incredist_docomoif/domain/Decrypto");

__android_log_print(ANDROID_LOG_DEBUG,"Tag","aesDecrypt2 : jclass make");

    jmethodID getStatusMethod,setStatusMethod,getTrack1SizeMethod,setTrack1SizeMethod,
            getTrack1Method,setTrack1Method,getTrack2SizeMethod,
            setTrack2SizeMethod,getTrack2Method,setTrack2Method,paramConstractor;


__android_log_print(ANDROID_LOG_DEBUG,"Tag","aesDecrypt2 : jmethodID Define");

    getStatusMethod = env->GetMethodID(c,"getStatus","()I");
    setStatusMethod = env->GetMethodID(c,"setStatus","(I)V");
    getTrack1SizeMethod = env->GetMethodID(c,"getTrack1Size","()I");
    setTrack1SizeMethod = env->GetMethodID(c,"setTrack1Size","(I)V");
    getTrack1Method = env->GetMethodID(c,"getTrack1","()[B");
    setTrack1Method = env->GetMethodID(c,"setTrack1","([B)V");
    getTrack2SizeMethod = env->GetMethodID(c,"getTrack2Size","()I");
    setTrack2SizeMethod = env->GetMethodID(c,"setTrack2Size","(I)V");
    getTrack2Method = env->GetMethodID(c,"getTrack2","()[B");
    setTrack2Method = env->GetMethodID(c,"setTrack2","([B)V");
    paramConstractor = env->GetMethodID(c,"<init>","(II[BI[B)V");


__android_log_print(ANDROID_LOG_DEBUG,"Tag","aesDecrypt2 : jobject Make Start");

//    jbyteArray retDecData = env->NewByteArray(sizeof(szDecData));
//    jbyte* pDecData = env->GetByteArrayElements(retDecData,0);
//    jbyteArray retEncData = env->NewByteArray(sizeof(szEncData));
//    jbyte* pEncData = env->GetByteArrayElements(retEncData,0);
//    int cnt = 0;
//    for(cnt=0; cnt< sizeof(szDecData); cnt++){
//        pDecData[cnt] = (jbyte)szDecData[cnt];
//    }
//    int cntEnc = 0;
//    for(cntEnc=0; cntEnc< sizeof(szEncData); cntEnc++){
//        pEncData[cntEnc] = (jbyte)szEncData[cntEnc];
//    }

    jbyteArray retDecData = createByteArray(env,szDecData,decSize);
    jbyteArray retEncData = createByteArray(env,szEncData,encSize);
    //xxx Decryptoクラスのオブジェクトを生成
    //xxx  status:1
    //xxx  track1size: sizeof(szDecData) はポインタのサイズになるので誤り(szDecDataで良い)
    //xxx  track1: 復号結果
    //xxx  track2size: 同様
    //xxx  track2: 入力暗号文
    ret = env->NewObject(c,
                         paramConstractor,
                         1,
                         sizeof(szDecData),
                         retDecData,
                         sizeof(szEncData),
                         retEncData
    );

__android_log_print(ANDROID_LOG_DEBUG,"Tag","aesDecrypt2 : jobject Make End");

    (*env).ReleaseByteArrayElements(encdata,arrEncData,0);
    (*env).ReleaseByteArrayElements(decdata,arrDecDeta,0);
    return ret;
}

//jbyteArray createByteArray(JNIEnv* env, char* array, int len, jbyte** tmp){
//    int cnt=0;
//    jbyteArray ret = env->NewByteArray(len);
//    jbyte* pret = env->GetByteArrayElements(ret,0);
//    for(cnt = 0; cnt < len; cnt++){
//        pret[cnt] = (jbyte)array[cnt];
//    }
//    *tmp = pret;
//    return ret;
//}

unsigned char* createCharArray(JNIEnv* env,jbyteArray array){
    int len = env->GetArrayLength(array);
    unsigned char* buf = new unsigned char[len];
    env->GetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte*>(buf));
    return buf;
}

jbyteArray createByteArray(JNIEnv* env,unsigned char* buf, int len) {
    jbyteArray array = env->NewByteArray (len);
//    env->SetByteArrayRegion (array, 0, len, reinterpret_cast<jbyte*>(buf));
    jbyte* b = (jbyte*)buf;
    env->SetByteArrayRegion (array, 0, len, b);
    return array;
}

JNIEXPORT jobject JNICALL Java_jp_co_nttdocomo_incredist_1docomoif_main_Incredist_1DocomoIF_desEncrypt
        (JNIEnv *env, jobject me, jlong wSize, jbyteArray szOrgData, jbyteArray encdata, jbyteArray sessionKey, jint parity_Flag)
{
    int ret;
    jobject retObj;
    int i;
    DES_key_schedule schedule[0x01];
    jboolean b;
    jbyte* arrEncData = (*env).GetByteArrayElements(encdata,&b);
    jbyte* arrDecDeta = (*env).GetByteArrayElements(szOrgData,&b);
    jbyte* arrSesDeta = (*env).GetByteArrayElements(sessionKey,&b);
    int encSize = (*env).GetArrayLength(encdata);
    int decSize = (*env).GetArrayLength(szOrgData);
    int sesSize = (*env).GetArrayLength(sessionKey);
    __android_log_print(ANDROID_LOG_DEBUG,"Tag","desEncrypt : encSize : %d", encSize);
    __android_log_print(ANDROID_LOG_DEBUG,"Tag","desEncrypt : decSize : %d", decSize);
    unsigned char *szEncData = createCharArray(env,encdata);
    unsigned char *szDecData = createCharArray(env,szOrgData);
    unsigned char *natSessionKey = createCharArray(env,sessionKey);

    // キー情報作成
    if( parity_Flag == 1 )
          DES_set_odd_parity((DES_cblock *)natSessionKey);

    // キースケジュール作成
    ret = DES_set_key_checked((DES_cblock *)natSessionKey, schedule);

    // 暗号化
    for(i=0; i<wSize/8; i++)
        DES_ecb_encrypt((const_DES_cblock *)&szDecData[i*8], (DES_cblock *)&szEncData[i*8], schedule, DES_ENCRYPT);

    //Return Object
    jclass c = env->FindClass("jp/co/nttdocomo/incredist_docomoif/domain/DesDecrypto");
    jmethodID getStatusMethod,setStatusMethod,getTrack1SizeMethod,setTrack1SizeMethod,
            getTrack1Method,setTrack1Method,getTrack2SizeMethod,
            setTrack2SizeMethod,getTrack2Method,setTrack2Method,
            getSessionKeyMethod,setSessionKeyMethod,paramConstractor;

    getStatusMethod = env->GetMethodID(c,"getStatus","()I");
    setStatusMethod = env->GetMethodID(c,"setStatus","(I)V");
    getTrack1SizeMethod = env->GetMethodID(c,"getTrack1Size","()I");
    setTrack1SizeMethod = env->GetMethodID(c,"setTrack1Size","(I)V");
    getTrack1Method = env->GetMethodID(c,"getTrack1","()[B");
    setTrack1Method = env->GetMethodID(c,"setTrack1","([B)V");
    getTrack2SizeMethod = env->GetMethodID(c,"getTrack2Size","()I");
    setTrack2SizeMethod = env->GetMethodID(c,"setTrack2Size","(I)V");
    getTrack2Method = env->GetMethodID(c,"getTrack2","()[B");
    setTrack2Method = env->GetMethodID(c,"setTrack2","([B)V");
//    getSessionKeyMethod = env->GetMethodID(c,"getSessionKey","()Ljava/lang/String;");
//    setSessionKeyMethod = env->GetMethodID(c,"setSessionKey","(Ljava/lang/String;)V");
    getSessionKeyMethod = env->GetMethodID(c,"getSessionKey","()[B");
    setSessionKeyMethod = env->GetMethodID(c,"setSessionKey","([B)V");
//    paramConstractor = env->GetMethodID(c,"<init>","(II[BI[BLjava/lang/String;)V");
    paramConstractor = env->GetMethodID(c,"<init>","(II[BI[B[B)V");

    __android_log_print(ANDROID_LOG_DEBUG,"Tag","desEncrypt : jobject Make Start");

    jbyteArray retDecData = createByteArray(env,szDecData,decSize);
    jbyteArray retEncData = createByteArray(env,szEncData,encSize);
    jbyteArray retStr = createByteArray(env,natSessionKey,sesSize);
//    jstring  retStr = env->NewStringUTF(natSessionKey);

    retObj = env->NewObject(c,
                         paramConstractor,
                         1,
                         sizeof(szOrgData),
                         retDecData,
                         sizeof(encdata),
                         retEncData,
                         retStr
    );

    __android_log_print(ANDROID_LOG_DEBUG,"Tag","desEncrypt : jobject Make End");
    (*env).ReleaseByteArrayElements(encdata,arrEncData,0);
    (*env).ReleaseByteArrayElements(szOrgData,arrDecDeta,0);
//    (*env).ReleaseStringUTFChars(sessionKey,natSessionKey);
    (*env).ReleaseByteArrayElements(sessionKey,arrSesDeta,0);
    return retObj;
}

JNIEXPORT jobject JNICALL Java_jp_co_nttdocomo_incredist_1docomoif_main_Incredist_1DocomoIF_desDecrypt
        (JNIEnv *env, jobject me, jlong wSize, jbyteArray encdata, jbyteArray decdata, jbyteArray sessionKey){

    int ret;
    jobject retObj;
    int i;
    DES_key_schedule schedule[0x01];

    __android_log_print(ANDROID_LOG_DEBUG,"Tag","desDecrypt : encdata size : %d", sizeof(encdata));
    jboolean b;
    jbyte* arrEncData = (*env).GetByteArrayElements(encdata,&b);
    jbyte* arrDecDeta = (*env).GetByteArrayElements(decdata,&b);
    jbyte* arrSesDeta = (*env).GetByteArrayElements(sessionKey,&b);
    int encSize = (*env).GetArrayLength(encdata);
    int decSize = (*env).GetArrayLength(decdata);
    int sesSize = (*env).GetArrayLength(sessionKey);
    __android_log_print(ANDROID_LOG_DEBUG,"Tag","desDecrypt : encSize : %d", encSize);
    __android_log_print(ANDROID_LOG_DEBUG,"Tag","desDecrypt : decSize : %d", decSize);
    unsigned char *szEncData = createCharArray(env,encdata);
    unsigned char *szDecData = createCharArray(env,decdata);
    unsigned char *natSessionKey = createCharArray(env,sessionKey);
//    const char *natSessionKey = env->GetStringUTFChars(sessionKey,&b);

    // キー情報作成
//    DES_set_odd_parity((DES_cblock *)sessionKey);
    DES_set_odd_parity((DES_cblock *)natSessionKey);

    // キースケジュール作成
//    ret = DES_set_key((DES_cblock *)sessionKey, schedule);
    ret = DES_set_key_checked((DES_cblock *)natSessionKey, schedule);

    // 復号
    for(i=0; i<wSize/8; i++)
        DES_ecb_encrypt((const_DES_cblock *)&szEncData[i*8], (DES_cblock *)&szDecData[i*8], schedule, DES_DECRYPT);

    //Return Object
    jclass c = env->FindClass("jp/co/nttdocomo/incredist_docomoif/domain/DesDecrypto");
    jmethodID getStatusMethod,setStatusMethod,getTrack1SizeMethod,setTrack1SizeMethod,
            getTrack1Method,setTrack1Method,getTrack2SizeMethod,
            setTrack2SizeMethod,getTrack2Method,setTrack2Method,
            getSessionKeyMethod,setSessionKeyMethod,paramConstractor;

    getStatusMethod = env->GetMethodID(c,"getStatus","()I");
    setStatusMethod = env->GetMethodID(c,"setStatus","(I)V");
    getTrack1SizeMethod = env->GetMethodID(c,"getTrack1Size","()I");
    setTrack1SizeMethod = env->GetMethodID(c,"setTrack1Size","(I)V");
    getTrack1Method = env->GetMethodID(c,"getTrack1","()[B");
    setTrack1Method = env->GetMethodID(c,"setTrack1","([B)V");
    getTrack2SizeMethod = env->GetMethodID(c,"getTrack2Size","()I");
    setTrack2SizeMethod = env->GetMethodID(c,"setTrack2Size","(I)V");
    getTrack2Method = env->GetMethodID(c,"getTrack2","()[B");
    setTrack2Method = env->GetMethodID(c,"setTrack2","([B)V");
//    getSessionKeyMethod = env->GetMethodID(c,"getSessionKey","()Ljava/lang/String;");
//    setSessionKeyMethod = env->GetMethodID(c,"setSessionKey","(Ljava/lang/String;)V");
    getSessionKeyMethod = env->GetMethodID(c,"getSessionKey","()[B");
    setSessionKeyMethod = env->GetMethodID(c,"setSessionKey","([B)V");
    paramConstractor = env->GetMethodID(c,"<init>","(II[BI[B[B)V");

    __android_log_print(ANDROID_LOG_DEBUG,"Tag","desDecrypt : jobject Make Start");

//    jbyteArray retDecData = env->NewByteArray(sizeof(szDecData));
//    jbyte* pDecData = env->GetByteArrayElements(retDecData,0);
//    jbyteArray retEncData = env->NewByteArray(sizeof(szEncData));
//    jbyte* pEncData = env->GetByteArrayElements(retEncData,0);
//    int cnt = 0;
//    for(cnt=0; cnt< sizeof(szDecData); cnt++){
//        pDecData[cnt] = (jbyte)szDecData[cnt];
//    }
//    int cntEnc = 0;
//    for(cntEnc=0; cntEnc< sizeof(szEncData); cntEnc++){
//        pEncData[cntEnc] = (jbyte)szEncData[cntEnc];
//    }
    jbyteArray retDecData = createByteArray(env,szDecData,decSize);
    jbyteArray retEncData = createByteArray(env,szEncData,encSize);
    jbyteArray retStr = createByteArray(env,natSessionKey,sesSize);
//    jstring  retStr = env->NewStringUTF(natSessionKey);

    retObj = env->NewObject(c,
                             paramConstractor,
                             1,
                             sizeof(szDecData),
                             retDecData,
                             sizeof(szEncData),
                             retEncData,
                            retStr
    );

    __android_log_print(ANDROID_LOG_DEBUG,"Tag","desDecrypt : jobject Make End");
//    retObj = env->NewObject(c,
//                         paramConstractor,
//                         1,
//                         sizeof(szDecData),
//                         szDecData,
//                         sizeof(szEncData),
//                         szEncData,
//                         sessionKey
//    );
    (*env).ReleaseByteArrayElements(encdata,arrEncData,0);
    (*env).ReleaseByteArrayElements(decdata,arrDecDeta,0);
    (*env).ReleaseByteArrayElements(sessionKey,arrSesDeta,0);
//    (*env).ReleaseStringUTFChars(sessionKey,natSessionKey);
    return retObj;

}



}
