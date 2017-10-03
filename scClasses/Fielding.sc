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
	var <steno, sens2paramSynth;
	var <alphabet;
	var <numSensors, <numParams, <numInfluences;
	var <allParamBus, <paramDirectBus, <paramLagBus, <paramTrigBus;


	*new{|addrs, numChannels = 2, numSensors = 8, numParams = 8, numInfluences = 3|
		^super.new.initFielding(addrs, numChannels, numSensors, numParams, numInfluences)
	}
	initFielding{|addrs, numChannels, argNumSensors, argNumParams, argNumInfluences|
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
		};

		steno = Steno(numChannels, true, server: this.bcServer);
		alphabet = (
			quellen: #[\a, \e, \i, \o, \u],
			filter:  #[\f, \l, \n, \g, \x],
			variablen: #[\0, \1, \2, \3],
			delays: #[\d]
		);

		// number of sensors
		numSensors = argNumSensors;
		// number of parameters available for synths
		numParams = argNumParams;
		// number of influences for each parameter
		numInfluences = argNumInfluences;

		allParamBus    = Bus.audio(this.bcServer, 3 * numParams);
		paramDirectBus = Bus.newFrom(allParamBus, 0 * numParams, numParams);
		paramLagBus    = Bus.newFrom(allParamBus, 1 * numParams, numParams);
		paramTrigBus   = Bus.newFrom(allParamBus, 2 * numParams, numParams);


	}
	sendSynths {|s|
		SynthDef(\fieldingAnalogIn, {|lagTime = 0.5, tThresh = 0.1|
			var bus = \bus.ir(0);

			var params, paramsLag, paramsTrig;

			var influenceIdx, influenceVals, parameterbuses;
			var sensors;

			sensors = s.isLocal.if({
				{LFNoise1.ar(Rand(0.1, 100))}!numSensors
			}, {
				AnalogIn.ar(Array.iota(numSensors))
			});

			influenceIdx = {
				{numSensors.rand}!numInfluences
			}!numParams;
			influenceVals = {
				{|i| 1.0.rand * ((i mod: 2) * 2-1)}!numInfluences
			}!numParams;

			params = influenceIdx.collect{|idxs, i|
				(influenceVals[i] * sensors[idxs]).sum
			};

			params = params.fold2(1);
			// params = params.tanh;
			paramsLag = params.lag(lagTime);
			// paramsLag = Median.ar(100, params);//.lag(lagTime);
			paramsTrig = Trig1.ar(HPF.ar(params, 100).abs - tThresh, 0.001) * paramsLag;

			ReplaceOut.ar(bus, params ++ paramsLag ++ paramsTrig);

		}).send(s);
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
				server.waitForBoot{
					this.sendSynths(server)
				};
			}, {
				server.startAliveThread(0);
				server.doWhenBooted({
					server.notify;
					server.initTree;
					this.sendSynths(server);
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
	sens2paramSynth {
		sens2paramSynth.isNil.if({
			sens2paramSynth = Synth(\fieldingAnalogIn, [\lagTime, 0.2, \tThresh, 0.1, \bus, allParamBus.index, \numParams, numParams], steno.group, \addBefore);
			CmdPeriod.add({sens2paramSynth = nil});
		});
		^sens2paramSynth
	}
	addrs {
		^servers.collect(_.addr)
	}
	queryAllNodes {
		servers.do{|s|
			s.queryAllNodes
		};
	}

	makeStatusWindow {
		var width = 400, elemExt, elemExtHalf, color, textView, randData, decorator;

		(statusWindow.notNil and: {statusWindow.isClosed.not}).if{^this};

		// pseudo randomness
		randData = thisThread.randData;
		thisThread.randSeed = 1979;

		statusWindow = Window("fielding", Rect(0, 0, width, 600)).decorate;
		decorator = statusWindow.view.decorator;
		elemExt     = (width - statusWindow.view.decorator.margin.x) - 10;
		elemExtHalf = (width/2 - statusWindow.view.decorator.margin.x - statusWindow.view.decorator.gap.x) - 5;

		TextField(statusWindow, (width - (2*statusWindow.view.decorator.margin.x))@40)
		.string_(steno.cmdLine)
		.keyUpAction_{|me| steno.value(me.value.asString.postln) }
		.font_(Font(Font.defaultMonoFace, 18));
		decorator.nextLine;

		EZSmoothSlider(
			statusWindow,
			elemExtHalf@20,
			\in_Lag,
			[0, 2].asSpec,
			{|me| sens2paramSynth.set(\lagTime, me.value)},
			0
		);
		EZSmoothSlider(
			statusWindow,
			elemExtHalf@20,
			\in_Thr,
			[0, 0.5].asSpec,
			{|me| sens2paramSynth.set(\tThresh, me.value)}
		);
		decorator.nextLine;

		(alphabet.quellen ++ alphabet.filter).do{|c|
			color = Color.rand.alpha_(0.5);
			EZSmoothSlider(
				statusWindow,
				elemExt@20,
				"%_MX".format(c).asSymbol,
				[ 0, 1].asSpec,
				{|me| steno.set(c,      \mix, me.value)},
				1
			).setColors(hiliteColor: color);
			decorator.nextLine;
		};

		(alphabet.variablen).do{|c|
			color = Color.rand.alpha_(0.5);
			EZSmoothSlider(
				statusWindow,
				elemExt@20,
				"%_MF".format(c).asSymbol,
				[ -1, 1].asSpec,
				{|me| steno.set(c, \mix, 1/(me.value.abs+1), \feedback, me.value)},
				0
			).setColors(hiliteColor: color);
			decorator.nextLine;
		};
		(alphabet.delays).do{|c|
			color = Color.rand.alpha_(0.5);
			EZSmoothSlider(
				statusWindow,
				elemExtHalf@20,
				"%_MX".format(c).asSymbol,
				[ 0, 1].asSpec,
				{|me| steno.set(c, \mix, me.value)},
				0
			).setColors(hiliteColor: color);
			EZSmoothSlider(
				statusWindow,
				elemExtHalf@20,
				"%_DT".format(c).asSymbol,
				[ 0, 0.1].asSpec,
				{|me| steno.set(c, \dt, me.value)},
				0
			).setColors(hiliteColor: color);
			decorator.nextLine;
		};

		decorator.nextLine;

		// server status
		servers.do{|server|
			server.makeView(statusWindow)
		};
		statusWindow.front;

		// pseudo randomness
		thisThread.randData = randData;
	}

	getParam {|ctl, idx, processed|
		var bus = switch (processed,
			\none, {paramDirectBus},
			\lag , {paramLagBus},
			\trig, {paramTrigBus}
		);

		^In.ar(bus.index + ((ctl.index + idx)% bus.numChannels))
	}

	quelle { |name, func, multiChannelExpand, update = true, numChannels|
		steno.quelle(name, func, multiChannelExpand, update, numChannels)
	}

	filter { |name, func, multiChannelExpand, update = true, numChannels|
		steno.quelle(name, func, multiChannelExpand, update, numChannels)
	}

	declareVariables { |names|
		steno.declareVariables(names)
	}
}
