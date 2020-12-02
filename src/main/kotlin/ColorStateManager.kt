class ColorStateManager {


    //TODO: This needs to be improved. Lack of static syntax makes me think that there is a better design pattern.
    companion object {
        var currentColors: MutableList<RGB> = mutableListOf()

        fun interpolateForLED(count: Int): List<RGB> {
            //TODO: Write an interpolation algorithm
            val output: MutableList<RGB> = mutableListOf()

            for (led in 1..count) { //Maybe do 0 to count-1?
                val first = currentColors.firstOrNull() ?: RGB(0, 0,0)
                output.add(first)
            }

            return output
        }
    }

}