SystemsEncounter : Steno {
	var <definitions, window, <cmdView;
	var <>numKrs = 8;
	var <>defaultDefPath;
	var <>myKeys = "aeioufldngx";
	*new { |numChannels = 2, expand = false, maxBracketDepth = 8, server, defaultDefPath|
		^super.new(
			numChannels, expand, maxBracketDepth, server ? Server.default
		).initDefs(defaultDefPath);
	}

	quelle { |name, func, multiChannelExpand, update = true, numChannels|
		definitions[name] = [\quelle, func, multiChannelExpand, update, numChannels];
		super.quelle(name, func, multiChannelExpand, update, numChannels)
	}
	filter { |name, func, multiChannelExpand, update = true, numChannels|
		definitions[name] = [\filter, func, multiChannelExpand, update, numChannels];
		super.filter(name, func, multiChannelExpand, update, numChannels)
	}

	value {|string|
		cmdView.notNil.if{
			cmdView.string = string;
		};
		super.value(string);
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

	writeDefs {|path, varName = "t", comment|
		var mixs, defs, file;

		path.isNil.if{
			// write to default location
			path = defaultDefPath +/+ "%-defs_%.scd".format(this.class, Date.getDate.asSortableString)
		};


		// defs.postln;
		file = File(path,"w");
		file.write("// %\n//\n".format(comment));
		file.write("// %\n\n".format(this.rawCmdLine));
		file.write("%.value(\"%\");\n\n".format(varName, this.rawCmdLine ? ""));

		// get all mix params
		mixs = myKeys.as(Array).collect(_.asSymbol).collect{|c|
			var mix = this.settings.synthSettings[c].mix;
			"%.set(\\mix, %);".format(varName, mix)
		};

		file.write("\n\n");

		mixs.do{|d|
			file.write(d);
			file.write("\n");
		};

		/// get all definitions
		defs = myKeys.as(Array).collect{|c|
			"%%;".format(varName, this.getDef(c.asSymbol).asString)
		};
		defs.do{|d|
			file.write(d);
			file.write("\n");
		};
		file.close;
		"SystemsEncounter: written defs to %".format(path).inform;

	}
	initDefs {|argDefaultDefPath|
		definitions = ();
		defaultDefPath = argDefaultDefPath ?? {"~/Desktop".standardizePath};


		this.addSynthDef(\monitor, { |out, in, amp = 0.1, level = 0.9|
			Out.ar(out,
				Limiter.ar(
					LeakDC.ar(In.ar(in, numChannels)) * amp.lag(0.1),
					level,
					0.05
				)
			)
		}, force:true);

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

	////////////// kr-Input
	dynIn {|idx, numChan|
		var idxs = (idx + Array.iota(numChan))%numKrs;
		^In.kr(idxs)
	}

	////////////// GUI

	window {
		var codeView, width = 425, height = 750, elemExt, elemExtHalf, textView, randData, decorator;

		(window.notNil and: {window.isClosed.not}).if{^this};

		// pseudo randomness
		randData = thisThread.randData;
		thisThread.randSeed = 2017;

		window = Window(
			"systems ∿ encounter",
			Rect(0, 0, width, height),
			false
		).decorate;
		decorator   = window.view.decorator;
		elemExt     = (width - window.view.decorator.margin.x) - 30;
		elemExtHalf = (width/2 - window.view.decorator.margin.x - window.view.decorator.gap.x) - 5;

		cmdView = TextView(window, (width - (2*window.view.decorator.margin.x))@40)
		.string_(this.rawCmdLine)
		.keyDownAction_{|me, c ... f|
			(c == $().if{
				(me.selectionSize == 0).if({
					me.selectedString = "()";
				}, {
					me.selectedString = "(" ++ me.selectedString ++ ")";
				});
				me.editable = false;
			};
			(c == ${).if{
				(me.selectionSize == 0).if({
					me.selectedString = "{}";
				}, {
					me.selectedString = "{" ++ me.selectedString ++ "}";
				});
				me.editable = false;
			};
			(c == $[).if{
				(me.selectionSize == 0).if({
					me.selectedString = "[]";
				}, {
					me.selectedString = "[" ++ me.selectedString ++ "]";
				});
				me.editable = false;
			};

			// prevent newline
			(f.last == 16777220).if{
				me.editable = false;
			}
		}
		.keyUpAction_{|me, c ... f|
			// print, backspace or delete
			(c.isPrint || #[16777219, 16777223].includes(f.last)).if{
				super.value(me.string.asString);
			};
			myKeys.includes(c).if{
				codeView.string_("t%".format(this.getDef(c.asSymbol)))
			};
			me.editable = true;
		}
		.font_(Font(Font.defaultMonoFace, 18));
		decorator.nextLine;

		codeView = TextView(window, (width - (2*window.view.decorator.margin.x))@200)
		.enterInterpretsSelection_(false)
		.font_(Font(Font.defaultMonoFace, 12))
		.keyDownAction_{|view ... b|
			(b.last == 16777220 and: {b[1] == 524288 or: {b[1] == 131072}}).if{
				view.string.interpret;
			}
		}
		.tabWidth_(21);
		decorator.nextLine;

		// set general amplitude
		"v".do{|c|
			var color = Color.red;
			EZSmoothSlider(
				window,
				(width - (2*window.view.decorator.margin.x))@20,
				// "%".format(c).asSymbol,
				nil,
				[ 0, 1].asSpec,
				{|me| this.monitor.set(\amp, me.value)}
			)
			.setColors(hiliteColor: color);
		};
		decorator.nextLine;
		decorator.nextLine;

		myKeys.do{|c|
			var color = Color.gray(rrand(0.5, 0.8)).alpha_(0.5);
			var displayFunc = {
				codeView
				.string_("t%".format(this.getDef(c.asSymbol)))
				// .syntaxColorize;
				//.stringColor_(color.copy.alpha_(1));
			};
			var button = SmoothButton(window, 20@20)
			.states_([[ c.asString ]] )
			.action_(displayFunc)
			.background_(color);
			var slider = EZSmoothSlider(
				window,
				elemExt@20,
				// "".format(c).asString,
				nil,
				[ 0, 1].asSpec,
				{|me| this.set(c.asSymbol,      \mix, me.value)},
				this.get(c.asSymbol, \mix) ? 0
			)
			.setColors(hiliteColor: color);
			slider.sliderView.mouseDownAction_(displayFunc);
			slider.numberView.mouseDownAction_(displayFunc);
			decorator.nextLine;
		};
		"0123".do{|c|
			var color = Color.rand.alpha_(0.5);
			SmoothButton(window, 20@20)
			.states_([[ c.asString ]] )
			// .action_({ this. })
			.background_(color);
			EZSmoothSlider(
				window,
				elemExt@20,
				// "%".format(c).asSymbol,
				nil,
				[ -1, 1].asSpec,
				{|me| this.set(c, \mix, 1/(me.value.abs+1), \feedback, me.value)},
				this.get(c.asSymbol, \feedback)
			)
			.setColors(hiliteColor: color);
			decorator.nextLine;
		};

		decorator.nextLine;
		"f".do{|c|
			var color = Color.blue;
			// write def file
			// description text
			var descTextView = TextView(window, elemExt@20);

			SmoothButton(window, 20@20)
			.states_([[ c.asString ]] )
			.action_({ this.writeDefs(comment: descTextView.string) })
			.background_(color);

			decorator.nextLine;
		};

		thisThread.randData = randData;
		// window.alwaysOnTop_(true);
		window.front;
	}

}
