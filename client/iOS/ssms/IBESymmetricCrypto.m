//
//  IBESymmetricCrypto.m
//  ssms
//
//  Created by 烨 王 on 12-3-27.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import "IBESymmetricCrypto.h"
#import <CommonCrypto/CommonCryptor.h>

@implementation IBEAESCrypt

- (id)init {
    self = [super init];
    if (self) {
        result = nil;
        mode = ENCRYPT_MODE;
    }
    return self;
}

- (id)initWithKey:(NSData*) key andIV:(NSData*) iv {
    self = [self init];
    if (self) {
        aeskey = [[NSMutableData alloc]initWithData:key];
        aesiv = [[NSMutableData alloc]initWithData:iv];
    }
    return self;
}

- (id)initWithKeyBytes:(unsigned char*) key withLength:(size_t) keyLength andIVBytes:(unsigned char*) iv withLength:(size_t) ivLength {
    self = [self init];
    if (self) {
        aeskey = [[NSMutableData alloc]initWithBytes:key length:keyLength];
        aesiv = [[NSMutableData alloc]initWithBytes:iv length:ivLength];
    }
    return self;
}

- (void)setMode:(NSInteger) cryptmode {
    if (cryptmode != ENCRYPT_MODE && cryptmode != DECRYPT_MODE) {
        return;
    }
    mode = cryptmode;
}

- (void)crypt:(NSData*) data {
    void* buffer = malloc([data length] + 16);
    size_t numBytesEncrypted;
    CCCryptorStatus cryptStatus = CCCrypt(mode == ENCRYPT_MODE ? kCCEncrypt : kCCDecrypt,
                                          kCCAlgorithmAES128,
                                          kCCOptionPKCS7Padding,
                                          [aeskey mutableBytes], kCCKeySizeAES256,
                                          [aesiv mutableBytes],
                                          [data bytes], [data length],
                                          buffer, [data length] + 16,
                                          &numBytesEncrypted);
    if (cryptStatus == kCCSuccess) {
        result = [[NSMutableData alloc]initWithBytesNoCopy:buffer length:numBytesEncrypted];
        return;
    }
    if (result) {
        void* pointer;
        NSInteger length;
        pointer = [result mutableBytes];
        length = [result length];
        memset(pointer, 0, length);
        result = nil;
    }
}

- (NSData*)getResult {
    NSData* dataCopy = [result copy];
    return dataCopy;
}

- (void)destroy {
    void* pointer;
    NSInteger length;

    pointer = [aeskey mutableBytes];
    length = [aeskey length];
    memset(pointer, 0, length);

    pointer = [aesiv mutableBytes];
    length = [aesiv length];
    memset(pointer, 0, length);

    pointer = [result mutableBytes];
    length = [result length];
    memset(pointer, 0, length);
}

@end
