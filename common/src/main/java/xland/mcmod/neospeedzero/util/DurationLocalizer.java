package xland.mcmod.neospeedzero.util;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import xland.mcmod.enchlevellangpatch.api.EnchantmentLevelLangPatch;

import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
@SuppressWarnings("ClassCanBeRecord")
public final class DurationLocalizer {
    private static final String PREFIX = "message.neospeedzero.duration.";
    private static final String TEMPLATE_KEY = PREFIX + "template";
    private static final String MAX_UNITS_KEY = PREFIX + "max_units";
    private static final String SHOW_ZERO_KEY = PREFIX + "show_zero_values";
    private static final String SEPARATOR_KEY = PREFIX + "separator";
    private static final String FINAL_SEPARATOR_KEY = PREFIX + "final_separator";

    private final @NotNull Map<String, String> translations;

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void bootstrap() {
        if (isLangPatchAvailable()) {
            LOGGER.debug("Found LangPatch. Applying patch.");
            EnchantmentLevelLangPatch.registerPatch(
                    Predicate.isEqual(TimeUtil.PLACEHOLDER_KEY),
                    EnchantmentLevelLangPatch.withFallback((translations, _, fallback) -> {
                        if (!Boolean.parseBoolean(translations.get(PREFIX + "apply_localizer"))) {
                            // Use fallback format
                            return null;
                        }

                        try {
                            final Duration duration = Duration.ofMillis(Long.parseUnsignedLong(fallback));
                            return new DurationLocalizer(translations).localize(duration);
                        } catch (Exception e) {
                            LOGGER.warn("Duration localization failed, using fallback", e);
                            return null;
                        }
                    })
            );
        } else {
            LOGGER.debug("LangPatch is not available. Use fallback settings.");
        }
    }

    private static boolean isLangPatchAvailable() {
        return PlatformAPI.getInstance().isLangPatchAvailable();
    }
    
    private DurationLocalizer(@NotNull Map<String, String> translations) {
        Objects.requireNonNull(translations, "translations");
        this.translations = translations;
    }
    
    @NotNull String localize(@NotNull Duration duration) {
        Objects.requireNonNull(duration, "duration");
        // Fetch config
        int maxUnits = getIntConfig(MAX_UNITS_KEY, Integer.MAX_VALUE);
        if (maxUnits < 0) maxUnits = Integer.MAX_VALUE;
        boolean showZeroValues = getBooleanConfig(SHOW_ZERO_KEY, false);
        String separator = getStringConfig(SEPARATOR_KEY, ", ");
        String finalSeparator = getStringConfig(FINAL_SEPARATOR_KEY, " and ");
        
        // Split into time units
        List<TimeUnitValue> units = decomposeDuration(duration, showZeroValues);
        
        // Localize each unit
        List<String> localizedUnits = units.stream()
                .limit(maxUnits)    // Limit unit count
                .map(this::localizeUnit)
                .toList();

        return joinUnits(localizedUnits, separator, finalSeparator);
    }
    
    private List<TimeUnitValue> decomposeDuration(Duration duration, boolean showZeroValues) {
        List<TimeUnitValue> units = new ArrayList<>(5);
        
        long seconds = duration.getSeconds();
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        long millis = duration.getNano() / 1_000_000;
        
        if (showZeroValues || days > 0) {
            units.add(new TimeUnitValue("days", days));
        }
        if (showZeroValues || hours > 0) {
            units.add(new TimeUnitValue("hours", hours));
        }
        if (showZeroValues || minutes > 0) {
            units.add(new TimeUnitValue("minutes", minutes));
        }
        if (showZeroValues || secs > 0) {
            units.add(new TimeUnitValue("seconds", secs));
        }
        if (showZeroValues || millis > 0) {
            units.add(new TimeUnitValue("millis", millis));
        }
        
        return units;
    }
    
    private String localizeUnit(TimeUnitValue unit) {
        try {
            String template = getStringConfig(TEMPLATE_KEY, "{value} {unit}");
            
            // Decide single/plural
            String unitKey = PREFIX + unit.name();
            String unitNames = translations.getOrDefault(unitKey, unit.name());

            String singular, plural;
            {
                String[] nameVariants = unitNames.split(",", 2);
                if (nameVariants.length == 1) {
                    singular = plural = nameVariants[0];
                } else {
                    singular = nameVariants[0];
                    plural = nameVariants[1];
                }
            }

            String unitName = unit.value() == 1 ? singular : plural;
            
            // Apply template
            return template.replace("{value}", String.valueOf(unit.value()))
                          .replace("{unit}", unitName);
        } catch (Exception e) {
            // Fallback to simple display
            return unit.value() + " " + unit.name();
        }
    }
    
    private String joinUnits(List<String> units, String separator, String finalSeparator) {
        return switch (units.size()) {
            case 0 -> getStringConfig(PREFIX + "zero", "0");
            case 1 -> units.getFirst();
            case 2 -> units.get(0) + finalSeparator + units.get(1);
            default -> {
                String joined = String.join(separator, units.subList(0, units.size() - 1));
                yield joined + finalSeparator + units.getLast();
            }
        };
    }
    
    public int getIntConfig(String key, int defaultValue) {
        try {
            return Integer.parseInt(translations.getOrDefault(key, String.valueOf(defaultValue)));
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    public boolean getBooleanConfig(String key, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(translations.getOrDefault(key, String.valueOf(defaultValue)));
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    private String getStringConfig(String key, String defaultValue) {
        return translations.getOrDefault(key, defaultValue);
    }

    private record TimeUnitValue(String name, long value) {}
}