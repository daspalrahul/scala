package scala.collection.immutable

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import scala.math._
import scala.util._

/* Tests various maps by making sure they all agree on the same answers. */
@RunWith(classOf[JUnit4])
class RangeConsistencyTest {
  def r2nr[T: Integral](
    r: Range, puff: T, stride: T, check: (T,T) => Boolean, bi: T => BigInt
  ): List[(BigInt,Try[Int])] = {
    val num = implicitly[Integral[T]]
    import num._
    val one = num.one
    
    if (!check(puff, fromInt(r.start))) return Nil
    val start = puff * fromInt(r.start)
    val sp1 = start + one
    val sn1 = start - one
      
    if (!check(puff, fromInt(r.end))) return Nil
    val end = puff * fromInt(r.end)
    val ep1 = end + one
    val en1 = end - one
    
    if (!check(stride, fromInt(r.step))) return Nil
    val step = stride * fromInt(r.step)
    
    def NR(s: T, e: T, i: T) = {
      val delta = (bi(e) - bi(s)).abs - (if (r.isInclusive) 0 else 1)
      val n = if (r.length == 0) BigInt(0) else delta / bi(i).abs + 1
      if (r.isInclusive) {
        (n, Try(NumericRange.inclusive(s,e,i).length))
      }
      else {
        (n, Try(NumericRange(s,e,i).length))
      }
    } 
    
    List(NR(start, end, step)) :::
    (if (sn1 < start) List(NR(sn1, end, step)) else Nil) :::
    (if (start < sp1) List(NR(sp1, end, step)) else Nil) :::
    (if (en1 < end) List(NR(start, en1, step)) else Nil) :::
    (if (end < ep1) List(NR(start, ep1, step)) else Nil)
  }
  
  // Motivated by SI-4370: Wrong result for Long.MinValue to Long.MaxValue by Int.MaxValue
  @Test
  def rangeChurnTest() {
    val rn = new Random(4370)
    for (i <- 0 to 10000) { control.Breaks.breakable {
      val start = rn.nextInt
      val end = rn.nextInt
      val step = rn.nextInt(4) match {
        case 0 => 1
        case 1 => -1
        case 2 => (rn.nextInt(11)+2)*(2*rn.nextInt(2)+1)
        case 3 => var x = rn.nextInt; while (x==0) x = rn.nextInt; x
      }
      val r = if (rn.nextBoolean) Range.inclusive(start, end, step) else Range(start, end, step)
      
      try { r.length }
      catch { case iae: IllegalArgumentException => control.Breaks.break }
      
      val lpuff = rn.nextInt(4) match {
        case 0 => 1L
        case 1 => rn.nextInt(11)+2L
        case 2 => 1L << rn.nextInt(60)
        case 3 => math.max(1L, math.abs(rn.nextLong))
      }
      val lstride = rn.nextInt(4) match {
        case 0 => lpuff
        case 1 => 1L
        case 2 => 1L << rn.nextInt(60)
        case 3 => math.max(1L, math.abs(rn.nextLong))
      }
      val lr = r2nr[Long](
        r, lpuff, lstride, 
        (a,b) => { val x = BigInt(a)*BigInt(b); x.isValidLong },
        x => BigInt(x)
      )
      
      lr.foreach{ case (n,t) => assert(
        t match {
          case Failure(_) => n > Int.MaxValue
          case Success(m) => n == m
        },
        (r.start, r.end, r.step, r.isInclusive, lpuff, lstride, n, t)
      )}
      
      val bipuff = rn.nextInt(3) match {
        case 0 => BigInt(1)
        case 1 => BigInt(rn.nextLong) + Long.MaxValue + 2
        case 2 => BigInt("1" + "0"*(rn.nextInt(100)+1))
      }
      val bistride = rn.nextInt(3) match {
        case 0 => bipuff
        case 1 => BigInt(1)
        case 2 => BigInt("1" + "0"*(rn.nextInt(100)+1))
      }
      val bir = r2nr[BigInt](r, bipuff, bistride, (a,b) => true, identity)
      
      bir.foreach{ case (n,t) => assert(
        t match {
          case Failure(_) => n > Int.MaxValue
          case Success(m) => n == m
        },
        (r.start, r.end, r.step, r.isInclusive, bipuff, bistride, n, t)
      )}              
    }}
  }
  
  @Test
  def testSI4370() { assert{
    Try((Long.MinValue to Long.MaxValue by Int.MaxValue).length) match {
      case Failure(iae: IllegalArgumentException) => true
      case _ => false
    }
  }}
}
