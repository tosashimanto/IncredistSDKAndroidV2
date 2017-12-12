/*
 *  DUKPT.c
 *
 *  Created by Nobumichi Okada.
 *  Copyright 2010 FLIGHT SYSTEM CONSULTING Inc. All rights reserved.
 *
 *  2011-05-07 - v1.1 : fixed getCurrentKey() issue
 *
 */

//#include "stdafx.h"
#include "DUKPT.h"
#include <stdio.h>
#include <string.h>
#include <openssl/des.h>
//#include "openssl/des.h"

const unsigned char xorCode[16] = {0xc0, 0xc0, 0xc0, 0xc0, 0x00, 0x00, 0x00, 0x00,
                                   0xc0, 0xc0, 0xc0, 0xc0, 0x00, 0x00, 0x00, 0x00};

unsigned char BDK_R[8];
unsigned char BDK_L[8];

unsigned char IPEK_L[8];
unsigned char IPEK_R[8];


int MSB(unsigned long counter)
{
    int pos = 0;

    counter >>= 1;
    while (counter) {
        counter >>= 1;
        pos++;
    }

    return pos;
}

int LSB(unsigned long counter)
{
    int pos = 0;

    while (counter) {
        if (counter & 0x01) {
            return pos;
        }
        pos++;
        counter >>= 1;
    };

    return -1;
}

int bitCounts(unsigned long counter)
{
    int bits = 0;

    while (counter) {
        bits += (counter & 0x01) ? 1 : 0;
        counter >>= 1;
    }

    return bits;
}



void obtainIPEK(unsigned char BDK[], unsigned char KSN[])
{
    unsigned char tempKSN[10];
    unsigned char temp_R[8], temp_L[8];
    unsigned char orgdata[8];
    unsigned char encdata1[8], encdata2[8];
    unsigned int i;

    memcpy(BDK_L, &BDK[0], 8);
    memcpy(BDK_R, &BDK[8], 8);

    for (i=0; i<10; i++) {
        tempKSN[i] = KSN[i];
    }
    // Set the 21 least-significant bits of this 10-byte register to zero
    tempKSN[9] = 0x00;
    tempKSN[8] = 0x00;
    tempKSN[7] &= 0xe0;
    for (i=0; i<8; i++) {
        orgdata[i] =  tempKSN[i];
    }

    des_block(orgdata, encdata1, BDK_L, encipher);
    des_block(encdata1, encdata2, BDK_R, decipher);
    des_block(encdata2, encdata1, BDK_L, encipher);

    for (i=0; i<8; i++) {
        IPEK_L[i] = encdata1[i];
    }

    for (i=0; i<8; i++) {
        temp_R[i] = BDK_R[i] ^ xorCode[i+8];
        temp_L[i] = BDK_L[i] ^ xorCode[i];
    }

    des_block(orgdata, encdata1, temp_L, encipher);
    des_block(encdata1, encdata2, temp_R, decipher);
    des_block(encdata2, encdata1, temp_L, encipher);

    for (i=0; i<8; i++) {
        IPEK_R[i] = encdata1[i];
    }

    // IPEK計算後はBDKの格納エリアをクリアしておく
    memset(BDK_L, 0, 8);
    memset(BDK_R, 0, 8);
}


void getCurrentKey(unsigned char KSN[], unsigned char currentKey[])
{
    unsigned int i;
    unsigned long TC = 0;
    unsigned long tempTC = 0;
    unsigned char tempKSN[10];
    unsigned char orgdata[8];
    unsigned char encdata1[8], encdata2[8];
    unsigned char nextKey[16];

    for (i=0; i<10; i++) {
        tempKSN[i] = KSN[i];
    }

    TC = KSN[9] & 0xff;
    TC |= KSN[8] << 8;
    TC |= (KSN[7] & 0x1f) << 16;

    for (i=0; i<8; i++) {
        currentKey[i] = IPEK_L[i];
        currentKey[i+8] = IPEK_R[i];
    }

    do {
        tempTC |= (1 << MSB(TC));

        tempKSN[9] = tempTC & 0x0000FF;
        tempKSN[8] = (tempTC >> 8) & 0x0000FF;
        tempKSN[7] &= 0xe0;
        tempKSN[7] |= (tempTC >> 16) & 0x00001F;

        for (i=0; i<8; i++) {
            orgdata[i] = tempKSN[i+2];
        }

        for (i=0; i<8; i++) {
            encdata1[i] = orgdata[i] ^ currentKey[i+8];
        }

        des_block(encdata1, encdata2, &(currentKey[0]), encipher);

        for (i=0; i<8; i++) {
            nextKey[i+8] = encdata2[i] ^ currentKey[i+8];
        }

        for (i=0; i<16; i++) {
            currentKey[i]   ^= xorCode[i];
        }

        for (i=0; i<8; i++) {
            orgdata[i] ^= currentKey[i+8];
        }

        des_block(orgdata, encdata1, &(currentKey[0]), encipher);

        for (i=0; i<8; i++) {
            nextKey[i] = encdata1[i] ^ currentKey[i+8];
        }

        for (i=0; i<16; i++) {
            currentKey[i] = nextKey[i];
        }

        TC &= ~tempTC;
    } while (TC);

    // Variant for PIN Encryption
    for (i=0; i<16; i++) {
        if (i == 7 || i == 15) {
            currentKey[i] ^= 0xFF;
        } else {
            currentKey[i] ^= 0x00;
        }
    }
}

void getNewCurrentKey(unsigned char KSN[], unsigned char currentKey[])
{
    unsigned int i;
    unsigned long TC = 0;
    unsigned long tempTC = 0;
    unsigned char tempKSN[10];
    unsigned char orgdata[8];
    unsigned char encdata1[8], encdata2[8];
    unsigned char nextKey[16];
    unsigned char variantKey[16];

    DES_key_schedule ks1, ks2;
    DES_cblock k1, k2, iv;

    for (i=0; i<10; i++) {
        tempKSN[i] = KSN[i];
    }

    TC = KSN[9] & 0xff;
    TC |= KSN[8] << 8;
    TC |= (KSN[7] & 0x1f) << 16;

    for (i=0; i<8; i++) {
        currentKey[i] = IPEK_L[i];
        currentKey[i+8] = IPEK_R[i];
    }

    do {
        tempTC |= (1 << MSB(TC));

        tempKSN[9] = tempTC & 0x0000FF;
        tempKSN[8] = (tempTC >> 8) & 0x0000FF;
        tempKSN[7] &= 0xe0;
        tempKSN[7] |= (tempTC >> 16) & 0x00001F;

        for (i=0; i<8; i++) {
            orgdata[i] = tempKSN[i+2];
        }

        for (i=0; i<8; i++) {
            encdata1[i] = orgdata[i] ^ currentKey[i+8];
        }

        des_block(encdata1, encdata2, &(currentKey[0]), encipher);

        for (i=0; i<8; i++) {
            nextKey[i+8] = encdata2[i] ^ currentKey[i+8];
        }

        for (i=0; i<16; i++) {
            currentKey[i]   ^= xorCode[i];
        }

        for (i=0; i<8; i++) {
            orgdata[i] ^= currentKey[i+8];
        }

        des_block(orgdata, encdata1, &(currentKey[0]), encipher);

        for (i=0; i<8; i++) {
            nextKey[i] = encdata1[i] ^ currentKey[i+8];
        }

        for (i=0; i<16; i++) {
            currentKey[i] = nextKey[i];
        }

        TC &= ~tempTC;
    } while (TC);

    // Key calculation for Data Encryption keys
    for (i=0; i<16; i++) {
        if (i == 5 || i == 13) {
            currentKey[i] ^= 0xFF;
        }
        variantKey[i] = currentKey[i];
    }

    memcpy(k1, &variantKey[0], sizeof(k1));
    memcpy(k2, &variantKey[8], sizeof(k2));
    DES_set_key(&k1, &ks1);
    DES_set_key(&k2, &ks2);

    memset(iv, 0, sizeof(iv));
    DES_ede2_cbc_encrypt(&currentKey[0], &nextKey[0], 8, &ks1, &ks2, &iv, DES_ENCRYPT);
    memset(iv, 0, sizeof(iv));
    DES_ede2_cbc_encrypt(&currentKey[8], &nextKey[8], 8, &ks1, &ks2, &iv, DES_ENCRYPT);

    for (i=0; i<16; i++) {
        currentKey[i] = nextKey[i];
    }
}

void countUpKSN(unsigned char KSN[])
{
    unsigned long TC;

    TC = (unsigned long)KSN[9];
    TC |= (unsigned long)KSN[8] << 8;
    TC |= ((unsigned long)KSN[7] & 0x1f) << 16;

    if (bitCounts(TC) < 10) {
        TC++;
    } else {
        TC += 1 << LSB(TC);
        TC &= 0x1fffff;
    }

    KSN[9] = TC & 0xff;
    KSN[8] = (TC >> 8) & 0xff;
    KSN[7] = (int)KSN[7] & 0xe0;
    KSN[7] = (int)KSN[7] | (int)((TC >> 16) & 0x1f);
}
