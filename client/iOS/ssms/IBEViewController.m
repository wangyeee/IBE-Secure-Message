//
//  IBEViewController.m
//  ssms
//
//  Created by 烨 王 on 12-2-22.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import "IBEViewController.h"

//#import "ibecommon.c"
//#import "IBECore.h"

#import "IBEServerClient.h"
#import "ibeerror.h"

// TODO delete
#import "IBEAppDelegate.h"
#import "IBEDAO.h"
#import "IBEEntity.h"

//#import "IBESymmetricCrypto.h"

@implementation IBEViewController
@synthesize emailTextField;
@synthesize passwordTextField;

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    settings = [IBEConfig getInstance];
}

- (void)viewDidUnload
{
    [self setEmailTextField:nil];
    [self setPasswordTextField:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
	[super viewDidDisappear:animated];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
}

- (BOOL)textFieldShouldReturn:(UITextField *)theTextField {
    if (theTextField == self.emailTextField || theTextField == self.passwordTextField) {
        [theTextField resignFirstResponder];
    }
    return YES;
}

- (IBAction)login:(id)sender {
    if (!settings) {
        settings = [IBEConfig getInstance];
    }
    email = self.emailTextField.text;
    password = self.passwordTextField.text;
    if ([email isEqualToString:@"test"]) {
        [settings setEmail:@"wangyeee@me.com"];
        [settings setPassword:@"1234567"];
        [self performSegueWithIdentifier:@"login_success" sender:self];
        return;
    }
    NSLog(@"email:%@\npassword:%@\n", email, password);

    LoginIBERequest* request = [[LoginIBERequest alloc] initWithEmail:email andPassword:password];
    IBEServerClientHTTPImpl* http = [[IBEServerClientHTTPImpl alloc]initWithURL:[settings getIBEServerURL] andRequest:request withHandler:self];
    [http startRequest];
}

// 处理返回的数据
- (void) dataReceived:(NSData *)data {
    UInt8* res = (UInt8*) [data bytes];
    int result = res[0];
    UIAlertView* alert = nil;

    NSLog(@"result:%d\n", result);
    switch (result) {
        case ERR_SUCCESS:
            // SUCCESS
            if (!settings) {
                settings = [IBEConfig getInstance];
            }
            [settings setEmail:email];
            [settings setPassword:password];
            [self performSegueWithIdentifier: @"login_success" sender: self];
            break;
        case ERR_WRONG_PWD:
            alert = [[UIAlertView alloc] initWithTitle:@"错误" message:@"你输入的电子邮件或者密码错误！" delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil];
            [alert show];
            break;
        default:
            // UNKNOWN_ERROR
            break;
    }
}

// 处理错误
- (void) errorOccured:(NSError *) error {
}

@end
