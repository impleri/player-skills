# Cannot hold item

skills reset @p[limit=1] playerskills:item_test

clear @p[limit=1]

execute at @e[tag=test_12,limit=1] run setblock ~1 ~ ~-1 barrel[facing=up] replace

execute at @e[tag=test_12,limit=1] run data merge block ~1 ~ ~-1 {Items:[{Slot:0,id:"minecraft:apple",Count:1}]}
