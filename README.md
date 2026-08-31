# Space Shooter — Android Studio Project

Procedurally-generated 2D top-down space shooter, Kotlin, Canvas-based game loop for
gameplay + Jetpack Compose for menus/Hangar. Fully offline, no external services.

## Opening the project (Android Studio)

1. Unzip this project anywhere on disk.
2. Open Android Studio → **Open** → select the `SpaceShooter` folder.
3. Let Gradle sync (Gradle 8.7 / AGP 8.5.2 / Kotlin 1.9.24 are pinned in the build files).
4. Press **Run ▶** with a device/emulator selected (minSdk 24 / Android 7+).

## Building the APK without Android Studio (GitHub Actions)

This project includes `.github/workflows/build-apk.yml`, which builds a debug APK in the
cloud on every push — you don't need Android Studio or any local Android SDK at all.

1. On GitHub, create a new **empty** repository (no README/gitignore).
2. Upload this whole `SpaceShooter` folder to it. Easiest way if you don't use git:
   go to the repo page → **"Add file" → "Upload files"** → drag in everything from the
   unzipped `SpaceShooter` folder (including the hidden `.github` folder — if your file
   picker hides dotfiles, use `git` instead, see below) → **Commit changes**.
   With git installed locally:
   ```
   cd SpaceShooter
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/<your-username>/<repo-name>.git
   git push -u origin main
   ```
3. Go to the **Actions** tab of your repo — a "Build APK" run should start automatically.
   Wait for the green checkmark (a few minutes).
4. Open that run → scroll to **Artifacts** → download **SpaceShooter-debug-apk**.
   It's a zip containing `app-debug.apk` — that's the installable file.
5. Transfer the APK to your phone (email, cloud drive, USB) and open it to install.
   You'll need to allow "Install from unknown sources" for whichever app you use to open it.

This is a **debug** build (unsigned, fine for installing on your own device). If you
later want to publish it, that needs a signing key — say so and I'll add that step.

## Project layout

```
app/src/main/java/com/spacegame/shooter/
  MainActivity.kt              Compose host: Main Menu <-> Hangar
  game/
    GameActivity.kt            Hosts the SurfaceView gameplay screen
    GameView.kt                SurfaceView: input + Canvas rendering
    GameThread.kt               Fixed-timestep loop (~60fps) on a background thread
    GameEngine.kt               Central game state/update, UI-agnostic
    entities/                  Player, Enemy, Bullet, PowerUp, Explosion
    formations/
      FormationGenerator.kt    8 geometric formation templates (line, V, circle,
                                diamond, cross, spiral, wave, zigzag) + hidden
                                3-lane system used only internally for spacing
      LevelGenerator.kt        Turns a level number into waves: difficulty curve,
                                enemy budget, per-wave fairness caps, elite/boss chance
    systems/
      CollisionSystem.kt       All hit-detection & its consequences
      SpawnSystem.kt           Converts a LevelPlan into timed Enemy spawns
      WeaponSystem.kt          Player + enemy firing logic, power-up-aware
  data/
    ShipData.kt                6 playable ship archetypes + base stats
    UpgradeData.kt              Upgrade levels/costs, resolves base+upgrades -> stats
    SaveManager.kt              SharedPreferences-backed permanent progress
  ui/
    MainMenuScreen.kt
    HangarScreen.kt              Ship select/unlock + upgrade shop (Compose)
res/drawable/                  44 game sprites (see below)
```

## Game loop

`Hangar → pick ship → Launch → GameActivity/GameView → fight procedurally generated
levels → on death, currency is banked back into SaveManager → back to Hangar → spend
currency on upgrades or unlock a new hull → repeat.`

## Procedural generation summary

- `FormationGenerator` produces spawn layouts in **relative** coordinates (0..1 across
  width, "row" units for vertical offset) so they're resolution-independent. Internally
  it also assigns each spawn to one of 3 invisible lanes — this is only used to keep the
  generator's own bookkeeping organized and is never drawn or exposed to the player.
- `LevelGenerator` combines formations + enemy types + timing into a `LevelPlan`, capping
  enemies-per-wave and enforcing a minimum gap between waves so no level can generate an
  unfair simultaneous wall of enemies. Elites unlock from level 3 onward with a capped
  probability; bosses appear every 5th level.
- `SpawnSystem` walks the plan against elapsed time and emits real `Enemy` instances.

## Assets (`res/drawable/`, 44 files)

| Category | Files |
|---|---|
| Player ships (6) | `ship_player_interceptor/scout/advanced/heavy/widebody/flagship` |
| Basic enemies (6) | `enemy_basic_delta/interceptor/twinturret/cruiser`, `enemy_heavy_gunship`, `enemy_fast_striker` |
| Elites (4) | `enemy_elite_triblade/render/gunboat/hexturret` |
| Bosses (2) | `enemy_boss_mech`, `enemy_boss_orb` |
| Projectiles (6) | `proj_energy_ball/double_shot/laser_beam/missile/homing_missile/energy_orb` |
| Power-up icons (9) | `icon_powerup_doubleweapon/tripleweapon/shieldring/slowmotion/rapidfire/homing/health`, `icon_pause` |
| Explosions (4) | `explosion_medium_smoke/medium/large/boss`, `fx_explosion_small` |
| Effects (3) | `fx_shield_bubble`, `fx_engine_flame`, `fx_target_reticle`, `fx_powerup_orb` |
| Asteroids (3) | `asteroid_small/medium/large` |

**Note on the elite/boss set (`enemy_elite_*`, `enemy_boss_mech`, `enemy_boss_orb`):**
these came from the sheet your AI labeled "player spaceships," but visually (mech walker,
turret pods, armored orbs) they read as elite/boss enemy designs, so I wired them in as
the elite + boss enemy roster instead. If you actually intended some of them as
selectable player hulls, tell me which ones and I'll move them into `ShipRoster` in
`ShipData.kt` and give them a Hangar card — it's a small change.

To add or swap any sprite: drop a PNG with a transparent background into `res/drawable/`
using the same lowercase_underscore name referenced in `ShipData.kt` / `EnemyRoster` /
`ProjectileKind` / `PowerUpType` / `ExplosionKind`, and it's picked up automatically
(sprites are loaded by resource name at runtime, no manual registration needed).

## What's implemented vs. still a stub

Implemented: procedural formations/levels, spawning, collisions, weapons incl. all
power-up types, save/load, Hangar with unlock+upgrade economy, 6 ships, 12 enemy types
incl. elites/bosses, HUD (score/level/health/shield/active power-up/pause).

Still worth adding next: a proper level-complete/game-over Compose overlay (currently
drawn as plain Canvas text), sound, and pooling for bullets/explosions if you start
seeing GC hitches at high enemy counts. Happy to keep going on any of these.
