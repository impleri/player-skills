# Cannot cause damage with an item from a tag

skills reset @p[limit=1] playerskills:item_test

clear @p[limit=1]

execute at @e[tag=test_14,limit=1] run setblock ~1 ~ ~-1 barrel[facing=up] replace

execute at @e[tag=test_14,limit=1] run data merge block ~1 ~ ~-1 {Items:[{Slot:0,id:"minecraft:wooden_sword",Count:1},{Slot:1,id:"minecraft:netherite_sword",Count:1},{Slot:2,id:"minecraft:netherite_axe",Count:1}]}

execute at @e[tag=test_14,limit=1] run summon minecraft:rabbit ~-2 ~ ~-2 {NoAI:1,Silent:1,RabbitType:99,Glowing:1,Health:2}
