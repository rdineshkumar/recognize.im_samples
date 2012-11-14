//
//  ItraffApi.h
//
//  Created by iTraff Technology.
//

#import <Foundation/Foundation.h>

@interface ItraffApi : NSObject {
@protected
    NSString *_id;
    const char *_key;
}

/*!
 @abstract set client id and api key
 @param clientId
 client id received at http://recognize.im
 @param apiKey
 corresponding api key
 */
-(id)initWithKey
    :(NSString *) clientId
    :(NSString *) apiKey
;

/*!
 @abstract send image for recognition
 @param jpgImage
 Image to send
 @param delegate
 NSURLConnection delegate.
 Use didReceiveData to handle received json message.
 
 Sample success message:
 {
    status: 0,
    id: "<ID>"
 }
 
 Sample error message:
 {
    status: 1,
    message: "Invalid message hash"
 }
 
 Sample no match message:
 {
    status: 2,
    message: "No match found"
 }
 
 */
-(void) send
    :(UIImage *) jpgImage
    :(id<NSURLConnectionDataDelegate>) delegate
;

@end
