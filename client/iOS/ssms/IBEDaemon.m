//
//  IBEDaemon.m
//  ssms
//
//  Created by 烨 王 on 12-4-22.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import "IBEDaemon.h"
#import "IBEAppDelegate.h"
#import "SBJson.h"
#import "NSData+Base64.h"
#import "IBESymmetricCrypto.h"
#import "ibecommon.h"

@implementation IBEMessageDaemon

static IBEMessageDaemon *instance;

+ (IBEMessageDaemon *)getInstance {
    if (instance) {
        return instance;
    }
    @synchronized(self) {
        instance = [[self alloc]init];
    }
    return instance;
}

- (id)init {
    self = [super init];
    if (self) {
        registeredMessageConsumers = [[NSMutableSet alloc]init];
        settings = [IBEConfig getInstance];
        IBEAppDelegate *appDelegate = [[UIApplication sharedApplication]delegate];
        NSManagedObjectContext *context = [appDelegate managedObjectContext];
        messageDAO = [[IBEMessageDAO alloc]initWithNSManagedObjectContext:context];
        contactDAO = [[IBEContactDAO alloc]initWithNSManagedObjectContext:context];
        identityDAO = [[IBEIdentityDescriptionDAO alloc]initWithNSManagedObjectContext:context];
        latestMessageDAO = [[IBELatestMessageDAO alloc]initWithNSManagedObjectContext:context];
        running = YES;
        alreadyRunning = NO;
    }
    return self;
}

- (void)moniteringMessage0:(NSArray*)receivedMessages {
    NSDate *lastestRead = [messageDAO getLastReadMessageDate];
    NSDate *now = [[NSDate alloc]init];
    RetrieveMessageRequest *request = [[RetrieveMessageRequest alloc]initWithEmail:settings.email andPassword:settings.password toGet:10 MessagesWithStatus:1 from:lastestRead to:now];
    if (receivedMessages) {
        NSMutableArray *msgs = [[NSMutableArray alloc]initWithCapacity:[receivedMessages count]];
        for (IBEMessage* msg in receivedMessages) {
            [msgs addObject:[NSNumber numberWithInt:[msg.message_id intValue]]];
        }
        [request setReceivedMessages:msgs];
    }
    IBERelayServerClientHTTPImpl *http = [[IBERelayServerClientHTTPImpl alloc]initWithURL:[settings getRelaryServerURL] andRequest:request withHandler:nil];
    NSData *response = [http startSynchronousRequest];
    NSString *json = [[NSString alloc]initWithData:response encoding:NSUTF8StringEncoding];
    
    NSLog(@"%@", json);
    // 解析JSON数据 显示信息
    // 对信息中content字段解密并封装成IBEMessage对象
    // 向监听器队列中的所有监听器发送新消息到达通知
    SBJsonParser * parser = [[SBJsonParser alloc] init];
    NSError * error = nil;
    NSArray *newMessagesJson = [parser objectWithString:json error:&error];
    if (error) {
        // TODO 处理异常 通常是数据丢失
        NSLog(@"%@", error);
    }

    if ([newMessagesJson isKindOfClass:[NSDictionary class]]) {
        NSString *ctrl = [newMessagesJson valueForKey:@"control"];
        if (ctrl) {
            if ([ctrl isEqualToString:@"nomessage"]) {
                NSLog(@"Long time no messages.");
                // 长时间未收到新消息
                // TODO sleep一段时间
                //sleep(1000);
                if (running) {
                    [self handleNewMessages:nil];
                }
                return;
            }
        }        
    }

    IBEAppDelegate *appDelegate = [[UIApplication sharedApplication]delegate];
    NSManagedObjectContext *context = [appDelegate managedObjectContext];
    NSMutableArray *newMessages = [[NSMutableArray alloc]initWithCapacity:[newMessagesJson count]];
    for (NSMutableDictionary *message in newMessagesJson) {
        NSString *fromID = [message valueForKey:@"FROM_USER"];
        NSString *msgDate = [message valueForKey:@"MESSAGE_DATE"];
        NSString *cipherContent = [message valueForKey:@"CONTENT"];
        NSInteger messageID = [[message valueForKey:@"MESSAGE_ID"]intValue];

        id temp = [messageDAO findByID:[NSNumber numberWithInt:messageID]];
        if (temp) {
            NSLog(@"Message %d already received.", messageID);
            continue;
        }
        cipherContent = [cipherContent stringByReplacingOccurrencesOfString:@"*" withString:@"+"];
        cipherContent = [cipherContent stringByReplacingOccurrencesOfString:@"-" withString:@"/"];
        IBEContact *from = [contactDAO find:fromID];
        if (!from) {
            from = [contactDAO saveDefault:fromID];
        }
        NSDateFormatter *formatter = [[NSDateFormatter alloc]init];
        [formatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
        NSDate *recDate = [formatter dateFromString:msgDate];

        IBEIdentityDescription *myKey = [identityDAO getByIDString:settings.email];
        if (!myKey) {
            // TODO 没有私钥 无法解密
            NSLog(@"fuck no key");
            return;
        }
        unsigned char* idData = (unsigned char*) [myKey.id_data bytes];
        idData += 384;
        int len = bytes2int(idData);
        idData += 4;
        NSData *pairing = [NSData dataWithBytes:idData length:len];
        idData += len;
        IBEPrivateKey *key = [[IBEPrivateKey alloc]init];
        [key setRID:[NSData dataWithBytes:idData length:PBC_ZR_SIZE]];
        idData += PBC_ZR_SIZE;
        [key setHID:[NSData dataWithBytes:idData length:PBC_G_SIZE]];
        [key setUserString:settings.email];
        [key setPairing:pairing];
        NSData *content = [NSData dataFromBase64String:cipherContent];
        unsigned char* cipher = (unsigned char*) [content bytes];
        IBECipherText *sessKeyCipher = [[IBECipherText alloc]init];
        [sessKeyCipher buildFromBytes:cipher withLength:385];
        cipher += 385;
        IBEPlainText *ibeSessKey = [IBEEngine decryptFromCipher:sessKeyCipher withKey:key];
        NSData *sessKey0 = [ibeSessKey toSignificantBytes];
        NSData *messageCipher = [NSData dataWithBytes:cipher length:[content length] - 385];
        unsigned char* sessKey = (unsigned char*) [sessKey0 bytes];
        IBEAESCrypt* aesCrypt = [[IBEAESCrypt alloc]initWithKeyBytes:sessKey withLength:32 andIVBytes:sessKey + 32 withLength:16];
        [aesCrypt setMode:DECRYPT_MODE];
        [aesCrypt crypt:messageCipher];
        NSData *messagePlain = [aesCrypt getResult];
        NSString *msgContent = [[NSString alloc]initWithData:messagePlain encoding:NSUTF8StringEncoding];

        IBEMessage *msg = [messageDAO newManagedObject];
        [msg setContent:msgContent];
        [msg setFrom:from];
        [msg setReceive_date:recDate];
        [msg setType:[NSNumber numberWithInt:MESSAGE_RECEIVED]];
        [msg setMessage_id:[NSNumber numberWithInt:messageID]];
        [latestMessageDAO updateLatestMessage:msg];
        NSLog(@"new message:%@", msg);

        [newMessages addObject:msg];
        [context save:nil];
    }
    if (running) {
        [self handleNewMessages:newMessages];
    } else {
        alreadyRunning = NO;
    }
}

- (void)moniteringMessage {
    if (running && !alreadyRunning) {
        alreadyRunning = YES;
        [self performSelectorInBackground:@selector(moniteringMessage0:) withObject:nil];
    }
}

- (void)handleNewMessages:(NSArray*)messages {
    // 向注册的监听器发送新消息到达事件
    if (messages != nil) {
        for (id consumer in registeredMessageConsumers) {
            [consumer messageReceived:messages];
        }
    }
    // 继续监听新消息
    if (running) {
        [self moniteringMessage0:messages];
    } else {
        alreadyRunning = NO;
    }
}

- (void)addMessageConsumer:(id)consumer {
    if (consumer) {
        @synchronized(registeredMessageConsumers) {
            [registeredMessageConsumers addObject:consumer];
        }
    }
}

- (void)removeMessageConsumer:(id)consumer {
    if (consumer) {
        @synchronized(registeredMessageConsumers) {
            [registeredMessageConsumers removeObject:consumer];
        }
    }
}

- (void)stopRunning {
    running = NO;
}

@end
