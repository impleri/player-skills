# In-Game Tests

This datapack provides everything necessary to create in-game tests for PlayerSkills functionality that cover a large
portion of PlayerSkills functionality. It is meant to be as portable as possible across modloaders and Minecraft
versions.

## Setup

Create a new creative superflat world using the Skills Test preset. You can optionally turn off daylight and weather
cycles as well as mob spawning. Execute the command `/function playerskills:setup` to create all of the tests.

## Layout

Tests are 9x9 square areas centered within a chunk. They are arranged in rows of up to 10 tests starting at (0,0). Each
row is expected to cover a single series of related tests (e.g. specific restriction, restriction-specific integrations).
Each test is a 9x9 square with a diamond block at the center. Next to the center block is a sign identifying the test.
Surrounding the center block are 4 button-activated command blocks with a sign indicating which action the button
performs. At the 4 extreme corners are blocks which help indicate the test state. There may be additional blocks within
the square that are necessary to support the test (e.g. if it requires interacting with a mob, the mob will be on an
identifiable block such as netherite). 

### Test Identifiers

Each test has 4 states:

- ***ready***: Inactive redstone lamps at the 4 corners
- ***running***: Inactive redstone lamps at the 4 corners
- ***success***: Active redstone lamps at the 4 corners
- ***failure***: Blocks of coal at the 4 corners

When a test enters the running state, a timer starts ticking and will automatically transition to the failed state after
a designated amount of time. The player should be notified in chat of what command or action to perform in order to 
validate the test, then trigger the correct state. Hopefully, a future iteration of this world will automate at least
some tests.

Some tests may require a mod to validate functionality. The second outermost ring of blocks will help identify this:

- Grass: Testable without another mod present
- Coarse Dirt: Testable only with another mod active (consult below)

Note: All mod integration tests will fall in the latter category

Occasionally, a feature may require a specific modloader. Such a scenario would occur if there is no established mod
equivalence between modloaders (example: Curios in Forge and Trinkets in Fabric are interchangeable enough that a test
for one would work for the other). This can be identified by the outermost ring of blocks:

- Quartz: all modloaders
- Lapis: Forge only
- Amethyst: Neo-Forge only
- Emerald: Fabric only


### Actions

- `Start`: Transitions to the running state and creates the conditions needed for the test to pass (might require 
            triggering the start again).
- `Success`: Transitions to the success state.
- `Fail`: Transitions to the failure state immediately.
- `Reset`: Transitions to ready state and removes conditions created by the `Start` action.

## Environments

In order to establish reproducibility, I am currently testing in the following environments:

1. Standalone 1.19.2
2. Client/Server 1.19.2 (excluding client-only interactions related to item identifiability)
3. Client/Server 1.18.2 (excluding client-only interactions related to item identifiability)

## Tests

Just a master list of all of the in-game tests. Tests begin at (0,y,0) and up to ten are placed along the x-axis before
incrementing along the z-axis. Each test occupies a single chunk.

### Core Skills

1. Basic skill can be acquired.
2. Numeric skill can be improved.
3. Tiered skill can be degraded.
4. Specialized skill can be changed.
5. Skill cannot be given an invalid type of value.
6. Skill cannot be given a value not in its allowed options.
7. Skill cannot be changed more than allowed.

### Core Integrations

10. [FTB Quest] Quest basic skill task can be completed and rewards a basic skill.
11. [FTB Quest] Quest numeric skill task can be completed and rewards an improvement to a numeric skill.
12. [FTB Quest] Quest tiered skill task can be completed and rewards a degradation to a tiered skill.
13. [FTB Quest] Quest specialized skill task can be completed and rewards a specialized skill.

### Item Restrictions

20. Cannot hold item. 
21. Cannot wear item in Dimension.
22. Cannot use any item from a mod namespace in Biome. 
23. Cannot cause damage with an item from a tag.
24. Cannot identify item in a chest/inventory (single-player only without KubeJS or CraftTweaker).

### Item Restrictions Integrations

30. [Curios/Trinkets] Cannot wear item.
31. [TheOneProbe/WTHIT/Jade] Cannot identify in-world item.

### Recipe Restrictions

40. Cannot craft a blocked recipe using inventory screen 2x2 crafting grid.
41. Cannot craft a blocked recipe using a crafting table.
42. Cannot craft a blocked recipe using a furnace.
43. Cannot craft a blocked recipe using a blast furnace.
44. Cannot craft a blocked recipe using a smoker.
45. Cannot craft a blocked recipe using a campfire.
46. Cannot craft a blocked recipe using a smithing table.
47. Cannot craft a blocked recipe using a stonecutter.

### Recipe Restrictions Integrations

50. [JEI/REI] Cannot view a blocked recipe for an item.
51. [JEI/REI] Item does not show up in JEI/REI if all recipes for it are blocked.
