//
//  ibecommon.h
//  ssms
//
//  Created by 烨 王 on 12-2-23.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#ifndef ssms_ibecommon_h
#define ssms_ibecommon_h

#define PBC_G_SIZE 128
#define PBC_ZR_SIZE 20
#define MAX_PAIRING_STR_LENGTH 512

typedef unsigned char byte;

typedef long long IBE_LONG;

unsigned int hex(byte* data, unsigned int length, char* buffer, int capacity);

unsigned int unhex(char* hex, unsigned int length, byte* data, int capacity);

void long2bytes(IBE_LONG long64, byte buffer[8]);

long long bytes2long(byte data[8]);

void int2bytes(int int32, byte buffer[4]);

int bytes2int(byte data[4]);

// DEBUG use only
unsigned int print_hex(byte* data, unsigned int length);

#endif
