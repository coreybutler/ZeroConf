package com.ecor;

import java.util.ArrayList;
import javax.jmdns.ServiceInfo;
import android.util.Log;
import org.json.JSONArray;
import java.security.MessageDigest;

public class MdnsServices extends Object {

  private ArrayList<ServiceInfo> services = new ArrayList<ServiceInfo>();
  private String TAG = "MDNS Services";
  private String type;

  public MdnsServices(){
    Log.i(TAG,"Initialized");
    type = "_http._tcp.";
  }

  public MdnsServices(String in_type) {
    Log.i(TAG,"Initialized for "+in_type);
    type = in_type;
  }

  public void setType(String in_type) {
    Log.d(TAG,"Set type to "+in_type);
    type = in_type;
  }

  public String getType() {
    return type;
  }

  public void add(ServiceInfo info){
    Log.d(TAG,"Attempting to add service of type "+info.getType()+" in a "+type+" collection.");
    if (info.getType().contains(type)){
      services.add(info);
      Log.d(TAG,"Added "+info.getName());
    }
  }

  public void remove(ServiceInfo info){
    if (contains(info)){
      Log.d(TAG,"Removing "+info.getName()+" at index "+getIndexOf(info));
      services.remove(getIndexOf(info));
    }
  }

  public boolean contains(ServiceInfo info){
    Log.d(TAG,"Checking to see if object exists in collection.");
    return services.contains(info);
  }

  public boolean contains(String id){
    for (ServiceInfo i : services){
      if (ServiceMD5(i).equals(id)){
        return true;
      }
    }
    return false;
  }

  public int getCount() {
    if (services == null){
      Log.d(TAG,"No records in the services ArrayList.");
      return 0;
    }
    return services.size();
  }

  public int getIndexOf(ServiceInfo svc) {
    int x = -1;
    for (ServiceInfo i : services) {
      x++;
      if (ServiceMD5(i).equals(ServiceMD5(svc))){
        break;
      }
    }
    return x;
  }

  private static String ServiceMD5(ServiceInfo info) {
    JSONArray addresses = new JSONArray();
    String[] add = info.getHostAddresses();
    for (int i = 0; i < add.length; i++) {
    addresses.put(add[i]);
    }
    try {
      String raw = info.getType()+addresses.toString()+Integer.toString(info.getPort())+info.getQualifiedName();
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(raw.getBytes("UTF-8"));
      byte[] digest = md.digest();
      StringBuffer hexString = new StringBuffer();
      for (int i=0;i<digest.length;i++) {
        String hex=Integer.toHexString(0xff & digest[i]);
        if(hex.length()==1) hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (Exception e) {
    e.printStackTrace();
    return null;
    }
  }
}
