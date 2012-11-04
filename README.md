FlyTime - For Towny
-------------------

This plugin allows you to grant time-limited flying permission, and restrict it to a player's own town borders. 

Permissions
-----------

 - flytime.townflight   - required to allow flight.
 - flytime.give         - required to allow giving out fly-time. 
                          (grant this permission to admins/mods)


Commands
--------

There is only one command: **/flytime**

With no arguments it enables flight when the player is within their town borders, has fly-time available, and has the flytime.townflight permission.  The players available fly-time is only used up while they are actually in flight.

if at any point the player remains on the ground for 5 seconds, flight is automatically disabled. this is to prevent exploiting /flytime for permanent fall-damage immunity.

**/flytime give \<player\> \<time_in_seconds\>** can be used by anyone with the flytime.give permission to give fly-time to other players.  







