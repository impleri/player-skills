# Test Watcher/Timeout
# Runs every tick until test completes. Repeating commmand block triggered by redstone block.

scoreboard players add test_47 test_time 1
execute if score test_47 test_time >= 2_moment test_time run function playerskills:action/fail
