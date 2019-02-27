fun main(args: Array<String>) {
    val arr = arrayOf(4, 1, 500, 3, 5, 2, 8, 4, 63, 22)

    var temp: Int

    for (n in arr.indices - 1) {
        for (i in arr.indices) {

            if (i + 1 >= arr.size) {
                break
            }

            val cuVal = arr[i]
            val inVal = arr[i + 1]

            if (cuVal > inVal) {
                temp = cuVal
                arr[i] = inVal
                arr[i + 1] = temp
            }

        }
    }

    println(arr.joinToString(","))
}