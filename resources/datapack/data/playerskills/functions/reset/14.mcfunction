# Cannot use any item from a mod namespace in Biome

skills reset @p[limit=1] playerskills:item_test

clear @p[limit=1]

execute at @e[tag=test_14,limit=1] run setblock ~1 ~ ~-1 barrel[facing=up] replace

execute at @e[tag=test_14,limit=1] run data merge block ~1 ~ ~-1 {Items:[{Slot:0,id:"naturescompass:naturescompass",Count:1}]}

execute at @e[tag=test_14,limit=1] run function playerskills:biome/plains
