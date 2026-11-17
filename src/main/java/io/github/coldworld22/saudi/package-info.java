/**
 * Validate, normalise and mask Saudi identifiers.
 *
 * <ul>
 *   <li>{@link io.github.coldworld22.saudi.NationalId} - National ID and Iqama numbers
 *   <li>{@link io.github.coldworld22.saudi.SaudiIban} - IBANs, with bank lookup
 *   <li>{@link io.github.coldworld22.saudi.SaudiMobile} - mobile numbers, normalised to E.164
 *   <li>{@link io.github.coldworld22.saudi.CardNumber} - payment card numbers
 *   <li>{@link io.github.coldworld22.saudi.PiiMasker} - reversible masking of all of the above in free text
 * </ul>
 *
 * <p>Every parser accepts Arabic-Indic digits and common separators. Annotations for Jakarta Bean
 * Validation live in {@link io.github.coldworld22.saudi.validation}.
 */
package io.github.coldworld22.saudi;
