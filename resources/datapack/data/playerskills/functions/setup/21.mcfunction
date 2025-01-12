# Cannot wear item in Dimension

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

execute at @e[tag=setup,limit=1] run setblock ~ ~-1 ~ minecraft:sea_lantern replace
execute at @e[tag=setup,limit=1] run setblock ~ ~-2 ~ minecraft:gold_block replace

execute at @e[tag=setup,limit=1] run setblock ~ ~-1 ~-2 minecraft:command_block[facing=down]{ auto: 0b, Command: "/execute as @p[limit=1] run function playerskills:teleport/21_to_nether" }
execute at @e[tag=setup,limit=1] run setblock ~ ~ ~-2 minecraft:stone_button[face=floor]
execute at @e[tag=setup,limit=1] run setblock ~ ~ ~-3 oak_sign[rotation=0]{ Text2: '{"text":"Teleport","bold":true,"color":"dark_blue"}' } replace

execute at @e[tag=setup,limit=1] run setblock ~2 ~-1 ~ minecraft:command_block[facing=down]{ auto: 0b, Command: "/execute as @e[tag=test_21,limit=1] at @s run function playerskills:action/fail" }
execute at @e[tag=setup,limit=1] run setblock ~2 ~ ~ minecraft:stone_button[face=floor,facing=east]
execute at @e[tag=setup,limit=1] run setblock ~3 ~ ~ oak_sign[rotation=4]{ Text2: '{"text":"Fail","bold":true,"color":"dark_red"}' } replace

execute at @e[tag=setup,limit=1] run setblock ~ ~-1 ~2 minecraft:command_block[facing=down]{ auto: 0b, Command: "/execute as @e[tag=test_21,limit=1] at @s run function playerskills:action/reset" }
execute at @e[tag=setup,limit=1] run setblock ~ ~-2 ~2 minecraft:chain_command_block[conditional=true,facing=down]{ auto: 1b, Command: "/execute as @e[tag=test_21,limit=1] at @s run function playerskills:reset/21" }
execute at @e[tag=setup,limit=1] run setblock ~ ~ ~2 minecraft:stone_button[face=floor]
execute at @e[tag=setup,limit=1] run setblock ~ ~ ~3 oak_sign[rotation=8]{ Text2: '{"text":"Reset","bold":true,"color":"black"}' } replace

execute at @e[tag=setup,limit=1] run setblock ~-2 ~-1 ~ minecraft:command_block[facing=down]{ auto: 0b, Command: "/execute as @e[tag=test_21,limit=1] at @s run function playerskills:action/succeed" }
execute at @e[tag=setup,limit=1] run setblock ~-2 ~ ~ minecraft:stone_button[face=floor,facing=east]
execute at @e[tag=setup,limit=1] run setblock ~-3 ~ ~ oak_sign[rotation=12]{ Text2: '{"text":"Pass","bold":true,"color":"dark_green"}' } replace

execute at @e[tag=setup,limit=1] run summon minecraft:armor_stand ~ ~ ~ { Tags: ["test", "test_21"], Small: 1b, Invisible: 1b, Invulnerable: 1b }
execute at @e[tag=setup,limit=1] run setblock ~-1 ~ ~-1 oak_sign[rotation=14]{ Text1: '{"text":"Item","bold":true}', Text2: '{"text": "is wearable"}', Text3: '{"text": "in Nether"}' } replace

execute as @e[tag=test_21,limit=1] at @s run function playerskills:reset/21

execute at @e[tag=setup,limit=1] run teleport @e[tag=setup,limit=1] ~16 ~ ~
scoreboard players add something test 1
