//
//  ApplyIDUIViewController.h
//  ssms
//
//  Created by 烨 王 on 12-4-13.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IBEDAO.h"
#import "IBECore.h"
#import "IBEServerClient.h"

@interface ApplyIDUIViewController : UIViewController <UIPickerViewDelegate, UIPickerViewDataSource, UITextFieldDelegate, IBEResponseHandler> {
@private
    UIActionSheet *selectSheet;
    UIPickerView *selectPickerView;

    NSManagedObjectContext *context;
    IBESystemDAO* systemDAO;
    IBEIdentityDescriptionDAO* iDescDAO;
    NSArray* systemList;

    NSString* newID;
    NSInteger selectedSystem;

    IBEConfig* settings;
}

@property (strong, nonatomic) IBOutlet UITextField *idStringTextField;
@property (strong, nonatomic) IBOutlet UITextField *accessPasswordTextField;
@property (strong, nonatomic) IBOutlet UIButton *selectButton;

- (IBAction)applyNewID:(id)sender;
- (IBAction)selectSystem:(id)sender;

@end
