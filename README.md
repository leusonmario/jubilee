[![Build Status](https://travis-ci.org/isaiah/jubilee.png?branch=master)](https://travis-ci.org/isaiah/jubilee)

Jubilee server
=========
 
> "We need a web framework for Vertx.", you said.

> "But why not use Vertx in your Rails applications, it's the most productive web framework ever created."

The Answer is Jubilee, a rack server with [vertx 2.0](http://vertx.io) awesomeness builtin. Check out the
[demo](http://192.241.201.68:8080/) [application](https://github.com/isaiah/jubilee/tree/master/examples/chatapp).

Why another rack server?
------------------------

> "Vert.x is a lightweight, high performance application platform for the JVM
> that's designed for modern mobile, web, and enterprise applications."
>      - vertx.io site

By using Vertx, jubilee inherent advantages in terms of performance, and all
the other cool features of Vertx:

* [EventBus](https://github.com/isaiah/jubilee/wiki/Event-Bus)
* [SharedData](https://github.com/isaiah/jubilee/wiki/SharedData)
* [Clustering](https://github.com/isaiah/jubilee/wiki/Clustering)



Installation
------------

    $ jruby -S gem install jubilee

Jubilee requires JRuby 1.7.5 or later, and JDK 7+

Build from source
-----------------

Checkout the source and run the following command in the root directory of the
project:

```shell
bundle && bundle exec rake install
```

Get started
-----------

    $ cd a-rack-app
    $ jruby -S jubilee

Setup
-----

If you use bundler, you might want to add `jubilee` to your Gemfile,
this is also required if you want use rails http streaming,

    $ jubilee

or if you prefer to use the rack handler(e.g. development) use:

    $ rails s jubilee

or

    $ rackup -s jubilee

Event Bus
=========

Event Bus is a pub/sub mechanism, it can be used from server to server, server
to client and client to client, with the same API! You can use it to build
living real time web application.

Examples
--------

Assume necessary javascript files are loaded in the page (they can be found [here](https://github.com/isaiah/jubilee/tree/master/examples/client)),
start jubilee in a rack application with:

```
$ jubilee --eventbus /eventbus
```

In one browser:

```javascript
var eb = new vertx.EventBus("/eventbus");
eb.registerHandler("test", function(data){
  console.info(data);
});

```

In another:

```javascript
var eb = new vertx.EventBus("/eventbus");
eb.send("test", "hello, world");
```

In the previous tab it should print the greetings you just sent.

For more advanced examples, please checkout the
[chatapp](https://github.com/isaiah/jubilee/tree/master/examples/chatapp).

Performance Tuning
-------------------

If you're creating a lot of connections to a Jubilee(Vert.x) server in a short
period of time, e.g. benchmarking with tools like [wrk](https://github.com/wg/wrk),
you may need to tweak some settings in order to avoid the TCP accept queue
getting full. This can result in connections being refused or packets being
dropped during the handshake which can then cause the client to retry.

A classic symptom of this is if you see long connection times just over
3000ms at your client.

How to tune this is operating system specific but in Linux you need to
increase a couple of settings in the TCP / Net config (10000 is an
arbitrarily large number)

```shell
sudo sysctl -w net.core.somaxconn=10000
sudo sysctl -w net.ipv4.tcp_max_syn_backlog=10000
```

For other operating systems, please consult your operating system
documentation.

Contributing
-------------

All kinds of contributions are welcome.

File an issue [here](https://github.com/isaiah/jubilee/issues) if you encounter any problems. Or if you prefer to fix by yourself:

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

License
--------

See [LICENSE.txt](https://github.com/isaiah/jubilee/blob/master/LICENSE.txt)

Acknowledgment
--------------

YourKit is kindly supporting Jubilee Server with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:
[YourKit Java
Profiler](http://www.yourkit.com/java/profiler/index.jsp) and
[YourKit .NET Profiler](http://www.yourkit.com/.net/profiler/index.jsp).
