# Test Watcher/Timeout
# Runs every tick until test completes. Repeating commmand block triggered by redstone block.
# function playerskills:timer/08

scoreboard players add test_08 test_time 1
execute if score test_08 test_time >= 2_moment test_time run function playerskills:action/fail
