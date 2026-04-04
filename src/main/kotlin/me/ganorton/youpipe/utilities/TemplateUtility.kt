package me.ganorton.youpipe.utilities

public object TemplateUtility {
	public fun formatNumber(num: Long): String {
		val suffices = " KMB"

		var i = 0
		var fnum: Double = num.toDouble()
		while (fnum > 1000 && i < suffices.length) {
			fnum /= 1000
			i++
		}
		val decimals = if (fnum < 10 && i > 0) "1" else "0"
		val result = "%.${decimals}f%s".format(fnum, suffices[i])
		return result
	}
}
