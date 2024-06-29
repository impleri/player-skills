# Global Setup Script

gamerule commandBlockOutput false
scoreboard objectives add test_time dummy
scoreboard objectives add test dummy

# scoreboard objectives add created dummy
# scoreboard objectives setdisplay sidebar created

# Set up test dummy
summon minecraft:armor_stand 4 -53 4 { Tags: ["setup"], Invisible: 1b, Invulnerable: 1b }

say Starting Testaforming process

function playerskills:setup/01
function playerskills:setup/02
function playerskills:setup/03
function playerskills:setup/04
function playerskills:setup/05
function playerskills:setup/06
function playerskills:setup/07
function playerskills:setup/08
function playerskills:setup/09
function playerskills:setup/10
function playerskills:setup/11

say Done Testaforming process

# Clean up test dummy
kill @e[tag=setup,limit=1]
scoreboard objectives remove test

scoreboard players set 1_sec test_time 20
scoreboard players set 2_sec test_time 40
scoreboard players set 3_sec test_time 60
scoreboard players set 4_sec test_time 80
scoreboard players set 5_sec test_time 100
scoreboard players set 6_sec test_time 120
scoreboard players set 7_sec test_time 140
scoreboard players set 8_sec test_time 160
scoreboard players set 9_sec test_time 180
