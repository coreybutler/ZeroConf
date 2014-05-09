# MDNS Android plugin for Cordova/Phonegap

mDNS (Multicast DNS) allows devices on a Local Area Network to broadcast/recognize services from other systems. It is compatible with
both ZeroConf and Bonjour, making it possible to detect mobile devices, computers, printer services, etc.

**Installation**

```sh
cordova plugin add https://github.com/coreybutler/mdns
```

**Origins & Use**

This plugin is a Cordova 3.0+ variation of the ZeroConf plugin created by [vstirbu](https://github.com/vstirbu/ZeroConf). There are some
subtle changes to the Java code, but the primary difference is the _how_ the JavaScript API is used.

## JavaScript Workflow (How To Use It)

The plugin creates a global JavaScript "singleton class", called `MDNS`, which acts like an event emitter.
There is no need to add an additional `<script></script>` block in your code.
Creating an `MDNS` object establishes a bridge to the mDNS monitor. The code can then process events/data emitted by the mDNS monitor.
For example:

```js
var mdns = new MDNS("_http._tcp.local.");  // Monitor HTTP/TCP network services

mdns.listen(function(data){
  switch(data.action){
    case "available":
      alert(data.service.name+" is available at "+data.service.addresses[0]+" on port "+data.service.port.toString());
      break;
    case "removed":
      ...
      break;
    case "resolved":
      ...
    default:
      alert("Unrecognized response: "+data.action);
  }
});
```

The code above listens for all devices broadcasting `_http._tcp._local.` (default value) services. The callback provided in the `listen()` method
handles every response, allowing the JavaScript application to handle all responses as it sees fit. The `data` argument of the callback will always have an `action`
attribute. The remainder of the payload depends on the action. Here's an example of how an action on a single service would look:

```js
{
  "service": {
    "port": 50930,
    "protocol": "tcp",
    "application": "http",
    "urls": ["http://192.168.2.2:50930", "http://fe80::7256:81ff:fe00:99e3:50930"],
    "description": "\\00",
    "name": "Black iPod",
    "domain": "local",
    "server": "",
    "addresses": ["192.168.2.2", "fe80::7256:81ff:fe00:99e3"],
    "type": "_http._tcp.local.",
    "qualifiedname": "Black iPod._http._tcp.local."
  },
  "action": "available"
}
```

### Alternate Workflow

If one big event handler callback isn't your style, MDNS offers an alternative approach mimicking an event emitter. There is an `on()` method that will only handle specific actions from thhe mDNS bridge. For example, if you only care about recognizing new services and removing old services, the following code could be used:

```js
var mdns = new MDNS("_http._tcp.local.");

mdns.on("available",function(service){
  alert(service.name+" is now available");
});

mdns.on("unavailable",function(service){
  alert(service.name+" is no longer available.");
});

mdns.listen();
```

There is also a `once()` method that will run a callback handler once and then destroy the handler (effectively only running it once). It is important to note that there is
no `off()` or `removeListener()`. When a handler is created, it remains on all the time, relying on the logic of the handler to execute/skip processing as necessary.

**Gotchas**

Notice that the event for identifying a service is called `available` and not `added`. This is a deliberate deviation from the original ZeroConf plugin naming convention.
The functionality is the same, but it was renamed to make more sense logically. Remote services can re-broadcast their availability. If a device re-broadcasts itself, it
will cause an event to be fired by the plugin. MDNS

## Commanding mDNS

The plugin also allows you to register/unregister services.


## Adding the Plugin to your project ##

In your application project directory:

```bash
cordova plugins add https://github.com/vstirbu/ZeroConf
```

## Using the plugin ##

There are five static methods on the ZeroConf object, as follows:

### `watch(type, callback)`
Note that `type` is a fully-qualified service type, including the domain, e.g. `"_http._tcp.local."`

`callback` is a function that is called when services are added and removed. The function is passed
an object with the following structure:

```javascript
{
  "service": {
    "port": 50930,
    "protocol": "tcp",
    "application": "http",
    "urls": ["http://192.168.2.2:50930", "http://fe80::7256:81ff:fe00:99e3:50930"],
    "description": "\\00",
    "name": "Black iPod",
    "domain": "local",
    "server": "",
    "addresses": ["192.168.2.2", "fe80::7256:81ff:fe00:99e3"],
    "type": "_http._tcp.local.",
    "qualifiedname": "Black iPod._http._tcp.local."
  },
  "action": "added"
}

```
For more information on the fields, see [the JmDNS docs](http://jmdns.sourceforge.net/apidocs/javax/jmdns/ServiceInfo.html).
If you edit ZeroConf.java, you can easily add more fields if you need them.

### `unwatch(type)`
Stops watching for services of the specified type.

### `close()`
Closes the service browser and stops watching.

### `register(type, name, port, text)`
Publishes a new service. The fields are as in the structure above. For more information,
see [the JmDNS docs](http://jmdns.sourceforge.net/apidocs/javax/jmdns/ServiceInfo.html).

### `unregister()`
Unregisters all published services.

## Credits

Original plugin [ZeroConf](https://github.com/purplecabbage/phonegap-plugins/tree/master/Android/ZeroConf) developed by [Matt Kane](https://github.com/ascorbic) / [Triggertrap Ltd](https://github.com/triggertrap).

It depends on [the JmDNS library](http://jmdns.sourceforge.net/). Bundles [the jmdns.jar](https://github.com/twitwi/AndroidDnssdDemo/) library.

## Licence ##

The MIT License
