/**
 * ZeroConf plugin for Cordova/Phonegap
 *
 * Copyright (c) 2013-2014 Vlad Stirbu <vlad.stirbu@ieee.org>
 * Converted to Cordova 3.x
 * Refactored initialization
 * MIT License
 *
 * @author Matt Kane
 * Copyright (c) Triggertrap Ltd. 2012. All Rights Reserved.
 * Available under the terms of the MIT License.
 *
 */

package com.ecor;

import java.io.IOException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.util.Log;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class MDNS extends CordovaPlugin {
  WifiManager.MulticastLock lock;

  private String TAG = "MDNS";
  private JmDNS jmdns;
  private ServiceListener listener;
  private CallbackContext callback;
  private InetAddress addr;
  private ServiceInfo[] services;

  private static interface Response {
    public static final String ADDED = "added";
    public static final String REMOVED = "removed";
    public static final String RESOLVED = "resolved";
  }

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    WifiManager wifi = (WifiManager) this.cordova.getActivity()
        .getSystemService(android.content.Context.WIFI_SERVICE);
    lock = wifi.createMulticastLock(TAG+"PluginLock");
    lock.setReferenceCounted(true);
    lock.acquire();

    WifiInfo wifiinfo = wifi.getConnectionInfo();
    int intaddr = wifiinfo.getIpAddress();

    byte[] byteaddr = new byte[] { (byte) (intaddr & 0xff), (byte) (intaddr >> 8 & 0xff), (byte) (intaddr >> 16 & 0xff), (byte) (intaddr >> 24 & 0xff) };

    try {
      addr = InetAddress.getByAddress(byteaddr);
    } catch (Exception e) {
      e.printStackTrace();
    }

    Log.v(TAG, "Initialized");
  }

  @Override
  public boolean execute(String action, JSONArray args,
      CallbackContext callbackContext) {
    this.callback = callbackContext;

    Log.d(TAG,"Action called: "+action);
    if (action.equals("monitor")) {
      final String type = args.optString(0);
      if (type != null) {
        Log.d(TAG,"Monitor type: "+type);
        cordova.getThreadPool().execute(new Runnable() {
          public void run() {
            watch(type); // Thread-safe.
          }
        });
      } else {
        callbackContext.error("Service type not specified.");
        return false;
      }
    } else if (action.equals("register")) {
      JSONObject obj = args.optJSONObject(0);
      if (obj != null) {
        final String type = obj.optString("type");
        final String name = obj.optString("name");
        final int port = obj.optInt("port");
        final String text = obj.optString("text");
        if (type == null) {
          callbackContext.error("Missing required service info.");
          return false;
        }
        cordova.getThreadPool().execute(new Runnable() {
          public void run() {
            register(type, name, port, text);
          }
        });
      } else {
        callbackContext.error("Missing required service info.");
        return false;

      }
    } else if (action.equals("close")) {
      if (jmdns != null) {
        try {
          jmdns.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } else if (action.equals("unregister")) {
      if (jmdns != null) {
        jmdns.unregisterAllServices();
      }

    } else {
      Log.e(TAG, "Invalid action: " + action);
      callbackContext.error("Invalid action.");
      return false;
    }
    PluginResult result = new PluginResult(Status.NO_RESULT);
    result.setKeepCallback(true);
    // return result;
    return true;
  }

  private void watch(String type) {
    if (jmdns == null) {
      setupWatcher();
    }

    // Create a timer to list services every second
    Timer timer = new Timer();
    final String t = type;
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        services = jmdns.list(t);
      }
    }, 0, 1000);


    Log.d(TAG, "Watch " + type);
    Log.d(TAG, "Name: " + jmdns.getName() + " host: " + jmdns.getHostName());
    jmdns.addServiceListener(type, listener);
  }

//  private void unwatch(String type) {
//    if (jmdns == null) {
//      return;
//    }
//    jmdns.removeServiceListener(type, listener);
//  }

  private void register(String type, String name, int port, String text) {
    if (name == null) {
      name = "";
    }

    if (text == null) {
      text = "";
    }

    try {
      ServiceInfo service = ServiceInfo.create(type, name, port, text);
      jmdns.registerService(service);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void setupWatcher() {
    Log.d(TAG, "Setup watcher");
    try {
      jmdns = JmDNS.create(addr);
      listener = new ServiceListener() {

        public void serviceResolved(ServiceEvent ev) {
          Log.d(TAG, Response.RESOLVED);

          sendCallback(Response.ADDED, ev.getInfo());
        }

        public void serviceRemoved(ServiceEvent ev) {
          Log.d(TAG, Response.REMOVED);

          sendCallback(Response.REMOVED, ev.getInfo());
        }

        public void serviceAdded(ServiceEvent event) {
          Log.d(TAG, Response.ADDED);

          // Force serviceResolved to be called again
          jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
        }
      };

    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
  }

  public void sendCallback(String action, ServiceInfo info) {
    JSONObject status = new JSONObject();
    try {
      status.put("action", action);
      status.put("service", jsonifyService(info));
      Log.d(TAG, "Sending result: " + status.toString());

      PluginResult result = new PluginResult(PluginResult.Status.OK,status);

      result.setKeepCallback(true);
      callback.sendPluginResult(result);
//      this.callback.success(status);

    } catch (JSONException e) {

      e.printStackTrace();
    }

  }

  public static JSONObject jsonifyService(ServiceInfo info) {
    JSONObject obj = new JSONObject();
    try {
      obj.put("application", info.getApplication());
      obj.put("domain", info.getDomain());
      obj.put("port", info.getPort());
      obj.put("name", info.getName());
      obj.put("server", info.getServer());
      obj.put("description", info.getNiceTextString());
      obj.put("protocol", info.getProtocol());
      obj.put("qualifiedname", info.getQualifiedName());
      obj.put("type", info.getType());

      JSONArray addresses = new JSONArray();
      String[] add = info.getHostAddresses();
      for (int i = 0; i < add.length; i++) {
        addresses.put(add[i]);
      }
      obj.put("addresses", addresses);
      JSONArray urls = new JSONArray();

      String[] url = info.getURLs();
      for (int i = 0; i < url.length; i++) {
        urls.put(url[i]);
      }
      obj.put("urls", urls);

    } catch (JSONException e) {
      e.printStackTrace();
      return null;
    }

    return obj;

  }

}
