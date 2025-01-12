# Nether Reset Script

execute at @e[tag=nether_test] run fill ~-4 ~-5 ~-4 ~4 ~-1 ~4 minecraft:netherrack replace
execute at @e[tag=nether_test] run fill ~-4 ~-1 ~-4 ~4 ~-1 ~4 minecraft:bedrock replace
execute at @e[tag=nether_test] run fill ~-4 ~ ~-4 ~4 ~ ~4 minecraft:air replace
kill @e[tag=nether_test]
