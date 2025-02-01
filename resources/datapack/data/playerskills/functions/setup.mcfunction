# Global Setup Script

function playerskills:setup/reset

gamerule commandBlockOutput false
scoreboard objectives add test_time dummy
scoreboard objectives add test dummy

# scoreboard objectives add created dummy
# scoreboard objectives setdisplay sidebar created

execute positioned -8 ~ 0 run function playerskills:biome/frozen_ocean
execute positioned 0 ~ -8 run function playerskills:biome/ocean

# Set up test dummy
summon minecraft:armor_stand 8 -53 8 { Tags: ["setup"], Invisible: 1b, Invulnerable: 1b }

say Starting Testaforming process

function playerskills:setup/01
function playerskills:setup/02
function playerskills:setup/03
function playerskills:setup/04
function playerskills:setup/05
function playerskills:setup/06
function playerskills:setup/07
function playerskills:setup/10
function playerskills:setup/11
function playerskills:setup/12
function playerskills:setup/13
function playerskills:setup/20
function playerskills:setup/21
function playerskills:setup/22
function playerskills:setup/23
function playerskills:setup/24
function playerskills:setup/30
function playerskills:setup/31
function playerskills:setup/40
function playerskills:setup/41
function playerskills:setup/42
function playerskills:setup/43
function playerskills:setup/44
function playerskills:setup/45
function playerskills:setup/46
function playerskills:setup/47
function playerskills:setup/50
function playerskills:setup/51

say Done Testaforming Overworld process

# Clean up test dummy
kill @e[tag=setup,limit=1]
scoreboard objectives remove test

scoreboard players set 1_moment test_time 200
scoreboard players set 2_moment test_time 400
scoreboard players set 3_moment test_time 600
scoreboard players set 4_moment test_time 800
scoreboard players set 5_moment test_time 1000
scoreboard players set 6_moment test_time 1200
scoreboard players set 7_moment test_time 1400
scoreboard players set 8_moment test_time 1600
scoreboard players set 9_moment test_time 1800

execute as @p[limit=1] in minecraft:the_nether run tp 8 128 8

say Execute "/function playerskills:nether/setup"
