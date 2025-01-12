# Cannot use any item from a mod namespace in Biome

skills reset @p[limit=1] playerskills:item_test

clear @p[limit=1]

setblock ~1 ~ ~-1 barrel[facing=up] replace

data merge block ~1 ~ ~-1 {Items:[{Slot:0,id:"minecraft:flint_and_steel",Count:1}]}

function playerskills:biome/ocean
