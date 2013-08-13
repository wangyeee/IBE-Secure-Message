//
//  IBEEntity.h
//  ssms
//
//  Created by 烨 王 on 12-4-3.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

#define APPLICATION_STATUS_ALL 0
#define APPLICATION_STARTED 1
#define APPLICATION_APPROVED 2
#define APPLICATION_DENIED 3
#define APPLICATION_ERROR 4
#define APPLICATION_NOT_VERIFIED 5
#define APPLICATION_PROCESSING 6


@class IBESystem;

@interface IBEContact : NSManagedObject

@property (nonatomic, retain) NSString *default_id;
@property (nonatomic, retain) NSString *family_name;
@property (nonatomic, retain) NSString *first_name;
@property (nonatomic, retain) IBESystem *system;
@property (nonatomic, retain) NSSet *sent_messages;

@end

@interface IBEContact (CoreDataGeneratedAccessors)

- (void)addSent_messagesObject:(NSManagedObject *)value;
- (void)removeSent_messagesObject:(NSManagedObject *)value;
- (void)addSent_messages:(NSSet *)values;
- (void)removeSent_messages:(NSSet *)values;

@end

#define MESSAGE_DRAFT 2
#define MESSAGE_SENT 1
#define MESSAGE_RECEIVED 0
@interface IBEMessage : NSManagedObject

@property (nonatomic, retain) NSString * content;
@property (nonatomic, retain) NSDate * receive_date;
@property (nonatomic, retain) IBEContact *from;
@property (nonatomic, retain) NSNumber *type;
@property (nonatomic, retain) NSNumber *message_id;

@end


@interface IBEIdentityDescription : NSManagedObject

@property (nonatomic, retain) NSData * id_data;
@property (nonatomic, retain) NSString * id_str;
@property (nonatomic, retain) NSNumber * id_status;

@end


@interface IBESystem : NSManagedObject

@property (nonatomic, retain) NSString *name;
@property (nonatomic, retain) NSData *pairing;
@property (nonatomic, retain) NSData *public_params;
@property (nonatomic, retain) NSNumber *ibe_system_number;
@property (nonatomic, retain) NSSet *contacts;

@end

@interface IBESystem (CoreDataGeneratedAccessors)

- (void)addContactsObject:(IBEContact *)value;
- (void)removeContactsObject:(IBEContact *)value;
- (void)addContacts:(NSSet *)values;
- (void)removeContacts:(NSSet *)values;

@end


#define MESSAGE_UNREAD 0
#define MESSAGE_READ 1
@interface IBELatestMessage : NSManagedObject

@property (nonatomic, retain) NSString * message_content;
@property (nonatomic, retain) NSString * contact;
@property (nonatomic, retain) NSDate * message_date;
@property (nonatomic, retain) NSNumber * message_read;

@end
