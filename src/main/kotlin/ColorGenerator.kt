class ColorGenerator() {

    //Testing
    private var r = 0;
    private var g = 1;
    private var b = 2;
    //Testing

    fun foo() {
//        r += 1
//        g += 1
//        b += 1
//        r = r % 256
//        g = g % 256
//        b = b % 256

        val colorData = RGB(1, 1, 1)
        val list = mutableListOf(colorData)




        ColorStateManager.currentColors = list
    }

}