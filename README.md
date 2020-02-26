# MidPoint Studio

## Overview
Plugin for Intellij Idea to help identity engineers customize [MidPoint](https://midpoint.evolveum.com) identity 
management solution by [Evolveum](https://evolveum.com).

## Features

## License

Project is licensed under Apache Licence 2.0.

## Development Notes

### Git Branches

* Development branch is `master`. 
* Early access preview `eap`
* Branch used for releases is called `stable`.

### Logging settings

Intellij Idea logs to file `studio-idea-plugin/build/idea-sandbox/system/log/idea.log`. Logging settings can be changed 
during runtime via top menu `Help/Debug Log Settings` by adding packages/classes on each line in format:

* `#LOGGER` to log LOGGER (package or class) on DEBUG level
* `#LOGGER:trace` to log LOGGER (package or class) on TRACE level