# Reset Test

skills reset @p[limit=1] playerskills:item_test

clear @p[limit=1]

execute at @e[tag=test_13_nether,limit=1] run setblock ~1 ~ ~-1 barrel[facing=up] replace

execute at @e[tag=test_13_nether,limit=1] run data merge block ~1 ~ ~-1 {Items:[{Slot:0,id:"minecraft:elytra",Count:1}]}
