# Trigger fail response
# function playerskills:utils/fail

# Make it clear that it failed
setblock ~4 ~ ~4 minecraft:coal_block replace
setblock ~4 ~ ~-4 minecraft:coal_block replace
setblock ~-4 ~ ~4 minecraft:coal_block replace
setblock ~-4 ~ ~-4 minecraft:coal_block replace

# Stop timer
setblock ~0 ~-2 ~0 minecraft:gold_block replace
