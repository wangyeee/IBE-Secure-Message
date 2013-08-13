//
//  IBECore.h
//  ssms
//
//  Created by 烨 王 on 12-3-24.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol IBESerializable <NSObject>

- (int)lengthInBytes;

- (int)toBytes: (unsigned char*) buffer withCapacity: (unsigned int) capacity;

- (int)buildFromBytes: (unsigned char*) data withLength: (unsigned int) length;

@end

@interface IBEPublicParameter : NSObject <IBESerializable>

@property (retain) NSData* paramG;
@property (retain) NSData* paramG1;
@property (retain) NSData* paramH;
@property (retain) NSData* pairing;

- (id)initWithBytesParamG:(unsigned char*) paramG paramG1:(unsigned char*) paramG1 paramH:(unsigned char*) paramH andPairing:(char*) pairing withLength:(int) length;

@end


@interface IBEPlainText : NSObject <IBESerializable>

@property (retain) NSData* plainText;
@property NSInteger length;

+ (IBEPlainText*) buildFromSignificantBytes: (unsigned char*) bytes withLength: (int) significantLength;

- (NSData*) toSignificantBytes;

@end


@interface IBECipherText : NSObject <IBESerializable>

@property (retain) NSData* cipherText;
@property NSInteger length;

@end


@interface IBEPrivateKey : NSObject <IBESerializable>

@property (retain) NSData* rID;
@property (retain) NSData* hID;
@property (retain) NSData* pairing;
@property (retain) NSString* userString;

@end


@interface IBEEngine : NSObject

+ (IBECipherText*) encryptData: (IBEPlainText*) plainText forReceiver: (NSString*) receiver underParameter: (IBEPublicParameter*) parameter;

+ (IBEPlainText*) decryptFromCipher: (IBECipherText*) cipherText withKey: (IBEPrivateKey*) key;

@end

@interface IBEConfig : NSObject {
@private
    IBEPublicParameter* defaultPublicParameter;
}

@property (retain) NSString* email;
@property (retain) NSString* password;

+ (IBEConfig*)getInstance;

- (NSString*)getIBEServerID;

- (NSString*)getIBEServerURL;

- (NSString*)getRelaryServerURL;

- (IBEPublicParameter*)getDefaultPublicParameter;
@end
