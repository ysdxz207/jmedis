import java.util.*

/**
 * 获取数组中位数
 * 奇数个，则为排序后中间那个数
 * 偶数个，则为中间两个数的平均值
 */
fun main(args: Array<String>) {
    val arr = Random().ints(9, 0, 100).toArray()
    println("数组：${arr.joinToString(",")}")
    arr.sort()
    println("排序后：${arr.joinToString(",")}")
    println("中位数：" + median(arr))
}

fun median(array: IntArray): Any {
    val heapSize = array.size / 2 + 1
    val heap = PriorityQueue<Int>(heapSize)
    for (i in 0 until heapSize) {
        heap.add(array[i])
    }
    for (i in heapSize until array.size) {
        if (heap.peek() < array[i]) {
            heap.poll()
            heap.add(array[i])
        }
    }
    return if (array.size % 2 == 1) {
        heap.peek()
    } else {
        (heap.poll() + heap.peek()).toDouble() / 2.0
    }
}