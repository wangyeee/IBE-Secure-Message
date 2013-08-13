//
//  IBECore.m
//  ssms
//
//  Created by 烨 王 on 12-3-24.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import "IBECore.h"
#import "ibe.h"
#import "IBEAppDelegate.h"
#import "IBEEntity.h"
#import "IBEDAO.h"

@implementation IBEPublicParameter

@synthesize paramG;
@synthesize paramG1;
@synthesize paramH;
@synthesize pairing;

- (id)initWithBytesParamG:(unsigned char*) g paramG1:(unsigned char*) g1 paramH:(unsigned char*) h andPairing:(char*) p withLength:(int) length {
    if (self = [super init]) {
        paramG = [[NSData alloc]initWithBytes:g length:PBC_G_SIZE];
        paramG1 = [[NSData alloc]initWithBytes:g1 length:PBC_G_SIZE];
        paramH = [[NSData alloc]initWithBytes:h length:PBC_G_SIZE];
        pairing = [[NSData alloc]initWithBytes:p length:length];
    }
    return self;
}

- (int) lengthInBytes {
    return PBC_G_SIZE * 3 + 4 + [pairing length];
}

- (int) toBytes: (unsigned char*) buffer withCapacity: (unsigned int) capacity {
    int length = [pairing length];
    byte* buffer0 = buffer;
    if (PBC_G_SIZE * 3 + 4 + length > capacity) {
        return -1;
    }
    memcpy(buffer0, [paramG bytes], PBC_G_SIZE);
    buffer0 += PBC_G_SIZE;
    memcpy(buffer0, [paramG1 bytes], PBC_G_SIZE);
    buffer0 += PBC_G_SIZE;
    memcpy(buffer0, [paramH bytes], PBC_G_SIZE);
    buffer0 += PBC_G_SIZE;
    byte intValue[4];
    int2bytes(length, intValue);
    memcpy(buffer0, intValue, 4);
    buffer0 += 4;
    memcpy(buffer0, [pairing bytes], length);
    return PBC_G_SIZE * 3 + 4 + length;
}

- (int) buildFromBytes: (unsigned char*) data withLength: (unsigned int) length {
    if (length < PBC_G_SIZE * 3 + 4)
        return -1;
    paramG = [NSData dataWithBytes: data length: PBC_G_SIZE];
    data += PBC_G_SIZE;
    paramG1 = [NSData dataWithBytes: data length: PBC_G_SIZE];
    data += PBC_G_SIZE;
    paramH = [NSData dataWithBytes: data length: PBC_G_SIZE];
    data += PBC_G_SIZE;
    byte intValue[4];
    memcpy(intValue, data, 4);
    int pairingLength = bytes2int(intValue);
    if (length < PBC_G_SIZE * 3 + 4 + pairingLength) {
        paramG = nil;
        paramG1 = nil;
        paramH = nil;
        return -1;
    }
    data += 4;
    pairing = [NSData dataWithBytes:data length:pairingLength];
    return PBC_G_SIZE * 3 + 4 + pairingLength;
}

@end


@implementation IBEPlainText

@synthesize plainText;
@synthesize length;

- (id) init {
    if (self = [super init]) {
    }
    return self;
}

+ (IBEPlainText*) buildFromSignificantBytes: (unsigned char*) bytes withLength: (int) significantLength {
    if (significantLength < 1 || significantLength > PBC_G_SIZE - 2)
        return nil;
    const int IBE_HALF = PBC_G_SIZE / 2 - 1;
    IBEPlainText* text = [[IBEPlainText alloc] init];
    byte* temp = (byte*) malloc(PBC_G_SIZE);
    memset(temp, 0, PBC_G_SIZE);
    if (significantLength > IBE_HALF) {
        memcpy(temp + PBC_G_SIZE - significantLength - 1, bytes, significantLength - IBE_HALF);
        memcpy(temp + PBC_G_SIZE - IBE_HALF, bytes + significantLength - IBE_HALF, IBE_HALF);
    } else {
        memcpy(temp + PBC_G_SIZE -significantLength, bytes, significantLength);
    }
    text.plainText = [NSData dataWithBytes:temp length:PBC_G_SIZE];
    text.length = significantLength;
    memset(temp, 0, PBC_G_SIZE);
    free(temp);
    return text;
}

- (NSData*) toSignificantBytes {
    byte* temp = (byte*) malloc(length);
    memset(temp, 0, length);
    int IBE_HALF = PBC_G_SIZE / 2 - 1;
    if (length > IBE_HALF) {
        memcpy(temp, [plainText bytes] + PBC_G_SIZE - length - 1, length - IBE_HALF);
        memcpy(temp + length - IBE_HALF, [plainText bytes] + PBC_G_SIZE - IBE_HALF, IBE_HALF);
    } else {
        memcpy(temp, [plainText bytes] + PBC_G_SIZE - length, length);
    }
    NSData* sig = [NSData dataWithBytes:temp length:length];
    memset(temp, 0, length);
    free(temp);
    return sig;
}

- (int) lengthInBytes {
    return PBC_G_SIZE + 1;    
}

- (int) toBytes: (unsigned char*) buffer withCapacity: (unsigned int) capacity {
    if (capacity < PBC_G_SIZE + 1)
        return -1;
    memcpy(buffer, [plainText bytes], PBC_G_SIZE);
    buffer[PBC_G_SIZE] = (byte) length;
    return PBC_G_SIZE + 1;
}

- (int) buildFromBytes: (unsigned char*) data withLength: (unsigned int) dataLength {
    if (dataLength < PBC_G_SIZE + 1) {
        return -1;
    }
    plainText = [NSData dataWithBytes:data length:PBC_G_SIZE];
    length = data[PBC_G_SIZE];
    return PBC_G_SIZE + 1;
}

@end


@implementation IBECipherText

@synthesize cipherText;
@synthesize length;

- (int)lengthInBytes {
    return PBC_G_SIZE * 3 + 1;
}

- (int)toBytes: (unsigned char*) buffer withCapacity: (unsigned int) capacity {
    if (capacity < PBC_G_SIZE * 3 + 1)
        return -1;
    memcpy(buffer, [cipherText bytes], PBC_G_SIZE * 3);
    buffer[PBC_G_SIZE * 3] = (byte) length;
    return PBC_G_SIZE * 3 + 1;
}

- (int)buildFromBytes: (unsigned char*) data withLength: (unsigned int) dataLength {
    if (dataLength < PBC_G_SIZE * 3 + 1)
        return -1;
    cipherText = [NSData dataWithBytes:data length:PBC_G_SIZE * 3];
    length = data[PBC_G_SIZE * 3];
    return PBC_G_SIZE * 3 + 1;
}

@end


@implementation IBEPrivateKey

@synthesize rID;
@synthesize hID;
@synthesize pairing;
@synthesize userString;

- (int) lengthInBytes {
    NSData* userRaw = [userString dataUsingEncoding:NSUTF8StringEncoding];
    return PBC_ZR_SIZE + PBC_G_SIZE + 8 + [userRaw length] + [pairing length];
}

- (int) toBytes: (unsigned char*) buffer withCapacity: (unsigned int) capacity {
    NSInteger plength = [pairing length];
    NSData* userRaw = [userString dataUsingEncoding:NSUTF8StringEncoding];
    NSInteger ulength = [userRaw length];
    if (PBC_ZR_SIZE + PBC_G_SIZE + 8 + ulength + plength > capacity)
        return -1;
    memcpy(buffer, [rID bytes], PBC_ZR_SIZE);
    buffer += PBC_ZR_SIZE;
    memcpy(buffer, [hID bytes], PBC_G_SIZE);
    buffer += PBC_G_SIZE;
    byte temp[4];
    memset(temp, 0, 4);
    int2bytes(ulength, temp);
    memcpy(buffer, temp, 4);
    buffer += 4;
    memcpy(buffer, [userRaw bytes], ulength);
    buffer += ulength;
    memset(temp, 0, 4);
    int2bytes([pairing length], temp);
    memcpy(buffer, temp, 4);
    buffer += 4;    
    memcpy(buffer, [pairing bytes], plength);
    return PBC_ZR_SIZE + PBC_G_SIZE + 8 + ulength + plength;
}

- (int) buildFromBytes: (unsigned char*) data withLength: (unsigned int) length {
    if (length < PBC_G_SIZE + PBC_ZR_SIZE + 8)
        return -1;
    rID = [NSData dataWithBytes:data length:PBC_ZR_SIZE];
    data += PBC_ZR_SIZE;
    hID = [NSData dataWithBytes:data length:PBC_G_SIZE];
    data += PBC_G_SIZE;
    byte temp[4];
    memcpy(temp, data, 4);
    data += 4;
    int ulength = bytes2int(temp);
    if (length < PBC_G_SIZE + PBC_ZR_SIZE + 8 + ulength)
        return -1;
    userString = [[NSString alloc] initWithBytes:data length:ulength encoding: NSUTF8StringEncoding];
    data += ulength;
    memcpy(temp, data, 4);
    data += 4;
    int plength = bytes2int(temp);
    if (length < PBC_G_SIZE + PBC_ZR_SIZE + 8 + ulength + plength)
        return -1;
    pairing = [NSData dataWithBytes:data length:plength];
    return PBC_ZR_SIZE + PBC_G_SIZE + 8 + ulength + plength;
}

@end


@implementation IBEEngine

+ (IBECipherText*) encryptData: (IBEPlainText*) plainText forReceiver: (NSString*) receiver underParameter: (IBEPublicParameter*) parameter {
    byte* cipherContext = (byte*) malloc(PBC_G_SIZE * 3);
    size_t succ = encrypt_str(cipherContext, PBC_G_SIZE * 3,
                              (byte*) [[plainText plainText] bytes], [[plainText plainText] length],           
                              (byte*) [[parameter paramG] bytes], PBC_G_SIZE,
                              (byte*) [[parameter paramG1] bytes], PBC_G_SIZE,
                              (byte*) [[parameter paramH] bytes], PBC_G_SIZE,
                              (byte*) [[receiver dataUsingEncoding:NSUTF8StringEncoding] bytes],
                              [receiver lengthOfBytesUsingEncoding:NSUTF8StringEncoding],
                              (char*) [[parameter pairing] bytes], [[parameter pairing] length]);
    if (succ == 0) {
        IBECipherText* cipherText = [[IBECipherText alloc] init];
        cipherText.cipherText = [NSData dataWithBytes:cipherContext length:PBC_G_SIZE * 3];
        cipherText.length = [plainText length];
        memset(cipherContext, 0, PBC_G_SIZE * 3);
        free(cipherContext);
        return cipherText;
    }
    free(cipherContext);
    return nil;
}

+ (IBEPlainText*) decryptFromCipher: (IBECipherText*) cipherText withKey: (IBEPrivateKey*) key {
    byte* plainContext = (byte*) malloc(PBC_G_SIZE);
    
    size_t succ = decrypt_str(plainContext, PBC_G_SIZE,
                              (byte*) [[cipherText cipherText] bytes], [[cipherText cipherText] length],               
                              (byte*) [[key rID] bytes], PBC_ZR_SIZE,
                              (byte*) [[key hID] bytes], PBC_G_SIZE,
                              (char*) [[key pairing] bytes], [[key pairing] length]);
    if (succ == 0) {
        IBEPlainText* plainText = [[IBEPlainText alloc] init];
        plainText.plainText = [NSData dataWithBytes:plainContext length:PBC_G_SIZE];
        plainText.length = [cipherText length];
        memset(plainContext, 0, PBC_G_SIZE);
        free(plainContext);
        return plainText;
    }
    free(plainContext);
    return nil;
}

@end

@implementation IBEConfig

@synthesize email = _email;
@synthesize password = _password;

static IBEConfig* instance;

+ (IBEConfig*) getInstance {
    if (instance) {
        return instance;
    }
    @synchronized(self) {
        instance = [[self alloc]init];
        return instance;
    }
}

- (NSString*)getIBEServerID {
    return @"localhost";
}

- (NSString*)getIBEServerURL {
    return @"http://127.0.0.1:8080/ibeserver/client";
}

- (NSString*)getRelaryServerURL {
    return @"http://127.0.0.1/relay/relay.php";
}

- (IBEPublicParameter*)getDefaultPublicParameter {
    if (defaultPublicParameter) {
        return defaultPublicParameter;
    }
    @synchronized(self) {
        IBEAppDelegate *appDelegate = [[UIApplication sharedApplication]delegate];
        NSManagedObjectContext *context = [appDelegate managedObjectContext];        
        IBESystemDAO* dao = [[IBESystemDAO alloc]initWithNSManagedObjectContext:context];
        IBESystem* system = [dao getDefaultSystem];
        NSData* data = [system public_params];
        byte* params = (byte*) [data bytes];
        NSData* pair = [system pairing];
        char* pairing = (char*) [pair bytes];
        defaultPublicParameter = [[IBEPublicParameter alloc]initWithBytesParamG:params paramG1:params + 128 paramH:params + 256 andPairing:pairing withLength:[pair length]];
    }
    return defaultPublicParameter;
}
@end
