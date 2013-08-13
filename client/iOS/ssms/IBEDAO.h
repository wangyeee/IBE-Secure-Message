//
//  IBEDAO.h
//  ssms
//
//  Created by 烨 王 on 12-4-1.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "IBEEntity.h"

@interface AbstractDAO : NSObject {
@protected
    NSManagedObjectContext* context;    // 访问SQLite数据库
}

- (id)initWithNSManagedObjectContext:(NSManagedObjectContext*) context;

- (id)newManagedObject;

- (NSString*)entityName;

@end


@interface IBESystemDAO : AbstractDAO

- (IBESystem*)getDefaultSystem;

- (NSArray*)listSystems;

@end


@interface IBEContactDAO : AbstractDAO

- (void)updateContact:(IBEContact*)contact;

- (IBEContact *)find:(NSString *)idStr;

- (IBEContact *)saveDefault:(NSString *)idStr;

- (NSArray *)listContacts;

@end


@interface IBEMessageDAO : AbstractDAO

- (IBEMessage*)findByID:(NSNumber*)msgID;

- (NSDate *)getLastReadMessageDate;

- (NSArray *)listMessages:(IBEContact *)contact;

- (void)removeAllMessages:(IBELatestMessage*)message;

@end


@interface IBEIdentityDescriptionDAO : AbstractDAO

- (NSArray*)listIdentityDescriptions;

- (IBEIdentityDescription*)getByIDString:(NSString*)idStr;

@end


@interface IBELatestMessageDAO : AbstractDAO

- (void)updateLatestMessage:(IBEMessage*)newMessage;

- (NSArray*)listLatestMessages;

- (IBELatestMessage*)getLatestMessage:(IBEContact*)contact;

@end
