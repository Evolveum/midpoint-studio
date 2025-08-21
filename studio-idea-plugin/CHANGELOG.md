# MidPoint Studio

## [4.9.3]
### Added
- Support for Intellij Idea 2025.2

## [4.9.2]
### Added
- Support for Intellij Idea 2025.1
- MID-9769 Added configuration option to remove metadata during cleanup
### Changed
- MID-9691 Improved bulk action result handling when no object has been processed
### Fixed
- MID-10275 Cleanup configuration fixed illegal state exception
- MID-9627 Fixing multiple places causing prism context not being available
- MID-10324 - Fixed suggestions for `@metadata` in  Midpoint Query 

## [4.9.1]
### Changed
- Support for Intellij 2024.3
- Updated midScribe dependencies
### Fixed
- MID-10157 Fixed: Can't add new path to object cleanup configuration
- Button visibility for multiple actions, also for non-midpoint projects

## [4.9.0]
### Added
- Support for Intellij 2024.1
- Improvements and configuration options for cleanup task
- Replace association shadowRef annotation/intention
- Added annotation/intention support for protected strings that aren't using secret providers
- Objects synchronization UI
- Enumerated values via valueEnumerationRef annotation are added as suggestions in autocompletion/inspections
- Support for shadow and dynamic extension schemas
### Changed
- Renamed occurrences Axiom query to MidPoint query
- Improved references handling (inlays, download/show intent, etc.)
- Improved UI for project configuration
- Improved caching for objects loaded from environment
- MID-9691 bulk actions execution now prints notification if there was console output on server
- Updated midScribe documentation generator
### Fixed
- MID-9282 Properties host injector
- MID-9383 Fixed groovy-all dependency in pom.xml in project template
- MID-9501 Missing "t" prefix in actions fixed
- MID-8404 Fixed expansion of properties in objects
- Fixed browser panel UI issues

## [4.8.0]
### Added
- Axiom query language support (highlighting, autocompletion, brackets matching) 
- Support for Intellij 2023.3

### Changed
- Updated MidPoint libraries to version 4.8

### Fixed
- Deprecation style for elements in XML files

## [4.7.1]
### Added
- support for Intellij 2023.2

### Fixed
- MID-8873 element annotator warning fixed

## [4.7.0]
### Added
- MID-8404 added new option ignoreMissingKeys
- MID-8527 _metadata XSD schema support
- Better detection of midpoint files
- Better midpoint expansion syntax highlighting $(...)

### Changed
- MidPoint libraries updated to 4.7

### Fixed
- MID-8744 Fixed remote diff of files that contain file includes
- MID-8733 Improved create/open midpoint project (also improved support for maven fast import in 2023.1)

## [4.6.2]
### Added
- Support for Idea 2023.1

### Fixed
- MID-8521 improved searching for authorization action URI

## [4.6.1]
### Added
- MID-8304 if midpoint namespace url is defined by user as external resource warning will be printed to midpoint console now

### Changed
- MID-8411 improved naming and confirmation messages for delete operation

### Fixed
- MID-8376 fix for code element not being marked and highlighted as groovy in sysconfig/globalPolicies
- MID-8397 updated template for new projects (4.4 -> 4.6)
- MID-8411 fixed non-existed MainToolbarRight group id for positioning in 2022.2 (that group exists in 2022.3)
- MID-8453 fixed groovy code injection in s:script/s:code (reports)
- MID-8452 improved problem with groovy reference via $(@FILE_PATH)

## [4.6.0]
### Added
- Added support for 2022.3
- MID-7672 autocomplete and highlighting for xml queries in browser panel
- Added panelType contributor, autocomplete support (if java lib for admin-gui is on classpath as maven dependency)

### Changed
- Studio now uses MidPoint 4.6 libraries
- Removed support for 2021.*

### Fixed
- MID-8076 fix for remote diff editor "User data is not supported"
- Multiple occurrences of premature plugin components initialization
- MID-8000 improved environment combobox selection, added colors
- MID-8128 fixed task upgrade to activity (stax classpath issue)
- MID-8045 password field handling in configuration editor

## [4.5.1]
### Added
- Support for IC 2022.2

### Changed
- Removed support for IC 2020.3

## [4.5.0]
### Fixed
- MID-7658 task upgrade action fix
- MID-7691 console logging fixes, upload/test resource weren't logged correctly
- MID-7695 namespace variants improvements
- MID-7735 upload/recompute now recomputes all uploaded objects
- MID-7810 encrypted credentials not expanded in diff now

## [4.4.2]
### Changed
- Changed format of local/remote diff (internal XML file representation)

### Fixed
- MID-7658 fix for task upgrade, when task had 528 archetype and no/wrong handler uri - delete task was created incorrectly
- MID-7658 invalid handler task test (generates no change)
- MID-7695 namespace variants missing fix
- MID-7691 console logs didn't correspond with task being executed (upload/test resource)

## [4.4.1]
### Added
- Model authorizations were added to list of possibilities when completion dialog is shown
- Added partial AXIOM query support
- Create diff of two local files that contain midPoint objects
- Internal midPoint libraries updated to 4.4.1

### Changed
- isImport option is now being sent correctly during upload together with raw option

### Fixed
- Fixed UI actions that accessed filesystem in wrong thread
- Fixed environment proxy settings
- Fixed threading and UI freezes on some long running actions (network related)
- Fixed defaults file includes when generating documentation
- Fixed file including for documentation generator on Windows

## [4.4.0]
### Added
- Xml autocompletion
    - OIDs for existing objects in workspace
    - Simple autocomplete for source/target path elements
    - Connector configuration elements in resource
- Copy selected objects oid/name from browse results using mouse right click
- Clickable oid references in xml
- Reconcile/recompute action added
- Upload of object delta now works (executes modify)
- MidPoint xml file cleanup action added to remove operational (useless) elements
- Objects verification using schemas bundled with MidPoint studio (can't be changed for now)
- Upgrade task action which upgrades task objects to new "activity" format (midPoint 4.4)

### Changed
- Improved inlay properties caching
- Bulk action tasks generator
  - Wrap action into task option removed
  - Bulk tasks are now created as iterative-script tasks
  - Generated XML much cleaner

### Fixed
- Small UI fixes when using Darcula theme
- Progress during REST operations
- REST actions cancelling
- UI Freezes

## [4.3]
### Added
- Bulk refresh predefined objects
- Import/export encrypted properties
- Test connection only for resource objects
- Environment properties inlayed to xml for better readability
- Delete object directly from browse tool window
  

### Changed
- Using MidPoint 4.3 libs
- Xml diff simplified UI, multiple diff strategies available
- Updated documentation generator

## [4.2]
### Added
- Browse/Upload with raw options for both operations
- Properties can be used to replace parts of xml. Properties can also be stored encrypted in keepass2 file within project
- XML tags autocompletion, links to documentation
- Groovy syntax highlighting and groovy code completion
- Groovy code common variables injection
- Object diff with option to ignore some parts of xml (metadata, id attributes, etc.)
- Simple OID generator
