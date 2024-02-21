# Player Skills

A library mod that handles everything related to player-specific skills and skill-based restrictions. Included in the
box are the following types of restrictions:

* Mob Spawning
* Mob Interaction (i.e. right-clicking to trade with a villager)
* Item equipment
* Item interactions (recognition in inventories, usability)
* Recipe visibility (JEI and REI)

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
restrictions here.

#### Mob Restriction

Mob restrictions target how players interact with mobs, including (dis)-allowing what mobs can spawn.

#### Item Restriction

An item restriction handles interaction and inventory of items. Restrictions can prevent holding or equipping items (
including as Curios/Trinkets), stop an item from being used as a tool or a weapon, prevent a recipe from working, and
reduce or remove its visibility from JEI and REI.

#### ID Parsing

Creating restrictions can be tedious. In order to help reduce that, all restriction identifiers as well as dimension and
biome facets share the same identifier parsing. This means that string identifiers can be used (e.g. `minecraft:zombie`)
as well as mod IDs (e.g. `minecraft:*` or `@minecraft`) and tags (`#minecraft:desert` or `#desert`) can be used where
appropriate. Note that not everything uses tags (e.g. dimensions), so it won't work with those.

##### NBT Parsing

Certain resources (e.g. items, blocks) may also have NBT data. These can be represented using data tags in combination
with the resource location. When parsing these types of resources, if it is not determined to be a namespace or tag,
we will attempt to parse the resource location with the data tags.

## Caveats

Because of the way we're handling spawn conditions, if you are using an `unless` condition, you should not also
manipulate `usable` in the same restriction. Just keep the two restrictions separate.

When using this with JEI or REI, please be sure to disable the vanilla recipe book as the restricted items can appear
there despite being uncraftable as this mod does not remove any actual recipe. Instead of removing recipes, it restricts
the crafting result when a player tries to craft an item and hides the recipes in JEI/REI. JEI integration does not
remove recipes related to the `unconsumable` flag. It does hide the recipes from right-clicking on the ingredient.
However, it does not remove the recipe itself -- only `unproducible` does that. That is, a crafty player could view a
recipe that produces the item, then right click on the produced item to see what recipes with which it can be consumed.

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
- `everyting`: Sets all restrictions to the default allowed state.
- `nothing`: Sets all restrictions to the restrictive state.

In either case, the condition provided can be a single condition object or an array of them. Also of note is that skills
registered via Data Pack will not have the

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

### Item Restrictions Data

Mob Restrictions are created using the `item_restrictions` grouping. In addition to the shared facet and condition
properties above, the schema exposes:

- `item`: ***required*** String representation of the item. See above ID Parsing section for what values are allowed.
- `identifiable`: Can this item be identified by a tooltip? (default value is `true`)
- `holdable`: Can this item be held in the player's inventory? (default value is `true`)
- `wearable`: Can this item be equipped by the player as armor, trinket, or curio? (default value is `true`)
- `usable`: Can this item be used as an item? This only applies if the item has a left- or right-click use in some way (
  default value is `true`)
- `harmful`: Can this item be used to harm a mob? (default value is `true`)

```json
{
  "item": "minecraft:shears",
  "usable": false,
  "identifiable": false,
  "unless": {
    "skill": "kill_count",
    "value": 8
  }
}

```

### Recipe Restrictions Data

Mob Restrictions are created using the `recipe_restrictions` grouping. In addition to the shared facet and condition
properties above, the schema exposes:

- `recipe`: ***required*** array of objects describing the recipe using `type`, `output`, and `ingredients`
    * `type`: Type of recipe to target (e.g. `smelting`, `crafting`). Default is `crafting`
    * `output`: String representation of the produced item. See above ID Parsing section for what values are allowed.
    * `ingredients`: Array of string representation of one or more items consumed in the recipe. See above ID Parsing
      section for what values are allowed.
- `producible`: Can this item be produced by any recipe? (default value is `true`)

The recipe data `output` and `ingredients` will be used when searching through registered recipes and all matching
entries will be restricted.

```json
{
  "recipe": [
    {
      "type": "smelting",
      "output": "minecraft:stone",
      "ingredients": [
        "minecraft:cobblestone"
      ]
    },
    {
      "type": "crafting",
      "output": "potion{Potion:\"minecraft:night_vision\"}",
      "ingredients": [
        "#minecraft:stone_bricks"
      ]
    }
  ],
  "producible": false,
  "unless": {
    "skill": "kill_count",
    "value": 8
  }
}

```

### Mob Restrictions Data

Mob Restrictions are created using the `mob_restrictions` grouping. In addition to the shared facet and condition
properties above, the schema exposes:

- `entity`: ***required*** either entity tag (e.g. `#minecraft:skeletons`), identifier (`minecraft:zombie`), or
  namespace (`ad_astra:*`)
- `usable`: Can the player interact with this mob? Only applies to mobs that have a right-click interaction. (default
  value is `true`)
- `spawnable`: Can this mob can spawn? (default value is `true`)
- `allPlayers`: Whether `spawnable` applies if any (`false`) or all (`true`) players within range match the conditions
  (default value is `false`)
- `always`: Forces `spawnable` to always apply (default value is `false`)
- `spawners`: Facet property for types of spawners (e.g. `spawner`, `natural`, `chunk`, `structure`, `breeding`, etc)

```json
{
  "entity": "minecraft:creeper",
  "spawnable": false,
  "allPlayers": true,
  "spawners": {
    "include": [
      "spawner"
    ]
  }
}
```

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

## Java API

Registering Skills and SkillTypes should happen during initialization (see `PlayerSkills.registerTypes`) using
a `DeferredRegister` to ensure it is happens at the right time.

```java
package my.custom.mod;

import dev.architectury.registry.registries.DeferredRegister;
import net.impleri.playerskills.api.skills.Skill;
import net.impleri.playerskills.api.skills.SkillType;
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
        ResourceLocation skillName = new ResourceLocation("examplemod:test");
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

## Modpacks

Want to use this in a modpack? Great! This was designed with modpack developers in mind. No need to ask.
