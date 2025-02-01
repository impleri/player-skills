# Test Watcher/Timeout
# Runs every tick until test completes. Repeating commmand block triggered by redstone block.

scoreboard players add test_44 test_time 1
execute if score test_44 test_time >= 2_moment test_time run function playerskills:action/fail
