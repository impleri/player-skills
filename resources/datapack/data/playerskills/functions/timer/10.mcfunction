# Test Watcher/Timeout
# Runs every tick until test completes. Repeating commmand block triggered by redstone block.
# function playerskills:timer/10

scoreboard players add test_10 test_time 1
execute as @e[tag=test_10,limit=1] at @s if score test_10 test_time >= 2_moment test_time run function playerskills:action/fail
