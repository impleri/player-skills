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
8. [FTB Quest] Quest basic skill task can be completed and rewards a basic skill.
9. [FTB Quest] Quest numeric skill task can be completed and rewards an improvement to a numeric skill.
10. [FTB Quest] Quest tiered skill task can be completed and rewards a degradation to a tiered skill.
11. [FTB Quest] Quest specialized skill task can be completed and rewards a specialized skill.
12. Cannot hold item.
13. Cannot wear item in Dimension.
14. Cannot use any item from a mod namespace in Biome.
15. Cannot cause damage with an item from a tag.
16. Cannot identify item in a chest.
17. [Curios/Trinkets] Cannot wear item.
18. [TheOneProbe/WTHIT/Jade] Cannot identify in-world item.
19. Cannot craft a blocked recipe using inventory screen 2x2 crafting grid.
20. Cannot craft a blocked recipe using a crafting table.
21. Cannot craft a blocked recipe using a furnace.
22. Cannot craft a blocked recipe using a smoker.
23. Cannot craft a blocked recipe using a blast furnace.
24. Cannot craft a blocked recipe using a smithing table.
25. Cannot craft a blocked recipe using a stonecutter.
26. Cannot craft a blocked recipe using an anvil.
27. Cannot craft a blocked recipe using a brewing stand.
28. [JEI/REI] Cannot view a blocked recipe for an item.
29. [JEI/REI] Item does not show up in JEI/REI if all recipes for it are blocked.
