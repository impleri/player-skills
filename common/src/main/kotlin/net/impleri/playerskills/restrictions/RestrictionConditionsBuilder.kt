package net.impleri.playerskills.restrictions

interface RestrictionConditionsBuilder<Target, Player, Restriction : AbstractRestriction<Target>> :
  PlayerConditions<Player>,
  BiomeConditions<Target, Restriction>,
  DimensionConditions<Target, Restriction>
