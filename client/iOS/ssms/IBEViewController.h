//
//  IBEViewController.h
//  ssms
//
//  Created by 烨 王 on 12-2-22.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IBEServerClient.h"

@interface IBEViewController : UIViewController <IBEResponseHandler, UITextFieldDelegate> {
@private
    IBEConfig* settings;
    NSString* email;
    NSString* password;
}

@property (weak, nonatomic) IBOutlet UITextField *emailTextField;
@property (weak, nonatomic) IBOutlet UITextField *passwordTextField;

- (IBAction)login:(id)sender;

@end
