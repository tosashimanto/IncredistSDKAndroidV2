/*
 *  DES.h
 *
 *  Created by Nobumichi Okada.
 *  Copyright 2010 FLIGHT SYSTEM CONSULTING Inc. All rights reserved.
 *
 */

#ifdef  __cplusplus
extern "C" {
#endif

typedef enum DesEorD_ {encipher, decipher} DesEorD;

unsigned char *des_block(const unsigned char *source, unsigned char *target, unsigned char *key, DesEorD direction);

////////////////////////////////////////////////////////////////////////////////
void left_shift(int n, unsigned char *arr);
void print(unsigned char arr[]);
void des_init(unsigned char key_ring[8]);
void des_encrypt();
void des_decrypt();
void tdes_encrypt(unsigned long n, unsigned char *in, unsigned char *out);
void tdes_decrypt(unsigned long n, unsigned char *in, unsigned char *out);
////////////////////////////////////////////////////////////////////////////////

#ifdef __cplusplus
}
#endif
