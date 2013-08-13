//
//  ApplyIDUIViewController.m
//  ssms
//
//  Created by 烨 王 on 12-4-13.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import "ApplyIDUIViewController.h"
#import "IBEEntity.h"
#import "ibeerror.h"
#import "IBEAppDelegate.h"

@implementation ApplyIDUIViewController

@synthesize idStringTextField = _idStringTextField;
@synthesize accessPasswordTextField = _accessPasswordTextField;
@synthesize selectButton = _selectButton;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
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

- (void)viewDidLoad {
    [super viewDidLoad];
    IBEAppDelegate *appDelegate = [[UIApplication sharedApplication]delegate];
    context = [appDelegate managedObjectContext];
    systemDAO = [[IBESystemDAO alloc]initWithNSManagedObjectContext:context];
    iDescDAO = [[IBEIdentityDescriptionDAO alloc]initWithNSManagedObjectContext:context];
    systemList = [systemDAO listSystems];
    if (!settings) {
        settings = [IBEConfig getInstance];
    }
}

- (void)viewDidUnload
{
    [self setIdStringTextField:nil];
    [self setAccessPasswordTextField:nil];
    [self setSelectButton:nil];
    newID = nil;
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

// TODO 处理返回的数据
- (void) dataReceived:(NSData *) data {
    UInt8* res = (UInt8*) [data bytes];
    int result = res[0];
    UIAlertView* alert = nil;
    IBEIdentityDescription* idesc = nil;
    NSError* error;
    
    NSLog(@"result:%d\n", result);
    switch (result) {
        case ERR_SUCCESS:
            idesc = [iDescDAO newManagedObject];
            [idesc setId_str:newID];
            [idesc setId_data:nil];
            [idesc setId_status:[NSNumber numberWithInt:APPLICATION_STARTED]];

            [context save:&error];
            if (error) {
                alert = [[UIAlertView alloc]initWithTitle:@"错误" message:@"申请发送成功，但是无法在本地保存结果" delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil];
                break;
            }
            alert = [[UIAlertView alloc]initWithTitle:@"信息" message:@"申请发送成功，系统将在确认ID所有者身份后声称密钥" delegate:self cancelButtonTitle:@"确定" otherButtonTitles:nil];
            alert.tag = APPLICATION_STARTED;
            break;
        case ERR_WRONG_PWD:
            alert = [[UIAlertView alloc]initWithTitle:@"错误" message:@"你输入的电子邮件或者密码错误！" delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil];
            break;
        default:
            // UNKNOWN_ERROR
            break;
    }
    [alert show];
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    switch (alertView.tag) {
        case APPLICATION_STARTED:
            [self.navigationController popViewControllerAnimated:YES];
            break;     
        default:
            break;
    }
}

// TODO 处理错误
- (void) errorOccured:(NSError *) error {
    NSLog(@"%@", error);
}

- (IBAction)applyNewID:(id)sender {
    newID = _idStringTextField.text;
    NSString* accessPwd = _accessPasswordTextField.text;
    
    ApplyNewIDRequest* request = [[ApplyNewIDRequest alloc]initWithEmail:settings.email password:settings.password system:selectedSystem idString:newID accessPassword:accessPwd];
    IBEServerClientHTTPImpl* http = [[IBEServerClientHTTPImpl alloc]initWithURL:[settings getIBEServerURL] andRequest:request withHandler:self];
    [http startRequest];
}

- (IBAction)selectSystem:(id)sender {
    selectSheet = [[UIActionSheet alloc]initWithTitle:nil delegate:nil cancelButtonTitle:nil destructiveButtonTitle:nil otherButtonTitles:nil];
    [selectSheet setActionSheetStyle:UIActionSheetStyleBlackTranslucent];    
    selectPickerView = [[UIPickerView alloc] initWithFrame:CGRectMake(0, 40, 0, 0)];
    selectPickerView.showsSelectionIndicator = YES;
    selectPickerView.dataSource = self;
    selectPickerView.delegate = self;
    [selectSheet addSubview:selectPickerView];
    UISegmentedControl *closeButton = [[UISegmentedControl alloc]initWithItems:[NSArray arrayWithObject:@"完成"]];
    closeButton.momentary = YES;
    closeButton.frame = CGRectMake(260, 7.0f, 50.0f, 30.0f);
    closeButton.segmentedControlStyle = UISegmentedControlStyleBar;
    closeButton.tintColor = [UIColor blackColor];
    [closeButton addTarget:self action:@selector(dismissActionSheet:) forControlEvents:UIControlEventValueChanged];
    [selectSheet addSubview:closeButton];
    [selectSheet showInView:[[UIApplication sharedApplication] keyWindow]];
    [selectSheet setBounds:CGRectMake(0, 0, 320, 485)];
}

// 完成选择系统 保存选取的系统序号并更新显示
- (IBAction)dismissActionSheet:(id)sender {
    [selectSheet dismissWithClickedButtonIndex:0 animated:YES];
    NSUInteger sel = [selectPickerView selectedRowInComponent:0];
    IBESystem* selSys = [systemList objectAtIndex:sel];
    selectedSystem = [selSys.ibe_system_number intValue];
    [_selectButton setTitle:selSys.name forState:UIControlStateNormal];
}

#pragma mark - UIPickerViewDelegate

// 返回供用户选择的系统列表
- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component {
    IBESystem *system = [systemList objectAtIndex:row];
    return system.name;
}

#pragma mark - UIPickerViewDataSource

// 只有一列
- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView {
    return 1;
}

// 返回系统数量
- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component {
    return [systemList count];
}

- (BOOL)textFieldShouldReturn:(UITextField *)theTextField {
    if (theTextField == _idStringTextField || theTextField == _accessPasswordTextField) {
        [theTextField resignFirstResponder];
    }
    return YES;
}

@end
