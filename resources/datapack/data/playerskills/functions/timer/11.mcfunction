# Test Watcher/Timeout
# Runs every tick until test completes. Repeating commmand block triggered by redstone block.
# function playerskills:timer/11

scoreboard players add test_11 test_time 1
execute as @e[tag=test_11,limit=1] at @s if score test_11 test_time >= 2_moment test_time run function playerskills:action/fail
