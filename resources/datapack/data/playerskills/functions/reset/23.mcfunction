# Cannot cause damage with an item from a tag

skills reset @p[limit=1] playerskills:item_test

clear @p[limit=1]

kill @e[tag=test_target]

setblock ~1 ~ ~-1 barrel[facing=up] replace

data merge block ~1 ~ ~-1 {Items:[{Slot:0,id:"minecraft:netherite_sword",Count:1},{Slot:1,id:"minecraft:netherite_axe",Count:1}]}

summon minecraft:rabbit ~-2 ~ ~-2 {NoAI:1,Silent:1,RabbitType:99,Glowing:1,Health:2,Tags:["test_target"]}
