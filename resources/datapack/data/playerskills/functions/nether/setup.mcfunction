# Nether Setup Script

function playerskills:nether/reset

summon minecraft:armor_stand 8 128 8 { Tags: ["setup"], Invisible: 1b, Invulnerable: 1b }

say Starting Nether Testaforming process

function playerskills:nether/setup/21
function playerskills:nether/setup/30

say Done Testaforming Nether process

# Clean up test dummy
kill @e[tag=setup,limit=1]
scoreboard objectives remove test

execute as @p[limit=1] in minecraft:overworld run tp 8 -53 8
