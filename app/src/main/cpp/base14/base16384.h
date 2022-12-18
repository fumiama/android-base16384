#ifndef _BASE16384_H_
#define _BASE16384_H_

/* base16384.h
 * This file is part of the base16384 distribution (https://github.com/fumiama/base16384).
 * Copyright (c) 2022 Fumiama Minamoto.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

#ifndef __cosmopolitan
#include <stdint.h>
#include <stdio.h>
#endif

// base16384_err_t is the return value of base16384_en/decode_file
enum base16384_err_t {
	base16384_err_ok,
	base16384_err_get_file_size,
	base16384_err_fopen_output_file,
	base16384_err_fopen_input_file,
	base16384_err_write_file,
	base16384_err_open_input_file,
	base16384_err_map_input_file,
	base16384_err_read_file,
};
// base16384_err_t is the return value of base16384_en/decode_file
typedef enum base16384_err_t base16384_err_t;

#define BASE16384_ENCBUFSZ (BUFSIZ*1024/7*7)
#define BASE16384_DECBUFSZ (BUFSIZ*1024/8*8+2)

// base16384_encode_len calc min buf size to fill encode result
static inline int base16384_encode_len(int dlen) {
	int outlen = dlen / 7 * 8;
	int offset = dlen % 7;
	switch(offset) {	// 算上偏移标志字符占用的2字节
		case 0: break;
		case 1: outlen += 4; break;
		case 2:
		case 3: outlen += 6; break;
		case 4:
		case 5: outlen += 8; break;
		case 6: outlen += 10; break;
		default: break;
	}
	return outlen + 8;	// 冗余的8B用于可能的结尾的覆盖
}

// base16384_decode_len calc min buf size to fill decode result
static inline int base16384_decode_len(int dlen) {
	return dlen / 8 * 7 + 1; // 多出1字节用于循环覆盖
}

#endif
