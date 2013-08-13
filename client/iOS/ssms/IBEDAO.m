//
//  IBEDAO.m
//  ssms
//
//  Created by 烨 王 on 12-4-1.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import "IBEDAO.h"

@implementation AbstractDAO

- (id)initWithNSManagedObjectContext:(NSManagedObjectContext*) ctx {
    self = [super init];
    if (self) {
        context = ctx;
    }
    return self;
}

- (id)newManagedObject {
    NSString* name = [self entityName];
    if (!name) {
        return nil;
    }
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:name inManagedObjectContext:context];
    NSManagedObject *obj = [[NSManagedObject alloc]initWithEntity:entityDescription insertIntoManagedObjectContext:context];
    return obj;
}

- (NSString*)entityName {
    return nil;
}

@end

@implementation IBESystemDAO

- (NSString*)entityName {
    return @"IBESystem";
}

- (IBESystem*)getDefaultSystem {
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"IBESystem" inManagedObjectContext:context];
    NSFetchRequest *request = [[NSFetchRequest alloc]init];
    [request setEntity:entityDescription];
    [request setIncludesSubentities:FALSE];
    [request setFetchLimit:1];  // 取出第一个IBESystem
    NSError *error;
    NSArray *systems = [context executeFetchRequest:request error:&error];
    if (error) {
        @throw error;
    }
    if ([systems count] == 0) {
        return nil;
    }
    return [systems objectAtIndex:0];
}

- (NSArray*)listSystems {
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"IBESystem" inManagedObjectContext:context];
    NSFetchRequest *request = [[NSFetchRequest alloc]init];
    [request setEntity:entityDescription];
    [request setIncludesSubentities:FALSE];
    NSError *error;
    NSArray *systems = [context executeFetchRequest:request error:&error];
    if (error) {
        @throw error;
    }
    if ([systems count] == 0) {
        return nil;
    }
    return systems;
}

@end

@implementation IBEContactDAO

- (void)updateContact:(IBEContact *)contact {
    
}

- (NSArray *)listContacts {
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"IBEContact" inManagedObjectContext:context];
    NSFetchRequest *request = [[NSFetchRequest alloc]init];
    [request setEntity:entityDescription];
    NSError *error;
    NSArray *ids = [context executeFetchRequest:request error:&error];
    if (error) {
        @throw error;
    }
    return ids;
}

- (IBEContact *)find:(NSString *)idStr {
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"IBEContact" inManagedObjectContext:context];
    NSFetchRequest *request = [[NSFetchRequest alloc]init];
    [request setEntity:entityDescription];
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"default_id=%@", idStr];
    [request setPredicate:predicate];
    [request setFetchLimit:1];
    NSError *error;
    NSArray *ids = [context executeFetchRequest:request error:&error];
    if (error) {
        @throw error;
    }
    if ([ids count] == 0) {
        return nil;
    }
    return [ids objectAtIndex:0];
}

- (IBEContact *)saveDefault:(NSString *)idStr {
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"IBEContact" inManagedObjectContext:context];
    IBEContact *contact = [[IBEContact alloc]initWithEntity:entityDescription insertIntoManagedObjectContext:context];
    [contact setDefault_id:idStr];
    [contact setFirst_name:nil];
    [contact setFamily_name:nil];
    
    IBESystemDAO* sysDAO = [[IBESystemDAO alloc]initWithNSManagedObjectContext:context];
    IBESystem *sys = [sysDAO getDefaultSystem];
    [contact setSystem:sys];

    NSError *error;
    [context save:&error];
    if (error) {
        @throw error;
    }
    return contact;
}

- (NSString*)entityName {
    return @"IBEContact";
}

@end


@implementation IBEIdentityDescriptionDAO

- (NSArray*)listIdentityDescriptions {
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"IBEIdentityDescription" inManagedObjectContext:context];
    NSFetchRequest *request = [[NSFetchRequest alloc]init];
    [request setEntity:entityDescription];
    NSError *error;
    NSArray *ids = [context executeFetchRequest:request error:&error];
    if (error) {
        @throw error;
    }
    return ids;
}

- (IBEIdentityDescription*)getByIDString:(NSString*)idStr {
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"IBEIdentityDescription" inManagedObjectContext:context];
    NSFetchRequest *request = [[NSFetchRequest alloc]init];
    [request setEntity:entityDescription];
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"id_str=%@", idStr];
    [request setPredicate:predicate];
    [request setFetchLimit:1];
    NSError *error;
    NSArray *ids = [context executeFetchRequest:request error:&error];
    if (error) {
        @throw error;
    }
    if ([ids count] > 0) {
        return [ids objectAtIndex:0];
    }
    return nil;
}

- (NSString*)entityName {
    return @"IBEIdentityDescription";
}

@end


@implementation IBELatestMessageDAO

- (void)updateLatestMessage:(IBEMessage*)newMessage {
    NSString *contact = newMessage.from.default_id;
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"IBELatestMessage" inManagedObjectContext:context];
    NSFetchRequest *request = [[NSFetchRequest alloc]init];
    [request setEntity:entityDescription];
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"contact=%@", contact];
    [request setPredicate:predicate];
    [request setFetchLimit:1];
    NSError *error;
    NSArray *msgs = [context executeFetchRequest:request error:&error];
    if (error) {
        @throw error;
    }
    IBELatestMessage *msg;
    if ([msgs count] > 0) {
        // 更新最近信息以供显示
        msg = [msgs objectAtIndex:0];
    } else {
        // 同该联系人第一次通信
        msg = [self newManagedObject];
        [msg setContact:contact];
    }
    [msg setMessage_content:newMessage.content];
    [msg setMessage_date:newMessage.receive_date];
    if ([newMessage.type intValue] == MESSAGE_RECEIVED) {
        [msg setMessage_read:[NSNumber numberWithInt:MESSAGE_UNREAD]];
    } else {
        [msg setMessage_read:[NSNumber numberWithInt:MESSAGE_READ]];
    }
    [context save:nil];
}

- (NSArray*)listLatestMessages {
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"IBELatestMessage" inManagedObjectContext:context];
    NSFetchRequest *request = [[NSFetchRequest alloc]init];
    [request setEntity:entityDescription];
    NSError *error;
    NSArray *msgs = [context executeFetchRequest:request error:&error];
    if (error) {
        @throw error;
    }
    return msgs;
}

- (IBELatestMessage*)getLatestMessage:(IBEContact*)contact {
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"IBELatestMessage" inManagedObjectContext:context];
    NSFetchRequest *request = [[NSFetchRequest alloc]init];
    [request setEntity:entityDescription];
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"contact=%@", contact.default_id];
    [request setPredicate:predicate];
    [request setFetchLimit:1];
    NSError *error;
    NSArray *msgs = [context executeFetchRequest:request error:&error];
    if (error) {
        @throw error;
    }
    if (msgs && [msgs count] == 1) {
        return [msgs objectAtIndex:0];
    }
    return nil;
}

- (NSString*)entityName {
    return @"IBELatestMessage";
}

@end


@implementation IBEMessageDAO

- (void)removeAllMessages:(IBELatestMessage*)message {
    IBEContactDAO *cdao = [[IBEContactDAO alloc]initWithNSManagedObjectContext:context];
    IBEContact *contact = [cdao find:message.contact];
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"IBEMessage" inManagedObjectContext:context];
    NSFetchRequest *request = [[NSFetchRequest alloc]init];
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"from=%@", contact];
    [request setPredicate:predicate];
    [request setEntity:entityDescription];
    NSError *error;
    NSArray *msgs = [context executeFetchRequest:request error:&error];
    if (error) {
        @throw error;
    }
    for (IBEMessage *msg in msgs) {
        [context deleteObject:msg];
    }
    [context deleteObject:message];
    [context deleteObject:contact];
    [context save:&error];
    if (error) {
        @throw error;
    }
}

- (IBEMessage*)findByID:(NSNumber*)msgID {
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"IBEMessage" inManagedObjectContext:context];
    NSFetchRequest *request = [[NSFetchRequest alloc]init];
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"message_id=%@", msgID];
    [request setPredicate:predicate];
    [request setEntity:entityDescription];
    [request setFetchLimit:1];
    NSError *error;
    NSArray *msgs = [context executeFetchRequest:request error:&error];
    if (error) {
        @throw error;
    }
    return [msgs count] == 0 ? nil : [msgs objectAtIndex:0];
}

- (NSDate *)getLastReadMessageDate {
    // TODO 从数据库中读取最新消息的日期
    /*
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"IBEMessage" inManagedObjectContext:context];
    NSFetchRequest *request = [[NSFetchRequest alloc]init];
    [request setEntity:entityDescription];
    //NSPredicate *predicate = [NSPredicate predicateWithFormat:@"from=%@", nil];
    //[request setPredicate:predicate];
    NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc]initWithKey:@"receive_date" ascending:NO];
    NSArray *sortDescriptors = [[NSArray alloc] initWithObjects:sortDescriptor, nil];
    [request setSortDescriptors:sortDescriptors];
    [request setFetchLimit:1];
    NSError *error;
    NSArray *dates = [context executeFetchRequest:request error:&error];
    if (error) {
        @throw error;
    }
    if ([dates count] == 1) {
        return [dates objectAtIndex:0];
    }*/
    NSDate *fuck = [NSDate dateWithTimeIntervalSince1970:10];
    return fuck;
}

- (NSArray *)listMessages:(IBEContact *)contact {
    NSEntityDescription *entityDescription = [NSEntityDescription entityForName:@"IBEMessage" inManagedObjectContext:context];
    NSFetchRequest *request = [[NSFetchRequest alloc]init];
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"from=%@", contact];
    [request setPredicate:predicate];
    [request setEntity:entityDescription];
    NSError *error;
    NSArray *msgs = [context executeFetchRequest:request error:&error];
    if (error) {
        @throw error;
    }
    return msgs;
}

- (NSString*)entityName {
    return @"IBEMessage";
}

@end