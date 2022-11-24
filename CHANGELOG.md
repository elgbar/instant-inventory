# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

### Changed

### Fixed

### Removed

---

## 1.0.1 - 2022-11-24

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