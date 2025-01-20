# Cannot identify in-world item

skills reset @p[limit=1] playerskills:item_test

clear @p[limit=1]

setblock ~1 ~ ~-1 barrel[facing=up] replace

data merge block ~1 ~ ~-1 {Items:[{Slot:0,id:"minecraft:shears",Count:1}]}

summon item ~-2 ~ ~-2 {Item:{id:"minecraft:shears",count:1,Tags:["test_target"]}}
