# Player Skills

A library mod that provides a baseline implementation of player-specific skills. On its own, this does very little as it
is meant to be as un-opinionated as possible.

Like GameStages and GamePhases, this provides a registry for tracking possible skills as well what skills a given player
currently possesses. Unlike the other mods, skills are added to a player immediately and leverage ability checking via
Skill Types.

[![CurseForge](https://cf.way2muchnoise.eu/short_711489.svg)](https://www.curseforge.com/minecraft/mc-mods/player-skills)
[![Modrinth](https://img.shields.io/modrinth/dt/player-skills?color=bcdeb7&label=%20&logo=modrinth&logoColor=096765&style=plastic)](https://modrinth.com/mod/player-skills)
[![MIT license](https://img.shields.io/github/license/impleri/player-skills?color=bcdeb7&label=Source&logo=github&style=flat)](https://github.com/impleri/player-skills)
[![Discord](https://img.shields.io/discord/1093178610950623233?color=096765&label=Community&logo=discord&logoColor=bcdeb7&style=plastic)](https://discord.com/invite/avxJgbaUmG)
[![1.19.2](https://img.shields.io/maven-metadata/v?label=1.19.2&color=096765&metadataUrl=https%3A%2F%2Fmaven.impleri.org%2Fminecraft%2Fnet%2Fimpleri%2Fplayer-skills-1.19.2%2Fmaven-metadata.xml&style=flat)](https://github.com/impleri/player-skills/tree/1.19.2#developers)
[![1.18.2](https://img.shields.io/maven-metadata/v?label=1.18.2&color=096765&metadataUrl=https%3A%2F%2Fmaven.impleri.org%2Fminecraft%2Fnet%2Fimpleri%2Fplayer-skills-1.18.2%2Fmaven-metadata.xml&style=flat)](https://github.com/impleri/player-skills/tree/1.18.2#developers)

### xSkills Mods

[Player Skills](https://github.com/impleri/player-skills)
| [Block Skills](https://github.com/impleri/block-skills)
| [Dimension Skills](https://github.com/impleri/dimension-skills)
| [Fluid Skills](https://github.com/impleri/fluid-skills)
| [Item Skills](https://github.com/impleri/item-skills)
| [Mob Skills](https://github.com/impleri/mob-skills)

## Concepts

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

## KubeJS API

### Registry Actions

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

## KubeJS Restrictions API

The various xSkills mods utilize a shared Restrictions API for managing restrictions. These are contained within
PlayerSkills but are surfaced in the dependent mod. These are documented here in order to keep documentation as
up-to-date as possible and the other mods may link back to here.

### ID Parsing

Creating restrictions can be tedious. In order to help reduce that, all restriction identifiers as well as dimension and
biome facets share the same identifier parsing. This means that string identifiers can be used (e.g. `minecraft:zombie`)
as well as mod IDs (e.g. `minecraft:*` or `@minecraft`) and tags (`#minecraft:desert` or `#desert`) can be used where
appropriate. Note that not everything uses tags (e.g. dimensions), so it won't work with those.

### Condition Methods

- `if`: Sets the condition which must evaluate to true in order to apply. This is a callback function with a signature
  of `Player -> Boolean`. Example: `.if(player => player.cannot('harvest', 5))`
- `unless`: Sets the condition which must evaluate to false. The callback function is the same as `if`

### Facet Methods

- `inDimension`: Adds a facet to the restriction applying to the restriction target (entity, block) only if it is in one
  of the listed dimensions. Example: `.inDimension('overworld').inDimension('the_nether')`
- `notInDimension`: Adds a facet to the restriction applying to the target only if it is not in one of the listed
  dimensions. Example: `.notInDimension('@ad_astra')`
- `inBiome`: Adds a facet to the restriction applying to the restriction target (entity, block) only if it is in one of
  the listed biomes. Example: `.inBiome('#desert')`
- `notInBiome`: Adds a facet to the restriction applying to the target only if it is not in one of the listed biomes

## Java API

Registering Skills and SkillTypes should happen during initialization (see `PlayerSkills.registerTypes`) using
a `DeferredRegister` to ensure it is happens at the right time.

```java
package my.custom.mod;

import dev.architectury.registry.registries.DeferredRegister;
import net.impleri.playerskills.registry.SkillTypes;
import net.impleri.playerskills.server.registry.Skills;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import my.custom.mod.custom.CustomSkill;
import my.custom.mod.custom.CustomSkillType;

public class ExampleMod {
    public static final String MOD_ID = 'mycustommod';
    private static final ResourceKey<Registry<Skill<?>>> SKILL_REGISTRY = ResourceKey.createRegistryKey(Skills.REGISTRY_KEY);
    private static final DeferredRegister<Skill<?>> SKILLS = DeferredRegister.create(MOD_ID, SKILL_REGISTRY);

    private static final ResourceKey<Registry<SkillType<?>>> SKILL_TYPE_REGISTRY = ResourceKey.createRegistryKey(SkillTypes.REGISTRY_KEY);
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
both client (`net.impleri.playerskills.client.ClientApi`) and server (`net.impleri.playerskills.server.ServerApi`).

Any manipulation to those skills (`set`) can only happen on the server side. It should be noted that the API layer does
not have the convenience methods exposed to KubeJS (`improve`, `degrade`) nor the built-in checking for conditions as
that functionality is expected to be handled at the modpack level via KubeJS scripts.

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
- `/skills debug`: Toggles debug-level logging for Player Skills. Requires mod permissions.
- `/skills set [player] skill value`: Set the `skill`'s value to `value` for the player (omitting a player targets the
  one performing the command). Note that this requires mod permissions.

## Developers

Add the following to your `build.gradle`. I depend
on [Architectury API](https://github.com/architectury/architectury-api)
and [KubeJS](https://github.com/KubeJS-Mods/KubeJS), so you'll need those as well.

```groovy
dependencies {
    // Common should always be included 
    modImplementation "net.impleri:player-skills-${minecraft_version}:${playerskills_version}"

    // Plus forge
    modApi "net.impleri:player-skills-${minecraft_version}-forge:${playerskills_version}"

    // Or fabric
    modApi "net.impleri:player-skills-${minecraft_version}-fabric:${playerskills_version}"
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
