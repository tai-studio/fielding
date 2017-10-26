/////////////////////

Server.killAll;

/////////////////////

a = Server(\others, NetAddr("localhost", 57110));
a.options.protocol = \tcp;
a.options.maxLogins = 10;
a.boot;
a.scope

a.dumpOSC(0)



{SinOsc.ar}.play(a)
a.freeAll

OSCFunc.trace(false)


Server Command



n = NetAddr("localhost", 57110)
n.connect

a.addr.sendMsg(\notify, 1)


thisProcess.openPorts

thisProcess



///////////////// start alive /////////////

(
q = q ? ();
q.server = Server(\others, NetAddr("localhost", 57110), ServerOptions.new.protocol_(\tcp));
q.server.addr.connect;
q.server.startAliveThread(0);
q.server.doWhenBooted({ "Yo".postln; s.notify; s.initTree });
CmdPeriod.add({q.server.freeAll});
q.server.makeWindow;
)

q.server.scope

q.server.dumpOSC(1)
