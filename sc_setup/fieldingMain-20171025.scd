/*
Server.killAll
*/

(
q = q ? ();


// local server
a = Server(\i0, NetAddr("localhost", 57110));
a.options.protocol = \tcp;
a.options.numAudioBusChannels = 4096;

a.options.maxLogins = 10;
a.waitForBoot{
	q.f = Fielding((
		\i0: a,

		// keys for bela-based servers have to start with $b
		\b0: NetAddr("192.168.23.10", 57110),
		\b1: NetAddr("192.168.23.11", 57110),

		// other laptop-based servers
		// \e0: NetAddr("192.168.23.200", 57110)
	), 4);

	q.f.prepareServers;
	q.f.makeSteno;
	Server.default = s = q.f.bcServer;

	q.f.makeStatusWindow;
}
)


// OSCFunc.trace(false)
a.scope(4, rate: \control)

(
q.f.steno.push;
q.f.steno.verbosity = 0;
q.f.steno.setGlobal(\assignment, 2);
q.f.steno.setGlobal(\fadeTime, 0, \attack, 0);

q.f.quelle(\u, {|in, ctl|
	var freq  = q.f.getParam(ctl, [0, 1], \none).linexp(-1, 1, 1000, 16000);
	var pFreq  = q.f.getParam(ctl, [2, 3], \none).linexp(-1, 1, 10, 32) * LFNoise1.ar(0.01, 0.5, 0.8);

	var amp  = q.f.getParam(ctl, [1, 2], \none).linlin(-1, 1, 0, 1);
	LFTri.ar(freq) * (amp-RMS.ar(in, 0.1)) * LFPulse.ar(pFreq, amp, 0.1)
});

q.f.quelle(\e, {|in, ctl|
	var freq  = q.f.getParam(ctl, 0, \lag).linexp(-1, 1, 500 + (ctl.index * 100), 200 + (ctl.index * 200));
	var phase = q.f.getParam(ctl, 1, \none);
	var trig  = q.f.getParam(ctl, 2, \trig);

	SinOsc.ar(freq, phase) * Decay.ar(trig, Latch.ar(trig, trig) * 0.1 + 0.01)

});


q.f.quelle(\a, {|in, ctl|
	var freq  = q.f.getParam(ctl, 0, \lag).linexp(-1, 1, 500, 8000);
	var width = q.f.getParam(ctl, 1, \none);
	var trig  = q.f.getParam(ctl, 2, \trig);

	Pulse.ar(freq, width) * Decay.kr(A2K.kr(trig), Latch.kr(trig, trig) * 0.0001)

});

q.f.quelle(\i, {|in, ctl|
	// var freq  = q.f.getParam(ctl, 0, \lag).linexp(-1, 1, 500, 8000);
	var width = q.f.getParam(ctl, 1, \none);
	var trig  = q.f.getParam(ctl, [0, 1], \trig);

	trig;// * Latch.ar(trig, trig)

});

q.f.quelle(\o, {|in, ctl|
	var freq  = q.f.getParam(ctl, [0, 1], \lag).linexp(-1, 1, 500, 1000);
	var freq2  = q.f.getParam(ctl, 3, \none).linexp(-1, 1, 10, 100);
	var fb    = q.f.getParam(ctl, 2, \lag).linlin(-1, 1, 0.1, 2);
	var amp    = q.f.getParam(ctl, 1, \none).linlin(-1, 1, 0, 1);
	SinOscFB.ar(SinOsc.ar(freq).range(1000, freq2), fb)

});



q.f.filter(\f, {|in, ctl|
	var freq  = q.f.getParam(ctl, [0,1], \lag).linexp(-1, 1, 500, 1000);
	var rq    = q.f.getParam(ctl, 3, \lag).linlin( -1, 1, 0.3, 2);

	RHPF.ar(in.reverse, freq, rq);
});

q.f.filter(\g, {|in, ctl|
	var freq  = q.f.getParam(ctl, [0,1], \lag).linexp(-1, 1, 500, 1000);
	var rq    = q.f.getParam(ctl, 3, \lag).linlin( -1, 1, 0.3, 2);

	RLPF.ar(in.reverse, freq, rq);
});

q.f.filter(\l, {|in, ctl|
	// var freq  = q.f.getParam(ctl, [0,1], \lag).linexp(-1, 1, 500, 1000);
	var dc = q.f.getParam(ctl, [0, 1], \lag).linlin(-1, 1, 0, 0.4);
	var dt = RMS.ar(in, 100).lag(0.1) * 0.1;
	// var rq    = q.f.getParam(ctl, 3, \lag).linlin( -1, 1, 0.3, 2);

	CombN.ar(in.reverse, 0.1, dt, dc);
});

q.f.filter(\n, {|in, ctl|
	var freq  = q.f.getParam(ctl, [0, 3], \lag).linexp(-1, 1, 0.1 + (ctl.index), 0.2 + (ctl.index * 2));

	in * LFNoise1.ar(freq, Rand())

});


q.f.filter(\d, {|in, ctl|
	var dt = q.f.getParam(ctl, [0, 1], \lag).linlin(-1, 1, 0.01, 0.1);
	// var dt = \dt.kr(0.1)!2;
	DelayN.ar(in.reverse, 0.1, dt)
});

q.f.declareVariables([\0, \1, \2, \3])
)

// send parameter synth
q.f.sens2paramSynth




q.f.queryAllNodes




////////////////////////////


0ud0


-- e

-- 0eed0

q.f.servers.i0.dumpOSC(0)



-- 0udaa0 0aaoa
-- 0udaf0

a.scope

q.f.allParamBus

///////////

q.f.steno.set(\0, \assignment, 2);
q.f.steno.set(\1, \assignment, 2);
q.f.steno.set(\2, \assignment, 2);
q.f.steno.setGlobal(\assignment, 2);

q.f.steno.set(\0, \feedback, -0.7, \mix, 0.9, \assignment, 1)
q.f.steno.set(\1, \feedback, -0.2, \mix, 0.7, \assignment, 1)
q.f.steno.set(\1, \feedback, -0.8, \mix, 0.3, \assignment, 1)


s.queryAllNodes
q.f.steno.set(\0, \feedback, -0.4, \mix, 0.2, \assignment, 3)

q.f.steno.set(\0, \feedback, -0.27, \mix, 0.99, \assignment, 1)
q.f.steno.set(\0, \feedback, -0.84, \mix, 0.5, \assignment, 2)
q.f.steno.set(\1, \feedback, -0.08, \mix, 0.6, \assignment,1)
q.f.steno.set(\1, \feedback, 0.68, \mix, 0.8, \assignment, 2)

q.f.steno.set(\d, \delay, 0.000101)
q.f.steno.set(\d, \delay, 0.02)
q.f.steno.set(\d, \delay, 0.08, \mix, 1)
q.f.steno.set(\d, \delay, 0.1, \mix, 0.3)
q.f.steno.set(\d, \delay, 0.01, \mix, 0.9)
q.f.steno.set(\d, \delay, {exprand(0.001, 0.1)})

t.value


q.f.servers.b1.sendMsg(15, 1002, "tThresh", 0.00615942)
