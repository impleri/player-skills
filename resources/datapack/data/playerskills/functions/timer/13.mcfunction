# Test Watcher/Timeout
# Runs every tick until test completes. Repeating commmand block triggered by redstone block.
# function playerskills:timer/12

scoreboard players add test_13 test_time 1
execute as @e[tag=test_13_nether,limit=1] at @s if score test_13 test_time >= 4_moment test_time run function playerskills:action/fail
