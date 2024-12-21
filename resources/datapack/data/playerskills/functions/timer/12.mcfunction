# Test Watcher/Timeout
# Runs every tick until test completes. Repeating commmand block triggered by redstone block.
# function playerskills:timer/12

scoreboard players add test_12 test_time 1
execute as @e[tag=test_12,limit=1] at @s if score test_12 test_time >= 4_moment test_time run function playerskills:action/fail
