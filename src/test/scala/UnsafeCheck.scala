import sun.misc.Unsafe

object UnsafeCheck extends App {

  val unsafe = {
    val unsafeConstructor = classOf[Unsafe].getDeclaredConstructor()
    unsafeConstructor.setAccessible(true)
    unsafeConstructor.newInstance()
  }

  val arr = Array.ofDim[Int](100)
  arr(50) = 99
  println(unsafe.arrayBaseOffset(classOf[Array[Integer]]))
  println(unsafe.arrayIndexScale(classOf[Array[Integer]]))

  println(unsafe.getInt(arr, unsafe.arrayBaseOffset(classOf[Array[Integer]]) + (50 * unsafe.arrayIndexScale(classOf[Array[Integer]]))))

  unsafe.putInt(arr, 16 + (51 * 4L), 98)
  println(arr(51))
}
