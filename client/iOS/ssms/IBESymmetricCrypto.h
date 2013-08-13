//
//  IBESymmetricCrypto.h
//  ssms
//
//  Created by 烨 王 on 12-3-27.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

#define ENCRYPT_MODE 0
#define DECRYPT_MODE 1

// AES对称加密工具
@interface IBEAESCrypt : NSObject {
@private
    NSMutableData* aeskey;
    NSMutableData* aesiv;
    NSInteger mode;
    NSMutableData* result;
}

- (id)initWithKey:(NSData*) key andIV:(NSData*) iv;

- (id)initWithKeyBytes:(unsigned char*) key withLength:(size_t) keyLength andIVBytes:(unsigned char*) iv withLength:(size_t) ivLength;

- (void)setMode:(NSInteger) mode;

- (void)crypt:(NSData *)data;

- (NSData*)getResult;

- (void)destroy;

@end
