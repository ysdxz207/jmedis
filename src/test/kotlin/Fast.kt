import java.util.*


fun main(args: Array<String>) {
    val arr = Random().ints(10, 0, 100).toArray()
    println("未排序数组：${arr.joinToString(",")}")
    sort(arr, 0, arr.size - 1)
    println("排序后数组：${arr.joinToString(",")}")
}

fun sort(arr: IntArray, leftIndex: Int, rightIndex: Int) {
    var tempLeftIndex = leftIndex
    var tempRightIndex = rightIndex
    if (leftIndex > rightIndex)
        return
    val primary = arr[leftIndex]
    while (tempLeftIndex != tempRightIndex) {
        // 从右往左第一个比基准值小的(最右边的比基准值小的)
        while (arr[tempRightIndex] <= primary && tempLeftIndex < tempRightIndex)
            tempRightIndex--
        // 从左往右第一个比基准值大的(最左边的比基准值大的)
        while (arr[tempLeftIndex] >= primary && tempLeftIndex < tempRightIndex)
            tempLeftIndex++
        // 利用异或位运算交换位置
        if (tempLeftIndex < tempRightIndex) {
            arr[tempLeftIndex] = arr[tempLeftIndex] xor arr[tempRightIndex]
            arr[tempRightIndex] = arr[tempLeftIndex] xor arr[tempRightIndex]
            arr[tempLeftIndex] = arr[tempLeftIndex] xor arr[tempRightIndex]
        }
    }


    arr[leftIndex] = arr[tempLeftIndex]
    arr[tempLeftIndex] = primary
    sort(arr, leftIndex, tempLeftIndex - 1)
    sort(arr, tempLeftIndex + 1, rightIndex)
}