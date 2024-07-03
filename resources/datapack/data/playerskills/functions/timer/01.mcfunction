# Test Watcher/Timeout
# Runs every tick until test completes. Repeating commmand block triggered by redstone block.
# function playerskills:timer/01

scoreboard players add test_01 test_time 1
execute as @e[tag=test_01,limit=1] at @s if score test_01 test_time >= 1_moment test_time run function playerskills:action/fail
