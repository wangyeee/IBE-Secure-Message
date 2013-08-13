//
//  SettingUITableViewController.h
//  ssms
//
//  Created by 烨 王 on 12-3-31.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IBEDAO.h"
#import "IBEServerClient.h"

@interface SettingUITableViewController : UITableViewController

@end


@interface IDManagementUITableViewController : UITableViewController <IBEResponseHandler> {
@private
    NSMutableArray* ids;
    IBEIdentityDescriptionDAO* idDAO;

    NSString *detailToShow;
}

@end


@interface IDDetailUIViewController : UIViewController <UIAlertViewDelegate, IBEResponseHandler> {
    @private
    IBEIdentityDescriptionDAO* idDAO;
    NSString* idToDisp;
}

@property (strong, nonatomic) IBOutlet UILabel *idString;
@property (strong, nonatomic) IBOutlet UILabel *systemString;
@property (strong, nonatomic) IBOutlet UILabel *fromOrStatus;
@property (strong, nonatomic) IBOutlet UILabel *endOrNil;
@property (strong, nonatomic) IBOutlet UIButton *downIDButton;

- (IBAction)downloadID:(id)sender;

- (void)setDispId:(NSString*)str;

@end
