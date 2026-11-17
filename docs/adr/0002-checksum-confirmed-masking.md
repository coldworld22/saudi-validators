# 2. Masking confirms candidates with the real checksum

Date: 2026-11-17 · Status: accepted

## Context

`PiiMasker` started as a regex-only redactor in front of an LLM gateway. Regex alone over-masks:
any 10-digit number starting with 1 looked like a National ID, so order numbers and references
were replaced too, which confused the model and the people reading its answers.

## Decision

Each pattern only finds candidates. A candidate is masked only if the matching validator
accepts it: Luhn for National IDs, Iqama numbers and cards, mod-97 for IBANs, the mobile parser
for phone numbers.

Rules run most specific first (IBAN, phone, card, ID, email), so one value is never claimed
twice. Phones run before cards because `00966551234567` is 14 digits and passes Luhn about one
time in ten.

Placeholders are numbered per type and shared across all texts in one call, so the same value
gets the same placeholder in every message of a conversation, and `restore` can put real values
back into a reply.

## Consequences

- About 1 in 10 random 10-digit numbers starting with 1 or 2 still pass Luhn and get masked.
  That is the right side to err on for personal data.
- Numbers written with internal spaces (`+966 55 123 4567`) are not found in free text; the
  parsers accept them, the masker does not. This keeps the patterns from matching across
  unrelated numbers in a sentence.
