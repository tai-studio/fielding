/*
MIT License

Copyright (c) 2017 Till Bovermann

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

Fielding {
	var <servers, bcServer;
	var <statusWindow;

	*new{|addrs|
		^super.new.initFielding(addrs)
	}
	initFielding{|addrs|
		servers = addrs.collect{|n, key|
			var options = n.isLocal.if({
				ServerOptions.new
				.maxLogins_(10)
				.numAudioBusChannels_(4096)
			}, {
				ServerOptions.new
				.protocol_(\tcp)
				// .numAudioBusChannels_(4096)
			});
			Server(key, n, options)
		}
	}
	connect {
		var addr;
		servers.do{|server|
			addr = server.addr;
			(server.options.protocol == \udp).if({
				"%: local address, using udp".format(addr).inform
			},{
				addr.tryConnectTCP({
					"%: connected".format(addr).inform
				}, {
					"%: failed to connect".format(addr).inform
				})
			})
		}
	}
	bootServers {
		this.connect;
		servers.do{|server|
			server.isLocal.if({
				server.boot;
			}, {
				server.startAliveThread(0);
				server.doWhenBooted({
					server.notify;
					server.initTree
				});
			})
			// s.latency = nil;
		};
		CmdPeriod.add({servers.do(_.freeAll)});
	}
	bcServer {
		bcServer.isNil.if({
			bcServer = BroadcastServer.for(servers.detect(_.isLocal), this.addrs.asArray );
		});
		^bcServer
	}
	addrs {
		^servers.collect(_.addr)
	}
	makeStatusWindow {
		(statusWindow.isNil or: {statusWindow.isClosed}).if{
			statusWindow = Window.new.decorate;
			servers.do{|server|
				server.makeView(statusWindow)
			};
			statusWindow.front
		}
	}
	queryAllNodes {
		servers.do{|s|
			s.queryAllNodes
		};
	}

}
