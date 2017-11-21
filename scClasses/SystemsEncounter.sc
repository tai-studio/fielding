SystemsEncounter : Steno {
	var <definitions;
	*new { |numChannels = 2, expand = false, maxBracketDepth = 8, server|
		^super.new(
			numChannels, expand, maxBracketDepth, server ? Server.default
		).initDefs;
	}

	quelle { |name, func, multiChannelExpand, update = true, numChannels|
		definitions[name] = [\quelle, func, multiChannelExpand, update, numChannels];
		super.quelle(name, func, multiChannelExpand, update, numChannels)
	}
	filter { |name, func, multiChannelExpand, update = true, numChannels|
		definitions[name] = [\filter, func, multiChannelExpand, update, numChannels];
		super.filter(name, func, multiChannelExpand, update, numChannels)
	}

	getDef {|name|
		var type, func, multiChannelExpand, update, numChannels;

		definitions[name].notNil.if({
			#type, func, multiChannelExpand, update, numChannels = definitions[name];
		}, {
			"aeiou".includes(name.asString.first).if({
				type = \quelle;
			}, {
				type = \filter;
			});
			func = {|in, ctl|};
		});
		// ^(".%(\%, %, %, %, %)".format(
		// 	type,
		// 	name,
		// 	func.asCompileString,
		// 	multiChannelExpand.asCompileString,
		// 	update.asCompileString,
		// 	numChannels.asCompileString)
		// );
		^".%(\%, %)".format(
			type,
			name.asSymbol.asCompileString,
			func.asCompileString,
		);
	}

	initDefs {
		definitions = ();
	}


	// feedback variables

	declareFBVars { |names|
		names.do { |name|
			name = name.asSymbol;
			if(variables[name].isNil) {
				if(verbosity > 0) {
					"new FB variable as ".post;
				};
				this.filter(name, { |input, controls|
					// Bus declaration inside synth func restores busses with
					// correct channel numbers, e.g. when number of channels changed on the fly
					var bus = Bus.audio(server, numChannels).postln;
					var read = LinXFade2.ar(
						inA: In.ar(bus, numChannels),
						inB: Limiter.ar(InFeedback.ar(bus, numChannels), 8, 0.01),
						// only do InFeedback for first appearance of variable
						pan: (controls.feedback.abs * (controls.index < 1) * 2 - 1)
					) * controls.feedback.sign;

					// \assignment can be increased for feeding in more than one signal
					Out.ar(bus, input * (controls.index < \assignment.kr(1)));

					read * controls[\env] + input
				});
				variables[name] = bus;
			} {
				"Variable '%' already declared".format(name).warn;
			}
		}
	}

}
