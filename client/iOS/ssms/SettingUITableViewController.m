//
//  SettingUITableViewController.m
//  ssms
//
//  Created by 烨 王 on 12-3-31.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import "SettingUITableViewController.h"
#import "IBEAppDelegate.h"
#import "ibeerror.h"
#import "ibecommon.h"

@implementation SettingUITableViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
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

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
}

- (void)viewDidUnload
{
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
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    //#warning Potentially incomplete method implementation.
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    //#warning Incomplete method implementation.
    // Return the number of rows in the section.
    return 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"settings_item";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;

    UILabel* idmgmt = cell.textLabel;
    idmgmt.font = [UIFont boldSystemFontOfSize:18];
    idmgmt.text = @"身份描述管理";
    return cell;
}

/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
    }   
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if ([indexPath indexAtPosition:0] == 0 && [indexPath indexAtPosition:1] == 0) {
        [self performSegueWithIdentifier:@"do_id_mgmt" sender:self];
        /*
        UIStoryboard* storyBoard = [UIStoryboard storyboardWithName:@"MainStoryboard" bundle:nil];
        IDManagementUITableViewController* nextController = [storyBoard instantiateViewControllerWithIdentifier:@"id_mgmt"];
        [self.navigationController pushViewController:nextController animated:YES];
        */
        return;
    }
}

@end

///////////////////////////////////////////////////////////////////////////////////////////////////

@implementation IDManagementUITableViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
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

// 开启一个新的线程 后台访问服务器
// 获取身份描述请求的处理结果
- (void)checkIDsBackground:(id)param {
    IBEConfig *settings = [IBEConfig getInstance];
    CheckIDRequest *request = [[CheckIDRequest alloc]initWithEmail:settings.email andPassword:settings.password page:0 amount:10 withStatus:APPLICATION_STATUS_ALL];
    IBEServerClientHTTPImpl* http = [[IBEServerClientHTTPImpl alloc]initWithURL:[settings getIBEServerURL] andRequest:request withHandler:self];
    [http startRequest];
}

- (void) dataReceived:(NSData *) data {
    NSLog(@"%@", data);
    unsigned char* pointer = (unsigned char*) [data bytes];
    switch (pointer[0]) {
        case ERR_SUCCESS:
            pointer++;
            break;
        case ERR_WRONG_PWD:
            NSLog(@"Wrong email/password.");
            return;
            // TODO other cases.
        default:
            NSLog(@"Unknown error.");
            return;
    }
    NSUInteger amount = pointer[0];
    pointer++;
    IBEAppDelegate *appDelegate = [[UIApplication sharedApplication]delegate];
    NSManagedObjectContext *context = [appDelegate managedObjectContext];
    for (NSUInteger i = 0; i < amount; i++) {
        NSEntityDescription *desc = [NSEntityDescription entityForName:@"IBEIdentityDescription" inManagedObjectContext:context];
        int descNo = bytes2int(pointer);
        pointer += 4;
        NSString* idStr = [[NSString alloc]initWithBytes:pointer + 1 length:pointer[0] encoding:NSUTF8StringEncoding];
        pointer += pointer[0];
        pointer++;
        int ibeSysNo = bytes2int(pointer);
        pointer += 4;
        IBE_LONG appDate = bytes2long(pointer);
        pointer += 8;
        int currStatus = pointer[0];
        pointer++;

        NSError *error;
        IBEIdentityDescription *exist = [idDAO getByIDString:idStr];
        if (!exist) {
            exist = [[IBEIdentityDescription alloc]initWithEntity:desc insertIntoManagedObjectContext:context];
            [exist setId_str:idStr];
            [exist setId_status:[NSNumber numberWithInt:currStatus]];
            [exist setId_data:nil];
            [context save:&error];
            if (error) {
                NSLog(@"error occured:%@", error);
                break;
            }
            continue;
        }

        NSLog(@"id:%@ ,status:%d", exist.id_str, [exist.id_status intValue]);

        if ([exist.id_status intValue] == APPLICATION_APPROVED) {
            continue;
        }
        [exist setId_status:[NSNumber numberWithInt:currStatus]];
        [context save:&error];
        if (error) {
            NSLog(@"error occured:%@", error);
            break;
        }
        // TODO 根据ID信息查找本地存储并更新数据
        NSLog(@"id No.:%d", descNo);
        NSLog(@"id:%@", idStr);
        NSLog(@"sys No.:%d", ibeSysNo);
        NSLog(@"app Date:%lld", appDate);
        NSLog(@"status:%d\n", currStatus);
    }
}

- (void) errorOccured:(NSError *) error {
    NSLog(@"%@", error);
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    [self checkIDsBackground:nil];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    if (ids) {
        [ids removeAllObjects];
        ids = nil;        
    }
    idDAO = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    IBEAppDelegate *appDelegate = [[UIApplication sharedApplication]delegate];
    NSManagedObjectContext *context = [appDelegate managedObjectContext];
    idDAO = [[IBEIdentityDescriptionDAO alloc]initWithNSManagedObjectContext:context];
    NSArray* idArray = [idDAO listIdentityDescriptions];
    ids = [[NSMutableArray alloc]init];
    [ids addObjectsFromArray:idArray];
    [self.tableView reloadData];
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
    [ids removeAllObjects];
    //ids = nil;
    //idDAO = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 1 + [ids count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"id_mgmt_cell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }
    UILabel* disp = cell.textLabel;
    disp.font = [UIFont boldSystemFontOfSize:18];
    if ([indexPath indexAtPosition:0] == 0 && [indexPath indexAtPosition:1] == [ids count]) {
        cell.accessoryType = UITableViewCellAccessoryNone;
        disp.text = @"申请新的身份描述...";
    } else {
        cell.accessoryType = UITableViewCellAccessoryDetailDisclosureButton;
        NSUInteger index = [indexPath indexAtPosition:1];
        IBEIdentityDescription* desc = [ids objectAtIndex:index];
        disp.text = [desc id_str];
    }
    return cell;
}

/*
 // Override to support conditional editing of the table view.
 - (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
 {
 // Return NO if you do not want the specified item to be editable.
 return YES;
 }
 */

/*
 // Override to support editing the table view.
 - (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
 {
 if (editingStyle == UITableViewCellEditingStyleDelete) {
 // Delete the row from the data source
 [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
 }   
 else if (editingStyle == UITableViewCellEditingStyleInsert) {
 // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
 }   
 }
 */

/*
 // Override to support rearranging the table view.
 - (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
 {
 }
 */

/*
 // Override to support conditional rearranging of the table view.
 - (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
 {
 // Return NO if you do not want the item to be re-orderable.
 return YES;
 }
 */

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView accessoryButtonTappedForRowWithIndexPath:(NSIndexPath *)indexPath {
    [self tableView:tableView didSelectRowAtIndexPath:indexPath];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if ([indexPath indexAtPosition:0] == 0 && [indexPath indexAtPosition:1] == [ids count]) {
        [self performSegueWithIdentifier:@"apply_new_id" sender:self];
        return;
    }
    IBEIdentityDescription* desc = [ids objectAtIndex:[indexPath indexAtPosition:1]];
    detailToShow = [desc id_str];
    [self performSegueWithIdentifier:@"show_id_detail" sender:self];
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    if ([[segue identifier] isEqualToString:@"show_id_detail"]) {
        IDDetailUIViewController *detailController = [segue destinationViewController];
        [detailController setDispId:detailToShow];
    }
}

@end


@implementation IDDetailUIViewController

@synthesize idString = _idString;
@synthesize systemString = _systemString;
@synthesize fromOrStatus = _fromOrStatus;
@synthesize endOrNil = _endOrNil;
@synthesize downIDButton = _downIDButton;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
    }
    return self;
}

- (IBAction)downloadID:(id)sender {
    UIAlertView *askPwd = [[UIAlertView alloc]initWithTitle:@"信息" message:@"请输入身份信息访问密码" delegate:self cancelButtonTitle:@"取消" otherButtonTitles:@"确定", nil];
    askPwd.alertViewStyle = UIAlertViewStyleSecureTextInput;
    [askPwd show];
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    if (buttonIndex == 0) {
        NSLog(@"btn:%d", buttonIndex);
        return;
    }
    IBEConfig *settings = [IBEConfig getInstance];
    NSString* accPwd = [alertView textFieldAtIndex:0].text;

    DownloadIDRequest *request = [[DownloadIDRequest alloc]initWithEmail:settings.email andPassword:settings.password forID:idToDisp withAccessPassword:accPwd];
    IBEServerClientHTTPImpl* http = [[IBEServerClientHTTPImpl alloc]initWithURL:[settings getIBEServerURL] andRequest:request withHandler:self];
    [http startRequest];
}

- (void) dataReceived:(NSData *) data {
    NSLog(@"data:%@", data);
    unsigned char* pointer = (unsigned char*) [data bytes];
    NSUInteger result = pointer[0];
    if (result != ERR_SUCCESS) {
        // TODO handle error
        NSLog(@"result:%d", result);
        return;
    }
    pointer++;

    int len = [data length] - 1;
    printf("ID content:\n");
    for (int i = 0; i < len; i++) {
        printf("%02x", pointer[i]);
    }
    printf("\n");

    NSError *error;
    IBEAppDelegate *appDelegate = [[UIApplication sharedApplication]delegate];
    NSManagedObjectContext *context = [appDelegate managedObjectContext];
    IBEIdentityDescription* desc = [idDAO getByIDString:idToDisp];
    [desc setId_data:[NSData dataWithBytes:pointer length:[data length] - 1]];
    [desc setId_status:[NSNumber numberWithInt:APPLICATION_APPROVED]];
    [context save:&error];
    if (error) {
        // TODO handle error
        NSLog(@"error:%@", error);
        return;
    }
    UIAlertView *alert = [[UIAlertView alloc]initWithTitle:@"信息" message:@"下载成功" delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil];
    [alert show];
}

- (void) errorOccured:(NSError *) error {
    NSLog(@"error:%@", error);
}

- (void)setDispId:(NSString*)str {
    idToDisp = str;
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
 - (void)viewDidLoad {
 [super viewDidLoad];
 }
 */

- (void)viewWillAppear:(BOOL)animated {
    if (idToDisp) {
        _idString.text = idToDisp;
        // TODO 显示ID详细信息
        IBEAppDelegate *appDelegate = [[UIApplication sharedApplication]delegate];
        NSManagedObjectContext *context = [appDelegate managedObjectContext];
        idDAO = [[IBEIdentityDescriptionDAO alloc]initWithNSManagedObjectContext:context];
        IBEIdentityDescription* iDesc = [idDAO getByIDString:idToDisp];
        if (iDesc) {
            int status = [iDesc.id_status intValue];
            switch (status) {
                case APPLICATION_STARTED:
                    _fromOrStatus.text = @"开始申请";
                    break;
                case APPLICATION_PROCESSING:
                    _fromOrStatus.text = @"申请处理中";
                    break;
                case APPLICATION_APPROVED:
                    break;
                case APPLICATION_DENIED:
                    _fromOrStatus.text = @"申请被驳回";
                    break;
                case APPLICATION_ERROR:
                    _fromOrStatus.text = @"处理错误";
                    break;
                case APPLICATION_NOT_VERIFIED:
                    _fromOrStatus.text = @"尚未验证身份";
                    break;
                default:
                    _fromOrStatus.text = @"未知错误";
            }
            if (status == APPLICATION_APPROVED) {
                NSData* data = iDesc.id_data;
                if (!data) {
                    _fromOrStatus.text = @"申请成功";
                    _downIDButton.hidden = NO;
                } else {
                    // 显示其它数据
                    unsigned char* pointer = (unsigned char*) [data bytes];
                    pointer += 384;
                    int len = bytes2int(pointer);
                    pointer += 4;
                    pointer += len;
                    pointer += 20;
                    pointer += 128;
                    pointer += 384;
                    len = bytes2int(pointer);
                    pointer += 4;
                    pointer += len;
                    pointer += 20;

                    IBE_LONG start = bytes2long(pointer);
                    pointer += 8;
                    IBE_LONG interval = bytes2long(pointer);
                    NSDate *startDate = [NSDate dateWithTimeIntervalSince1970:start / 1000];
                    NSDate *endDate = [NSDate dateWithTimeIntervalSince1970:(start + interval) / 1000];
                    NSDateFormatter *formatter = [[NSDateFormatter alloc]init];
                    [formatter setDateFormat:@"yyyy-MM-dd HH:mm"];
                    NSLocale* locale = [[NSLocale alloc]initWithLocaleIdentifier:@"zh_CN"];
                    [formatter setLocale:locale];
                    _fromOrStatus.text = [NSString stringWithFormat:@"生效日期：%@", [formatter stringFromDate:startDate]];
                    _endOrNil.text = [NSString stringWithFormat:@"失效日期：%@", [formatter stringFromDate:endDate]];
                }
            }
            return;
        }
    }
    _idString.text = @"error";
}

- (void)viewDidUnload
{
    [self setIdString:nil];
    [self setSystemString:nil];
    [self setFromOrStatus:nil];
    [self setEndOrNil:nil];
    [self setIdString:nil];
    [self setDownIDButton:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}
@end
