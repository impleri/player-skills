# Setup for test

execute at @e[tag=setup,limit=1] run fill ~-4 ~-1 ~-4 ~4 ~-1 ~4 minecraft:quartz_block replace
execute at @e[tag=setup,limit=1] run fill ~-3 ~-1 ~-3 ~3 ~-1 ~3 minecraft:grass_block replace
execute at @e[tag=setup,limit=1] run fill ~-1 ~-1 ~-1 ~1 ~-1 ~1 minecraft:iron_block replace

execute at @e[tag=setup,limit=1] run setblock ~4 ~-1 ~4 minecraft:gold_block replace
execute at @e[tag=setup,limit=1] run setblock ~4 ~-1 ~-4 minecraft:gold_block replace
execute at @e[tag=setup,limit=1] run setblock ~-4 ~-1 ~4 minecraft:gold_block replace
execute at @e[tag=setup,limit=1] run setblock ~-4 ~-1 ~-4 minecraft:gold_block replace

execute at @e[tag=setup,limit=1] run setblock ~4 ~ ~4 minecraft:redstone_lamp replace
execute at @e[tag=setup,limit=1] run setblock ~4 ~ ~-4 minecraft:redstone_lamp replace
execute at @e[tag=setup,limit=1] run setblock ~-4 ~ ~4 minecraft:redstone_lamp replace
execute at @e[tag=setup,limit=1] run setblock ~-4 ~ ~-4 minecraft:redstone_lamp replace

execute at @e[tag=setup,limit=1] run setblock ~ ~-1 ~ minecraft:diamond_block replace
execute at @e[tag=setup,limit=1] run setblock ~ ~-2 ~ minecraft:gold_block replace
execute at @e[tag=setup,limit=1] run setblock ~ ~-3 ~ minecraft:repeating_command_block[facing=down]{ auto: 0b, Command: "/function playerskills:timer/06" } replace

execute at @e[tag=setup,limit=1] run setblock ~ ~-1 ~-2 minecraft:command_block[facing=down]{ auto: 0b, Command: "/scoreboard players reset test_06 test_time" }
execute at @e[tag=setup,limit=1] run setblock ~ ~-2 ~-2 minecraft:chain_command_block[conditional=true,facing=down]{ auto: 1b, Command: "/execute as @e[tag=test_06,limit=1] at @s run function playerskills:action/start" }
execute at @e[tag=setup,limit=1] run setblock ~ ~-3 ~-2 minecraft:chain_command_block[conditional=true,facing=down]{ auto: 1b, Command: "/execute as @e[tag=test_06,limit=1] at @s run function playerskills:test/06" }
execute at @e[tag=setup,limit=1] run setblock ~ ~ ~-2 minecraft:stone_button[face=floor]
execute at @e[tag=setup,limit=1] run setblock ~ ~ ~-3 oak_sign[rotation=0]{ Text2: '{"text":"Start","bold":true,"color":"dark_blue"}' } replace

execute at @e[tag=setup,limit=1] run setblock ~2 ~-1 ~ minecraft:command_block[facing=down]{ auto: 0b, Command: "/execute as @e[tag=test_06,limit=1] at @s run function playerskills:action/fail" }
execute at @e[tag=setup,limit=1] run setblock ~2 ~ ~ minecraft:stone_button[face=floor,facing=east]
execute at @e[tag=setup,limit=1] run setblock ~3 ~ ~ oak_sign[rotation=4]{ Text2: '{"text":"Fail","bold":true,"color":"dark_red"}' } replace

execute at @e[tag=setup,limit=1] run setblock ~ ~-1 ~2 minecraft:command_block[facing=down]{ auto: 0b, Command: "/execute as @e[tag=test_06,limit=1] at @s run function playerskills:action/reset" }
execute at @e[tag=setup,limit=1] run setblock ~ ~-2 ~2 minecraft:chain_command_block[conditional=true,facing=down]{ auto: 1b, Command: "/execute as @e[tag=test_06,limit=1] at @s run function playerskills:reset/06" }
execute at @e[tag=setup,limit=1] run setblock ~ ~ ~2 minecraft:stone_button[face=floor]
execute at @e[tag=setup,limit=1] run setblock ~ ~ ~3 oak_sign[rotation=8]{ Text2: '{"text":"Reset","bold":true,"color":"black"}' } replace

execute at @e[tag=setup,limit=1] run setblock ~-2 ~-1 ~ minecraft:command_block[facing=down]{ auto: 0b, Command: "/execute as @e[tag=test_06,limit=1] at @s run function playerskills:action/succeed" }
execute at @e[tag=setup,limit=1] run setblock ~-2 ~ ~ minecraft:stone_button[face=floor,facing=east]
execute at @e[tag=setup,limit=1] run setblock ~-3 ~ ~ oak_sign[rotation=12]{ Text2: '{"text":"Pass","bold":true,"color":"dark_green"}' } replace

execute at @e[tag=setup,limit=1] run summon minecraft:armor_stand ~ ~ ~ { Tags: ["test", "test_06"], Small: 1b, Invisible: 1b, Invulnerable: 1b }
execute at @e[tag=setup,limit=1] run setblock ~-1 ~ ~-1 oak_sign[rotation=14]{ Text1: '{"text":"Not in Option","bold":true}', Text3: '{"text": "no skill change"}' } replace

execute as @e[tag=test_06,limit=1] at @s run function playerskills:reset/06

execute at @e[tag=setup,limit=1] run teleport @e[tag=setup,limit=1] ~10 ~ ~
scoreboard players add something test 1
