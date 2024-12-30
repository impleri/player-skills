# Test Watcher/Timeout
# Runs every tick until test completes. Repeating commmand block triggered by redstone block.
# function playerskills:timer/16

scoreboard players add test_16 test_time 1
execute as @e[tag=test_16,limit=1] at @s if score test_16 test_time >= 3_moment test_time run function playerskills:action/fail
