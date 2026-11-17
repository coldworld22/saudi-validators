# saudi-validators

[![CI](https://github.com/coldworld22/saudi-validators/actions/workflows/ci.yml/badge.svg)](https://github.com/coldworld22/saudi-validators/actions/workflows/ci.yml)
[![JitPack](https://jitpack.io/v/coldworld22/saudi-validators.svg)](https://jitpack.io/#coldworld22/saudi-validators)
![Java 17+](https://img.shields.io/badge/Java-17%2B-007396?logo=openjdk&logoColor=white)
![Dependencies: none](https://img.shields.io/badge/runtime%20dependencies-0-brightgreen)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Validate, normalise and mask Saudi identifiers in Java: **National ID / Iqama**, **IBAN**,
**mobile numbers** and **card numbers**, plus a reversible **PII masker** for free text such as
LLM prompts, logs and support tickets.

Zero runtime dependencies. Optional Jakarta Bean Validation annotations for Spring Boot,
Quarkus and Jakarta EE.

```java
NationalId.parse("١٠١٢٣٤٥٦٧٢")            // Optional[NationalId[101****672, CITIZEN]]
SaudiIban.parse("sa03 8000 0000 6080 1016 7519").get().bankName()   // Optional[Al Rajhi Bank]
SaudiMobile.parse("055 123 4567").get().e164()                      // "+966551234567"

record SignUp(@NotNull @ValidNationalId String nationalId,
              @ValidSaudiMobile String mobile) {}
```

## Why

Almost every Saudi backend I've worked on, from government e-services to ERP integrations,
re-implemented the same checks, usually as a regex in a controller:

- **A regex is not validation.** `^[12]\d{9}$` accepts two billion numbers; only one in ten
  of them has a valid check digit. The last digit is a Luhn check digit, so typos can be caught at the form
  instead of failing later at Nafath, Absher or a bank.
- **Users type Arabic digits.** `١٠١٢٣٤٥٦٧٢` is a valid ID. Most validators reject it.
- **Phone numbers arrive in six formats** and end up stored in all six, so lookups and
  WhatsApp/SMS delivery fail. Normalise to E.164 once, at the edge.
- **IDs and IBANs leak into logs and LLM prompts.** Value types here print masked by default,
  and `PiiMasker` replaces them in free text and puts them back afterwards.

## Install

Via [JitPack](https://jitpack.io/#coldworld22/saudi-validators):

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.coldworld22</groupId>
    <artifactId>saudi-validators</artifactId>
    <version>v1.0.0</version>
</dependency>
```

Gradle: `implementation("com.github.coldworld22:saudi-validators:v1.0.0")` with
`maven("https://jitpack.io")`. Each release is also published to
[GitHub Packages](https://github.com/coldworld22/saudi-validators/packages) and attached to its
[GitHub Release](https://github.com/coldworld22/saudi-validators/releases).

## Usage

### National ID and Iqama

```java
Optional<NationalId> id = NationalId.parse(input);     // empty if invalid
NationalId.isValid("1012345672");                      // true
NationalId.isValid("1012345678");                      // false: wrong check digit

NationalId nid = id.orElseThrow();
nid.type();      // CITIZEN (starts with 1) or RESIDENT (starts with 2, Iqama)
nid.masked();    // "101****672"
nid.toString();  // "NationalId[101****672, CITIZEN]": safe to log
```

### IBAN

```java
SaudiIban iban = SaudiIban.parse("SA03 8000 0000 6080 1016 7519").orElseThrow();
iban.value();          // "SA0380000000608010167519" (electronic form, for storage and APIs)
iban.formatted();      // "SA03 8000 0000 6080 1016 7519" (for screens and PDFs)
iban.bankCode();       // "80"
iban.bankName();       // Optional[Al Rajhi Bank]
iban.masked();         // "SA******************7519"
```

### Mobile numbers

```java
// All of these give +966551234567:
// 0551234567, 551234567, 966551234567, 00966551234567, +966 55 123 4567, (055) 123-4567, ٠٥٥١٢٣٤٥٦٧
SaudiMobile m = SaudiMobile.parse(input).orElseThrow();
m.e164();       // "+966551234567"  for storage, SMS and WhatsApp APIs
m.national();   // "0551234567"     for display
m.masked();     // "+96655*****67"
```

### Masking PII in free text

```java
PiiMasker masker = PiiMasker.all();   // or PiiMasker.of(PiiType.NATIONAL_ID, PiiType.IBAN)

PiiMasker.Result r = masker.mask(
        "My Iqama is 2123456788, refund to SA0380000000608010167519, call 0551234567");

r.text();      // "My Iqama is [NATIONAL_ID_1], refund to [IBAN_1], call [PHONE_1]"
String answer = llm.complete(r.text());                 // the provider never sees real values
r.restore(answer);                                      // real values back in the reply
```

Every candidate is confirmed with its checksum, so order numbers, dates and amounts are left
alone. Masking several messages in one call (`mask(List<String>)`) gives the same value the
same placeholder across a whole conversation.

### Bean Validation

```java
record OpenAccountRequest(
        @NotNull @ValidNationalId(allow = NationalId.Type.CITIZEN) String nationalId,
        @NotNull @ValidSaudiIban String iban,
        @ValidSaudiMobile String mobile) {}

@PostMapping("/accounts")
ResponseEntity<?> open(@Valid @RequestBody OpenAccountRequest request) { ... }
```

`null` counts as valid, following the Bean Validation convention; add `@NotNull` for required
fields.

## Design

| Decision | Why |
|---|---|
| Value types are `record`s that can't hold an invalid value | Parse once at the edge, then pass `NationalId` around instead of re-checking strings ([parse, don't validate](https://lexi-lambda.github.io/blog/2019/11/05/parse-don-t-validate/)) |
| `parse` returns `Optional`, constructors throw | Untrusted input is an expected failure; a bad value in code is a bug |
| `toString()` is masked | An ID logged by accident doesn't become a data-protection incident (PDPL) |
| No runtime dependencies | Drops into any project without version conflicts ([ADR 1](docs/adr/0001-zero-runtime-dependencies.md)) |
| Masker confirms regex matches with checksums | Fewer false positives in prompts and logs ([ADR 2](docs/adr/0002-checksum-confirmed-masking.md)) |

## Development

```bash
mvn verify      # tests, 90% line / 85% branch coverage gate, Javadoc
```

CI runs on Java 17 and 21. Releases are cut by pushing a `vX.Y.Z` tag that matches the version
in `pom.xml`; see [CHANGELOG.md](CHANGELOG.md).

## Limits

- Validation proves a number is well-formed, not that it was issued or belongs to the person.
  Use Nafath or Yakeen for identity verification.
- The bank-name table covers the largest banks; other valid codes return an empty name.
- `PiiMasker` finds identifiers written without internal spaces. It is a safety net, not a
  guarantee.

## License

MIT
