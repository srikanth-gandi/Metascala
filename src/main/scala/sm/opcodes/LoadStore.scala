package sm
package imm
package opcodes

import org.objectweb.asm
import sm.imm.{Type, OpCode}

import collection.mutable
import sm.vrt

object LoadStore {
  case object Nop extends OpCode{
    def insnName = "nop"
    def id = 0
    def op = _ => ()
  }

  class PushOpCode(val id: Byte, val insnName: String, value: vrt.StackVal) extends OpCode{
    def op = _.frame.stack.push(value)
  }

  case object AConstNull extends PushOpCode(1, "aconst_null", vrt.Null)
  case object IConstNull extends PushOpCode(2, "iconst_m1", -1)

  case object IConst0 extends PushOpCode(3, "iconst_0", 0)
  case object IConst1 extends PushOpCode(4, "iconst_1", 1)
  case object IConst2 extends PushOpCode(5, "iconst_2", 2)
  case object IConst3 extends PushOpCode(6, "iconst_3", 3)
  case object IConst4 extends PushOpCode(7, "iconst_4", 4)
  case object IConst5 extends PushOpCode(8, "iconst_5", 5)

  case object LConst0 extends PushOpCode(9, "lconst_0", 0L)
  case object LConst1 extends PushOpCode(10, "lconst_1", 1L)

  case object FConst0 extends PushOpCode(11, "fconst_0", 0f)
  case object FConst1 extends PushOpCode(12, "fconst_1", 1f)
  case object FConst2 extends PushOpCode(13, "fconst_2", 2f)

  case object DConst0 extends PushOpCode(14, "dconst_0", 0d)
  case object DConst1 extends PushOpCode(15, "dconst_1", 1d)

  class PushValOpCode(val id: Byte, val insnName: String, value: vrt.Int) extends OpCode{
    def op = _.frame.stack.push(value)
  }

  case class BiPush(value: Int) extends PushValOpCode(16, "bipush", value)
  case class SiPush(value: Int) extends PushValOpCode(17,"sipush", value)


  def anyValToStackVal(x: Any): vrt.StackVal = x match {
    case x: scala.Byte => vrt.Int(x).toStackVal
    case x: scala.Char => vrt.Int(x).toStackVal
    case x: scala.Short => vrt.Int(x).toStackVal
    case x: scala.Int => x
    case x: scala.Float => x
    case x: scala.Long => x
    case x: scala.Double => x
  }

  case class Ldc(const: Any) extends BaseOpCode(18, "ldc"){
    def op = implicit vt => {
      import vt.vm
      import vm._
      val newConst: vrt.StackVal = const match{
        case s: String =>
          val v: vrt.Obj = s
          vt.vm.InternedStrings(v)
        case t: asm.Type =>
          Type.Cls(t.getClassName).obj

        case x => anyValToStackVal(x)
      }

      vt.push(newConst)
    }
  }

  // Not used, because ASM converts these Ldc(const: Any)
  //===============================================================
  val LdcW = UnusedOpCode(19, "ldc_w")
  val Ldc2W = UnusedOpCode(20, "ldc2_w")
  //===============================================================

  abstract class PushLocalIndexed(val id: Byte, val insnName: String) extends OpCode{
    def op = (ctx => ctx.frame.stack.push(ctx.frame.locals(index)))
    def index: Int
  }

  case class ILoad(index: Int) extends PushLocalIndexed(21, "iLoad")
  case class LLoad(index: Int) extends PushLocalIndexed(22, "lLoad")
  case class FLoad(index: Int) extends PushLocalIndexed(23, "fLoad")
  case class DLoad(index: Int) extends PushLocalIndexed(24, "dLoad")
  case class ALoad(index: Int) extends PushLocalIndexed(25, "aLoad")



  // Not used, because ASM converts these to raw XLoad(index: Int)s
  //===============================================================
  val ILoad0 = UnusedOpCode(26, "iLoad_0")
  val ILoad1 = UnusedOpCode(27, "iLoad_1")
  val ILoad2 = UnusedOpCode(28, "iLoad_2")
  val ILoad3 = UnusedOpCode(29, "iLoad_3")

  val LLoad0 = UnusedOpCode(30, "lLoad_0")
  val LLoad1 = UnusedOpCode(31, "lLoad_1")
  val LLoad2 = UnusedOpCode(32, "lLoad_2")
  val LLoad3 = UnusedOpCode(33, "lLoad_3")

  val FLoad0 = UnusedOpCode(34, "fLoad_0")
  val FLoad1 = UnusedOpCode(35, "fLoad_1")
  val FLoad2 = UnusedOpCode(36, "fLoad_2")
  val FLoad3 = UnusedOpCode(37, "fLoad_3")

  val DLoad0 = UnusedOpCode(38, "dLoad_0")
  val DLoad1 = UnusedOpCode(39, "dLoad_1")
  val DLoad2 = UnusedOpCode(40, "dLoad_2")
  val DLoad3 = UnusedOpCode(41, "dLoad_3")

  val ALoad0 = UnusedOpCode(42, "aLoad_0")
  val ALoad1 = UnusedOpCode(43, "aLoad_1")
  val ALoad2 = UnusedOpCode(44, "aLoad_2")
  val ALoad3 = UnusedOpCode(45, "aLoad_3")
  //===============================================================


  class PushFromArray(val id: Byte, val insnName: String) extends OpCode{
    def op = implicit vt => (vt.pop, vt.pop) match {
      case (vrt.Int(index), arr: vrt.Arr)=>
        import vt._
        if (arr.backing.isDefinedAt(index)){
          vt.push(arr(index).toStackVal)
        }else{
          throwException{
            sm.vrt.Obj("java/lang/ArrayIndexOutOfBoundsException",
              "detailMessage" -> (index+"")
            )
          }
        }
    }
  }

  case object IALoad extends PushFromArray(46, "iaLoad")
  case object LALoad extends PushFromArray(47, "laLoad")
  case object FALoad extends PushFromArray(48, "faLoad")
  case object DALoad extends PushFromArray(49, "daLoad")
  case object AALoad extends PushFromArray(50, "aaLoad")
  case object BALoad extends PushFromArray(51, "baLoad")
  case object CALoad extends PushFromArray(52, "caLoad")
  case object SALoad extends PushFromArray(53, "saLoad")

  abstract class StoreLocal(val id: Byte, val insnName: String) extends OpCode{
    def varId: Int
    def op = vt => vt.frame.locals(varId) = vt.pop

  }
  case class IStore(varId: Int) extends StoreLocal(54, "istore")
  case class LStore(varId: Int) extends StoreLocal(55, "lstore")
  case class FStore(varId: Int) extends StoreLocal(56, "fstore")
  case class DStore(varId: Int) extends StoreLocal(57, "dstore")
  case class AStore(varId: Int) extends StoreLocal(58, "astore")

  // Not used, because ASM converts these to raw XStore(index: Int)s
  //===============================================================
  val IStore0 = UnusedOpCode(59, "istore_0")
  val IStore1 = UnusedOpCode(60, "istore_1")
  val IStore2 = UnusedOpCode(61, "istore_2")
  val IStore3 = UnusedOpCode(62, "istore_3")

  val LStore0 = UnusedOpCode(63, "lstore_0")
  val LStore1 = UnusedOpCode(64, "lstore_1")
  val LStore2 = UnusedOpCode(65, "lstore_2")
  val LStore3 = UnusedOpCode(66, "lstore_3")

  val FStore0 = UnusedOpCode(67, "fstore_0")
  val FStore1 = UnusedOpCode(68, "fstore_1")
  val FStore2 = UnusedOpCode(69, "fstore_2")
  val FStore3 = UnusedOpCode(70, "fstore_3")

  val DStore0 = UnusedOpCode(71, "dstore_0")
  val DStore1 = UnusedOpCode(72, "dstore_1")
  val DStore2 = UnusedOpCode(73, "dstore_2")
  val DStore3 = UnusedOpCode(74, "dstore_3")

  val AStore0 = UnusedOpCode(75, "astore_0")
  val AStore1 = UnusedOpCode(76, "astore_1")
  val AStore2 = UnusedOpCode(77, "astore_2")
  val AStore3 = UnusedOpCode(78, "astore_3")
  //===============================================================

  class StoreArray(val id: Byte, val insnName: String)(store: PartialFunction[(vrt.StackVal, Int, Array[_]), Unit]) extends OpCode{
    def op = vt => (vt.pop, vt.pop, vt.pop) match {
      case (value, vrt.Int(index), arr: vrt.Arr) =>  store(value, index, arr.backing)
    }
  }

  case object IAStore extends StoreArray(79, "iastore")({case (vrt.Int(value), i, backing: Array[Int]) => backing(i) = value})
  case object LAStore extends StoreArray(80, "lastore")({case (vrt.Long(value), i, backing: Array[Long]) => backing(i) = value})
  case object FAStore extends StoreArray(81, "fastore")({case (vrt.Float(value), i, backing: Array[Float]) => backing(i) = value})
  case object DAStore extends StoreArray(82, "dastore")({case (vrt.Double(value), i, backing: Array[Double]) => backing(i) = value})
  type VVal = vrt.Val
  case object AAStore extends StoreArray(83, "aastore")({case (value, i, backing: Array[VVal]) => backing(i) = value})
  case object BAStore extends StoreArray(84, "bastore")({
    case (vrt.Int(value), i, backing: Array[Byte]) => backing(i) = value.toByte
    case (vrt.Int(value), i, backing: Array[Boolean]) => backing(i) = value.toByte != 0
  })
  case object CAStore extends StoreArray(85, "castore")({case (vrt.Int(value), i, backing: Array[Char]) => backing(i) = value.toChar})
  case object SAStore extends StoreArray(86, "sastore")({case (vrt.Int(value), i, backing: Array[Short]) => backing(i) = value.toShort})
}
