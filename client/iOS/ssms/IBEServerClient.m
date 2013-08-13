//
//  IBEServerClient.m
//  ssms
//
//  Created by 烨 王 on 12-3-26.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import "IBEServerClient.h"
#import "IBESymmetricCrypto.h"
#import "IBECore.h"
#import "ibecommon.h"
#import "Security/Security.h"
#import "NSData+Base64.h"

@implementation RetrieveMessageRequest

- (id)initWithEmail:(NSString *)eml andPassword:(NSString *)pwd toGet:(NSInteger)amt MessagesWithStatus:(NSInteger)sts from:(NSDate *)s to:(NSDate *)e {
    self = [super init];
    if (self) {
        email = eml;
        password = pwd;
        amount = amt;
        status = sts;
        start = s;
        end = e;
        receivedMessages = nil;
    }
    return self;
}

- (void)setReceivedMessages:(NSArray*)messages {
    receivedMessages = messages;
}

- (NSString*)getAction {
    return @"getmessage";
}

- (NSMutableDictionary *)getDataToPost {
    NSTimeInterval date = start.timeIntervalSince1970;
    NSMutableDictionary *post = [NSMutableDictionary dictionary];
    [post setValue:email forKey:@"email"];
    [post setValue:password forKey:@"password"];
    [post setValue:[NSString stringWithFormat:@"%d", amount] forKey:@"amount"];
    [post setValue:[NSString stringWithFormat:@"%d", status] forKey:@"status"];
    [post setValue:[NSString stringWithFormat:@"%f", date] forKey:@"start"];
    date = end.timeIntervalSince1970;
    [post setValue:[NSString stringWithFormat:@"%f", date] forKey:@"end"];
    if (receivedMessages && [receivedMessages count] > 0) {
        NSMutableString *msgs = [[NSMutableString alloc]init];
        for (NSNumber* mid in receivedMessages) {
            [msgs appendFormat:@"%d", [mid intValue]];
            [msgs appendString:@","];
        }
        NSRange range;
        range.location = [msgs length] - 1;
        range.length = 1;
        [msgs deleteCharactersInRange:range]; 
        NSLog(@"Received Messages:%@", msgs);
        [post setValue:msgs forKey:@"rmessages"];
    }
    return post;
}

@end

@implementation SendNewMessageRequest

- (NSString*)getAction {
    return @"newmessage";
}

- (NSMutableDictionary *)getDataToPost {
    NSMutableDictionary *post = [NSMutableDictionary dictionary];
    [post setValue:email forKey:@"email"];
    [post setValue:password forKey:@"password"];
    [post setValue:recipient forKey:@"recipient"];
    [post setValue:content forKey:@"content"];
    return post;
}

- (id)initWithEmail:(NSString*)eml andPassword:(NSString*)pwd WithMessage:(NSString*)cnt forRecipient:(NSString*)rpt {
    self = [super init];
    if (self) {
        email = eml;
        password = pwd;
        content = cnt;
        recipient = rpt;
    }
    return self;
}

@end

/////////////////////////////////////////////////////////////////////////////

@implementation DownloadIDRequest

- (id)initWithEmail:(NSString*)eml andPassword:(NSString*)pwd forID:(NSString*)idString withAccessPassword:(NSString*)accessPassword {
    self = [super init];
    if (self) {
        email = eml;
        password = pwd;
        idStr = idString;
        accPwd = accessPassword;
    }
    return self;
}

- (NSString*)getAction {
    return @"getUserKey";
}

- (NSData *)getDataToSent {
    NSData* emailbin = [email dataUsingEncoding:NSUTF8StringEncoding];
    NSData* pwdbin = [password dataUsingEncoding:NSUTF8StringEncoding];
    NSData* idBin = [idStr dataUsingEncoding:NSUTF8StringEncoding];
    NSData* acPwdBin = [accPwd dataUsingEncoding:NSUTF8StringEncoding];
    byte len[4];
    NSMutableData* data = [[NSMutableData alloc]init];
    len[0] = [emailbin length];
    len[1] = [pwdbin length];
    len[2] = [idBin length];
    len[3] = [acPwdBin length];
    [data appendBytes:len length:1];
    [data appendData:emailbin];
    [data appendBytes:len + 1 length:1];
    [data appendData:pwdbin];
    [data appendBytes:len + 2 length:1];
    [data appendData:idBin];
    [data appendBytes:len + 3 length:1];
    [data appendData:acPwdBin];
    return data;
}

@end

/////////////////////////////////////////////////////////////////////////////

@implementation CheckIDRequest

- (id)initWithEmail:(NSString *)eml andPassword:(NSString *)pwd page:(NSUInteger)pg amount:(NSUInteger)amt {
    return [self initWithEmail:eml andPassword:pwd page:pg amount:amt withStatus:0];
}

- (id)initWithEmail:(NSString *)eml andPassword:(NSString *)pwd page:(NSUInteger)pg amount:(NSUInteger)amt withStatus:(NSUInteger)sts {
    self = [super init];
    if (self) {
        email = eml;
        password = pwd;
        page = pg;
        amount = amt;
        status = sts;
    }
    return self;
}

- (NSString*)getAction {
    return @"listIds";
}

- (NSData *)getDataToSent {
    NSData* emailbin = [email dataUsingEncoding:NSUTF8StringEncoding];
    NSData* pwdbin = [password dataUsingEncoding:NSUTF8StringEncoding];
    byte len[2];
    byte pageBin[4];
    NSMutableData* data = [[NSMutableData alloc]init];
    len[0] = [email length];
    len[1] = [password length];
    [data appendBytes:len length:1];
    [data appendData:emailbin];
    [data appendBytes:len + 1 length:1];
    [data appendData:pwdbin];
    int2bytes(page, pageBin);
    [data appendBytes:pageBin length:4];
    len[0] = amount;
    len[1] = status;
    [data appendBytes:len length:2];
    return data;
}

@end

@implementation ApplyNewIDRequest

- (id)initWithEmail:(NSString*)em password:(NSString*)pwd system:(NSInteger)sys idString:(NSString*)idString accessPassword:(NSString*)accPwd {
    self = [super init];
    if (self) {
        email = em;
        password = pwd;
        systemNumber = sys;
        idStr = idString;
        accessPassword = accPwd;
    }
    return self;
}

- (NSString*)getAction {
    return @"applyUserKey";
}

- (NSData *)getDataToSent {
    NSData* emailbin = [email dataUsingEncoding:NSUTF8StringEncoding];
    NSData* pwdbin = [password dataUsingEncoding:NSUTF8StringEncoding];
    NSData* idBin = [idStr dataUsingEncoding:NSUTF8StringEncoding];
    NSData* acPwdBin = [accessPassword dataUsingEncoding:NSUTF8StringEncoding];
    byte len[4];
    byte sysNo[4];
    NSMutableData* data = [[NSMutableData alloc]init];
    len[0] = [emailbin length];
    len[1] = [pwdbin length];
    len[2] = [idBin length];
    len[3] = [acPwdBin length];
    [data appendBytes:len length:1];
    [data appendData:emailbin];
    [data appendBytes:len + 1 length:1];
    [data appendData:pwdbin];
    memset(sysNo, 0, 4);
    int2bytes(systemNumber, sysNo);
    [data appendBytes:sysNo length:4];
    [data appendBytes:len + 2 length:1];
    [data appendData:idBin];
    [data appendBytes:len + 3 length:1];
    [data appendData:acPwdBin];
    return data;
}

@end

//////////////////////////////////////////////////////////////////////

@implementation LoginIBERequest

// 获取要执行的操作
- (NSString*)getAction {
    return @"login";
}

// 获取要发送的数据
// 如果没有数据要发送 返回nil
- (NSData *)getDataToSent {
    NSData* emailbin = [email dataUsingEncoding:NSUTF8StringEncoding];
    NSData* pwdbin = [password dataUsingEncoding:NSUTF8StringEncoding];
    byte len[2];
    NSMutableData* data = [[NSMutableData alloc]init];
    len[0] = [email length];
    len[1] = [password length];
    [data appendBytes:len length:1];
    [data appendData:emailbin];
    [data appendBytes:len + 1 length:1];
    [data appendData:pwdbin];
    return data;
}

- (id)initWithEmail:(NSString*) em andPassword:(NSString*) pwd {
    self = [super init];
    if (self) {
        email = em;
        password = pwd;
    }
    return self;
}

@end
//////////////////////////////////////////////////////////////////////

@implementation RegisterIBERequest

- (NSString*)getAction {
    return @"register";
}

// 获取要发送的数据
// 如果没有数据要发送 返回nil
- (NSData *)getDataToSent {
    NSData* namebin = [username dataUsingEncoding:NSUTF8StringEncoding];
    NSData* emailbin = [email dataUsingEncoding:NSUTF8StringEncoding];
    NSData* pwdbin = [password dataUsingEncoding:NSUTF8StringEncoding];
    byte len[3];
    NSMutableData* data = [[NSMutableData alloc]init];
    len[0] = [username length];
    len[1] = [email length];
    len[2] = [password length];
    [data appendBytes:len length:1];
    [data appendData:namebin];
    [data appendBytes:len + 1 length:1];
    [data appendData:emailbin];
    [data appendBytes:len + 2 length:1];
    [data appendData:pwdbin];
    return data;
}

- (id)initWithUsername:(NSString*) name email:(NSString*) mail andPassword:(NSString*) pwd {
    self = [super init];
    if (self) {
        username = name;
        email = mail;
        password = pwd;
    }
    return self;
}
@end
//////////////////////////////////////////////////////////////////////

@implementation IBEServerClientHTTPImpl

@synthesize request = _request;
@synthesize dataReady = _dataReady;
@synthesize responseHandler = _responseHandler;

- (id)init {
    self = [super init];
    if (self) {
        _dataReady = -1;
        settings = [IBEConfig getInstance];
    }
    return self;
}

- (id)initWithURL:(NSString*) url {
    self = [self init];
    if (self) {
        requestUrl = url;
    }
    return self;
}

- (id)initWithURL:(NSString*) url andRequest:(id) request {
    self = [self initWithURL:url];
    if (self) {
        _request = request;
    }
    return self;
}

- (id)initWithURL:(NSString*) url andRequest:(id) request withHandler:(id) handler {
    self = [self initWithURL:url andRequest:request];
    if (self) {
        _responseHandler = handler;
    }
    return self;
}

- (void)startRequest0 {
    unsigned char lentemp[1];
    NSMutableData* body = [[NSMutableData alloc]init];
    NSData* action = [[_request getAction] dataUsingEncoding:NSUTF8StringEncoding];
    lentemp[0] = [action length];
    [body appendBytes:lentemp length:1];
    [body appendData:action];
    
    byte* sessKey = (byte*) malloc(48);
    int randgen = SecRandomCopyBytes(kSecRandomDefault, 48, sessKey);
    if (randgen == -1) {
        // TODO error
    }
    IBEPlainText* plain = [IBEPlainText buildFromSignificantBytes:sessKey withLength:48];
    IBECipherText* secretSessKey = [IBEEngine encryptData:plain forReceiver:[settings getIBEServerID] underParameter:[settings getDefaultPublicParameter]];
    [body appendData:[secretSessKey cipherText]];

    IBEAESCrypt* aesCrypt = [[IBEAESCrypt alloc]initWithKeyBytes:sessKey withLength:32 andIVBytes:sessKey + 32 withLength:16];
    [aesCrypt setMode:ENCRYPT_MODE];
    [aesCrypt crypt:[_request getDataToSent]];
    [body appendData:[aesCrypt getResult]];
    memset(sessKey, 0, 48);
    NSURL* url = [NSURL URLWithString:requestUrl];
    NSMutableURLRequest* request = [NSMutableURLRequest requestWithURL:url];
    [request setHTTPMethod:@"POST"];
    [request setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-Type"];
    [request setHTTPBody:body];
    _dataReady = 1;

    NSURLResponse* response;
    NSError* error;
    NSData* data = [NSURLConnection sendSynchronousRequest:request returningResponse:&response error:&error];
    if (error) {
        [_responseHandler errorOccured:error];
        [aesCrypt destroy];
        return;
    }
    _dataReady = 0;
    [aesCrypt setMode:DECRYPT_MODE];
    [aesCrypt crypt:data];
    if (_responseHandler) {
        NSData* dataCopy = [aesCrypt getResult];
        [aesCrypt destroy];
        [_responseHandler dataReceived:dataCopy];
        return;
    }
    [aesCrypt destroy];
}

- (void)startRequest {
    [self performSelectorInBackground:@selector(startRequest0) withObject:nil];
}

@end


@implementation IBERelayServerClientHTTPImpl

- (id)initWithURL:(NSString*) url {
    self = [super init];
    if (self) {
        relayUrl = url;
    }
    return self;
}

/*
 IBE密文格式：
 1字节密文长度（恒等于48）
 384字节IBE密文
 若干字节AES密文
 */
- (void)startRequest0 {
    @try {
        NSData *responseData = [self startSynchronousRequest];
        [super.responseHandler dataReceived:responseData];
    }
    @catch (NSError *error) {
        [super.responseHandler errorOccured:error];
    }
    @finally {
    }    
}

- (void)startRequest {
    if (!settings) {
        NSLog(@"Warning: Null Settings!");
        settings = [IBEConfig getInstance];
    }
    [self performSelectorInBackground:@selector(startRequest0) withObject:nil];
}

- (NSData*)startSynchronousRequest {
    static NSString *ContentKey = @"content";
    NSMutableDictionary *data = [super.request getDataToPost];
    NSString *toEncrypt = [data valueForKey:ContentKey];
    if (toEncrypt) {
        NSMutableData *body = [[NSMutableData alloc]init];
        byte len[4];
        len[0] = 48;
        
        byte* sessKey = (byte*) malloc(48);
        int randgen = SecRandomCopyBytes(kSecRandomDefault, 48, sessKey);
        if (randgen == -1) {
            // TODO error
        }

        printf("session key:\n");
        for (int x = 0; x < 48; x++) {
            printf("%02x", sessKey[x]);
        }
        printf("\n");

        IBEPlainText* plain = [IBEPlainText buildFromSignificantBytes:sessKey withLength:48];
        IBECipherText* secretSessKey = [IBEEngine encryptData:plain forReceiver:[data valueForKey:@"recipient"] underParameter:[settings getDefaultPublicParameter]];
        [body appendData:[secretSessKey cipherText]];
        [body appendBytes:len length:1];

        IBEAESCrypt* aesCrypt = [[IBEAESCrypt alloc]initWithKeyBytes:sessKey withLength:32 andIVBytes:sessKey + 32 withLength:16];
        [aesCrypt setMode:ENCRYPT_MODE];
        [aesCrypt crypt:[toEncrypt dataUsingEncoding:NSUTF8StringEncoding]];
        [body appendData:[aesCrypt getResult]];
        
        NSLog(@"%@", body);
        NSLog(@"%d", [body length]); 
        
        memset(sessKey, 0, 48);
        NSString *newContent = [body base64EncodedString];
        
        NSLog(@"%@", newContent);

        newContent = [newContent stringByReplacingOccurrencesOfString:@"=" withString:@"%3D"];
        newContent = [newContent stringByReplacingOccurrencesOfString:@"+" withString:@"*"];
        newContent = [newContent stringByReplacingOccurrencesOfString:@"/" withString:@"-"];
        NSLog(@"%@", newContent);
        
        [data setValue:newContent forKey:ContentKey];
        [aesCrypt destroy];
    }
    [data setValue:[super.request getAction]forKey:@"action"];
    NSURL* url = [NSURL URLWithString:relayUrl];
    NSMutableURLRequest* request = [NSMutableURLRequest requestWithURL:url];
    [request setHTTPMethod:@"POST"];
    [request setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-Type"];
    NSMutableString *postBody = [[NSMutableString alloc]init];
    for (NSString *key in [data allKeys]) {
        [postBody appendFormat:@"%@=%@&", key, [data valueForKey:key]];
    }
    NSRange range;
    range.location = [postBody length] - 1;
    range.length = 1;
    [postBody deleteCharactersInRange:range];   // 删除最后一个&
    NSLog(@"request body:%@", postBody);
    [request setHTTPBody:[postBody dataUsingEncoding:NSUTF8StringEncoding]];

    NSURLResponse* response;
    NSError* error;
    NSData* responseData = [NSURLConnection sendSynchronousRequest:request returningResponse:&response error:&error];
    if (error) {
        //TODO 处理异常
        if (error.code == -1001 && [error.domain isEqualToString:@"NSURLErrorDomain"]) {
            // TODO 网络连接超时
            NSLog(@"%@", error.localizedDescription);
        } else {
            @throw error;
        }
    }
    return responseData;
}

@end
