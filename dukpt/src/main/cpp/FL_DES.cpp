/*
 *  DES.c
 *
 *  Created by Nobumichi Okada.
 *  Copyright 2010 FLIGHT SYSTEM CONSULTING Inc. All rights reserved.
 *
 */

//#include "stdafx.h"
#include "FL_DES.h"
#include <stdio.h>


////////////////////////////////////////////////////////////////////////////////
unsigned char ip[64]={
	58, 50, 42, 34, 26, 18, 10, 2,
	60, 52, 44, 36, 28, 20, 12, 4,
	62, 54, 46, 38, 30, 22, 14, 6,
	64, 56, 48, 40, 32, 24, 16, 8,
	57, 49, 41, 33, 25, 17,  9, 1,
	59, 51, 43, 35, 27, 19, 11, 3,
	61, 53, 45, 37, 29, 21, 13, 5,
	63, 55, 47, 39, 31, 23, 15, 7
};
////////////////////////////////////////////////////////////////////////////////
unsigned char ip_inv[64]={
	40, 8, 48, 16, 56, 24, 64, 32,
	39, 7, 47, 15, 55, 23, 63, 31,
	38, 6, 46, 14, 54, 22, 62, 30,
	37, 5, 45, 13, 53, 21, 61, 29,
	36, 4, 44, 12, 52, 20, 60, 28,
	35, 3, 43, 11, 51, 19, 59, 27,
	34, 2, 42, 10, 50, 18, 58, 26,
	33, 1, 41,  9, 49, 17, 57, 25,
};
////////////////////////////////////////////////////////////////////////////////
unsigned char e[48]={
	32,  1,  2,  3,  4,  5,
	4,  5,  6,  7,  8,  9,
	8,  9, 10, 11, 12, 13,
	12, 13, 14, 15, 16, 17,
	16, 17, 18, 19, 20, 21,
	20, 21, 22, 23, 24, 25,
	24, 25, 26, 27, 28, 29,
	28, 29, 30, 31, 32,  1
};
////////////////////////////////////////////////////////////////////////////////
unsigned char s1[4][16]={
	{14,  4, 13,  1,  2, 15, 11,  8,  3, 10,  6, 12,  5,  9,  0,  7},
	{ 0, 15,  7,  4, 14,  2, 13,  1, 10,  6, 12, 11,  9,  5,  3,  8},
	{ 4,  1, 14,  8, 13,  6,  2, 11, 15, 12,  9,  7,  3, 10,  5,  0},
	{15, 12,  8,  2,  4,  9,  1,  7,  5, 11,  3, 14, 10,  0,  6, 13}
};
////////////////////////////////////////////////////////////////////////////////
unsigned char s2[4][16]={
	{15,  1,  8, 14,  6, 11,  3,  4,  9,  7,  2, 13, 12,  0,  5, 10},
	{ 3, 13,  4,  7, 15,  2,  8, 14, 12,  0,  1, 10,  6,  9, 11,  5},
	{ 0, 14,  7, 11, 10,  4, 13,  1,  5,  8, 12,  6,  9,  3,  2, 15},
	{13,  8, 10,  1,  3, 15,  4,  2, 11,  6,  7, 12,  0,  5, 14,  9}
};
////////////////////////////////////////////////////////////////////////////////
unsigned char s3[4][16]={
	{10,  0,  9, 14,  6,  3, 15,  5,  1, 13, 12,  7, 11,  4,  2,  8},
	{13,  7,  0,  9,  3,  4,  6, 10,  2,  8,  5, 14, 12, 11, 15,  1},
	{13,  6,  4,  9,  8, 15,  3,  0, 11,  1,  2, 12,  5, 10, 14,  7},
	{ 1, 10, 13,  0,  6,  9,  8,  7,  4, 15, 14,  3, 11,  5,  2, 12}
};
////////////////////////////////////////////////////////////////////////////////
unsigned char s4[4][16]={
	{ 7, 13, 14,  3,  0,  6,  9, 10,  1,  2,  8,  5, 11, 12,  4, 15},
	{13,  8, 11,  5,  6, 15,  0,  3,  4,  7,  2, 12,  1, 10, 14,  9},
	{10,  6,  9,  0, 12, 11,  7, 13, 15,  1,  3, 14,  5,  2,  8,  4},
	{ 3, 15,  0,  6, 10,  1, 13,  8,  9,  4,  5, 11, 12,  7,  2, 14}
};
////////////////////////////////////////////////////////////////////////////////
unsigned char s5[4][16]={
	{ 2, 12,  4,  1,  7, 10, 11,  6,  8,  5,  3, 15, 13,  0, 14,  9},
	{14, 11,  2, 12,  4,  7, 13,  1,  5,  0, 15, 10,  3,  9,  8,  6},
	{ 4,  2,  1, 11, 10, 13,  7,  8, 15,  9, 12,  5,  6,  3,  0, 14},
	{11,  8, 12,  7,  1, 14,  2, 13,  6, 15,  0,  9, 10,  4,  5,  3}
};
////////////////////////////////////////////////////////////////////////////////
unsigned char s6[4][16]={
	{12,  1, 10, 15,  9,  2,  6,  8,  0, 13,  3,  4, 14,  7,  5, 11},
	{10, 15,  4,  2,  7, 12,  9,  5,  6,  1, 13, 14,  0, 11,  3,  8},
	{ 9, 14, 15,  5,  2,  8, 12,  3,  7,  0,  4, 10,  1, 13, 11,  6},
	{ 4,  3,  2, 12,  9,  5, 15, 10, 11, 14,  1,  7,  6,  0,  8, 13}
};
////////////////////////////////////////////////////////////////////////////////
unsigned char s7[4][16]={
	{ 4, 11,  2, 14, 15,  0,  8, 13,  3, 12,  9,  7,  5, 10,  6,  1},
	{13,  0, 11,  7,  4,  9,  1, 10, 14,  3,  5, 12,  2, 15,  8,  6},
	{ 1,  4, 11, 13, 12,  3,  7, 14, 10, 15,  6,  8,  0,  5,  9,  2},
	{ 6, 11, 13,  8,  1,  4, 10,  7,  9,  5,  0, 15, 14,  2,  3, 12}
};
////////////////////////////////////////////////////////////////////////////////
unsigned char s8[4][16]={
	{13,  2,  8,  4,  6, 15, 11,  1, 10,  9,  3, 14,  5,  0, 12,  7},
	{ 1, 15, 13,  8, 10,  3,  7,  4, 12,  5,  6, 11,  0, 14,  9,  2},
	{ 7, 11,  4,  1,  9, 12, 14,  2,  0,  6, 10, 13, 15,  3,  5,  8},
	{ 2,  1, 14,  7,  4, 10,  8, 13, 15, 12,  9,  0,  3,  5,  6, 11}
};
////////////////////////////////////////////////////////////////////////////////
unsigned char p[32]={
	16,  7, 20, 21,
	29, 12, 28, 17,
	1, 15, 23, 26,
	5, 18, 31, 10,
	2,  8, 24, 14,
	32, 27,  3,  9,
	19, 13, 30,  6,
	22, 11,  4, 25
};
////////////////////////////////////////////////////////////////////////////////
unsigned char pc1[56]={
	57, 49, 41, 33, 25, 17,  9,
	1, 58, 50, 42, 34, 26, 18,
	10,  2, 59, 51, 43, 35, 27,
	19, 11,  3, 60, 52, 44, 36,
	
	63, 55, 47, 39, 31, 23, 15,
	7, 62, 54, 46, 38, 30, 22,
	14,  6, 61, 53, 45, 37, 29,
	21, 13,  5, 28, 20, 12,  4
};
////////////////////////////////////////////////////////////////////////////////
unsigned char left_shifts[16]={
	1,
	1,
	2,
	2,
	2,
	2,
	2,
	2,
	1,
	2,
	2,
	2,
	2,
	2,
	2,
	1
};
////////////////////////////////////////////////////////////////////////////////
unsigned char pc2[48]={
	14, 17, 11, 24,  1,  5,
	3, 28, 15,  6, 21, 10,
	23, 19, 12,  4, 26,  8,
	16,  7, 27, 20, 13,  2,
	41, 52, 31, 37, 47, 55,
	30, 40, 51, 45, 33, 48,
	44, 49, 39, 56, 34, 53,
	46, 42, 50, 36, 29, 32
};
////////////////////////////////////////////////////////////////////////////////
unsigned char row; // global variable
unsigned char col; // global variable
unsigned char s_element; // global variable
unsigned char temp; // global variable
unsigned char perm_input[64]; // global variable
unsigned char pre_output[64]; // global variable
unsigned char k[16][48]; // global variable
unsigned char l[32]; // global variable
unsigned char r[32]; // global variable
unsigned char b[48]; // global variable
unsigned char s[32]; // global variable
unsigned char f[32]; // global variable
unsigned char c[28]; // global variable
unsigned char d[28]; // global variable
unsigned char cd[56]; // global variable
unsigned char key[64];
unsigned char pt[64];
unsigned char et[64];




void left_shift(int n, unsigned char *arr) {
	int i, j;
	for (i=0; i<n; i++) {
		temp=*(arr+0);
		for (j=0; j<28; j++) {
			*(arr+j)=*(arr+j+1);
		}
		*(arr+27)=temp;
	}
}

void print(unsigned char arr[]) {
	int i;
	for (i=0; i<64; i++) {
		printf("%4d", arr[i]);
		if ((i+1)%8==0) {
			printf("��n");
		}
	}
	printf("��n");
}

void des_init(unsigned char key_ring[8]) {
	int i, j;
	for (i=0; i<8; i++) {
		for (j=0; j<8; j++) {
			key[i*8+j]=(key_ring[i]&(0x01<<(7-j)))>>(7-j);
		}
	}
	for (i=0; i<28; i++) {
		c[i]=key[pc1[i]-1];
		d[i]=key[pc1[i+28]-1];
	}
	for (i=0; i<16; i++) {
		left_shift(left_shifts[i], c);
		left_shift(left_shifts[i], d);
		for (j=0; j<28; j++) {
			cd[j]=c[j];
			cd[j+28]=d[j];
		}
		for (j=0; j<48; j++) {
			k[i][j]=cd[pc2[j]-1];
		}
	}
}

void des_encrypt() {
	int i, j;
	for (i=0; i<64; i++) {
		et[i]=pt[i];
	}
	for (i=0; i<64; i++) {
		perm_input[i]=et[ip[i]-1];
	}
	for (i=0; i<32; i++) {
		l[i]=perm_input[i];
		r[i]=perm_input[i+32];
	}
	for (i=0; i<16; i++) {
		for (j=0; j<48; j++) {
			b[j]=r[e[j]-1]^k[i][j];
		}
		for (j=0; j<8; j++) {
			row=(b[j*6+0]<<1)|(b[j*6+5]);
			col=(b[j*6+1]<<3)|(b[j*6+2]<<2)|(b[j*6+3]<<1)|b[j*6+4];
			switch (j) {
				case 0:
					s_element=s1[row][col];
					break;
				case 1:
					s_element=s2[row][col];
					break;
				case 2:
					s_element=s3[row][col];
					break;
				case 3:
					s_element=s4[row][col];
					break;
				case 4:
					s_element=s5[row][col];
					break;
				case 5:
					s_element=s6[row][col];
					break;
				case 6:
					s_element=s7[row][col];
					break;
				case 7:
					s_element=s8[row][col];
					break;
				default:
					break;
			}
			s[j*4+0]=(s_element&0x08)>>3;
			s[j*4+1]=(s_element&0x04)>>2;
			s[j*4+2]=(s_element&0x02)>>1;
			s[j*4+3]=s_element&0x01;
		}
		for (j=0; j<32; j++) {
			f[j]=s[p[j]-1];
		}
		for (j=0; j<32; j++) {
			temp=r[j];
			r[j]=l[j]^f[j];
			l[j]=temp;
		}
	}
	for (i=0; i<32; i++) {
		pre_output[i]=r[i];
		pre_output[i+32]=l[i];
	}
	for (i=0; i<64; i++) {
		et[i]=pre_output[ip_inv[i]-1];
	}
}

void des_decrypt() {
	int i, j;
	for (i=0; i<64; i++) {
		pt[i]=et[i];
	}
	for (i=0; i<64; i++) {
		perm_input[i]=pt[ip[i]-1];
	}
	for (i=0; i<32; i++) {
		r[i]=perm_input[i];
		l[i]=perm_input[i+32];
	}
	for (i=16; i>0; i--) {
		for (j=0; j<48; j++) {
			b[j]=l[e[j]-1]^k[i-1][j];
		}
		for (j=0; j<8; j++) {
			row=(b[j*6+0]<<1)|(b[j*6+5]);
			col=(b[j*6+1]<<3)|(b[j*6+2]<<2)|(b[j*6+3]<<1)|b[j*6+4];
			switch (j) {
				case 0:
					s_element=s1[row][col];
					break;
				case 1:
					s_element=s2[row][col];
					break;
				case 2:
					s_element=s3[row][col];
					break;
				case 3:
					s_element=s4[row][col];
					break;
				case 4:
					s_element=s5[row][col];
					break;
				case 5:
					s_element=s6[row][col];
					break;
				case 6:
					s_element=s7[row][col];
					break;
				case 7:
					s_element=s8[row][col];
					break;
				default:
					break;
			}
			s[j*4+0]=(s_element&0x08)>>3;
			s[j*4+1]=(s_element&0x04)>>2;
			s[j*4+2]=(s_element&0x02)>>1;
			s[j*4+3]=s_element&0x01;
		}
		for (j=0; j<32; j++) {
			f[j]=s[p[j]-1];
		}
		for (j=0; j<32; j++) {
			temp=l[j];
			l[j]=r[j]^f[j];
			r[j]=temp;
		}
	}
	for (i=0; i<32; i++) {
		pre_output[i]=l[i];
		pre_output[i+32]=r[i];
	}
	for (i=0; i<64; i++) {
		pt[i]=pre_output[ip_inv[i]-1];
	}
}


unsigned char *des_block(const unsigned char *source, unsigned char *target, unsigned char *key, DesEorD direction)
{
	int i, j;
	
	des_init(key);
	if (direction == encipher) {
		for (i=0; i<8; i++) {
			for (j=0; j<8; j++) {
				pt[i * 8 + j]=((*(source + i)) & (0x01 << (7 - j))) >> (7 - j);
			}
		}
		des_encrypt();
		for (i=0; i<8; i++) {
			*(target + i) = 0x00;
			for (j=0; j<8; j++) {
				if (et[i * 8 + j] == 0x01) {
					*(target + i) |= (1 << (7 - j));
				} else {
					*(target + i) &= ~(1 << (7 - j));
				}
			}
		}
	} else {
		for (i=0; i<8; i++) {
			for (j=0; j<8; j++) {
				et[i * 8 + j]=((*(source + i)) & (0x01 << (7 - j))) >> (7 - j);
			}
		}
		des_decrypt();
		for (i=0; i<8; i++) {
			*(target + i) = 0x00;
			for (j=0; j<8; j++) {
				if (pt[i * 8 + j] == 0x01) {
					*(target + i) |= (1 << (7 - j));
				} else {
					*(target + i) &= ~(1 << (7 - j));
				}
			}
		}
	}
	
	return target;
}