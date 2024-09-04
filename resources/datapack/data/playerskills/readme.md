# In-Game Tests

This datapack provides everything necessary to create in-game tests for PlayerSkills functionality that cover a large
portion of PlayerSkills functionality. It is meant to be as portable as possible across modloaders and Minecraft
versions.

## Setup

Create a new creative superflat world using the Skills Test preset. You can optionally turn off daylight and weather
cycles as well as mob spawning. Execute the command `/function playerskills:setup/run` to create all of the tests.

## Layout

Tests are 9x9 square areas with a 1 block gap between them. They are arranged in rows of 10 tests starting at (0,0).
Each test is a 9x9 square with a diamond block at the center. Next to the center block is a sign identifying the test.
Surrounding the center block are 4 button-activated command blocks with a sign indicating which action the button
performs. At the 4 extreme corners are blocks which help indicate the test state.

### Test Identifiers

Each test has 4 states:

- ***ready***: Inactive redstone lamps at the 4 corners
- ***running***: Inactive redstone lamps at the 4 corners
- ***success***: Active redstone lamps at the 4 corners
- ***failure***: Blocks of coal at the 4 corners

When a test enters the running state, a timer starts ticking and will automatically transition to the failed state. The
player should be notified in chat of what command or action to perform in order to validate the test, then trigger the
correct state. Hopefully, a future iteration of this world will automate at least some tests.

Occasionally, a feature may require a specific modloader. This can be identified by the outermost ring of blocks:

- Quartz: all modloaders
- Lapis: Forge
- Amethyst: Neo-Forge
- Emerald: Fabric

Finally, some tests may require a mod to validate functionality. The second outermost ring of blocks:

- Grass: Testable without another mod present
- Coarse Dirt: Testable only with another mod active

Note: All mod integration tests will fall in the latter category

### Actions

- `Start`: Transitions to the running state and creates the conditions needed for the test.
- `Success`: Transitions to the success state.
- `Fail`: Transitions to the failure state.
- `Reset`: Transitions to ready state and removes conditions created by the `Start` action.

## Tests

Just a master list of all of the in-game tests. Tests begin at (0,y,0) and ten are placed along the x-axis before
incrementing along the z-axis.

1. Basic skill can be acquired.
2. Numeric skill can be improved.
3. Tiered skill can be degraded.
4. Specialized skill can be changed.
5. Skill cannot be given an invalid type of value.
6. Skill cannot be given a value not in its allowed options.
7. Skill cannot be changed more than allowed.
8. Quest basic skill task can be completed and rewards a basic skill.
9. Quest numeric skill task can be completed and rewards an improvement to a numeric skill.
10. Quest tiered skill task can be completed and rewards a degradation to a tiered skill.
11. Quest specialized skill task can be completed and rewards a specialized skill.
12. Skill gained when on a team syncs to the online and offline team members.
13. Syncing a single player's skills to a team works.
14. Syncing skills for a whole team works.
15. Cannot hold item in a given dimension.
16. Cannot wear item.
17. Cannot cause damage with item in a given biome.
18. Cannot use item.
19. Cannot wear item (Curios/Trinkets).
20. Cannot identify item in a chest.
21. Cannot identify in-world item (TheOneProbe/WTHIT/Jade).
22. Cannot craft a blocked recipe in a given dimension.
23. Cannot craft a blocked recipe in a given biome with a crafting table.
24. Cannot craft a blocked recipe in a smelter.
25. Cannot view a blocked recipe (JEI/REI).
26. Item does not show up in JEI/REI if all recipes for it are blocked.
