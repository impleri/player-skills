# Nether Setup Script

summon minecraft:armor_stand 4 128 4 { Tags: ["setup"], Invisible: 1b, Invulnerable: 1b }

say Starting Nether Testaforming process

function playerskills:nether_setup/13

say Done Testaforming process

# Clean up test dummy
kill @e[tag=setup,limit=1]
scoreboard objectives remove test

execute as @p[limit=1] in minecraft:overworld run tp 4 -53 4
