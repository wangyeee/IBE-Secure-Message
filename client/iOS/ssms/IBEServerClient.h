//
//  IBEServerClient.h
//  ssms
//
//  Created by 烨 王 on 12-3-26.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "IBECore.h"

// 请求协议
@protocol IBERequest <NSObject>

// 获取要执行的操作
- (NSString*)getAction;

@optional
// 获取要发送的数据
// 如果没有数据要发送 返回nil
- (NSData *)getDataToSent;

// 收发信息时 向中继服务器发送POST请求
- (NSMutableDictionary *)getDataToPost;

@end


// 处理服务器响应协议
@protocol IBEResponseHandler <NSObject>

// 处理返回的数据
- (void) dataReceived:(NSData *) data;

@optional
// 处理错误
- (void) errorOccured:(NSError *) error;

@end

//////////////////////////////////////////////////////////////////////

@interface RetrieveMessageRequest : NSObject <IBERequest> {
@private
    NSString *email;
    NSString *password;
    NSInteger amount;
    NSInteger status;
    NSDate *start;
    NSDate *end;
    NSArray *receivedMessages;
}

- (void)setReceivedMessages:(NSArray*)messages;

- (id)initWithEmail:(NSString *)email andPassword:(NSString *)password toGet:(NSInteger)amount MessagesWithStatus:(NSInteger)status from:(NSDate *)start to:(NSDate *)end;

@end

//////////////////////////////////////////////////////////////////////

@interface SendNewMessageRequest : NSObject <IBERequest> {
@private
    NSString *recipient;
    NSString *content;
    NSString *email;
    NSString *password;
}

- (id)initWithEmail:(NSString*)email andPassword:(NSString*)password WithMessage:(NSString*)content forRecipient:(NSString*)recipient;

@end

//////////////////////////////////////////////////////////////////////

@interface DownloadIDRequest : NSObject <IBERequest> {
@private
    NSString* email;
    NSString* password;    
    NSString* idStr;
    NSString* accPwd;
}

- (id)initWithEmail:(NSString*)email andPassword:(NSString*)password forID:(NSString*)idString withAccessPassword:(NSString*)accessPassword;

@end

//////////////////////////////////////////////////////////////////////

@interface CheckIDRequest : NSObject <IBERequest> {
@private
    NSString* email;
    NSString* password;
    NSUInteger page;
    NSUInteger amount;
    NSUInteger status;
}

- (id)initWithEmail:(NSString*)email andPassword:(NSString*)password page:(NSUInteger)page amount:(NSUInteger)amount;

- (id)initWithEmail:(NSString*)email andPassword:(NSString*)password page:(NSUInteger)page amount:(NSUInteger)amount withStatus:(NSUInteger)status;

@end

//////////////////////////////////////////////////////////////////////

@interface ApplyNewIDRequest : NSObject <IBERequest> {
@private
    NSString* email;
    NSString* password;
    NSInteger systemNumber;
    NSString* idStr;
    NSString* accessPassword;
}

- (id)initWithEmail:(NSString*)email password:(NSString*)password system:(NSInteger)system idString:(NSString*)idStr accessPassword:(NSString*)accessPassword;

@end

//////////////////////////////////////////////////////////////////////

@interface LoginIBERequest : NSObject <IBERequest> {
@private
    NSString* email;
    NSString* password;
}

- (id)initWithEmail:(NSString*) email andPassword:(NSString*) password;

@end
//////////////////////////////////////////////////////////////////////

@interface RegisterIBERequest : NSObject <IBERequest> {
@private
    NSString* username;
    NSString* email;
    NSString* password;
}

- (id)initWithUsername:(NSString*)username email:(NSString*)email andPassword:(NSString*)password;
@end

//////////////////////////////////////////////////////////////////////s

@interface IBEServerClientHTTPImpl : NSObject {
@private
    NSString* requestUrl;
@protected
    IBEConfig* settings;
}

// 要发送的请求 遵循IBERequest协议
@property (retain) id request;
@property (retain) id responseHandler;
@property (readonly) NSInteger dataReady;

// 通过一个url初始化
- (id)initWithURL:(NSString*) url;

// 通过url和待发送请求初始化
- (id)initWithURL:(NSString*) url andRequest:(id) request;

// 通过url、待发送请求和响应处理器初始化
- (id)initWithURL:(NSString*) url andRequest:(id) request withHandler:(id) handler;

// 发送请求
- (void)startRequest;

@end

//////////////////////////////////////////////////////////////////////s

@interface IBERelayServerClientHTTPImpl : IBEServerClientHTTPImpl {
@private
    NSString *relayUrl;
}

- (NSData*)startSynchronousRequest;

@end
