require File.join(File.dirname(__FILE__), "../jars/vertx-core-1.3.0.final.jar")
require File.join(File.dirname(__FILE__), "../jars/netty-3.6.0.Beta1.jar")

require 'ext/jubilee.jar'
require 'rack'
require 'jubilee/const'
require 'jubilee/null_io'
require 'jubilee/runner'
require 'jubilee/server'
require 'rack/adapter/loader'