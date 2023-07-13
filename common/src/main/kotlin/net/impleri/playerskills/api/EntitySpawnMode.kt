package net.impleri.playerskills.api

enum class EntitySpawnMode {
  /**
   * Allow the spawn always.
   */
  ALLOW_ALWAYS,

  /**
   * Allow the spawn if any player matches the predicate.
   */
  ALLOW_IF_ANY_MATCH,

  /**
   * Allow the spawn if all players match the predicate.
   */
  ALLOW_IF_ALL_MATCH,

  /**
   * Allow the spawn if all players do not match the predicate.
   */
  ALLOW_UNLESS_ANY_MATCH,

  /**
   * Allow the spawn if any player does not match the predicate.
   */
  ALLOW_UNLESS_ALL_MATCH,

  /**
   * Deny the spawn if any player match the predicate.
   * Allow the spawn if all players do not match the predicate.
   */
  DENY_IF_ANY_MATCH,

  /**
   * Deny the spawn if all players match the predicate.
   * Allow the spawn if any player does not match the predicate.
   */
  DENY_IF_ALL_MATCH,

  /**
   * Deny the spawn if no player match the predicate.
   * Allow the spawn if any player matches the predicate.
   */
  DENY_UNLESS_ANY_MATCH,

  /**
   * Deny the spawn if all players do not match the predicate.
   * Allow the spawn if all players match the predicate.
   */
  DENY_UNLESS_ALL_MATCH,

  /**
   * Deny the spawn always.
   */
  DENY_ALWAYS,
}
