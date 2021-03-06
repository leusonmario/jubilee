package org.jruby.jubilee;

import org.jruby.*;
import org.jruby.anno.JRubyMethod;
import org.jruby.jubilee.vertx.JubileeVertx;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by isaiah on 23/01/2014.
 */
public class RubyPlatformManager extends RubyObject {
  private PlatformManager pm;

  public static void createPlatformManagerClass(Ruby runtime) {
    RubyModule mJubilee = runtime.defineModule("Jubilee");
    RubyClass serverClass = mJubilee.defineClassUnder("PlatformManager", runtime.getObject(), ALLOCATOR);
    serverClass.defineAnnotatedMethods(RubyPlatformManager.class);
  }

  private static ObjectAllocator ALLOCATOR = new ObjectAllocator() {
    public IRubyObject allocate(Ruby ruby, RubyClass rubyClass) {
      return new RubyPlatformManager(ruby, rubyClass);
    }
  };

  public RubyPlatformManager(Ruby ruby, RubyClass rubyClass) {
    super(ruby, rubyClass);
  }

  @JRubyMethod
  public IRubyObject initialize(ThreadContext context, IRubyObject config) {
    RubyHash options = config.convertToHash();
    Ruby runtime = context.runtime;
    RubySymbol clustered_k = runtime.newSymbol("clustered");
    RubySymbol cluster_host_k = runtime.newSymbol("cluster_host");
    RubySymbol cluster_port_k = runtime.newSymbol("cluster_port");
    if (options.containsKey(clustered_k) && options.op_aref(context, clustered_k).isTrue()) {
      int clusterPort = 0;
      String clusterHost = null;
      if (options.containsKey(cluster_port_k))
        clusterPort = RubyNumeric.num2int(options.op_aref(context, cluster_port_k));
      if (options.containsKey(cluster_host_k))
        clusterHost = options.op_aref(context, cluster_host_k).asJavaString();
      if (clusterHost == null) clusterHost = getDefaultAddress();
      pm = PlatformLocator.factory.createPlatformManager(clusterPort, clusterHost);
    } else {
      pm = PlatformLocator.factory.createPlatformManager();
    }
    JubileeVertx.init(pm.vertx());
    int ins = RubyNumeric.num2int(options.op_aref(context, RubySymbol.newSymbol(context.runtime, "instances")));
    pm.deployVerticle("org.jruby.jubilee.JubileeVerticle", new JsonObject(parseOptions(options)),
            context.runtime.getJRubyClassLoader().getURLs(), ins, null, new AsyncResultHandler<String>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.succeeded()) {
//          System.out.println("Deployment ID is " + result.result());
        } else{
          result.cause().printStackTrace();
        }
      }
    });
    return this;
  }

  @JRubyMethod
  public IRubyObject stop(ThreadContext context) {
    pm.stop();
    return context.runtime.getNil();
  }

  private Map<String, Object> parseOptions(RubyHash options) {
    Ruby runtime = options.getRuntime();
    ThreadContext context = runtime.getCurrentContext();
    RubySymbol port_k = runtime.newSymbol("Port");
    RubySymbol host_k = runtime.newSymbol("Host");
    RubySymbol ssl_k = runtime.newSymbol("ssl");
    RubySymbol rack_app_k = runtime.newSymbol("rackapp");
    RubySymbol rack_up_k = runtime.newSymbol("rackup");
    RubySymbol ssl_keystore_k = runtime.newSymbol("ssl_keystore");
    RubySymbol ssl_password_k = runtime.newSymbol("ssl_password");
    RubySymbol eventbus_prefix_k = runtime.newSymbol("eventbus_prefix");
    RubySymbol quiet_k = runtime.newSymbol("quiet");
    RubySymbol environment_k = runtime.newSymbol("environment");
    Map<String, Object> map = new HashMap<>();
    map.put("host", options.op_aref(context, host_k).asJavaString());
    map.put("port", RubyNumeric.num2int(options.op_aref(context, port_k)));

    if (options.has_key_p(rack_up_k).isTrue())
    map.put("rackup", options.op_aref(context, rack_up_k).asJavaString());
    if (options.has_key_p(rack_app_k).isTrue())
      map.put("rackapp", options.op_aref(context, rack_app_k));
    map.put("quiet", options.containsKey(quiet_k) && options.op_aref(context, quiet_k).isTrue());

    map.put("environment", options.op_aref(context, environment_k).asJavaString());

    boolean ssl = options.op_aref(context, ssl_k).isTrue();
    if (ssl) {
      map.put("keystore_path", options.op_aref(context, ssl_keystore_k).asJavaString());
      if (options.has_key_p(ssl_password_k).isTrue())
         map.put("keystore_password", options.op_aref(context, ssl_password_k).asJavaString());
    }
    map.put("ssl", ssl);
    if (options.has_key_p(eventbus_prefix_k).isTrue())
      map.put("event_bus", options.op_aref(context, eventbus_prefix_k).asJavaString());
    // This is a trick to put an Object into the config object
    map.put("ruby", runtime);
    return map;
  }
  /*
  Get default interface to use since the user hasn't specified one
   */
  private String getDefaultAddress() {
    Enumeration<NetworkInterface> nets;
    try {
      nets = NetworkInterface.getNetworkInterfaces();
    } catch (SocketException e) {
      return null;
    }
    NetworkInterface netinf;
    while (nets.hasMoreElements()) {
      netinf = nets.nextElement();

      Enumeration<InetAddress> addresses = netinf.getInetAddresses();

      while (addresses.hasMoreElements()) {
        InetAddress address = addresses.nextElement();
        if (!address.isAnyLocalAddress() && !address.isMulticastAddress()
                && !(address instanceof Inet6Address)) {
          return address.getHostAddress();
        }
      }
    }
    return null;
  }
}
