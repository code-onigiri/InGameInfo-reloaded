package df11zomgraves.ingameinfo.value;

import java.util.Locale;

import df11zomgraves.ingameinfo.InGameInfoXML;

public abstract class ValueMath extends ValueComplex {
    @Override
    public boolean isValidSize() {
        return this.values.size() == 2;
    }

    public static class ValueAdd extends ValueMath {
        @Override
        public String getValue() {
            try {
                final long arg0 = getLongValue(0);
                final long arg1 = getLongValue(1);
                return String.valueOf(Math.addExact(arg0, arg1));
            } catch (final Exception e1) {
                try {
                    final double arg0 = getDoubleValue(0);
                    final double arg1 = getDoubleValue(1);
                    return String.valueOf(arg0 + arg1);
                } catch (final Exception e2) {
                    InGameInfoXML.logger.debug("Failed to evaluate math value!", e2);
                    return "0";
                }
            }
        }
    }

    public static class ValueSub extends ValueMath {
        @Override
        public String getValue() {
            try {
                final long arg0 = getLongValue(0);
                final long arg1 = getLongValue(1);
                return String.valueOf(Math.subtractExact(arg0, arg1));
            } catch (final Exception e1) {
                try {
                    final double arg0 = getDoubleValue(0);
                    final double arg1 = getDoubleValue(1);
                    return String.valueOf(arg0 - arg1);
                } catch (final Exception e2) {
                    InGameInfoXML.logger.debug("Failed to evaluate math value!", e2);
                    return "0";
                }
            }
        }
    }

    public static class ValueMul extends ValueMath {
        @Override
        public String getValue() {
            try {
                final long arg0 = getLongValue(0);
                final long arg1 = getLongValue(1);
                return String.valueOf(Math.multiplyExact(arg0, arg1));
            } catch (final Exception e1) {
                try {
                    final double arg0 = getDoubleValue(0);
                    final double arg1 = getDoubleValue(1);
                    return String.valueOf(arg0 * arg1);
                } catch (final Exception e2) {
                    InGameInfoXML.logger.debug("Failed to evaluate math value!", e2);
                    return "0";
                }
            }
        }
    }

    public static class ValueDiv extends ValueMath {
        @Override
        public String getValue() {
            try {
                final double arg0 = getDoubleValue(0);
                final double arg1 = getDoubleValue(1);
                return arg1 == 0.0 ? "0" : String.valueOf(arg0 / arg1);
            } catch (final Exception e) {
                InGameInfoXML.logger.debug("Failed to evaluate math value!", e);
                return "0";
            }
        }
    }

    public static class ValueRound extends ValueMath {
        @Override
        public String getValue() {
            try {
                final double arg0 = getDoubleValue(0);
                final int arg1 = getIntValue(1);
                if (arg1 > 0) {
                    return String.format(Locale.ENGLISH, "%." + arg1 + "f", arg0);
                }
                if (arg1 == 0) {
                    return String.valueOf(Math.round(arg0));
                }
                final long scale = (long) Math.pow(10, -arg1);
                return String.valueOf(Math.round(arg0 / scale) * scale);
            } catch (final Exception e2) {
                InGameInfoXML.logger.debug("Failed to evaluate math value!", e2);
                return "0";
            }
        }
    }

    public static class ValueMod extends ValueMath {
        @Override
        public String getValue() {
            try {
                final double arg0 = getDoubleValue(0);
                final double arg1 = getDoubleValue(1);
                return String.valueOf(Math.round((arg0 % arg1) * 1e6) / 1e6);
            } catch (final Exception e2) {
                InGameInfoXML.logger.debug("Failed to evaluate math value!", e2);
                return "0";
            }
        }
    }

    public static class ValueModi extends ValueMath {
        @Override
        public String getValue() {
            try {
                final int arg0 = getIntValue(0);
                final int arg1 = getIntValue(1);
                return String.valueOf(arg0 % arg1);
            } catch (final Exception e2) {
                InGameInfoXML.logger.debug("Failed to evaluate math value!", e2);
                return "0";
            }
        }
    }

    public static class ValuePercent extends ValueMath {
        @Override
        public String getValue() {
            try {
                final double arg0 = getDoubleValue(0);
                final double arg1 = getDoubleValue(1);
                return arg1 == 0.0 ? "0" : String.valueOf(arg0 / arg1 * 100);
            } catch (final Exception e) {
                InGameInfoXML.logger.debug("Failed to evaluate math value!", e);
                return "0";
            }
        }
    }

    public static void register() {
        ValueRegistry.INSTANCE.register(new ValueAdd().setName("add"));
        ValueRegistry.INSTANCE.register(new ValueSub().setName("sub"));
        ValueRegistry.INSTANCE.register(new ValueMul().setName("mul"));
        ValueRegistry.INSTANCE.register(new ValueDiv().setName("div"));
        ValueRegistry.INSTANCE.register(new ValueRound().setName("round"));
        ValueRegistry.INSTANCE.register(new ValueMod().setName("mod").setAliases("modulo"));
        ValueRegistry.INSTANCE.register(new ValueModi().setName("modi").setAliases("modint", "moduloi", "moduloint"));
        ValueRegistry.INSTANCE.register(new ValuePercent().setName("pct").setAliases("percent", "percentage"));
    }
}
