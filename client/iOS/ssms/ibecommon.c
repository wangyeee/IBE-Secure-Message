//
//  ibecommon.c
//  ssms
//
//  Created by 烨 王 on 12-2-23.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#include "ibecommon.h"
#include "stdio.h"
#include "stdlib.h"

unsigned int hex(byte* data, unsigned int length, char* buffer, int capacity) {
    int i;
    for (i = 0; i < length; i++) {
        if (i + i >= capacity)
            break;
        buffer += sprintf(buffer, "%02x", data[i]);
    }
    return i + i;
}

unsigned int unhex(char* buffer, unsigned int length, byte* data, int capacity) {
    int i;
    char tmp[3];
    tmp[2] = '\0';
    for (i = 0; i < length; i++) {
        tmp[0] = buffer[i];
        i++;
        tmp[1] = buffer[i];
        int value = strtol(tmp, NULL, 16);
        *data = (byte) value;
        data++;
    }
    return i;
}
void long2bytes(IBE_LONG long64, byte buffer[8]) {
    buffer[0] = (byte) (long64 >> 56);
    buffer[1] = (byte) (long64 >> 48);
    buffer[2] = (byte) (long64 >> 40);
    buffer[3] = (byte) (long64 >> 32);
    buffer[4] = (byte) (long64 >> 24);
    buffer[5] = (byte) (long64 >> 16);
    buffer[6] = (byte) (long64 >> 8);
    buffer[7] = (byte) long64;
}

long long bytes2long(byte data[8]) {
    return (((long long) data[0] << 56) |
            ((long long) data[1] << 48) |
            ((long long) data[2] << 40) |
            ((long long) data[3] << 32) |
            ((long long) data[4] << 24) |
            ((long long) data[5] << 16) |
            ((long long) data[6] <<  8) |
            ((long long) data[7]));
}

void int2bytes(int int32, byte buffer[4]) {
    buffer[0] = (byte) (int32 >> 24);
    buffer[1] = (byte) (int32 >> 16);
    buffer[2] = (byte) (int32 >> 8);
    buffer[3] = (byte) (int32);
}

int bytes2int(byte data[4]) {
    return (int) (data[0] << 24 | data[1] << 16 | data[2] << 8 | data[3]);
}

// DEBUG
unsigned int print_hex(byte* data, unsigned int length) {
    unsigned int i;
    for (i = 0; i < length; i++)
        printf("%02x", data[i]);
    printf("\n");
    return i;
}
