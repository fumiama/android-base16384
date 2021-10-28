//base1464le.c
//fumiama 20210407
#include <stdio.h>
#include <stdlib.h>
#include "base1464le.h"

//#define DEBUG

LENDAT* encode(const uint8_t* data, const int64_t len) {
	LENDAT* encd = (LENDAT*)malloc(sizeof(LENDAT));
	int64_t outlen = len / 7 * 8;
	uint8_t offset = len % 7;
	switch(offset) {	//算上偏移标志字符占用的2字节
		case 0: break;
		case 1: outlen += 4; break;
		case 2:
		case 3: outlen += 6; break;
		case 4:
		case 5: outlen += 8; break;
		case 6: outlen += 10; break;
		default: break;
	}
#ifdef DEBUG
	printf("outlen: %llu, offset: %u, malloc: %llu\n", outlen, offset, outlen + 8);
#endif
	encd->data = (uint8_t*)malloc(outlen + 8);	//冗余的8B用于可能的结尾的覆盖
	encd->len = outlen;
	uint64_t* vals = (uint64_t*)(encd->data);
	uint64_t n = 0;
	int64_t i = 0;
	for(; i <= len - 7; i += 7) {
		register uint64_t sum = 0x000000000000003f & ((uint64_t)data[i] >> 2);
		sum |= (((uint64_t)data[i + 1] << 6) | (data[i] << 14)) & 0x000000000000ff00;
		sum |= (((uint64_t)data[i + 1] << 20) | ((uint64_t)data[i + 2] << 12)) & 0x00000000003f0000;
		sum |= (((uint64_t)data[i + 2] << 28) | ((uint64_t)data[i + 3] << 20)) & 0x00000000ff000000;
		sum |= (((uint64_t)data[i + 3] << 34) | ((uint64_t)data[i + 4] << 26)) & 0x0000003f00000000;
		sum |= (((uint64_t)data[i + 4] << 42) | ((uint64_t)data[i + 5] << 34)) & 0x0000ff0000000000;
		sum |= ((uint64_t)data[i + 5] << 48) & 0x003f000000000000;
		sum |= ((uint64_t)data[i + 6] << 56) & 0xff00000000000000;
		sum += 0x004e004e004e004e;
		vals[n++] = sum;
#ifdef DEBUG
		printf("i: %llu, add sum: %016llx\n", i, sum);
#endif
	}
	uint8_t o = offset;
	if(o--) {
		register uint64_t sum = 0x000000000000003f & (data[i] >> 2);
		sum |= ((uint64_t)data[i] << 14) & 0x000000000000c000;
		if(o--) {
			sum |= ((uint64_t)data[i + 1] << 6) & 0x0000000000003f00;
			sum |= ((uint64_t)data[i + 1] << 20) & 0x0000000000300000;
			if(o--) {
				sum |= ((uint64_t)data[i + 2] << 12) & 0x00000000000f0000;
				sum |= ((uint64_t)data[i + 2] << 28) & 0x00000000f0000000;
				if(o--) {
					sum |= ((uint64_t)data[i + 3] << 20) & 0x000000000f000000;
					sum |= ((uint64_t)data[i + 3] << 34) & 0x0000003c00000000;
					if(o--) {
						sum |= ((uint64_t)data[i + 4] << 26) & 0x0000000300000000;
						sum |= ((uint64_t)data[i + 4] << 42) & 0x0000fc0000000000;
						if(o--) {
							sum |= ((uint64_t)data[i + 5] << 34) & 0x0000030000000000;
							sum |= ((uint64_t)data[i + 5] << 48) & 0x003f000000000000;
						}
					}
				}
			}
		}
		sum += 0x004e004e004e004e;
		vals[n] = sum;
#ifdef DEBUG
		printf("i: %llu, add sum: %016llx\n", i, sum);
#endif
		encd->data[outlen - 2] = '=';
		encd->data[outlen - 1] = offset;
	}
	return encd;
}

LENDAT* decode(const uint8_t* data, const int64_t len) {
	LENDAT* decd = (LENDAT*)malloc(sizeof(LENDAT));
	int64_t outlen = len;
	uint8_t offset = 0;
	if(data[len-2] == '=') {
		offset = data[len-1];
		switch(offset) {	//算上偏移标志字符占用的2字节
			case 0: break;
			case 1: outlen -= 4; break;
			case 2:
			case 3: outlen -= 6; break;
			case 4:
			case 5: outlen -= 8; break;
			case 6: outlen -= 10; break;
			default: break;
		}
	}
	outlen = outlen / 8 * 7 + offset;
	decd->data = (uint8_t*)malloc(outlen);
	decd->len = outlen;
	uint64_t* vals = (uint64_t*)data;
	uint64_t n = 0;
	int64_t i = 0;
	for(; i <= outlen - 7; n++) {
		register uint64_t sum = vals[n] - 0x004e004e004e004e;
		decd->data[i++] = ((sum & 0x000000000000003f) << 2) | ((sum & 0x000000000000c000) >> 14);
		decd->data[i++] = ((sum & 0x0000000000003f00) >> 6) | ((sum & 0x0000000000300000) >> 20);
		decd->data[i++] = ((sum & 0x00000000000f0000) >> 12) | ((sum & 0x00000000f0000000) >> 28);
		decd->data[i++] = ((sum & 0x000000000f000000) >> 20) | ((sum & 0x0000003c00000000) >> 34);
		decd->data[i++] = ((sum & 0x0000000300000000) >> 26) | ((sum & 0x0000fc0000000000) >> 42);
		decd->data[i++] = ((sum & 0x0000030000000000) >> 34) | ((sum & 0x003f000000000000) >> 48);
		decd->data[i++] = ((sum & 0xff00000000000000) >> 56);
	}
	if(offset--) {
		register uint64_t sum = vals[n] - 0x000000000000004e;
		decd->data[i++] = ((sum & 0x000000000000003f) << 2) | ((sum & 0x000000000000c000) >> 14);
		if(offset--) {
			sum -= 0x00000000004e0000;
			decd->data[i++] = ((sum & 0x0000000000003f00) >> 6) | ((sum & 0x0000000000300000) >> 20);
			if(offset--) {
				decd->data[i++] = ((sum & 0x00000000000f0000) >> 12) | ((sum & 0x00000000f0000000) >> 28);
				if(offset--) {
					sum -= 0x0000004e00000000;
					decd->data[i++] = ((sum & 0x000000000f000000) >> 20) | ((sum & 0x0000003c00000000) >> 34);
					if(offset--) {
						decd->data[i++] = ((sum & 0x0000000300000000) >> 26) | ((sum & 0x0000fc0000000000) >> 42);
						if(offset--) {
							sum -= 0x004e000000000000;
							decd->data[i] = ((sum & 0x0000030000000000) >> 34) | ((sum & 0x003f000000000000) >> 48);
						}
					}
				}
			}
		}
	}
	return decd;
}
