//
//  ItraffApi.m
//
//  Created by iTraff Technology.
//

#import "ItraffApi.h"

#import <CommonCrypto/CommonDigest.h>

@implementation ItraffApi

-(id)initWithKey
    :(NSString *)clientId 
    :(NSString *)apiKey 
{
    self = [super init];
    
    if (self) {
        self->_id = clientId;
        self->_key = [apiKey UTF8String];
    }
    
    return self;
}

/*!
 @abstract extract data from image
 @discussion image is scaled and converted to gray scale 
 @param source
 source image
 @return NSData image data
 */
-(NSData *)imageData
    :(UIImage *)source
{
    float ratio = source.size.width / source.size.height;
    int w, h;
    if (ratio > 1) {
        w = 480 * ratio;
        h = 480;
    } else {
        w = 480;
        h = 480 * ratio;
    }
    
	CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceGray();
	CGContextRef context = CGBitmapContextCreate(nil, w, h, 8, 0, colorSpace, kCGImageAlphaNone);
	CGColorSpaceRelease(colorSpace);
    
	if (NULL == context)
		return nil;
    
	CGContextDrawImage(context, CGRectMake(0, 0, w, h), source.CGImage);
	UIImage *grayImage = [UIImage imageWithCGImage:CGBitmapContextCreateImage(context)];
	CGContextRelease(context);
    
	return UIImageJPEGRepresentation(grayImage, 0.6);
}

/*!
 @abstract generate md5
 @param image
 image data
 @return NSString hash as a 32-character hexadecimal number
 */
- (NSString *)hash
    :(NSData *)image
{
    if (strlen(_key) < 1)
        return nil;
    
    CC_MD5_CTX c;
    CC_MD5_Init(&c);
    CC_MD5_Update(&c, _key, strlen(_key));
    CC_MD5_Update(&c, [image bytes], [image length]);
    unsigned char result[16];
    CC_MD5_Final(result, &c);
    return [NSString stringWithFormat:
        @"%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x",
        result[0], result[1], result[2], result[3], 
        result[4], result[5], result[6], result[7],
        result[8], result[9], result[10], result[11],
        result[12], result[13], result[14], result[15]
    ];  
}

- (void) send
    :(UIImage *) jpgImage
    :(id<NSURLConnectionDataDelegate>) delegate
{
    NSData *data = [self imageData:jpgImage];
    NSString *url = [NSString stringWithFormat:@"http://recognize.im/recognize/%@", _id];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:url]];
    [request setHTTPMethod:@"POST"];
    [request setValue:@"image/jpeg" forHTTPHeaderField: @"Content-Type"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];
    [request setValue:[self hash:data] forHTTPHeaderField:@"X-iTraff-hash"];
    [request setHTTPBody:data];
    [[[NSURLConnection alloc] initWithRequest:request delegate:delegate] start];
}

@end
