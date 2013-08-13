//
//  MainUITableViewController.h
//  ssms
//
//  Created by 烨 王 on 12-3-31.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IBEDaemon.h"
#import "IBEDAO.h"

@interface MainUITableViewController : UITableViewController <IBEMessageConsumer, UIAlertViewDelegate> {
@private
    IBEMessageDaemon *ibemsgd;
    IBELatestMessageDAO *latestMessageDAO;
    IBEMessageDAO *messageDAO;
    NSMutableArray *messages;
    BOOL firstLoad;

    // 删除时保存临时变量
    UITableView *deleteView;
    NSIndexPath *deletePath;
}

@property (strong, nonatomic) IBOutlet UINavigationItem *messageBarItem;

@end

@interface ConversationDetailViewController : UIViewController <UITextFieldDelegate, IBEMessageConsumer, UITableViewDelegate, UITableViewDataSource, UIAlertViewDelegate> {
@private
    IBEMessageDAO *messageDAO;
    IBELatestMessageDAO *latestMessageDAO;
    IBEContact *ibeContact;
    NSMutableArray *messages;
    IBEMessage *draft;
    IBEMessageDaemon *ibemsgd;
    BOOL isMessageEditingMode;
    NSMutableArray *selectedMessages;   // 保存每一行是否被选择YES为是，默认NO
}

@property (strong, nonatomic) IBOutlet UIBarButtonItem *messageEditButton;
@property (retain) NSString *contact;

@property (strong, nonatomic) IBOutlet UINavigationItem *titleNavigationItem;
@property (strong, nonatomic) IBOutlet UITextField *editNewMessageTextField;
@property (retain, nonatomic) IBOutlet UITableView *tableView;
//@property (strong, nonatomic) IBOutlet UITableView *tableView;

- (IBAction)editMessages:(id)sender;
- (IBAction)sendMesage:(id)sender;
- (void)setViewMovedUp:(BOOL)movedUp;
@end

@interface IBEMessageCell : NSObject

+ (void)setAddresser:(NSString*) addresser andLastMessage:(NSString*) message onLastDate:(NSDate*) date isRead:(NSInteger)read forCell:(UITableViewCell*) cell;

@end
