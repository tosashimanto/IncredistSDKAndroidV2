/*
 *  DUKPT.h
 *
 *  Created by Nobumichi Okada.
 *  Copyright 2010 FLIGHT SYSTEM CONSULTING Inc. All rights reserved.
 *
 */

#ifdef  __cplusplus
extern "C" {
#endif

#include "FL_DES.h"


void obtainIPEK(unsigned char bdk[], unsigned char KSN[]);
void obtainNewIPEK(unsigned char bdk[], unsigned char KSN[]);
void getCurrentKey(unsigned char KSN[], unsigned char currentKey[]);
void getNewCurrentKey(unsigned char KSN[], unsigned char currentKey[]);
void countUpKSN(unsigned char KSN[]);

int MSB(unsigned long counter);
int LSB(unsigned long counter);
int bitCounts(unsigned long counter);

#ifdef __cplusplus
}
#endif
