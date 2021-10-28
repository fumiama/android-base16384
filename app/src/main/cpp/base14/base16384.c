#include <stdio.h>
#include <stdlib.h>
#include "base16384.h"

int encode_file(const char* input, const char* output) {
	FILE* fp = NULL;
	fp = fopen(input, "rb");
	if(fp) {
		FILE* fpo = NULL;
		fpo = fopen(output, "wb");
		if(fpo) {
			char* bufi = (char*)malloc(B14BUFSIZ/7*7);
			if(bufi) {
				int cnt;
				fputc(0xFE, fpo);
    			fputc(0xFF, fpo);
				fflush(fpo);
				while((cnt = fread(bufi, sizeof(char), B14BUFSIZ/7*7, fp))) {
					LENDAT* ld = encode(bufi, cnt);
					if(fwrite(ld->data, ld->len, 1, fpo) <= 0) {
						puts("Write file error!");
                        return 1;
					}
					free(ld->data);
					free(ld);
				}
				free(bufi);
			} else {
			    puts("Allocate input buffer error!");
                return 2;
			}
			fclose(fpo);
		} else {
		    puts("Open output file error!");
            return 3;
		}
		fclose(fp);
        return 0;
	} else {
	    puts("Open input file error!");
        return 4;
	}
}

void rm_head(FILE* fp) {
	int ch = fgetc(fp);
	if(ch == 0xFE) fgetc(fp);
	else rewind(fp);
}

static int is_next_end(FILE* fp) {
	int ch = fgetc(fp);
	if(ch == '=') return fgetc(fp);
	else {
		ungetc(ch, fp);
		return 0;
	}
}

int decode_file(const char* input, const char* output) {
	FILE* fp = NULL;
	fp = fopen(input, "rb");
	if(fp) {
		FILE* fpo = NULL;
		fpo = fopen(output, "wb");
		if(fpo) {
			char* bufi = (char*)malloc(B14BUFSIZ/8*8 + 2);		//+2避免漏检结束偏移标志
			if(bufi) {
				int cnt, end;
				rm_head(fp);
				while((cnt = fread(bufi, sizeof(char), B14BUFSIZ/8*8, fp))) {
					if((end = is_next_end(fp))) {
						bufi[cnt++] = '=';
						bufi[cnt++] = (char)end;
					}
					LENDAT* ld = decode(bufi, cnt);
					if(fwrite(ld->data, ld->len, 1, fpo) <= 0) {
						puts("Write file error!");
                        return 1;
					}
					free(ld->data);
					free(ld);
				}
				free(bufi);
			} else {
                puts("Allocate input buffer error!");
                return 2;
            }
            fclose(fpo);
        } else {
            puts("Open output file error!");
            return 3;
        }
        fclose(fp);
        return 0;
    } else {
        puts("Open input file error!");
        return 4;
    }
}
