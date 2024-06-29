# Test Watcher/Timeout
# Runs every tick until test completes. Repeating commmand block triggered by redstone block.
# We compare against preset time lengths (N_sec) also saved in the test_time scoreboard
# function playerskills:timer/ID

execute as @e[tag=test_ID,limit=1] at @s if score test_ID test_time >= 2_sec test_time run function playerskills:action/fail
scoreboard players add test_ID test_time 1
