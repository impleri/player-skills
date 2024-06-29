# Trigger success response
# function playerskills:actions/succeed

setblock ~4 ~-1 ~4 minecraft:redstone_block replace
setblock ~4 ~-1 ~-4 minecraft:redstone_block replace
setblock ~-4 ~-1 ~4 minecraft:redstone_block replace
setblock ~-4 ~-1 ~-4 minecraft:redstone_block replace

# Stop timer
setblock ~0 ~-2 ~0 minecraft:gold_block replace
