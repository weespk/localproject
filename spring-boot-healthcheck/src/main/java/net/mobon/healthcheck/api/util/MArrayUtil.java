package net.mobon.healthcheck.api.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class MArrayUtil {
	public static int[] toInts(String arrayString, String delimit) {
		if(StringUtils.isEmpty(arrayString))	return null;
		if(StringUtils.isEmpty(delimit)) 		return null;
		
		String[] strs = StringUtils.split(arrayString, delimit);
		int[] nos = new int[strs.length];
		for(int idx = 0; idx < strs.length; idx++) {
			nos[idx] = NumberUtils.toInt(strs[idx], 0);
		}
		return nos;
	}
}
