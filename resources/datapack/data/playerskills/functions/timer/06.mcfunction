# Test Watcher/Timeout
# Runs every tick until test completes. Repeating commmand block triggered by redstone block.
# function playerskills:timer/06

scoreboard players add test_06 test_time 1
execute as @e[tag=test_06,limit=1] at @s if score test_06 test_time >= 1_moment test_time run function playerskills:action/fail
