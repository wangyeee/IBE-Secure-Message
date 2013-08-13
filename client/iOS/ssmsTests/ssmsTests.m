//
//  ssmsTests.m
//  ssmsTests
//
//  Created by 烨 王 on 12-2-22.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import "ssmsTests.h"
#import "ibecommon.h"
#import "IBECore.h"

@implementation ssmsTests

- (void)setUp
{
    [super setUp];
    
    // Set-up code here.
}

- (void)tearDown
{
    // Tear-down code here.
    
    [super tearDown];
}

- (void)testExample
{/*
    char* hex0 = "65cf3983fc810a12bf66231a826dd6374b171a5db24b8192e3c951dbdede06714f5c93e1fc013ece70a85b0df3ea365c6f333f6eefeeae408d60219c28b48ef6ef08f6c7a20064f51f29babe3432586ff8126b3f90befddcdf1162624bb071419bd3afedf1123a12100fa4839736cfe73579fa761df472d3f64b7e44";
    int hexLength = strlen(hex0);
    NSLog(@"hex length:%d\n", hexLength);
    byte* raw = (byte*) malloc(hexLength / 2);
    unhex(hex0, hexLength, raw, hexLength / 2);
    IBEPlainText* plain = [IBEPlainText buildFromSignificantBytes:raw withLength:hexLength / 2];
    byte* storage = (byte*) malloc([plain lengthInBytes]);
    [plain toBytes:storage withCapacity:[plain lengthInBytes]];
    
    char* hexStor = (char*) malloc([plain lengthInBytes] * 2 + 1);
    hexStor[[plain lengthInBytes] * 2] = '\0';
    hex(storage, [plain lengthInBytes], hexStor, [plain lengthInBytes] * 2);
    printf("plain:\n%s\n", hexStor);
    free(raw);
    free(storage);
*/}

- (void) testDecJava {/*
    char* hexCipher = "a700509c3e6cfaff6a8f078eae40414de346f9fb6bdafa2882087f4c9e9c7a926932feb531717ad50b00967e418eebffb7b2107030e701b43c66bbd29a48aaf111abb5507f595368edec648b75fb8555715eb5aed4c5c0e12fb56e3f3b2bde2a61e378e815261c284e0482b95816d8600c6f6d20671a893ca5301975dfb1f510187132be1d12f37d2ce6ed020693788ae5c5cc0a82f36cd26eac358b44c1b4ffee8e6312443bcef9ac33dec4738699e636831140f73c3aebd8660a4c406693791bad7cd0bf36aa7e4fb23d41a8d1d2ebfadd851dbed725eef4cebb8ab952a3234e3114a3e3e28ab75335e53704f46026cff7464f03ebdea3a3982a374dc35a6fa1465bc7fb76636abbee845bf9e30d7aadbef03cce2e42f9a1429248f20f050bf2a1b642e7e3384e15fed9ec3a0a7df5e75d4ebbeac6a9f48adc38d9fef9682e7174d43a4d52ff268e94292cd7738f70e749b29ff4c9abf3cd4213fbe4773433fec0344035d14bdd15fff16477be3b70aa147c5bc3f018216e02f444f5ee0dd46b";

    char* hexKey = "09993c27fcee62bae4031a6783da9e9c0a8bc67b2764ccd00995baacc3260d20f321ddfc525ff45feb3f1513da1d23660dd7702881bd3b979eb85f4e5bb71265508de720c7ec5467b9b496867ebfe85a43a9328d76f333cc22eab81b4b1429bed24cd2e296d3daaa5b19c689d631a77b918e6e7c534aca70bd961fbe2dc5ce05c1715403e3ffe882b3d95f11a9575542e347dede0000001277616e677965656540676d61696c2e636f6d00000167747970652061207120383738303731303739393636333331323532323433373738313938343735343034393831353830363838333139393431343230383231313032383635333339393236363437353633303838303232323935373037383632353137393432323636323232313432333135353835383736393538323331373435393237373731333336373331373438313332343932353132393939383232343739312068203132303136303132323634383931313436303739333838383231333636373430353334323034383032393534343031323531333131383232393139363135313331303437323037323839333539373034353331313032383434383032313833393036353337373836373736207220373330373530383138363635343531363231333631313139323435353731353034393031343035393736353539363137206578703220313539206578703120313037207369676e312031207369676e30203120";

    byte* cipherRaw = (byte*) malloc(strlen(hexCipher) / 2);
    unhex(hexCipher, strlen(hexCipher), cipherRaw, strlen(hexCipher) / 2);

    byte* rawKey = (byte*) malloc(strlen(hexKey) / 2);
    unhex(hexKey, strlen(hexKey), rawKey, strlen(hexKey) / 2);

    IBECipherText* cipher = [[IBECipherText alloc] init];
    [cipher buildFromBytes:cipherRaw withLength:strlen(hexCipher) / 2];

    IBEPrivateKey* key = [[IBEPrivateKey alloc] init];
    [key buildFromBytes:rawKey withLength:strlen(hexKey) / 2];

    IBEPlainText* plain = [IBEEngine decryptFromCipher:cipher withKey:key];
    NSData* rawData = [plain toSignificantBytes];
    char ttt[257];
    memset(ttt, 0, 257);
    hex((byte*) [rawData bytes], [rawData length], ttt, 256);
    printf("dec:\n%s\n", ttt);

    NSString* str = [[NSString alloc] initWithData:rawData encoding:NSASCIIStringEncoding];
    NSLog(@"Message From Java:\n%@\n", str);
    free(cipherRaw);
    free(rawKey);
    */
}

- (void) testPlainAutoPadding {/*
    char* hexData = "48656c6c6f206a736768666467626a6664686267686a666462677569726562677579646662676466756267646675626764667562676466626766646267646662676466686a62676466686a62676466686a62676466686a62676466686a676268646762646a576f726c6421";
    int length = strlen(hexData);
    byte* data = (byte*) malloc(length / 2);
    unhex(hexData, length, data, length / 2);
    
    IBEPlainText* plain = [IBEPlainText buildFromSignificantBytes:data withLength:length / 2];
    NSData* padded = [plain toSignificantBytes];
    char* hex2 = (char*) malloc([plain length] * 2 + 1);
    hex2[[plain length] * 2] = '\0';
    hex((byte*) [padded bytes], [padded length], hex2, [plain length] * 2);
    printf("%s\n", hex2);
    printf("equal?%d\n", strcmp(hex2, hexData));
    free(data);*/
}

- (void) testEncryptForJava {
    NSString* receiver = @"wangyeee@gmail.com";
    char* testPlain = "Hello From Obj-C.";
    char* hexParam = "685a0e7fc44a99bce11f020525c3ca7d9c1b23d988636c0e422955a6f33c469ea909e97e8ce837e037af38053fa6f32025005f3becbae1bd027bddf2d56b31d594a6d380ae99b4deee551e03120835a0b18de2f71628f5527907a840d591134d567099c5daf6fd88c8fff3a97201d25ddadb17d603380d210b53481ceb2c87747c7fe6f3a2444e0e55e49e274a7ac46677614ddd8bf12e2c27e0b2274aedfc99b52054d2645ff8774ceb46292bb67e2c0e2eb4ee1735d968bf32ea1458f74e736574391624e029e768451458fdffab82a573e2a16d0422e8f06b48d5c4387e4bd1969478174cbb9a4292933b4276d9cf85d15546409412382179178c169ecb1c93d5812927b98c302a76a769981a6070b1901a74a80e2e2c82e8326c6325f61c7dc5ee577ed096f6bb58568c7b84aabe8bf96f7c1e234dde47fd7bcfd3fe782e87d1f9ddd002bc296d0dca97d15664376d98290ad194cb87b5515a92ac7c0350571fd0056d1ac8d030cdf197dee0c20e51c5fb8ab7ef720cd51efeea0b93ee8100000167747970652061207120383738303731303739393636333331323532323433373738313938343735343034393831353830363838333139393431343230383231313032383635333339393236363437353633303838303232323935373037383632353137393432323636323232313432333135353835383736393538323331373435393237373731333336373331373438313332343932353132393939383232343739312068203132303136303132323634383931313436303739333838383231333636373430353334323034383032393534343031323531333131383232393139363135313331303437323037323839333539373034353331313032383434383032313833393036353337373836373736207220373330373530383138363635343531363231333631313139323435353731353034393031343035393736353539363137206578703220313539206578703120313037207369676e312031207369676e30203120";

    int paramLength = strlen(hexParam);
    byte* paramRaw = (byte*) malloc(paramLength / 2);
    
    IBEPublicParameter* pub = [[IBEPublicParameter alloc] init];
    [pub buildFromBytes:paramRaw withLength:paramLength / 2];
    
    IBEPlainText* plain = [IBEPlainText buildFromSignificantBytes:(byte*) testPlain withLength:strlen(testPlain)];
    IBECipherText* cipher = [IBEEngine encryptData:plain forReceiver:receiver underParameter:pub];
    byte* cipherBuffer = (byte*) malloc([cipher lengthInBytes]);
    [cipher toBytes:cipherBuffer withCapacity:[cipher lengthInBytes]];
    
    char* hexCipher = (char*) malloc([cipher lengthInBytes] * 2 + 1);
    hexCipher[[cipher lengthInBytes] * 2] = '\0';
    hex(cipherBuffer, [cipher lengthInBytes], hexCipher, [cipher lengthInBytes] * 2);
    printf("Cipher Text:\n%s\n", hexCipher);

    free(paramRaw);
    free(cipherBuffer);
    free(hexCipher);
}

@end
