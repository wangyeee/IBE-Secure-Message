//
//  MainUITableViewController.m
//  ssms
//
//  Created by 烨 王 on 12-3-31.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import "MainUITableViewController.h"
#import "IBEAppDelegate.h"
#import "IBEEntity.h"
#import "ibecommon.h"
#import "IBECore.h"
#import "ibeerror.h"

@implementation MainUITableViewController

@synthesize messageBarItem = _messageBarItem;

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
    firstLoad = YES;
    ibemsgd = [IBEMessageDaemon getInstance];
    [ibemsgd moniteringMessage];
    IBEAppDelegate *appDelegate = [[UIApplication sharedApplication]delegate];
    NSManagedObjectContext *context = [appDelegate managedObjectContext];
    IBESystemDAO* dao = [[IBESystemDAO alloc]initWithNSManagedObjectContext:context];
    latestMessageDAO = [[IBELatestMessageDAO alloc]initWithNSManagedObjectContext:context];
    messageDAO = [[IBEMessageDAO alloc]initWithNSManagedObjectContext:context];
    messages = [[NSMutableArray alloc]initWithArray:[latestMessageDAO listLatestMessages]];
    NSArray* systems = [dao listSystems];
    if (!systems) {
        // 插入默认IBE系统
        unsigned char g[128];
        unsigned char g1[128];
        unsigned char h[128];
        char* g_hex = "9eb3926de267d35bd6a78a97a99549fb61e64536ce615dd25bd7fab394b4ced624e202aec4faa9be87d24ebaf8e1f122652f6d3b87b44db86182618dccc40ef54e9bd9c48c73f3b8b1af8b1dcb34c43fd99d5f88cf6994ac2bfc8cafc4d544395a88bc0a081b863a711ffc39bc629f2d1bb1d5d3e9ff906061d068475a4712c3";
        char* g1_hex = "15cb9560fd7a39f8879fa7c1440a8b92023bdb790ee47e19c44fe57f09c5d49d0748868fe43d8dbc00680945790c630919f9853d03a509a1b66a3793f34a8900064cad66bf71d735a75ebe8c4d26ea2f93e29748cd7c4d991982ec8667f85b8fe755f12e573d0e96437196d22ffa36813e9377fc4ae5c5ab8953336f912d612a";
        char* h_hex = "8a9fee3cc1ad0b49f9383285c5f440914fcbb8f8a7161ae39e214dfe0f93aa9f489e14af65182da90e2d3e8f935dc82bd1ca32aebd0e5909259dd32b5de74dfb31f698735e23974486f087c302c7a35e87c34f68fba048e4fa7e7595d2d1cefb399952c2a743494c89f6ff5388ab020a7a1a4ec19963fdeb2b35a59f059e16f7";
        char* pairing = "type a q 8780710799663312522437781984754049815806883199414208211028653399266475630880222957078625179422662221423155858769582317459277713367317481324925129998224791 h 12016012264891146079388821366740534204802954401251311822919615131047207289359704531102844802183906537786776 r 730750818665451621361119245571504901405976559617 exp2 159 exp1 107 sign1 1 sign0 1 ";

        unhex(g_hex, 256, g, 128);
        unhex(g1_hex, 256, g1, 128);
        unhex(h_hex, 256, h, 128);

        NSMutableData* params = [[NSMutableData alloc]initWithBytes:g length:128];
        [params appendBytes:g1 length:128];
        [params appendBytes:h length:128];
        NSData* pair = [[NSData alloc]initWithBytes:pairing length:strlen(pairing)];
        IBESystem* system = [dao newManagedObject];
        [system setName:@"默认系统"];
        [system setPublic_params:params];
        [system setPairing:pair];
        [system setIbe_system_number:[NSNumber numberWithInt:1]];

        NSError* error;
        [context save:&error];
        if (error) {
            // TODO 错误提示
        }
    }
}

- (void)viewDidUnload
{
    [self setMessageBarItem:nil];
    messageDAO = nil;
    latestMessageDAO = nil;
    [ibemsgd removeMessageConsumer:self];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
    if (!firstLoad) {
        if ([messages count] > 0) {
            [messages removeAllObjects];        
        }
        messages = nil;
        messages = [[NSMutableArray alloc]initWithArray:[latestMessageDAO listLatestMessages]];
        [self.tableView performSelectorOnMainThread:@selector(reloadData) withObject:nil waitUntilDone:YES];
    }
    [ibemsgd addMessageConsumer:self];
    [super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
    firstLoad = NO;
    [ibemsgd removeMessageConsumer:self];
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

- (void)messageReceived:(NSArray*)msgs {
    if (msgs && [msgs count] > 0) {
        [messages removeAllObjects];
        messages = nil;
        messages = [[NSMutableArray alloc]initWithArray:[latestMessageDAO listLatestMessages]];
        [self.tableView performSelectorOnMainThread:@selector(reloadData) withObject:nil waitUntilDone:YES];
    }
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [messages count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"message_item";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    }
    NSArray *viewToDelete = [cell.contentView subviews];
    if (viewToDelete) {
        for (UIView *subview in viewToDelete)
            [subview removeFromSuperview];
    }
    NSUInteger index = [indexPath indexAtPosition:1];
    IBELatestMessage *message = [messages objectAtIndex:index];
    [IBEMessageCell setAddresser:message.contact andLastMessage:message.message_content onLastDate:  message.message_date isRead:[message.message_read intValue] forCell:cell];
    return cell;
}

- (NSString *)tableView:(UITableView *)tableView titleForDeleteConfirmationButtonForRowAtIndexPath:(NSIndexPath *)indexPath{
    return @"删除";
}

- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    return YES;
}

- (void)showMessageDeleteResult:(NSError*)error {
    if (error) {
        NSLog(@"%@", error);
        NSString *errMsg = [NSString stringWithFormat:@"无法删除会话：%@", [error localizedDescription]];
        UIAlertView *alert = [[UIAlertView alloc]initWithTitle:@"错误" message:errMsg delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil];
        [alert show];
        return;
    }
    [deleteView deleteRowsAtIndexPaths:[NSArray arrayWithObject:deletePath] withRowAnimation:UITableViewRowAnimationFade];
    deletePath = nil;
    deleteView = nil;
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        deletePath = indexPath;
        deleteView = tableView;
        UIAlertView *confirm = [[UIAlertView alloc]initWithTitle:@"警告" message:@"确认删除会话吗？" delegate:self cancelButtonTitle:@"取消" otherButtonTitles:@"确定", nil];
        [confirm show];
    }
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex{
    if (buttonIndex > 0) {  // "取消"是第一个按钮
        NSUInteger index = [deletePath indexAtPosition:1];
        IBELatestMessage *message = [messages objectAtIndex:index];
        [messages removeObjectAtIndex:index];
        [messageDAO removeAllMessages:message];
        NSError *error;
        [message.managedObjectContext save:&error];
        [self performSelectorOnMainThread:@selector(showMessageDeleteResult:) withObject:error waitUntilDone:YES];
    }
}

- (void)alertViewCancel:(UIAlertView *)alertView {
    UITableViewCell *cell = [deleteView cellForRowAtIndexPath:deletePath];
    [cell setEditing:NO animated:YES];
    deletePath = nil;
    deleteView = nil;
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView accessoryButtonTappedForRowWithIndexPath:(NSIndexPath *)indexPath {
    [self tableView:tableView didSelectRowAtIndexPath:indexPath];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    UIStoryboard* storyBoard = [UIStoryboard storyboardWithName:@"MainStoryboard" bundle:nil];
    ConversationDetailViewController* nextController = [storyBoard instantiateViewControllerWithIdentifier:@"conversation_detail_view"];
    NSUInteger index = [indexPath indexAtPosition:1];
    IBELatestMessage *message = [messages objectAtIndex:index];
    [nextController setContact:message.contact];
    [self.navigationController pushViewController:nextController animated:YES];
}

@end

//////////////////////////////////////////////////////////////////////////////////

@implementation ConversationDetailViewController

@synthesize titleNavigationItem = _titleNavigationItem;
@synthesize editNewMessageTextField = _editNewMessageTextField;
@synthesize tableView = _tableView;
@synthesize messageEditButton = _messageEditButton;
@synthesize contact = _contact;

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
    
    [_tableView setBackgroundColor:[UIColor clearColor]];
    [_tableView setSeparatorColor:[UIColor clearColor]];
    
    IBEAppDelegate *appDelegate = [[UIApplication sharedApplication]delegate];
    NSManagedObjectContext *context = [appDelegate managedObjectContext];
    messageDAO = [[IBEMessageDAO alloc]initWithNSManagedObjectContext:context];
    latestMessageDAO = [[IBELatestMessageDAO alloc]initWithNSManagedObjectContext:context];
    IBEContactDAO *contactDAO = [[IBEContactDAO alloc]initWithNSManagedObjectContext:context];
    ibeContact = [contactDAO find:_contact];
    if (ibeContact.first_name == nil && ibeContact.family_name == nil) {
        _titleNavigationItem.title = ibeContact.default_id;
    } else if (ibeContact.first_name != nil && ibeContact.family_name == nil) {
        _titleNavigationItem.title = ibeContact.first_name;
    } else if (ibeContact.first_name == nil && ibeContact.family_name != nil) {
        _titleNavigationItem.title = ibeContact.family_name;
    } else {
        _titleNavigationItem.title = [NSString stringWithFormat:@"%@ %@", ibeContact.family_name, ibeContact.first_name];
    }
    ibemsgd = [IBEMessageDaemon getInstance];
    messages = [[NSMutableArray alloc]init];
    [messages addObjectsFromArray:[messageDAO listMessages:ibeContact]];
    selectedMessages = [[NSMutableArray alloc]initWithCapacity:[messages count]];
    for (int i = [messages count]; i > 0; i--) {
        [selectedMessages addObject:[NSNumber numberWithBool:NO]];
    }
}

- (void)viewDidUnload
{
    [self setTitleNavigationItem:nil];
    [self setEditNewMessageTextField:nil];
    [self setTableView:nil];
    [ibemsgd removeMessageConsumer:self];
    [self setMessageEditButton:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)displayLatestMessages {
    NSUInteger index[2];
    index[0] = 0;
    index[1] = [messages count] - 1;
    NSIndexPath *path = [[NSIndexPath alloc]initWithIndexes:index length:2];
    [_tableView scrollToRowAtIndexPath:path atScrollPosition:UITableViewScrollPositionBottom animated:NO];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [ibemsgd addMessageConsumer:self];
    [self performSelectorOnMainThread:@selector(displayLatestMessages) withObject:nil waitUntilDone:YES];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    IBELatestMessage *lmsg = [latestMessageDAO getLatestMessage:ibeContact];
    [lmsg setMessage_read:[NSNumber numberWithInt:MESSAGE_READ]];
    [lmsg.managedObjectContext save:nil];
    isMessageEditingMode = NO;
}

- (void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
	[super viewDidDisappear:animated];
    [ibemsgd removeMessageConsumer:self];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
}

- (void)messageReceived:(NSArray*)msgs {
    if (msgs && [msgs count] > 0) {
        for (IBEMessage* msg in msgs) {
            if (![msg.from.default_id isEqualToString:ibeContact.default_id]) {
                NSMutableString *dispName = [[NSMutableString alloc]init];
                if (msg.from.family_name) {
                    [dispName appendString:msg.from.family_name];
                }
                if (msg.from.first_name) {
                    [dispName appendString:msg.from.first_name];
                }
                if ([dispName length] == 0) {
                    [dispName appendString:msg.from.default_id];
                }
                UIAlertView *alert = [[UIAlertView alloc]initWithTitle:[NSString stringWithFormat:@"%@发来了新消息", dispName] message:msg.content delegate:nil cancelButtonTitle:@"确认" otherButtonTitles:nil];
                [alert performSelectorOnMainThread:@selector(show) withObject:nil waitUntilDone:YES];
            } else {
                [messages removeAllObjects];
                messages = nil;
                messages = [[NSMutableArray alloc]initWithArray:[messageDAO listMessages:ibeContact]];
                [selectedMessages addObject:[NSNumber numberWithBool:NO]];
                [_tableView performSelectorOnMainThread:@selector(reloadData) withObject:nil waitUntilDone:YES];
                return;
            }
        }
    }
}

- (void)refreshMessages {
    [messages addObject:draft];
    [_tableView reloadData];
    [_editNewMessageTextField resignFirstResponder];
    [_editNewMessageTextField setText:@""];
    if (self.view.frame.origin.y < 0)
        [self setViewMovedUp:NO];
}

- (void)toogleEditMode:(NSNumber*)doReload {
    if (isMessageEditingMode) {
        // 从编辑模式转换成正常模式
        isMessageEditingMode = NO;
        _messageEditButton.title = @"编辑";
        [_titleNavigationItem setLeftBarButtonItem:nil animated:YES];
        [self.tableView setEditing:NO animated:YES];
    } else {
        // 从正常模式转换为编辑模式
        isMessageEditingMode = YES;
        _messageEditButton.title = @"取消";
        UIBarButtonItem *backItem = [[UIBarButtonItem alloc] initWithTitle:@"删除" style:UIBarButtonItemStyleDone target:nil action:nil];
        backItem.action = @selector(doDeleteMessages:);
        backItem.target = self;
        [_titleNavigationItem setLeftBarButtonItem:backItem animated:YES];
        [_tableView setEditing:YES animated:YES];
    }
    if (doReload && [doReload boolValue]) {
        [_tableView reloadData];
    }
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex{
    if (buttonIndex > 0) {  // "取消"是第一个按钮
        NSUInteger i = 0;
        IBEAppDelegate *appDelegate = [[UIApplication sharedApplication]delegate];
        NSManagedObjectContext *context = [appDelegate managedObjectContext];
        BOOL hasDel = NO;
        for (NSNumber *toDel in selectedMessages) {
            if ([toDel boolValue]) {
                IBEMessage *msg = [messages objectAtIndex:i];
                [context deleteObject:msg];
                [messages removeObject:msg];
                hasDel = YES;
            }
            i++;
        }
        if (!hasDel) {
            [self toogleEditMode:nil];
            return;
        }
        NSError *error;
        [context save:&error];
        if (error) {
            NSLog(@"%@", error);
            NSString *errMsg = [NSString stringWithFormat:@"无法删除消息：%@", [error localizedDescription]];
            UIAlertView *alert = [[UIAlertView alloc]initWithTitle:@"错误" message:errMsg delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil];
            [alert show];
            return;
        }
        [selectedMessages removeAllObjects];
        for (i = 0; i < [messages count]; i++) {
            [selectedMessages addObject:[NSNumber numberWithBool:NO]];
        }
        [self toogleEditMode:[NSNumber numberWithBool:YES]];
    }
}

- (void)alertViewCancel:(UIAlertView *)alertView {
    [_tableView setEditing:NO animated:YES];
}

- (IBAction)doDeleteMessages:(id)sender {
    BOOL hasDel = NO;
    for (NSNumber *toDel in selectedMessages) {
        if ([toDel boolValue]) {
            hasDel = YES;
        }
    }
    if (hasDel) {
        UIAlertView *confirm = [[UIAlertView alloc]initWithTitle:@"确认删除" message:@"你确定从手机中删除这些消息吗？" delegate:self cancelButtonTitle:@"取消" otherButtonTitles:@"确定", nil];
        [confirm show];
    }
}

- (IBAction)editMessages:(id)sender {
    [self toogleEditMode:nil];
}

- (IBAction)sendMesage:(id)sender {
    NSString *recipient = ibeContact.default_id;
    NSString *content = _editNewMessageTextField.text;
    IBEConfig *settings = [IBEConfig getInstance];
    SendNewMessageRequest *request = [[SendNewMessageRequest alloc]initWithEmail:settings.email andPassword:settings.password WithMessage:content forRecipient:recipient];    
    IBERelayServerClientHTTPImpl *http = [[IBERelayServerClientHTTPImpl alloc]initWithURL:[settings getRelaryServerURL] andRequest:request withHandler:self];
    [http startRequest];
    // 将发送的信息存入本地数据库 信息表 信息状态为草稿
    draft = [messageDAO newManagedObject];
    [draft setFrom:ibeContact];
    [draft setContent:content];
    [draft setType:[NSNumber numberWithInt:MESSAGE_DRAFT]];
    NSDate *date = [[NSDate alloc]init];
    [draft setReceive_date:date];
    [draft setMessage_id:[NSNumber numberWithInt:-1]];
    NSError *error;
    [draft.managedObjectContext save:&error];
    if (error) {
        NSLog(@"%@", error);
        UIAlertView *alert = [[UIAlertView alloc]initWithTitle:@"错误" message:@"保存已发信息时发生错误" delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil];
        [alert show];
    }
}

- (void) dataReceived:(NSData *) data {
    unsigned char* poniter = (unsigned char *) [data bytes];
    if (ERR_SUCCESS == poniter[0] - '0') {
        [draft setType:[NSNumber numberWithInt:MESSAGE_SENT]];
        [latestMessageDAO updateLatestMessage:draft];
        [selectedMessages addObject:[NSNumber numberWithBool:NO]];
        [self performSelectorOnMainThread:@selector(refreshMessages) withObject:nil waitUntilDone:YES];
        [self performSelectorOnMainThread:@selector(displayLatestMessages) withObject:nil waitUntilDone:YES];
    }
}

- (void) errorOccured:(NSError *) error {
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [messages count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *CellIdentifier = @"detail_mesage_item";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
        cell.accessoryType = UITableViewCellAccessoryNone;
    }
    NSUInteger index = [indexPath indexAtPosition:1];
    IBEMessage *message = [messages objectAtIndex:index];
    NSArray *viewToDelete = [cell.contentView subviews];
    if (viewToDelete) {
        for (UIView *subview in viewToDelete)
            [subview removeFromSuperview];
    }
    UIImage *bubble = [UIImage imageWithContentsOfFile:[[NSBundle mainBundle] pathForResource:[message.type intValue] ? @"bubbleSelf" : @"bubble" ofType:@"png"]];
    UIImageView *imageView = [[UIImageView alloc] initWithImage:[bubble stretchableImageWithLeftCapWidth:21 topCapHeight:14]];    
    UIFont *font = [UIFont systemFontOfSize:12];
    CGSize size = [message.content sizeWithFont:font constrainedToSize:CGSizeMake(150, 1000) lineBreakMode:UILineBreakModeCharacterWrap];
    UILabel *bubbleText = [[UILabel alloc]init];
    bubbleText.backgroundColor = [UIColor clearColor];
    bubbleText.font = font;
    bubbleText.numberOfLines = 0;
    bubbleText.lineBreakMode = UILineBreakModeCharacterWrap;
    bubbleText.text = message.content;
    imageView.backgroundColor = [UIColor clearColor];
    if([message.type intValue]) {
        imageView.frame = CGRectMake(270 - size.width, 10, size.width + 30, size.height + 10);
        bubbleText.frame = CGRectMake(285 - size.width, 15, size.width + 10, size.height);
    } else {
        imageView.frame = CGRectMake(15, 10, size.width + 30, size.height + 10);
        bubbleText.frame = CGRectMake(30, 15, size.width + 10, size.height);
    }
    [cell.contentView addSubview:imageView];
    [cell.contentView addSubview:bubbleText];
    UIView *transBack = [[UIView alloc]init];
    transBack.alpha = 0;
    cell.selectedBackgroundView = transBack;
    UIView *mutilTransBack = [[UIView alloc]init];
    mutilTransBack.alpha = 0;
    cell.multipleSelectionBackgroundView = mutilTransBack;
    return cell;
}

- (BOOL)textFieldShouldReturn:(UITextField *)theTextField {
    if (theTextField == _editNewMessageTextField) {
        [theTextField resignFirstResponder];
    }
    [self performSelectorOnMainThread:@selector(displayLatestMessages) withObject:nil waitUntilDone:YES];
    return YES;
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    if (isMessageEditingMode) {
        NSUInteger index = [indexPath indexAtPosition:1];
        BOOL selected = [[selectedMessages objectAtIndex:index]boolValue];
        [selectedMessages replaceObjectAtIndex:index withObject:[NSNumber numberWithBool:!selected]];
    }
}

- (void)tableView:(UITableView *)tableView didDeselectRowAtIndexPath:(NSIndexPath *)indexPath {
    if (isMessageEditingMode) {
        NSUInteger index = [indexPath indexAtPosition:1];
        BOOL selected = [[selectedMessages objectAtIndex:index]boolValue];
        [selectedMessages replaceObjectAtIndex:index withObject:[NSNumber numberWithBool:!selected]];
    }
}

- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath {
    return UITableViewCellEditingStyleDelete | UITableViewCellEditingStyleInsert;
}

#define kOFFSET_FOR_KEYBOARD 250.0

- (void)textFieldDidBeginEditing:(UITextField *)sender {
    if ([sender isEqual:_editNewMessageTextField]) {
        if (self.view.frame.origin.y >= 0) {
            [self setViewMovedUp:YES];
            [self performSelectorOnMainThread:@selector(displayLatestMessages) withObject:nil waitUntilDone:YES];
        }
    }
}

- (void)textFieldDidEndEditing:(UITextField *)sender {
    if ([sender isEqual:_editNewMessageTextField]) {
        if (self.view.frame.origin.y < 0) {
            [self setViewMovedUp:NO];
        }
    }
}

- (void)setViewMovedUp:(BOOL)movedUp {
    [UIView beginAnimations:nil context:NULL];
    [UIView setAnimationDuration:0.3];
    CGRect rect = self.view.frame;
    if (movedUp) {
        rect.origin.y -= kOFFSET_FOR_KEYBOARD;
        rect.size.height += kOFFSET_FOR_KEYBOARD;
    } else {
        rect.origin.y += kOFFSET_FOR_KEYBOARD;
        rect.size.height -= kOFFSET_FOR_KEYBOARD;
    }
    self.view.frame = rect;
    [UIView commitAnimations];
}

@end

///////////////////////////////////////////////////////////////////////////////////////////////
@implementation IBEMessageCell

+ (void)setAddresser:(NSString*) addresser andLastMessage:(NSString*) message onLastDate:(NSDate*) date isRead:(NSInteger)read forCell:(UITableViewCell*) cell {
    NSDateFormatter* formatter = [[NSDateFormatter alloc]init];
    [formatter setDateFormat:@"yyyy-MM-dd"];

    if (read == MESSAGE_UNREAD) {
        // 未读消息有不同的背景色
        UIView *backgrdView = [[UIView alloc] initWithFrame:cell.frame];
        backgrdView.backgroundColor = [UIColor colorWithRed:0.53 green:0.81 blue:0.92 alpha:0.5];
        cell.backgroundView = backgrdView;
    } else {
        UIView *backgrdView = [[UIView alloc] initWithFrame:cell.frame];
        backgrdView.backgroundColor = nil;
        cell.backgroundView = backgrdView;
    }

    UILabel* addresserLabel = [[UILabel alloc]initWithFrame:CGRectMake(12, 2, 178, 21)];
    addresserLabel.textColor = [UIColor blackColor];
    addresserLabel.textAlignment = UITextAlignmentLeft;
    addresserLabel.font = [UIFont boldSystemFontOfSize:18];
    addresserLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
    addresserLabel.text = addresser;
    addresserLabel.backgroundColor = [UIColor clearColor];
    [cell.contentView addSubview:addresserLabel];

    UILabel* lastDateLabel = [[UILabel alloc]initWithFrame:CGRectMake(200, 2, 80, 21)];
    lastDateLabel.textColor = [UIColor blueColor];
    lastDateLabel.textAlignment = UITextAlignmentRight;
    lastDateLabel.font = [UIFont systemFontOfSize:14];
    lastDateLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
    lastDateLabel.text = [formatter stringFromDate:date];
    lastDateLabel.backgroundColor = [UIColor clearColor];
    [cell.contentView addSubview:lastDateLabel];

    UILabel* lastMessageLabel = [[UILabel alloc]initWithFrame:CGRectMake(12, 22, 264, 22)];
    lastMessageLabel.textColor = [UIColor grayColor];
    lastMessageLabel.textAlignment = UITextAlignmentLeft;
    lastMessageLabel.font = [UIFont systemFontOfSize:16];
    lastMessageLabel.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleHeight;
    lastMessageLabel.text = message;
    lastMessageLabel.backgroundColor = [UIColor clearColor];
    [cell.contentView addSubview:lastMessageLabel];
}

@end
