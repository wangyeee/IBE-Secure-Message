//
//  RegisterViewController.h
//  ssms
//
//  Created by 烨 王 on 12-3-29.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IBEServerClient.h"

@interface RegisterViewController : UIViewController <IBEResponseHandler, UITextFieldDelegate, UIAlertViewDelegate> {
@private
    IBEConfig* settings;
}

@property (strong, nonatomic) IBOutlet UITextField *userTextField;
@property (strong, nonatomic) IBOutlet UITextField *emailTextField;
@property (strong, nonatomic) IBOutlet UITextField *passwordTextField;
@property (strong, nonatomic) IBOutlet UITextField *confirmTextField;

// 处理用户注册事件
- (IBAction)doRegister:(id)sender;

@end
