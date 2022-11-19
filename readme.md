# Player Skills

A library mod that provides a baseline implementation of player-specific skills. On its own, this does very little as it
is meant to be as un-opinionated as possible.

Like GameStages and GamePhases, this provides a registry for tracking possible skills as well what skills a given player
currently possesses. Unlike the other mods, skills are added to a player immediately and leverage ability checking via
Skill Types.

## Concepts

This mod provides two entities that can be extended: Skill Types and Skills.

### Skill Type

A skill type handles all logic around managing skills tied to its type. That includes providing a way to (de)serialize
the skill into a persistent format. More advanced use cases can leverage the skill type for determining if a skill is of
a sufficient threshold. Because of this nature, skill types cannot be created via KubeJS.

#### Builtin Types

1. `Basic` provides a simple boolean `true`/`false`. Great for simple skills such as "can read."
2. `Numeric` provides an integer value (0, 1, 2, etc). Great for skills which can be improved such as "can climb."
3. `Tiered` are specialized version of `Numeric` in that strings can be used. One example could be university
   education (undergraduate, postgraduate, doctorate)
4. `Specialization` provide a skill which allows one or more specializations out of a list (e.g. "can pick {X} lock")

### SKill

A skill tracks the data needed for SKill Types to calculate if a player _can_ perform a skill-based action. Skills get
serialized and persisted in player-specific NBT data. Skills are exposed via KubeJS for modpack authors to manipulate as
they see fit.

## KubeJS API

### Registry Actions

For all registry actions, we use the same `SkillEvents.skills` event. Every action on the event starts with the skill
name. This is a string that will be cast into a ResourceLocation, so it must conform to ResourceLocation rules (namely,
snake_case instead of camelCase). If no namespace is given, it will automatically be placed into the `skills:`
namespace.

When adding a skill, you must provide a skill type string as the second parameter. Like the name, this will be cast into
a ResourceLocation and given the default `skills:` namespace if none provided. If you are modifying a skill, you may add
a skill type parameter after the name to _change_ the skill typing.

For both adding and modifying skills, the final argument is a callback function which will interact with the skill
builder for the type. Modifying skills will start with their existing configurations while new skills will start with
whatever the default options are for the type.

#### Add a skill

By default, we provide a simple boolean skill type (yes/no) which resembles what comes from Game Stages and Game Phases.

```js
SkillEvents.skills(event => {
  event.add('skills:started_quest', 'basic', skill => {
    skill.initialValue(false)
      .description('Indicates a Player has joined the Great Quest');
  });
});
```

#### Modify Skill

```js
SkillEvents.skills(event => {
  event.modify('skills:test', skill => {
    skill.initialValue(true)
      .description('Less of a test value');
  });
});
```

#### Remove Skill

```js
SkillEvents.skills(event => {
  event.remove('test');
});
```

### Player Actions

Whenever an event is triggered in KubeJS that has a Player associated with it, we attach some Player-specific functions.

#### Get Player's Current Skill Set

KubeJS will have read-only access to a player's skill set. Note that we store _every_ skill on a player, so do not use
this for determining if a player can perform an action.

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
    event.block.createEntity('green_guardian').spawn()
  }
})
```

This example is expecting `skills:harvest` to be a `Numeric` skill and checks if the player has a skill level of 2 ***or
greater***. If no skill value is used (i.e. `event.entity.data.skills.can('skills:harvest')`), then the check only looks
to see if the skill is truthy (`true`, greater than 0, not `null`). If you want to invert that check, you can use
`cannot` instead of `can`.

#### Set

Oftentimes, you will want to set a player's skill level based on some arbitrary rules. We don't build in those rules!
However, you will have a few options. Sometimes you will want to simply improve a skill:

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

BlockEvents.rightClicked('minecraft:dirt', event => {
  // this is a basic skill, so we ensure it's true
  event.entity.data.skills.improve('skills:dirt_watcher');
  
  // this is a numeric skill, but we want to stop improvements this way once it hits 5
  event.entity.data.skills.improve('skills:harvest', condition => condition.max(5));
  
  // this is a tiered skill, we're allowing an upgrade from iron -> gold but only if the player hasn't gained the `crop_farmer` skill
  event.entity.data.skills.improve('skills:undead_killer', condition => condition
    .if(event.entity.data.skills.can('skills:undead_killer', KILLER_TIERS.iron))
    .max(KILLER_TIERS.gold)
    .unless(event.entity.data.skills.can('skills:crop_farmer'))
  );
})
```

The following conditions are available in the second parameter callback:

- `min`: If the player's skill is below the minimum, it will jump to the minimum value. If it's at or above, it will
  increment appropriately
- `max`: If the player's skill is at or above the maximum value, nothing will change. Otherwise, increment the skill
  appropriately
- `if`: Add an expression which evaluates to a boolean value. Will only increment the value if this is true
- `unless`: Add an expression which evaluates to a boolean value. Will only increment the value if this is false

The same can apply in reverse: if you want to reduce a skill level, use `degrade`. Conditions will be adapted
appropriately.

There's also a shorthand method for resetting a skill back to the game registry's default: `reset`. Like `improve` and
`degrade`, it can take conditions.

### Global Utilities

Lastly, KubeJS scripts have access to a `PlayerSkills` object that provides the following information:

- `PlayerSkills.skillTypes`: Returns an array of all registered Skill Types
- `PlayerSkills.skills`: Returns an array of all registered Skills

## Java API

Registering Skills and SkillTypes should happen during initialization (see `PlayerSkillsCore.registerCoreTypes`) using
a `DeferredRegister` to ensure it is happens at the right time.

## Modpacks

Want to use this in a modpack? Great! This was designed with modpack developers in mind. No need to ask.

## TODO

- [] Specialization skill type
