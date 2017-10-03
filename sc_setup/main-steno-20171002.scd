(
q = q ? ();

q.f = Fielding((
	// \b0: NetAddr("192.168.23.10", 57110),
	// \b1: NetAddr("192.168.23.11", 57110),
	\i0: NetAddr("localhost"    , 57110),
));
q.f.bootServers;
Server.default = s = q.f.bcServer;

q.f.makeStatusWindow;

)

q.f.steno.busIndices
q.f.steno.tailBus
q.f.queryAllNodes


q.f.allParamBus.scope




s.scope

(
q.f.steno.push;
q.f.steno.verbosity = 0;
q.f.steno.setGlobal(\assignment, 2);
q.f.steno.setGlobal(\fadeTime, 1, \attack, 1);

q.f.quelle(\u, {|in, ctl|
	Trig.ar(q.f.getParam(ctl, 0, \trig).abs.clip(0, 1) - 0.001, 0.00000001)!2;
});

q.f.quelle(\e, {|in, ctl|
	var freq  = q.f.getParam(ctl, 0, \lag).linexp(-1, 1, 100 + (ctl.index * 100), 200 + (ctl.index * 200));
	var phase = q.f.getParam(ctl, 1, \none);
	var trig  = q.f.getParam(ctl, 2, \trig);

	SinOsc.ar(freq, phase) * Decay.ar(trig, Latch.ar(trig, trig) * 0.1 + 0.01)

});

q.f.quelle(\a, {|in, ctl|
	var freq  = q.f.getParam(ctl, 0, \lag).linexp(-1, 1, 500, 8000);
	var width = q.f.getParam(ctl, 1, \none);
	var trig  = q.f.getParam(ctl, 2, \trig);

	Pulse.ar(freq, width) * Decay.ar(trig, Latch.ar(trig, trig) * 0.1)

});


q.f.quelle(\o, {|in, ctl|
	var freq  = q.f.getParam(ctl, [0, 1], \lag).linexp(-1, 1, 500, 800);
	var fb    = q.f.getParam(ctl, 2, \lag).linexp(-1, 1, 0, 0.4);
	var amp    = q.f.getParam(ctl, [3, 4], \lag).linlin(-1, 1, 0, 0.1);
	SinOscFB.ar(freq, fb)

});



q.f.filter(\f, {|in, ctl|
	var freq  = q.f.getParam(ctl, [0,1], \lag).linexp(-1, 1, 500, 1000);
	var rq    = q.f.getParam(ctl, 3, \lag).linlin( -1, 1, 0.3, 2);

	RLPF.ar(in.reverse, freq, rq);
});

q.f.filter(\d, {|in, ctl|
	// var dt = q.f.getParam(ctl, [0, 1], \lag).linlin(-1, 1, 0, 0.1);
	var dt = \dt.kr(0.1)!2;
	DelayL.ar(in.reverse, 0.1, dt)
});

q.f.steno.quelle(\i, {|in, ctl|
	SoundIn.ar([0, 1])
});

q.f.declareVariables([\0, \1, \2, \3])
)
q.f.sens2paramSynth

-- e

-- 0eeed0 1afd1 a


s.scope

q.f.allParamBus

///////////

q.f.steno.set(\0, \feedback, -0.0, \mix, 1, \assignment, 1);
q.f.steno.set(\1, \feedback, -0.0, \mix, 1, \assignment, 1);

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
