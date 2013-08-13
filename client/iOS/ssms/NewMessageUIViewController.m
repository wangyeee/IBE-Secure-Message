//
//  NewMessageUIViewController.m
//  ssms
//
//  Created by 烨 王 on 12-3-31.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import "NewMessageUIViewController.h"
#import "IBECore.h"
#import "ibeerror.h"
#import "IBEAppDelegate.h"

@implementation NewMessageUIViewController

@synthesize receiptTextField = _receiptTextField;
@synthesize mesageTextField = _mesageTextField;

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

- (void)viewDidLoad
{
    [super viewDidLoad];
    IBEAppDelegate *appDelegate = [[UIApplication sharedApplication]delegate];
    NSManagedObjectContext *context = [appDelegate managedObjectContext];
    messageDAO = [[IBEMessageDAO alloc]initWithNSManagedObjectContext:context];
    contactDAO = [[IBEContactDAO alloc]initWithNSManagedObjectContext:context];
    latestMessageDAO = [[IBELatestMessageDAO alloc]initWithNSManagedObjectContext:context];
}

- (void)viewDidUnload
{
    [self setReceiptTextField:nil];
    [self setMesageTextField:nil];
    [super viewDidUnload];
    messageDAO = nil;
    contactDAO = nil;
    latestMessageDAO = nil;
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

- (IBAction)selectFromContacts:(id)sender {
}

- (IBAction)sendNewMessage:(id)sender {
    NSString *recipient = _receiptTextField.text;
    NSString *content = _mesageTextField.text;
    IBEConfig *settings = [IBEConfig getInstance];
    SendNewMessageRequest *request = [[SendNewMessageRequest alloc]initWithEmail:settings.email andPassword:settings.password WithMessage:content forRecipient:recipient];    
    IBERelayServerClientHTTPImpl *http = [[IBERelayServerClientHTTPImpl alloc]initWithURL:[settings getRelaryServerURL] andRequest:request withHandler:self];
    [http startRequest];
    // 将发送的信息存入本地数据库 信息表 信息状态为草稿
    draft = [messageDAO newManagedObject];
    IBEContact *to = [contactDAO find:recipient];
    if (!to) {
        NSLog(@"Contact %@ not exist, create it.", recipient);
        to = [contactDAO saveDefault:recipient];
    } else {
        NSLog(@"Contact %@ exist, use it.", recipient);
    }
    [draft setFrom:to];
    [draft setContent:content];
    [draft setType:[NSNumber numberWithInt:MESSAGE_DRAFT]];
    [draft setMessage_id:[NSNumber numberWithInt:-1]];
    NSDate *date = [[NSDate alloc]init];
    [draft setReceive_date:date];
    NSError *error;
    [draft.managedObjectContext save:&error];
    if (error) {
        NSLog(@"%@", error);
    }
}

- (void) back {
    [self performSegueWithIdentifier: @"discard_new_message" sender:self];
}

- (IBAction)discardNewMessage:(id)sender {
    [self back];
}

- (void) dataReceived:(NSData *) data {
    NSLog(@"%@", data);
    unsigned char* poniter = (unsigned char *) [data bytes];
    if (ERR_SUCCESS == poniter[0] - '0') {
        // TODO 将发送的信息存入本地数据库 最近信息表 供显示 并更新信息状态为已发送
        [draft setType:[NSNumber numberWithInt:MESSAGE_SENT]];
        [latestMessageDAO updateLatestMessage:draft];
        [self performSelectorOnMainThread:@selector(back) withObject:nil waitUntilDone:YES];
    }
}

- (void) errorOccured:(NSError *) error {
}

@end
