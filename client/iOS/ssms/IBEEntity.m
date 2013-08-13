//
//  IBEEntity.m
//  ssms
//
//  Created by 烨 王 on 12-4-3.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import "IBEEntity.h"

@implementation IBEContact

@dynamic default_id;
@dynamic family_name;
@dynamic first_name;
@dynamic system;
@dynamic sent_messages;

@end


@implementation IBEIdentityDescription

@dynamic id_data;
@dynamic id_str;
@dynamic id_status;

@end


@implementation IBESystem

@dynamic name;
@dynamic pairing;
@dynamic public_params;
@dynamic ibe_system_number;
@dynamic contacts;

@end


@implementation IBEMessage

@dynamic content;
@dynamic receive_date;
@dynamic from;
@dynamic type;
@dynamic message_id;

@end


@implementation IBELatestMessage

@dynamic message_content;
@dynamic contact;
@dynamic message_date;
@dynamic message_read;

@end
