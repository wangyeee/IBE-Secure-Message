//
//  RegisterViewController.m
//  ssms
//
//  Created by 烨 王 on 12-3-29.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import "RegisterViewController.h"
#import "ibeerror.h"

@implementation RegisterViewController

@synthesize userTextField = _userTextField;
@synthesize emailTextField = _emailTextField;
@synthesize passwordTextField = _passwordTextField;
@synthesize confirmTextField = _confirmTextField;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
    }
    return self;
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

/*
// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView
{
}
*/

/*
// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad
{
    [super viewDidLoad];
}
*/

- (void)viewDidUnload
{
    [self setUserTextField:nil];
    [self setEmailTextField:nil];
    [self setPasswordTextField:nil];
    [self setConfirmTextField:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

// 让虚拟键盘在输入完成后消失
- (BOOL)textFieldShouldReturn:(UITextField *)theTextField {
    if (theTextField == _userTextField ||
        theTextField == _emailTextField ||
        theTextField == _passwordTextField ||
        theTextField == _confirmTextField) {
        [theTextField resignFirstResponder];
    }
    return YES;
}

- (IBAction)doRegister:(id)sender {
    NSString* username = _userTextField.text;
    NSString* email = _emailTextField.text;
    NSString* password = _passwordTextField.text;
    NSString* confirmPassword = _confirmTextField.text;

    // TODO 验证各个字段合法性
    NSLog(@"User Registeration:\nusername:%@\nemail:%@\npassword:%@\nconfirm password:%@\n",
          username, email, password, confirmPassword);

    if (!settings) {
        settings = [IBEConfig getInstance];
    }
    RegisterIBERequest* request = [[RegisterIBERequest alloc]initWithUsername:username email:email andPassword:password];
    IBEServerClientHTTPImpl* http = [[IBEServerClientHTTPImpl alloc]initWithURL:[settings getIBEServerURL] andRequest:request withHandler:self];
    // TODO 开启一个新线程运行
    [http startRequest];
}

- (void) dataReceived:(NSData *) data {
    UInt8* res = (UInt8*) [data bytes];
    int result = res[0];
    UIAlertView* alert = nil;
    
    NSLog(@"result:%d\n", result);
    switch (result) {
        case ERR_SUCCESS:
            alert = [[UIAlertView alloc]initWithTitle:@"注册完成" message:@"注册信息已经提交，请登录你的邮箱查收激活邮件。" delegate:self cancelButtonTitle:@"确定" otherButtonTitles:nil];
            break;
        case ERR_EMAIL_USED:
            alert = [[UIAlertView alloc]initWithTitle:@"错误" message:@"你使用的电子邮件地址已被占用，请更换电子邮件地址。" delegate:self cancelButtonTitle:@"确定" otherButtonTitles:nil];
            break;
        // TODO 处理其它返回值
        default:
            alert = [[UIAlertView alloc]initWithTitle:@"错误" message:@"发生未知错误，请稍后重试。" delegate:self cancelButtonTitle:@"确定" otherButtonTitles:nil];
            break;
    }
    alert.tag = result;
    [alert show];
}

- (void) errorOccured:(NSError *) error {
}

// 处理对话框点击事件
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    switch (alertView.tag) {
        case ERR_SUCCESS:
            [self performSegueWithIdentifier: @"back_login" sender: self];
            break;            
        default:
            break;
    }
}

@end
