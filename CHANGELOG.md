# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added

### Changed

### Fixed

### Removed

---

## 1.2.4 - 2025-08-22

### Changed

* Cleanup internal varbit service

### Fixed

* Fix allowing items to be unequipped and dropped in the same tick

---

## 1.2.2 - 2025-02-08

### Changed

* Update badges to api.runelite.net, completes [#23](https://github.com/elgbar/instant-inventory/issues/23)
* Move `Appearance` config section to the top above `Features`

### Fixed

* Add support for locked inventory bank slots, completes [#24](https://github.com/elgbar/instant-inventory/issues/24)
* Fix [#21](https://github.com/elgbar/instant-inventory/issues/21) equipping 2H when having one-handed and shield and a
  full inventory
* Fix missing inventory containers, it appears to be different widgets for fixed and resizeable modes

---

## 1.2.1 - 2024-03-02

### Fixed

* Fix issue [#13](https://github.com/elgbar/instant-inventory/issues/13), incompatibility with customized shift-clicking
  from the Menu Swapper plugin
* Fix issue [#16](https://github.com/elgbar/instant-inventory/issues/16), depositing into the group ironman storage does
  not work correctly
* Fix wrong quantity when depositing `All` when the item is not stackable

---

## 1.2.0 - 2024-02-11

### Added

* Add instant equip items feature, completes [#5](https://github.com/elgbar/instant-inventory/issues/5)
  * Updates the inventory to how it shows when the item is equipped
* Add instant withdraw feature, completes rest of [#2](https://github.com/elgbar/instant-inventory/issues/2)
  * Updates widget in bank and in inventory
* Deposit all items in inventory into the bank instantly when clicking `Deposit inventory` (also part
  of [#2](https://github.com/elgbar/instant-inventory/issues/2))
* Hide and changed item opacity is configurable
* Add instant deposit when clicking `Add all` in the guide prices interface

### Changed

* Only reset all features when the game stage changes to `LOGGED_IN`
* Simplify the clean feature by editing inventory widgets directly, instead of using an overlay
* Update configuration sections

### Fixed

* Fix the quantity of the item in the bank not being updated client-side when depositing items
* Fix missing unit in config
* Do not try to hide dropped items that are considered valuable
* Parse `-All` and `-All-but-1` in the menu entry correctly

---

## 1.1.2 - 2023-09-08

### Fixed

* Fix issue [#8](https://github.com/elgbar/instant-inventory/issues/8) showing items other plugins have hidden

---

## 1.1.1 - 2023-04-21

### Fixed

* Fix erroneously hiding stacked/noted items when depositing less than all items

---

## 1.1.0 - 2023-04-11

### Added

* Instant deposit items into the bank, completes part of [#2](https://github.com/elgbar/instant-inventory/issues/2)

### Changed

* Move feature toggling config items into their own config section
* Update copyright to 2023 and apply to all relevant files

---

## 1.0.1 - 2022-11-30

## Added

* The number of ticks an item is hidden is customizable,
  completes [#4](https://github.com/elgbar/instant-inventory/issues/4)

### Changed

* Rewrite most of the plugin to easier be able to add additional features
* When a feature is disabled, events will no longer be listened to
* Update plugin description
* Update plugin tags
* Update config display names

### Fixed

* When trying to clean a herb you do not have the level to clean, the cleaned herb would be shown
  anyway
* Failing to drop an item would indefinitely hide it

---

## 1.0.0 - 2022-11-21

### Added

* When items are `Drop`ed (either by shift-click or via menu) the item will be hidden
* The cleaned herb is shown instantly when `Clean`ing a grimy herb
* Config to toggle dropping and cleaning
