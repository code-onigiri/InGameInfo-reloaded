package df11zomgraves.ingameinfo.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import df11zomgraves.ingameinfo.handler.ConfigurationHandler;
import df11zomgraves.ingameinfo.reference.Names;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

public class StringConvertUtils {
	private static final DecimalFormat[] FLOAT_FORMATS = new DecimalFormat[7];

	static {
		for (int i = 0; i < FLOAT_FORMATS.length; i++) {
			DecimalFormat df = new DecimalFormat(i == 0 ? "0" : ("0." + "0".repeat(i)),
					DecimalFormatSymbols.getInstance(Locale.ENGLISH));
			df.setRoundingMode(RoundingMode.HALF_UP);
			FLOAT_FORMATS[i] = df;
		}
	}

	public static String numToRoman(int amplifier) {
		StringBuilder s = new StringBuilder();
		if (amplifier == 0)
			return "";
		amplifier++;
		if (amplifier >= 40)
			return String.format(" %d", amplifier);
		s.append(' ');
		int[] values = { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };
		String[] symbols = { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };
		int i = 0;
		while (amplifier > 0) {
			if (amplifier >= values[i]) {
				s.append(symbols[i]);
				amplifier -= values[i];
			} else {
				i++;
			}
		}
		return s.toString();
	}
	
	public static void sendSeedToChat(long seed) {
		if (seed != 0 && ConfigurationHandler.sendSeedToChat) {
			Component component = ComponentUtils.copyOnClickText(String.valueOf(seed));
			Minecraft mc = Minecraft.getInstance();
			mc.gui.getChat().addMessage(Component.translatable(Names.SHOW_SEED, component));
		}
	}
	
	public static String getFloatDisplayFormat(double value, int decimalPlace) {
		if (decimalPlace < 0)
			decimalPlace = 0;
		else if (decimalPlace > 6)
			decimalPlace = 6;
		// DecimalFormat is not thread-safe, but this is only called from the client main thread.
		return FLOAT_FORMATS[decimalPlace].format(value);
	}
}
