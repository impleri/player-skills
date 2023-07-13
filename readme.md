# Player Skills

A library mod that handles everything related to player-specific skills and skill-based restrictions. Included in the
box are the following types of restrictions:

* Mob Spawning
* Mob Interaction (i.e. right-clicking to trade with a villager)

Like GameStages and GamePhases, this provides a registry for tracking possible skills as well what skills a given player
currently possesses. Unlike the other mods, skills are added to a player immediately and leverage ability checking via
Skill Types.

[![CurseForge](https://cf.way2muchnoise.eu/short_711489.svg)](https://www.curseforge.com/minecraft/mc-mods/player-skills)
[![Modrinth](https://img.shields.io/modrinth/dt/player-skills?color=bcdeb7&label=%20&logo=modrinth&logoColor=096765&style=plastic)](https://modrinth.com/mod/player-skills)
[![MIT license](https://img.shields.io/github/license/impleri/player-skills?color=bcdeb7&label=Source&logo=github&style=flat)](https://github.com/impleri/player-skills)
[![Discord](https://img.shields.io/discord/1093178610950623233?color=096765&label=Community&logo=discord&logoColor=bcdeb7&style=plastic)](https://discord.com/invite/avxJgbaUmG)
[![Maven](https://img.shields.io/maven-metadata/v?label=1.19.2&color=096765&metadataUrl=https%3A%2F%2Fmaven.impleri.org%2Fminecraft%2Fnet%2Fimpleri%2Fplayerskills%2Fmaven-metadata.xml&style=flat)](https://github.com/impleri/player-skills/tree/1.19.2#developers)

## Core Concepts

This mod provides three entities that can be extended: Skill Types, Skills, and (Skill-based) Restrictions.

### Skill Type

A skill type handles all logic around managing skills tied to its type. That includes providing a way to (de)serialize
the skill into a persistent format. More advanced use cases can leverage the skill type for determining if a skill is of
a sufficient threshold. Because of this nature, skill types cannot be created via KubeJS.

#### Builtin Types

1. `Basic` provides a simple boolean `true`/`false`. Great for simple skills such as "can read."
2. `Numeric` provides an integer value (0, 1, 2, etc). Great for skills or attributes which can be improved such as "
   strength."
3. `Tiered` are specialized version of `Numeric` in that strings can be used. One example could be "education"
   education (crude/primary, basic/secondary, intermediate/university, advanced/postgraduate, top/doctorate).
4. `Specialization` provide a skill which allows one or more specializations out of a list. An example is "magic type" (
   red, black, white, green, blue).

### Skill

A skill tracks the data needed for SKill Types to calculate if a player _can_ perform a skill-based action. Skills get
serialized and persisted in player-specific NBT data. Skills are exposed via KubeJS for modpack authors to manipulate as
they see fit. This mod provides no built-in skills, as we want to encourage modpacks and other mods to provide that
shape.

### Restriction

A restrictions handles the logic for determining if a player has an ability based on the set condition, current
dimension, and current biome. It may also provide a replacement to the target resource. Like Skills, we provide no
restrictions here. The other xSkills mods implements restrictions for many vanilla elements.

#### ID Parsing

Creating restrictions can be tedious. In order to help reduce that, all restriction identifiers as well as dimension and
biome facets share the same identifier parsing. This means that string identifiers can be used (e.g. `minecraft:zombie`)
as well as mod IDs (e.g. `minecraft:*` or `@minecraft`) and tags (`#minecraft:desert` or `#desert`) can be used where
appropriate. Note that not everything uses tags (e.g. dimensions), so it won't work with those. These work in Data
Packs, KubeJS, and CraftTweaker.

## Caveats

Because of the way we're handling spawn conditions, if you are using an `unless` condition, you should not also
manipulate `usable` in the same restriction. Just keep the two restrictions separate.

## Data Packs

You can create skills using data packs! Just use the `skills` grouping. The file name will be used as the skill name.
Example: `mymod/skills/something_cool.json` will be registered as `mymod:something_cool`. The only required field is
`type`; all others can either be omitted or set to `null`.

- `initialValue` and `options` elements should be typed according to the `type` (e.g. use numbers for numerics, booleans
  for basic, and strings otherwise).
- `notify` can be either a string pointing to the translation string to use or a simple boolean `true` to use the
  default message.
- `teamMode` can be either an object (see below) or a string with the value of `teamMode.mode` if not
  using `proportional` or `limited`.

The JSON schema is:

```json
{
  "type": "basic|numeric|tiered|specialized",
  "description": "More about the skill.",
  "initialValue": null,
  "options": [
    "one",
    "two",
    "three"
  ],
  "changesAllowed": 5,
  "notify": "mymod.translation.key",
  "teamMode": {
    "mode": "off|shared|splitEvenly|pyramid|proportional|limited",
    "rate": 0.45
  }
}
```

### Data Pack Restrictions API

There is less dynamism in the data pack handling of restrictions mostly because the medium of Data Packs (JSON) is
static. However, we have done our best to make it work as close as possible as using KubeJS or CraftTweaker. These
properties are available on every restriction.

#### Condition Properties

- `if`: Sets the condition which must evaluate to true in order to apply.
- `unless`: Sets the condition which must evaluate to false.

In either case, the condition provided can be a single condition object or an array of them.

```json
{
  "if": [
    {
      "skill": "some_basic_skill"
    },
    {
      "skill": "numeric_skill",
      "value": 3
    }
  ],
  "unless": {
    "skill": "that_specialization_skill",
    "value": "blue"
  }
}
```

#### Facet Properties

- `dimensions`: Add dimension facets to the restriction
- `biomes`: Add biome facets to the restriction

Both facets can either be an array of string values that will be parsed as IDs (see above) or an object with `include`
and/or `exclude` properties that are array of string values.

```json
{
  "dimensions": [
    "overworld",
    "minecraft:nether",
    "@ad_astra"
  ],
  "biomes": {
    "include": [
      "plains",
      "#desert",
      "ad_astra:*"
    ],
    "exclude": [
      "#minecraft:ocean"
    ]
  }
}
```

## KubeJS API

### Skills Registry Actions

For all registry actions, we use two separate events:

- `SkillEvents.registration` ***startup*** event to _add_ skills.
- `SkillEvents.modification` ***server*** event to _modify_ and _delete_ skills registered in the startup script or by
  mods.

Both events use the skill name as the first parameter. This is a string that will be cast into a `ResourceLocation`, so
it must conform to ResourceLocation rules (namely, `snake_case` instead of `camelCase`). If no namespace is given, it
will automatically be placed into the `skills:` namespace.

When adding a skill, you must provide a skill type string as the second parameter. Like the name, this will be cast into
a `ResourceLocation` and given the default `skills:` namespace if none provided. If you are modifying a skill, you may
add a skill type parameter after the name to _change_ the skill typing. Be sure to change the initial value of the skill
if you're changing the type or else you will see errors.

For both adding and modifying skills, the final argument is a callback function which will interact with the skill
builder for the type. Modifying skills will start with their existing configurations while new skills will start with
whatever the default options are for the type.

#### Skill Builder Methods

- `initialValue(newValue: T)` - Sets a starting value for all players
- `clearValue()` - Empties the initial value
- `description(desc: string)` - Sets the skill description
- `limitChanges(limit: number)` - How many times the skill can change before it is locked
- `unlimitedChanges()` - Allows the skill to always change
- `notifyOnChange(translationKey?: string)` - Send a notification when the skill changes (using either the provided
  translation key or the base skill name)
- `clearNotification()` - Turns off notification on skill change
- `options(choices: T[])` - Sets what values will be allowed
- `sharedWithTeam()` - Sync progress to all players on a team for the shared skill (requires FTM Teams)
- `teamLimitedTo(amount: number)` - Limits the progress to only `amount` players on the team (requires FTB Teams)
- `percentageOfTeam(percentage: number)` = Limits the progress to a `percentage` of the players on the team (requires
  FTB Teams)
- `splitEvenlyAcrossTeam()` - Limits the progress of a _specialization_ skill so that there must be an even distribution
  of specializations across the team.
- `pyramid()` - Limits the progress of a _tiered_ skill so that fewer and fewer players on a team can progress until
  only one player has the highest tier.

#### Add a skill

By default, we provide a simple boolean skill type (yes/no) which resembles what comes from Game Stages and Game Phases.

```js
// Let's assume we have these tiers for `undead_killer` skill
const KILLER_TIERS = {
  wood: 0,
  stone: 1,
  iron: 2,
  gold: 3,
  diamond: 4,
  netherite: 5,
};

SkillEvents.registration(event => {
  event.add('started_quest', 'basic', skill => {
    skill.initialValue(false)
      .description('Indicates a Player has joined the Great Quest');
  });

  // Shares the skill with the rest of the team
  event.add('team_started_quest', 'basic', skill => {
    skill.initialValue(false)
      .description('Indicates a Team has joined the Great Quest')
      .sharedWithTeam();
  });

  // Only 16 percent of the team (rounded up) can complete the quest 
  event.add('team_completed_quest', 'basic', skill => {
    skill.initialValue(false)
      .description('Indicates a Player has completed the Great Quest for the team')
      .percentageOfTeam(16.0);
  });

  // Only the first 4 Players on the team to gain this skill will receive it 
  event.add('team_completed_quest', 'basic', skill => {
    skill.initialValue(false)
      .description('Indicates a Player has completed the Great Quest for the team')
      .teamLimitedTo(4);
  });

  // Create a pyramid of tier limits (1 Nehtherite, 2 Diamond, 4 Gold, 8 Iron, 16 Stone) 
  event.add('undead_killer', 'basic', skill => {
    skill.initialValue(false)
      .description('Tier of undead killer level')
      .pyramis();
  });
});
```

#### Modify Skill

```js
SkillEvents.modification(event => {
  event.modify('skills:test', skill => {
    skill.initialValue(true)
      .description('Less of a test value');
  });
});
```

#### Remove Skill

```js
SkillEvents.modification(event => {
  event.remove('test');
});
```

### Player Actions

Whenever an event is triggered in KubeJS that has a Player associated with it, we attach some Player-specific functions.

#### Get Player's Current Skill Set

KubeJS will have read-only access to a player's skill set. Note that we store _every_ skill on a player, so do not use
the existence of a skill for determining if a player can perform an action.

```js
BlockEvents.rightClicked('minecraft:dirt', event => {
  event.entity.data.skills.all.forEach(skill => console.info(`Player has ${skill.name} at ${skill.value}`))
})
```

#### Can?

Instead, we provide a function to determine if a player has a sufficient Skill:

```js
BlockEvents.rightClicked('minecraft:dirt', event => {
  if (event.entity.data.skills.can('skills:harvest', 2)) {
    // If the player does have a harvest skill of 2 or greater, spawn a Green Guardian to plague them
    event.block.createEntity('custom:green_guardian').spawn()
  }
})
```

This example is expecting `skills:harvest` to be a `Numeric` skill and checks if the player has a skill level of 2 ***or
greater***. If no skill value is used (i.e. `event.entity.data.skills.can('skills:harvest')`), then the check only looks
to see if the skill is truthy (`true`, greater than 0, not `null`). If you want to invert the condition, you can use
`cannot` instead of `can`.

#### Change Skills

Oftentimes, you will want to set a player's skill level based on some arbitrary rules. We don't build in those rules!
However, you will have a few options:

- `improve(skill: string, builder?: Builder)` - Increase the value
- `degrade(skill: string, builder?: Builder)` - Decrease the value
- `set(skill: name, newValue: T, builder?: Builder)` - Set the skill to an arbitrary value
- `reset(skill: name, builder?: Builder)` - Reset the skill back to the initial value

##### Condition Builder

Each of the methods take an optional condition builder callback. Here, you can provide more boundaries.

- `min(value: T)` - If the player's skill is below the minimum, it will jump to the minimum value. If it's at or above,
  it will
  increment appropriately
- `max(value: T)` - If the player's skill is at or above the maximum value, nothing will change. Otherwise, increment
  the skill
  appropriately
- `if(condition: boolean)` - Add an expression which evaluates to a boolean value. Will only increment the value if this
  is true
- `unless(condition: boolean)` - Add an expression which evaluates to a boolean value. Will only increment the value if
  this is false
- `chance(percentage: number)` - Make the skill gain based on random chance using the provided. Values are between 0
  and 100. Anything over 100 is guaranteed success. Default is 100

##### Examples

```js
BlockEvents.rightClicked('minecraft:dirt', event => {
  // this is a basic skill, so we ensure it's true
  event.entity.data.skills.improve('skills:dirt_watcher');

  // this is a numeric skill, but we want to stop improvements this way once it hits 5 and only grant it 1/3rd of the time
  event.entity.data.skills.improve('skills:harvest', condition => condition.max(5).chance(33.3));

  // this is a tiered skill, we're allowing an upgrade from iron -> gold but only if the player hasn't gained the `crop_farmer` skill
  event.entity.data.skills.improve('skills:undead_killer', condition => condition
    .if(event.entity.data.skills.can('skills:undead_killer', KILLER_TIERS.iron))
    .max(KILLER_TIERS.gold)
    .unless(event.entity.data.skills.can('skills:crop_farmer'))
  );
})
```

### Global Utilities

Lastly, KubeJS scripts have access to a `PlayerSkills` object that provides the following information:

- `PlayerSkills.skillTypes`: Returns an array of all registered Skill Types
- `PlayerSkills.skills`: Returns an array of all registered Skills

### Shared Restrictions API

Restrictions are built using a callback in KubeJS (
e.g. `SomeRestrictions.register("target", builder => builder.if(player => true));`). Below are methods available to
every restriction.

#### Condition Methods

- `if`: Sets the condition which must evaluate to true in order to apply. This is a callback function with a signature
  of `Player -> Boolean`. Example: `.if(player => player.cannot('harvest', 5))`
- `unless`: Sets the condition which must evaluate to false. The callback function is the same as `if`

#### Facet Methods

- `inDimension`: Adds a facet to the restriction applying to the restriction target (entity, block) only if it is in one
  of the listed dimensions. Example: `.inDimension('overworld').inDimension('the_nether')`
- `notInDimension`: Adds a facet to the restriction applying to the target only if it is not in one of the listed
  dimensions. Example: `.notInDimension('@ad_astra')`
- `inBiome`: Adds a facet to the restriction applying to the restriction target (entity, block) only if it is in one of
  the listed biomes. Example: `.inBiome('#desert')`
- `notInBiome`: Adds a facet to the restriction applying to the target only if it is not in one of the listed biomes

### Mob Restrictions Registry Actions

We use the `MobSkillEvents.register` ***server*** event to register mob restrictions.

Spawn restrictions are then calculated at spawn time based on the players in the entity's despawn range (i.e. 128 blocks
for most mobs). If the players ***match*** the conditions, the restrictions are applied.

Other restrictions are calculated when the player attempts to interact with the mob. If the player matches the
conditions, the restriction is applied.

Restrictions can cascade with other restrictions, so any restrictions which disallow an action will trump any which do
allow it. We also expose these methods to indicate what restrictions are in place for when a player meets that
condition. By default, no restrictions are set, so be sure to set actual
restrictions. [See Player Skills documentation for the shared API](https://github.com/impleri/player-skills#kubejs-restrictions-api).

#### Allow Restriction Methods

- `nothing()` - shorthand to apply all "allow" restrictions
- `spawnable(matchAllInsteadOfAny?: boolean)` - The mob can spawn if any (or all) players in range match the criteria
- `usable()` - Players that meet the condition can interact with the entity (e.g. trade with a villager)

#### Deny Restriction Methods

- `everything()` - shorthand to apply the below "deny" abilities
- `unspawnable(matchAllInsteadOfAny?: boolean)` - The mob cannot spawn if any (or all) players in range match the
  criteria
- `unusable()` - Players that meet the condition cannot interact with the entity (e.g. trade with a villager)

#### Additional Methods

- `always()` - Change `spanwable`/`unspawnable` into an absolute value instead of a condition
- `fromSpawner(spawner: string)` - Add a spawn type

##### Spawn Types

We have a shortened list of spawn types which we are allowing granular spawn restrictions. We want to keep breeding,
spawn eggs, direct summons, and more interaction-based spawns as-is.

- `natural` - A normal, random spawn
- `spawner` - A nearby mob spawner block
- `structure` - A spawn related to a structure (e.g. guardians, wither skeletons)
- `patrol` - Really a subset of "natural" but related to illager patrols
- `chunk` - A natural spawn from when the chunk generates (e.g. villager)

#### Examples

```js
MobSkillEvents.register(event => {
  // Always prevent blazes from spawning
  event.restrict('minecraft:blaze', is => is.unspawnable().always());

  // Prevent all vanilla mobs from spawning
  event.restrict('@minecraft', is => is.unspawnable().always());

  // Prevent all illagers from spawning
  event.restrict('#raiders', is => is.unspawnable().always());

  // Prevent all vanilla illager patrols from spawning
  event.restrict('minecraft:*', is => is.unspawnable().fromSpawner("patrol").always());

  // ALLOW creepers to spawn IF ALL players in range have the `started_quest` skill
  event.restrict("minecraft:creeper", is => is.spawnable(true).if(player => player.can("skills:started_quest")));

  // ALLOW cows to spawn UNLESS ANY players in range have the `started_quest` skill
  event.restrict("minecraft:cow", is => is.spawnable().unless(player => player.can("skills:started_quest")));

  // DENY zombies from spawning IF ANY player in range has the `started_quest` skill
  event.restrict('minecraft:zombie', is => is.unspawnable().if(player => player.can('skills:started_quest')));

  // DENY sheep from spawning UNLESS ALL player in range has the `started_quest` skill
  event.restrict('minecraft:sheep', is => is.unspawnable(true).unless(player => player.can('skills:started_quest')));

  // Players cannot interact with villagers unless they have `started_quest` skill
  event.restrict("minecraft:villager", is => is.unusable().unless(player => player.can("skills:started_quest")));
});
```

## CraftTweaker API

In v2.0, CraftTweaker support was added as an alternative to KubeJS. It tries to remain very similar to the Kube API,
but some changes have been made due to how ZenScript works compared to JavaScript.

### Skills Registry Actions

For all registry actions, we use four methods to add or modify skills and one to remove them:

- `mods.playerskills.Skills.createBasic(name: String)` to create a new or modify an existing basic skill.
- `mods.playerskills.Skills.createNumeric(name: String)` to create a new or modify an existing numeric skill.
- `mods.playerskills.Skills.createTiered(name: String)` to create a new or modify an existing tiered skill.
- `mods.playerskills.Skills.createSpecialized(name: String)` to create a new or modify an existing specialized skill.
- `mods.playerskills.Skills.remove(name: String)` to remove a possibly existing skill.

Each method uses the skill name as the first parameter. This is a string that will be cast into a `ResourceLocation`, so
it must conform to ResourceLocation rules (namely, `snake_case` instead of `camelCase`). If no namespace is given, it
will automatically be placed into the `skills:` namespace. The return for each of these methods is the same: a Skill
Builder.

#### Skill Builder Methods

- `save(): Boolean` - Commits the skill to the registry. If this is not called, the skill will not be added or modified.
- `initialValue(newValue: T)` - Sets a starting value for all players
- `clearValue()` - Empties the initial value
- `description(desc: string)` - Sets the skill description
- `limitChanges(limit: number)` - How many times the skill can change before it is locked
- `unlimitedChanges()` - Allows the skill to always change
- `notifyOnChange(translationKey?: string)` - Send a notification when the skill changes (using either the provided
  translation key or the base skill name)
- `clearNotification()` - Turns off notification on skill change
- `options(choices: T[])` - Sets what values will be allowed
- `sharedWithTeam()` - Sync progress to all players on a team for the shared skill (requires FTM Teams)
- `teamLimitedTo(amount: number)` - Limits the progress to only `amount` players on the team (requires FTB Teams)
- `percentageOfTeam(percentage: number)` = Limits the progress to a `percentage` of the players on the team (requires
  FTB Teams)
- `splitEvenlyAcrossTeam()` - Limits the progress of a _specialization_ skill so that there must be an even distribution
  of specializations across the team.
- `pyramid()` - Limits the progress of a _tiered_ skill so that fewer and fewer players on a team can progress until
  only one player has the highest tier.

#### Add a skill

By default, we provide a simple boolean skill type (yes/no) which resembles what comes from Game Stages and Game Phases.

```zs
#priority 10

import mods.playerskills.Skills;

// Let's assume we have these tiers for `undead_killer` skill
// Because this is a class, it'll be accessible to all scripts with a lower priority 
public class KILLER_LEVELS {
  public static val stone as string = "stone";
  public static val iron as string = "iron";
  public static val gold as string = "gold";
  public static val diamond as string = "diamond";
  public static val netherite as string = "netherite";
}

Skills.basic("started_quest")
    .description("Indicates a Player has joined the Great Quest")
    .initialValue(false)
    .save();

// Shares the skill with the rest of the team
Skills.basic("team_started_quest")
    .description("Indicates a Player has joined the Great Quest for the team")
    .initialValue(false)
    .sharedWithTeam()
    .save();

// Only 16 percent of the team (rounded up) can complete the quest
Skills.basic("team_completed_quest")
    .description("Indicates a Player has completed the Great Quest for the team")
    .initialValue(false)
    .percentageOfTeam(16.0)
    .save();

// Only the first 4 Players on the team to gain this skill will receive it 
Skills.basic("team_completed_quest")
    .description("Indicates a Player has completed the Great Quest for the team")
    .initialValue(false)
    .teamLimitedTo(4)
    .save();

// Create a pyramid of tier limits (1 Nehtherite, 2 Diamond, 4 Gold, 8 Iron, 16 Stone)
Skills.tiered("undead_killer")
  .initialValue(KILLER_LEVELS.iron)
  .options([KILLER_LEVELS.stone, KILLER_LEVELS.iron, KILLER_LEVELS.gold, KILLER_LEVELS.diamond, KILLER_LEVELS.netherite] as string[])
  .description('Tier of undead killer level')
  .pyramid()
  .save();
```

#### Modify Skill

```zs
// Now we want to modify undead_killer to change the initial value
Skills.tiered("undead_killer")
  .initialValue(KILLER_LEVELS.stone)
  .save();
```

#### Remove Skill

```zs
Skills.remove('test');
```

### Expand Player Methods

Any script that access the `Player` class will also have access to several PlayerSkills methods.

- `player.skills` (or if you want `player.getSkills()`) - a List of a player's skill set. Note that we store _every_
  skill on a player, so do not use the existence of a skill for determining if a player can perform an action.
- `player.can(skillName: String, expectedValue?: T)` - Checks a specific skill if it is at or above an optional expected
  value (default is "truthy" for the skill type). `!player.can()` is the same as `player.cannot()`
- `player.cannot(skillName: String, expectedValue?: T)` - Checks a specific skill if it is below an optional expected
  value (default is "truthy" for the skill type). `!player.cannot()` is the same as `player.can()`

```zs
// This event is only in Forge
crafttweaker.api.events.CTEventManager.register<crafttweaker.api.event.entity.player.interact.RightClickBlockEvent>((event) => {
  val player = event.player;
  // Note that you'll need to be aware of how often an event can be fired (e.g. 4x here) and limit things accordingly
  if (player.level.isClientSide || event.hand != <constant:minecraft:interactionhand:main_hand>) {
    return;
  }

  val blockState = player.level.getBlockState(event.blockPos);
  if (<block:minecraft:dirt>.matches(blockState.block) && player.cannot("denied_quest")) {
    player.improveSkill("started_quest");
  }
  
  if (<block:minecraft:grass>.matches(blockState.block) && !player.can("harvest", 2)) {
    // do something here if the player does not have a high enough harvest skill when right clicking a grass block 
  }
});
```

#### Change Skills

Oftentimes, you will want to set a player's skill level based on some arbitrary rules. We don't build in those rules!
However, you will have a few options:

- `improveSkill(skill: string, min?: T, max?: T)` - Increase the value
- `degradeSkill(skill: string, min?: T, max?: T)` - Decrease the value
- `setSkill(skill: name, newValue: T)` - Set the skill to an arbitrary value
- `resetSkill(skill: name)` - Reset the skill back to the initial value

##### Examples

```zs
// This event is only in Forge
crafttweaker.api.events.CTEventManager.register<crafttweaker.api.event.entity.player.interact.RightClickBlockEvent>((event) => {
  val player = event.player;
  // Note that you'll need to be aware of how often an event can be fired (e.g. 4x here) and limit things accordingly
  if (player.level.isClientSide || event.hand != <constant:minecraft:interactionhand:main_hand>) {
    return;
  }

  val blockState = player.level.getBlockState(event.blockPos);
  if (<block:minecraft:dirt>.matches(blockState.block) && player.cannot("denied_quest")) {
    // this is a basic skill, so we ensure it's true
    player.improveSkill("skills:dirt_watcher");
    
    // this is a numeric skill, but we want to stop improvements this way once it hits 5 and only grant it 1/3rd of the time
    player.improveSkill("skills:harvest", null, 5);
    
    // this is a tiered skill, we're allowing an upgrade from iron -> gold but only if the player hasn't gained the `crop_farmer` skill
    if (player.can("skills:undead_killer", KILLER_TIERS.iron) && player.cannot("skills:crop_farmer")) {
      player.improveSkill("skills:undead_killer", null, KILLER_TIERS.gold);
    }
  }
});
```

### Shared Restrictions API

Restrictions are build as chained calls in CraftTweaker (
e.g. `SomeRestrictions.create("target").condition(player => true).save()`). These methods are available for every
restriction.

- `save`: Completes the building of this restriction and adds the restriction to the registry.

#### Condition Methods

- `condition`: Sets the condition which must evaluate to true in order to apply. This is a callback function with a
  signature
  of `Player -> Boolean`. Example: `.if(player => player.cannot('harvest', 5))`
- `unless`: Sets the condition which must evaluate to false. The callback function is the same as `if`

#### Facet Methods

- `inDimension`: Adds a facet to the restriction applying to the restriction target (entity, block) only if it is in one
  of the listed dimensions. Example: `.inDimension('overworld').inDimension('the_nether')`
- `notInDimension`: Adds a facet to the restriction applying to the target only if it is not in one of the listed
  dimensions. Example: `.notInDimension('@ad_astra')`
- `inBiome`: Adds a facet to the restriction applying to the restriction target (entity, block) only if it is in one of
  the listed biomes. Example: `.inBiome('#desert')`
- `notInBiome`: Adds a facet to the restriction applying to the target only if it is not in one of the listed biomes

### Mob Restrictions

We use the `MobRestrictions.create` static method to register mob restrictions.

Spawn restrictions are then calculated at spawn time based on the players in the entity's despawn range (i.e. 128 blocks
for most mobs). If the players ***match*** the conditions, the restrictions are applied.

Other restrictions are calculated when the player attempts to interact with the mob. If the player matches the
conditions, the restriction is applied.

Restrictions can cascade with other restrictions, so any restrictions which disallow an action will trump any which do
allow it. We also expose these methods to indicate what restrictions are in place for when a player meets that
condition. By default, no restrictions are set, so be sure to set actual
restrictions. [See Player Skills documentation for the shared API](https://github.com/impleri/player-skills#kubejs-restrictions-api).

#### Allow Builder Methods

- `nothing()` - shorthand to apply all "allow" restrictions
- `spawnable(matchAllInsteadOfAny?: boolean)` - The mob can spawn if any (or all) players in range match the criteria
- `usable()` - Players that meet the condition can interact with the entity (e.g. trade with a villager)

#### Deny Builder Methods

- `everything()` - shorthand to apply the below "deny" abilities
- `unspawnable(matchAllInsteadOfAny?: boolean)` - The mob cannot spawn if any (or all) players in range match the
  criteria
- `unusable()` - Players that meet the condition cannot interact with the entity (e.g. trade with a villager)

#### Additional Methods

- `always()` - Change `spanwable`/`unspawnable` into an absolute value instead of a condition
- `fromSpawner(spawner: string)` - Add a spawn type

##### Spawn Types

We have a shortened list of spawn types which we are allowing granular spawn restrictions. We want to keep breeding,
spawn eggs, direct summons, and more interaction-based spawns as-is.

- `natural` - A normal, random spawn
- `spawner` - A nearby mob spawner block
- `structure` - A spawn related to a structure (e.g. guardians, wither skeletons)
- `patrol` - Really a subset of "natural" but related to illager patrols
- `chunk` - A natural spawn from when the chunk generates (e.g. villager)

#### Examples

```zs
import mods.mobskills.MobSkillEvents;

// Always prevent blazes from spawning
MobSkillEvents.create('minecraft:blaze')
  .unspawnable()
  .always()
  .save();

  // Prevent all vanilla mobs from spawning
  MobSkillEvents.create('@minecraft')
    .unspawnable()
    .always()
    .save();

  // Prevent all illagers from spawning
  MobSkillEvents.create('#raiders')
  .unspawnable()
  .always()
  .save();

  // Prevent all vanilla illager patrols from spawning
  MobSkillEvents.create('minecraft:*')
  .unspawnable()
  .fromSpawner("patrol")
  .always()
  .save();

  // ALLOW creepers to spawn IF ALL players in range have the `started_quest` skill
  MobSkillEvents.create("minecraft:creeper")
    .spawnable(true)
    .if(player => player.can("skills:started_quest"))
    .save();

  // ALLOW cows to spawn UNLESS ANY players in range have the `started_quest` skill
  MobSkillEvents.create("minecraft:cow")
    .spawnable()
    .unless(player => player.can("skills:started_quest"))
    .save();

  // DENY zombies from spawning IF ANY player in range has the `started_quest` skill
  MobSkillEvents.create('minecraft:zombie')
    .unspawnable().if(player => player.can('skills:started_quest'))
    .save();

  // DENY sheep from spawning UNLESS ALL player in range has the `started_quest` skill
  MobSkillEvents.create('minecraft:sheep')
    .unspawnable(true)
    .unless(player => player.can('skills:started_quest'))
    .save();

  // Players cannot interact with villagers unless they have `started_quest` skill
  MobSkillEvents.create("minecraft:villager")
    .unusable()
    .unless(player => player.can("skills:started_quest"))
    .save();
```

## Java API

Registering Skills and SkillTypes should happen during initialization (see `PlayerSkills.registerTypes`) using
a `DeferredRegister` to ensure it is happens at the right time.

```java
package my.custom.mod;

import dev.architectury.registry.registries.DeferredRegister;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import my.custom.mod.custom.CustomSkill;
import my.custom.mod.custom.CustomSkillType;

public class ExampleMod {
    public static final String MOD_ID = 'mycustommod';
    private static final ResourceKey<Registry<Skill<?>>> SKILL_REGISTRY = ResourceKey.createRegistryKey(Skill.REGISTRY_KEY);
    private static final DeferredRegister<Skill<?>> SKILLS = DeferredRegister.create(MOD_ID, SKILL_REGISTRY);

    private static final ResourceKey<Registry<SkillType<?>>> SKILL_TYPE_REGISTRY = ResourceKey.createRegistryKey(SkillType.REGISTRY_KEY);
    private static final DeferredRegister<SkillType<?>> SKILL_TYPES = DeferredRegister.create(MOD_ID, SKILL_TYPE_REGISTRY);

    public ExampleMod() {
        // All that is needed to register a skill type
        SKILL_TYPES.register(CustomSkillType.name, CustomSkillType::new);
        SKILL_TYPES.register();

        // And to register a skill
        ResourceLocation skillName = SkillResourceLocation.of("test");
        SKILLS.register(skillName, () -> new CustomSkill(skillName));
        SKILLS.register();
    }
}

```

`SkillType`s are available in both the logical server and the logical client sides
via `net.impleri.playerskills.api.SkillType` static methods. Post-modification `Skill`s are only available on the server
side via `net.impleri.playerskills.server.api.Skill` static methods. Validating a player's skills (`can`) can be done on
both client (`net.impleri.playerskills.client.PlayerClient`) and server (`net.impleri.playerskills.api.Player`).

Any manipulation to those skills (`set`) can only happen on the server side. It should be noted that the API layer does
have the convenience methods (e.g. `improve`, `degrade`).

### Networking

We expose a pair of network messages for communicating between the client and server: `SyncSkills` and `ResyncSkills`.
The server side sends `SyncSkills` whenever the local player on the client has updated skills. It sends the player's
most recent set of skills and gets cached in the client-side registry which supplies data necessary for the `ClientApi`.
The client side can also request an update via the `ResyncSkills` message.

### Events

Events are now triggered on both the server side and the client side as a player's skills are changed. On the server
side, `SkillChangedEvent` is broadcast with information about the specific skill change. This is consumed in `KubeJS`
scripts as it is rebroadcast to the `SkillEvents.onChanged` handler. It is also consumed on the server side network
handler which updates that specific player's client side.

When a client receives an updated list of skills, `ClientSkillsUpdatedEvent` is broadcast for any client-side libraries
to handle updates.

## In-Game Commands

Lastly, we expose a handful of in-game commands for players and mods:

- `/skills types`: List all registered skill types
- `/skills all`: List all registered skills (post-modification)
- `/skills mine`: List the current player's skills _and values_
- `/skills team share`: Syncs the current player's team-shared skills to the rest of the player's team overriding their
  values. (e.g. if Player 1 had kill_count of 3 and Player 2 had a kill_count of 8, both players would have a value of 3
  after Player 1 executes the command)
- `/skills team sync [player]`: Syncs the player's team's team-shared skills to the rest of the player's team overriding
  their values with the "best" value. (if Player 1 had kill_count of 3 and Player 2 had a kill_count of 8, both players
  would have a value of 8 after an op runs the command targeting Player 1). Requires mod permissions.
- `/skills debug [category]`: Toggles debug-level logging for Player Skills. Requires mod permissions.
- `/skills set [player] skill value`: Set the `skill`'s value to `value` for the player (omitting a player targets the
  one performing the command). Note that this requires mod permissions.

## Developers

Add the following to your `build.gradle`. I depend
on [Architectury API](https://github.com/architectury/architectury-api)
and [KubeJS](https://github.com/KubeJS-Mods/KubeJS), so you'll need those as well.

```groovy
dependencies {
    // Common should always be included 
    modCompileOnly "net.impleri:playerskills-${minecraft_version}:${playerskills_version}"

    // Plus forge
    modImplementation "net.impleri:player-skills-${minecraft_version}-forge:${playerskills_version}"

    // Or fabric
    modImplementation "net.impleri:player-skills-${minecraft_version}-fabric:${playerskills_version}"
}

repositories {
    maven {
        url = "https://maven.impleri.org/minecraft"
        name = "Impleri Mods"
        content {
            includeGroup "net.impleri"
        }
    }
}

```

## Modpacks

Want to use this in a modpack? Great! This waBecause os designed with modpack developers in mind. No need to ask.
