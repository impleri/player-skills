# Cannot craft a blocked recipe using a blast furnace

skills reset @p[limit=1] playerskills:item_test

clear @p[limit=1]

setblock ~1 ~ ~-1 barrel[facing=up] replace

data merge block ~1 ~ ~-1 {Items:[{Slot:0,id:"minecraft:raw_gold",Count:1},{Slot:1,id:"minecraft:coal",Count:1}]}
