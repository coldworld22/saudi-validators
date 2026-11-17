package io.github.coldworld22.saudi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces personal data in free text with numbered placeholders such as {@code [NATIONAL_ID_1]},
 * and puts the real values back afterwards.
 *
 * <p>Typical use: mask a prompt before it goes to an external LLM, a log line before it is shipped,
 * or a support ticket before it is shared, then {@link Result#restore(String) restore} the reply.
 *
 * <p>Candidates are found with patterns and then confirmed with the real checksum (Luhn for IDs
 * and cards, mod-97 for IBANs), so ordinary numbers such as order IDs are left alone. Rules run
 * most specific first, so an IBAN's digits are never also matched as a phone number. Pattern
 * matching catches well-formed identifiers; treat it as a safety net, not a guarantee.
 *
 * <p>Instances are immutable and thread-safe.
 */
public final class PiiMasker {

    private record Rule(PiiType type, Pattern pattern, Predicate<String> accept) {}

    private static final List<Rule> ALL_RULES = List.of(
            new Rule(PiiType.IBAN,
                    Pattern.compile("\\bSA\\d{2}[0-9A-Z]{20}\\b"), SaudiIban::isValid),
            // Phones before cards: "00966551234567" is 14 digits and could pass Luhn by chance.
            new Rule(PiiType.PHONE,
                    Pattern.compile("(?<![\\d+])(?:\\+966|00966|966|0)5\\d{8}(?!\\d)"), SaudiMobile::isValid),
            new Rule(PiiType.CARD,
                    Pattern.compile("(?<!\\d)(?:\\d[ -]?){12,18}\\d(?!\\d)"), CardNumber::isValid),
            new Rule(PiiType.NATIONAL_ID,
                    Pattern.compile("(?<!\\d)[12]\\d{9}(?!\\d)"), NationalId::isValid),
            new Rule(PiiType.EMAIL,
                    Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"), v -> true));

    private final List<Rule> rules;

    private PiiMasker(Set<PiiType> types) {
        this.rules = ALL_RULES.stream().filter(r -> types.contains(r.type())).toList();
    }

    /**
     * A masker for every supported type.
     *
     * @return a masker for all {@link PiiType}s
     */
    public static PiiMasker all() {
        return new PiiMasker(EnumSet.allOf(PiiType.class));
    }

    /**
     * A masker for some types only.
     *
     * @param first the first type to mask
     * @param rest  further types to mask
     * @return a masker for the given types
     */
    public static PiiMasker of(PiiType first, PiiType... rest) {
        return new PiiMasker(EnumSet.of(first, rest));
    }

    /**
     * The masked texts plus the placeholder-to-value map needed to restore a reply.
     *
     * @param texts        the masked texts, in input order
     * @param replacements placeholder to original value, in order of first appearance
     */
    public record Result(List<String> texts, Map<String, String> replacements) {

        /** Copies both collections, so a result can be shared safely. */
        public Result {
            texts = List.copyOf(texts);
            replacements = Collections.unmodifiableMap(new LinkedHashMap<>(replacements));
        }

        /**
         * The number of distinct values masked.
         *
         * @return how many different values were replaced
         */
        public int count() {
            return replacements.size();
        }

        /**
         * The single masked text, for results of {@link PiiMasker#mask(String)}.
         *
         * @return the first masked text
         */
        public String text() {
            return texts.get(0);
        }

        /**
         * Puts the original values back wherever their placeholders appear.
         *
         * @param text text containing placeholders, for example a model's answer
         * @return the text with real values restored
         */
        public String restore(String text) {
            String restored = text;
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                restored = restored.replace(entry.getKey(), entry.getValue());
            }
            return restored;
        }
    }

    /**
     * Masks one text.
     *
     * @param text the text to mask
     * @return the masked text and the replacements
     */
    public Result mask(String text) {
        return mask(List.of(text));
    }

    /**
     * Masks several texts with one shared numbering, so the same value gets the same placeholder in
     * every message of a conversation.
     *
     * @param texts the texts to mask
     * @return the masked texts and the replacements
     */
    public Result mask(List<String> texts) {
        Objects.requireNonNull(texts, "texts");
        Map<String, String> valueToPlaceholder = new LinkedHashMap<>();
        Map<PiiType, Integer> counters = new LinkedHashMap<>();
        List<String> masked = new ArrayList<>(texts.size());

        for (String text : texts) {
            String current = Objects.requireNonNull(text, "text");
            for (Rule rule : rules) {
                Matcher matcher = rule.pattern().matcher(current);
                StringBuilder out = new StringBuilder();
                while (matcher.find()) {
                    String value = matcher.group();
                    String replacement = value;
                    if (rule.accept().test(value)) {
                        replacement = valueToPlaceholder.computeIfAbsent(value, v -> {
                            int n = counters.merge(rule.type(), 1, Integer::sum);
                            return "[" + rule.type() + "_" + n + "]";
                        });
                    }
                    matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
                }
                matcher.appendTail(out);
                current = out.toString();
            }
            masked.add(current);
        }

        Map<String, String> placeholderToValue = new LinkedHashMap<>();
        valueToPlaceholder.forEach((value, placeholder) -> placeholderToValue.put(placeholder, value));
        return new Result(masked, placeholderToValue);
    }
}
