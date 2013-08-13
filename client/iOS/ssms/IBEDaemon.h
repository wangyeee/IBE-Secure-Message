//
//  IBEDaemon.h
//  ssms
//
//  Created by 烨 王 on 12-4-22.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "IBECore.h"
#import "IBEServerClient.h"
#import "IBEDAO.h"

@protocol IBEMessageConsumer <NSObject>

- (void)messageReceived:(NSArray*)messages;

@end


@interface IBEMessageDaemon : NSObject {
@private
    NSMutableSet *registeredMessageConsumers;
    IBEConfig *settings;
    IBEMessageDAO *messageDAO;
    IBEContactDAO *contactDAO;
    IBEIdentityDescriptionDAO *identityDAO;
    IBELatestMessageDAO *latestMessageDAO;
    BOOL running;
    BOOL alreadyRunning;
}

+ (IBEMessageDaemon *)getInstance;

- (void)addMessageConsumer:(id)consumer;

- (void)removeMessageConsumer:(id)consumer;

- (void)moniteringMessage;

- (void)handleNewMessages:(NSArray*)messages;

- (void)stopRunning;

@end
