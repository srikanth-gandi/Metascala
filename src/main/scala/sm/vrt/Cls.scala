package sm.vrt

import sm.imm
import sm.{vrt, VM}


object Type{
  def apply(t: imm.Type)(implicit vm: VM) = t match{
    case tpe: imm.Type.Cls => new Cls(tpe)
    case tpe => new Type(tpe)
  }
}
class Type(val tpe: imm.Type, initMembers: (String, vrt.Val)*)
          (implicit vm: VM)
          extends Obj(vm.Classes(imm.Type.Cls("java/lang/Class")), initMembers: _*){
  def getDeclaredConstructors() = new Array[vrt.Obj](0)
  def getDeclaredFields() = new Array[vrt.Obj](0)
  def getDeclaredMethods() = new Array[vrt.Obj](0)
  def getInterfaces() = new Array[vrt.Obj](0)
  override def toString = {
    s"vrt.Type(${tpe.unparse})"
  }
}
class Cls(override val tpe: imm.Type.Cls)
            (implicit vm: VM)
             extends Type(tpe, "name" -> tpe.name.replace('/', '.')){
  import vm._
  def name = tpe.unparse
  override def getDeclaredConstructors(): Array[vrt.Obj] = {
    tpe.clsData
      .methods
      .filter(_.name == "<init>")
      .map{m =>
      vrt.Obj("java/lang/reflect/Constructor",
        "clazz" -> tpe.obj,
        "slot" -> 0,
        "parameterTypes" -> new vrt.Arr.Obj(imm.Type.Cls("java/lang/reflect/Type"), m.desc.args.map(_.obj).toArray),
        "exceptionTypes" -> new Array[vrt.Cls](0),
        "modifiers" -> m.access
      )
    }.toArray
  }

  override def getDeclaredFields() = {
      tpe.clsData.fields.map {f =>

        vrt.Obj("java/lang/reflect/Field",
          "clazz" -> this,
          "slot" -> f.name.hashCode,
          "name" -> vm.InternedStrings(f.name),
          "modifiers" -> f.access,
          "type" -> f.desc.obj

        )
      }.toArray
  }

  override def getDeclaredMethods() = {

    tpe.clsData.methods.map {m =>
      vrt.Obj("java/lang/reflect/Method",
        "clazz" -> this,
        "slot" -> m.name.hashCode,
        "name" -> vm.InternedStrings(m.name),

        "modifiers" -> m.access,
        "returnType" -> m.desc.ret.obj,
        "parameterTypes" -> new vrt.Arr.Obj(imm.Type.Cls("java/lang/reflect/Type"), m.desc.args.map(_.obj).toArray),
        "exceptionTypes" -> new Array[vrt.Cls](0)

      )
    }.toArray
  }
  override def getInterfaces(): Array[vrt.Obj] = {
    tpe.clsData.interfaces.map(_.obj).toArray
  }
  override def toString = {
    s"vrt.Cls(${tpe.unparse}})"
  }
}
