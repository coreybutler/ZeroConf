#include <sys/types.h>
#include <sys/sysctl.h>

#import <Cordova/CDV.h>
#import "CDVMDNS.h"

@interface CDVMDNS () {}
@end

@implementation CDVMDNS

- (void)identify:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


@end
