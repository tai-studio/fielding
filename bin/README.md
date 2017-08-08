# Helper scripts

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