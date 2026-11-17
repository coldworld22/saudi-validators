# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses
[Semantic Versioning](https://semver.org/).

## [1.0.0] - 2026-11-17

### Added

- `NationalId`: National ID and Iqama validation with the Luhn check digit, citizen/resident type, masking.
- `SaudiIban`: mod-97 validation, bank code and name lookup, print form, masking.
- `SaudiMobile`: parses every common format into E.164, local form, masking.
- `CardNumber`: Luhn validation and PCI-style masking.
- `PiiMasker`: reversible masking of all of the above plus emails in free text, with checksum confirmation.
- Jakarta Bean Validation constraints `@ValidNationalId`, `@ValidSaudiIban`, `@ValidSaudiMobile`.
- Arabic-Indic digit support in every parser.
