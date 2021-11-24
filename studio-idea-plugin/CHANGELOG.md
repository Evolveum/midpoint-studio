# MidPoint Studio

## [Unreleased]
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

### Changed
- Improved inlay properties caching
- Bulk action tasks generator
  - Wrap action into task option removed
  - Bulk tasks are now created as iterative-script tasks
  - Generated XML much cleaner

### Fixed
- Small UI fixes when using Darcula theme

## 4.3
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

## 4.2
### Added
- Browse/Upload with raw options for both operations
- Properties can be used to replace parts of xml. Properties can also be stored encrypted in keepass2 file within project
- XML tags autocompletion, links to documentation
- Groovy syntax highlighting and groovy code completion
- Groovy code common variables injection
- Object diff with option to ignore some parts of xml (metadata, id attributes, etc.)
- Simple OID generator
