# Helper scripts

## network config (as reserved in TP-link AC750 router)

box1       08-02-8E-8E-78-7D   192.168.0.10
box2       08-02-8E-9A-09-05   192.168.0.11
box3       8C-3B-AD-1F-F8-C4   192.168.0.12

hcl        8C-85-90-D3-8D-5D   192.168.0.2
katha      5C-96-9D-77-36-73   192.168.0.34

x201       00-24-D7-4B-7C-A8   192.168.0.201 
e130       84-A6-C8-C3-E6-46   192.168.0.130 
e131       00-C2-C6-5A-64-F5   192.168.0.131
## `fielding`

remote-control BELA boards connected either over USB or a standard network connection like WiFi or Ethernet. 

Current features are

+ send commands to one or many boards at the same time 
+ send and execute a shell command
+ get information on current status
+ set date either from local date or via ntp
+ start scsynth process
+ start and stop BELA IDE (survives reboot)
+ halt board

written in ruby 2.1.2 (standard OSX ruby) but should work on more recent versions, too. It uses the awesome [Trollop](http://manageiq.github.io/trollop/) cli option parser (included in `lib`).
Since `fielding` makes extensive use of `ssh`, you may want to set up your ssh configuration to [use public key authentification](http://linuxproblem.org/art_9.html).

