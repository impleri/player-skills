# Trigger success response
# function playerskills:actions/reset

# Turn off lamps (if they exist)
setblock ~4 ~-1 ~4 minecraft:gold_block replace
setblock ~4 ~-1 ~-4 minecraft:gold_block replace
setblock ~-4 ~-1 ~4 minecraft:gold_block replace
setblock ~-4 ~-1 ~-4 minecraft:gold_block replace

# Replace coal with lamps (if necessary)
setblock ~4 ~ ~4 minecraft:redstone_lamp replace
setblock ~4 ~ ~-4 minecraft:redstone_lamp replace
setblock ~-4 ~ ~4 minecraft:redstone_lamp replace
setblock ~-4 ~ ~-4 minecraft:redstone_lamp replace

# Stop timer
setblock ~0 ~-2 ~-0 minecraft:gold_block replace
