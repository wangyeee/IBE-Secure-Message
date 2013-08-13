//
//  NewMessageUIViewController.h
//  ssms
//
//  Created by 烨 王 on 12-3-31.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IBEServerClient.h"
#import "IBEDAO.h"
#import "IBEEntity.h"

@interface NewMessageUIViewController : UIViewController <IBEResponseHandler> {
@private
    IBEMessageDAO *messageDAO;
    IBEContactDAO *contactDAO;
    IBEMessage *draft;
    IBELatestMessageDAO *latestMessageDAO;
}
@property (strong, nonatomic) IBOutlet UITextField *receiptTextField;
@property (strong, nonatomic) IBOutlet UITextField *mesageTextField;

- (IBAction)selectFromContacts:(id)sender;
- (IBAction)sendNewMessage:(id)sender;
- (IBAction)discardNewMessage:(id)sender;

@end
