(function(){
'use strict';
const $linkingInfo = Object.freeze({
  "assumingES6": true,
  "productionMode": false,
  "linkerVersion": "1.0.1",
  "fileLevelThis": this
});
const $imul = Math.imul;
const $fround = Math.fround;
const $clz32 = Math.clz32;
let $L0;
function $propertyName(obj) {
  for (const prop in obj) {
    return prop
  }
}
class $Char {
  constructor(c) {
    this.c = c
  };
  toString() {
    return String.fromCharCode(this.c)
  };
}
function $throwClassCastException(instance, classFullName) {
  throw new $c_Lorg_scalajs_linker_runtime_UndefinedBehaviorError(new $c_jl_ClassCastException(((instance + " is not an instance of ") + classFullName)))
}
function $throwArrayCastException(instance, classArrayEncodedName, depth) {
  while ((--depth)) {
    classArrayEncodedName = ("[" + classArrayEncodedName)
  };
  $throwClassCastException(instance, classArrayEncodedName)
}
function $throwArrayIndexOutOfBoundsException(i) {
  throw new $c_Lorg_scalajs_linker_runtime_UndefinedBehaviorError(new $c_jl_ArrayIndexOutOfBoundsException(((i === null) ? null : ("" + i))))
}
function $noIsInstance(instance) {
  throw new TypeError("Cannot call isInstance() on a Class representing a JS trait/object")
}
function $makeNativeArrayWrapper(arrayClassData, nativeArray) {
  return new arrayClassData.constr(nativeArray)
}
function $newArrayObject(arrayClassData, lengths) {
  return $newArrayObjectInternal(arrayClassData, lengths, 0)
}
function $newArrayObjectInternal(arrayClassData, lengths, lengthIndex) {
  const result = new arrayClassData.constr(lengths[lengthIndex]);
  if ((lengthIndex < (lengths.length - 1))) {
    const subArrayClassData = arrayClassData.componentData;
    const subLengthIndex = (lengthIndex + 1);
    const underlying = result.u;
    for (let i = 0; (i < underlying.length); (i++)) {
      underlying[i] = $newArrayObjectInternal(subArrayClassData, lengths, subLengthIndex)
    }
  };
  return result
}
function $objectGetClass(instance) {
  switch ((typeof instance)) {
    case "string": {
      return $d_T.getClassOf()
    }
    case "number": {
      if ($isInt(instance)) {
        if ((((instance << 24) >> 24) === instance)) {
          return $d_jl_Byte.getClassOf()
        } else if ((((instance << 16) >> 16) === instance)) {
          return $d_jl_Short.getClassOf()
        } else {
          return $d_jl_Integer.getClassOf()
        }
      } else {
        return $d_jl_Float.getClassOf()
      }
    }
    case "boolean": {
      return $d_jl_Boolean.getClassOf()
    }
    case "undefined": {
      return $d_jl_Void.getClassOf()
    }
    default: {
      if ((instance === null)) {
        return instance.getClass__jl_Class()
      } else if ((instance instanceof $c_RTLong)) {
        return $d_jl_Long.getClassOf()
      } else if ((instance instanceof $Char)) {
        return $d_jl_Character.getClassOf()
      } else if ((!(!(instance && instance.$classData)))) {
        return instance.$classData.getClassOf()
      } else {
        return null
      }
    }
  }
}
function $objectClassName(instance) {
  switch ((typeof instance)) {
    case "string": {
      return "java.lang.String"
    }
    case "number": {
      if ($isInt(instance)) {
        if ((((instance << 24) >> 24) === instance)) {
          return "java.lang.Byte"
        } else if ((((instance << 16) >> 16) === instance)) {
          return "java.lang.Short"
        } else {
          return "java.lang.Integer"
        }
      } else {
        return "java.lang.Float"
      }
    }
    case "boolean": {
      return "java.lang.Boolean"
    }
    case "undefined": {
      return "java.lang.Void"
    }
    default: {
      if ((instance === null)) {
        return instance.getClass__jl_Class()
      } else if ((instance instanceof $c_RTLong)) {
        return "java.lang.Long"
      } else if ((instance instanceof $Char)) {
        return "java.lang.Character"
      } else if ((!(!(instance && instance.$classData)))) {
        return instance.$classData.name
      } else {
        return null.getName__T()
      }
    }
  }
}
function $dp_toString__T(instance) {
  return ((instance === (void 0)) ? "undefined" : instance.toString())
}
function $dp_getClass__jl_Class(instance) {
  return instance.getClass__jl_Class()
}
function $dp_clone__O(instance) {
  return instance.clone__O()
}
function $dp_notify__V(instance) {
  return instance.notify__V()
}
function $dp_notifyAll__V(instance) {
  return instance.notifyAll__V()
}
function $dp_finalize__V(instance) {
  return instance.finalize__V()
}
function $dp_equals__O__Z(instance, x0) {
  if (((!(!(instance && instance.$classData))) || (instance === null))) {
    return instance.equals__O__Z(x0)
  } else if (((typeof instance) === "number")) {
    return $f_jl_Double__equals__O__Z(instance, x0)
  } else if ((instance instanceof $Char)) {
    return $f_jl_Character__equals__O__Z(instance, x0)
  } else {
    return $c_O.prototype.equals__O__Z.call(instance, x0)
  }
}
function $dp_hashCode__I(instance) {
  switch ((typeof instance)) {
    case "string": {
      return $f_T__hashCode__I(instance)
    }
    case "number": {
      return $f_jl_Double__hashCode__I(instance)
    }
    case "boolean": {
      return $f_jl_Boolean__hashCode__I(instance)
    }
    case "undefined": {
      return $f_jl_Void__hashCode__I(instance)
    }
    default: {
      if (((!(!(instance && instance.$classData))) || (instance === null))) {
        return instance.hashCode__I()
      } else if ((instance instanceof $Char)) {
        return $f_jl_Character__hashCode__I(instance)
      } else {
        return $systemIdentityHashCode(instance)
      }
    }
  }
}
function $dp_compareTo__O__I(instance, x0) {
  return instance.compareTo__O__I(x0)
}
function $dp_length__I(instance) {
  return instance.length__I()
}
function $dp_charAt__I__C(instance, x0) {
  return instance.charAt__I__C(x0)
}
function $dp_subSequence__I__I__jl_CharSequence(instance, x0, x1) {
  if (((typeof instance) === "string")) {
    return $f_T__subSequence__I__I__jl_CharSequence(instance, x0, x1)
  } else {
    return instance.subSequence__I__I__jl_CharSequence(x0, x1)
  }
}
function $dp_byteValue__B(instance) {
  return instance.byteValue__B()
}
function $dp_shortValue__S(instance) {
  return instance.shortValue__S()
}
function $dp_intValue__I(instance) {
  return instance.intValue__I()
}
function $dp_longValue__J(instance) {
  return instance.longValue__J()
}
function $dp_floatValue__F(instance) {
  return instance.floatValue__F()
}
function $dp_doubleValue__D(instance) {
  return instance.doubleValue__D()
}
function $intDiv(x, y) {
  if ((y === 0)) {
    throw new $c_jl_ArithmeticException("/ by zero")
  } else {
    return ((x / y) | 0)
  }
}
function $intMod(x, y) {
  if ((y === 0)) {
    throw new $c_jl_ArithmeticException("/ by zero")
  } else {
    return ((x % y) | 0)
  }
}
function $doubleToInt(x) {
  return ((x > 2147483647) ? 2147483647 : ((x < (-2147483648)) ? (-2147483648) : (x | 0)))
}
function $newJSObjectWithVarargs(ctor, args) {
  const instance = Object.create(ctor.prototype);
  const result = ctor.apply(instance, args);
  switch ((typeof result)) {
    case "string":
    case "number":
    case "boolean":
    case "undefined":
    case "symbol": {
      return instance
    }
    default: {
      return ((result === null) ? instance : result)
    }
  }
}
function $resolveSuperRef(superClass, propName) {
  const getPrototypeOf = Object.getPrototyeOf;
  const getOwnPropertyDescriptor = Object.getOwnPropertyDescriptor;
  let superProto = superClass.prototype;
  while ((superProto !== null)) {
    const desc = getOwnPropertyDescriptor(superProto, propName);
    if ((desc !== (void 0))) {
      return desc
    };
    superProto = getPrototypeOf(superProto)
  }
}
function $superGet(superClass, self, propName) {
  const desc = $resolveSuperRef(superClass, propName);
  if ((desc !== (void 0))) {
    const getter = desc.get;
    return ((getter !== (void 0)) ? getter.call(self) : getter.value)
  }
}
function $superSet(superClass, self, propName, value) {
  const desc = $resolveSuperRef(superClass, propName);
  if ((desc !== (void 0))) {
    const setter = desc.set;
    if ((setter !== (void 0))) {
      setter.call(self, value);
      return (void 0)
    }
  };
  throw new TypeError((("super has no setter '" + propName) + "'."))
}
function $systemArraycopy(src, srcPos, dest, destPos, length) {
  const srcu = src.u;
  const destu = dest.u;
  if ((((((srcPos < 0) || (destPos < 0)) || (length < 0)) || (srcPos > ((srcu.length - length) | 0))) || (destPos > ((destu.length - length) | 0)))) {
    $throwArrayIndexOutOfBoundsException(null)
  };
  if ((((srcu !== destu) || (destPos < srcPos)) || (((srcPos + length) | 0) < destPos))) {
    for (let i = 0; (i < length); i = ((i + 1) | 0)) {
      destu[((destPos + i) | 0)] = srcu[((srcPos + i) | 0)]
    }
  } else {
    for (let i = ((length - 1) | 0); (i >= 0); i = ((i - 1) | 0)) {
      destu[((destPos + i) | 0)] = srcu[((srcPos + i) | 0)]
    }
  }
}
let $lastIDHash = 0;
const $idHashCodeMap = new WeakMap();
function $systemIdentityHashCode(obj) {
  switch ((typeof obj)) {
    case "string":
    case "number":
    case "bigint":
    case "boolean":
    case "undefined": {
      return $dp_hashCode__I(obj)
    }
    default: {
      if ((obj === null)) {
        return 0
      } else {
        let hash = $idHashCodeMap.get(obj);
        if ((hash === (void 0))) {
          hash = (($lastIDHash + 1) | 0);
          $lastIDHash = hash;
          $idHashCodeMap.set(obj, hash)
        };
        return hash
      }
    }
  }
}
function $isByte(v) {
  return ((((typeof v) === "number") && (((v << 24) >> 24) === v)) && ((1 / v) !== (1 / (-0))))
}
function $isShort(v) {
  return ((((typeof v) === "number") && (((v << 16) >> 16) === v)) && ((1 / v) !== (1 / (-0))))
}
function $isInt(v) {
  return ((((typeof v) === "number") && ((v | 0) === v)) && ((1 / v) !== (1 / (-0))))
}
function $bC(c) {
  return new $Char(c)
}
const $bC0 = $bC(0);
function $uV(v) {
  return (((v === (void 0)) || (v === null)) ? (void 0) : $throwClassCastException(v, "java.lang.Void"))
}
function $uZ(v) {
  return ((((typeof v) === "boolean") || (v === null)) ? (!(!v)) : $throwClassCastException(v, "java.lang.Boolean"))
}
function $uC(v) {
  return (((v instanceof $Char) || (v === null)) ? ((v === null) ? 0 : v.c) : $throwClassCastException(v, "java.lang.Character"))
}
function $uB(v) {
  return (($isByte(v) || (v === null)) ? (v | 0) : $throwClassCastException(v, "java.lang.Byte"))
}
function $uS(v) {
  return (($isShort(v) || (v === null)) ? (v | 0) : $throwClassCastException(v, "java.lang.Short"))
}
function $uI(v) {
  return (($isInt(v) || (v === null)) ? (v | 0) : $throwClassCastException(v, "java.lang.Integer"))
}
function $uJ(v) {
  return (((v instanceof $c_RTLong) || (v === null)) ? ((v === null) ? $L0 : v) : $throwClassCastException(v, "java.lang.Long"))
}
function $uF(v) {
  return ((((typeof v) === "number") || (v === null)) ? (+v) : $throwClassCastException(v, "java.lang.Float"))
}
function $uD(v) {
  return ((((typeof v) === "number") || (v === null)) ? (+v) : $throwClassCastException(v, "java.lang.Double"))
}
function $uT(v) {
  return ((((typeof v) === "string") || (v === null)) ? ((v === null) ? "" : v) : $throwClassCastException(v, "java.lang.String"))
}
function $byteArray2TypedArray(value) {
  return new Int8Array(value.u)
}
function $typedArray2ByteArray(value) {
  return new ($d_B.getArrayOf().constr)(new Int8Array(value))
}
function $shortArray2TypedArray(value) {
  return new Int16Array(value.u)
}
function $typedArray2ShortArray(value) {
  return new ($d_S.getArrayOf().constr)(new Int16Array(value))
}
function $charArray2TypedArray(value) {
  return new Uint16Array(value.u)
}
function $typedArray2CharArray(value) {
  return new ($d_C.getArrayOf().constr)(new Uint16Array(value))
}
function $intArray2TypedArray(value) {
  return new Int32Array(value.u)
}
function $typedArray2IntArray(value) {
  return new ($d_I.getArrayOf().constr)(new Int32Array(value))
}
function $floatArray2TypedArray(value) {
  return new Float32Array(value.u)
}
function $typedArray2FloatArray(value) {
  return new ($d_F.getArrayOf().constr)(new Float32Array(value))
}
function $doubleArray2TypedArray(value) {
  return new Float64Array(value.u)
}
function $typedArray2DoubleArray(value) {
  return new ($d_D.getArrayOf().constr)(new Float64Array(value))
}
class $TypeData {
  constructor() {
    this.constr = (void 0);
    this.ancestors = null;
    this.componentData = null;
    this.arrayBase = null;
    this.arrayDepth = 0;
    this.zero = null;
    this.arrayEncodedName = "";
    this._classOf = (void 0);
    this._arrayOf = (void 0);
    this.isArrayOf = (void 0);
    this.name = "";
    this.isPrimitive = false;
    this.isInterface = false;
    this.isArrayClass = false;
    this.isJSClass = false;
    this.isInstance = (void 0)
  };
  initPrim(zero, arrayEncodedName, displayName, isArrayOf) {
    this.ancestors = {};
    this.zero = zero;
    this.arrayEncodedName = arrayEncodedName;
    this.isArrayOf = isArrayOf;
    this.name = displayName;
    this.isPrimitive = true;
    this.isInstance = (function(obj) {
      return false
    });
    return this
  };
  initClass(internalNameObj, isInterface, fullName, ancestors, isJSType, parentData, isInstance, isArrayOf) {
    const internalName = $propertyName(internalNameObj);
    this.ancestors = ancestors;
    this.arrayEncodedName = (("L" + fullName) + ";");
    this.isArrayOf = (isArrayOf || (function(obj, depth) {
      return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors[internalName])))
    }));
    this.isJSType = (!(!isJSType));
    this.name = fullName;
    this.isInterface = isInterface;
    this.isInstance = (isInstance || (function(obj) {
      return (!(!((obj && obj.$classData) && obj.$classData.ancestors[internalName])))
    }));
    return this
  };
  initArray(componentData) {
    const componentZero = ((componentData.zero === "longZero") ? $L0 : componentData.zero);
    class ArrayClass extends $c_O {
      constructor(arg) {
        super();
        if (((typeof arg) === "number")) {
          this.u = new Array(arg);
          for (let i = 0; (i < arg); (i++)) {
            this.u[i] = componentZero
          }
        } else {
          this.u = arg
        }
      };
      get(i) {
        if (((i < 0) || (i >= this.u.length))) {
          $throwArrayIndexOutOfBoundsException(i)
        };
        return this.u[i]
      };
      set(i, v) {
        if (((i < 0) || (i >= this.u.length))) {
          $throwArrayIndexOutOfBoundsException(i)
        };
        this.u[i] = v
      };
      clone__O() {
        return new ArrayClass(((this.u instanceof Array) ? this.u.slice(0) : new this.u.constructor(this.u)))
      };
    }
    ArrayClass.prototype.$classData = this;
    const encodedName = ("[" + componentData.arrayEncodedName);
    const componentBase = (componentData.arrayBase || componentData);
    const arrayDepth = (componentData.arrayDepth + 1);
    this.constr = ArrayClass;
    this.ancestors = {
      O: 1,
      jl_Cloneable: 1,
      Ljava_io_Serializable: 1
    };
    this.componentData = componentData;
    this.arrayBase = componentBase;
    this.arrayDepth = arrayDepth;
    this.arrayEncodedName = encodedName;
    this.name = encodedName;
    this.isArrayClass = true;
    this.isInstance = (function(obj) {
      return componentBase.isArrayOf(obj, arrayDepth)
    });
    return this
  };
  getArrayOf() {
    if ((!this._arrayOf)) {
      this._arrayOf = new $TypeData().initArray(this)
    };
    return this._arrayOf
  };
  getClassOf() {
    if ((!this._classOf)) {
      this._classOf = new $c_jl_Class(this)
    };
    return this._classOf
  };
  "isAssignableFrom"(that) {
    if ((this.isPrimitive || that.isPrimitive)) {
      return (this === that)
    } else {
      let thatFakeInstance;
      if ((that === $d_T)) {
        thatFakeInstance = ""
      } else if ((that === $d_jl_Boolean)) {
        thatFakeInstance = false
      } else if ((((((that === $d_jl_Byte) || (that === $d_jl_Short)) || (that === $d_jl_Integer)) || (that === $d_jl_Float)) || (that === $d_jl_Double))) {
        thatFakeInstance = 0
      } else if ((that === $d_jl_Long)) {
        thatFakeInstance = $L0
      } else if ((that === $d_jl_Character)) {
        thatFakeInstance = $bC0
      } else if ((that === $d_jl_Void)) {
        thatFakeInstance = (void 0)
      } else {
        thatFakeInstance = {
          $classData: that
        }
      };
      return this.isInstance(thatFakeInstance)
    }
  };
  "checkCast"(obj) {
    if ((((obj !== null) && (!this.isJSType)) && (!this.isInstance(obj)))) {
      $throwClassCastException(obj, this.name)
    }
  };
  "getSuperclass"() {
    return (this.parentData ? this.parentData.getClassOf() : null)
  };
  "getComponentType"() {
    return (this.componentData ? this.componentData.getClassOf() : null)
  };
  "newArrayOfThisClass"(lengths) {
    let arrayClassData = this;
    for (let i = 0; (i < lengths.length); (i++)) {
      arrayClassData = arrayClassData.getArrayOf()
    };
    return $newArrayObject(arrayClassData, lengths)
  };
}
function $isArrayOf_V(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && (obj.$classData.arrayBase === $d_V))))
}
function $isArrayOf_Z(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && (obj.$classData.arrayBase === $d_Z))))
}
function $isArrayOf_C(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && (obj.$classData.arrayBase === $d_C))))
}
function $isArrayOf_B(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && (obj.$classData.arrayBase === $d_B))))
}
function $isArrayOf_S(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && (obj.$classData.arrayBase === $d_S))))
}
function $isArrayOf_I(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && (obj.$classData.arrayBase === $d_I))))
}
function $isArrayOf_J(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && (obj.$classData.arrayBase === $d_J))))
}
function $isArrayOf_F(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && (obj.$classData.arrayBase === $d_F))))
}
function $isArrayOf_D(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && (obj.$classData.arrayBase === $d_D))))
}
function $asArrayOf_V(obj, depth) {
  if (($isArrayOf_V(obj, depth) || (obj === null))) {
    return obj
  } else {
    $throwArrayCastException(obj, "V", depth)
  }
}
function $asArrayOf_Z(obj, depth) {
  if (($isArrayOf_Z(obj, depth) || (obj === null))) {
    return obj
  } else {
    $throwArrayCastException(obj, "Z", depth)
  }
}
function $asArrayOf_C(obj, depth) {
  if (($isArrayOf_C(obj, depth) || (obj === null))) {
    return obj
  } else {
    $throwArrayCastException(obj, "C", depth)
  }
}
function $asArrayOf_B(obj, depth) {
  if (($isArrayOf_B(obj, depth) || (obj === null))) {
    return obj
  } else {
    $throwArrayCastException(obj, "B", depth)
  }
}
function $asArrayOf_S(obj, depth) {
  if (($isArrayOf_S(obj, depth) || (obj === null))) {
    return obj
  } else {
    $throwArrayCastException(obj, "S", depth)
  }
}
function $asArrayOf_I(obj, depth) {
  if (($isArrayOf_I(obj, depth) || (obj === null))) {
    return obj
  } else {
    $throwArrayCastException(obj, "I", depth)
  }
}
function $asArrayOf_J(obj, depth) {
  if (($isArrayOf_J(obj, depth) || (obj === null))) {
    return obj
  } else {
    $throwArrayCastException(obj, "J", depth)
  }
}
function $asArrayOf_F(obj, depth) {
  if (($isArrayOf_F(obj, depth) || (obj === null))) {
    return obj
  } else {
    $throwArrayCastException(obj, "F", depth)
  }
}
function $asArrayOf_D(obj, depth) {
  if (($isArrayOf_D(obj, depth) || (obj === null))) {
    return obj
  } else {
    $throwArrayCastException(obj, "D", depth)
  }
}
const $d_V = new $TypeData().initPrim((void 0), "V", "void", $isArrayOf_V);
const $d_Z = new $TypeData().initPrim(false, "Z", "boolean", $isArrayOf_Z);
const $d_C = new $TypeData().initPrim(0, "C", "char", $isArrayOf_C);
const $d_B = new $TypeData().initPrim(0, "B", "byte", $isArrayOf_B);
const $d_S = new $TypeData().initPrim(0, "S", "short", $isArrayOf_S);
const $d_I = new $TypeData().initPrim(0, "I", "int", $isArrayOf_I);
const $d_J = new $TypeData().initPrim("longZero", "J", "long", $isArrayOf_J);
const $d_F = new $TypeData().initPrim(0.0, "F", "float", $isArrayOf_F);
const $d_D = new $TypeData().initPrim(0.0, "D", "double", $isArrayOf_D);
class $c_O {
  hashCode__I() {
    return $systemIdentityHashCode(this)
  };
  equals__O__Z(that) {
    return (this === that)
  };
  toString__T() {
    const $$x1 = $objectClassName(this);
    const i = this.hashCode__I();
    return (($$x1 + "@") + $as_T($uD((i >>> 0)).toString(16)))
  };
  "toString"() {
    return this.toString__T()
  };
}
function $is_O(obj) {
  return (obj !== null)
}
function $as_O(obj) {
  return obj
}
function $isArrayOf_O(obj, depth) {
  const data = (obj && obj.$classData);
  if ((!data)) {
    return false
  } else {
    const arrayDepth = (data.arrayDepth || 0);
    return ((!(arrayDepth < depth)) && ((arrayDepth > depth) || (!data.arrayBase.isPrimitive)))
  }
}
function $asArrayOf_O(obj, depth) {
  return (($isArrayOf_O(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Ljava.lang.Object;", depth))
}
const $d_O = new $TypeData().initClass({
  O: 0
}, false, "java.lang.Object", {
  O: 1
}, (void 0), (void 0), $is_O, $isArrayOf_O);
$c_O.prototype.$classData = $d_O;
const $s_Lcom_regblanc_scalavator_html5_Main__main__AT__V = (function(args) {
  const this$1 = $m_Lcom_regblanc_scalavator_html5_Main$();
  $f_Lsgl_html5_Html5App__main__AT__V(this$1, args)
});
class $c_jl_Class extends $c_O {
  constructor(data0) {
    super();
    this.jl_Class__f_data = null;
    this.jl_Class__f_data = data0
  };
  toString__T() {
    return ((this.isInterface__Z() ? "interface " : (this.isPrimitive__Z() ? "" : "class ")) + this.getName__T())
  };
  isAssignableFrom__jl_Class__Z(that) {
    return $uZ(this.jl_Class__f_data.isAssignableFrom(that.jl_Class__f_data))
  };
  isInterface__Z() {
    return $uZ(this.jl_Class__f_data.isInterface)
  };
  isArray__Z() {
    return $uZ(this.jl_Class__f_data.isArrayClass)
  };
  isPrimitive__Z() {
    return $uZ(this.jl_Class__f_data.isPrimitive)
  };
  getName__T() {
    return $as_T(this.jl_Class__f_data.name)
  };
}
const $d_jl_Class = new $TypeData().initClass({
  jl_Class: 0
}, false, "java.lang.Class", {
  jl_Class: 1,
  O: 1
});
$c_jl_Class.prototype.$classData = $d_jl_Class;
class $c_jl_FloatingPointBits$ extends $c_O {
  constructor() {
    super();
    this.jl_FloatingPointBits$__f_java$lang$FloatingPointBits$$_areTypedArraysSupported = false;
    this.jl_FloatingPointBits$__f_arrayBuffer = null;
    this.jl_FloatingPointBits$__f_int32Array = null;
    this.jl_FloatingPointBits$__f_float32Array = null;
    this.jl_FloatingPointBits$__f_float64Array = null;
    this.jl_FloatingPointBits$__f_areTypedArraysBigEndian = false;
    this.jl_FloatingPointBits$__f_highOffset = 0;
    this.jl_FloatingPointBits$__f_lowOffset = 0;
    $n_jl_FloatingPointBits$ = this;
    this.jl_FloatingPointBits$__f_java$lang$FloatingPointBits$$_areTypedArraysSupported = true;
    this.jl_FloatingPointBits$__f_arrayBuffer = new ArrayBuffer(8);
    this.jl_FloatingPointBits$__f_int32Array = new Int32Array(this.jl_FloatingPointBits$__f_arrayBuffer, 0, 2);
    this.jl_FloatingPointBits$__f_float32Array = new Float32Array(this.jl_FloatingPointBits$__f_arrayBuffer, 0, 2);
    this.jl_FloatingPointBits$__f_float64Array = new Float64Array(this.jl_FloatingPointBits$__f_arrayBuffer, 0, 1);
    this.jl_FloatingPointBits$__f_int32Array[0] = 16909060;
    this.jl_FloatingPointBits$__f_areTypedArraysBigEndian = ($uB(new Int8Array(this.jl_FloatingPointBits$__f_arrayBuffer, 0, 8)[0]) === 1);
    this.jl_FloatingPointBits$__f_highOffset = (this.jl_FloatingPointBits$__f_areTypedArraysBigEndian ? 0 : 1);
    this.jl_FloatingPointBits$__f_lowOffset = (this.jl_FloatingPointBits$__f_areTypedArraysBigEndian ? 1 : 0)
  };
  numberHashCode__D__I(value) {
    const iv = $uI((value | 0));
    if (((iv === value) && ((1.0 / value) !== (-Infinity)))) {
      return iv
    } else {
      const t = this.doubleToLongBits__D__J(value);
      const lo = t.RTLong__f_lo;
      const hi = t.RTLong__f_hi;
      return (lo ^ hi)
    }
  };
  doubleToLongBits__D__J(value) {
    this.jl_FloatingPointBits$__f_float64Array[0] = value;
    const value$1 = $uI(this.jl_FloatingPointBits$__f_int32Array[this.jl_FloatingPointBits$__f_highOffset]);
    const value$2 = $uI(this.jl_FloatingPointBits$__f_int32Array[this.jl_FloatingPointBits$__f_lowOffset]);
    return new $c_RTLong(value$2, value$1)
  };
}
const $d_jl_FloatingPointBits$ = new $TypeData().initClass({
  jl_FloatingPointBits$: 0
}, false, "java.lang.FloatingPointBits$", {
  jl_FloatingPointBits$: 1,
  O: 1
});
$c_jl_FloatingPointBits$.prototype.$classData = $d_jl_FloatingPointBits$;
let $n_jl_FloatingPointBits$ = (void 0);
function $m_jl_FloatingPointBits$() {
  if ((!$n_jl_FloatingPointBits$)) {
    $n_jl_FloatingPointBits$ = new $c_jl_FloatingPointBits$()
  };
  return $n_jl_FloatingPointBits$
}
const $f_jl_Void__equals__O__Z = (function($thiz, that) {
  return ($thiz === that)
});
const $f_jl_Void__hashCode__I = (function($thiz) {
  return 0
});
const $f_jl_Void__toString__T = (function($thiz) {
  return "undefined"
});
function $as_jl_Void(obj) {
  return (((obj === (void 0)) || (obj === null)) ? obj : $throwClassCastException(obj, "java.lang.Void"))
}
function $isArrayOf_jl_Void(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.jl_Void)))
}
function $asArrayOf_jl_Void(obj, depth) {
  return (($isArrayOf_jl_Void(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Ljava.lang.Void;", depth))
}
const $d_jl_Void = new $TypeData().initClass({
  jl_Void: 0
}, false, "java.lang.Void", {
  jl_Void: 1,
  O: 1
}, (void 0), (void 0), ((x) => (x === (void 0))));
class $c_ju_Arrays$ extends $c_O {
  binarySearch__AI__I__I(a, key) {
    let startIndex = 0;
    let endIndex = a.u.length;
    while (true) {
      if ((startIndex === endIndex)) {
        return (((-1) - startIndex) | 0)
      } else {
        const mid = ((((startIndex + endIndex) | 0) >>> 1) | 0);
        const elem = a.get(mid);
        if ((key < elem)) {
          endIndex = mid
        } else if ($m_sr_BoxesRunTime$().equals__O__O__Z(key, elem)) {
          return mid
        } else {
          startIndex = ((1 + mid) | 0)
        }
      }
    }
  };
}
const $d_ju_Arrays$ = new $TypeData().initClass({
  ju_Arrays$: 0
}, false, "java.util.Arrays$", {
  ju_Arrays$: 1,
  O: 1
});
$c_ju_Arrays$.prototype.$classData = $d_ju_Arrays$;
let $n_ju_Arrays$ = (void 0);
function $m_ju_Arrays$() {
  if ((!$n_ju_Arrays$)) {
    $n_ju_Arrays$ = new $c_ju_Arrays$()
  };
  return $n_ju_Arrays$
}
const $p_Lorg_scalajs_dom_package$__window$lzycompute__Lorg_scalajs_dom_raw_Window = (function($thiz) {
  if (((33554432 & $thiz.Lorg_scalajs_dom_package$__f_bitmap$0) === 0)) {
    $thiz.Lorg_scalajs_dom_package$__f_window = window;
    $thiz.Lorg_scalajs_dom_package$__f_bitmap$0 = (33554432 | $thiz.Lorg_scalajs_dom_package$__f_bitmap$0)
  };
  return $thiz.Lorg_scalajs_dom_package$__f_window
});
const $p_Lorg_scalajs_dom_package$__document$lzycompute__Lorg_scalajs_dom_raw_HTMLDocument = (function($thiz) {
  if (((67108864 & $thiz.Lorg_scalajs_dom_package$__f_bitmap$0) === 0)) {
    $thiz.Lorg_scalajs_dom_package$__f_document = $thiz.window__Lorg_scalajs_dom_raw_Window().document;
    $thiz.Lorg_scalajs_dom_package$__f_bitmap$0 = (67108864 | $thiz.Lorg_scalajs_dom_package$__f_bitmap$0)
  };
  return $thiz.Lorg_scalajs_dom_package$__f_document
});
const $p_Lorg_scalajs_dom_package$__console$lzycompute__Lorg_scalajs_dom_raw_Console = (function($thiz) {
  if (((134217728 & $thiz.Lorg_scalajs_dom_package$__f_bitmap$0) === 0)) {
    $thiz.Lorg_scalajs_dom_package$__f_console = $thiz.window__Lorg_scalajs_dom_raw_Window().console;
    $thiz.Lorg_scalajs_dom_package$__f_bitmap$0 = (134217728 | $thiz.Lorg_scalajs_dom_package$__f_bitmap$0)
  };
  return $thiz.Lorg_scalajs_dom_package$__f_console
});
class $c_Lorg_scalajs_dom_package$ extends $c_O {
  constructor() {
    super();
    this.Lorg_scalajs_dom_package$__f_ApplicationCache = null;
    this.Lorg_scalajs_dom_package$__f_Blob = null;
    this.Lorg_scalajs_dom_package$__f_BlobPropertyBag = null;
    this.Lorg_scalajs_dom_package$__f_DOMException = null;
    this.Lorg_scalajs_dom_package$__f_Event = null;
    this.Lorg_scalajs_dom_package$__f_EventException = null;
    this.Lorg_scalajs_dom_package$__f_EventSource = null;
    this.Lorg_scalajs_dom_package$__f_FileReader = null;
    this.Lorg_scalajs_dom_package$__f_FormData = null;
    this.Lorg_scalajs_dom_package$__f_KeyboardEvent = null;
    this.Lorg_scalajs_dom_package$__f_MediaError = null;
    this.Lorg_scalajs_dom_package$__f_MutationObserverInit = null;
    this.Lorg_scalajs_dom_package$__f_Node = null;
    this.Lorg_scalajs_dom_package$__f_NodeFilter = null;
    this.Lorg_scalajs_dom_package$__f_PerformanceNavigation = null;
    this.Lorg_scalajs_dom_package$__f_PositionError = null;
    this.Lorg_scalajs_dom_package$__f_Range = null;
    this.Lorg_scalajs_dom_package$__f_TextEvent = null;
    this.Lorg_scalajs_dom_package$__f_TextTrack = null;
    this.Lorg_scalajs_dom_package$__f_URL = null;
    this.Lorg_scalajs_dom_package$__f_VisibilityState = null;
    this.Lorg_scalajs_dom_package$__f_WebSocket = null;
    this.Lorg_scalajs_dom_package$__f_WheelEvent = null;
    this.Lorg_scalajs_dom_package$__f_XMLHttpRequest = null;
    this.Lorg_scalajs_dom_package$__f_XPathResult = null;
    this.Lorg_scalajs_dom_package$__f_window = null;
    this.Lorg_scalajs_dom_package$__f_document = null;
    this.Lorg_scalajs_dom_package$__f_console = null;
    this.Lorg_scalajs_dom_package$__f_bitmap$0 = 0
  };
  window__Lorg_scalajs_dom_raw_Window() {
    return (((33554432 & this.Lorg_scalajs_dom_package$__f_bitmap$0) === 0) ? $p_Lorg_scalajs_dom_package$__window$lzycompute__Lorg_scalajs_dom_raw_Window(this) : this.Lorg_scalajs_dom_package$__f_window)
  };
  document__Lorg_scalajs_dom_raw_HTMLDocument() {
    return (((67108864 & this.Lorg_scalajs_dom_package$__f_bitmap$0) === 0) ? $p_Lorg_scalajs_dom_package$__document$lzycompute__Lorg_scalajs_dom_raw_HTMLDocument(this) : this.Lorg_scalajs_dom_package$__f_document)
  };
  console__Lorg_scalajs_dom_raw_Console() {
    return (((134217728 & this.Lorg_scalajs_dom_package$__f_bitmap$0) === 0) ? $p_Lorg_scalajs_dom_package$__console$lzycompute__Lorg_scalajs_dom_raw_Console(this) : this.Lorg_scalajs_dom_package$__f_console)
  };
}
const $d_Lorg_scalajs_dom_package$ = new $TypeData().initClass({
  Lorg_scalajs_dom_package$: 0
}, false, "org.scalajs.dom.package$", {
  Lorg_scalajs_dom_package$: 1,
  O: 1
});
$c_Lorg_scalajs_dom_package$.prototype.$classData = $d_Lorg_scalajs_dom_package$;
let $n_Lorg_scalajs_dom_package$ = (void 0);
function $m_Lorg_scalajs_dom_package$() {
  if ((!$n_Lorg_scalajs_dom_package$)) {
    $n_Lorg_scalajs_dom_package$ = new $c_Lorg_scalajs_dom_package$()
  };
  return $n_Lorg_scalajs_dom_package$
}
class $c_s_FallbackArrayBuilding extends $c_O {
}
function $is_F0(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.F0)))
}
function $as_F0(obj) {
  return (($is_F0(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.Function0"))
}
function $isArrayOf_F0(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.F0)))
}
function $asArrayOf_F0(obj, depth) {
  return (($isArrayOf_F0(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.Function0;", depth))
}
function $is_F1(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.F1)))
}
function $as_F1(obj) {
  return (($is_F1(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.Function1"))
}
function $isArrayOf_F1(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.F1)))
}
function $asArrayOf_F1(obj, depth) {
  return (($isArrayOf_F1(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.Function1;", depth))
}
const $f_s_Proxy__equals__O__Z = (function($thiz, that) {
  return ((that !== null) && (((that === $thiz) || (that === $thiz.scm_Builder$$anon$1__f_self)) || $dp_equals__O__Z(that, $thiz.scm_Builder$$anon$1__f_self)))
});
const $f_s_Proxy__toString__T = (function($thiz) {
  return ("" + $thiz.scm_Builder$$anon$1__f_self)
});
class $c_sc_$colon$plus$ extends $c_O {
}
const $d_sc_$colon$plus$ = new $TypeData().initClass({
  sc_$colon$plus$: 0
}, false, "scala.collection.$colon$plus$", {
  sc_$colon$plus$: 1,
  O: 1
});
$c_sc_$colon$plus$.prototype.$classData = $d_sc_$colon$plus$;
let $n_sc_$colon$plus$ = (void 0);
function $m_sc_$colon$plus$() {
  if ((!$n_sc_$colon$plus$)) {
    $n_sc_$colon$plus$ = new $c_sc_$colon$plus$()
  };
  return $n_sc_$colon$plus$
}
class $c_sc_$plus$colon$ extends $c_O {
}
const $d_sc_$plus$colon$ = new $TypeData().initClass({
  sc_$plus$colon$: 0
}, false, "scala.collection.$plus$colon$", {
  sc_$plus$colon$: 1,
  O: 1
});
$c_sc_$plus$colon$.prototype.$classData = $d_sc_$plus$colon$;
let $n_sc_$plus$colon$ = (void 0);
function $m_sc_$plus$colon$() {
  if ((!$n_sc_$plus$colon$)) {
    $n_sc_$plus$colon$ = new $c_sc_$plus$colon$()
  };
  return $n_sc_$plus$colon$
}
function $is_sc_GenTraversableOnce(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.sc_GenTraversableOnce)))
}
function $as_sc_GenTraversableOnce(obj) {
  return (($is_sc_GenTraversableOnce(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.GenTraversableOnce"))
}
function $isArrayOf_sc_GenTraversableOnce(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sc_GenTraversableOnce)))
}
function $asArrayOf_sc_GenTraversableOnce(obj, depth) {
  return (($isArrayOf_sc_GenTraversableOnce(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.GenTraversableOnce;", depth))
}
class $c_sc_Iterator$ extends $c_O {
  constructor() {
    super();
    this.sc_Iterator$__f_empty = null;
    $n_sc_Iterator$ = this;
    this.sc_Iterator$__f_empty = new $c_sc_Iterator$$anon$2()
  };
}
const $d_sc_Iterator$ = new $TypeData().initClass({
  sc_Iterator$: 0
}, false, "scala.collection.Iterator$", {
  sc_Iterator$: 1,
  O: 1
});
$c_sc_Iterator$.prototype.$classData = $d_sc_Iterator$;
let $n_sc_Iterator$ = (void 0);
function $m_sc_Iterator$() {
  if ((!$n_sc_Iterator$)) {
    $n_sc_Iterator$ = new $c_sc_Iterator$()
  };
  return $n_sc_Iterator$
}
class $c_scg_GenericCompanion extends $c_O {
  empty__sc_GenTraversable() {
    return $as_sc_GenTraversable(this.newBuilder__scm_Builder().result__O())
  };
  apply__sc_Seq__sc_GenTraversable(elems) {
    if (elems.isEmpty__Z()) {
      return this.empty__sc_GenTraversable()
    } else {
      const b = this.newBuilder__scm_Builder();
      b.$plus$plus$eq__sc_TraversableOnce__scg_Growable(elems);
      return $as_sc_GenTraversable(b.result__O())
    }
  };
}
class $c_sci_Stream$$hash$colon$colon$ extends $c_O {
}
const $d_sci_Stream$$hash$colon$colon$ = new $TypeData().initClass({
  sci_Stream$$hash$colon$colon$: 0
}, false, "scala.collection.immutable.Stream$$hash$colon$colon$", {
  sci_Stream$$hash$colon$colon$: 1,
  O: 1
});
$c_sci_Stream$$hash$colon$colon$.prototype.$classData = $d_sci_Stream$$hash$colon$colon$;
let $n_sci_Stream$$hash$colon$colon$ = (void 0);
function $m_sci_Stream$$hash$colon$colon$() {
  if ((!$n_sci_Stream$$hash$colon$colon$)) {
    $n_sci_Stream$$hash$colon$colon$ = new $c_sci_Stream$$hash$colon$colon$()
  };
  return $n_sci_Stream$$hash$colon$colon$
}
const $p_sci_StreamIterator$LazyCell__v$lzycompute__sci_Stream = (function($thiz) {
  if ((!$thiz.sci_StreamIterator$LazyCell__f_bitmap$0)) {
    $thiz.sci_StreamIterator$LazyCell__f_v = $as_sci_Stream($thiz.sci_StreamIterator$LazyCell__f_st.apply__O());
    $thiz.sci_StreamIterator$LazyCell__f_bitmap$0 = true
  };
  $thiz.sci_StreamIterator$LazyCell__f_st = null;
  return $thiz.sci_StreamIterator$LazyCell__f_v
});
class $c_sci_StreamIterator$LazyCell extends $c_O {
  constructor(outer, st) {
    super();
    this.sci_StreamIterator$LazyCell__f_v = null;
    this.sci_StreamIterator$LazyCell__f_st = null;
    this.sci_StreamIterator$LazyCell__f_bitmap$0 = false;
    this.sci_StreamIterator$LazyCell__f_$outer = null;
    this.sci_StreamIterator$LazyCell__f_st = st;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.sci_StreamIterator$LazyCell__f_$outer = outer
    }
  };
  v__sci_Stream() {
    return ((!this.sci_StreamIterator$LazyCell__f_bitmap$0) ? $p_sci_StreamIterator$LazyCell__v$lzycompute__sci_Stream(this) : this.sci_StreamIterator$LazyCell__f_v)
  };
}
const $d_sci_StreamIterator$LazyCell = new $TypeData().initClass({
  sci_StreamIterator$LazyCell: 0
}, false, "scala.collection.immutable.StreamIterator$LazyCell", {
  sci_StreamIterator$LazyCell: 1,
  O: 1
});
$c_sci_StreamIterator$LazyCell.prototype.$classData = $d_sci_StreamIterator$LazyCell;
class $c_sci_StringOps$ extends $c_O {
  equals$extension__T__O__Z(this$, x$1) {
    if ((x$1 instanceof $c_sci_StringOps)) {
      const StringOps$1 = ((x$1 === null) ? null : $as_sci_StringOps(x$1).sci_StringOps__f_repr);
      return (this$ === StringOps$1)
    } else {
      return false
    }
  };
}
const $d_sci_StringOps$ = new $TypeData().initClass({
  sci_StringOps$: 0
}, false, "scala.collection.immutable.StringOps$", {
  sci_StringOps$: 1,
  O: 1
});
$c_sci_StringOps$.prototype.$classData = $d_sci_StringOps$;
let $n_sci_StringOps$ = (void 0);
function $m_sci_StringOps$() {
  if ((!$n_sci_StringOps$)) {
    $n_sci_StringOps$ = new $c_sci_StringOps$()
  };
  return $n_sci_StringOps$
}
const $f_sci_VectorPointer__initFrom__sci_VectorPointer__I__V = (function($thiz, that, depth) {
  $thiz.depth_$eq__I__V(depth);
  const x1 = (((-1) + depth) | 0);
  switch (x1) {
    case (-1): {
      break
    }
    case 0: {
      $thiz.display0_$eq__AO__V(that.display0__AO());
      break
    }
    case 1: {
      $thiz.display1_$eq__AO__V(that.display1__AO());
      $thiz.display0_$eq__AO__V(that.display0__AO());
      break
    }
    case 2: {
      $thiz.display2_$eq__AO__V(that.display2__AO());
      $thiz.display1_$eq__AO__V(that.display1__AO());
      $thiz.display0_$eq__AO__V(that.display0__AO());
      break
    }
    case 3: {
      $thiz.display3_$eq__AO__V(that.display3__AO());
      $thiz.display2_$eq__AO__V(that.display2__AO());
      $thiz.display1_$eq__AO__V(that.display1__AO());
      $thiz.display0_$eq__AO__V(that.display0__AO());
      break
    }
    case 4: {
      $thiz.display4_$eq__AO__V(that.display4__AO());
      $thiz.display3_$eq__AO__V(that.display3__AO());
      $thiz.display2_$eq__AO__V(that.display2__AO());
      $thiz.display1_$eq__AO__V(that.display1__AO());
      $thiz.display0_$eq__AO__V(that.display0__AO());
      break
    }
    case 5: {
      $thiz.display5_$eq__AO__V(that.display5__AO());
      $thiz.display4_$eq__AO__V(that.display4__AO());
      $thiz.display3_$eq__AO__V(that.display3__AO());
      $thiz.display2_$eq__AO__V(that.display2__AO());
      $thiz.display1_$eq__AO__V(that.display1__AO());
      $thiz.display0_$eq__AO__V(that.display0__AO());
      break
    }
    default: {
      throw new $c_s_MatchError(x1)
    }
  }
});
const $f_sci_VectorPointer__getElem__I__I__O = (function($thiz, index, xor) {
  if ((xor < 32)) {
    return $thiz.display0__AO().get((31 & index))
  } else if ((xor < 1024)) {
    return $asArrayOf_O($thiz.display1__AO().get((31 & ((index >>> 5) | 0))), 1).get((31 & index))
  } else if ((xor < 32768)) {
    return $asArrayOf_O($asArrayOf_O($thiz.display2__AO().get((31 & ((index >>> 10) | 0))), 1).get((31 & ((index >>> 5) | 0))), 1).get((31 & index))
  } else if ((xor < 1048576)) {
    return $asArrayOf_O($asArrayOf_O($asArrayOf_O($thiz.display3__AO().get((31 & ((index >>> 15) | 0))), 1).get((31 & ((index >>> 10) | 0))), 1).get((31 & ((index >>> 5) | 0))), 1).get((31 & index))
  } else if ((xor < 33554432)) {
    return $asArrayOf_O($asArrayOf_O($asArrayOf_O($asArrayOf_O($thiz.display4__AO().get((31 & ((index >>> 20) | 0))), 1).get((31 & ((index >>> 15) | 0))), 1).get((31 & ((index >>> 10) | 0))), 1).get((31 & ((index >>> 5) | 0))), 1).get((31 & index))
  } else if ((xor < 1073741824)) {
    return $asArrayOf_O($asArrayOf_O($asArrayOf_O($asArrayOf_O($asArrayOf_O($thiz.display5__AO().get((31 & ((index >>> 25) | 0))), 1).get((31 & ((index >>> 20) | 0))), 1).get((31 & ((index >>> 15) | 0))), 1).get((31 & ((index >>> 10) | 0))), 1).get((31 & ((index >>> 5) | 0))), 1).get((31 & index))
  } else {
    throw $ct_jl_IllegalArgumentException__(new $c_jl_IllegalArgumentException())
  }
});
const $f_sci_VectorPointer__gotoPos__I__I__V = (function($thiz, index, xor) {
  if ((xor >= 32)) {
    if ((xor < 1024)) {
      $thiz.display0_$eq__AO__V($asArrayOf_O($thiz.display1__AO().get((31 & ((index >>> 5) | 0))), 1))
    } else if ((xor < 32768)) {
      $thiz.display1_$eq__AO__V($asArrayOf_O($thiz.display2__AO().get((31 & ((index >>> 10) | 0))), 1));
      $thiz.display0_$eq__AO__V($asArrayOf_O($thiz.display1__AO().get((31 & ((index >>> 5) | 0))), 1))
    } else if ((xor < 1048576)) {
      $thiz.display2_$eq__AO__V($asArrayOf_O($thiz.display3__AO().get((31 & ((index >>> 15) | 0))), 1));
      $thiz.display1_$eq__AO__V($asArrayOf_O($thiz.display2__AO().get((31 & ((index >>> 10) | 0))), 1));
      $thiz.display0_$eq__AO__V($asArrayOf_O($thiz.display1__AO().get((31 & ((index >>> 5) | 0))), 1))
    } else if ((xor < 33554432)) {
      $thiz.display3_$eq__AO__V($asArrayOf_O($thiz.display4__AO().get((31 & ((index >>> 20) | 0))), 1));
      $thiz.display2_$eq__AO__V($asArrayOf_O($thiz.display3__AO().get((31 & ((index >>> 15) | 0))), 1));
      $thiz.display1_$eq__AO__V($asArrayOf_O($thiz.display2__AO().get((31 & ((index >>> 10) | 0))), 1));
      $thiz.display0_$eq__AO__V($asArrayOf_O($thiz.display1__AO().get((31 & ((index >>> 5) | 0))), 1))
    } else if ((xor < 1073741824)) {
      $thiz.display4_$eq__AO__V($asArrayOf_O($thiz.display5__AO().get((31 & ((index >>> 25) | 0))), 1));
      $thiz.display3_$eq__AO__V($asArrayOf_O($thiz.display4__AO().get((31 & ((index >>> 20) | 0))), 1));
      $thiz.display2_$eq__AO__V($asArrayOf_O($thiz.display3__AO().get((31 & ((index >>> 15) | 0))), 1));
      $thiz.display1_$eq__AO__V($asArrayOf_O($thiz.display2__AO().get((31 & ((index >>> 10) | 0))), 1));
      $thiz.display0_$eq__AO__V($asArrayOf_O($thiz.display1__AO().get((31 & ((index >>> 5) | 0))), 1))
    } else {
      throw $ct_jl_IllegalArgumentException__(new $c_jl_IllegalArgumentException())
    }
  }
});
const $f_sci_VectorPointer__gotoNextBlockStart__I__I__V = (function($thiz, index, xor) {
  if ((xor < 1024)) {
    $thiz.display0_$eq__AO__V($asArrayOf_O($thiz.display1__AO().get((31 & ((index >>> 5) | 0))), 1))
  } else if ((xor < 32768)) {
    $thiz.display1_$eq__AO__V($asArrayOf_O($thiz.display2__AO().get((31 & ((index >>> 10) | 0))), 1));
    $thiz.display0_$eq__AO__V($asArrayOf_O($thiz.display1__AO().get(0), 1))
  } else if ((xor < 1048576)) {
    $thiz.display2_$eq__AO__V($asArrayOf_O($thiz.display3__AO().get((31 & ((index >>> 15) | 0))), 1));
    $thiz.display1_$eq__AO__V($asArrayOf_O($thiz.display2__AO().get(0), 1));
    $thiz.display0_$eq__AO__V($asArrayOf_O($thiz.display1__AO().get(0), 1))
  } else if ((xor < 33554432)) {
    $thiz.display3_$eq__AO__V($asArrayOf_O($thiz.display4__AO().get((31 & ((index >>> 20) | 0))), 1));
    $thiz.display2_$eq__AO__V($asArrayOf_O($thiz.display3__AO().get(0), 1));
    $thiz.display1_$eq__AO__V($asArrayOf_O($thiz.display2__AO().get(0), 1));
    $thiz.display0_$eq__AO__V($asArrayOf_O($thiz.display1__AO().get(0), 1))
  } else if ((xor < 1073741824)) {
    $thiz.display4_$eq__AO__V($asArrayOf_O($thiz.display5__AO().get((31 & ((index >>> 25) | 0))), 1));
    $thiz.display3_$eq__AO__V($asArrayOf_O($thiz.display4__AO().get(0), 1));
    $thiz.display2_$eq__AO__V($asArrayOf_O($thiz.display3__AO().get(0), 1));
    $thiz.display1_$eq__AO__V($asArrayOf_O($thiz.display2__AO().get(0), 1));
    $thiz.display0_$eq__AO__V($asArrayOf_O($thiz.display1__AO().get(0), 1))
  } else {
    throw $ct_jl_IllegalArgumentException__(new $c_jl_IllegalArgumentException())
  }
});
const $f_sci_VectorPointer__gotoNextBlockStartWritable__I__I__V = (function($thiz, index, xor) {
  if ((xor < 1024)) {
    if (($thiz.depth__I() === 1)) {
      $thiz.display1_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
      $thiz.display1__AO().set(0, $thiz.display0__AO());
      $thiz.depth_$eq__I__V(((1 + $thiz.depth__I()) | 0))
    };
    $thiz.display0_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
    $thiz.display1__AO().set((31 & ((index >>> 5) | 0)), $thiz.display0__AO())
  } else if ((xor < 32768)) {
    if (($thiz.depth__I() === 2)) {
      $thiz.display2_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
      $thiz.display2__AO().set(0, $thiz.display1__AO());
      $thiz.depth_$eq__I__V(((1 + $thiz.depth__I()) | 0))
    };
    $thiz.display0_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
    $thiz.display1_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
    $thiz.display1__AO().set((31 & ((index >>> 5) | 0)), $thiz.display0__AO());
    $thiz.display2__AO().set((31 & ((index >>> 10) | 0)), $thiz.display1__AO())
  } else if ((xor < 1048576)) {
    if (($thiz.depth__I() === 3)) {
      $thiz.display3_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
      $thiz.display3__AO().set(0, $thiz.display2__AO());
      $thiz.depth_$eq__I__V(((1 + $thiz.depth__I()) | 0))
    };
    $thiz.display0_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
    $thiz.display1_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
    $thiz.display2_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
    $thiz.display1__AO().set((31 & ((index >>> 5) | 0)), $thiz.display0__AO());
    $thiz.display2__AO().set((31 & ((index >>> 10) | 0)), $thiz.display1__AO());
    $thiz.display3__AO().set((31 & ((index >>> 15) | 0)), $thiz.display2__AO())
  } else if ((xor < 33554432)) {
    if (($thiz.depth__I() === 4)) {
      $thiz.display4_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
      $thiz.display4__AO().set(0, $thiz.display3__AO());
      $thiz.depth_$eq__I__V(((1 + $thiz.depth__I()) | 0))
    };
    $thiz.display0_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
    $thiz.display1_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
    $thiz.display2_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
    $thiz.display3_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
    $thiz.display1__AO().set((31 & ((index >>> 5) | 0)), $thiz.display0__AO());
    $thiz.display2__AO().set((31 & ((index >>> 10) | 0)), $thiz.display1__AO());
    $thiz.display3__AO().set((31 & ((index >>> 15) | 0)), $thiz.display2__AO());
    $thiz.display4__AO().set((31 & ((index >>> 20) | 0)), $thiz.display3__AO())
  } else if ((xor < 1073741824)) {
    if (($thiz.depth__I() === 5)) {
      $thiz.display5_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
      $thiz.display5__AO().set(0, $thiz.display4__AO());
      $thiz.depth_$eq__I__V(((1 + $thiz.depth__I()) | 0))
    };
    $thiz.display0_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
    $thiz.display1_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
    $thiz.display2_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
    $thiz.display3_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
    $thiz.display4_$eq__AO__V($newArrayObject($d_O.getArrayOf(), [32]));
    $thiz.display1__AO().set((31 & ((index >>> 5) | 0)), $thiz.display0__AO());
    $thiz.display2__AO().set((31 & ((index >>> 10) | 0)), $thiz.display1__AO());
    $thiz.display3__AO().set((31 & ((index >>> 15) | 0)), $thiz.display2__AO());
    $thiz.display4__AO().set((31 & ((index >>> 20) | 0)), $thiz.display3__AO());
    $thiz.display5__AO().set((31 & ((index >>> 25) | 0)), $thiz.display4__AO())
  } else {
    throw $ct_jl_IllegalArgumentException__(new $c_jl_IllegalArgumentException())
  }
});
const $f_sci_VectorPointer__copyOf__AO__AO = (function($thiz, a) {
  const copy = $newArrayObject($d_O.getArrayOf(), [a.u.length]);
  const length = a.u.length;
  $systemArraycopy(a, 0, copy, 0, length);
  return copy
});
const $f_sci_VectorPointer__stabilize__I__V = (function($thiz, index) {
  const x1 = (((-1) + $thiz.depth__I()) | 0);
  switch (x1) {
    case 5: {
      const a = $thiz.display5__AO();
      $thiz.display5_$eq__AO__V($f_sci_VectorPointer__copyOf__AO__AO($thiz, a));
      const a$1 = $thiz.display4__AO();
      $thiz.display4_$eq__AO__V($f_sci_VectorPointer__copyOf__AO__AO($thiz, a$1));
      const a$2 = $thiz.display3__AO();
      $thiz.display3_$eq__AO__V($f_sci_VectorPointer__copyOf__AO__AO($thiz, a$2));
      const a$3 = $thiz.display2__AO();
      $thiz.display2_$eq__AO__V($f_sci_VectorPointer__copyOf__AO__AO($thiz, a$3));
      const a$4 = $thiz.display1__AO();
      $thiz.display1_$eq__AO__V($f_sci_VectorPointer__copyOf__AO__AO($thiz, a$4));
      $thiz.display5__AO().set((31 & ((index >>> 25) | 0)), $thiz.display4__AO());
      $thiz.display4__AO().set((31 & ((index >>> 20) | 0)), $thiz.display3__AO());
      $thiz.display3__AO().set((31 & ((index >>> 15) | 0)), $thiz.display2__AO());
      $thiz.display2__AO().set((31 & ((index >>> 10) | 0)), $thiz.display1__AO());
      $thiz.display1__AO().set((31 & ((index >>> 5) | 0)), $thiz.display0__AO());
      break
    }
    case 4: {
      const a$5 = $thiz.display4__AO();
      $thiz.display4_$eq__AO__V($f_sci_VectorPointer__copyOf__AO__AO($thiz, a$5));
      const a$6 = $thiz.display3__AO();
      $thiz.display3_$eq__AO__V($f_sci_VectorPointer__copyOf__AO__AO($thiz, a$6));
      const a$7 = $thiz.display2__AO();
      $thiz.display2_$eq__AO__V($f_sci_VectorPointer__copyOf__AO__AO($thiz, a$7));
      const a$8 = $thiz.display1__AO();
      $thiz.display1_$eq__AO__V($f_sci_VectorPointer__copyOf__AO__AO($thiz, a$8));
      $thiz.display4__AO().set((31 & ((index >>> 20) | 0)), $thiz.display3__AO());
      $thiz.display3__AO().set((31 & ((index >>> 15) | 0)), $thiz.display2__AO());
      $thiz.display2__AO().set((31 & ((index >>> 10) | 0)), $thiz.display1__AO());
      $thiz.display1__AO().set((31 & ((index >>> 5) | 0)), $thiz.display0__AO());
      break
    }
    case 3: {
      const a$9 = $thiz.display3__AO();
      $thiz.display3_$eq__AO__V($f_sci_VectorPointer__copyOf__AO__AO($thiz, a$9));
      const a$10 = $thiz.display2__AO();
      $thiz.display2_$eq__AO__V($f_sci_VectorPointer__copyOf__AO__AO($thiz, a$10));
      const a$11 = $thiz.display1__AO();
      $thiz.display1_$eq__AO__V($f_sci_VectorPointer__copyOf__AO__AO($thiz, a$11));
      $thiz.display3__AO().set((31 & ((index >>> 15) | 0)), $thiz.display2__AO());
      $thiz.display2__AO().set((31 & ((index >>> 10) | 0)), $thiz.display1__AO());
      $thiz.display1__AO().set((31 & ((index >>> 5) | 0)), $thiz.display0__AO());
      break
    }
    case 2: {
      const a$12 = $thiz.display2__AO();
      $thiz.display2_$eq__AO__V($f_sci_VectorPointer__copyOf__AO__AO($thiz, a$12));
      const a$13 = $thiz.display1__AO();
      $thiz.display1_$eq__AO__V($f_sci_VectorPointer__copyOf__AO__AO($thiz, a$13));
      $thiz.display2__AO().set((31 & ((index >>> 10) | 0)), $thiz.display1__AO());
      $thiz.display1__AO().set((31 & ((index >>> 5) | 0)), $thiz.display0__AO());
      break
    }
    case 1: {
      const a$14 = $thiz.display1__AO();
      $thiz.display1_$eq__AO__V($f_sci_VectorPointer__copyOf__AO__AO($thiz, a$14));
      $thiz.display1__AO().set((31 & ((index >>> 5) | 0)), $thiz.display0__AO());
      break
    }
    case 0: {
      break
    }
    default: {
      throw new $c_s_MatchError(x1)
    }
  }
});
class $c_sci_WrappedString$ extends $c_O {
  newBuilder__scm_Builder() {
    const this$3 = $ct_scm_StringBuilder__(new $c_scm_StringBuilder());
    const f = new $c_sjsr_AnonFunction1(((this$2) => ((x$2) => {
      const x = $as_T(x$2);
      return new $c_sci_WrappedString(x)
    }))(this));
    return new $c_scm_Builder$$anon$1(this$3, f)
  };
}
const $d_sci_WrappedString$ = new $TypeData().initClass({
  sci_WrappedString$: 0
}, false, "scala.collection.immutable.WrappedString$", {
  sci_WrappedString$: 1,
  O: 1
});
$c_sci_WrappedString$.prototype.$classData = $d_sci_WrappedString$;
let $n_sci_WrappedString$ = (void 0);
function $m_sci_WrappedString$() {
  if ((!$n_sci_WrappedString$)) {
    $n_sci_WrappedString$ = new $c_sci_WrappedString$()
  };
  return $n_sci_WrappedString$
}
class $c_s_math_Ordered$ extends $c_O {
}
const $d_s_math_Ordered$ = new $TypeData().initClass({
  s_math_Ordered$: 0
}, false, "scala.math.Ordered$", {
  s_math_Ordered$: 1,
  O: 1
});
$c_s_math_Ordered$.prototype.$classData = $d_s_math_Ordered$;
let $n_s_math_Ordered$ = (void 0);
function $m_s_math_Ordered$() {
  if ((!$n_s_math_Ordered$)) {
    $n_s_math_Ordered$ = new $c_s_math_Ordered$()
  };
  return $n_s_math_Ordered$
}
class $c_s_package$ extends $c_O {
  constructor() {
    super();
    this.s_package$__f_BigDecimal = null;
    this.s_package$__f_BigInt = null;
    this.s_package$__f_AnyRef = null;
    this.s_package$__f_Traversable = null;
    this.s_package$__f_Iterable = null;
    this.s_package$__f_Seq = null;
    this.s_package$__f_IndexedSeq = null;
    this.s_package$__f_Iterator = null;
    this.s_package$__f_List = null;
    this.s_package$__f_Nil = null;
    this.s_package$__f_$colon$colon = null;
    this.s_package$__f_$plus$colon = null;
    this.s_package$__f_$colon$plus = null;
    this.s_package$__f_Stream = null;
    this.s_package$__f_$hash$colon$colon = null;
    this.s_package$__f_Vector = null;
    this.s_package$__f_StringBuilder = null;
    this.s_package$__f_Range = null;
    this.s_package$__f_Equiv = null;
    this.s_package$__f_Fractional = null;
    this.s_package$__f_Integral = null;
    this.s_package$__f_Numeric = null;
    this.s_package$__f_Ordered = null;
    this.s_package$__f_Ordering = null;
    this.s_package$__f_Either = null;
    this.s_package$__f_Left = null;
    this.s_package$__f_Right = null;
    this.s_package$__f_bitmap$0 = 0;
    $n_s_package$ = this;
    this.s_package$__f_AnyRef = new $c_s_package$$anon$1();
    this.s_package$__f_Traversable = $m_sc_Traversable$();
    this.s_package$__f_Iterable = $m_sc_Iterable$();
    this.s_package$__f_Seq = $m_sc_Seq$();
    this.s_package$__f_IndexedSeq = $m_sc_IndexedSeq$();
    this.s_package$__f_Iterator = $m_sc_Iterator$();
    this.s_package$__f_List = $m_sci_List$();
    this.s_package$__f_Nil = $m_sci_Nil$();
    this.s_package$__f_$colon$colon = $m_sci_$colon$colon$();
    this.s_package$__f_$plus$colon = $m_sc_$plus$colon$();
    this.s_package$__f_$colon$plus = $m_sc_$colon$plus$();
    this.s_package$__f_Stream = $m_sci_Stream$();
    this.s_package$__f_$hash$colon$colon = $m_sci_Stream$$hash$colon$colon$();
    this.s_package$__f_Vector = $m_sci_Vector$();
    this.s_package$__f_StringBuilder = $m_scm_StringBuilder$();
    this.s_package$__f_Range = $m_sci_Range$();
    this.s_package$__f_Equiv = $m_s_math_Equiv$();
    this.s_package$__f_Fractional = $m_s_math_Fractional$();
    this.s_package$__f_Integral = $m_s_math_Integral$();
    this.s_package$__f_Numeric = $m_s_math_Numeric$();
    this.s_package$__f_Ordered = $m_s_math_Ordered$();
    this.s_package$__f_Ordering = $m_s_math_Ordering$();
    this.s_package$__f_Either = $m_s_util_Either$();
    this.s_package$__f_Left = $m_s_util_Left$();
    this.s_package$__f_Right = $m_s_util_Right$()
  };
}
const $d_s_package$ = new $TypeData().initClass({
  s_package$: 0
}, false, "scala.package$", {
  s_package$: 1,
  O: 1
});
$c_s_package$.prototype.$classData = $d_s_package$;
let $n_s_package$ = (void 0);
function $m_s_package$() {
  if ((!$n_s_package$)) {
    $n_s_package$ = new $c_s_package$()
  };
  return $n_s_package$
}
class $c_sr_BoxesRunTime$ extends $c_O {
  equals__O__O__Z(x, y) {
    if ((x === y)) {
      return true
    } else if ($is_jl_Number(x)) {
      const x2 = $as_jl_Number(x);
      return this.equalsNumObject__jl_Number__O__Z(x2, y)
    } else if ((x instanceof $Char)) {
      const x3 = $as_jl_Character(x);
      return this.equalsCharObject__jl_Character__O__Z(x3, y)
    } else {
      return ((x === null) ? (y === null) : $dp_equals__O__Z(x, y))
    }
  };
  equalsNumObject__jl_Number__O__Z(xn, y) {
    if ($is_jl_Number(y)) {
      const x2 = $as_jl_Number(y);
      return this.equalsNumNum__jl_Number__jl_Number__Z(xn, x2)
    } else if ((y instanceof $Char)) {
      const x3 = $as_jl_Character(y);
      if (((typeof xn) === "number")) {
        const x2$1 = $uD(xn);
        return (x2$1 === $uC(x3))
      } else if ((xn instanceof $c_RTLong)) {
        const t = $uJ(xn);
        const lo = t.RTLong__f_lo;
        const hi = t.RTLong__f_hi;
        const value = $uC(x3);
        const hi$1 = (value >> 31);
        return ((lo === value) && (hi === hi$1))
      } else {
        return ((xn === null) ? (x3 === null) : $dp_equals__O__Z(xn, x3))
      }
    } else {
      return ((xn === null) ? (y === null) : $dp_equals__O__Z(xn, y))
    }
  };
  equalsNumNum__jl_Number__jl_Number__Z(xn, yn) {
    if (((typeof xn) === "number")) {
      const x2 = $uD(xn);
      if (((typeof yn) === "number")) {
        const x2$2 = $uD(yn);
        return (x2 === x2$2)
      } else if ((yn instanceof $c_RTLong)) {
        const t = $uJ(yn);
        const lo = t.RTLong__f_lo;
        const hi = t.RTLong__f_hi;
        return (x2 === $m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$toDouble__I__I__D(lo, hi))
      } else if ((yn instanceof $c_s_math_ScalaNumber)) {
        const x4 = $as_s_math_ScalaNumber(yn);
        return x4.equals__O__Z(x2)
      } else {
        return false
      }
    } else if ((xn instanceof $c_RTLong)) {
      const t$1 = $uJ(xn);
      const lo$1 = t$1.RTLong__f_lo;
      const hi$1 = t$1.RTLong__f_hi;
      if ((yn instanceof $c_RTLong)) {
        const t$2 = $uJ(yn);
        const lo$2 = t$2.RTLong__f_lo;
        const hi$2 = t$2.RTLong__f_hi;
        return ((lo$1 === lo$2) && (hi$1 === hi$2))
      } else if (((typeof yn) === "number")) {
        const x3$3 = $uD(yn);
        return ($m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$toDouble__I__I__D(lo$1, hi$1) === x3$3)
      } else if ((yn instanceof $c_s_math_ScalaNumber)) {
        const x4$2 = $as_s_math_ScalaNumber(yn);
        return x4$2.equals__O__Z(new $c_RTLong(lo$1, hi$1))
      } else {
        return false
      }
    } else {
      return ((xn === null) ? (yn === null) : $dp_equals__O__Z(xn, yn))
    }
  };
  equalsCharObject__jl_Character__O__Z(xc, y) {
    if ((y instanceof $Char)) {
      const x2 = $as_jl_Character(y);
      return ($uC(xc) === $uC(x2))
    } else if ($is_jl_Number(y)) {
      const x3 = $as_jl_Number(y);
      if (((typeof x3) === "number")) {
        const x2$1 = $uD(x3);
        return (x2$1 === $uC(xc))
      } else if ((x3 instanceof $c_RTLong)) {
        const t = $uJ(x3);
        const lo = t.RTLong__f_lo;
        const hi = t.RTLong__f_hi;
        const value = $uC(xc);
        const hi$1 = (value >> 31);
        return ((lo === value) && (hi === hi$1))
      } else {
        return ((x3 === null) ? (xc === null) : $dp_equals__O__Z(x3, xc))
      }
    } else {
      return ((xc === null) && (y === null))
    }
  };
}
const $d_sr_BoxesRunTime$ = new $TypeData().initClass({
  sr_BoxesRunTime$: 0
}, false, "scala.runtime.BoxesRunTime$", {
  sr_BoxesRunTime$: 1,
  O: 1
});
$c_sr_BoxesRunTime$.prototype.$classData = $d_sr_BoxesRunTime$;
let $n_sr_BoxesRunTime$ = (void 0);
function $m_sr_BoxesRunTime$() {
  if ((!$n_sr_BoxesRunTime$)) {
    $n_sr_BoxesRunTime$ = new $c_sr_BoxesRunTime$()
  };
  return $n_sr_BoxesRunTime$
}
class $c_sr_ScalaRunTime$ extends $c_O {
  array_apply__O__I__O(xs, idx) {
    if ($isArrayOf_O(xs, 1)) {
      const x2 = $asArrayOf_O(xs, 1);
      return x2.get(idx)
    } else if ($isArrayOf_I(xs, 1)) {
      const x3 = $asArrayOf_I(xs, 1);
      return x3.get(idx)
    } else if ($isArrayOf_D(xs, 1)) {
      const x4 = $asArrayOf_D(xs, 1);
      return x4.get(idx)
    } else if ($isArrayOf_J(xs, 1)) {
      const x5 = $asArrayOf_J(xs, 1);
      return x5.get(idx)
    } else if ($isArrayOf_F(xs, 1)) {
      const x6 = $asArrayOf_F(xs, 1);
      return x6.get(idx)
    } else if ($isArrayOf_C(xs, 1)) {
      const x7 = $asArrayOf_C(xs, 1);
      return $bC(x7.get(idx))
    } else if ($isArrayOf_B(xs, 1)) {
      const x8 = $asArrayOf_B(xs, 1);
      return x8.get(idx)
    } else if ($isArrayOf_S(xs, 1)) {
      const x9 = $asArrayOf_S(xs, 1);
      return x9.get(idx)
    } else if ($isArrayOf_Z(xs, 1)) {
      const x10 = $asArrayOf_Z(xs, 1);
      return x10.get(idx)
    } else if ($isArrayOf_jl_Void(xs, 1)) {
      const x11 = $asArrayOf_jl_Void(xs, 1);
      return x11.get(idx)
    } else if ((xs === null)) {
      throw new $c_jl_NullPointerException()
    } else {
      throw new $c_s_MatchError(xs)
    }
  };
  array_update__O__I__O__V(xs, idx, value) {
    if ($isArrayOf_O(xs, 1)) {
      const x2 = $asArrayOf_O(xs, 1);
      x2.set(idx, value)
    } else if ($isArrayOf_I(xs, 1)) {
      const x3 = $asArrayOf_I(xs, 1);
      x3.set(idx, $uI(value))
    } else if ($isArrayOf_D(xs, 1)) {
      const x4 = $asArrayOf_D(xs, 1);
      x4.set(idx, $uD(value))
    } else if ($isArrayOf_J(xs, 1)) {
      const x5 = $asArrayOf_J(xs, 1);
      x5.set(idx, $uJ(value))
    } else if ($isArrayOf_F(xs, 1)) {
      const x6 = $asArrayOf_F(xs, 1);
      x6.set(idx, $uF(value))
    } else if ($isArrayOf_C(xs, 1)) {
      const x7 = $asArrayOf_C(xs, 1);
      x7.set(idx, $uC(value))
    } else if ($isArrayOf_B(xs, 1)) {
      const x8 = $asArrayOf_B(xs, 1);
      x8.set(idx, $uB(value))
    } else if ($isArrayOf_S(xs, 1)) {
      const x9 = $asArrayOf_S(xs, 1);
      x9.set(idx, $uS(value))
    } else if ($isArrayOf_Z(xs, 1)) {
      const x10 = $asArrayOf_Z(xs, 1);
      x10.set(idx, $uZ(value))
    } else if ($isArrayOf_jl_Void(xs, 1)) {
      const x11 = $asArrayOf_jl_Void(xs, 1);
      x11.set(idx, (void 0))
    } else if ((xs === null)) {
      throw new $c_jl_NullPointerException()
    } else {
      throw new $c_s_MatchError(xs)
    }
  };
  array_length__O__I(xs) {
    if ($isArrayOf_O(xs, 1)) {
      const x2 = $asArrayOf_O(xs, 1);
      return x2.u.length
    } else if ($isArrayOf_I(xs, 1)) {
      const x3 = $asArrayOf_I(xs, 1);
      return x3.u.length
    } else if ($isArrayOf_D(xs, 1)) {
      const x4 = $asArrayOf_D(xs, 1);
      return x4.u.length
    } else if ($isArrayOf_J(xs, 1)) {
      const x5 = $asArrayOf_J(xs, 1);
      return x5.u.length
    } else if ($isArrayOf_F(xs, 1)) {
      const x6 = $asArrayOf_F(xs, 1);
      return x6.u.length
    } else if ($isArrayOf_C(xs, 1)) {
      const x7 = $asArrayOf_C(xs, 1);
      return x7.u.length
    } else if ($isArrayOf_B(xs, 1)) {
      const x8 = $asArrayOf_B(xs, 1);
      return x8.u.length
    } else if ($isArrayOf_S(xs, 1)) {
      const x9 = $asArrayOf_S(xs, 1);
      return x9.u.length
    } else if ($isArrayOf_Z(xs, 1)) {
      const x10 = $asArrayOf_Z(xs, 1);
      return x10.u.length
    } else if ($isArrayOf_jl_Void(xs, 1)) {
      const x11 = $asArrayOf_jl_Void(xs, 1);
      return x11.u.length
    } else if ((xs === null)) {
      throw new $c_jl_NullPointerException()
    } else {
      throw new $c_s_MatchError(xs)
    }
  };
  _toString__s_Product__T(x) {
    const this$1 = x.productIterator__sc_Iterator();
    const start = (x.productPrefix__T() + "(");
    return $f_sc_TraversableOnce__mkString__T__T__T__T(this$1, start, ",", ")")
  };
}
const $d_sr_ScalaRunTime$ = new $TypeData().initClass({
  sr_ScalaRunTime$: 0
}, false, "scala.runtime.ScalaRunTime$", {
  sr_ScalaRunTime$: 1,
  O: 1
});
$c_sr_ScalaRunTime$.prototype.$classData = $d_sr_ScalaRunTime$;
let $n_sr_ScalaRunTime$ = (void 0);
function $m_sr_ScalaRunTime$() {
  if ((!$n_sr_ScalaRunTime$)) {
    $n_sr_ScalaRunTime$ = new $c_sr_ScalaRunTime$()
  };
  return $n_sr_ScalaRunTime$
}
class $c_sr_Statics$ extends $c_O {
  mix__I__I__I(hash, data) {
    let h = this.mixLast__I__I__I(hash, data);
    const i = h;
    h = ((i << 13) | ((i >>> 19) | 0));
    return (((-430675100) + $imul(5, h)) | 0)
  };
  mixLast__I__I__I(hash, data) {
    let k = data;
    k = $imul((-862048943), k);
    const i = k;
    k = ((i << 15) | ((i >>> 17) | 0));
    k = $imul(461845907, k);
    return (hash ^ k)
  };
  finalizeHash__I__I__I(hash, length) {
    return this.avalanche__I__I((hash ^ length))
  };
  avalanche__I__I(h0) {
    let h = h0;
    h = (h ^ ((h >>> 16) | 0));
    h = $imul((-2048144789), h);
    h = (h ^ ((h >>> 13) | 0));
    h = $imul((-1028477387), h);
    h = (h ^ ((h >>> 16) | 0));
    return h
  };
  longHash__J__I(lv) {
    const lo = lv.RTLong__f_lo;
    const lo$1 = lv.RTLong__f_hi;
    return ((lo$1 === (lo >> 31)) ? lo : (lo ^ lo$1))
  };
  doubleHash__D__I(dv) {
    const iv = $doubleToInt(dv);
    if ((iv === dv)) {
      return iv
    } else {
      const this$1 = $m_RTLong$();
      const lo = this$1.org$scalajs$linker$runtime$RuntimeLong$$fromDoubleImpl__D__I(dv);
      const hi = this$1.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn;
      return (($m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$toDouble__I__I__D(lo, hi) === dv) ? (lo ^ hi) : $m_jl_FloatingPointBits$().numberHashCode__D__I(dv))
    }
  };
  anyHash__O__I(x) {
    if ((x === null)) {
      return 0
    } else if (((typeof x) === "number")) {
      const x3 = $uD(x);
      return this.doubleHash__D__I(x3)
    } else if ((x instanceof $c_RTLong)) {
      const t = $uJ(x);
      const lo = t.RTLong__f_lo;
      const hi = t.RTLong__f_hi;
      return this.longHash__J__I(new $c_RTLong(lo, hi))
    } else {
      return $dp_hashCode__I(x)
    }
  };
}
const $d_sr_Statics$ = new $TypeData().initClass({
  sr_Statics$: 0
}, false, "scala.runtime.Statics$", {
  sr_Statics$: 1,
  O: 1
});
$c_sr_Statics$.prototype.$classData = $d_sr_Statics$;
let $n_sr_Statics$ = (void 0);
function $m_sr_Statics$() {
  if ((!$n_sr_Statics$)) {
    $n_sr_Statics$ = new $c_sr_Statics$()
  };
  return $n_sr_Statics$
}
class $c_sjsr_package$ extends $c_O {
  wrapJavaScriptException__O__jl_Throwable(e) {
    if ((e instanceof $c_jl_Throwable)) {
      const x2 = $as_jl_Throwable(e);
      return x2
    } else {
      return new $c_sjs_js_JavaScriptException(e)
    }
  };
  unwrapJavaScriptException__jl_Throwable__O(th) {
    if ((th instanceof $c_sjs_js_JavaScriptException)) {
      const x2 = $as_sjs_js_JavaScriptException(th);
      const e = x2.sjs_js_JavaScriptException__f_exception;
      return e
    } else {
      return th
    }
  };
}
const $d_sjsr_package$ = new $TypeData().initClass({
  sjsr_package$: 0
}, false, "scala.scalajs.runtime.package$", {
  sjsr_package$: 1,
  O: 1
});
$c_sjsr_package$.prototype.$classData = $d_sjsr_package$;
let $n_sjsr_package$ = (void 0);
function $m_sjsr_package$() {
  if ((!$n_sjsr_package$)) {
    $n_sjsr_package$ = new $c_sjsr_package$()
  };
  return $n_sjsr_package$
}
class $c_s_util_control_Breaks extends $c_O {
  constructor() {
    super();
    this.s_util_control_Breaks__f_scala$util$control$Breaks$$breakException = null;
    this.s_util_control_Breaks__f_scala$util$control$Breaks$$breakException = new $c_s_util_control_BreakControl()
  };
}
const $d_s_util_control_Breaks = new $TypeData().initClass({
  s_util_control_Breaks: 0
}, false, "scala.util.control.Breaks", {
  s_util_control_Breaks: 1,
  O: 1
});
$c_s_util_control_Breaks.prototype.$classData = $d_s_util_control_Breaks;
const $f_s_util_control_NoStackTrace__fillInStackTrace__jl_Throwable = (function($thiz) {
  const this$1 = $m_s_util_control_NoStackTrace$();
  if (this$1.s_util_control_NoStackTrace$__f__noSuppression) {
    return $c_jl_Throwable.prototype.fillInStackTrace__jl_Throwable.call($thiz)
  } else {
    return $as_jl_Throwable($thiz)
  }
});
const $p_s_util_hashing_MurmurHash3__avalanche__I__I = (function($thiz, hash) {
  let h = hash;
  h = (h ^ ((h >>> 16) | 0));
  h = $imul((-2048144789), h);
  h = (h ^ ((h >>> 13) | 0));
  h = $imul((-1028477387), h);
  h = (h ^ ((h >>> 16) | 0));
  return h
});
class $c_s_util_hashing_MurmurHash3 extends $c_O {
  mix__I__I__I(hash, data) {
    let h = this.mixLast__I__I__I(hash, data);
    const i = h;
    h = ((i << 13) | ((i >>> 19) | 0));
    return (((-430675100) + $imul(5, h)) | 0)
  };
  mixLast__I__I__I(hash, data) {
    let k = data;
    k = $imul((-862048943), k);
    const i = k;
    k = ((i << 15) | ((i >>> 17) | 0));
    k = $imul(461845907, k);
    return (hash ^ k)
  };
  finalizeHash__I__I__I(hash, length) {
    return $p_s_util_hashing_MurmurHash3__avalanche__I__I(this, (hash ^ length))
  };
  productHash__s_Product__I__I(x, seed) {
    const arr = x.productArity__I();
    if ((arr === 0)) {
      return $f_T__hashCode__I(x.productPrefix__T())
    } else {
      let h = seed;
      let i = 0;
      while ((i < arr)) {
        const $$x1 = h;
        const x$1 = x.productElement__I__O(i);
        h = this.mix__I__I__I($$x1, $m_sr_Statics$().anyHash__O__I(x$1));
        i = ((1 + i) | 0)
      };
      return this.finalizeHash__I__I__I(h, arr)
    }
  };
  orderedHash__sc_TraversableOnce__I__I(xs, seed) {
    const n = new $c_sr_IntRef(0);
    const h = new $c_sr_IntRef(seed);
    xs.foreach__F1__V(new $c_sjsr_AnonFunction1(((this$3, h$1, n$1) => ((x$2) => {
      h$1.sr_IntRef__f_elem = this$3.mix__I__I__I(h$1.sr_IntRef__f_elem, $m_sr_Statics$().anyHash__O__I(x$2));
      n$1.sr_IntRef__f_elem = ((1 + n$1.sr_IntRef__f_elem) | 0)
    }))(this, h, n)));
    return this.finalizeHash__I__I__I(h.sr_IntRef__f_elem, n.sr_IntRef__f_elem)
  };
  listHash__sci_List__I__I(xs, seed) {
    let n = 0;
    let h = seed;
    let elems = xs;
    while ((!elems.isEmpty__Z())) {
      const head = elems.head__O();
      const this$1 = elems;
      const tail = this$1.tail__sci_List();
      h = this.mix__I__I__I(h, $m_sr_Statics$().anyHash__O__I(head));
      n = ((1 + n) | 0);
      elems = tail
    };
    return this.finalizeHash__I__I__I(h, n)
  };
}
const $f_Lsgl_GameLoopComponent__framePeriod__I__J = (function($thiz, fps) {
  const this$1 = $m_RTLong$();
  const value = (1000.0 / fps);
  const lo = this$1.org$scalajs$linker$runtime$RuntimeLong$$fromDoubleImpl__D__I(value);
  const hi = this$1.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn;
  return new $c_RTLong(lo, hi)
});
const $f_Lsgl_GameLoopComponent__gameLoopStep__J__Lsgl_GraphicsProvider$Graphics$AbstractCanvas__V = (function($thiz, dt, canvas) {
  const this$1 = $thiz.Lcom_regblanc_scalavator_html5_Main$__f_MaxLoopStepDelta;
  const arg1 = this$1.s_Some__f_value;
  const t = $uJ(arg1);
  const lo = t.RTLong__f_lo;
  const hi = t.RTLong__f_hi;
  const ahi = dt.RTLong__f_hi;
  const this$6 = new $c_s_Some((((ahi === hi) ? (((-2147483648) ^ dt.RTLong__f_lo) < ((-2147483648) ^ lo)) : (ahi < hi)) ? dt : new $c_RTLong(lo, hi)));
  const t$1 = $uJ(this$6.s_Some__f_value);
  const lo$1 = t$1.RTLong__f_lo;
  const hi$1 = t$1.RTLong__f_hi;
  const this$7 = $as_Lsgl_GameStateComponent($thiz).Lcom_regblanc_scalavator_html5_Main$__f_gameState;
  const screensStack = this$7.Lsgl_GameStateComponent$GameState__f_screens;
  const this$8 = $as_Lsgl_GameStateComponent($thiz).Lcom_regblanc_scalavator_html5_Main$__f_gameState;
  const currentScreen = $as_Lsgl_GameStateComponent$GameScreen(this$8.Lsgl_GameStateComponent$GameState__f_screens.head__O());
  let $$x1;
  if (currentScreen.Lsgl_GameStateComponent$GameScreen__f__isLoading) {
    const this$9 = currentScreen.Lsgl_GameStateComponent$GameScreen__f_preloaders;
    _return: {
      let these = this$9;
      while ((!these.isEmpty__Z())) {
        const arg1$1 = these.head__O();
        const l = $as_Lsgl_util_Loader(arg1$1);
        if ((!l.isLoaded__Z())) {
          $$x1 = true;
          break _return
        };
        these = $as_sc_LinearSeqOptimized(these.tail__O())
      };
      $$x1 = false
    }
  } else {
    $$x1 = false
  };
  if ((!$$x1)) {
    if (currentScreen.Lsgl_GameStateComponent$GameScreen__f__isLoading) {
      currentScreen.Lsgl_GameStateComponent$GameScreen__f__isLoading = false
    };
    if (((hi$1 === 0) ? (lo$1 !== 0) : (hi$1 > 0))) {
      $as_Lsgl_InputProvider($thiz).Input__Lsgl_InputProvider$Input$().onLoopUpdate__V();
      currentScreen.update__J__V(new $c_RTLong(lo$1, hi$1))
    };
    const this$10 = $as_Lsgl_GameStateComponent($thiz).Lcom_regblanc_scalavator_html5_Main$__f_gameState;
    const x$2 = this$10.Lsgl_GameStateComponent$GameState__f_screens;
    if (((screensStack === null) ? (x$2 === null) : screensStack.equals__O__Z(x$2))) {
      const this$11 = $as_Lsgl_GameStateComponent($thiz).Lcom_regblanc_scalavator_html5_Main$__f_gameState;
      const this$12 = this$11.Lsgl_GameStateComponent$GameState__f_screens;
      const b = new $c_scm_ListBuffer();
      let these$1 = this$12;
      while (true) {
        let $$x2;
        if ((!these$1.isEmpty__Z())) {
          const arg1$2 = these$1.head__O();
          const x$1 = $as_Lsgl_GameStateComponent$GameScreen(arg1$2);
          $$x2 = (!x$1.Lsgl_GameStateComponent$GameScreen__f_isOpaque)
        } else {
          $$x2 = false
        };
        if ($$x2) {
          b.$plus$eq__O__scm_ListBuffer(these$1.head__O());
          const this$13 = these$1;
          these$1 = this$13.tail__sci_List()
        } else {
          break
        }
      };
      const transparentScreens = b.toList__sci_List();
      const this$14 = $as_Lsgl_GameStateComponent($thiz).Lcom_regblanc_scalavator_html5_Main$__f_gameState;
      const this$15 = this$14.Lsgl_GameStateComponent$GameState__f_screens;
      let lastOpaqueScreen;
      _return$1: {
        let these$2 = this$15;
        while ((!these$2.isEmpty__Z())) {
          const arg1$3 = these$2.head__O();
          const x$2$2 = $as_Lsgl_GameStateComponent$GameScreen(arg1$3);
          if (x$2$2.Lsgl_GameStateComponent$GameScreen__f_isOpaque) {
            lastOpaqueScreen = new $c_s_Some(these$2.head__O());
            break _return$1
          };
          these$2 = $as_sc_LinearSeqOptimized(these$2.tail__O())
        };
        lastOpaqueScreen = $m_s_None$()
      };
      const $$x4 = lastOpaqueScreen.toList__sci_List();
      const $$x3 = transparentScreens.reverse__sci_List();
      const this$17 = $m_sc_Iterable$();
      const renderedScreens = $as_sc_Iterable($$x4.$plus$plus__sc_GenTraversableOnce__scg_CanBuildFrom__O($$x3, this$17.scg_GenTraversableFactory__f_ReusableCBFInstance));
      renderedScreens.foreach__F1__V(new $c_sjsr_AnonFunction1(((this$6$1, canvas$1) => ((x$3$2) => {
        const x$3 = $as_Lsgl_GameStateComponent$GameScreen(x$3$2);
        x$3.render__Lsgl_GraphicsProvider$Graphics$AbstractCanvas__V(canvas$1)
      }))($thiz, canvas)))
    }
  }
});
const $f_Lsgl_GameLoopComponent__$init$__V = (function($thiz) {
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_TargetFps = new $c_s_Some(30);
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_gameLoopListener = $thiz.SilentGameLoopListener__Lsgl_GameLoopComponent$SilentGameLoopListener$()
});
const $ct_Lsgl_GameLoopComponent$GameLoopListener__Lsgl_GameLoopComponent__ = (function($thiz, outer) {
  if ((outer === null)) {
    throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
  } else {
    $thiz.Lsgl_GameLoopComponent$GameLoopListener__f_$outer = outer
  };
  return $thiz
});
class $c_Lsgl_GameLoopComponent$GameLoopListener extends $c_O {
  constructor() {
    super();
    this.Lsgl_GameLoopComponent$GameLoopListener__f_$outer = null
  };
}
const $f_Lsgl_GameStateComponent__$init$__V = (function($thiz) {
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_gameState = new $c_Lsgl_GameStateComponent$GameState($thiz)
});
function $is_Lsgl_GameStateComponent(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.Lsgl_GameStateComponent)))
}
function $as_Lsgl_GameStateComponent(obj) {
  return (($is_Lsgl_GameStateComponent(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.GameStateComponent"))
}
function $isArrayOf_Lsgl_GameStateComponent(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_GameStateComponent)))
}
function $asArrayOf_Lsgl_GameStateComponent(obj, depth) {
  return (($isArrayOf_Lsgl_GameStateComponent(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.GameStateComponent;", depth))
}
const $ct_Lsgl_GameStateComponent$GameScreen__Lsgl_GameStateComponent__ = (function($thiz, outer) {
  if ((outer === null)) {
    throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
  } else {
    $thiz.Lsgl_GameStateComponent$GameScreen__f_$outer = outer
  };
  $thiz.Lsgl_GameStateComponent$GameScreen__f_logTag = new $c_Lsgl_util_LoggingProvider$Logger$Tag($as_Lsgl_util_LoggingProvider(outer).Logger__Lsgl_util_LoggingProvider$Logger$(), "game-screen");
  $thiz.Lsgl_GameStateComponent$GameScreen__f_preloaders = $m_sci_Nil$();
  $thiz.Lsgl_GameStateComponent$GameScreen__f__isLoading = true;
  $thiz.Lsgl_GameStateComponent$GameScreen__f_isOpaque = false;
  return $thiz
});
class $c_Lsgl_GameStateComponent$GameScreen extends $c_O {
  constructor() {
    super();
    this.Lsgl_GameStateComponent$GameScreen__f_logTag = null;
    this.Lsgl_GameStateComponent$GameScreen__f_preloaders = null;
    this.Lsgl_GameStateComponent$GameScreen__f__isLoading = false;
    this.Lsgl_GameStateComponent$GameScreen__f_isOpaque = false;
    this.Lsgl_GameStateComponent$GameScreen__f_$outer = null
  };
}
function $as_Lsgl_GameStateComponent$GameScreen(obj) {
  return (((obj instanceof $c_Lsgl_GameStateComponent$GameScreen) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.GameStateComponent$GameScreen"))
}
function $isArrayOf_Lsgl_GameStateComponent$GameScreen(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_GameStateComponent$GameScreen)))
}
function $asArrayOf_Lsgl_GameStateComponent$GameScreen(obj, depth) {
  return (($isArrayOf_Lsgl_GameStateComponent$GameScreen(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.GameStateComponent$GameScreen;", depth))
}
class $c_Lsgl_GameStateComponent$GameState extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_GameStateComponent$GameState__f_screens = null;
    this.Lsgl_GameStateComponent$GameState__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_GameStateComponent$GameState__f_$outer = outer
    };
    this.Lsgl_GameStateComponent$GameState__f_screens = $m_sci_Nil$()
  };
  newScreen__Lsgl_GameStateComponent$GameScreen__V(screen) {
    const this$1 = this.Lsgl_GameStateComponent$GameState__f_screens;
    if ((!this$1.isEmpty__Z())) {
      $as_Lsgl_GameStateComponent$GameScreen(this.Lsgl_GameStateComponent$GameState__f_screens.head__O())
    };
    $m_sci_List$();
    const array = [screen];
    let i = (((-1) + $uI(array.length)) | 0);
    let result = $m_sci_Nil$();
    while ((i >= 0)) {
      const this$5 = result;
      const index = i;
      const x = array[index];
      result = new $c_sci_$colon$colon(x, this$5);
      i = (((-1) + i) | 0)
    };
    this.Lsgl_GameStateComponent$GameState__f_screens = result
  };
}
const $d_Lsgl_GameStateComponent$GameState = new $TypeData().initClass({
  Lsgl_GameStateComponent$GameState: 0
}, false, "sgl.GameStateComponent$GameState", {
  Lsgl_GameStateComponent$GameState: 1,
  O: 1
});
$c_Lsgl_GameStateComponent$GameState.prototype.$classData = $d_Lsgl_GameStateComponent$GameState;
const $ct_Lsgl_GraphicsProvider$Graphics$AbstractFont__Lsgl_GraphicsProvider$Graphics__ = (function($thiz, outer) {
  if ((outer === null)) {
    throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
  } else {
    $thiz.Lsgl_GraphicsProvider$Graphics$AbstractFont__f_$outer = outer
  };
  return $thiz
});
class $c_Lsgl_GraphicsProvider$Graphics$AbstractFont extends $c_O {
  constructor() {
    super();
    this.Lsgl_GraphicsProvider$Graphics$AbstractFont__f_$outer = null
  };
}
const $p_Lsgl_GraphicsProvider$Graphics$Alignments$__Center$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_GraphicsProvider$Graphics$Alignments$__f_Center$module === null)) {
    $thiz.Lsgl_GraphicsProvider$Graphics$Alignments$__f_Center$module = new $c_Lsgl_GraphicsProvider$Graphics$Alignments$Center$($thiz)
  }
});
const $p_Lsgl_GraphicsProvider$Graphics$Alignments$__Left$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_GraphicsProvider$Graphics$Alignments$__f_Left$module === null)) {
    $thiz.Lsgl_GraphicsProvider$Graphics$Alignments$__f_Left$module = new $c_Lsgl_GraphicsProvider$Graphics$Alignments$Left$($thiz)
  }
});
const $p_Lsgl_GraphicsProvider$Graphics$Alignments$__Right$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_GraphicsProvider$Graphics$Alignments$__f_Right$module === null)) {
    $thiz.Lsgl_GraphicsProvider$Graphics$Alignments$__f_Right$module = new $c_Lsgl_GraphicsProvider$Graphics$Alignments$Right$($thiz)
  }
});
class $c_Lsgl_GraphicsProvider$Graphics$Alignments$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_GraphicsProvider$Graphics$Alignments$__f_Center$module = null;
    this.Lsgl_GraphicsProvider$Graphics$Alignments$__f_Left$module = null;
    this.Lsgl_GraphicsProvider$Graphics$Alignments$__f_Right$module = null
  };
  Center__Lsgl_GraphicsProvider$Graphics$Alignments$Center$() {
    if ((this.Lsgl_GraphicsProvider$Graphics$Alignments$__f_Center$module === null)) {
      $p_Lsgl_GraphicsProvider$Graphics$Alignments$__Center$lzycompute$1__V(this)
    };
    return this.Lsgl_GraphicsProvider$Graphics$Alignments$__f_Center$module
  };
  Left__Lsgl_GraphicsProvider$Graphics$Alignments$Left$() {
    if ((this.Lsgl_GraphicsProvider$Graphics$Alignments$__f_Left$module === null)) {
      $p_Lsgl_GraphicsProvider$Graphics$Alignments$__Left$lzycompute$1__V(this)
    };
    return this.Lsgl_GraphicsProvider$Graphics$Alignments$__f_Left$module
  };
  Right__Lsgl_GraphicsProvider$Graphics$Alignments$Right$() {
    if ((this.Lsgl_GraphicsProvider$Graphics$Alignments$__f_Right$module === null)) {
      $p_Lsgl_GraphicsProvider$Graphics$Alignments$__Right$lzycompute$1__V(this)
    };
    return this.Lsgl_GraphicsProvider$Graphics$Alignments$__f_Right$module
  };
}
const $d_Lsgl_GraphicsProvider$Graphics$Alignments$ = new $TypeData().initClass({
  Lsgl_GraphicsProvider$Graphics$Alignments$: 0
}, false, "sgl.GraphicsProvider$Graphics$Alignments$", {
  Lsgl_GraphicsProvider$Graphics$Alignments$: 1,
  O: 1
});
$c_Lsgl_GraphicsProvider$Graphics$Alignments$.prototype.$classData = $d_Lsgl_GraphicsProvider$Graphics$Alignments$;
const $ct_Lsgl_GraphicsProvider$Graphics$ColorCompanion__Lsgl_GraphicsProvider$Graphics__ = (function($thiz, outer) {
  if ((outer === null)) {
    throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
  } else {
    $thiz.Lsgl_GraphicsProvider$Graphics$ColorCompanion__f_$outer = outer
  };
  return $thiz
});
class $c_Lsgl_GraphicsProvider$Graphics$ColorCompanion extends $c_O {
  constructor() {
    super();
    this.Lsgl_GraphicsProvider$Graphics$ColorCompanion__f_$outer = null
  };
}
const $p_Lsgl_GraphicsProvider$Graphics$FontCompanion__Bold$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_Bold$module === null)) {
    $thiz.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_Bold$module = new $c_Lsgl_GraphicsProvider$Graphics$FontCompanion$Bold$($thiz)
  }
});
const $p_Lsgl_GraphicsProvider$Graphics$FontCompanion__BoldItalic$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_BoldItalic$module === null)) {
    $thiz.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_BoldItalic$module = new $c_Lsgl_GraphicsProvider$Graphics$FontCompanion$BoldItalic$($thiz)
  }
});
const $p_Lsgl_GraphicsProvider$Graphics$FontCompanion__Italic$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_Italic$module === null)) {
    $thiz.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_Italic$module = new $c_Lsgl_GraphicsProvider$Graphics$FontCompanion$Italic$($thiz)
  }
});
const $p_Lsgl_GraphicsProvider$Graphics$FontCompanion__Normal$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_Normal$module === null)) {
    $thiz.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_Normal$module = new $c_Lsgl_GraphicsProvider$Graphics$FontCompanion$Normal$($thiz)
  }
});
const $ct_Lsgl_GraphicsProvider$Graphics$FontCompanion__Lsgl_GraphicsProvider$Graphics__ = (function($thiz, outer) {
  if ((outer === null)) {
    throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
  } else {
    $thiz.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_$outer = outer
  };
  return $thiz
});
class $c_Lsgl_GraphicsProvider$Graphics$FontCompanion extends $c_O {
  constructor() {
    super();
    this.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_Bold$module = null;
    this.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_BoldItalic$module = null;
    this.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_Italic$module = null;
    this.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_Normal$module = null;
    this.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_$outer = null
  };
  Bold__Lsgl_GraphicsProvider$Graphics$FontCompanion$Bold$() {
    if ((this.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_Bold$module === null)) {
      $p_Lsgl_GraphicsProvider$Graphics$FontCompanion__Bold$lzycompute$1__V(this)
    };
    return this.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_Bold$module
  };
  BoldItalic__Lsgl_GraphicsProvider$Graphics$FontCompanion$BoldItalic$() {
    if ((this.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_BoldItalic$module === null)) {
      $p_Lsgl_GraphicsProvider$Graphics$FontCompanion__BoldItalic$lzycompute$1__V(this)
    };
    return this.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_BoldItalic$module
  };
  Italic__Lsgl_GraphicsProvider$Graphics$FontCompanion$Italic$() {
    if ((this.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_Italic$module === null)) {
      $p_Lsgl_GraphicsProvider$Graphics$FontCompanion__Italic$lzycompute$1__V(this)
    };
    return this.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_Italic$module
  };
  Normal__Lsgl_GraphicsProvider$Graphics$FontCompanion$Normal$() {
    if ((this.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_Normal$module === null)) {
      $p_Lsgl_GraphicsProvider$Graphics$FontCompanion__Normal$lzycompute$1__V(this)
    };
    return this.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_Normal$module
  };
}
const $f_Lsgl_InputProvider__$init$__V = (function($thiz) {
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_sgl$InputProvider$$LogTag = new $c_Lsgl_util_LoggingProvider$Logger$Tag($as_Lsgl_util_LoggingProvider($thiz).Logger__Lsgl_util_LoggingProvider$Logger$(), "sgl.input")
});
function $is_Lsgl_InputProvider(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.Lsgl_InputProvider)))
}
function $as_Lsgl_InputProvider(obj) {
  return (($is_Lsgl_InputProvider(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.InputProvider"))
}
function $isArrayOf_Lsgl_InputProvider(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_InputProvider)))
}
function $asArrayOf_Lsgl_InputProvider(obj, depth) {
  return (($isArrayOf_Lsgl_InputProvider(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.InputProvider;", depth))
}
const $p_Lsgl_InputProvider$Input$__MouseButtons$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$__f_MouseButtons$module === null)) {
    $thiz.Lsgl_InputProvider$Input$__f_MouseButtons$module = new $c_Lsgl_InputProvider$Input$MouseButtons$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$__Keys$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$__f_Keys$module === null)) {
    $thiz.Lsgl_InputProvider$Input$__f_Keys$module = new $c_Lsgl_InputProvider$Input$Keys$($thiz)
  }
});
class $c_Lsgl_InputProvider$Input$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_InputProvider$Input$__f_KeyDownEvent$module = null;
    this.Lsgl_InputProvider$Input$__f_KeyUpEvent$module = null;
    this.Lsgl_InputProvider$Input$__f_MouseMovedEvent$module = null;
    this.Lsgl_InputProvider$Input$__f_MouseDownEvent$module = null;
    this.Lsgl_InputProvider$Input$__f_MouseUpEvent$module = null;
    this.Lsgl_InputProvider$Input$__f_MouseScrolledEvent$module = null;
    this.Lsgl_InputProvider$Input$__f_SystemActionEvent$module = null;
    this.Lsgl_InputProvider$Input$__f_TouchMovedEvent$module = null;
    this.Lsgl_InputProvider$Input$__f_TouchDownEvent$module = null;
    this.Lsgl_InputProvider$Input$__f_TouchUpEvent$module = null;
    this.Lsgl_InputProvider$Input$__f_PointerDownEvent$module = null;
    this.Lsgl_InputProvider$Input$__f_PointerUpEvent$module = null;
    this.Lsgl_InputProvider$Input$__f_PointerMovedEvent$module = null;
    this.Lsgl_InputProvider$Input$__f_MouseButtons$module = null;
    this.Lsgl_InputProvider$Input$__f_Keys$module = null;
    this.Lsgl_InputProvider$Input$__f_Actions$module = null;
    this.Lsgl_InputProvider$Input$__f_eventProcessor = null;
    this.Lsgl_InputProvider$Input$__f_eventQueue = null;
    this.Lsgl_InputProvider$Input$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$__f_$outer = outer
    };
    this.Lsgl_InputProvider$Input$__f_eventProcessor = $m_s_None$();
    this.Lsgl_InputProvider$Input$__f_eventQueue = $ct_scm_Queue__(new $c_scm_Queue())
  };
  MouseButtons__Lsgl_InputProvider$Input$MouseButtons$() {
    if ((this.Lsgl_InputProvider$Input$__f_MouseButtons$module === null)) {
      $p_Lsgl_InputProvider$Input$__MouseButtons$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$__f_MouseButtons$module
  };
  Keys__Lsgl_InputProvider$Input$Keys$() {
    if ((this.Lsgl_InputProvider$Input$__f_Keys$module === null)) {
      $p_Lsgl_InputProvider$Input$__Keys$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$__f_Keys$module
  };
  setEventProcessor__F1__V(processor) {
    this.Lsgl_InputProvider$Input$__f_eventProcessor = new $c_s_Some(processor)
  };
  newEvent__Lsgl_InputProvider$Input$InputEvent__V(event) {
    if ((this.Lsgl_InputProvider$Input$__f_$outer.Lcom_regblanc_scalavator_html5_Main$__f_ProcessInputsDuringUpdate || this.Lsgl_InputProvider$Input$__f_eventProcessor.isEmpty__Z())) {
      const this$1 = $as_Lsgl_util_LoggingProvider(this.Lsgl_InputProvider$Input$__f_$outer);
      const this$2 = this$1.Lcom_regblanc_scalavator_html5_Main$__f_logger;
      const tag = this.Lsgl_InputProvider$Input$__f_$outer.Lcom_regblanc_scalavator_html5_Main$__f_sgl$InputProvider$$LogTag;
      const this$3 = this$2.Lsgl_html5_util_Html5VerboseConsoleLoggingProvider$VerboseConsoleLogger$__f_logLevel;
      const that = this$2.Lsgl_util_LoggingProvider$Logger__f_$outer.Logger__Lsgl_util_LoggingProvider$Logger$().Trace__Lsgl_util_LoggingProvider$Logger$Trace$();
      if ($f_s_math_Ordered__$greater$eq__O__Z(this$3, that)) {
        this$2.log__Lsgl_util_LoggingProvider$Logger$LogLevel__Lsgl_util_LoggingProvider$Logger$Tag__T__V(this$2.Lsgl_util_LoggingProvider$Logger__f_$outer.Logger__Lsgl_util_LoggingProvider$Logger$().Trace__Lsgl_util_LoggingProvider$Logger$Trace$(), tag, (("Adding new event: " + event) + " to the queue."))
      };
      const this$6 = this.Lsgl_InputProvider$Input$__f_eventQueue;
      const array = [event];
      let i = 0;
      const len = $uI(array.length);
      while ((i < len)) {
        const index = i;
        const arg1 = array[index];
        this$6.$plus$eq__O__scm_MutableList(arg1);
        i = ((1 + i) | 0)
      }
    } else {
      const this$7 = $as_Lsgl_util_LoggingProvider(this.Lsgl_InputProvider$Input$__f_$outer);
      const this$8 = this$7.Lcom_regblanc_scalavator_html5_Main$__f_logger;
      const tag$1 = this.Lsgl_InputProvider$Input$__f_$outer.Lcom_regblanc_scalavator_html5_Main$__f_sgl$InputProvider$$LogTag;
      const this$9 = this$8.Lsgl_html5_util_Html5VerboseConsoleLoggingProvider$VerboseConsoleLogger$__f_logLevel;
      const that$1 = this$8.Lsgl_util_LoggingProvider$Logger__f_$outer.Logger__Lsgl_util_LoggingProvider$Logger$().Trace__Lsgl_util_LoggingProvider$Logger$Trace$();
      if ($f_s_math_Ordered__$greater$eq__O__Z(this$9, that$1)) {
        this$8.log__Lsgl_util_LoggingProvider$Logger$LogLevel__Lsgl_util_LoggingProvider$Logger$Tag__T__V(this$8.Lsgl_util_LoggingProvider$Logger__f_$outer.Logger__Lsgl_util_LoggingProvider$Logger$().Trace__Lsgl_util_LoggingProvider$Logger$Trace$(), tag$1, "executing event right away")
      };
      $as_F1(this.Lsgl_InputProvider$Input$__f_eventProcessor.get__O()).apply__O__O(event)
    }
  };
  onLoopUpdate__V() {
    if (this.Lsgl_InputProvider$Input$__f_$outer.Lcom_regblanc_scalavator_html5_Main$__f_ProcessInputsDuringUpdate) {
      const this$1 = this.Lsgl_InputProvider$Input$__f_eventProcessor;
      if ((!this$1.isEmpty__Z())) {
        const arg1 = this$1.get__O();
        const p = $as_F1(arg1);
        while (true) {
          const this$2 = this.Lsgl_InputProvider$Input$__f_$outer.Input__Lsgl_InputProvider$Input$().Lsgl_InputProvider$Input$__f_eventQueue;
          if ((!this$2.isEmpty__Z())) {
            p.apply__O__O(this.Lsgl_InputProvider$Input$__f_$outer.Input__Lsgl_InputProvider$Input$().Lsgl_InputProvider$Input$__f_eventQueue.dequeue__O())
          } else {
            break
          }
        }
      }
    }
  };
}
const $d_Lsgl_InputProvider$Input$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$: 0
}, false, "sgl.InputProvider$Input$", {
  Lsgl_InputProvider$Input$: 1,
  O: 1
});
$c_Lsgl_InputProvider$Input$.prototype.$classData = $d_Lsgl_InputProvider$Input$;
function $is_Lsgl_InputProvider$Input$InputEvent(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.Lsgl_InputProvider$Input$InputEvent)))
}
function $as_Lsgl_InputProvider$Input$InputEvent(obj) {
  return (($is_Lsgl_InputProvider$Input$InputEvent(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.InputProvider$Input$InputEvent"))
}
function $isArrayOf_Lsgl_InputProvider$Input$InputEvent(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_InputProvider$Input$InputEvent)))
}
function $asArrayOf_Lsgl_InputProvider$Input$InputEvent(obj, depth) {
  return (($isArrayOf_Lsgl_InputProvider$Input$InputEvent(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.InputProvider$Input$InputEvent;", depth))
}
const $p_Lsgl_InputProvider$Input$Keys$__A$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_A$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_A$module = new $c_Lsgl_InputProvider$Input$Keys$A$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__B$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_B$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_B$module = new $c_Lsgl_InputProvider$Input$Keys$B$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__C$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_C$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_C$module = new $c_Lsgl_InputProvider$Input$Keys$C$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__D$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_D$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_D$module = new $c_Lsgl_InputProvider$Input$Keys$D$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__E$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_E$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_E$module = new $c_Lsgl_InputProvider$Input$Keys$E$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__F$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_F$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_F$module = new $c_Lsgl_InputProvider$Input$Keys$F$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__G$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_G$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_G$module = new $c_Lsgl_InputProvider$Input$Keys$G$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__H$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_H$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_H$module = new $c_Lsgl_InputProvider$Input$Keys$H$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__I$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_I$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_I$module = new $c_Lsgl_InputProvider$Input$Keys$I$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__J$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_J$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_J$module = new $c_Lsgl_InputProvider$Input$Keys$J$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__K$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_K$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_K$module = new $c_Lsgl_InputProvider$Input$Keys$K$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__L$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_L$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_L$module = new $c_Lsgl_InputProvider$Input$Keys$L$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__M$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_M$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_M$module = new $c_Lsgl_InputProvider$Input$Keys$M$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__N$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_N$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_N$module = new $c_Lsgl_InputProvider$Input$Keys$N$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__O$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_O$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_O$module = new $c_Lsgl_InputProvider$Input$Keys$O$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__P$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_P$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_P$module = new $c_Lsgl_InputProvider$Input$Keys$P$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__Q$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_Q$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_Q$module = new $c_Lsgl_InputProvider$Input$Keys$Q$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__R$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_R$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_R$module = new $c_Lsgl_InputProvider$Input$Keys$R$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__S$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_S$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_S$module = new $c_Lsgl_InputProvider$Input$Keys$S$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__T$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_T$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_T$module = new $c_Lsgl_InputProvider$Input$Keys$T$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__U$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_U$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_U$module = new $c_Lsgl_InputProvider$Input$Keys$U$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__V$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_V$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_V$module = new $c_Lsgl_InputProvider$Input$Keys$V$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__W$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_W$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_W$module = new $c_Lsgl_InputProvider$Input$Keys$W$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__X$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_X$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_X$module = new $c_Lsgl_InputProvider$Input$Keys$X$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__Y$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_Y$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_Y$module = new $c_Lsgl_InputProvider$Input$Keys$Y$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__Z$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_Z$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_Z$module = new $c_Lsgl_InputProvider$Input$Keys$Z$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__Num0$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_Num0$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_Num0$module = new $c_Lsgl_InputProvider$Input$Keys$Num0$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__Num1$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_Num1$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_Num1$module = new $c_Lsgl_InputProvider$Input$Keys$Num1$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__Num2$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_Num2$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_Num2$module = new $c_Lsgl_InputProvider$Input$Keys$Num2$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__Num3$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_Num3$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_Num3$module = new $c_Lsgl_InputProvider$Input$Keys$Num3$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__Num4$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_Num4$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_Num4$module = new $c_Lsgl_InputProvider$Input$Keys$Num4$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__Num5$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_Num5$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_Num5$module = new $c_Lsgl_InputProvider$Input$Keys$Num5$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__Num6$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_Num6$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_Num6$module = new $c_Lsgl_InputProvider$Input$Keys$Num6$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__Num7$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_Num7$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_Num7$module = new $c_Lsgl_InputProvider$Input$Keys$Num7$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__Num8$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_Num8$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_Num8$module = new $c_Lsgl_InputProvider$Input$Keys$Num8$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__Num9$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_Num9$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_Num9$module = new $c_Lsgl_InputProvider$Input$Keys$Num9$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__Space$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_Space$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_Space$module = new $c_Lsgl_InputProvider$Input$Keys$Space$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__Left$lzycompute$2__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_Left$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_Left$module = new $c_Lsgl_InputProvider$Input$Keys$Left$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__Up$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_Up$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_Up$module = new $c_Lsgl_InputProvider$Input$Keys$Up$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__Right$lzycompute$2__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_Right$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_Right$module = new $c_Lsgl_InputProvider$Input$Keys$Right$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$Keys$__Down$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$Keys$__f_Down$module === null)) {
    $thiz.Lsgl_InputProvider$Input$Keys$__f_Down$module = new $c_Lsgl_InputProvider$Input$Keys$Down$($thiz)
  }
});
class $c_Lsgl_InputProvider$Input$Keys$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_InputProvider$Input$Keys$__f_A$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_B$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_C$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_D$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_E$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_F$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_G$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_H$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_I$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_J$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_K$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_L$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_M$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_N$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_O$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_P$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_Q$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_R$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_S$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_T$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_U$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_V$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_W$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_X$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_Y$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_Z$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_Num0$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_Num1$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_Num2$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_Num3$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_Num4$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_Num5$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_Num6$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_Num7$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_Num8$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_Num9$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_Space$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_Left$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_Up$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_Right$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_Down$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_ButtonStart$module = null;
    this.Lsgl_InputProvider$Input$Keys$__f_ButtonSelect$module = null
  };
  A__Lsgl_InputProvider$Input$Keys$A$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_A$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__A$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_A$module
  };
  B__Lsgl_InputProvider$Input$Keys$B$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_B$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__B$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_B$module
  };
  C__Lsgl_InputProvider$Input$Keys$C$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_C$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__C$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_C$module
  };
  D__Lsgl_InputProvider$Input$Keys$D$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_D$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__D$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_D$module
  };
  E__Lsgl_InputProvider$Input$Keys$E$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_E$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__E$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_E$module
  };
  F__Lsgl_InputProvider$Input$Keys$F$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_F$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__F$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_F$module
  };
  G__Lsgl_InputProvider$Input$Keys$G$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_G$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__G$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_G$module
  };
  H__Lsgl_InputProvider$Input$Keys$H$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_H$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__H$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_H$module
  };
  I__Lsgl_InputProvider$Input$Keys$I$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_I$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__I$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_I$module
  };
  J__Lsgl_InputProvider$Input$Keys$J$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_J$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__J$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_J$module
  };
  K__Lsgl_InputProvider$Input$Keys$K$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_K$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__K$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_K$module
  };
  L__Lsgl_InputProvider$Input$Keys$L$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_L$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__L$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_L$module
  };
  M__Lsgl_InputProvider$Input$Keys$M$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_M$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__M$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_M$module
  };
  N__Lsgl_InputProvider$Input$Keys$N$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_N$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__N$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_N$module
  };
  O__Lsgl_InputProvider$Input$Keys$O$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_O$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__O$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_O$module
  };
  P__Lsgl_InputProvider$Input$Keys$P$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_P$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__P$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_P$module
  };
  Q__Lsgl_InputProvider$Input$Keys$Q$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_Q$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__Q$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_Q$module
  };
  R__Lsgl_InputProvider$Input$Keys$R$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_R$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__R$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_R$module
  };
  S__Lsgl_InputProvider$Input$Keys$S$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_S$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__S$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_S$module
  };
  T__Lsgl_InputProvider$Input$Keys$T$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_T$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__T$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_T$module
  };
  U__Lsgl_InputProvider$Input$Keys$U$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_U$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__U$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_U$module
  };
  V__Lsgl_InputProvider$Input$Keys$V$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_V$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__V$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_V$module
  };
  W__Lsgl_InputProvider$Input$Keys$W$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_W$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__W$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_W$module
  };
  X__Lsgl_InputProvider$Input$Keys$X$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_X$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__X$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_X$module
  };
  Y__Lsgl_InputProvider$Input$Keys$Y$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_Y$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__Y$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_Y$module
  };
  Z__Lsgl_InputProvider$Input$Keys$Z$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_Z$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__Z$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_Z$module
  };
  Num0__Lsgl_InputProvider$Input$Keys$Num0$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_Num0$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__Num0$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_Num0$module
  };
  Num1__Lsgl_InputProvider$Input$Keys$Num1$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_Num1$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__Num1$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_Num1$module
  };
  Num2__Lsgl_InputProvider$Input$Keys$Num2$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_Num2$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__Num2$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_Num2$module
  };
  Num3__Lsgl_InputProvider$Input$Keys$Num3$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_Num3$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__Num3$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_Num3$module
  };
  Num4__Lsgl_InputProvider$Input$Keys$Num4$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_Num4$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__Num4$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_Num4$module
  };
  Num5__Lsgl_InputProvider$Input$Keys$Num5$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_Num5$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__Num5$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_Num5$module
  };
  Num6__Lsgl_InputProvider$Input$Keys$Num6$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_Num6$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__Num6$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_Num6$module
  };
  Num7__Lsgl_InputProvider$Input$Keys$Num7$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_Num7$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__Num7$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_Num7$module
  };
  Num8__Lsgl_InputProvider$Input$Keys$Num8$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_Num8$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__Num8$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_Num8$module
  };
  Num9__Lsgl_InputProvider$Input$Keys$Num9$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_Num9$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__Num9$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_Num9$module
  };
  Space__Lsgl_InputProvider$Input$Keys$Space$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_Space$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__Space$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_Space$module
  };
  Left__Lsgl_InputProvider$Input$Keys$Left$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_Left$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__Left$lzycompute$2__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_Left$module
  };
  Up__Lsgl_InputProvider$Input$Keys$Up$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_Up$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__Up$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_Up$module
  };
  Right__Lsgl_InputProvider$Input$Keys$Right$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_Right$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__Right$lzycompute$2__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_Right$module
  };
  Down__Lsgl_InputProvider$Input$Keys$Down$() {
    if ((this.Lsgl_InputProvider$Input$Keys$__f_Down$module === null)) {
      $p_Lsgl_InputProvider$Input$Keys$__Down$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$Keys$__f_Down$module
  };
}
const $d_Lsgl_InputProvider$Input$Keys$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$: 0
}, false, "sgl.InputProvider$Input$Keys$", {
  Lsgl_InputProvider$Input$Keys$: 1,
  O: 1
});
$c_Lsgl_InputProvider$Input$Keys$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$;
function $is_Lsgl_InputProvider$Input$Keys$Key(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.Lsgl_InputProvider$Input$Keys$Key)))
}
function $as_Lsgl_InputProvider$Input$Keys$Key(obj) {
  return (($is_Lsgl_InputProvider$Input$Keys$Key(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.InputProvider$Input$Keys$Key"))
}
function $isArrayOf_Lsgl_InputProvider$Input$Keys$Key(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_InputProvider$Input$Keys$Key)))
}
function $asArrayOf_Lsgl_InputProvider$Input$Keys$Key(obj, depth) {
  return (($isArrayOf_Lsgl_InputProvider$Input$Keys$Key(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.InputProvider$Input$Keys$Key;", depth))
}
const $p_Lsgl_InputProvider$Input$MouseButtons$__Left$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$MouseButtons$__f_Left$module === null)) {
    $thiz.Lsgl_InputProvider$Input$MouseButtons$__f_Left$module = new $c_Lsgl_InputProvider$Input$MouseButtons$Left$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$MouseButtons$__Right$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$MouseButtons$__f_Right$module === null)) {
    $thiz.Lsgl_InputProvider$Input$MouseButtons$__f_Right$module = new $c_Lsgl_InputProvider$Input$MouseButtons$Right$($thiz)
  }
});
const $p_Lsgl_InputProvider$Input$MouseButtons$__Middle$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_InputProvider$Input$MouseButtons$__f_Middle$module === null)) {
    $thiz.Lsgl_InputProvider$Input$MouseButtons$__f_Middle$module = new $c_Lsgl_InputProvider$Input$MouseButtons$Middle$($thiz)
  }
});
class $c_Lsgl_InputProvider$Input$MouseButtons$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_InputProvider$Input$MouseButtons$__f_Left$module = null;
    this.Lsgl_InputProvider$Input$MouseButtons$__f_Right$module = null;
    this.Lsgl_InputProvider$Input$MouseButtons$__f_Middle$module = null
  };
  Left__Lsgl_InputProvider$Input$MouseButtons$Left$() {
    if ((this.Lsgl_InputProvider$Input$MouseButtons$__f_Left$module === null)) {
      $p_Lsgl_InputProvider$Input$MouseButtons$__Left$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$MouseButtons$__f_Left$module
  };
  Right__Lsgl_InputProvider$Input$MouseButtons$Right$() {
    if ((this.Lsgl_InputProvider$Input$MouseButtons$__f_Right$module === null)) {
      $p_Lsgl_InputProvider$Input$MouseButtons$__Right$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$MouseButtons$__f_Right$module
  };
  Middle__Lsgl_InputProvider$Input$MouseButtons$Middle$() {
    if ((this.Lsgl_InputProvider$Input$MouseButtons$__f_Middle$module === null)) {
      $p_Lsgl_InputProvider$Input$MouseButtons$__Middle$lzycompute$1__V(this)
    };
    return this.Lsgl_InputProvider$Input$MouseButtons$__f_Middle$module
  };
}
const $d_Lsgl_InputProvider$Input$MouseButtons$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$MouseButtons$: 0
}, false, "sgl.InputProvider$Input$MouseButtons$", {
  Lsgl_InputProvider$Input$MouseButtons$: 1,
  O: 1
});
$c_Lsgl_InputProvider$Input$MouseButtons$.prototype.$classData = $d_Lsgl_InputProvider$Input$MouseButtons$;
function $is_Lsgl_SystemProvider(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.Lsgl_SystemProvider)))
}
function $as_Lsgl_SystemProvider(obj) {
  return (($is_Lsgl_SystemProvider(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.SystemProvider"))
}
function $isArrayOf_Lsgl_SystemProvider(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_SystemProvider)))
}
function $asArrayOf_Lsgl_SystemProvider(obj, depth) {
  return (($isArrayOf_Lsgl_SystemProvider(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.SystemProvider;", depth))
}
function $is_Lsgl_WindowProvider(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.Lsgl_WindowProvider)))
}
function $as_Lsgl_WindowProvider(obj) {
  return (($is_Lsgl_WindowProvider(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.WindowProvider"))
}
function $isArrayOf_Lsgl_WindowProvider(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_WindowProvider)))
}
function $asArrayOf_Lsgl_WindowProvider(obj, depth) {
  return (($isArrayOf_Lsgl_WindowProvider(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.WindowProvider;", depth))
}
const $ct_Lsgl_WindowProvider$AbstractWindow__Lsgl_WindowProvider__ = (function($thiz, outer) {
  if ((outer === null)) {
    throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
  } else {
    $thiz.Lsgl_WindowProvider$AbstractWindow__f_$outer = outer
  };
  return $thiz
});
class $c_Lsgl_WindowProvider$AbstractWindow extends $c_O {
  constructor() {
    super();
    this.Lsgl_WindowProvider$AbstractWindow__f_$outer = null
  };
}
class $c_Lsgl_html5_themes_Theme extends $c_O {
}
class $c_Lsgl_util_ChunkedTask {
}
function $as_Lsgl_util_ChunkedTask(obj) {
  return (((obj instanceof $c_Lsgl_util_ChunkedTask) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.util.ChunkedTask"))
}
function $isArrayOf_Lsgl_util_ChunkedTask(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_util_ChunkedTask)))
}
function $asArrayOf_Lsgl_util_ChunkedTask(obj, depth) {
  return (($isArrayOf_Lsgl_util_ChunkedTask(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.util.ChunkedTask;", depth))
}
class $c_Lsgl_util_ChunkedTask$Status extends $c_O {
}
function $is_Lsgl_util_Loader(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.Lsgl_util_Loader)))
}
function $as_Lsgl_util_Loader(obj) {
  return (($is_Lsgl_util_Loader(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.util.Loader"))
}
function $isArrayOf_Lsgl_util_Loader(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_util_Loader)))
}
function $asArrayOf_Lsgl_util_Loader(obj, depth) {
  return (($isArrayOf_Lsgl_util_Loader(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.util.Loader;", depth))
}
function $is_Lsgl_util_LoggingProvider(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.Lsgl_util_LoggingProvider)))
}
function $as_Lsgl_util_LoggingProvider(obj) {
  return (($is_Lsgl_util_LoggingProvider(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.util.LoggingProvider"))
}
function $isArrayOf_Lsgl_util_LoggingProvider(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_util_LoggingProvider)))
}
function $asArrayOf_Lsgl_util_LoggingProvider(obj, depth) {
  return (($isArrayOf_Lsgl_util_LoggingProvider(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.util.LoggingProvider;", depth))
}
const $ct_Lsgl_util_LoggingProvider$Logger__Lsgl_util_LoggingProvider__ = (function($thiz, outer) {
  if ((outer === null)) {
    throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
  } else {
    $thiz.Lsgl_util_LoggingProvider$Logger__f_$outer = outer
  };
  return $thiz
});
class $c_Lsgl_util_LoggingProvider$Logger extends $c_O {
  constructor() {
    super();
    this.Lsgl_util_LoggingProvider$Logger__f_$outer = null
  };
}
const $p_Lsgl_util_LoggingProvider$Logger$__NoLogging$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_util_LoggingProvider$Logger$__f_NoLogging$module === null)) {
    $thiz.Lsgl_util_LoggingProvider$Logger$__f_NoLogging$module = new $c_Lsgl_util_LoggingProvider$Logger$NoLogging$($thiz)
  }
});
const $p_Lsgl_util_LoggingProvider$Logger$__Error$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_util_LoggingProvider$Logger$__f_Error$module === null)) {
    $thiz.Lsgl_util_LoggingProvider$Logger$__f_Error$module = new $c_Lsgl_util_LoggingProvider$Logger$Error$($thiz)
  }
});
const $p_Lsgl_util_LoggingProvider$Logger$__Warning$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_util_LoggingProvider$Logger$__f_Warning$module === null)) {
    $thiz.Lsgl_util_LoggingProvider$Logger$__f_Warning$module = new $c_Lsgl_util_LoggingProvider$Logger$Warning$($thiz)
  }
});
const $p_Lsgl_util_LoggingProvider$Logger$__Info$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_util_LoggingProvider$Logger$__f_Info$module === null)) {
    $thiz.Lsgl_util_LoggingProvider$Logger$__f_Info$module = new $c_Lsgl_util_LoggingProvider$Logger$Info$($thiz)
  }
});
const $p_Lsgl_util_LoggingProvider$Logger$__Debug$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_util_LoggingProvider$Logger$__f_Debug$module === null)) {
    $thiz.Lsgl_util_LoggingProvider$Logger$__f_Debug$module = new $c_Lsgl_util_LoggingProvider$Logger$Debug$($thiz)
  }
});
const $p_Lsgl_util_LoggingProvider$Logger$__Trace$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_util_LoggingProvider$Logger$__f_Trace$module === null)) {
    $thiz.Lsgl_util_LoggingProvider$Logger$__f_Trace$module = new $c_Lsgl_util_LoggingProvider$Logger$Trace$($thiz)
  }
});
class $c_Lsgl_util_LoggingProvider$Logger$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_util_LoggingProvider$Logger$__f_NoLogging$module = null;
    this.Lsgl_util_LoggingProvider$Logger$__f_Error$module = null;
    this.Lsgl_util_LoggingProvider$Logger$__f_Warning$module = null;
    this.Lsgl_util_LoggingProvider$Logger$__f_Info$module = null;
    this.Lsgl_util_LoggingProvider$Logger$__f_Debug$module = null;
    this.Lsgl_util_LoggingProvider$Logger$__f_Trace$module = null;
    this.Lsgl_util_LoggingProvider$Logger$__f_Tag$module = null
  };
  NoLogging__Lsgl_util_LoggingProvider$Logger$NoLogging$() {
    if ((this.Lsgl_util_LoggingProvider$Logger$__f_NoLogging$module === null)) {
      $p_Lsgl_util_LoggingProvider$Logger$__NoLogging$lzycompute$1__V(this)
    };
    return this.Lsgl_util_LoggingProvider$Logger$__f_NoLogging$module
  };
  Error__Lsgl_util_LoggingProvider$Logger$Error$() {
    if ((this.Lsgl_util_LoggingProvider$Logger$__f_Error$module === null)) {
      $p_Lsgl_util_LoggingProvider$Logger$__Error$lzycompute$1__V(this)
    };
    return this.Lsgl_util_LoggingProvider$Logger$__f_Error$module
  };
  Warning__Lsgl_util_LoggingProvider$Logger$Warning$() {
    if ((this.Lsgl_util_LoggingProvider$Logger$__f_Warning$module === null)) {
      $p_Lsgl_util_LoggingProvider$Logger$__Warning$lzycompute$1__V(this)
    };
    return this.Lsgl_util_LoggingProvider$Logger$__f_Warning$module
  };
  Info__Lsgl_util_LoggingProvider$Logger$Info$() {
    if ((this.Lsgl_util_LoggingProvider$Logger$__f_Info$module === null)) {
      $p_Lsgl_util_LoggingProvider$Logger$__Info$lzycompute$1__V(this)
    };
    return this.Lsgl_util_LoggingProvider$Logger$__f_Info$module
  };
  Debug__Lsgl_util_LoggingProvider$Logger$Debug$() {
    if ((this.Lsgl_util_LoggingProvider$Logger$__f_Debug$module === null)) {
      $p_Lsgl_util_LoggingProvider$Logger$__Debug$lzycompute$1__V(this)
    };
    return this.Lsgl_util_LoggingProvider$Logger$__f_Debug$module
  };
  Trace__Lsgl_util_LoggingProvider$Logger$Trace$() {
    if ((this.Lsgl_util_LoggingProvider$Logger$__f_Trace$module === null)) {
      $p_Lsgl_util_LoggingProvider$Logger$__Trace$lzycompute$1__V(this)
    };
    return this.Lsgl_util_LoggingProvider$Logger$__f_Trace$module
  };
}
const $d_Lsgl_util_LoggingProvider$Logger$ = new $TypeData().initClass({
  Lsgl_util_LoggingProvider$Logger$: 0
}, false, "sgl.util.LoggingProvider$Logger$", {
  Lsgl_util_LoggingProvider$Logger$: 1,
  O: 1
});
$c_Lsgl_util_LoggingProvider$Logger$.prototype.$classData = $d_Lsgl_util_LoggingProvider$Logger$;
const $ct_Lsgl_util_SchedulerProvider$Scheduler__Lsgl_util_SchedulerProvider__ = (function($thiz, outer) {
  if ((outer === null)) {
    throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
  } else {
    $thiz.Lsgl_util_SchedulerProvider$Scheduler__f_$outer = outer
  };
  return $thiz
});
class $c_Lsgl_util_SchedulerProvider$Scheduler extends $c_O {
  constructor() {
    super();
    this.Lsgl_util_SchedulerProvider$Scheduler__f_$outer = null
  };
}
class $c_Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen extends $c_Lsgl_GameStateComponent$GameScreen {
  constructor(outer) {
    super();
    this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_name = null;
    this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterWidth = 0;
    this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterHeight = 0;
    this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterPosition = null;
    this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterVelocity = null;
    this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_Gravity = null;
    this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_$outer = outer
    };
    $ct_Lsgl_GameStateComponent$GameScreen__Lsgl_GameStateComponent__(this, $as_Lsgl_GameStateComponent(outer));
    this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_name = "main-screen";
    this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterWidth = 32;
    this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterHeight = 64;
    const this$1 = $as_Lsgl_WindowProvider(outer);
    const $$x2 = this$1.Lcom_regblanc_scalavator_html5_Main$__f_Window.width__I();
    const $$x1 = this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterWidth;
    const this$2 = $as_Lsgl_WindowProvider(outer);
    this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterPosition = new $c_Lsgl_geometry_Point($fround((((($$x2 / 2) | 0) - (($$x1 / 2) | 0)) | 0)), $fround(this$2.Lcom_regblanc_scalavator_html5_Main$__f_Window.height__I()));
    this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterVelocity = new $c_Lsgl_geometry_Vec(0.0, 0.0);
    this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_Gravity = new $c_Lsgl_geometry_Vec(0.0, 430.0);
    $as_Lsgl_InputProvider(outer).Input__Lsgl_InputProvider$Input$().setEventProcessor__F1__V(new $c_sjsr_AnonFunction1(((this$3) => ((ev$2) => {
      const ev = $as_Lsgl_InputProvider$Input$InputEvent(ev$2);
      this$3.handleInput__Lsgl_InputProvider$Input$InputEvent__V(ev)
    }))(this)))
  };
  handleInput__Lsgl_InputProvider$Input$InputEvent__V(ev) {
    let $$x1;
    matchEnd15: {
      if ((ev instanceof $c_Lsgl_InputProvider$Input$TouchUpEvent)) {
        $$x1 = true;
        break matchEnd15
      };
      if ((ev instanceof $c_Lsgl_InputProvider$Input$MouseUpEvent)) {
        const x6 = $as_Lsgl_InputProvider$Input$MouseUpEvent(ev);
        const p9 = x6.Lsgl_InputProvider$Input$MouseUpEvent__f_mouseButton;
        const x = $as_Lsgl_InputProvider(this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_$outer).Input__Lsgl_InputProvider$Input$().MouseButtons__Lsgl_InputProvider$Input$MouseButtons$().Left__Lsgl_InputProvider$Input$MouseButtons$Left$();
        if ((x === p9)) {
          $$x1 = true;
          break matchEnd15
        }
      };
      $$x1 = false
    };
    if ($$x1) {
      this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterVelocity = new $c_Lsgl_geometry_Vec(0.0, (-320.0))
    }
  };
  update__J__V(dt) {
    this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterPosition = this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterPosition.$plus__Lsgl_geometry_Vec__Lsgl_geometry_Point(this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterVelocity.$times__F__Lsgl_geometry_Vec($fround(($fround($m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$toDouble__I__I__D(dt.RTLong__f_lo, dt.RTLong__f_hi)) / 1000.0))));
    this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterVelocity = this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterVelocity.$plus__Lsgl_geometry_Vec__Lsgl_geometry_Vec(this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_Gravity.$times__F__Lsgl_geometry_Vec($fround(($fround($m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$toDouble__I__I__D(dt.RTLong__f_lo, dt.RTLong__f_hi)) / 1000.0))));
    const $$x1 = this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterPosition.Lsgl_geometry_Point__f_y;
    const this$1 = $as_Lsgl_WindowProvider(this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_$outer);
    if (((($doubleToInt($$x1) - this$1.Lcom_regblanc_scalavator_html5_Main$__f_Window.height__I()) | 0) > 0)) {
      this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterVelocity = new $c_Lsgl_geometry_Vec(0.0, 0.0)
    }
  };
  render__Lsgl_GraphicsProvider$Graphics$AbstractCanvas__V(canvas) {
    const this$1 = $as_Lsgl_GraphicsProvider(this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_$outer);
    const this$2 = this$1.Lcom_regblanc_scalavator_html5_Main$__f_Graphics;
    const this$3 = this$2.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Color;
    const color = this$3.rgb__I__I__I__T(255, 255, 255);
    canvas.drawColor__T__V(color);
    const x = this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterPosition.Lsgl_geometry_Point__f_x;
    const y = $fround((this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterPosition.Lsgl_geometry_Point__f_y - $fround(this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterHeight)));
    const width = $fround(this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterWidth);
    const height = $fround(this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_characterHeight);
    const this$4 = $as_Lsgl_GraphicsProvider(this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_$outer);
    const this$5 = this$4.Lcom_regblanc_scalavator_html5_Main$__f_Graphics;
    const this$9 = this$5.defaultPaint__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint();
    const this$6 = $as_Lsgl_GraphicsProvider(this.Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen__f_$outer);
    const this$7 = this$6.Lcom_regblanc_scalavator_html5_Main$__f_Graphics;
    const this$8 = this$7.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Color;
    const color$1 = this$8.rgb__I__I__I__T(0, 255, 0);
    const paint = this$9.withColor__T__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint(color$1);
    canvas.drawRect__F__F__F__F__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__V(x, y, width, height, paint)
  };
}
const $d_Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen = new $TypeData().initClass({
  Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen: 0
}, false, "com.regblanc.scalavator.core.ScalavatorGame$MainScreen", {
  Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen: 1,
  Lsgl_GameStateComponent$GameScreen: 1,
  O: 1
});
$c_Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen.prototype.$classData = $d_Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen;
const $p_jl_Character$__nonASCIIZeroDigitCodePoints$lzycompute__AI = (function($thiz) {
  if (((((16 & $thiz.jl_Character$__f_bitmap$0) << 24) >> 24) === 0)) {
    $thiz.jl_Character$__f_nonASCIIZeroDigitCodePoints = $makeNativeArrayWrapper($d_I.getArrayOf(), [1632, 1776, 1984, 2406, 2534, 2662, 2790, 2918, 3046, 3174, 3302, 3430, 3664, 3792, 3872, 4160, 4240, 6112, 6160, 6470, 6608, 6784, 6800, 6992, 7088, 7232, 7248, 42528, 43216, 43264, 43472, 43600, 44016, 65296, 66720, 69734, 69872, 69942, 70096, 71360, 120782, 120792, 120802, 120812, 120822]);
    $thiz.jl_Character$__f_bitmap$0 = (((16 | $thiz.jl_Character$__f_bitmap$0) << 24) >> 24)
  };
  return $thiz.jl_Character$__f_nonASCIIZeroDigitCodePoints
});
const $p_jl_Character$__nonASCIIZeroDigitCodePoints__AI = (function($thiz) {
  return (((((16 & $thiz.jl_Character$__f_bitmap$0) << 24) >> 24) === 0) ? $p_jl_Character$__nonASCIIZeroDigitCodePoints$lzycompute__AI($thiz) : $thiz.jl_Character$__f_nonASCIIZeroDigitCodePoints)
});
class $c_jl_Character$ extends $c_O {
  constructor() {
    super();
    this.jl_Character$__f_java$lang$Character$$charTypesFirst256 = null;
    this.jl_Character$__f_charTypeIndices = null;
    this.jl_Character$__f_charTypes = null;
    this.jl_Character$__f_isMirroredIndices = null;
    this.jl_Character$__f_nonASCIIZeroDigitCodePoints = null;
    this.jl_Character$__f_bitmap$0 = 0
  };
  digitWithValidRadix__I__I__I(codePoint, radix) {
    let value;
    if ((codePoint < 256)) {
      value = (((codePoint >= 48) && (codePoint <= 57)) ? (((-48) + codePoint) | 0) : (((codePoint >= 65) && (codePoint <= 90)) ? (((-55) + codePoint) | 0) : (((codePoint >= 97) && (codePoint <= 122)) ? (((-87) + codePoint) | 0) : (-1))))
    } else if (((codePoint >= 65313) && (codePoint <= 65338))) {
      value = (((-65303) + codePoint) | 0)
    } else if (((codePoint >= 65345) && (codePoint <= 65370))) {
      value = (((-65335) + codePoint) | 0)
    } else {
      const a = $p_jl_Character$__nonASCIIZeroDigitCodePoints__AI(this);
      const p = $m_ju_Arrays$().binarySearch__AI__I__I(a, codePoint);
      const zeroCodePointIndex = ((p < 0) ? (((-2) - p) | 0) : p);
      if ((zeroCodePointIndex < 0)) {
        value = (-1)
      } else {
        const v = ((codePoint - $p_jl_Character$__nonASCIIZeroDigitCodePoints__AI(this).get(zeroCodePointIndex)) | 0);
        value = ((v > 9) ? (-1) : v)
      }
    };
    return ((value < radix) ? value : (-1))
  };
}
const $d_jl_Character$ = new $TypeData().initClass({
  jl_Character$: 0
}, false, "java.lang.Character$", {
  jl_Character$: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_jl_Character$.prototype.$classData = $d_jl_Character$;
let $n_jl_Character$ = (void 0);
function $m_jl_Character$() {
  if ((!$n_jl_Character$)) {
    $n_jl_Character$ = new $c_jl_Character$()
  };
  return $n_jl_Character$
}
const $p_jl_Integer$__fail$1__T__E = (function($thiz, s$1) {
  throw new $c_jl_NumberFormatException((("For input string: \"" + s$1) + "\""))
});
class $c_jl_Integer$ extends $c_O {
  parseInt__T__I__I(s, radix) {
    const len = ((s === null) ? 0 : $uI(s.length));
    if ((((len === 0) || (radix < 2)) || (radix > 36))) {
      $p_jl_Integer$__fail$1__T__E(this, s)
    };
    const firstChar = (65535 & $uI(s.charCodeAt(0)));
    const negative = (firstChar === 45);
    const maxAbsValue = (negative ? 2.147483648E9 : 2.147483647E9);
    let i = ((negative || (firstChar === 43)) ? 1 : 0);
    if ((i >= $uI(s.length))) {
      $p_jl_Integer$__fail$1__T__E(this, s)
    };
    let result = 0.0;
    while ((i !== len)) {
      const $$x1 = $m_jl_Character$();
      const index = i;
      const digit = $$x1.digitWithValidRadix__I__I__I((65535 & $uI(s.charCodeAt(index))), radix);
      result = ((result * radix) + digit);
      if (((digit === (-1)) || (result > maxAbsValue))) {
        $p_jl_Integer$__fail$1__T__E(this, s)
      };
      i = ((1 + i) | 0)
    };
    if (negative) {
      const n = (-result);
      return $uI((n | 0))
    } else {
      const n$1 = result;
      return $uI((n$1 | 0))
    }
  };
}
const $d_jl_Integer$ = new $TypeData().initClass({
  jl_Integer$: 0
}, false, "java.lang.Integer$", {
  jl_Integer$: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_jl_Integer$.prototype.$classData = $d_jl_Integer$;
let $n_jl_Integer$ = (void 0);
function $m_jl_Integer$() {
  if ((!$n_jl_Integer$)) {
    $n_jl_Integer$ = new $c_jl_Integer$()
  };
  return $n_jl_Integer$
}
class $c_jl_Number extends $c_O {
}
function $is_jl_Number(obj) {
  return ((obj instanceof $c_jl_Number) || ((typeof obj) === "number"))
}
function $as_jl_Number(obj) {
  return (($is_jl_Number(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "java.lang.Number"))
}
function $isArrayOf_jl_Number(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.jl_Number)))
}
function $asArrayOf_jl_Number(obj, depth) {
  return (($isArrayOf_jl_Number(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Ljava.lang.Number;", depth))
}
const $ct_jl_Throwable__T__jl_Throwable__Z__Z__ = (function($thiz, s, e, enableSuppression, writableStackTrace) {
  $thiz.jl_Throwable__f_s = s;
  $thiz.jl_Throwable__f_e = e;
  $thiz.jl_Throwable__f_enableSuppression = enableSuppression;
  $thiz.jl_Throwable__f_writableStackTrace = writableStackTrace;
  if (writableStackTrace) {
    $thiz.fillInStackTrace__jl_Throwable()
  };
  return $thiz
});
class $c_jl_Throwable extends Error {
  constructor() {
    super();
    this.jl_Throwable__f_s = null;
    this.jl_Throwable__f_e = null;
    this.jl_Throwable__f_enableSuppression = false;
    this.jl_Throwable__f_writableStackTrace = false;
    this.jl_Throwable__f_stackTraceStateInternal = null;
    this.jl_Throwable__f_stackTrace = null;
    this.jl_Throwable__f_suppressed = null
  };
  getMessage__T() {
    return this.jl_Throwable__f_s
  };
  fillInStackTrace__jl_Throwable() {
    const identifyingString = Object.prototype.toString.call(this);
    if ((identifyingString === "[object Error]")) {
      this.jl_Throwable__f_stackTraceStateInternal = this
    } else if ((Error.captureStackTrace === (void 0))) {
      const e = new Error();
      this.jl_Throwable__f_stackTraceStateInternal = e
    } else {
      Error.captureStackTrace(this);
      this.jl_Throwable__f_stackTraceStateInternal = this
    };
    return this
  };
  toString__T() {
    const className = $objectClassName(this);
    const message = this.getMessage__T();
    return ((message === null) ? className : ((className + ": ") + message))
  };
  $js$exported$meth$toString__O() {
    return this.toString__T()
  };
  $js$exported$prop$name__O() {
    return $objectClassName(this)
  };
  $js$exported$prop$message__O() {
    const m = this.getMessage__T();
    return ((m === null) ? "" : m)
  };
  hashCode__I() {
    return $c_O.prototype.hashCode__I.call(this)
  };
  equals__O__Z(that) {
    return $c_O.prototype.equals__O__Z.call(this, that)
  };
  get "message"() {
    return this.$js$exported$prop$message__O()
  };
  get "name"() {
    return this.$js$exported$prop$name__O()
  };
  "toString"() {
    return this.$js$exported$meth$toString__O()
  };
}
function $as_jl_Throwable(obj) {
  return (((obj instanceof $c_jl_Throwable) || (obj === null)) ? obj : $throwClassCastException(obj, "java.lang.Throwable"))
}
function $isArrayOf_jl_Throwable(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.jl_Throwable)))
}
function $asArrayOf_jl_Throwable(obj, depth) {
  return (($isArrayOf_jl_Throwable(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Ljava.lang.Throwable;", depth))
}
const $p_ju_regex_Matcher__ensureLastMatch__sjs_js_RegExp$ExecResult = (function($thiz) {
  if (($thiz.ju_regex_Matcher__f_lastMatch === null)) {
    throw new $c_jl_IllegalStateException("No match available")
  };
  return $thiz.ju_regex_Matcher__f_lastMatch
});
class $c_ju_regex_Matcher extends $c_O {
  constructor(pattern0, input0, regionStart0, regionEnd0) {
    super();
    this.ju_regex_Matcher__f_pattern0 = null;
    this.ju_regex_Matcher__f_input0 = null;
    this.ju_regex_Matcher__f_regionStart0 = 0;
    this.ju_regex_Matcher__f_regionEnd0 = 0;
    this.ju_regex_Matcher__f_regexp = null;
    this.ju_regex_Matcher__f_inputstr = null;
    this.ju_regex_Matcher__f_lastMatch = null;
    this.ju_regex_Matcher__f_lastMatchIsValid = false;
    this.ju_regex_Matcher__f_canStillFind = false;
    this.ju_regex_Matcher__f_appendPos = 0;
    this.ju_regex_Matcher__f_startOfGroupCache = null;
    this.ju_regex_Matcher__f_pattern0 = pattern0;
    this.ju_regex_Matcher__f_input0 = input0;
    this.ju_regex_Matcher__f_regionStart0 = regionStart0;
    this.ju_regex_Matcher__f_regionEnd0 = regionEnd0;
    this.ju_regex_Matcher__f_regexp = this.ju_regex_Matcher__f_pattern0.newJSRegExp__sjs_js_RegExp();
    this.ju_regex_Matcher__f_inputstr = $dp_toString__T($dp_subSequence__I__I__jl_CharSequence(this.ju_regex_Matcher__f_input0, this.ju_regex_Matcher__f_regionStart0, this.ju_regex_Matcher__f_regionEnd0));
    this.ju_regex_Matcher__f_lastMatch = null;
    this.ju_regex_Matcher__f_lastMatchIsValid = false;
    this.ju_regex_Matcher__f_canStillFind = true;
    this.ju_regex_Matcher__f_appendPos = 0
  };
  find__Z() {
    if (this.ju_regex_Matcher__f_canStillFind) {
      this.ju_regex_Matcher__f_lastMatchIsValid = true;
      this.ju_regex_Matcher__f_lastMatch = this.ju_regex_Matcher__f_regexp.exec(this.ju_regex_Matcher__f_inputstr);
      if ((this.ju_regex_Matcher__f_lastMatch !== null)) {
        const value = this.ju_regex_Matcher__f_lastMatch[0];
        if ((value === (void 0))) {
          throw $ct_ju_NoSuchElementException__T__(new $c_ju_NoSuchElementException(), "undefined.get")
        };
        const this$5 = $as_T(value);
        if ((this$5 === "")) {
          const ev$1 = this.ju_regex_Matcher__f_regexp;
          ev$1.lastIndex = ((1 + $uI(ev$1.lastIndex)) | 0)
        }
      } else {
        this.ju_regex_Matcher__f_canStillFind = false
      };
      this.ju_regex_Matcher__f_startOfGroupCache = null;
      return (this.ju_regex_Matcher__f_lastMatch !== null)
    } else {
      return false
    }
  };
  appendReplacement__jl_StringBuffer__T__ju_regex_Matcher(sb, replacement) {
    const this$1 = this.ju_regex_Matcher__f_inputstr;
    const beginIndex = this.ju_regex_Matcher__f_appendPos;
    const endIndex = this.start__I();
    sb.append__T__jl_StringBuffer($as_T(this$1.substring(beginIndex, endIndex)));
    const len = $uI(replacement.length);
    let i = 0;
    while ((i < len)) {
      const index = i;
      const x1 = (65535 & $uI(replacement.charCodeAt(index)));
      switch (x1) {
        case 36: {
          i = ((1 + i) | 0);
          const j = i;
          while (true) {
            let $$x1;
            if ((i < len)) {
              const index$1 = i;
              const c = (65535 & $uI(replacement.charCodeAt(index$1)));
              $$x1 = ((c >= 48) && (c <= 57))
            } else {
              $$x1 = false
            };
            if ($$x1) {
              i = ((1 + i) | 0)
            } else {
              break
            }
          };
          const endIndex$1 = i;
          const s = $as_T(replacement.substring(j, endIndex$1));
          const this$2 = $m_jl_Integer$();
          const group = this$2.parseInt__T__I__I(s, 10);
          sb.append__T__jl_StringBuffer(this.group__I__T(group));
          break
        }
        case 92: {
          i = ((1 + i) | 0);
          if ((i < len)) {
            const index$2 = i;
            sb.append__C__jl_StringBuffer((65535 & $uI(replacement.charCodeAt(index$2))))
          };
          i = ((1 + i) | 0);
          break
        }
        default: {
          sb.append__C__jl_StringBuffer(x1);
          i = ((1 + i) | 0)
        }
      }
    };
    this.ju_regex_Matcher__f_appendPos = this.end__I();
    return this
  };
  appendTail__jl_StringBuffer__jl_StringBuffer(sb) {
    const this$1 = this.ju_regex_Matcher__f_inputstr;
    const beginIndex = this.ju_regex_Matcher__f_appendPos;
    sb.append__T__jl_StringBuffer($as_T(this$1.substring(beginIndex)));
    const this$3 = this.ju_regex_Matcher__f_inputstr;
    this.ju_regex_Matcher__f_appendPos = $uI(this$3.length);
    return sb
  };
  replaceAll__T__T(replacement) {
    this.reset__ju_regex_Matcher();
    const sb = $ct_jl_StringBuffer__(new $c_jl_StringBuffer());
    while (this.find__Z()) {
      this.appendReplacement__jl_StringBuffer__T__ju_regex_Matcher(sb, replacement)
    };
    this.appendTail__jl_StringBuffer__jl_StringBuffer(sb);
    return sb.toString__T()
  };
  reset__ju_regex_Matcher() {
    this.ju_regex_Matcher__f_regexp.lastIndex = 0;
    this.ju_regex_Matcher__f_lastMatch = null;
    this.ju_regex_Matcher__f_lastMatchIsValid = false;
    this.ju_regex_Matcher__f_canStillFind = true;
    this.ju_regex_Matcher__f_appendPos = 0;
    this.ju_regex_Matcher__f_startOfGroupCache = null;
    return this
  };
  start__I() {
    return $uI($p_ju_regex_Matcher__ensureLastMatch__sjs_js_RegExp$ExecResult(this).index)
  };
  end__I() {
    const $$x1 = this.start__I();
    const this$1 = this.group__T();
    return (($$x1 + $uI(this$1.length)) | 0)
  };
  group__T() {
    const value = $p_ju_regex_Matcher__ensureLastMatch__sjs_js_RegExp$ExecResult(this)[0];
    if ((value === (void 0))) {
      throw $ct_ju_NoSuchElementException__T__(new $c_ju_NoSuchElementException(), "undefined.get")
    };
    return $as_T(value)
  };
  group__I__T(group) {
    const value = $p_ju_regex_Matcher__ensureLastMatch__sjs_js_RegExp$ExecResult(this)[group];
    return $as_T(((value === (void 0)) ? null : value))
  };
}
const $d_ju_regex_Matcher = new $TypeData().initClass({
  ju_regex_Matcher: 0
}, false, "java.util.regex.Matcher", {
  ju_regex_Matcher: 1,
  O: 1,
  ju_regex_MatchResult: 1
});
$c_ju_regex_Matcher.prototype.$classData = $d_ju_regex_Matcher;
const $p_RTLong$__toUnsignedString__I__I__T = (function($thiz, lo, hi) {
  if ((((-2097152) & hi) === 0)) {
    const this$1 = ((4.294967296E9 * hi) + $uD((lo >>> 0)));
    return ("" + this$1)
  } else {
    return $as_T($p_RTLong$__unsignedDivModHelper__I__I__I__I__I__O($thiz, lo, hi, 1000000000, 0, 2))
  }
});
const $p_RTLong$__unsigned_$div__I__I__I__I__I = (function($thiz, alo, ahi, blo, bhi) {
  if ((((-2097152) & ahi) === 0)) {
    if ((((-2097152) & bhi) === 0)) {
      const aDouble = ((4.294967296E9 * ahi) + $uD((alo >>> 0)));
      const bDouble = ((4.294967296E9 * bhi) + $uD((blo >>> 0)));
      const rDouble = (aDouble / bDouble);
      const x = (rDouble / 4.294967296E9);
      $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = $uI((x | 0));
      return $uI((rDouble | 0))
    } else {
      $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = 0;
      return 0
    }
  } else if (((bhi === 0) && ((blo & (((-1) + blo) | 0)) === 0))) {
    const pow = ((31 - $clz32(blo)) | 0);
    $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = ((ahi >>> pow) | 0);
    return (((alo >>> pow) | 0) | ((ahi << 1) << ((31 - pow) | 0)))
  } else if (((blo === 0) && ((bhi & (((-1) + bhi) | 0)) === 0))) {
    const pow$2 = ((31 - $clz32(bhi)) | 0);
    $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = 0;
    return ((ahi >>> pow$2) | 0)
  } else {
    return $uI($p_RTLong$__unsignedDivModHelper__I__I__I__I__I__O($thiz, alo, ahi, blo, bhi, 0))
  }
});
const $p_RTLong$__unsigned_$percent__I__I__I__I__I = (function($thiz, alo, ahi, blo, bhi) {
  if ((((-2097152) & ahi) === 0)) {
    if ((((-2097152) & bhi) === 0)) {
      const aDouble = ((4.294967296E9 * ahi) + $uD((alo >>> 0)));
      const bDouble = ((4.294967296E9 * bhi) + $uD((blo >>> 0)));
      const rDouble = (aDouble % bDouble);
      const x = (rDouble / 4.294967296E9);
      $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = $uI((x | 0));
      return $uI((rDouble | 0))
    } else {
      $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = ahi;
      return alo
    }
  } else if (((bhi === 0) && ((blo & (((-1) + blo) | 0)) === 0))) {
    $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = 0;
    return (alo & (((-1) + blo) | 0))
  } else if (((blo === 0) && ((bhi & (((-1) + bhi) | 0)) === 0))) {
    $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = (ahi & (((-1) + bhi) | 0));
    return alo
  } else {
    return $uI($p_RTLong$__unsignedDivModHelper__I__I__I__I__I__O($thiz, alo, ahi, blo, bhi, 1))
  }
});
const $p_RTLong$__unsignedDivModHelper__I__I__I__I__I__O = (function($thiz, alo, ahi, blo, bhi, ask) {
  let shift = ((((bhi !== 0) ? $clz32(bhi) : ((32 + $clz32(blo)) | 0)) - ((ahi !== 0) ? $clz32(ahi) : ((32 + $clz32(alo)) | 0))) | 0);
  const n = shift;
  const lo = (((32 & n) === 0) ? (blo << n) : 0);
  const hi = (((32 & n) === 0) ? (((((blo >>> 1) | 0) >>> ((31 - n) | 0)) | 0) | (bhi << n)) : (blo << n));
  let bShiftLo = lo;
  let bShiftHi = hi;
  let remLo = alo;
  let remHi = ahi;
  let quotLo = 0;
  let quotHi = 0;
  while (((shift >= 0) && (((-2097152) & remHi) !== 0))) {
    const alo$1 = remLo;
    const ahi$1 = remHi;
    const blo$1 = bShiftLo;
    const bhi$1 = bShiftHi;
    if (((ahi$1 === bhi$1) ? (((-2147483648) ^ alo$1) >= ((-2147483648) ^ blo$1)) : (((-2147483648) ^ ahi$1) >= ((-2147483648) ^ bhi$1)))) {
      const lo$1 = remLo;
      const hi$1 = remHi;
      const lo$2 = bShiftLo;
      const hi$2 = bShiftHi;
      const lo$3 = ((lo$1 - lo$2) | 0);
      const hi$3 = ((((-2147483648) ^ lo$3) > ((-2147483648) ^ lo$1)) ? (((-1) + ((hi$1 - hi$2) | 0)) | 0) : ((hi$1 - hi$2) | 0));
      remLo = lo$3;
      remHi = hi$3;
      if ((shift < 32)) {
        quotLo = (quotLo | (1 << shift))
      } else {
        quotHi = (quotHi | (1 << shift))
      }
    };
    shift = (((-1) + shift) | 0);
    const lo$4 = bShiftLo;
    const hi$4 = bShiftHi;
    const lo$5 = (((lo$4 >>> 1) | 0) | (hi$4 << 31));
    const hi$5 = ((hi$4 >>> 1) | 0);
    bShiftLo = lo$5;
    bShiftHi = hi$5
  };
  const alo$2 = remLo;
  const ahi$2 = remHi;
  if (((ahi$2 === bhi) ? (((-2147483648) ^ alo$2) >= ((-2147483648) ^ blo)) : (((-2147483648) ^ ahi$2) >= ((-2147483648) ^ bhi)))) {
    const lo$6 = remLo;
    const hi$6 = remHi;
    const remDouble = ((4.294967296E9 * hi$6) + $uD((lo$6 >>> 0)));
    const bDouble = ((4.294967296E9 * bhi) + $uD((blo >>> 0)));
    if ((ask !== 1)) {
      const x = (remDouble / bDouble);
      const lo$7 = $uI((x | 0));
      const x$1 = (x / 4.294967296E9);
      const hi$7 = $uI((x$1 | 0));
      const lo$8 = quotLo;
      const hi$8 = quotHi;
      const lo$9 = ((lo$8 + lo$7) | 0);
      const hi$9 = ((((-2147483648) ^ lo$9) < ((-2147483648) ^ lo$8)) ? ((1 + ((hi$8 + hi$7) | 0)) | 0) : ((hi$8 + hi$7) | 0));
      quotLo = lo$9;
      quotHi = hi$9
    };
    if ((ask !== 0)) {
      const rem_mod_bDouble = (remDouble % bDouble);
      remLo = $uI((rem_mod_bDouble | 0));
      const x$2 = (rem_mod_bDouble / 4.294967296E9);
      remHi = $uI((x$2 | 0))
    }
  };
  if ((ask === 0)) {
    $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = quotHi;
    return quotLo
  } else if ((ask === 1)) {
    $thiz.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = remHi;
    return remLo
  } else {
    const lo$10 = quotLo;
    const hi$10 = quotHi;
    const quot = ((4.294967296E9 * hi$10) + $uD((lo$10 >>> 0)));
    const this$3 = remLo;
    const remStr = ("" + this$3);
    const start = $uI(remStr.length);
    return ((("" + quot) + $as_T("000000000".substring(start))) + remStr)
  }
});
class $c_RTLong$ extends $c_O {
  constructor() {
    super();
    this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = 0
  };
  org$scalajs$linker$runtime$RuntimeLong$$toString__I__I__T(lo, hi) {
    return ((hi === (lo >> 31)) ? ("" + lo) : ((hi < 0) ? ("-" + $p_RTLong$__toUnsignedString__I__I__T(this, ((-lo) | 0), ((lo !== 0) ? (~hi) : ((-hi) | 0)))) : $p_RTLong$__toUnsignedString__I__I__T(this, lo, hi)))
  };
  org$scalajs$linker$runtime$RuntimeLong$$toDouble__I__I__D(lo, hi) {
    if ((hi < 0)) {
      const x = ((lo !== 0) ? (~hi) : ((-hi) | 0));
      const $$x1 = $uD((x >>> 0));
      const x$1 = ((-lo) | 0);
      return (-((4.294967296E9 * $$x1) + $uD((x$1 >>> 0))))
    } else {
      return ((4.294967296E9 * hi) + $uD((lo >>> 0)))
    }
  };
  fromInt__I__RTLong(value) {
    return new $c_RTLong(value, (value >> 31))
  };
  fromDouble__D__RTLong(value) {
    const lo = this.org$scalajs$linker$runtime$RuntimeLong$$fromDoubleImpl__D__I(value);
    return new $c_RTLong(lo, this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn)
  };
  org$scalajs$linker$runtime$RuntimeLong$$fromDoubleImpl__D__I(value) {
    if ((value < (-9.223372036854776E18))) {
      this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = (-2147483648);
      return 0
    } else if ((value >= 9.223372036854776E18)) {
      this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = 2147483647;
      return (-1)
    } else {
      const rawLo = $uI((value | 0));
      const x = (value / 4.294967296E9);
      const rawHi = $uI((x | 0));
      this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = (((value < 0.0) && (rawLo !== 0)) ? (((-1) + rawHi) | 0) : rawHi);
      return rawLo
    }
  };
  org$scalajs$linker$runtime$RuntimeLong$$compare__I__I__I__I__I(alo, ahi, blo, bhi) {
    return ((ahi === bhi) ? ((alo === blo) ? 0 : ((((-2147483648) ^ alo) < ((-2147483648) ^ blo)) ? (-1) : 1)) : ((ahi < bhi) ? (-1) : 1))
  };
  divideImpl__I__I__I__I__I(alo, ahi, blo, bhi) {
    if (((blo | bhi) === 0)) {
      throw new $c_jl_ArithmeticException("/ by zero")
    };
    if ((ahi === (alo >> 31))) {
      if ((bhi === (blo >> 31))) {
        if (((alo === (-2147483648)) && (blo === (-1)))) {
          this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = 0;
          return (-2147483648)
        } else {
          const lo = $intDiv(alo, blo);
          this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = (lo >> 31);
          return lo
        }
      } else if (((alo === (-2147483648)) && ((blo === (-2147483648)) && (bhi === 0)))) {
        this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = (-1);
        return (-1)
      } else {
        this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = 0;
        return 0
      }
    } else {
      let aAbs__lo;
      let aAbs__hi;
      if ((ahi < 0)) {
        const lo$1 = ((-alo) | 0);
        const hi = ((alo !== 0) ? (~ahi) : ((-ahi) | 0));
        const $$x1__lo = lo$1;
        const $$x1__hi = hi;
        aAbs__lo = $$x1__lo;
        aAbs__hi = $$x1__hi
      } else {
        const $$x2__lo = alo;
        const $$x2__hi = ahi;
        aAbs__lo = $$x2__lo;
        aAbs__hi = $$x2__hi
      };
      let bAbs__lo;
      let bAbs__hi;
      if ((bhi < 0)) {
        const lo$2 = ((-blo) | 0);
        const hi$1 = ((blo !== 0) ? (~bhi) : ((-bhi) | 0));
        const $$x3__lo = lo$2;
        const $$x3__hi = hi$1;
        bAbs__lo = $$x3__lo;
        bAbs__hi = $$x3__hi
      } else {
        const $$x4__lo = blo;
        const $$x4__hi = bhi;
        bAbs__lo = $$x4__lo;
        bAbs__hi = $$x4__hi
      };
      const absRLo = $p_RTLong$__unsigned_$div__I__I__I__I__I(this, aAbs__lo, aAbs__hi, bAbs__lo, bAbs__hi);
      if (((ahi ^ bhi) >= 0)) {
        return absRLo
      } else {
        const hi$2 = this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn;
        this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = ((absRLo !== 0) ? (~hi$2) : ((-hi$2) | 0));
        return ((-absRLo) | 0)
      }
    }
  };
  remainderImpl__I__I__I__I__I(alo, ahi, blo, bhi) {
    if (((blo | bhi) === 0)) {
      throw new $c_jl_ArithmeticException("/ by zero")
    };
    if ((ahi === (alo >> 31))) {
      if ((bhi === (blo >> 31))) {
        if ((blo !== (-1))) {
          const lo = $intMod(alo, blo);
          this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = (lo >> 31);
          return lo
        } else {
          this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = 0;
          return 0
        }
      } else if (((alo === (-2147483648)) && ((blo === (-2147483648)) && (bhi === 0)))) {
        this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = 0;
        return 0
      } else {
        this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = ahi;
        return alo
      }
    } else {
      let aAbs__lo;
      let aAbs__hi;
      if ((ahi < 0)) {
        const lo$1 = ((-alo) | 0);
        const hi = ((alo !== 0) ? (~ahi) : ((-ahi) | 0));
        const $$x1__lo = lo$1;
        const $$x1__hi = hi;
        aAbs__lo = $$x1__lo;
        aAbs__hi = $$x1__hi
      } else {
        const $$x2__lo = alo;
        const $$x2__hi = ahi;
        aAbs__lo = $$x2__lo;
        aAbs__hi = $$x2__hi
      };
      let bAbs__lo;
      let bAbs__hi;
      if ((bhi < 0)) {
        const lo$2 = ((-blo) | 0);
        const hi$1 = ((blo !== 0) ? (~bhi) : ((-bhi) | 0));
        const $$x3__lo = lo$2;
        const $$x3__hi = hi$1;
        bAbs__lo = $$x3__lo;
        bAbs__hi = $$x3__hi
      } else {
        const $$x4__lo = blo;
        const $$x4__hi = bhi;
        bAbs__lo = $$x4__lo;
        bAbs__hi = $$x4__hi
      };
      const absRLo = $p_RTLong$__unsigned_$percent__I__I__I__I__I(this, aAbs__lo, aAbs__hi, bAbs__lo, bAbs__hi);
      if ((ahi < 0)) {
        const hi$2 = this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn;
        this.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn = ((absRLo !== 0) ? (~hi$2) : ((-hi$2) | 0));
        return ((-absRLo) | 0)
      } else {
        return absRLo
      }
    }
  };
}
const $d_RTLong$ = new $TypeData().initClass({
  RTLong$: 0
}, false, "org.scalajs.linker.runtime.RuntimeLong$", {
  RTLong$: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_RTLong$.prototype.$classData = $d_RTLong$;
let $n_RTLong$ = (void 0);
function $m_RTLong$() {
  if ((!$n_RTLong$)) {
    $n_RTLong$ = new $c_RTLong$()
  };
  return $n_RTLong$
}
const $f_sc_TraversableOnce__size__I = (function($thiz) {
  const result = new $c_sr_IntRef(0);
  $thiz.foreach__F1__V(new $c_sjsr_AnonFunction1(((this$2, result$1) => ((x$2) => {
    result$1.sr_IntRef__f_elem = ((1 + result$1.sr_IntRef__f_elem) | 0)
  }))($thiz, result)));
  return result.sr_IntRef__f_elem
});
const $f_sc_TraversableOnce__to__scg_CanBuildFrom__O = (function($thiz, cbf) {
  const b = cbf.apply__scm_Builder();
  b.$plus$plus$eq__sc_TraversableOnce__scg_Growable($thiz.seq__sc_TraversableOnce());
  return b.result__O()
});
const $f_sc_TraversableOnce__mkString__T__T__T__T = (function($thiz, start, sep, end) {
  const this$1 = $thiz.addString__scm_StringBuilder__T__T__T__scm_StringBuilder($ct_scm_StringBuilder__(new $c_scm_StringBuilder()), start, sep, end);
  return this$1.scm_StringBuilder__f_underlying.jl_StringBuilder__f_java$lang$StringBuilder$$content
});
const $f_sc_TraversableOnce__addString__scm_StringBuilder__T__T__T__scm_StringBuilder = (function($thiz, b, start, sep, end) {
  const first = new $c_sr_BooleanRef(true);
  b.append__T__scm_StringBuilder(start);
  $thiz.foreach__F1__V(new $c_sjsr_AnonFunction1(((this$2, first$1, b$1, sep$1) => ((x$2) => {
    if (first$1.sr_BooleanRef__f_elem) {
      b$1.append__O__scm_StringBuilder(x$2);
      first$1.sr_BooleanRef__f_elem = false;
      return (void 0)
    } else {
      b$1.append__T__scm_StringBuilder(sep$1);
      return b$1.append__O__scm_StringBuilder(x$2)
    }
  }))($thiz, first, b, sep)));
  b.append__T__scm_StringBuilder(end);
  return b
});
function $is_sc_TraversableOnce(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.sc_TraversableOnce)))
}
function $as_sc_TraversableOnce(obj) {
  return (($is_sc_TraversableOnce(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.TraversableOnce"))
}
function $isArrayOf_sc_TraversableOnce(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sc_TraversableOnce)))
}
function $asArrayOf_sc_TraversableOnce(obj, depth) {
  return (($isArrayOf_sc_TraversableOnce(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.TraversableOnce;", depth))
}
const $ct_scg_GenTraversableFactory__ = (function($thiz) {
  $thiz.scg_GenTraversableFactory__f_ReusableCBFInstance = new $c_scg_GenTraversableFactory$$anon$1($thiz);
  return $thiz
});
class $c_scg_GenTraversableFactory extends $c_scg_GenericCompanion {
  constructor() {
    super();
    this.scg_GenTraversableFactory__f_ReusableCBFInstance = null
  };
}
const $ct_scg_GenTraversableFactory$GenericCanBuildFrom__scg_GenTraversableFactory__ = (function($thiz, outer) {
  if ((outer === null)) {
    throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
  } else {
    $thiz.scg_GenTraversableFactory$GenericCanBuildFrom__f_$outer = outer
  };
  return $thiz
});
class $c_scg_GenTraversableFactory$GenericCanBuildFrom extends $c_O {
  constructor() {
    super();
    this.scg_GenTraversableFactory$GenericCanBuildFrom__f_$outer = null
  };
  apply__scm_Builder() {
    return this.scg_GenTraversableFactory$GenericCanBuildFrom__f_$outer.newBuilder__scm_Builder()
  };
  apply__O__scm_Builder(from) {
    const from$1 = $as_sc_GenTraversable(from);
    return from$1.companion__scg_GenericCompanion().newBuilder__scm_Builder()
  };
}
const $p_scg_Growable__loop$1__sc_LinearSeq__V = (function($thiz, xs) {
  while (true) {
    const this$1 = xs;
    if ((!this$1.isEmpty__Z())) {
      $thiz.$plus$eq__O__scg_Growable(xs.head__O());
      xs = $as_sc_LinearSeq(xs.tail__O());
      continue
    };
    break
  }
});
const $f_scg_Growable__$plus$plus$eq__sc_TraversableOnce__scg_Growable = (function($thiz, xs) {
  if ($is_sc_LinearSeq(xs)) {
    const x2 = $as_sc_LinearSeq(xs);
    $p_scg_Growable__loop$1__sc_LinearSeq__V($thiz, x2)
  } else {
    xs.foreach__F1__V(new $c_sjsr_AnonFunction1(((this$1) => ((elem$2) => this$1.$plus$eq__O__scg_Growable(elem$2)))($thiz)))
  };
  return $thiz
});
class $c_sci_List$$anon$1 extends $c_O {
  toString__T() {
    return "<function1>"
  };
  apply__O__O(x) {
    return this
  };
}
const $d_sci_List$$anon$1 = new $TypeData().initClass({
  sci_List$$anon$1: 0
}, false, "scala.collection.immutable.List$$anon$1", {
  sci_List$$anon$1: 1,
  O: 1,
  F1: 1
});
$c_sci_List$$anon$1.prototype.$classData = $d_sci_List$$anon$1;
const $f_s_math_Ordered__$greater$eq__O__Z = (function($thiz, that) {
  return ($thiz.compare__O__I(that) >= 0)
});
class $c_s_package$$anon$1 extends $c_O {
  toString__T() {
    return "object AnyRef"
  };
}
const $d_s_package$$anon$1 = new $TypeData().initClass({
  s_package$$anon$1: 0
}, false, "scala.package$$anon$1", {
  s_package$$anon$1: 1,
  O: 1,
  s_Specializable: 1
});
$c_s_package$$anon$1.prototype.$classData = $d_s_package$$anon$1;
class $c_sr_AbstractFunction0 extends $c_O {
  toString__T() {
    return "<function0>"
  };
}
class $c_sr_AbstractFunction1 extends $c_O {
  toString__T() {
    return "<function1>"
  };
}
class $c_sr_BooleanRef extends $c_O {
  constructor(elem) {
    super();
    this.sr_BooleanRef__f_elem = false;
    this.sr_BooleanRef__f_elem = elem
  };
  toString__T() {
    const b = this.sr_BooleanRef__f_elem;
    return ("" + b)
  };
}
const $d_sr_BooleanRef = new $TypeData().initClass({
  sr_BooleanRef: 0
}, false, "scala.runtime.BooleanRef", {
  sr_BooleanRef: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_sr_BooleanRef.prototype.$classData = $d_sr_BooleanRef;
class $c_sr_IntRef extends $c_O {
  constructor(elem) {
    super();
    this.sr_IntRef__f_elem = 0;
    this.sr_IntRef__f_elem = elem
  };
  toString__T() {
    const i = this.sr_IntRef__f_elem;
    return ("" + i)
  };
}
const $d_sr_IntRef = new $TypeData().initClass({
  sr_IntRef: 0
}, false, "scala.runtime.IntRef", {
  sr_IntRef: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_sr_IntRef.prototype.$classData = $d_sr_IntRef;
class $c_sr_ObjectRef extends $c_O {
  constructor(elem) {
    super();
    this.sr_ObjectRef__f_elem = null;
    this.sr_ObjectRef__f_elem = elem
  };
  toString__T() {
    const obj = this.sr_ObjectRef__f_elem;
    return ("" + obj)
  };
}
const $d_sr_ObjectRef = new $TypeData().initClass({
  sr_ObjectRef: 0
}, false, "scala.runtime.ObjectRef", {
  sr_ObjectRef: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_sr_ObjectRef.prototype.$classData = $d_sr_ObjectRef;
class $c_s_util_hashing_MurmurHash3$ extends $c_s_util_hashing_MurmurHash3 {
  constructor() {
    super();
    this.s_util_hashing_MurmurHash3$__f_seqSeed = 0;
    this.s_util_hashing_MurmurHash3$__f_mapSeed = 0;
    this.s_util_hashing_MurmurHash3$__f_setSeed = 0;
    $n_s_util_hashing_MurmurHash3$ = this;
    this.s_util_hashing_MurmurHash3$__f_seqSeed = $f_T__hashCode__I("Seq");
    this.s_util_hashing_MurmurHash3$__f_mapSeed = $f_T__hashCode__I("Map");
    this.s_util_hashing_MurmurHash3$__f_setSeed = $f_T__hashCode__I("Set")
  };
  seqHash__sc_Seq__I(xs) {
    if ((xs instanceof $c_sci_List)) {
      const x2 = $as_sci_List(xs);
      return this.listHash__sci_List__I__I(x2, this.s_util_hashing_MurmurHash3$__f_seqSeed)
    } else {
      return this.orderedHash__sc_TraversableOnce__I__I(xs, this.s_util_hashing_MurmurHash3$__f_seqSeed)
    }
  };
}
const $d_s_util_hashing_MurmurHash3$ = new $TypeData().initClass({
  s_util_hashing_MurmurHash3$: 0
}, false, "scala.util.hashing.MurmurHash3$", {
  s_util_hashing_MurmurHash3$: 1,
  s_util_hashing_MurmurHash3: 1,
  O: 1
});
$c_s_util_hashing_MurmurHash3$.prototype.$classData = $d_s_util_hashing_MurmurHash3$;
let $n_s_util_hashing_MurmurHash3$ = (void 0);
function $m_s_util_hashing_MurmurHash3$() {
  if ((!$n_s_util_hashing_MurmurHash3$)) {
    $n_s_util_hashing_MurmurHash3$ = new $c_s_util_hashing_MurmurHash3$()
  };
  return $n_s_util_hashing_MurmurHash3$
}
class $c_Lsgl_GameLoopComponent$SilentGameLoopListener$ extends $c_Lsgl_GameLoopComponent$GameLoopListener {
  constructor(outer) {
    super();
    $ct_Lsgl_GameLoopComponent$GameLoopListener__Lsgl_GameLoopComponent__(this, outer)
  };
}
const $d_Lsgl_GameLoopComponent$SilentGameLoopListener$ = new $TypeData().initClass({
  Lsgl_GameLoopComponent$SilentGameLoopListener$: 0
}, false, "sgl.GameLoopComponent$SilentGameLoopListener$", {
  Lsgl_GameLoopComponent$SilentGameLoopListener$: 1,
  Lsgl_GameLoopComponent$GameLoopListener: 1,
  O: 1
});
$c_Lsgl_GameLoopComponent$SilentGameLoopListener$.prototype.$classData = $d_Lsgl_GameLoopComponent$SilentGameLoopListener$;
function $is_Lsgl_GraphicsProvider(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.Lsgl_GraphicsProvider)))
}
function $as_Lsgl_GraphicsProvider(obj) {
  return (($is_Lsgl_GraphicsProvider(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.GraphicsProvider"))
}
function $isArrayOf_Lsgl_GraphicsProvider(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_GraphicsProvider)))
}
function $asArrayOf_Lsgl_GraphicsProvider(obj, depth) {
  return (($isArrayOf_Lsgl_GraphicsProvider(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.GraphicsProvider;", depth))
}
class $c_Lsgl_SilentLifecyclieListener$ extends $c_O {
}
const $d_Lsgl_SilentLifecyclieListener$ = new $TypeData().initClass({
  Lsgl_SilentLifecyclieListener$: 0
}, false, "sgl.SilentLifecyclieListener$", {
  Lsgl_SilentLifecyclieListener$: 1,
  O: 1,
  Lsgl_LifecycleListener: 1
});
$c_Lsgl_SilentLifecyclieListener$.prototype.$classData = $d_Lsgl_SilentLifecyclieListener$;
let $n_Lsgl_SilentLifecyclieListener$ = (void 0);
function $m_Lsgl_SilentLifecyclieListener$() {
  if ((!$n_Lsgl_SilentLifecyclieListener$)) {
    $n_Lsgl_SilentLifecyclieListener$ = new $c_Lsgl_SilentLifecyclieListener$()
  };
  return $n_Lsgl_SilentLifecyclieListener$
}
const $f_Lsgl_html5_Html5AudioProvider__$init$__V = (function($thiz) {
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_GuardAutoPlay = true;
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_Audio = $thiz.Html5Audio__Lsgl_html5_Html5AudioProvider$Html5Audio$()
});
class $c_Lsgl_html5_Html5AudioProvider$Html5Audio$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_html5_Html5AudioProvider$Html5Audio$__f_Html5Sound$module = null;
    this.Lsgl_html5_Html5AudioProvider$Html5Audio$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_html5_Html5AudioProvider$Html5Audio$__f_$outer = outer
    }
  };
}
const $d_Lsgl_html5_Html5AudioProvider$Html5Audio$ = new $TypeData().initClass({
  Lsgl_html5_Html5AudioProvider$Html5Audio$: 0
}, false, "sgl.html5.Html5AudioProvider$Html5Audio$", {
  Lsgl_html5_Html5AudioProvider$Html5Audio$: 1,
  O: 1,
  Lsgl_AudioProvider$Audio: 1
});
$c_Lsgl_html5_Html5AudioProvider$Html5Audio$.prototype.$classData = $d_Lsgl_html5_Html5AudioProvider$Html5Audio$;
class $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5ColorCompanion$ extends $c_Lsgl_GraphicsProvider$Graphics$ColorCompanion {
  constructor(outer) {
    super();
    $ct_Lsgl_GraphicsProvider$Graphics$ColorCompanion__Lsgl_GraphicsProvider$Graphics__(this, outer)
  };
  rgb__I__I__I__T(r, g, b) {
    return (((((("rgb(" + r) + ",") + g) + ",") + b) + ")")
  };
}
const $d_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5ColorCompanion$ = new $TypeData().initClass({
  Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5ColorCompanion$: 0
}, false, "sgl.html5.Html5GraphicsProvider$Html5Graphics$Html5ColorCompanion$", {
  Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5ColorCompanion$: 1,
  Lsgl_GraphicsProvider$Graphics$ColorCompanion: 1,
  O: 1
});
$c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5ColorCompanion$.prototype.$classData = $d_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5ColorCompanion$;
const $p_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$__Default$lzycompute__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font = (function($thiz) {
  if (((((1 & $thiz.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$__f_bitmap$0) << 24) >> 24) === 0)) {
    $thiz.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$__f_Default = new $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font($as_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$($thiz.Lsgl_GraphicsProvider$Graphics$FontCompanion__f_$outer), "sans-serif", $thiz.Normal__Lsgl_GraphicsProvider$Graphics$FontCompanion$Normal$(), 10);
    $thiz.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$__f_bitmap$0 = (((1 | $thiz.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$__f_bitmap$0) << 24) >> 24)
  };
  return $thiz.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$__f_Default
});
class $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$ extends $c_Lsgl_GraphicsProvider$Graphics$FontCompanion {
  constructor(outer) {
    super();
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$__f_fontLock$module = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$__f_Default = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$__f_DefaultBold = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$__f_Monospace = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$__f_SansSerif = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$__f_Serif = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$__f_fontId = 0;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$__f_bitmap$0 = 0;
    $ct_Lsgl_GraphicsProvider$Graphics$FontCompanion__Lsgl_GraphicsProvider$Graphics__(this, outer);
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$__f_fontId = (-1)
  };
  toCssStyle__Lsgl_GraphicsProvider$Graphics$FontCompanion$Style__T(s) {
    const x = this.Bold__Lsgl_GraphicsProvider$Graphics$FontCompanion$Bold$();
    if ((x === s)) {
      return "bold"
    } else {
      const x$3 = this.Italic__Lsgl_GraphicsProvider$Graphics$FontCompanion$Italic$();
      if ((x$3 === s)) {
        return "italic"
      } else {
        const x$5 = this.Normal__Lsgl_GraphicsProvider$Graphics$FontCompanion$Normal$();
        if ((x$5 === s)) {
          return "normal"
        } else {
          const x$7 = this.BoldItalic__Lsgl_GraphicsProvider$Graphics$FontCompanion$BoldItalic$();
          if ((x$7 === s)) {
            return "italic bold"
          } else {
            throw new $c_s_MatchError(s)
          }
        }
      }
    }
  };
  Default__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font() {
    return (((((1 & this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$__f_bitmap$0) << 24) >> 24) === 0) ? $p_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$__Default$lzycompute__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font(this) : this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$__f_Default)
  };
}
const $d_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$ = new $TypeData().initClass({
  Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$: 0
}, false, "sgl.html5.Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$", {
  Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$: 1,
  Lsgl_GraphicsProvider$Graphics$FontCompanion: 1,
  O: 1
});
$c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$.prototype.$classData = $d_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$;
const $p_Lsgl_html5_Html5InputProvider__mouseEventButton__Lorg_scalajs_dom_raw_MouseEvent__Lsgl_InputProvider$Input$MouseButtons$MouseButton = (function($thiz, e) {
  const x1 = $uI(e.button);
  switch (x1) {
    case 0: {
      return $thiz.Input__Lsgl_InputProvider$Input$().MouseButtons__Lsgl_InputProvider$Input$MouseButtons$().Left__Lsgl_InputProvider$Input$MouseButtons$Left$();
      break
    }
    case 1: {
      return $thiz.Input__Lsgl_InputProvider$Input$().MouseButtons__Lsgl_InputProvider$Input$MouseButtons$().Middle__Lsgl_InputProvider$Input$MouseButtons$Middle$();
      break
    }
    case 2: {
      return $thiz.Input__Lsgl_InputProvider$Input$().MouseButtons__Lsgl_InputProvider$Input$MouseButtons$().Right__Lsgl_InputProvider$Input$MouseButtons$Right$();
      break
    }
    default: {
      return $thiz.Input__Lsgl_InputProvider$Input$().MouseButtons__Lsgl_InputProvider$Input$MouseButtons$().Left__Lsgl_InputProvider$Input$MouseButtons$Left$()
    }
  }
});
const $p_Lsgl_html5_Html5InputProvider__getCursorPosition__Lorg_scalajs_dom_raw_HTMLCanvasElement__I__I__T2 = (function($thiz, canvas, clientX, clientY) {
  const rect = canvas.getBoundingClientRect();
  const x = $doubleToInt(((clientX - $uD(rect.left)) * $uD($m_Lorg_scalajs_dom_package$().window__Lorg_scalajs_dom_raw_Window().devicePixelRatio)));
  const y = $doubleToInt(((clientY - $uD(rect.top)) * $uD($m_Lorg_scalajs_dom_package$().window__Lorg_scalajs_dom_raw_Window().devicePixelRatio)));
  return new $c_T2$mcII$sp(x, y)
});
const $p_Lsgl_html5_Html5InputProvider__getCursorPosition__Lorg_scalajs_dom_raw_HTMLCanvasElement__Lorg_scalajs_dom_raw_MouseEvent__T2 = (function($thiz, canvas, e) {
  return $p_Lsgl_html5_Html5InputProvider__getCursorPosition__Lorg_scalajs_dom_raw_HTMLCanvasElement__I__I__T2($thiz, canvas, $doubleToInt($uD(e.clientX)), $doubleToInt($uD(e.clientY)))
});
const $p_Lsgl_html5_Html5InputProvider__triggerUserInteraction__V = (function($thiz) {
  if ((!$thiz.Lcom_regblanc_scalavator_html5_Main$__f_sgl$html5$Html5InputProvider$$hasUserInteracted)) {
    $thiz.Lcom_regblanc_scalavator_html5_Main$__f_sgl$html5$Html5InputProvider$$hasUserInteracted = true;
    const this$1 = $thiz.Lcom_regblanc_scalavator_html5_Main$__f_sgl$html5$Html5InputProvider$$actionsOnUserInteraction;
    const this$2 = this$1.scm_ListBuffer__f_scala$collection$mutable$ListBuffer$$start;
    let these = this$2;
    while ((!these.isEmpty__Z())) {
      const arg1 = these.head__O();
      const f = $as_F0(arg1);
      f.apply__O();
      const this$3 = these;
      these = this$3.tail__sci_List()
    }
  }
});
const $p_Lsgl_html5_Html5InputProvider__domEventToKey__Lorg_scalajs_dom_raw_KeyboardEvent__s_Option = (function($thiz, e) {
  const x1 = $uI(e.keyCode);
  switch (x1) {
    case 32: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().Space__Lsgl_InputProvider$Input$Keys$Space$());
      break
    }
    case 37: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().Left__Lsgl_InputProvider$Input$Keys$Left$());
      break
    }
    case 38: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().Up__Lsgl_InputProvider$Input$Keys$Up$());
      break
    }
    case 39: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().Right__Lsgl_InputProvider$Input$Keys$Right$());
      break
    }
    case 40: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().Down__Lsgl_InputProvider$Input$Keys$Down$());
      break
    }
    case 48: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().Num0__Lsgl_InputProvider$Input$Keys$Num0$());
      break
    }
    case 49: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().Num1__Lsgl_InputProvider$Input$Keys$Num1$());
      break
    }
    case 50: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().Num2__Lsgl_InputProvider$Input$Keys$Num2$());
      break
    }
    case 51: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().Num3__Lsgl_InputProvider$Input$Keys$Num3$());
      break
    }
    case 52: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().Num4__Lsgl_InputProvider$Input$Keys$Num4$());
      break
    }
    case 53: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().Num5__Lsgl_InputProvider$Input$Keys$Num5$());
      break
    }
    case 54: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().Num6__Lsgl_InputProvider$Input$Keys$Num6$());
      break
    }
    case 55: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().Num7__Lsgl_InputProvider$Input$Keys$Num7$());
      break
    }
    case 56: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().Num8__Lsgl_InputProvider$Input$Keys$Num8$());
      break
    }
    case 57: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().Num9__Lsgl_InputProvider$Input$Keys$Num9$());
      break
    }
    case 65: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().A__Lsgl_InputProvider$Input$Keys$A$());
      break
    }
    case 66: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().B__Lsgl_InputProvider$Input$Keys$B$());
      break
    }
    case 67: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().C__Lsgl_InputProvider$Input$Keys$C$());
      break
    }
    case 68: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().D__Lsgl_InputProvider$Input$Keys$D$());
      break
    }
    case 69: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().E__Lsgl_InputProvider$Input$Keys$E$());
      break
    }
    case 70: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().F__Lsgl_InputProvider$Input$Keys$F$());
      break
    }
    case 71: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().G__Lsgl_InputProvider$Input$Keys$G$());
      break
    }
    case 72: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().H__Lsgl_InputProvider$Input$Keys$H$());
      break
    }
    case 73: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().I__Lsgl_InputProvider$Input$Keys$I$());
      break
    }
    case 74: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().J__Lsgl_InputProvider$Input$Keys$J$());
      break
    }
    case 75: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().K__Lsgl_InputProvider$Input$Keys$K$());
      break
    }
    case 76: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().L__Lsgl_InputProvider$Input$Keys$L$());
      break
    }
    case 77: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().M__Lsgl_InputProvider$Input$Keys$M$());
      break
    }
    case 78: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().N__Lsgl_InputProvider$Input$Keys$N$());
      break
    }
    case 79: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().O__Lsgl_InputProvider$Input$Keys$O$());
      break
    }
    case 80: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().P__Lsgl_InputProvider$Input$Keys$P$());
      break
    }
    case 81: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().Q__Lsgl_InputProvider$Input$Keys$Q$());
      break
    }
    case 82: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().R__Lsgl_InputProvider$Input$Keys$R$());
      break
    }
    case 83: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().S__Lsgl_InputProvider$Input$Keys$S$());
      break
    }
    case 84: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().T__Lsgl_InputProvider$Input$Keys$T$());
      break
    }
    case 85: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().U__Lsgl_InputProvider$Input$Keys$U$());
      break
    }
    case 86: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().V__Lsgl_InputProvider$Input$Keys$V$());
      break
    }
    case 87: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().W__Lsgl_InputProvider$Input$Keys$W$());
      break
    }
    case 88: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().X__Lsgl_InputProvider$Input$Keys$X$());
      break
    }
    case 89: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().Y__Lsgl_InputProvider$Input$Keys$Y$());
      break
    }
    case 90: {
      return new $c_s_Some($thiz.Input__Lsgl_InputProvider$Input$().Keys__Lsgl_InputProvider$Input$Keys$().Z__Lsgl_InputProvider$Input$Keys$Z$());
      break
    }
    default: {
      return $m_s_None$()
    }
  }
});
const $f_Lsgl_html5_Html5InputProvider__registerInputListeners__V = (function($thiz) {
  const qual$1 = $as_Lsgl_html5_Html5App($thiz).Lcom_regblanc_scalavator_html5_Main$__f_htmlCanvas;
  const x$2 = ((arg$outer) => ((arg1$2) => {
    $f_Lsgl_html5_Html5InputProvider__sgl$html5$Html5InputProvider$$$anonfun$registerInputListeners$1__Lorg_scalajs_dom_raw_MouseEvent__V(arg$outer, arg1$2)
  }))($as_Lsgl_html5_Html5App($thiz));
  qual$1.addEventListener("mousedown", x$2);
  const qual$2 = $as_Lsgl_html5_Html5App($thiz).Lcom_regblanc_scalavator_html5_Main$__f_htmlCanvas;
  const x$5 = ((arg$outer$1) => ((arg1$2$1) => {
    $f_Lsgl_html5_Html5InputProvider__sgl$html5$Html5InputProvider$$$anonfun$registerInputListeners$2__Lorg_scalajs_dom_raw_MouseEvent__V(arg$outer$1, arg1$2$1)
  }))($as_Lsgl_html5_Html5App($thiz));
  qual$2.addEventListener("mouseup", x$5);
  const qual$3 = $as_Lsgl_html5_Html5App($thiz).Lcom_regblanc_scalavator_html5_Main$__f_htmlCanvas;
  const x$8 = ((arg$outer$2) => ((arg1$2$2) => {
    $f_Lsgl_html5_Html5InputProvider__sgl$html5$Html5InputProvider$$$anonfun$registerInputListeners$3__Lorg_scalajs_dom_raw_MouseEvent__V(arg$outer$2, arg1$2$2)
  }))($as_Lsgl_html5_Html5App($thiz));
  qual$3.addEventListener("mousemove", x$8);
  const qual$4 = $as_Lsgl_html5_Html5App($thiz).Lcom_regblanc_scalavator_html5_Main$__f_htmlCanvas;
  const x$11 = ((arg$outer$3) => ((arg1$2$3) => {
    $f_Lsgl_html5_Html5InputProvider__sgl$html5$Html5InputProvider$$$anonfun$registerInputListeners$4__Lorg_scalajs_dom_raw_Event__V(arg$outer$3, arg1$2$3)
  }))($as_Lsgl_html5_Html5App($thiz));
  qual$4.addEventListener("touchstart", x$11);
  const qual$5 = $as_Lsgl_html5_Html5App($thiz).Lcom_regblanc_scalavator_html5_Main$__f_htmlCanvas;
  const x$14 = ((arg$outer$4) => ((arg1$2$4) => {
    $f_Lsgl_html5_Html5InputProvider__sgl$html5$Html5InputProvider$$$anonfun$registerInputListeners$5__Lorg_scalajs_dom_raw_Event__V(arg$outer$4, arg1$2$4)
  }))($as_Lsgl_html5_Html5App($thiz));
  qual$5.addEventListener("touchend", x$14);
  const qual$6 = $as_Lsgl_html5_Html5App($thiz).Lcom_regblanc_scalavator_html5_Main$__f_htmlCanvas;
  const x$17 = ((arg$outer$5) => ((arg1$2$5) => {
    $f_Lsgl_html5_Html5InputProvider__sgl$html5$Html5InputProvider$$$anonfun$registerInputListeners$6__Lorg_scalajs_dom_raw_Event__V(arg$outer$5, arg1$2$5)
  }))($as_Lsgl_html5_Html5App($thiz));
  qual$6.addEventListener("touchmove", x$17);
  $m_Lorg_scalajs_dom_package$().document__Lorg_scalajs_dom_raw_HTMLDocument().addEventListener("keydown", ((arg$outer$6) => ((arg1$2$6) => {
    $f_Lsgl_html5_Html5InputProvider__sgl$html5$Html5InputProvider$$$anonfun$registerInputListeners$7__Lorg_scalajs_dom_raw_KeyboardEvent__V(arg$outer$6, arg1$2$6)
  }))($as_Lsgl_html5_Html5App($thiz)));
  $m_Lorg_scalajs_dom_package$().document__Lorg_scalajs_dom_raw_HTMLDocument().addEventListener("keyup", ((arg$outer$7) => ((arg1$2$7) => {
    $f_Lsgl_html5_Html5InputProvider__sgl$html5$Html5InputProvider$$$anonfun$registerInputListeners$10__Lorg_scalajs_dom_raw_KeyboardEvent__V(arg$outer$7, arg1$2$7)
  }))($as_Lsgl_html5_Html5App($thiz)))
});
const $f_Lsgl_html5_Html5InputProvider__sgl$html5$Html5InputProvider$$$anonfun$registerInputListeners$1__Lorg_scalajs_dom_raw_MouseEvent__V = (function($thiz, e) {
  $p_Lsgl_html5_Html5InputProvider__triggerUserInteraction__V($thiz);
  const x1 = $p_Lsgl_html5_Html5InputProvider__getCursorPosition__Lorg_scalajs_dom_raw_HTMLCanvasElement__Lorg_scalajs_dom_raw_MouseEvent__T2($thiz, $as_Lsgl_html5_Html5App($thiz).Lcom_regblanc_scalavator_html5_Main$__f_htmlCanvas, e);
  if ((x1 === null)) {
    throw new $c_s_MatchError(x1)
  };
  const x = x1._1$mcI$sp__I();
  const y = x1._2$mcI$sp__I();
  $thiz.Input__Lsgl_InputProvider$Input$().newEvent__Lsgl_InputProvider$Input$InputEvent__V(new $c_Lsgl_InputProvider$Input$MouseDownEvent($thiz.Input__Lsgl_InputProvider$Input$(), x, y, $p_Lsgl_html5_Html5InputProvider__mouseEventButton__Lorg_scalajs_dom_raw_MouseEvent__Lsgl_InputProvider$Input$MouseButtons$MouseButton($thiz, e)))
});
const $f_Lsgl_html5_Html5InputProvider__sgl$html5$Html5InputProvider$$$anonfun$registerInputListeners$2__Lorg_scalajs_dom_raw_MouseEvent__V = (function($thiz, e) {
  $p_Lsgl_html5_Html5InputProvider__triggerUserInteraction__V($thiz);
  const x1 = $p_Lsgl_html5_Html5InputProvider__getCursorPosition__Lorg_scalajs_dom_raw_HTMLCanvasElement__Lorg_scalajs_dom_raw_MouseEvent__T2($thiz, $as_Lsgl_html5_Html5App($thiz).Lcom_regblanc_scalavator_html5_Main$__f_htmlCanvas, e);
  if ((x1 === null)) {
    throw new $c_s_MatchError(x1)
  };
  const x = x1._1$mcI$sp__I();
  const y = x1._2$mcI$sp__I();
  $thiz.Input__Lsgl_InputProvider$Input$().newEvent__Lsgl_InputProvider$Input$InputEvent__V(new $c_Lsgl_InputProvider$Input$MouseUpEvent($thiz.Input__Lsgl_InputProvider$Input$(), x, y, $p_Lsgl_html5_Html5InputProvider__mouseEventButton__Lorg_scalajs_dom_raw_MouseEvent__Lsgl_InputProvider$Input$MouseButtons$MouseButton($thiz, e)))
});
const $f_Lsgl_html5_Html5InputProvider__sgl$html5$Html5InputProvider$$$anonfun$registerInputListeners$3__Lorg_scalajs_dom_raw_MouseEvent__V = (function($thiz, e) {
  const x1 = $p_Lsgl_html5_Html5InputProvider__getCursorPosition__Lorg_scalajs_dom_raw_HTMLCanvasElement__Lorg_scalajs_dom_raw_MouseEvent__T2($thiz, $as_Lsgl_html5_Html5App($thiz).Lcom_regblanc_scalavator_html5_Main$__f_htmlCanvas, e);
  if ((x1 === null)) {
    throw new $c_s_MatchError(x1)
  };
  const x = x1._1$mcI$sp__I();
  const y = x1._2$mcI$sp__I();
  $thiz.Input__Lsgl_InputProvider$Input$().newEvent__Lsgl_InputProvider$Input$InputEvent__V(new $c_Lsgl_InputProvider$Input$MouseMovedEvent($thiz.Input__Lsgl_InputProvider$Input$(), x, y))
});
const $f_Lsgl_html5_Html5InputProvider__sgl$html5$Html5InputProvider$$$anonfun$registerInputListeners$4__Lorg_scalajs_dom_raw_Event__V = (function($thiz, e) {
  $p_Lsgl_html5_Html5InputProvider__triggerUserInteraction__V($thiz);
  e.preventDefault();
  e.stopPropagation();
  const touches = e.changedTouches;
  let i = 0;
  while ((i < $uI(touches.length))) {
    const touch = touches[i];
    i = ((1 + i) | 0);
    const x1 = $p_Lsgl_html5_Html5InputProvider__getCursorPosition__Lorg_scalajs_dom_raw_HTMLCanvasElement__I__I__T2($thiz, $as_Lsgl_html5_Html5App($thiz).Lcom_regblanc_scalavator_html5_Main$__f_htmlCanvas, $doubleToInt($uD(touch.clientX)), $doubleToInt($uD(touch.clientY)));
    if ((x1 === null)) {
      throw new $c_s_MatchError(x1)
    };
    const x = x1._1$mcI$sp__I();
    const y = x1._2$mcI$sp__I();
    const id = $doubleToInt($uD(touch.identifier));
    $thiz.Input__Lsgl_InputProvider$Input$().newEvent__Lsgl_InputProvider$Input$InputEvent__V(new $c_Lsgl_InputProvider$Input$TouchDownEvent($thiz.Input__Lsgl_InputProvider$Input$(), x, y, id))
  }
});
const $f_Lsgl_html5_Html5InputProvider__sgl$html5$Html5InputProvider$$$anonfun$registerInputListeners$5__Lorg_scalajs_dom_raw_Event__V = (function($thiz, e) {
  $p_Lsgl_html5_Html5InputProvider__triggerUserInteraction__V($thiz);
  e.preventDefault();
  e.stopPropagation();
  const touches = e.changedTouches;
  let i = 0;
  while ((i < $uI(touches.length))) {
    const touch = touches[i];
    i = ((1 + i) | 0);
    const x1 = $p_Lsgl_html5_Html5InputProvider__getCursorPosition__Lorg_scalajs_dom_raw_HTMLCanvasElement__I__I__T2($thiz, $as_Lsgl_html5_Html5App($thiz).Lcom_regblanc_scalavator_html5_Main$__f_htmlCanvas, $doubleToInt($uD(touch.clientX)), $doubleToInt($uD(touch.clientY)));
    if ((x1 === null)) {
      throw new $c_s_MatchError(x1)
    };
    const x = x1._1$mcI$sp__I();
    const y = x1._2$mcI$sp__I();
    const id = $doubleToInt($uD(touch.identifier));
    $thiz.Input__Lsgl_InputProvider$Input$().newEvent__Lsgl_InputProvider$Input$InputEvent__V(new $c_Lsgl_InputProvider$Input$TouchUpEvent($thiz.Input__Lsgl_InputProvider$Input$(), x, y, id))
  }
});
const $f_Lsgl_html5_Html5InputProvider__sgl$html5$Html5InputProvider$$$anonfun$registerInputListeners$6__Lorg_scalajs_dom_raw_Event__V = (function($thiz, e) {
  e.preventDefault();
  e.stopPropagation();
  const touches = e.changedTouches;
  let i = 0;
  while ((i < $uI(touches.length))) {
    const touch = touches[i];
    i = ((1 + i) | 0);
    const x1 = $p_Lsgl_html5_Html5InputProvider__getCursorPosition__Lorg_scalajs_dom_raw_HTMLCanvasElement__I__I__T2($thiz, $as_Lsgl_html5_Html5App($thiz).Lcom_regblanc_scalavator_html5_Main$__f_htmlCanvas, $doubleToInt($uD(touch.clientX)), $doubleToInt($uD(touch.clientY)));
    if ((x1 === null)) {
      throw new $c_s_MatchError(x1)
    };
    const x = x1._1$mcI$sp__I();
    const y = x1._2$mcI$sp__I();
    const id = $doubleToInt($uD(touch.identifier));
    $thiz.Input__Lsgl_InputProvider$Input$().newEvent__Lsgl_InputProvider$Input$InputEvent__V(new $c_Lsgl_InputProvider$Input$TouchMovedEvent($thiz.Input__Lsgl_InputProvider$Input$(), x, y, id))
  }
});
const $f_Lsgl_html5_Html5InputProvider__sgl$html5$Html5InputProvider$$$anonfun$registerInputListeners$7__Lorg_scalajs_dom_raw_KeyboardEvent__V = (function($thiz, e) {
  $p_Lsgl_html5_Html5InputProvider__triggerUserInteraction__V($thiz);
  const this$1 = $p_Lsgl_html5_Html5InputProvider__domEventToKey__Lorg_scalajs_dom_raw_KeyboardEvent__s_Option($thiz, e);
  if ((!this$1.isEmpty__Z())) {
    const arg1 = this$1.get__O();
    const key = $as_Lsgl_InputProvider$Input$Keys$Key(arg1);
    $thiz.Input__Lsgl_InputProvider$Input$().newEvent__Lsgl_InputProvider$Input$InputEvent__V(new $c_Lsgl_InputProvider$Input$KeyDownEvent($thiz.Input__Lsgl_InputProvider$Input$(), key))
  }
});
const $f_Lsgl_html5_Html5InputProvider__sgl$html5$Html5InputProvider$$$anonfun$registerInputListeners$10__Lorg_scalajs_dom_raw_KeyboardEvent__V = (function($thiz, e) {
  $p_Lsgl_html5_Html5InputProvider__triggerUserInteraction__V($thiz);
  const this$1 = $p_Lsgl_html5_Html5InputProvider__domEventToKey__Lorg_scalajs_dom_raw_KeyboardEvent__s_Option($thiz, e);
  if ((!this$1.isEmpty__Z())) {
    const arg1 = this$1.get__O();
    const key = $as_Lsgl_InputProvider$Input$Keys$Key(arg1);
    $thiz.Input__Lsgl_InputProvider$Input$().newEvent__Lsgl_InputProvider$Input$InputEvent__V(new $c_Lsgl_InputProvider$Input$KeyUpEvent($thiz.Input__Lsgl_InputProvider$Input$(), key))
  }
});
const $f_Lsgl_html5_Html5InputProvider__$init$__V = (function($thiz) {
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_ProcessInputsDuringUpdate = false;
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_sgl$html5$Html5InputProvider$$hasUserInteracted = false;
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_sgl$html5$Html5InputProvider$$actionsOnUserInteraction = new $c_scm_ListBuffer()
});
class $c_Lsgl_html5_Html5SystemProvider$Html5System$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_html5_Html5SystemProvider$Html5System$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_html5_Html5SystemProvider$Html5System$__f_$outer = outer
    }
  };
  nanoTime__J() {
    const this$1 = $m_RTLong$();
    const value = (1000.0 * (1000.0 * $uD($m_Lorg_scalajs_dom_package$().window__Lorg_scalajs_dom_raw_Window().performance.now())));
    const lo = this$1.org$scalajs$linker$runtime$RuntimeLong$$fromDoubleImpl__D__I(value);
    const hi = this$1.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn;
    return new $c_RTLong(lo, hi)
  };
}
const $d_Lsgl_html5_Html5SystemProvider$Html5System$ = new $TypeData().initClass({
  Lsgl_html5_Html5SystemProvider$Html5System$: 0
}, false, "sgl.html5.Html5SystemProvider$Html5System$", {
  Lsgl_html5_Html5SystemProvider$Html5System$: 1,
  O: 1,
  Lsgl_SystemProvider$System: 1
});
$c_Lsgl_html5_Html5SystemProvider$Html5System$.prototype.$classData = $d_Lsgl_html5_Html5SystemProvider$Html5System$;
const $f_Lsgl_html5_Html5WindowProvider__$init$__V = (function($thiz) {
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_Window = new $c_Lsgl_html5_Html5WindowProvider$Html5Window($as_Lsgl_html5_Html5App($thiz))
});
class $c_Lsgl_html5_Html5WindowProvider$Html5Window extends $c_Lsgl_WindowProvider$AbstractWindow {
  constructor(outer) {
    super();
    $ct_Lsgl_WindowProvider$AbstractWindow__Lsgl_WindowProvider__(this, outer)
  };
  width__I() {
    return $uI($as_Lsgl_html5_Html5App(this.Lsgl_WindowProvider$AbstractWindow__f_$outer).Lcom_regblanc_scalavator_html5_Main$__f_htmlCanvas.width)
  };
  height__I() {
    return $uI($as_Lsgl_html5_Html5App(this.Lsgl_WindowProvider$AbstractWindow__f_$outer).Lcom_regblanc_scalavator_html5_Main$__f_htmlCanvas.height)
  };
}
const $d_Lsgl_html5_Html5WindowProvider$Html5Window = new $TypeData().initClass({
  Lsgl_html5_Html5WindowProvider$Html5Window: 0
}, false, "sgl.html5.Html5WindowProvider$Html5Window", {
  Lsgl_html5_Html5WindowProvider$Html5Window: 1,
  Lsgl_WindowProvider$AbstractWindow: 1,
  O: 1
});
$c_Lsgl_html5_Html5WindowProvider$Html5Window.prototype.$classData = $d_Lsgl_html5_Html5WindowProvider$Html5Window;
class $c_Lsgl_html5_themes_NoTheme extends $c_Lsgl_html5_themes_Theme {
}
const $d_Lsgl_html5_themes_NoTheme = new $TypeData().initClass({
  Lsgl_html5_themes_NoTheme: 0
}, false, "sgl.html5.themes.NoTheme", {
  Lsgl_html5_themes_NoTheme: 1,
  Lsgl_html5_themes_Theme: 1,
  O: 1
});
$c_Lsgl_html5_themes_NoTheme.prototype.$classData = $d_Lsgl_html5_themes_NoTheme;
function $is_Lsgl_html5_util_Html5ConsoleLoggingProvider(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.Lsgl_html5_util_Html5ConsoleLoggingProvider)))
}
function $as_Lsgl_html5_util_Html5ConsoleLoggingProvider(obj) {
  return (($is_Lsgl_html5_util_Html5ConsoleLoggingProvider(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.html5.util.Html5ConsoleLoggingProvider"))
}
function $isArrayOf_Lsgl_html5_util_Html5ConsoleLoggingProvider(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_html5_util_Html5ConsoleLoggingProvider)))
}
function $asArrayOf_Lsgl_html5_util_Html5ConsoleLoggingProvider(obj, depth) {
  return (($isArrayOf_Lsgl_html5_util_Html5ConsoleLoggingProvider(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.html5.util.Html5ConsoleLoggingProvider;", depth))
}
const $p_Lsgl_html5_util_Html5ConsoleLoggingProvider$ConsoleLogger__format__Lsgl_util_LoggingProvider$Logger$Tag__T__T = (function($thiz, tag, msg) {
  const prefix = (("[ " + tag.Lsgl_util_LoggingProvider$Logger$Tag__f_name) + " ]");
  const $$x1 = $as_T(msg.trim());
  const this$2 = new $c_sci_StringOps(" ");
  const n = $uI(prefix.length);
  const alignedMsg = $f_T__replaceAll__T__T__T($$x1, "\n", ("\n" + $f_sci_StringLike__$times__I__T(this$2, n)));
  return ((prefix + " ") + alignedMsg)
});
class $c_Lsgl_html5_util_Html5ConsoleLoggingProvider$ConsoleLogger extends $c_Lsgl_util_LoggingProvider$Logger {
  log__Lsgl_util_LoggingProvider$Logger$LogLevel__Lsgl_util_LoggingProvider$Logger$Tag__T__V(level, tag, msg) {
    const x = $as_Lsgl_html5_util_Html5ConsoleLoggingProvider(this.Lsgl_util_LoggingProvider$Logger__f_$outer).Logger__Lsgl_util_LoggingProvider$Logger$().NoLogging__Lsgl_util_LoggingProvider$Logger$NoLogging$();
    if ((!(x === level))) {
      const x$3 = $as_Lsgl_html5_util_Html5ConsoleLoggingProvider(this.Lsgl_util_LoggingProvider$Logger__f_$outer).Logger__Lsgl_util_LoggingProvider$Logger$().Error__Lsgl_util_LoggingProvider$Logger$Error$();
      if ((x$3 === level)) {
        $m_Lorg_scalajs_dom_package$().console__Lorg_scalajs_dom_raw_Console().error($p_Lsgl_html5_util_Html5ConsoleLoggingProvider$ConsoleLogger__format__Lsgl_util_LoggingProvider$Logger$Tag__T__T(this, tag, msg))
      } else {
        const x$5 = $as_Lsgl_html5_util_Html5ConsoleLoggingProvider(this.Lsgl_util_LoggingProvider$Logger__f_$outer).Logger__Lsgl_util_LoggingProvider$Logger$().Warning__Lsgl_util_LoggingProvider$Logger$Warning$();
        if ((x$5 === level)) {
          $m_Lorg_scalajs_dom_package$().console__Lorg_scalajs_dom_raw_Console().warn($p_Lsgl_html5_util_Html5ConsoleLoggingProvider$ConsoleLogger__format__Lsgl_util_LoggingProvider$Logger$Tag__T__T(this, tag, msg))
        } else {
          const x$7 = $as_Lsgl_html5_util_Html5ConsoleLoggingProvider(this.Lsgl_util_LoggingProvider$Logger__f_$outer).Logger__Lsgl_util_LoggingProvider$Logger$().Info__Lsgl_util_LoggingProvider$Logger$Info$();
          if ((x$7 === level)) {
            $m_Lorg_scalajs_dom_package$().console__Lorg_scalajs_dom_raw_Console().info($p_Lsgl_html5_util_Html5ConsoleLoggingProvider$ConsoleLogger__format__Lsgl_util_LoggingProvider$Logger$Tag__T__T(this, tag, msg))
          } else {
            const x$9 = $as_Lsgl_html5_util_Html5ConsoleLoggingProvider(this.Lsgl_util_LoggingProvider$Logger__f_$outer).Logger__Lsgl_util_LoggingProvider$Logger$().Debug__Lsgl_util_LoggingProvider$Logger$Debug$();
            if ((x$9 === level)) {
              $m_Lorg_scalajs_dom_package$().console__Lorg_scalajs_dom_raw_Console().log($p_Lsgl_html5_util_Html5ConsoleLoggingProvider$ConsoleLogger__format__Lsgl_util_LoggingProvider$Logger$Tag__T__T(this, tag, msg))
            } else {
              const x$11 = $as_Lsgl_html5_util_Html5ConsoleLoggingProvider(this.Lsgl_util_LoggingProvider$Logger__f_$outer).Logger__Lsgl_util_LoggingProvider$Logger$().Trace__Lsgl_util_LoggingProvider$Logger$Trace$();
              if ((x$11 === level)) {
                $m_Lorg_scalajs_dom_package$().console__Lorg_scalajs_dom_raw_Console().log($p_Lsgl_html5_util_Html5ConsoleLoggingProvider$ConsoleLogger__format__Lsgl_util_LoggingProvider$Logger$Tag__T__T(this, tag, msg))
              } else {
                throw new $c_s_MatchError(level)
              }
            }
          }
        }
      }
    }
  };
}
const $f_Lsgl_util_SingleThreadSchedulerProvider__$init$__V = (function($thiz) {
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_sgl$util$SingleThreadSchedulerProvider$$Tag = new $c_Lsgl_util_LoggingProvider$Logger$Tag($as_Lsgl_util_LoggingProvider($thiz).Logger__Lsgl_util_LoggingProvider$Logger$(), "single-thread-scheduler");
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_Scheduler = new $c_Lsgl_util_SingleThreadSchedulerProvider$SingleThreadScheduler($thiz)
});
function $is_Lsgl_util_SingleThreadSchedulerProvider(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.Lsgl_util_SingleThreadSchedulerProvider)))
}
function $as_Lsgl_util_SingleThreadSchedulerProvider(obj) {
  return (($is_Lsgl_util_SingleThreadSchedulerProvider(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.util.SingleThreadSchedulerProvider"))
}
function $isArrayOf_Lsgl_util_SingleThreadSchedulerProvider(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_util_SingleThreadSchedulerProvider)))
}
function $asArrayOf_Lsgl_util_SingleThreadSchedulerProvider(obj, depth) {
  return (($isArrayOf_Lsgl_util_SingleThreadSchedulerProvider(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.util.SingleThreadSchedulerProvider;", depth))
}
class $c_Lsgl_util_SingleThreadSchedulerProvider$SingleThreadScheduler extends $c_Lsgl_util_SchedulerProvider$Scheduler {
  constructor(outer) {
    super();
    this.Lsgl_util_SingleThreadSchedulerProvider$SingleThreadScheduler__f_taskQueue = null;
    $ct_Lsgl_util_SchedulerProvider$Scheduler__Lsgl_util_SchedulerProvider__(this, outer);
    this.Lsgl_util_SingleThreadSchedulerProvider$SingleThreadScheduler__f_taskQueue = $ct_scm_Queue__(new $c_scm_Queue())
  };
  run__J__Z(ms) {
    const this$1 = $as_Lsgl_util_LoggingProvider($as_Lsgl_util_SingleThreadSchedulerProvider(this.Lsgl_util_SchedulerProvider$Scheduler__f_$outer));
    const this$2 = this$1.Lcom_regblanc_scalavator_html5_Main$__f_logger;
    const tag = $as_Lsgl_util_SingleThreadSchedulerProvider(this.Lsgl_util_SchedulerProvider$Scheduler__f_$outer).Lcom_regblanc_scalavator_html5_Main$__f_sgl$util$SingleThreadSchedulerProvider$$Tag;
    const this$3 = this$2.Lsgl_html5_util_Html5VerboseConsoleLoggingProvider$VerboseConsoleLogger$__f_logLevel;
    const that = this$2.Lsgl_util_LoggingProvider$Logger__f_$outer.Logger__Lsgl_util_LoggingProvider$Logger$().Trace__Lsgl_util_LoggingProvider$Logger$Trace$();
    if ($f_s_math_Ordered__$greater$eq__O__Z(this$3, that)) {
      const $$x1 = this$2.Lsgl_util_LoggingProvider$Logger__f_$outer.Logger__Lsgl_util_LoggingProvider$Logger$().Trace__Lsgl_util_LoggingProvider$Logger$Trace$();
      const this$4 = this.Lsgl_util_SingleThreadSchedulerProvider$SingleThreadScheduler__f_taskQueue;
      this$2.log__Lsgl_util_LoggingProvider$Logger$LogLevel__Lsgl_util_LoggingProvider$Logger$Tag__T__V($$x1, tag, ("Running SingleThreadScheduler with taskQueue size of: " + this$4.scm_MutableList__f_len))
    };
    const this$5 = $as_Lsgl_SystemProvider($as_Lsgl_util_SingleThreadSchedulerProvider(this.Lsgl_util_SchedulerProvider$Scheduler__f_$outer));
    const t = this$5.Lcom_regblanc_scalavator_html5_Main$__f_System.nanoTime__J();
    const lo$2 = t.RTLong__f_lo;
    const hi$2 = t.RTLong__f_hi;
    const blo = ms.RTLong__f_lo;
    const b0 = (65535 & blo);
    const b1 = ((blo >>> 16) | 0);
    const a0b0 = $imul(1000, b0);
    const a0b1 = $imul(1000, b1);
    const lo = ((a0b0 + (a0b1 << 16)) | 0);
    const c1part = ((((a0b0 >>> 16) | 0) + a0b1) | 0);
    const hi = (($imul(1000, ms.RTLong__f_hi) + ((c1part >>> 16) | 0)) | 0);
    const b0$1 = (65535 & lo);
    const b1$1 = ((lo >>> 16) | 0);
    const a0b0$1 = $imul(1000, b0$1);
    const a0b1$1 = $imul(1000, b1$1);
    const lo$1 = ((a0b0$1 + (a0b1$1 << 16)) | 0);
    const c1part$1 = ((((a0b0$1 >>> 16) | 0) + a0b1$1) | 0);
    const hi$1 = (($imul(1000, hi) + ((c1part$1 >>> 16) | 0)) | 0);
    const lo$3 = ((lo$2 + lo$1) | 0);
    const hi$3 = ((((-2147483648) ^ lo$3) < ((-2147483648) ^ lo$2)) ? ((1 + ((hi$2 + hi$1) | 0)) | 0) : ((hi$2 + hi$1) | 0));
    const this$7 = $as_Lsgl_SystemProvider($as_Lsgl_util_SingleThreadSchedulerProvider(this.Lsgl_util_SchedulerProvider$Scheduler__f_$outer));
    const b = this$7.Lcom_regblanc_scalavator_html5_Main$__f_System.nanoTime__J();
    const bhi = b.RTLong__f_hi;
    const lo$4 = ((lo$3 - b.RTLong__f_lo) | 0);
    const hi$4 = ((((-2147483648) ^ lo$4) > ((-2147483648) ^ lo$3)) ? (((-1) + ((hi$3 - bhi) | 0)) | 0) : ((hi$3 - bhi) | 0));
    let remaining__lo = lo$4;
    let remaining__hi = hi$4;
    while (true) {
      const this$9__lo = remaining__lo;
      const this$9__hi = remaining__hi;
      const ahi = this$9__hi;
      let $$x3;
      if (((ahi === 0) ? (this$9__lo !== 0) : (ahi > 0))) {
        const this$10 = this.Lsgl_util_SingleThreadSchedulerProvider$SingleThreadScheduler__f_taskQueue;
        $$x3 = (!this$10.isEmpty__Z())
      } else {
        $$x3 = false
      };
      if ($$x3) {
        const this$11__lo = remaining__lo;
        const this$11__hi = remaining__hi;
        const this$12 = $m_RTLong$();
        const lo$5 = this$12.divideImpl__I__I__I__I__I(this$11__lo, this$11__hi, 1000000, 0);
        const hi$5 = this$12.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn;
        const t$1 = (((hi$5 === 0) ? (((-2147483648) ^ lo$5) < (-2147483643)) : (hi$5 < 0)) ? new $c_RTLong(lo$5, hi$5) : new $c_RTLong(5, 0));
        const lo$6 = t$1.RTLong__f_lo;
        const hi$6 = t$1.RTLong__f_hi;
        const task = $as_Lsgl_util_ChunkedTask(this.Lsgl_util_SingleThreadSchedulerProvider$SingleThreadScheduler__f_taskQueue.dequeue__O());
        task.doRun__J__V(new $c_RTLong(lo$6, hi$6));
        const x = task.status__Lsgl_util_ChunkedTask$Status();
        const x$2 = $m_Lsgl_util_ChunkedTask$InProgress$();
        if (((x !== null) && (x === x$2))) {
          const this$19 = this.Lsgl_util_SingleThreadSchedulerProvider$SingleThreadScheduler__f_taskQueue;
          const array = [task];
          let i = 0;
          const len = $uI(array.length);
          while ((i < len)) {
            const index = i;
            const arg1 = array[index];
            this$19.$plus$eq__O__scm_MutableList(arg1);
            i = ((1 + i) | 0)
          }
        };
        const this$20 = $as_Lsgl_SystemProvider($as_Lsgl_util_SingleThreadSchedulerProvider(this.Lsgl_util_SchedulerProvider$Scheduler__f_$outer));
        const b$1 = this$20.Lcom_regblanc_scalavator_html5_Main$__f_System.nanoTime__J();
        const bhi$1 = b$1.RTLong__f_hi;
        const lo$7 = ((lo$3 - b$1.RTLong__f_lo) | 0);
        const hi$7 = ((((-2147483648) ^ lo$7) > ((-2147483648) ^ lo$3)) ? (((-1) + ((hi$3 - bhi$1) | 0)) | 0) : ((hi$3 - bhi$1) | 0));
        const $$x2__lo = lo$7;
        const $$x2__hi = hi$7;
        remaining__lo = $$x2__lo;
        remaining__hi = $$x2__hi
      } else {
        break
      }
    };
    return this.Lsgl_util_SingleThreadSchedulerProvider$SingleThreadScheduler__f_taskQueue.isEmpty__Z()
  };
}
const $d_Lsgl_util_SingleThreadSchedulerProvider$SingleThreadScheduler = new $TypeData().initClass({
  Lsgl_util_SingleThreadSchedulerProvider$SingleThreadScheduler: 0
}, false, "sgl.util.SingleThreadSchedulerProvider$SingleThreadScheduler", {
  Lsgl_util_SingleThreadSchedulerProvider$SingleThreadScheduler: 1,
  Lsgl_util_SchedulerProvider$Scheduler: 1,
  O: 1
});
$c_Lsgl_util_SingleThreadSchedulerProvider$SingleThreadScheduler.prototype.$classData = $d_Lsgl_util_SingleThreadSchedulerProvider$SingleThreadScheduler;
const $f_jl_Boolean__equals__O__Z = (function($thiz, that) {
  return ($thiz === that)
});
const $f_jl_Boolean__hashCode__I = (function($thiz) {
  return ($uZ($thiz) ? 1231 : 1237)
});
const $f_jl_Boolean__toString__T = (function($thiz) {
  const b = $uZ($thiz);
  return ("" + b)
});
const $d_jl_Boolean = new $TypeData().initClass({
  jl_Boolean: 0
}, false, "java.lang.Boolean", {
  jl_Boolean: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), ((x) => ((typeof x) === "boolean")));
const $f_jl_Character__hashCode__I = (function($thiz) {
  return $uC($thiz)
});
const $f_jl_Character__equals__O__Z = (function($thiz, that) {
  if ((that instanceof $Char)) {
    const $$x1 = $uC($thiz);
    const this$1 = $as_jl_Character(that);
    return ($$x1 === $uC(this$1))
  } else {
    return false
  }
});
const $f_jl_Character__toString__T = (function($thiz) {
  const c = $uC($thiz);
  return $as_T(String.fromCharCode(c))
});
function $as_jl_Character(obj) {
  return (((obj instanceof $Char) || (obj === null)) ? obj : $throwClassCastException(obj, "java.lang.Character"))
}
function $isArrayOf_jl_Character(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.jl_Character)))
}
function $asArrayOf_jl_Character(obj, depth) {
  return (($isArrayOf_jl_Character(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Ljava.lang.Character;", depth))
}
const $d_jl_Character = new $TypeData().initClass({
  jl_Character: 0
}, false, "java.lang.Character", {
  jl_Character: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), ((x) => (x instanceof $Char)));
class $c_jl_Error extends $c_jl_Throwable {
}
class $c_jl_Exception extends $c_jl_Throwable {
}
const $p_ju_regex_Pattern__jsPattern__T = (function($thiz) {
  return $as_T($thiz.ju_regex_Pattern__f_jsRegExp.source)
});
const $p_ju_regex_Pattern__jsFlags__T = (function($thiz) {
  return ((($uZ($thiz.ju_regex_Pattern__f_jsRegExp.global) ? "g" : "") + ($uZ($thiz.ju_regex_Pattern__f_jsRegExp.ignoreCase) ? "i" : "")) + ($uZ($thiz.ju_regex_Pattern__f_jsRegExp.multiline) ? "m" : ""))
});
class $c_ju_regex_Pattern extends $c_O {
  constructor(jsRegExp, _pattern, _flags) {
    super();
    this.ju_regex_Pattern__f_groupCount = 0;
    this.ju_regex_Pattern__f_groupStartMapper = null;
    this.ju_regex_Pattern__f_jsRegExp = null;
    this.ju_regex_Pattern__f__pattern = null;
    this.ju_regex_Pattern__f__flags = 0;
    this.ju_regex_Pattern__f_bitmap$0 = 0;
    this.ju_regex_Pattern__f_jsRegExp = jsRegExp;
    this.ju_regex_Pattern__f__pattern = _pattern;
    this.ju_regex_Pattern__f__flags = _flags
  };
  toString__T() {
    return this.ju_regex_Pattern__f__pattern
  };
  newJSRegExp__sjs_js_RegExp() {
    const r = new RegExp(this.ju_regex_Pattern__f_jsRegExp);
    return ((!Object.is(r, this.ju_regex_Pattern__f_jsRegExp)) ? r : new RegExp($p_ju_regex_Pattern__jsPattern__T(this), $p_ju_regex_Pattern__jsFlags__T(this)))
  };
}
const $d_ju_regex_Pattern = new $TypeData().initClass({
  ju_regex_Pattern: 0
}, false, "java.util.regex.Pattern", {
  ju_regex_Pattern: 1,
  O: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_ju_regex_Pattern.prototype.$classData = $d_ju_regex_Pattern;
class $c_ju_regex_Pattern$ extends $c_O {
  constructor() {
    super();
    this.ju_regex_Pattern$__f_java$util$regex$Pattern$$splitHackPat = null;
    this.ju_regex_Pattern$__f_java$util$regex$Pattern$$flagHackPat = null;
    $n_ju_regex_Pattern$ = this;
    this.ju_regex_Pattern$__f_java$util$regex$Pattern$$splitHackPat = new RegExp("^\\\\Q(.|\\n|\\r)\\\\E$");
    this.ju_regex_Pattern$__f_java$util$regex$Pattern$$flagHackPat = new RegExp("^\\(\\?([idmsuxU]*)(?:-([idmsuxU]*))?\\)")
  };
  compile__T__I__ju_regex_Pattern(regex, flags) {
    let x1;
    if (((16 & flags) !== 0)) {
      x1 = $ct_T2__O__O__(new $c_T2(), this.quote__T__T(regex), flags)
    } else {
      const m = this.ju_regex_Pattern$__f_java$util$regex$Pattern$$splitHackPat.exec(regex);
      let this$5;
      if ((m !== null)) {
        const value = m[1];
        if ((value === (void 0))) {
          throw $ct_ju_NoSuchElementException__T__(new $c_ju_NoSuchElementException(), "undefined.get")
        };
        this$5 = new $c_s_Some($ct_T2__O__O__(new $c_T2(), this.quote__T__T($as_T(value)), flags))
      } else {
        this$5 = $m_s_None$()
      };
      let this$28;
      if (this$5.isEmpty__Z()) {
        const this$6 = $m_ju_regex_Pattern$();
        const m$1 = this$6.ju_regex_Pattern$__f_java$util$regex$Pattern$$flagHackPat.exec(regex);
        if ((m$1 !== null)) {
          const value$1 = m$1[0];
          if ((value$1 === (void 0))) {
            throw $ct_ju_NoSuchElementException__T__(new $c_ju_NoSuchElementException(), "undefined.get")
          };
          const this$11 = $as_T(value$1);
          const beginIndex = $uI(this$11.length);
          const newPat = $as_T(regex.substring(beginIndex));
          let elem = 0;
          elem = flags;
          const value$2 = m$1[1];
          if ((value$2 !== (void 0))) {
            const chars = $as_T(value$2);
            const end = $uI(chars.length);
            let i = 0;
            while ((i < end)) {
              const arg1 = i;
              elem = (elem | $m_ju_regex_Pattern$().java$util$regex$Pattern$$charToFlag__C__I((65535 & $uI(chars.charCodeAt(arg1)))));
              i = ((1 + i) | 0)
            }
          };
          const value$3 = m$1[2];
          if ((value$3 !== (void 0))) {
            const chars$3 = $as_T(value$3);
            const end$1 = $uI(chars$3.length);
            let i$1 = 0;
            while ((i$1 < end$1)) {
              const arg1$1 = i$1;
              elem = (elem & (~$m_ju_regex_Pattern$().java$util$regex$Pattern$$charToFlag__C__I((65535 & $uI(chars$3.charCodeAt(arg1$1))))));
              i$1 = ((1 + i$1) | 0)
            }
          };
          this$28 = new $c_s_Some($ct_T2__O__O__(new $c_T2(), newPat, elem))
        } else {
          this$28 = $m_s_None$()
        }
      } else {
        this$28 = this$5
      };
      x1 = $as_T2((this$28.isEmpty__Z() ? $ct_T2__O__O__(new $c_T2(), regex, flags) : this$28.get__O()))
    };
    if ((x1 === null)) {
      throw new $c_s_MatchError(x1)
    };
    const jsPattern = $as_T(x1._1__O());
    const flags1 = x1._2$mcI$sp__I();
    const jsFlags = (("g" + (((2 & flags1) !== 0) ? "i" : "")) + (((8 & flags1) !== 0) ? "m" : ""));
    const jsRegExp = new RegExp(jsPattern, jsFlags);
    return new $c_ju_regex_Pattern(jsRegExp, regex, flags1)
  };
  quote__T__T(s) {
    let result = "";
    let i = 0;
    while ((i < $uI(s.length))) {
      const index = i;
      const c = (65535 & $uI(s.charCodeAt(index)));
      const $$x2 = result;
      let $$x1;
      switch (c) {
        case 92:
        case 46:
        case 40:
        case 41:
        case 91:
        case 93:
        case 123:
        case 125:
        case 124:
        case 63:
        case 42:
        case 43:
        case 94:
        case 36: {
          $$x1 = ("\\" + $bC(c));
          break
        }
        default: {
          $$x1 = $bC(c)
        }
      };
      result = (("" + $$x2) + $$x1);
      i = ((1 + i) | 0)
    };
    return result
  };
  java$util$regex$Pattern$$charToFlag__C__I(c) {
    switch (c) {
      case 105: {
        return 2;
        break
      }
      case 100: {
        return 1;
        break
      }
      case 109: {
        return 8;
        break
      }
      case 115: {
        return 32;
        break
      }
      case 117: {
        return 64;
        break
      }
      case 120: {
        return 4;
        break
      }
      case 85: {
        return 256;
        break
      }
      default: {
        throw $ct_jl_IllegalArgumentException__T__(new $c_jl_IllegalArgumentException(), "bad in-pattern flag")
      }
    }
  };
}
const $d_ju_regex_Pattern$ = new $TypeData().initClass({
  ju_regex_Pattern$: 0
}, false, "java.util.regex.Pattern$", {
  ju_regex_Pattern$: 1,
  O: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_ju_regex_Pattern$.prototype.$classData = $d_ju_regex_Pattern$;
let $n_ju_regex_Pattern$ = (void 0);
function $m_ju_regex_Pattern$() {
  if ((!$n_ju_regex_Pattern$)) {
    $n_ju_regex_Pattern$ = new $c_ju_regex_Pattern$()
  };
  return $n_ju_regex_Pattern$
}
const $f_s_Product2__productElement__I__O = (function($thiz, n) {
  switch (n) {
    case 0: {
      return $thiz._1__O();
      break
    }
    case 1: {
      return $thiz._2__O();
      break
    }
    default: {
      throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + n))
    }
  }
});
class $c_sc_IndexedSeq$$anon$1 extends $c_scg_GenTraversableFactory$GenericCanBuildFrom {
  constructor() {
    super();
    $ct_scg_GenTraversableFactory$GenericCanBuildFrom__scg_GenTraversableFactory__(this, $m_sc_IndexedSeq$())
  };
  apply__scm_Builder() {
    $m_sc_IndexedSeq$();
    $m_sci_IndexedSeq$();
    $m_sci_Vector$();
    return new $c_sci_VectorBuilder()
  };
}
const $d_sc_IndexedSeq$$anon$1 = new $TypeData().initClass({
  sc_IndexedSeq$$anon$1: 0
}, false, "scala.collection.IndexedSeq$$anon$1", {
  sc_IndexedSeq$$anon$1: 1,
  scg_GenTraversableFactory$GenericCanBuildFrom: 1,
  O: 1,
  scg_CanBuildFrom: 1
});
$c_sc_IndexedSeq$$anon$1.prototype.$classData = $d_sc_IndexedSeq$$anon$1;
const $f_sc_Iterator__foreach__F1__V = (function($thiz, f) {
  while ($thiz.hasNext__Z()) {
    f.apply__O__O($thiz.next__O())
  }
});
const $f_sc_Iterator__toStream__sci_Stream = (function($thiz) {
  if ($thiz.hasNext__Z()) {
    const hd = $thiz.next__O();
    const tl = new $c_sjsr_AnonFunction0(((this$1) => (() => this$1.toStream__sci_Stream()))($thiz));
    return new $c_sci_Stream$Cons(hd, tl)
  } else {
    $m_sci_Stream$();
    return $m_sci_Stream$Empty$()
  }
});
class $c_scg_GenSeqFactory extends $c_scg_GenTraversableFactory {
}
class $c_scg_GenTraversableFactory$$anon$1 extends $c_scg_GenTraversableFactory$GenericCanBuildFrom {
  constructor(outer) {
    super();
    this.scg_GenTraversableFactory$$anon$1__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.scg_GenTraversableFactory$$anon$1__f_$outer = outer
    };
    $ct_scg_GenTraversableFactory$GenericCanBuildFrom__scg_GenTraversableFactory__(this, outer)
  };
  apply__scm_Builder() {
    return this.scg_GenTraversableFactory$$anon$1__f_$outer.newBuilder__scm_Builder()
  };
}
const $d_scg_GenTraversableFactory$$anon$1 = new $TypeData().initClass({
  scg_GenTraversableFactory$$anon$1: 0
}, false, "scala.collection.generic.GenTraversableFactory$$anon$1", {
  scg_GenTraversableFactory$$anon$1: 1,
  scg_GenTraversableFactory$GenericCanBuildFrom: 1,
  O: 1,
  scg_CanBuildFrom: 1
});
$c_scg_GenTraversableFactory$$anon$1.prototype.$classData = $d_scg_GenTraversableFactory$$anon$1;
class $c_sci_$colon$colon$ extends $c_O {
  toString__T() {
    return "::"
  };
}
const $d_sci_$colon$colon$ = new $TypeData().initClass({
  sci_$colon$colon$: 0
}, false, "scala.collection.immutable.$colon$colon$", {
  sci_$colon$colon$: 1,
  O: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_sci_$colon$colon$.prototype.$classData = $d_sci_$colon$colon$;
let $n_sci_$colon$colon$ = (void 0);
function $m_sci_$colon$colon$() {
  if ((!$n_sci_$colon$colon$)) {
    $n_sci_$colon$colon$ = new $c_sci_$colon$colon$()
  };
  return $n_sci_$colon$colon$
}
class $c_sci_Range$ extends $c_O {
  constructor() {
    super();
    this.sci_Range$__f_MAX_PRINT = 0;
    this.sci_Range$__f_MAX_PRINT = 512
  };
}
const $d_sci_Range$ = new $TypeData().initClass({
  sci_Range$: 0
}, false, "scala.collection.immutable.Range$", {
  sci_Range$: 1,
  O: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_sci_Range$.prototype.$classData = $d_sci_Range$;
let $n_sci_Range$ = (void 0);
function $m_sci_Range$() {
  if ((!$n_sci_Range$)) {
    $n_sci_Range$ = new $c_sci_Range$()
  };
  return $n_sci_Range$
}
class $c_sci_Stream$StreamCanBuildFrom extends $c_scg_GenTraversableFactory$GenericCanBuildFrom {
  constructor() {
    super();
    $ct_scg_GenTraversableFactory$GenericCanBuildFrom__scg_GenTraversableFactory__(this, $m_sci_Stream$())
  };
}
const $d_sci_Stream$StreamCanBuildFrom = new $TypeData().initClass({
  sci_Stream$StreamCanBuildFrom: 0
}, false, "scala.collection.immutable.Stream$StreamCanBuildFrom", {
  sci_Stream$StreamCanBuildFrom: 1,
  scg_GenTraversableFactory$GenericCanBuildFrom: 1,
  O: 1,
  scg_CanBuildFrom: 1
});
$c_sci_Stream$StreamCanBuildFrom.prototype.$classData = $d_sci_Stream$StreamCanBuildFrom;
const $f_scm_Builder__sizeHint__sc_TraversableLike__V = (function($thiz, coll) {
  const x1 = coll.sizeHintIfCheap__I();
  if ((x1 !== (-1))) {
    $thiz.sizeHint__I__V(x1)
  }
});
const $f_scm_Builder__sizeHint__sc_TraversableLike__I__V = (function($thiz, coll, delta) {
  const x1 = coll.sizeHintIfCheap__I();
  if ((x1 !== (-1))) {
    $thiz.sizeHint__I__V(((x1 + delta) | 0))
  }
});
const $f_scm_Builder__sizeHintBounded__I__sc_TraversableLike__V = (function($thiz, size, boundingColl) {
  const x1 = boundingColl.sizeHintIfCheap__I();
  if ((x1 !== (-1))) {
    $thiz.sizeHint__I__V(((size < x1) ? size : x1))
  }
});
function $is_scm_Builder(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.scm_Builder)))
}
function $as_scm_Builder(obj) {
  return (($is_scm_Builder(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.mutable.Builder"))
}
function $isArrayOf_scm_Builder(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.scm_Builder)))
}
function $asArrayOf_scm_Builder(obj, depth) {
  return (($isArrayOf_scm_Builder(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.mutable.Builder;", depth))
}
class $c_scm_StringBuilder$ extends $c_O {
}
const $d_scm_StringBuilder$ = new $TypeData().initClass({
  scm_StringBuilder$: 0
}, false, "scala.collection.mutable.StringBuilder$", {
  scm_StringBuilder$: 1,
  O: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_scm_StringBuilder$.prototype.$classData = $d_scm_StringBuilder$;
let $n_scm_StringBuilder$ = (void 0);
function $m_scm_StringBuilder$() {
  if ((!$n_scm_StringBuilder$)) {
    $n_scm_StringBuilder$ = new $c_scm_StringBuilder$()
  };
  return $n_scm_StringBuilder$
}
class $c_s_math_Fractional$ extends $c_O {
}
const $d_s_math_Fractional$ = new $TypeData().initClass({
  s_math_Fractional$: 0
}, false, "scala.math.Fractional$", {
  s_math_Fractional$: 1,
  O: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_s_math_Fractional$.prototype.$classData = $d_s_math_Fractional$;
let $n_s_math_Fractional$ = (void 0);
function $m_s_math_Fractional$() {
  if ((!$n_s_math_Fractional$)) {
    $n_s_math_Fractional$ = new $c_s_math_Fractional$()
  };
  return $n_s_math_Fractional$
}
class $c_s_math_Integral$ extends $c_O {
}
const $d_s_math_Integral$ = new $TypeData().initClass({
  s_math_Integral$: 0
}, false, "scala.math.Integral$", {
  s_math_Integral$: 1,
  O: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_s_math_Integral$.prototype.$classData = $d_s_math_Integral$;
let $n_s_math_Integral$ = (void 0);
function $m_s_math_Integral$() {
  if ((!$n_s_math_Integral$)) {
    $n_s_math_Integral$ = new $c_s_math_Integral$()
  };
  return $n_s_math_Integral$
}
class $c_s_math_Numeric$ extends $c_O {
}
const $d_s_math_Numeric$ = new $TypeData().initClass({
  s_math_Numeric$: 0
}, false, "scala.math.Numeric$", {
  s_math_Numeric$: 1,
  O: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_s_math_Numeric$.prototype.$classData = $d_s_math_Numeric$;
let $n_s_math_Numeric$ = (void 0);
function $m_s_math_Numeric$() {
  if ((!$n_s_math_Numeric$)) {
    $n_s_math_Numeric$ = new $c_s_math_Numeric$()
  };
  return $n_s_math_Numeric$
}
class $c_s_math_ScalaNumber {
}
function $as_s_math_ScalaNumber(obj) {
  return (((obj instanceof $c_s_math_ScalaNumber) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.math.ScalaNumber"))
}
function $isArrayOf_s_math_ScalaNumber(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.s_math_ScalaNumber)))
}
function $asArrayOf_s_math_ScalaNumber(obj, depth) {
  return (($isArrayOf_s_math_ScalaNumber(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.math.ScalaNumber;", depth))
}
class $c_sjsr_AnonFunction0 extends $c_sr_AbstractFunction0 {
  constructor(f) {
    super();
    this.sjsr_AnonFunction0__f_f = null;
    this.sjsr_AnonFunction0__f_f = f
  };
  apply__O() {
    return (0, this.sjsr_AnonFunction0__f_f)()
  };
}
const $d_sjsr_AnonFunction0 = new $TypeData().initClass({
  sjsr_AnonFunction0: 0
}, false, "scala.scalajs.runtime.AnonFunction0", {
  sjsr_AnonFunction0: 1,
  sr_AbstractFunction0: 1,
  O: 1,
  F0: 1
});
$c_sjsr_AnonFunction0.prototype.$classData = $d_sjsr_AnonFunction0;
class $c_sjsr_AnonFunction1 extends $c_sr_AbstractFunction1 {
  constructor(f) {
    super();
    this.sjsr_AnonFunction1__f_f = null;
    this.sjsr_AnonFunction1__f_f = f
  };
  apply__O__O(arg1) {
    return (0, this.sjsr_AnonFunction1__f_f)(arg1)
  };
}
const $d_sjsr_AnonFunction1 = new $TypeData().initClass({
  sjsr_AnonFunction1: 0
}, false, "scala.scalajs.runtime.AnonFunction1", {
  sjsr_AnonFunction1: 1,
  sr_AbstractFunction1: 1,
  O: 1,
  F1: 1
});
$c_sjsr_AnonFunction1.prototype.$classData = $d_sjsr_AnonFunction1;
class $c_s_util_Either$ extends $c_O {
}
const $d_s_util_Either$ = new $TypeData().initClass({
  s_util_Either$: 0
}, false, "scala.util.Either$", {
  s_util_Either$: 1,
  O: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_s_util_Either$.prototype.$classData = $d_s_util_Either$;
let $n_s_util_Either$ = (void 0);
function $m_s_util_Either$() {
  if ((!$n_s_util_Either$)) {
    $n_s_util_Either$ = new $c_s_util_Either$()
  };
  return $n_s_util_Either$
}
class $c_s_util_Left$ extends $c_O {
  toString__T() {
    return "Left"
  };
}
const $d_s_util_Left$ = new $TypeData().initClass({
  s_util_Left$: 0
}, false, "scala.util.Left$", {
  s_util_Left$: 1,
  O: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_s_util_Left$.prototype.$classData = $d_s_util_Left$;
let $n_s_util_Left$ = (void 0);
function $m_s_util_Left$() {
  if ((!$n_s_util_Left$)) {
    $n_s_util_Left$ = new $c_s_util_Left$()
  };
  return $n_s_util_Left$
}
class $c_s_util_Right$ extends $c_O {
  toString__T() {
    return "Right"
  };
}
const $d_s_util_Right$ = new $TypeData().initClass({
  s_util_Right$: 0
}, false, "scala.util.Right$", {
  s_util_Right$: 1,
  O: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_s_util_Right$.prototype.$classData = $d_s_util_Right$;
let $n_s_util_Right$ = (void 0);
function $m_s_util_Right$() {
  if ((!$n_s_util_Right$)) {
    $n_s_util_Right$ = new $c_s_util_Right$()
  };
  return $n_s_util_Right$
}
class $c_s_util_control_NoStackTrace$ extends $c_O {
  constructor() {
    super();
    this.s_util_control_NoStackTrace$__f__noSuppression = false;
    this.s_util_control_NoStackTrace$__f__noSuppression = false
  };
}
const $d_s_util_control_NoStackTrace$ = new $TypeData().initClass({
  s_util_control_NoStackTrace$: 0
}, false, "scala.util.control.NoStackTrace$", {
  s_util_control_NoStackTrace$: 1,
  O: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_s_util_control_NoStackTrace$.prototype.$classData = $d_s_util_control_NoStackTrace$;
let $n_s_util_control_NoStackTrace$ = (void 0);
function $m_s_util_control_NoStackTrace$() {
  if ((!$n_s_util_control_NoStackTrace$)) {
    $n_s_util_control_NoStackTrace$ = new $c_s_util_control_NoStackTrace$()
  };
  return $n_s_util_control_NoStackTrace$
}
const $p_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__Html5FontCompanion$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Html5FontCompanion$module === null)) {
    $thiz.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Html5FontCompanion$module = new $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$($thiz)
  }
});
const $p_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__Html5ColorCompanion$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Html5ColorCompanion$module === null)) {
    $thiz.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Html5ColorCompanion$module = new $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5ColorCompanion$($thiz)
  }
});
const $p_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__Alignments$lzycompute$1__V = (function($thiz) {
  if (($thiz.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Alignments$module === null)) {
    $thiz.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Alignments$module = new $c_Lsgl_GraphicsProvider$Graphics$Alignments$($thiz)
  }
});
class $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Html5Bitmap$module = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Html5Font$module = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Html5FontCompanion$module = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Html5ColorCompanion$module = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Html5Paint$module = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Html5Canvas$module = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Html5TextLayout$module = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Font = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Color = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Alignments$module = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_BitmapRegion$module = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Animation$module = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_$outer = outer
    };
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Font = this.Html5FontCompanion__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$();
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Color = this.Html5ColorCompanion__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5ColorCompanion$()
  };
  Html5FontCompanion__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5FontCompanion$() {
    if ((this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Html5FontCompanion$module === null)) {
      $p_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__Html5FontCompanion$lzycompute$1__V(this)
    };
    return this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Html5FontCompanion$module
  };
  Html5ColorCompanion__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5ColorCompanion$() {
    if ((this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Html5ColorCompanion$module === null)) {
      $p_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__Html5ColorCompanion$lzycompute$1__V(this)
    };
    return this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Html5ColorCompanion$module
  };
  Alignments__Lsgl_GraphicsProvider$Graphics$Alignments$() {
    if ((this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Alignments$module === null)) {
      $p_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__Alignments$lzycompute$1__V(this)
    };
    return this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Alignments$module
  };
  defaultPaint__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint() {
    const $$x1 = this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Font.Default__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font();
    const this$1 = this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Color;
    return new $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint(this, $$x1, this$1.rgb__I__I__I__T(0, 0, 0), this.Alignments__Lsgl_GraphicsProvider$Graphics$Alignments$().Left__Lsgl_GraphicsProvider$Graphics$Alignments$Left$())
  };
}
function $as_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$(obj) {
  return (((obj instanceof $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.html5.Html5GraphicsProvider$Html5Graphics$"))
}
function $isArrayOf_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$)))
}
function $asArrayOf_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$(obj, depth) {
  return (($isArrayOf_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.html5.Html5GraphicsProvider$Html5Graphics$;", depth))
}
const $d_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$ = new $TypeData().initClass({
  Lsgl_html5_Html5GraphicsProvider$Html5Graphics$: 0
}, false, "sgl.html5.Html5GraphicsProvider$Html5Graphics$", {
  Lsgl_html5_Html5GraphicsProvider$Html5Graphics$: 1,
  O: 1,
  Lsgl_GraphicsProvider$Graphics: 1,
  Lsgl_GraphicsHelpersComponent$GraphicsExtension: 1
});
$c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$.prototype.$classData = $d_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$;
const $f_Lsgl_html5_Html5SystemProvider__$init$__V = (function($thiz) {
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_System = $thiz.Html5System__Lsgl_html5_Html5SystemProvider$Html5System$();
  const this$3 = $m_s_package$().s_package$__f_Vector;
  const array = ["static"];
  let $$x1;
  if (($uI(array.length) === 0)) {
    $$x1 = this$3.sci_Vector$__f_NIL
  } else {
    const b = new $c_sci_VectorBuilder();
    let i = 0;
    const len = $uI(array.length);
    while ((i < len)) {
      const index = i;
      const arg1 = array[index];
      b.$plus$eq__O__sci_VectorBuilder(arg1);
      i = ((1 + i) | 0)
    };
    $$x1 = b.result__sci_Vector()
  };
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_ResourcesRoot = new $c_Lsgl_PartsResourcePathProvider$PartsResourcePath($thiz, $$x1);
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_MultiDPIResourcesRoot = new $c_Lsgl_PartsResourcePathProvider$PartsResourcePath($thiz, $as_sci_Vector($m_s_package$().s_package$__f_Vector.apply__sc_Seq__sc_GenTraversable($m_sci_Nil$())))
});
const $f_Lsgl_util_LoggingProvider$Logger$LogLevel__compare__Lsgl_util_LoggingProvider$Logger$LogLevel__I = (function($thiz, ll) {
  return (($thiz.ordinal__I() - ll.ordinal__I()) | 0)
});
function $is_Lsgl_util_LoggingProvider$Logger$LogLevel(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.Lsgl_util_LoggingProvider$Logger$LogLevel)))
}
function $as_Lsgl_util_LoggingProvider$Logger$LogLevel(obj) {
  return (($is_Lsgl_util_LoggingProvider$Logger$LogLevel(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.util.LoggingProvider$Logger$LogLevel"))
}
function $isArrayOf_Lsgl_util_LoggingProvider$Logger$LogLevel(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_util_LoggingProvider$Logger$LogLevel)))
}
function $asArrayOf_Lsgl_util_LoggingProvider$Logger$LogLevel(obj, depth) {
  return (($isArrayOf_Lsgl_util_LoggingProvider$Logger$LogLevel(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.util.LoggingProvider$Logger$LogLevel;", depth))
}
const $f_jl_Byte__equals__O__Z = (function($thiz, that) {
  return Object.is($thiz, that)
});
const $f_jl_Byte__hashCode__I = (function($thiz) {
  return $uB($thiz)
});
const $f_jl_Byte__toString__T = (function($thiz) {
  const b = $uB($thiz);
  return ("" + b)
});
const $d_jl_Byte = new $TypeData().initClass({
  jl_Byte: 0
}, false, "java.lang.Byte", {
  jl_Byte: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), ((x) => $isByte(x)));
const $f_jl_Double__equals__O__Z = (function($thiz, that) {
  return Object.is($thiz, that)
});
const $f_jl_Double__hashCode__I = (function($thiz) {
  const value = $uD($thiz);
  return $m_jl_FloatingPointBits$().numberHashCode__D__I(value)
});
const $f_jl_Double__toString__T = (function($thiz) {
  const d = $uD($thiz);
  return ("" + d)
});
function $as_jl_Double(obj) {
  return ((((typeof obj) === "number") || (obj === null)) ? obj : $throwClassCastException(obj, "java.lang.Double"))
}
function $isArrayOf_jl_Double(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.jl_Double)))
}
function $asArrayOf_jl_Double(obj, depth) {
  return (($isArrayOf_jl_Double(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Ljava.lang.Double;", depth))
}
const $d_jl_Double = new $TypeData().initClass({
  jl_Double: 0
}, false, "java.lang.Double", {
  jl_Double: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), ((x) => ((typeof x) === "number")));
const $f_jl_Float__equals__O__Z = (function($thiz, that) {
  return Object.is($thiz, that)
});
const $f_jl_Float__hashCode__I = (function($thiz) {
  const value = $uF($thiz);
  return $m_jl_FloatingPointBits$().numberHashCode__D__I(value)
});
const $f_jl_Float__toString__T = (function($thiz) {
  const f = $uF($thiz);
  return ("" + f)
});
const $d_jl_Float = new $TypeData().initClass({
  jl_Float: 0
}, false, "java.lang.Float", {
  jl_Float: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), ((x) => ((typeof x) === "number")));
const $f_jl_Integer__equals__O__Z = (function($thiz, that) {
  return Object.is($thiz, that)
});
const $f_jl_Integer__hashCode__I = (function($thiz) {
  return $uI($thiz)
});
const $f_jl_Integer__toString__T = (function($thiz) {
  const i = $uI($thiz);
  return ("" + i)
});
const $d_jl_Integer = new $TypeData().initClass({
  jl_Integer: 0
}, false, "java.lang.Integer", {
  jl_Integer: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), ((x) => $isInt(x)));
const $f_jl_Long__equals__O__Z = (function($thiz, that) {
  if ((that instanceof $c_RTLong)) {
    const x2 = $as_jl_Long(that);
    const t = $uJ($thiz);
    const lo = t.RTLong__f_lo;
    const hi = t.RTLong__f_hi;
    const b = $uJ(x2);
    return ((lo === b.RTLong__f_lo) && (hi === b.RTLong__f_hi))
  } else {
    return false
  }
});
const $f_jl_Long__hashCode__I = (function($thiz) {
  const t = $uJ($thiz);
  const lo = t.RTLong__f_lo;
  const hi = t.RTLong__f_hi;
  return (lo ^ hi)
});
const $f_jl_Long__toString__T = (function($thiz) {
  const t = $uJ($thiz);
  const lo = t.RTLong__f_lo;
  const hi = t.RTLong__f_hi;
  return $m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$toString__I__I__T(lo, hi)
});
function $as_jl_Long(obj) {
  return (((obj instanceof $c_RTLong) || (obj === null)) ? obj : $throwClassCastException(obj, "java.lang.Long"))
}
function $isArrayOf_jl_Long(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.jl_Long)))
}
function $asArrayOf_jl_Long(obj, depth) {
  return (($isArrayOf_jl_Long(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Ljava.lang.Long;", depth))
}
const $d_jl_Long = new $TypeData().initClass({
  jl_Long: 0
}, false, "java.lang.Long", {
  jl_Long: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), ((x) => (x instanceof $c_RTLong)));
class $c_jl_RuntimeException extends $c_jl_Exception {
}
const $f_jl_Short__equals__O__Z = (function($thiz, that) {
  return Object.is($thiz, that)
});
const $f_jl_Short__hashCode__I = (function($thiz) {
  return $uS($thiz)
});
const $f_jl_Short__toString__T = (function($thiz) {
  const s = $uS($thiz);
  return ("" + s)
});
const $d_jl_Short = new $TypeData().initClass({
  jl_Short: 0
}, false, "java.lang.Short", {
  jl_Short: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
}, (void 0), (void 0), ((x) => $isShort(x)));
const $f_T__hashCode__I = (function($thiz) {
  let res = 0;
  let mul = 1;
  let i = (((-1) + $uI($thiz.length)) | 0);
  while ((i >= 0)) {
    const $$x1 = res;
    const index = i;
    res = (($$x1 + $imul((65535 & $uI($thiz.charCodeAt(index))), mul)) | 0);
    mul = $imul(31, mul);
    i = (((-1) + i) | 0)
  };
  return res
});
const $f_T__equals__O__Z = (function($thiz, that) {
  return ($thiz === that)
});
const $f_T__replaceAll__T__T__T = (function($thiz, regex, replacement) {
  const this$1 = $m_ju_regex_Pattern$();
  const this$2 = this$1.compile__T__I__ju_regex_Pattern(regex, 0);
  return new $c_ju_regex_Matcher(this$2, $thiz, 0, $uI($thiz.length)).replaceAll__T__T(replacement)
});
const $f_T__subSequence__I__I__jl_CharSequence = (function($thiz, beginIndex, endIndex) {
  return $as_T($thiz.substring(beginIndex, endIndex))
});
const $f_T__toString__T = (function($thiz) {
  return $thiz
});
function $as_T(obj) {
  return ((((typeof obj) === "string") || (obj === null)) ? obj : $throwClassCastException(obj, "java.lang.String"))
}
function $isArrayOf_T(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.T)))
}
function $asArrayOf_T(obj, depth) {
  return (($isArrayOf_T(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Ljava.lang.String;", depth))
}
const $d_T = new $TypeData().initClass({
  T: 0
}, false, "java.lang.String", {
  T: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1,
  jl_CharSequence: 1
}, (void 0), (void 0), ((x) => ((typeof x) === "string")));
const $ct_jl_StringBuffer__jl_StringBuilder__ = (function($thiz, builder) {
  $thiz.jl_StringBuffer__f_builder = builder;
  return $thiz
});
const $ct_jl_StringBuffer__ = (function($thiz) {
  $ct_jl_StringBuffer__jl_StringBuilder__($thiz, $ct_jl_StringBuilder__(new $c_jl_StringBuilder()));
  return $thiz
});
class $c_jl_StringBuffer extends $c_O {
  constructor() {
    super();
    this.jl_StringBuffer__f_builder = null
  };
  append__T__jl_StringBuffer(str) {
    const this$1 = this.jl_StringBuffer__f_builder;
    this$1.jl_StringBuilder__f_java$lang$StringBuilder$$content = (("" + this$1.jl_StringBuilder__f_java$lang$StringBuilder$$content) + str);
    return this
  };
  append__C__jl_StringBuffer(c) {
    const this$1 = this.jl_StringBuffer__f_builder;
    const str = $as_T(String.fromCharCode(c));
    this$1.jl_StringBuilder__f_java$lang$StringBuilder$$content = (("" + this$1.jl_StringBuilder__f_java$lang$StringBuilder$$content) + str);
    return this
  };
  subSequence__I__I__jl_CharSequence(start, end) {
    const this$1 = this.jl_StringBuffer__f_builder;
    return this$1.substring__I__I__T(start, end)
  };
  toString__T() {
    return this.jl_StringBuffer__f_builder.jl_StringBuilder__f_java$lang$StringBuilder$$content
  };
}
const $d_jl_StringBuffer = new $TypeData().initClass({
  jl_StringBuffer: 0
}, false, "java.lang.StringBuffer", {
  jl_StringBuffer: 1,
  O: 1,
  jl_CharSequence: 1,
  jl_Appendable: 1,
  Ljava_io_Serializable: 1
});
$c_jl_StringBuffer.prototype.$classData = $d_jl_StringBuffer;
const $ct_jl_StringBuilder__ = (function($thiz) {
  $thiz.jl_StringBuilder__f_java$lang$StringBuilder$$content = "";
  return $thiz
});
const $ct_jl_StringBuilder__I__ = (function($thiz, initialCapacity) {
  $ct_jl_StringBuilder__($thiz);
  if ((initialCapacity < 0)) {
    throw new $c_jl_NegativeArraySizeException()
  };
  return $thiz
});
class $c_jl_StringBuilder extends $c_O {
  constructor() {
    super();
    this.jl_StringBuilder__f_java$lang$StringBuilder$$content = null
  };
  toString__T() {
    return this.jl_StringBuilder__f_java$lang$StringBuilder$$content
  };
  length__I() {
    const this$1 = this.jl_StringBuilder__f_java$lang$StringBuilder$$content;
    return $uI(this$1.length)
  };
  charAt__I__C(index) {
    const this$1 = this.jl_StringBuilder__f_java$lang$StringBuilder$$content;
    return (65535 & $uI(this$1.charCodeAt(index)))
  };
  subSequence__I__I__jl_CharSequence(start, end) {
    return this.substring__I__I__T(start, end)
  };
  substring__I__I__T(start, end) {
    const this$1 = this.jl_StringBuilder__f_java$lang$StringBuilder$$content;
    return $as_T(this$1.substring(start, end))
  };
}
const $d_jl_StringBuilder = new $TypeData().initClass({
  jl_StringBuilder: 0
}, false, "java.lang.StringBuilder", {
  jl_StringBuilder: 1,
  O: 1,
  jl_CharSequence: 1,
  jl_Appendable: 1,
  Ljava_io_Serializable: 1
});
$c_jl_StringBuilder.prototype.$classData = $d_jl_StringBuilder;
class $c_jl_VirtualMachineError extends $c_jl_Error {
}
class $c_RTLong extends $c_jl_Number {
  constructor(lo, hi) {
    super();
    this.RTLong__f_lo = 0;
    this.RTLong__f_hi = 0;
    this.RTLong__f_lo = lo;
    this.RTLong__f_hi = hi
  };
  equals__O__Z(that) {
    if ((that instanceof $c_RTLong)) {
      const x2 = $as_RTLong(that);
      return ((this.RTLong__f_lo === x2.RTLong__f_lo) && (this.RTLong__f_hi === x2.RTLong__f_hi))
    } else {
      return false
    }
  };
  hashCode__I() {
    return (this.RTLong__f_lo ^ this.RTLong__f_hi)
  };
  toString__T() {
    return $m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$toString__I__I__T(this.RTLong__f_lo, this.RTLong__f_hi)
  };
  toInt__I() {
    return this.RTLong__f_lo
  };
  toDouble__D() {
    return $m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$toDouble__I__I__D(this.RTLong__f_lo, this.RTLong__f_hi)
  };
  byteValue__B() {
    return ((this.RTLong__f_lo << 24) >> 24)
  };
  shortValue__S() {
    return ((this.RTLong__f_lo << 16) >> 16)
  };
  intValue__I() {
    return this.RTLong__f_lo
  };
  longValue__J() {
    return $uJ(this)
  };
  floatValue__F() {
    return $fround($m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$toDouble__I__I__D(this.RTLong__f_lo, this.RTLong__f_hi))
  };
  doubleValue__D() {
    return $m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$toDouble__I__I__D(this.RTLong__f_lo, this.RTLong__f_hi)
  };
  compareTo__jl_Long__I(that) {
    return $m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$compare__I__I__I__I__I(this.RTLong__f_lo, this.RTLong__f_hi, that.RTLong__f_lo, that.RTLong__f_hi)
  };
  equals__RTLong__Z(b) {
    return ((this.RTLong__f_lo === b.RTLong__f_lo) && (this.RTLong__f_hi === b.RTLong__f_hi))
  };
  notEquals__RTLong__Z(b) {
    return (!((this.RTLong__f_lo === b.RTLong__f_lo) && (this.RTLong__f_hi === b.RTLong__f_hi)))
  };
  $less__RTLong__Z(b) {
    const ahi = this.RTLong__f_hi;
    const bhi = b.RTLong__f_hi;
    return ((ahi === bhi) ? (((-2147483648) ^ this.RTLong__f_lo) < ((-2147483648) ^ b.RTLong__f_lo)) : (ahi < bhi))
  };
  $less$eq__RTLong__Z(b) {
    const ahi = this.RTLong__f_hi;
    const bhi = b.RTLong__f_hi;
    return ((ahi === bhi) ? (((-2147483648) ^ this.RTLong__f_lo) <= ((-2147483648) ^ b.RTLong__f_lo)) : (ahi < bhi))
  };
  $greater__RTLong__Z(b) {
    const ahi = this.RTLong__f_hi;
    const bhi = b.RTLong__f_hi;
    return ((ahi === bhi) ? (((-2147483648) ^ this.RTLong__f_lo) > ((-2147483648) ^ b.RTLong__f_lo)) : (ahi > bhi))
  };
  $greater$eq__RTLong__Z(b) {
    const ahi = this.RTLong__f_hi;
    const bhi = b.RTLong__f_hi;
    return ((ahi === bhi) ? (((-2147483648) ^ this.RTLong__f_lo) >= ((-2147483648) ^ b.RTLong__f_lo)) : (ahi > bhi))
  };
  unary_$tilde__RTLong() {
    return new $c_RTLong((~this.RTLong__f_lo), (~this.RTLong__f_hi))
  };
  $bar__RTLong__RTLong(b) {
    return new $c_RTLong((this.RTLong__f_lo | b.RTLong__f_lo), (this.RTLong__f_hi | b.RTLong__f_hi))
  };
  $amp__RTLong__RTLong(b) {
    return new $c_RTLong((this.RTLong__f_lo & b.RTLong__f_lo), (this.RTLong__f_hi & b.RTLong__f_hi))
  };
  $up__RTLong__RTLong(b) {
    return new $c_RTLong((this.RTLong__f_lo ^ b.RTLong__f_lo), (this.RTLong__f_hi ^ b.RTLong__f_hi))
  };
  $less$less__I__RTLong(n) {
    return new $c_RTLong((((32 & n) === 0) ? (this.RTLong__f_lo << n) : 0), (((32 & n) === 0) ? (((((this.RTLong__f_lo >>> 1) | 0) >>> ((31 - n) | 0)) | 0) | (this.RTLong__f_hi << n)) : (this.RTLong__f_lo << n)))
  };
  $greater$greater$greater__I__RTLong(n) {
    return new $c_RTLong((((32 & n) === 0) ? (((this.RTLong__f_lo >>> n) | 0) | ((this.RTLong__f_hi << 1) << ((31 - n) | 0))) : ((this.RTLong__f_hi >>> n) | 0)), (((32 & n) === 0) ? ((this.RTLong__f_hi >>> n) | 0) : 0))
  };
  $greater$greater__I__RTLong(n) {
    return new $c_RTLong((((32 & n) === 0) ? (((this.RTLong__f_lo >>> n) | 0) | ((this.RTLong__f_hi << 1) << ((31 - n) | 0))) : (this.RTLong__f_hi >> n)), (((32 & n) === 0) ? (this.RTLong__f_hi >> n) : (this.RTLong__f_hi >> 31)))
  };
  unary_$minus__RTLong() {
    const lo = this.RTLong__f_lo;
    const hi = this.RTLong__f_hi;
    return new $c_RTLong(((-lo) | 0), ((lo !== 0) ? (~hi) : ((-hi) | 0)))
  };
  $plus__RTLong__RTLong(b) {
    const alo = this.RTLong__f_lo;
    const ahi = this.RTLong__f_hi;
    const bhi = b.RTLong__f_hi;
    const lo = ((alo + b.RTLong__f_lo) | 0);
    return new $c_RTLong(lo, ((((-2147483648) ^ lo) < ((-2147483648) ^ alo)) ? ((1 + ((ahi + bhi) | 0)) | 0) : ((ahi + bhi) | 0)))
  };
  $minus__RTLong__RTLong(b) {
    const alo = this.RTLong__f_lo;
    const ahi = this.RTLong__f_hi;
    const bhi = b.RTLong__f_hi;
    const lo = ((alo - b.RTLong__f_lo) | 0);
    return new $c_RTLong(lo, ((((-2147483648) ^ lo) > ((-2147483648) ^ alo)) ? (((-1) + ((ahi - bhi) | 0)) | 0) : ((ahi - bhi) | 0)))
  };
  $times__RTLong__RTLong(b) {
    const alo = this.RTLong__f_lo;
    const blo = b.RTLong__f_lo;
    const a0 = (65535 & alo);
    const a1 = ((alo >>> 16) | 0);
    const b0 = (65535 & blo);
    const b1 = ((blo >>> 16) | 0);
    const a0b0 = $imul(a0, b0);
    const a1b0 = $imul(a1, b0);
    const a0b1 = $imul(a0, b1);
    const lo = ((a0b0 + (((a1b0 + a0b1) | 0) << 16)) | 0);
    const c1part = ((((a0b0 >>> 16) | 0) + a0b1) | 0);
    const hi = (((((((($imul(alo, b.RTLong__f_hi) + $imul(this.RTLong__f_hi, blo)) | 0) + $imul(a1, b1)) | 0) + ((c1part >>> 16) | 0)) | 0) + (((((65535 & c1part) + a1b0) | 0) >>> 16) | 0)) | 0);
    return new $c_RTLong(lo, hi)
  };
  $div__RTLong__RTLong(b) {
    const this$1 = $m_RTLong$();
    const lo = this$1.divideImpl__I__I__I__I__I(this.RTLong__f_lo, this.RTLong__f_hi, b.RTLong__f_lo, b.RTLong__f_hi);
    return new $c_RTLong(lo, this$1.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn)
  };
  $percent__RTLong__RTLong(b) {
    const this$1 = $m_RTLong$();
    const lo = this$1.remainderImpl__I__I__I__I__I(this.RTLong__f_lo, this.RTLong__f_hi, b.RTLong__f_lo, b.RTLong__f_hi);
    return new $c_RTLong(lo, this$1.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn)
  };
  compareTo__O__I(x$1) {
    const that = $as_jl_Long(x$1);
    return $m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$compare__I__I__I__I__I(this.RTLong__f_lo, this.RTLong__f_hi, that.RTLong__f_lo, that.RTLong__f_hi)
  };
}
function $as_RTLong(obj) {
  return (((obj instanceof $c_RTLong) || (obj === null)) ? obj : $throwClassCastException(obj, "org.scalajs.linker.runtime.RuntimeLong"))
}
function $isArrayOf_RTLong(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.RTLong)))
}
function $asArrayOf_RTLong(obj, depth) {
  return (($isArrayOf_RTLong(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lorg.scalajs.linker.runtime.RuntimeLong;", depth))
}
const $d_RTLong = new $TypeData().initClass({
  RTLong: 0
}, false, "org.scalajs.linker.runtime.RuntimeLong", {
  RTLong: 1,
  jl_Number: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  jl_Comparable: 1
});
$c_RTLong.prototype.$classData = $d_RTLong;
const $p_s_Array$__slowcopy__O__I__O__I__I__V = (function($thiz, src, srcPos, dest, destPos, length) {
  let i = srcPos;
  let j = destPos;
  const srcUntil = ((srcPos + length) | 0);
  while ((i < srcUntil)) {
    $m_sr_ScalaRunTime$().array_update__O__I__O__V(dest, j, $m_sr_ScalaRunTime$().array_apply__O__I__O(src, i));
    i = ((1 + i) | 0);
    j = ((1 + j) | 0)
  }
});
class $c_s_Array$ extends $c_s_FallbackArrayBuilding {
  copy__O__I__O__I__I__V(src, srcPos, dest, destPos, length) {
    const srcClass = $objectGetClass(src);
    if ((srcClass.isArray__Z() && $objectGetClass(dest).isAssignableFrom__jl_Class__Z(srcClass))) {
      $systemArraycopy(src, srcPos, dest, destPos, length)
    } else {
      $p_s_Array$__slowcopy__O__I__O__I__I__V(this, src, srcPos, dest, destPos, length)
    }
  };
}
const $d_s_Array$ = new $TypeData().initClass({
  s_Array$: 0
}, false, "scala.Array$", {
  s_Array$: 1,
  s_FallbackArrayBuilding: 1,
  O: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_s_Array$.prototype.$classData = $d_s_Array$;
let $n_s_Array$ = (void 0);
function $m_s_Array$() {
  if ((!$n_s_Array$)) {
    $n_s_Array$ = new $c_s_Array$()
  };
  return $n_s_Array$
}
class $c_sc_AbstractIterator extends $c_O {
  foreach__F1__V(f) {
    $f_sc_Iterator__foreach__F1__V(this, f)
  };
  toStream__sci_Stream() {
    return $f_sc_Iterator__toStream__sci_Stream(this)
  };
  toString__T() {
    return "<iterator>"
  };
  size__I() {
    return $f_sc_TraversableOnce__size__I(this)
  };
  toList__sci_List() {
    const this$1 = $m_sci_List$();
    const cbf = this$1.scg_GenTraversableFactory__f_ReusableCBFInstance;
    return $as_sci_List($f_sc_TraversableOnce__to__scg_CanBuildFrom__O(this, cbf))
  };
  addString__scm_StringBuilder__T__T__T__scm_StringBuilder(b, start, sep, end) {
    return $f_sc_TraversableOnce__addString__scm_StringBuilder__T__T__T__scm_StringBuilder(this, b, start, sep, end)
  };
  seq__sc_TraversableOnce() {
    return this
  };
}
class $c_scm_GrowingBuilder extends $c_O {
  constructor(empty) {
    super();
    this.scm_GrowingBuilder__f_empty = null;
    this.scm_GrowingBuilder__f_elems = null;
    this.scm_GrowingBuilder__f_empty = empty;
    this.scm_GrowingBuilder__f_elems = empty
  };
  sizeHint__I__V(size) {
    /*<skip>*/
  };
  sizeHintBounded__I__sc_TraversableLike__V(size, boundingColl) {
    $f_scm_Builder__sizeHintBounded__I__sc_TraversableLike__V(this, size, boundingColl)
  };
  $plus$plus$eq__sc_TraversableOnce__scg_Growable(xs) {
    return $f_scg_Growable__$plus$plus$eq__sc_TraversableOnce__scg_Growable(this, xs)
  };
  $plus$eq__O__scm_GrowingBuilder(x) {
    this.scm_GrowingBuilder__f_elems.$plus$eq__O__scg_Growable(x);
    return this
  };
  result__O() {
    return this.scm_GrowingBuilder__f_elems
  };
  $plus$eq__O__scg_Growable(elem) {
    return this.$plus$eq__O__scm_GrowingBuilder(elem)
  };
  $plus$eq__O__scm_Builder(elem) {
    return this.$plus$eq__O__scm_GrowingBuilder(elem)
  };
}
const $d_scm_GrowingBuilder = new $TypeData().initClass({
  scm_GrowingBuilder: 0
}, false, "scala.collection.mutable.GrowingBuilder", {
  scm_GrowingBuilder: 1,
  O: 1,
  scm_Builder: 1,
  scg_Growable: 1,
  scg_Clearable: 1
});
$c_scm_GrowingBuilder.prototype.$classData = $d_scm_GrowingBuilder;
class $c_s_math_Equiv$ extends $c_O {
}
const $d_s_math_Equiv$ = new $TypeData().initClass({
  s_math_Equiv$: 0
}, false, "scala.math.Equiv$", {
  s_math_Equiv$: 1,
  O: 1,
  s_math_LowPriorityEquiv: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_s_math_Equiv$.prototype.$classData = $d_s_math_Equiv$;
let $n_s_math_Equiv$ = (void 0);
function $m_s_math_Equiv$() {
  if ((!$n_s_math_Equiv$)) {
    $n_s_math_Equiv$ = new $c_s_math_Equiv$()
  };
  return $n_s_math_Equiv$
}
class $c_s_math_Ordering$ extends $c_O {
}
const $d_s_math_Ordering$ = new $TypeData().initClass({
  s_math_Ordering$: 0
}, false, "scala.math.Ordering$", {
  s_math_Ordering$: 1,
  O: 1,
  s_math_LowPriorityOrderingImplicits: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_s_math_Ordering$.prototype.$classData = $d_s_math_Ordering$;
let $n_s_math_Ordering$ = (void 0);
function $m_s_math_Ordering$() {
  if ((!$n_s_math_Ordering$)) {
    $n_s_math_Ordering$ = new $c_s_math_Ordering$()
  };
  return $n_s_math_Ordering$
}
class $c_Lsgl_util_LoggingProvider$Logger$Debug$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_util_LoggingProvider$Logger$Debug$__f_ordinal = 0;
    this.Lsgl_util_LoggingProvider$Logger$Debug$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_util_LoggingProvider$Logger$Debug$__f_$outer = outer
    };
    this.Lsgl_util_LoggingProvider$Logger$Debug$__f_ordinal = 3
  };
  ordinal__I() {
    return this.Lsgl_util_LoggingProvider$Logger$Debug$__f_ordinal
  };
  compare__O__I(that) {
    const ll = $as_Lsgl_util_LoggingProvider$Logger$LogLevel(that);
    return $f_Lsgl_util_LoggingProvider$Logger$LogLevel__compare__Lsgl_util_LoggingProvider$Logger$LogLevel__I(this, ll)
  };
}
const $d_Lsgl_util_LoggingProvider$Logger$Debug$ = new $TypeData().initClass({
  Lsgl_util_LoggingProvider$Logger$Debug$: 0
}, false, "sgl.util.LoggingProvider$Logger$Debug$", {
  Lsgl_util_LoggingProvider$Logger$Debug$: 1,
  O: 1,
  Lsgl_util_LoggingProvider$Logger$LogLevel: 1,
  s_math_Ordered: 1,
  jl_Comparable: 1
});
$c_Lsgl_util_LoggingProvider$Logger$Debug$.prototype.$classData = $d_Lsgl_util_LoggingProvider$Logger$Debug$;
class $c_Lsgl_util_LoggingProvider$Logger$Error$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_util_LoggingProvider$Logger$Error$__f_ordinal = 0;
    this.Lsgl_util_LoggingProvider$Logger$Error$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_util_LoggingProvider$Logger$Error$__f_$outer = outer
    };
    this.Lsgl_util_LoggingProvider$Logger$Error$__f_ordinal = 0
  };
  ordinal__I() {
    return this.Lsgl_util_LoggingProvider$Logger$Error$__f_ordinal
  };
  compare__O__I(that) {
    const ll = $as_Lsgl_util_LoggingProvider$Logger$LogLevel(that);
    return $f_Lsgl_util_LoggingProvider$Logger$LogLevel__compare__Lsgl_util_LoggingProvider$Logger$LogLevel__I(this, ll)
  };
}
const $d_Lsgl_util_LoggingProvider$Logger$Error$ = new $TypeData().initClass({
  Lsgl_util_LoggingProvider$Logger$Error$: 0
}, false, "sgl.util.LoggingProvider$Logger$Error$", {
  Lsgl_util_LoggingProvider$Logger$Error$: 1,
  O: 1,
  Lsgl_util_LoggingProvider$Logger$LogLevel: 1,
  s_math_Ordered: 1,
  jl_Comparable: 1
});
$c_Lsgl_util_LoggingProvider$Logger$Error$.prototype.$classData = $d_Lsgl_util_LoggingProvider$Logger$Error$;
class $c_Lsgl_util_LoggingProvider$Logger$Info$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_util_LoggingProvider$Logger$Info$__f_ordinal = 0;
    this.Lsgl_util_LoggingProvider$Logger$Info$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_util_LoggingProvider$Logger$Info$__f_$outer = outer
    };
    this.Lsgl_util_LoggingProvider$Logger$Info$__f_ordinal = 2
  };
  ordinal__I() {
    return this.Lsgl_util_LoggingProvider$Logger$Info$__f_ordinal
  };
  compare__O__I(that) {
    const ll = $as_Lsgl_util_LoggingProvider$Logger$LogLevel(that);
    return $f_Lsgl_util_LoggingProvider$Logger$LogLevel__compare__Lsgl_util_LoggingProvider$Logger$LogLevel__I(this, ll)
  };
}
const $d_Lsgl_util_LoggingProvider$Logger$Info$ = new $TypeData().initClass({
  Lsgl_util_LoggingProvider$Logger$Info$: 0
}, false, "sgl.util.LoggingProvider$Logger$Info$", {
  Lsgl_util_LoggingProvider$Logger$Info$: 1,
  O: 1,
  Lsgl_util_LoggingProvider$Logger$LogLevel: 1,
  s_math_Ordered: 1,
  jl_Comparable: 1
});
$c_Lsgl_util_LoggingProvider$Logger$Info$.prototype.$classData = $d_Lsgl_util_LoggingProvider$Logger$Info$;
class $c_Lsgl_util_LoggingProvider$Logger$NoLogging$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_util_LoggingProvider$Logger$NoLogging$__f_ordinal = 0;
    this.Lsgl_util_LoggingProvider$Logger$NoLogging$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_util_LoggingProvider$Logger$NoLogging$__f_$outer = outer
    };
    this.Lsgl_util_LoggingProvider$Logger$NoLogging$__f_ordinal = (-1)
  };
  ordinal__I() {
    return this.Lsgl_util_LoggingProvider$Logger$NoLogging$__f_ordinal
  };
  compare__O__I(that) {
    const ll = $as_Lsgl_util_LoggingProvider$Logger$LogLevel(that);
    return $f_Lsgl_util_LoggingProvider$Logger$LogLevel__compare__Lsgl_util_LoggingProvider$Logger$LogLevel__I(this, ll)
  };
}
const $d_Lsgl_util_LoggingProvider$Logger$NoLogging$ = new $TypeData().initClass({
  Lsgl_util_LoggingProvider$Logger$NoLogging$: 0
}, false, "sgl.util.LoggingProvider$Logger$NoLogging$", {
  Lsgl_util_LoggingProvider$Logger$NoLogging$: 1,
  O: 1,
  Lsgl_util_LoggingProvider$Logger$LogLevel: 1,
  s_math_Ordered: 1,
  jl_Comparable: 1
});
$c_Lsgl_util_LoggingProvider$Logger$NoLogging$.prototype.$classData = $d_Lsgl_util_LoggingProvider$Logger$NoLogging$;
class $c_Lsgl_util_LoggingProvider$Logger$Trace$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_util_LoggingProvider$Logger$Trace$__f_ordinal = 0;
    this.Lsgl_util_LoggingProvider$Logger$Trace$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_util_LoggingProvider$Logger$Trace$__f_$outer = outer
    };
    this.Lsgl_util_LoggingProvider$Logger$Trace$__f_ordinal = 4
  };
  ordinal__I() {
    return this.Lsgl_util_LoggingProvider$Logger$Trace$__f_ordinal
  };
  compare__O__I(that) {
    const ll = $as_Lsgl_util_LoggingProvider$Logger$LogLevel(that);
    return $f_Lsgl_util_LoggingProvider$Logger$LogLevel__compare__Lsgl_util_LoggingProvider$Logger$LogLevel__I(this, ll)
  };
}
const $d_Lsgl_util_LoggingProvider$Logger$Trace$ = new $TypeData().initClass({
  Lsgl_util_LoggingProvider$Logger$Trace$: 0
}, false, "sgl.util.LoggingProvider$Logger$Trace$", {
  Lsgl_util_LoggingProvider$Logger$Trace$: 1,
  O: 1,
  Lsgl_util_LoggingProvider$Logger$LogLevel: 1,
  s_math_Ordered: 1,
  jl_Comparable: 1
});
$c_Lsgl_util_LoggingProvider$Logger$Trace$.prototype.$classData = $d_Lsgl_util_LoggingProvider$Logger$Trace$;
class $c_Lsgl_util_LoggingProvider$Logger$Warning$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_util_LoggingProvider$Logger$Warning$__f_ordinal = 0;
    this.Lsgl_util_LoggingProvider$Logger$Warning$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_util_LoggingProvider$Logger$Warning$__f_$outer = outer
    };
    this.Lsgl_util_LoggingProvider$Logger$Warning$__f_ordinal = 1
  };
  ordinal__I() {
    return this.Lsgl_util_LoggingProvider$Logger$Warning$__f_ordinal
  };
  compare__O__I(that) {
    const ll = $as_Lsgl_util_LoggingProvider$Logger$LogLevel(that);
    return $f_Lsgl_util_LoggingProvider$Logger$LogLevel__compare__Lsgl_util_LoggingProvider$Logger$LogLevel__I(this, ll)
  };
}
const $d_Lsgl_util_LoggingProvider$Logger$Warning$ = new $TypeData().initClass({
  Lsgl_util_LoggingProvider$Logger$Warning$: 0
}, false, "sgl.util.LoggingProvider$Logger$Warning$", {
  Lsgl_util_LoggingProvider$Logger$Warning$: 1,
  O: 1,
  Lsgl_util_LoggingProvider$Logger$LogLevel: 1,
  s_math_Ordered: 1,
  jl_Comparable: 1
});
$c_Lsgl_util_LoggingProvider$Logger$Warning$.prototype.$classData = $d_Lsgl_util_LoggingProvider$Logger$Warning$;
class $c_jl_ArithmeticException extends $c_jl_RuntimeException {
  constructor(s) {
    super();
    $ct_jl_Throwable__T__jl_Throwable__Z__Z__(this, s, null, true, true)
  };
}
const $d_jl_ArithmeticException = new $TypeData().initClass({
  jl_ArithmeticException: 0
}, false, "java.lang.ArithmeticException", {
  jl_ArithmeticException: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_jl_ArithmeticException.prototype.$classData = $d_jl_ArithmeticException;
class $c_jl_ClassCastException extends $c_jl_RuntimeException {
  constructor(s) {
    super();
    $ct_jl_Throwable__T__jl_Throwable__Z__Z__(this, s, null, true, true)
  };
}
const $d_jl_ClassCastException = new $TypeData().initClass({
  jl_ClassCastException: 0
}, false, "java.lang.ClassCastException", {
  jl_ClassCastException: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_jl_ClassCastException.prototype.$classData = $d_jl_ClassCastException;
const $ct_jl_IllegalArgumentException__T__ = (function($thiz, s) {
  $ct_jl_Throwable__T__jl_Throwable__Z__Z__($thiz, s, null, true, true);
  return $thiz
});
const $ct_jl_IllegalArgumentException__ = (function($thiz) {
  $ct_jl_Throwable__T__jl_Throwable__Z__Z__($thiz, null, null, true, true);
  return $thiz
});
class $c_jl_IllegalArgumentException extends $c_jl_RuntimeException {
}
const $d_jl_IllegalArgumentException = new $TypeData().initClass({
  jl_IllegalArgumentException: 0
}, false, "java.lang.IllegalArgumentException", {
  jl_IllegalArgumentException: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_jl_IllegalArgumentException.prototype.$classData = $d_jl_IllegalArgumentException;
class $c_jl_IllegalStateException extends $c_jl_RuntimeException {
  constructor(s) {
    super();
    $ct_jl_Throwable__T__jl_Throwable__Z__Z__(this, s, null, true, true)
  };
}
const $d_jl_IllegalStateException = new $TypeData().initClass({
  jl_IllegalStateException: 0
}, false, "java.lang.IllegalStateException", {
  jl_IllegalStateException: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_jl_IllegalStateException.prototype.$classData = $d_jl_IllegalStateException;
const $ct_jl_IndexOutOfBoundsException__T__ = (function($thiz, s) {
  $ct_jl_Throwable__T__jl_Throwable__Z__Z__($thiz, s, null, true, true);
  return $thiz
});
class $c_jl_IndexOutOfBoundsException extends $c_jl_RuntimeException {
}
const $d_jl_IndexOutOfBoundsException = new $TypeData().initClass({
  jl_IndexOutOfBoundsException: 0
}, false, "java.lang.IndexOutOfBoundsException", {
  jl_IndexOutOfBoundsException: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_jl_IndexOutOfBoundsException.prototype.$classData = $d_jl_IndexOutOfBoundsException;
class $c_jl_NegativeArraySizeException extends $c_jl_RuntimeException {
  constructor() {
    super();
    $ct_jl_Throwable__T__jl_Throwable__Z__Z__(this, null, null, true, true)
  };
}
const $d_jl_NegativeArraySizeException = new $TypeData().initClass({
  jl_NegativeArraySizeException: 0
}, false, "java.lang.NegativeArraySizeException", {
  jl_NegativeArraySizeException: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_jl_NegativeArraySizeException.prototype.$classData = $d_jl_NegativeArraySizeException;
class $c_jl_NullPointerException extends $c_jl_RuntimeException {
  constructor() {
    super();
    $ct_jl_Throwable__T__jl_Throwable__Z__Z__(this, null, null, true, true)
  };
}
const $d_jl_NullPointerException = new $TypeData().initClass({
  jl_NullPointerException: 0
}, false, "java.lang.NullPointerException", {
  jl_NullPointerException: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_jl_NullPointerException.prototype.$classData = $d_jl_NullPointerException;
class $c_jl_UnsupportedOperationException extends $c_jl_RuntimeException {
  constructor(s) {
    super();
    $ct_jl_Throwable__T__jl_Throwable__Z__Z__(this, s, null, true, true)
  };
}
const $d_jl_UnsupportedOperationException = new $TypeData().initClass({
  jl_UnsupportedOperationException: 0
}, false, "java.lang.UnsupportedOperationException", {
  jl_UnsupportedOperationException: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_jl_UnsupportedOperationException.prototype.$classData = $d_jl_UnsupportedOperationException;
const $ct_ju_NoSuchElementException__T__ = (function($thiz, s) {
  $ct_jl_Throwable__T__jl_Throwable__Z__Z__($thiz, s, null, true, true);
  return $thiz
});
const $ct_ju_NoSuchElementException__ = (function($thiz) {
  $ct_jl_Throwable__T__jl_Throwable__Z__Z__($thiz, null, null, true, true);
  return $thiz
});
class $c_ju_NoSuchElementException extends $c_jl_RuntimeException {
}
const $d_ju_NoSuchElementException = new $TypeData().initClass({
  ju_NoSuchElementException: 0
}, false, "java.util.NoSuchElementException", {
  ju_NoSuchElementException: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_ju_NoSuchElementException.prototype.$classData = $d_ju_NoSuchElementException;
class $c_Lorg_scalajs_linker_runtime_UndefinedBehaviorError extends $c_jl_VirtualMachineError {
  constructor(cause) {
    super();
    const message = ((cause === null) ? null : cause.toString__T());
    $ct_jl_Throwable__T__jl_Throwable__Z__Z__(this, message, cause, true, true)
  };
}
const $d_Lorg_scalajs_linker_runtime_UndefinedBehaviorError = new $TypeData().initClass({
  Lorg_scalajs_linker_runtime_UndefinedBehaviorError: 0
}, false, "org.scalajs.linker.runtime.UndefinedBehaviorError", {
  Lorg_scalajs_linker_runtime_UndefinedBehaviorError: 1,
  jl_VirtualMachineError: 1,
  jl_Error: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_Lorg_scalajs_linker_runtime_UndefinedBehaviorError.prototype.$classData = $d_Lorg_scalajs_linker_runtime_UndefinedBehaviorError;
const $p_s_MatchError__objString$lzycompute__T = (function($thiz) {
  if ((!$thiz.s_MatchError__f_bitmap$0)) {
    $thiz.s_MatchError__f_objString = (($thiz.s_MatchError__f_obj === null) ? "null" : $p_s_MatchError__liftedTree1$1__T($thiz));
    $thiz.s_MatchError__f_bitmap$0 = true
  };
  return $thiz.s_MatchError__f_objString
});
const $p_s_MatchError__objString__T = (function($thiz) {
  return ((!$thiz.s_MatchError__f_bitmap$0) ? $p_s_MatchError__objString$lzycompute__T($thiz) : $thiz.s_MatchError__f_objString)
});
const $p_s_MatchError__ofClass$1__T = (function($thiz) {
  const this$1 = $thiz.s_MatchError__f_obj;
  return ("of class " + $objectClassName(this$1))
});
const $p_s_MatchError__liftedTree1$1__T = (function($thiz) {
  try {
    return ((($dp_toString__T($thiz.s_MatchError__f_obj) + " (") + $p_s_MatchError__ofClass$1__T($thiz)) + ")")
  } catch (e) {
    const e$2 = $m_sjsr_package$().wrapJavaScriptException__O__jl_Throwable(e);
    if ((e$2 !== null)) {
      return ("an instance " + $p_s_MatchError__ofClass$1__T($thiz))
    } else {
      throw e
    }
  }
});
class $c_s_MatchError extends $c_jl_RuntimeException {
  constructor(obj) {
    super();
    this.s_MatchError__f_objString = null;
    this.s_MatchError__f_obj = null;
    this.s_MatchError__f_bitmap$0 = false;
    this.s_MatchError__f_obj = obj;
    $ct_jl_Throwable__T__jl_Throwable__Z__Z__(this, null, null, true, true)
  };
  getMessage__T() {
    return $p_s_MatchError__objString__T(this)
  };
}
const $d_s_MatchError = new $TypeData().initClass({
  s_MatchError: 0
}, false, "scala.MatchError", {
  s_MatchError: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_s_MatchError.prototype.$classData = $d_s_MatchError;
class $c_s_Option extends $c_O {
  toList__sci_List() {
    return (this.isEmpty__Z() ? $m_sci_Nil$() : new $c_sci_$colon$colon(this.get__O(), $m_sci_Nil$()))
  };
}
function $as_s_Option(obj) {
  return (((obj instanceof $c_s_Option) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.Option"))
}
function $isArrayOf_s_Option(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.s_Option)))
}
function $asArrayOf_s_Option(obj, depth) {
  return (($isArrayOf_s_Option(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.Option;", depth))
}
class $c_sc_Iterable$ extends $c_scg_GenTraversableFactory {
  constructor() {
    super();
    $ct_scg_GenTraversableFactory__(this)
  };
  newBuilder__scm_Builder() {
    $m_sci_Iterable$();
    return new $c_scm_ListBuffer()
  };
}
const $d_sc_Iterable$ = new $TypeData().initClass({
  sc_Iterable$: 0
}, false, "scala.collection.Iterable$", {
  sc_Iterable$: 1,
  scg_GenTraversableFactory: 1,
  scg_GenericCompanion: 1,
  O: 1,
  scg_TraversableFactory: 1,
  scg_GenericSeqCompanion: 1
});
$c_sc_Iterable$.prototype.$classData = $d_sc_Iterable$;
let $n_sc_Iterable$ = (void 0);
function $m_sc_Iterable$() {
  if ((!$n_sc_Iterable$)) {
    $n_sc_Iterable$ = new $c_sc_Iterable$()
  };
  return $n_sc_Iterable$
}
class $c_sc_Iterator$$anon$2 extends $c_sc_AbstractIterator {
  hasNext__Z() {
    return false
  };
  next__E() {
    throw $ct_ju_NoSuchElementException__T__(new $c_ju_NoSuchElementException(), "next on empty iterator")
  };
  next__O() {
    this.next__E()
  };
}
const $d_sc_Iterator$$anon$2 = new $TypeData().initClass({
  sc_Iterator$$anon$2: 0
}, false, "scala.collection.Iterator$$anon$2", {
  sc_Iterator$$anon$2: 1,
  sc_AbstractIterator: 1,
  O: 1,
  sc_Iterator: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1
});
$c_sc_Iterator$$anon$2.prototype.$classData = $d_sc_Iterator$$anon$2;
class $c_sc_LinearSeqLike$$anon$1 extends $c_sc_AbstractIterator {
  constructor(outer) {
    super();
    this.sc_LinearSeqLike$$anon$1__f_these = null;
    this.sc_LinearSeqLike$$anon$1__f_these = outer
  };
  hasNext__Z() {
    return (!this.sc_LinearSeqLike$$anon$1__f_these.isEmpty__Z())
  };
  next__O() {
    if (this.hasNext__Z()) {
      const result = this.sc_LinearSeqLike$$anon$1__f_these.head__O();
      this.sc_LinearSeqLike$$anon$1__f_these = $as_sc_LinearSeqLike(this.sc_LinearSeqLike$$anon$1__f_these.tail__O());
      return result
    } else {
      return $m_sc_Iterator$().sc_Iterator$__f_empty.next__O()
    }
  };
  toList__sci_List() {
    const xs = this.sc_LinearSeqLike$$anon$1__f_these.toList__sci_List();
    this.sc_LinearSeqLike$$anon$1__f_these = $as_sc_LinearSeqLike(this.sc_LinearSeqLike$$anon$1__f_these.take__I__O(0));
    return xs
  };
}
const $d_sc_LinearSeqLike$$anon$1 = new $TypeData().initClass({
  sc_LinearSeqLike$$anon$1: 0
}, false, "scala.collection.LinearSeqLike$$anon$1", {
  sc_LinearSeqLike$$anon$1: 1,
  sc_AbstractIterator: 1,
  O: 1,
  sc_Iterator: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1
});
$c_sc_LinearSeqLike$$anon$1.prototype.$classData = $d_sc_LinearSeqLike$$anon$1;
class $c_sc_Traversable$ extends $c_scg_GenTraversableFactory {
  constructor() {
    super();
    this.sc_Traversable$__f_breaks = null;
    $ct_scg_GenTraversableFactory__(this);
    $n_sc_Traversable$ = this;
    this.sc_Traversable$__f_breaks = new $c_s_util_control_Breaks()
  };
  newBuilder__scm_Builder() {
    $m_sci_Traversable$();
    return new $c_scm_ListBuffer()
  };
}
const $d_sc_Traversable$ = new $TypeData().initClass({
  sc_Traversable$: 0
}, false, "scala.collection.Traversable$", {
  sc_Traversable$: 1,
  scg_GenTraversableFactory: 1,
  scg_GenericCompanion: 1,
  O: 1,
  scg_TraversableFactory: 1,
  scg_GenericSeqCompanion: 1
});
$c_sc_Traversable$.prototype.$classData = $d_sc_Traversable$;
let $n_sc_Traversable$ = (void 0);
function $m_sc_Traversable$() {
  if ((!$n_sc_Traversable$)) {
    $n_sc_Traversable$ = new $c_sc_Traversable$()
  };
  return $n_sc_Traversable$
}
class $c_sci_Iterable$ extends $c_scg_GenTraversableFactory {
  constructor() {
    super();
    $ct_scg_GenTraversableFactory__(this)
  };
  newBuilder__scm_Builder() {
    return new $c_scm_ListBuffer()
  };
}
const $d_sci_Iterable$ = new $TypeData().initClass({
  sci_Iterable$: 0
}, false, "scala.collection.immutable.Iterable$", {
  sci_Iterable$: 1,
  scg_GenTraversableFactory: 1,
  scg_GenericCompanion: 1,
  O: 1,
  scg_TraversableFactory: 1,
  scg_GenericSeqCompanion: 1
});
$c_sci_Iterable$.prototype.$classData = $d_sci_Iterable$;
let $n_sci_Iterable$ = (void 0);
function $m_sci_Iterable$() {
  if ((!$n_sci_Iterable$)) {
    $n_sci_Iterable$ = new $c_sci_Iterable$()
  };
  return $n_sci_Iterable$
}
class $c_sci_StreamIterator extends $c_sc_AbstractIterator {
  constructor(self) {
    super();
    this.sci_StreamIterator__f_these = null;
    this.sci_StreamIterator__f_these = new $c_sci_StreamIterator$LazyCell(this, new $c_sjsr_AnonFunction0(((this$1, self$1) => (() => self$1))(this, self)))
  };
  hasNext__Z() {
    const this$1 = this.sci_StreamIterator__f_these.v__sci_Stream();
    return (!this$1.isEmpty__Z())
  };
  next__O() {
    if ((!this.hasNext__Z())) {
      return $m_sc_Iterator$().sc_Iterator$__f_empty.next__O()
    } else {
      const cur = this.sci_StreamIterator__f_these.v__sci_Stream();
      const result = cur.head__O();
      this.sci_StreamIterator__f_these = new $c_sci_StreamIterator$LazyCell(this, new $c_sjsr_AnonFunction0(((this$1, cur$1) => (() => $as_sci_Stream(cur$1.tail__O())))(this, cur)));
      return result
    }
  };
  toStream__sci_Stream() {
    const result = this.sci_StreamIterator__f_these.v__sci_Stream();
    this.sci_StreamIterator__f_these = new $c_sci_StreamIterator$LazyCell(this, new $c_sjsr_AnonFunction0(((this$1) => (() => {
      $m_sci_Stream$();
      return $m_sci_Stream$Empty$()
    }))(this)));
    return result
  };
  toList__sci_List() {
    const this$1 = this.toStream__sci_Stream();
    const this$2 = $m_sci_List$();
    const cbf = this$2.scg_GenTraversableFactory__f_ReusableCBFInstance;
    return $as_sci_List($f_sc_TraversableLike__to__scg_CanBuildFrom__O(this$1, cbf))
  };
}
const $d_sci_StreamIterator = new $TypeData().initClass({
  sci_StreamIterator: 0
}, false, "scala.collection.immutable.StreamIterator", {
  sci_StreamIterator: 1,
  sc_AbstractIterator: 1,
  O: 1,
  sc_Iterator: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1
});
$c_sci_StreamIterator.prototype.$classData = $d_sci_StreamIterator;
class $c_sci_Traversable$ extends $c_scg_GenTraversableFactory {
  constructor() {
    super();
    $ct_scg_GenTraversableFactory__(this)
  };
  newBuilder__scm_Builder() {
    return new $c_scm_ListBuffer()
  };
}
const $d_sci_Traversable$ = new $TypeData().initClass({
  sci_Traversable$: 0
}, false, "scala.collection.immutable.Traversable$", {
  sci_Traversable$: 1,
  scg_GenTraversableFactory: 1,
  scg_GenericCompanion: 1,
  O: 1,
  scg_TraversableFactory: 1,
  scg_GenericSeqCompanion: 1
});
$c_sci_Traversable$.prototype.$classData = $d_sci_Traversable$;
let $n_sci_Traversable$ = (void 0);
function $m_sci_Traversable$() {
  if ((!$n_sci_Traversable$)) {
    $n_sci_Traversable$ = new $c_sci_Traversable$()
  };
  return $n_sci_Traversable$
}
class $c_scm_Builder$$anon$1 extends $c_O {
  constructor(outer, f$1) {
    super();
    this.scm_Builder$$anon$1__f_self = null;
    this.scm_Builder$$anon$1__f_f$1 = null;
    this.scm_Builder$$anon$1__f_f$1 = f$1;
    this.scm_Builder$$anon$1__f_self = outer
  };
  hashCode__I() {
    return this.scm_Builder$$anon$1__f_self.hashCode__I()
  };
  equals__O__Z(that) {
    return $f_s_Proxy__equals__O__Z(this, that)
  };
  toString__T() {
    return $f_s_Proxy__toString__T(this)
  };
  $plus$eq__O__scm_Builder$$anon$1(x) {
    this.scm_Builder$$anon$1__f_self.$plus$eq__O__scm_Builder(x);
    return this
  };
  $plus$plus$eq__sc_TraversableOnce__scm_Builder$$anon$1(xs) {
    this.scm_Builder$$anon$1__f_self.$plus$plus$eq__sc_TraversableOnce__scg_Growable(xs);
    return this
  };
  sizeHint__I__V(size) {
    this.scm_Builder$$anon$1__f_self.sizeHint__I__V(size)
  };
  sizeHintBounded__I__sc_TraversableLike__V(size, boundColl) {
    this.scm_Builder$$anon$1__f_self.sizeHintBounded__I__sc_TraversableLike__V(size, boundColl)
  };
  result__O() {
    return this.scm_Builder$$anon$1__f_f$1.apply__O__O(this.scm_Builder$$anon$1__f_self.result__O())
  };
  $plus$plus$eq__sc_TraversableOnce__scg_Growable(xs) {
    return this.$plus$plus$eq__sc_TraversableOnce__scm_Builder$$anon$1(xs)
  };
  $plus$eq__O__scg_Growable(elem) {
    return this.$plus$eq__O__scm_Builder$$anon$1(elem)
  };
  $plus$eq__O__scm_Builder(elem) {
    return this.$plus$eq__O__scm_Builder$$anon$1(elem)
  };
}
const $d_scm_Builder$$anon$1 = new $TypeData().initClass({
  scm_Builder$$anon$1: 0
}, false, "scala.collection.mutable.Builder$$anon$1", {
  scm_Builder$$anon$1: 1,
  O: 1,
  scm_Builder: 1,
  scg_Growable: 1,
  scg_Clearable: 1,
  s_Proxy: 1
});
$c_scm_Builder$$anon$1.prototype.$classData = $d_scm_Builder$$anon$1;
const $ct_scm_LazyBuilder__ = (function($thiz) {
  $thiz.scm_LazyBuilder__f_parts = new $c_scm_ListBuffer();
  return $thiz
});
class $c_scm_LazyBuilder extends $c_O {
  constructor() {
    super();
    this.scm_LazyBuilder__f_parts = null
  };
  sizeHint__I__V(size) {
    /*<skip>*/
  };
  sizeHintBounded__I__sc_TraversableLike__V(size, boundingColl) {
    $f_scm_Builder__sizeHintBounded__I__sc_TraversableLike__V(this, size, boundingColl)
  };
  $plus$eq__O__scm_LazyBuilder(x) {
    const $$x1 = this.scm_LazyBuilder__f_parts;
    $m_sci_List$();
    const array = [x];
    let i = (((-1) + $uI(array.length)) | 0);
    let result = $m_sci_Nil$();
    while ((i >= 0)) {
      const this$4 = result;
      const index = i;
      const x$1 = array[index];
      result = new $c_sci_$colon$colon(x$1, this$4);
      i = (((-1) + i) | 0)
    };
    $$x1.$plus$eq__O__scm_ListBuffer(result);
    return this
  };
  $plus$plus$eq__sc_TraversableOnce__scm_LazyBuilder(xs) {
    this.scm_LazyBuilder__f_parts.$plus$eq__O__scm_ListBuffer(xs);
    return this
  };
  $plus$plus$eq__sc_TraversableOnce__scg_Growable(xs) {
    return this.$plus$plus$eq__sc_TraversableOnce__scm_LazyBuilder(xs)
  };
  $plus$eq__O__scg_Growable(elem) {
    return this.$plus$eq__O__scm_LazyBuilder(elem)
  };
  $plus$eq__O__scm_Builder(elem) {
    return this.$plus$eq__O__scm_LazyBuilder(elem)
  };
}
class $c_scm_LinkedListLike$$anon$1 extends $c_sc_AbstractIterator {
  constructor(outer) {
    super();
    this.scm_LinkedListLike$$anon$1__f_elems = null;
    this.scm_LinkedListLike$$anon$1__f_elems = outer
  };
  hasNext__Z() {
    const this$1 = this.scm_LinkedListLike$$anon$1__f_elems;
    return (!$f_scm_LinkedListLike__isEmpty__Z(this$1))
  };
  next__O() {
    const res = this.scm_LinkedListLike$$anon$1__f_elems.scm_LinkedList__f_elem;
    const this$1 = this.scm_LinkedListLike$$anon$1__f_elems;
    this.scm_LinkedListLike$$anon$1__f_elems = this$1.scm_LinkedList__f_next;
    return res
  };
}
const $d_scm_LinkedListLike$$anon$1 = new $TypeData().initClass({
  scm_LinkedListLike$$anon$1: 0
}, false, "scala.collection.mutable.LinkedListLike$$anon$1", {
  scm_LinkedListLike$$anon$1: 1,
  sc_AbstractIterator: 1,
  O: 1,
  sc_Iterator: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1
});
$c_scm_LinkedListLike$$anon$1.prototype.$classData = $d_scm_LinkedListLike$$anon$1;
class $c_scm_ListBuffer$$anon$1 extends $c_sc_AbstractIterator {
  constructor(outer) {
    super();
    this.scm_ListBuffer$$anon$1__f_cursor = null;
    this.scm_ListBuffer$$anon$1__f_cursor = (outer.isEmpty__Z() ? $m_sci_Nil$() : outer.scm_ListBuffer__f_scala$collection$mutable$ListBuffer$$start)
  };
  hasNext__Z() {
    return (this.scm_ListBuffer$$anon$1__f_cursor !== $m_sci_Nil$())
  };
  next__O() {
    if ((!this.hasNext__Z())) {
      throw $ct_ju_NoSuchElementException__T__(new $c_ju_NoSuchElementException(), "next on empty Iterator")
    } else {
      const ans = this.scm_ListBuffer$$anon$1__f_cursor.head__O();
      const this$1 = this.scm_ListBuffer$$anon$1__f_cursor;
      this.scm_ListBuffer$$anon$1__f_cursor = this$1.tail__sci_List();
      return ans
    }
  };
}
const $d_scm_ListBuffer$$anon$1 = new $TypeData().initClass({
  scm_ListBuffer$$anon$1: 0
}, false, "scala.collection.mutable.ListBuffer$$anon$1", {
  scm_ListBuffer$$anon$1: 1,
  sc_AbstractIterator: 1,
  O: 1,
  sc_Iterator: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1
});
$c_scm_ListBuffer$$anon$1.prototype.$classData = $d_scm_ListBuffer$$anon$1;
class $c_scm_MutableList$$anon$1 extends $c_sc_AbstractIterator {
  constructor(outer) {
    super();
    this.scm_MutableList$$anon$1__f_elems = null;
    this.scm_MutableList$$anon$1__f_count = 0;
    this.scm_MutableList$$anon$1__f_elems = outer.scm_MutableList__f_first0;
    this.scm_MutableList$$anon$1__f_count = outer.scm_MutableList__f_len
  };
  hasNext__Z() {
    if ((this.scm_MutableList$$anon$1__f_count > 0)) {
      const this$1 = this.scm_MutableList$$anon$1__f_elems;
      return (!$f_scm_LinkedListLike__isEmpty__Z(this$1))
    } else {
      return false
    }
  };
  next__O() {
    if ((!this.hasNext__Z())) {
      throw $ct_ju_NoSuchElementException__(new $c_ju_NoSuchElementException())
    };
    this.scm_MutableList$$anon$1__f_count = (((-1) + this.scm_MutableList$$anon$1__f_count) | 0);
    const e = this.scm_MutableList$$anon$1__f_elems.scm_LinkedList__f_elem;
    let $$x1;
    if ((this.scm_MutableList$$anon$1__f_count === 0)) {
      $$x1 = null
    } else {
      const this$1 = this.scm_MutableList$$anon$1__f_elems;
      $$x1 = this$1.scm_LinkedList__f_next
    };
    this.scm_MutableList$$anon$1__f_elems = $$x1;
    return e
  };
}
const $d_scm_MutableList$$anon$1 = new $TypeData().initClass({
  scm_MutableList$$anon$1: 0
}, false, "scala.collection.mutable.MutableList$$anon$1", {
  scm_MutableList$$anon$1: 1,
  sc_AbstractIterator: 1,
  O: 1,
  sc_Iterator: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1
});
$c_scm_MutableList$$anon$1.prototype.$classData = $d_scm_MutableList$$anon$1;
class $c_sr_ScalaRunTime$$anon$1 extends $c_sc_AbstractIterator {
  constructor(x$2) {
    super();
    this.sr_ScalaRunTime$$anon$1__f_c = 0;
    this.sr_ScalaRunTime$$anon$1__f_cmax = 0;
    this.sr_ScalaRunTime$$anon$1__f_x$2 = null;
    this.sr_ScalaRunTime$$anon$1__f_x$2 = x$2;
    this.sr_ScalaRunTime$$anon$1__f_c = 0;
    this.sr_ScalaRunTime$$anon$1__f_cmax = x$2.productArity__I()
  };
  hasNext__Z() {
    return (this.sr_ScalaRunTime$$anon$1__f_c < this.sr_ScalaRunTime$$anon$1__f_cmax)
  };
  next__O() {
    const result = this.sr_ScalaRunTime$$anon$1__f_x$2.productElement__I__O(this.sr_ScalaRunTime$$anon$1__f_c);
    this.sr_ScalaRunTime$$anon$1__f_c = ((1 + this.sr_ScalaRunTime$$anon$1__f_c) | 0);
    return result
  };
}
const $d_sr_ScalaRunTime$$anon$1 = new $TypeData().initClass({
  sr_ScalaRunTime$$anon$1: 0
}, false, "scala.runtime.ScalaRunTime$$anon$1", {
  sr_ScalaRunTime$$anon$1: 1,
  sc_AbstractIterator: 1,
  O: 1,
  sc_Iterator: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1
});
$c_sr_ScalaRunTime$$anon$1.prototype.$classData = $d_sr_ScalaRunTime$$anon$1;
class $c_s_util_control_BreakControl extends $c_jl_Throwable {
  constructor() {
    super();
    $ct_jl_Throwable__T__jl_Throwable__Z__Z__(this, null, null, true, true)
  };
  fillInStackTrace__jl_Throwable() {
    return $f_s_util_control_NoStackTrace__fillInStackTrace__jl_Throwable(this)
  };
}
const $d_s_util_control_BreakControl = new $TypeData().initClass({
  s_util_control_BreakControl: 0
}, false, "scala.util.control.BreakControl", {
  s_util_control_BreakControl: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  s_util_control_ControlThrowable: 1,
  s_util_control_NoStackTrace: 1
});
$c_s_util_control_BreakControl.prototype.$classData = $d_s_util_control_BreakControl;
class $c_Lsgl_geometry_Point extends $c_O {
  constructor(x, y) {
    super();
    this.Lsgl_geometry_Point__f_x = 0.0;
    this.Lsgl_geometry_Point__f_y = 0.0;
    this.Lsgl_geometry_Point__f_x = x;
    this.Lsgl_geometry_Point__f_y = y
  };
  $plus__Lsgl_geometry_Vec__Lsgl_geometry_Point(m) {
    return new $c_Lsgl_geometry_Point($fround((this.Lsgl_geometry_Point__f_x + m.Lsgl_geometry_Vec__f_x)), $fround((this.Lsgl_geometry_Point__f_y + m.Lsgl_geometry_Vec__f_y)))
  };
  productPrefix__T() {
    return "Point"
  };
  productArity__I() {
    return 2
  };
  productElement__I__O(x$1) {
    switch (x$1) {
      case 0: {
        return this.Lsgl_geometry_Point__f_x;
        break
      }
      case 1: {
        return this.Lsgl_geometry_Point__f_y;
        break
      }
      default: {
        throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
      }
    }
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    let acc = (-889275714);
    const hash = acc;
    const fv = this.Lsgl_geometry_Point__f_x;
    const this$1 = $m_sr_Statics$();
    const data = this$1.doubleHash__D__I(fv);
    acc = $m_sr_Statics$().mix__I__I__I(hash, data);
    const hash$1 = acc;
    const fv$1 = this.Lsgl_geometry_Point__f_y;
    const this$2 = $m_sr_Statics$();
    const data$1 = this$2.doubleHash__D__I(fv$1);
    acc = $m_sr_Statics$().mix__I__I__I(hash$1, data$1);
    const hash$2 = acc;
    return $m_sr_Statics$().finalizeHash__I__I__I(hash$2, 2)
  };
  toString__T() {
    return $m_sr_ScalaRunTime$()._toString__s_Product__T(this)
  };
  equals__O__Z(x$1) {
    if ((this === x$1)) {
      return true
    } else if ((x$1 instanceof $c_Lsgl_geometry_Point)) {
      const Point$1 = $as_Lsgl_geometry_Point(x$1);
      return ((this.Lsgl_geometry_Point__f_x === Point$1.Lsgl_geometry_Point__f_x) && (this.Lsgl_geometry_Point__f_y === Point$1.Lsgl_geometry_Point__f_y))
    } else {
      return false
    }
  };
}
function $as_Lsgl_geometry_Point(obj) {
  return (((obj instanceof $c_Lsgl_geometry_Point) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.geometry.Point"))
}
function $isArrayOf_Lsgl_geometry_Point(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_geometry_Point)))
}
function $asArrayOf_Lsgl_geometry_Point(obj, depth) {
  return (($isArrayOf_Lsgl_geometry_Point(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.geometry.Point;", depth))
}
const $d_Lsgl_geometry_Point = new $TypeData().initClass({
  Lsgl_geometry_Point: 0
}, false, "sgl.geometry.Point", {
  Lsgl_geometry_Point: 1,
  O: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_geometry_Point.prototype.$classData = $d_Lsgl_geometry_Point;
class $c_Lsgl_geometry_Vec extends $c_O {
  constructor(x, y) {
    super();
    this.Lsgl_geometry_Vec__f_x = 0.0;
    this.Lsgl_geometry_Vec__f_y = 0.0;
    this.Lsgl_geometry_Vec__f_x = x;
    this.Lsgl_geometry_Vec__f_y = y
  };
  $plus__Lsgl_geometry_Vec__Lsgl_geometry_Vec(m) {
    return new $c_Lsgl_geometry_Vec($fround((this.Lsgl_geometry_Vec__f_x + m.Lsgl_geometry_Vec__f_x)), $fround((this.Lsgl_geometry_Vec__f_y + m.Lsgl_geometry_Vec__f_y)))
  };
  $times__F__Lsgl_geometry_Vec(s) {
    return new $c_Lsgl_geometry_Vec($fround((this.Lsgl_geometry_Vec__f_x * s)), $fround((this.Lsgl_geometry_Vec__f_y * s)))
  };
  productPrefix__T() {
    return "Vec"
  };
  productArity__I() {
    return 2
  };
  productElement__I__O(x$1) {
    switch (x$1) {
      case 0: {
        return this.Lsgl_geometry_Vec__f_x;
        break
      }
      case 1: {
        return this.Lsgl_geometry_Vec__f_y;
        break
      }
      default: {
        throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
      }
    }
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    let acc = (-889275714);
    const hash = acc;
    const fv = this.Lsgl_geometry_Vec__f_x;
    const this$1 = $m_sr_Statics$();
    const data = this$1.doubleHash__D__I(fv);
    acc = $m_sr_Statics$().mix__I__I__I(hash, data);
    const hash$1 = acc;
    const fv$1 = this.Lsgl_geometry_Vec__f_y;
    const this$2 = $m_sr_Statics$();
    const data$1 = this$2.doubleHash__D__I(fv$1);
    acc = $m_sr_Statics$().mix__I__I__I(hash$1, data$1);
    const hash$2 = acc;
    return $m_sr_Statics$().finalizeHash__I__I__I(hash$2, 2)
  };
  toString__T() {
    return $m_sr_ScalaRunTime$()._toString__s_Product__T(this)
  };
  equals__O__Z(x$1) {
    if ((this === x$1)) {
      return true
    } else if ((x$1 instanceof $c_Lsgl_geometry_Vec)) {
      const Vec$1 = $as_Lsgl_geometry_Vec(x$1);
      return ((this.Lsgl_geometry_Vec__f_x === Vec$1.Lsgl_geometry_Vec__f_x) && (this.Lsgl_geometry_Vec__f_y === Vec$1.Lsgl_geometry_Vec__f_y))
    } else {
      return false
    }
  };
}
function $as_Lsgl_geometry_Vec(obj) {
  return (((obj instanceof $c_Lsgl_geometry_Vec) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.geometry.Vec"))
}
function $isArrayOf_Lsgl_geometry_Vec(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_geometry_Vec)))
}
function $asArrayOf_Lsgl_geometry_Vec(obj, depth) {
  return (($isArrayOf_Lsgl_geometry_Vec(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.geometry.Vec;", depth))
}
const $d_Lsgl_geometry_Vec = new $TypeData().initClass({
  Lsgl_geometry_Vec: 0
}, false, "sgl.geometry.Vec", {
  Lsgl_geometry_Vec: 1,
  O: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_geometry_Vec.prototype.$classData = $d_Lsgl_geometry_Vec;
class $c_Lsgl_util_LoggingProvider$Logger$Tag extends $c_O {
  constructor(outer, name) {
    super();
    this.Lsgl_util_LoggingProvider$Logger$Tag__f_name = null;
    this.Lsgl_util_LoggingProvider$Logger$Tag__f_$outer = null;
    this.Lsgl_util_LoggingProvider$Logger$Tag__f_name = name;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_util_LoggingProvider$Logger$Tag__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "Tag"
  };
  productArity__I() {
    return 1
  };
  productElement__I__O(x$1) {
    if ((x$1 === 0)) {
      return this.Lsgl_util_LoggingProvider$Logger$Tag__f_name
    } else {
      throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
    }
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    const this$2 = $m_s_util_hashing_MurmurHash3$();
    return this$2.productHash__s_Product__I__I(this, (-889275714))
  };
  toString__T() {
    return $m_sr_ScalaRunTime$()._toString__s_Product__T(this)
  };
  equals__O__Z(x$1) {
    if ((this === x$1)) {
      return true
    } else if (((x$1 instanceof $c_Lsgl_util_LoggingProvider$Logger$Tag) && ($as_Lsgl_util_LoggingProvider$Logger$Tag(x$1).Lsgl_util_LoggingProvider$Logger$Tag__f_$outer === this.Lsgl_util_LoggingProvider$Logger$Tag__f_$outer))) {
      const Tag$1 = $as_Lsgl_util_LoggingProvider$Logger$Tag(x$1);
      return (this.Lsgl_util_LoggingProvider$Logger$Tag__f_name === Tag$1.Lsgl_util_LoggingProvider$Logger$Tag__f_name)
    } else {
      return false
    }
  };
}
function $as_Lsgl_util_LoggingProvider$Logger$Tag(obj) {
  return (((obj instanceof $c_Lsgl_util_LoggingProvider$Logger$Tag) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.util.LoggingProvider$Logger$Tag"))
}
function $isArrayOf_Lsgl_util_LoggingProvider$Logger$Tag(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_util_LoggingProvider$Logger$Tag)))
}
function $asArrayOf_Lsgl_util_LoggingProvider$Logger$Tag(obj, depth) {
  return (($isArrayOf_Lsgl_util_LoggingProvider$Logger$Tag(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.util.LoggingProvider$Logger$Tag;", depth))
}
const $d_Lsgl_util_LoggingProvider$Logger$Tag = new $TypeData().initClass({
  Lsgl_util_LoggingProvider$Logger$Tag: 0
}, false, "sgl.util.LoggingProvider$Logger$Tag", {
  Lsgl_util_LoggingProvider$Logger$Tag: 1,
  O: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_util_LoggingProvider$Logger$Tag.prototype.$classData = $d_Lsgl_util_LoggingProvider$Logger$Tag;
class $c_jl_ArrayIndexOutOfBoundsException extends $c_jl_IndexOutOfBoundsException {
  constructor(s) {
    super();
    $ct_jl_Throwable__T__jl_Throwable__Z__Z__(this, s, null, true, true)
  };
}
const $d_jl_ArrayIndexOutOfBoundsException = new $TypeData().initClass({
  jl_ArrayIndexOutOfBoundsException: 0
}, false, "java.lang.ArrayIndexOutOfBoundsException", {
  jl_ArrayIndexOutOfBoundsException: 1,
  jl_IndexOutOfBoundsException: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_jl_ArrayIndexOutOfBoundsException.prototype.$classData = $d_jl_ArrayIndexOutOfBoundsException;
class $c_jl_NumberFormatException extends $c_jl_IllegalArgumentException {
  constructor(s) {
    super();
    $ct_jl_Throwable__T__jl_Throwable__Z__Z__(this, s, null, true, true)
  };
}
const $d_jl_NumberFormatException = new $TypeData().initClass({
  jl_NumberFormatException: 0
}, false, "java.lang.NumberFormatException", {
  jl_NumberFormatException: 1,
  jl_IllegalArgumentException: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1
});
$c_jl_NumberFormatException.prototype.$classData = $d_jl_NumberFormatException;
class $c_s_None$ extends $c_s_Option {
  isEmpty__Z() {
    return true
  };
  get__E() {
    throw $ct_ju_NoSuchElementException__T__(new $c_ju_NoSuchElementException(), "None.get")
  };
  productPrefix__T() {
    return "None"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 2433880
  };
  toString__T() {
    return "None"
  };
  get__O() {
    this.get__E()
  };
}
const $d_s_None$ = new $TypeData().initClass({
  s_None$: 0
}, false, "scala.None$", {
  s_None$: 1,
  s_Option: 1,
  O: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_s_None$.prototype.$classData = $d_s_None$;
let $n_s_None$ = (void 0);
function $m_s_None$() {
  if ((!$n_s_None$)) {
    $n_s_None$ = new $c_s_None$()
  };
  return $n_s_None$
}
class $c_s_Some extends $c_s_Option {
  constructor(value) {
    super();
    this.s_Some__f_value = null;
    this.s_Some__f_value = value
  };
  isEmpty__Z() {
    return false
  };
  get__O() {
    return this.s_Some__f_value
  };
  productPrefix__T() {
    return "Some"
  };
  productArity__I() {
    return 1
  };
  productElement__I__O(x$1) {
    if ((x$1 === 0)) {
      return this.s_Some__f_value
    } else {
      throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
    }
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    const this$2 = $m_s_util_hashing_MurmurHash3$();
    return this$2.productHash__s_Product__I__I(this, (-889275714))
  };
  toString__T() {
    return $m_sr_ScalaRunTime$()._toString__s_Product__T(this)
  };
  equals__O__Z(x$1) {
    if ((this === x$1)) {
      return true
    } else if ((x$1 instanceof $c_s_Some)) {
      const Some$1 = $as_s_Some(x$1);
      return $m_sr_BoxesRunTime$().equals__O__O__Z(this.s_Some__f_value, Some$1.s_Some__f_value)
    } else {
      return false
    }
  };
}
function $as_s_Some(obj) {
  return (((obj instanceof $c_s_Some) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.Some"))
}
function $isArrayOf_s_Some(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.s_Some)))
}
function $asArrayOf_s_Some(obj, depth) {
  return (($isArrayOf_s_Some(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.Some;", depth))
}
const $d_s_Some = new $TypeData().initClass({
  s_Some: 0
}, false, "scala.Some", {
  s_Some: 1,
  s_Option: 1,
  O: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_s_Some.prototype.$classData = $d_s_Some;
const $ct_T2__O__O__ = (function($thiz, _1, _2) {
  $thiz.T2__f__1 = _1;
  $thiz.T2__f__2 = _2;
  return $thiz
});
class $c_T2 extends $c_O {
  constructor() {
    super();
    this.T2__f__1 = null;
    this.T2__f__2 = null
  };
  productArity__I() {
    return 2
  };
  productElement__I__O(n) {
    return $f_s_Product2__productElement__I__O(this, n)
  };
  _1__O() {
    return this.T2__f__1
  };
  _2__O() {
    return this.T2__f__2
  };
  toString__T() {
    return (((("(" + this._1__O()) + ",") + this._2__O()) + ")")
  };
  productPrefix__T() {
    return "Tuple2"
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    const this$2 = $m_s_util_hashing_MurmurHash3$();
    return this$2.productHash__s_Product__I__I(this, (-889275714))
  };
  equals__O__Z(x$1) {
    if ((this === x$1)) {
      return true
    } else if ((x$1 instanceof $c_T2)) {
      const Tuple2$1 = $as_T2(x$1);
      return ($m_sr_BoxesRunTime$().equals__O__O__Z(this._1__O(), Tuple2$1._1__O()) && $m_sr_BoxesRunTime$().equals__O__O__Z(this._2__O(), Tuple2$1._2__O()))
    } else {
      return false
    }
  };
  _1$mcI$sp__I() {
    return $uI(this._1__O())
  };
  _2$mcI$sp__I() {
    return $uI(this._2__O())
  };
}
function $as_T2(obj) {
  return (((obj instanceof $c_T2) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.Tuple2"))
}
function $isArrayOf_T2(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.T2)))
}
function $asArrayOf_T2(obj, depth) {
  return (($isArrayOf_T2(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.Tuple2;", depth))
}
const $d_T2 = new $TypeData().initClass({
  T2: 0
}, false, "scala.Tuple2", {
  T2: 1,
  O: 1,
  s_Product2: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_T2.prototype.$classData = $d_T2;
const $f_sc_GenSeqLike__equals__O__Z = (function($thiz, that) {
  if ($is_sc_GenSeq(that)) {
    const x2 = $as_sc_GenSeq(that);
    return ((x2 === $thiz) || $thiz.sameElements__sc_GenIterable__Z(x2))
  } else {
    return false
  }
});
function $is_sc_GenTraversable(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.sc_GenTraversable)))
}
function $as_sc_GenTraversable(obj) {
  return (($is_sc_GenTraversable(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.GenTraversable"))
}
function $isArrayOf_sc_GenTraversable(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sc_GenTraversable)))
}
function $asArrayOf_sc_GenTraversable(obj, depth) {
  return (($isArrayOf_sc_GenTraversable(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.GenTraversable;", depth))
}
class $c_scg_SeqFactory extends $c_scg_GenSeqFactory {
}
class $c_sci_Stream$StreamBuilder extends $c_scm_LazyBuilder {
  constructor() {
    super();
    $ct_scm_LazyBuilder__(this)
  };
  result__sci_Stream() {
    const this$1 = this.scm_LazyBuilder__f_parts;
    return $as_sci_Stream(this$1.scm_ListBuffer__f_scala$collection$mutable$ListBuffer$$start.toStream__sci_Stream().flatMap__F1__scg_CanBuildFrom__O(new $c_sjsr_AnonFunction1(((this$2) => ((x$5$2) => {
      const x$5 = $as_sc_TraversableOnce(x$5$2);
      return x$5.toStream__sci_Stream()
    }))(this)), ($m_sci_Stream$(), new $c_sci_Stream$StreamCanBuildFrom())))
  };
  result__O() {
    return this.result__sci_Stream()
  };
}
function $as_sci_Stream$StreamBuilder(obj) {
  return (((obj instanceof $c_sci_Stream$StreamBuilder) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.immutable.Stream$StreamBuilder"))
}
function $isArrayOf_sci_Stream$StreamBuilder(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sci_Stream$StreamBuilder)))
}
function $asArrayOf_sci_Stream$StreamBuilder(obj, depth) {
  return (($isArrayOf_sci_Stream$StreamBuilder(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.immutable.Stream$StreamBuilder;", depth))
}
const $d_sci_Stream$StreamBuilder = new $TypeData().initClass({
  sci_Stream$StreamBuilder: 0
}, false, "scala.collection.immutable.Stream$StreamBuilder", {
  sci_Stream$StreamBuilder: 1,
  scm_LazyBuilder: 1,
  O: 1,
  scm_ReusableBuilder: 1,
  scm_Builder: 1,
  scg_Growable: 1,
  scg_Clearable: 1
});
$c_sci_Stream$StreamBuilder.prototype.$classData = $d_sci_Stream$StreamBuilder;
class $c_sci_VectorBuilder extends $c_O {
  constructor() {
    super();
    this.sci_VectorBuilder__f_blockIndex = 0;
    this.sci_VectorBuilder__f_lo = 0;
    this.sci_VectorBuilder__f_depth = 0;
    this.sci_VectorBuilder__f_display0 = null;
    this.sci_VectorBuilder__f_display1 = null;
    this.sci_VectorBuilder__f_display2 = null;
    this.sci_VectorBuilder__f_display3 = null;
    this.sci_VectorBuilder__f_display4 = null;
    this.sci_VectorBuilder__f_display5 = null;
    this.sci_VectorBuilder__f_display0 = $newArrayObject($d_O.getArrayOf(), [32]);
    this.sci_VectorBuilder__f_depth = 1;
    this.sci_VectorBuilder__f_blockIndex = 0;
    this.sci_VectorBuilder__f_lo = 0
  };
  sizeHint__I__V(size) {
    /*<skip>*/
  };
  sizeHintBounded__I__sc_TraversableLike__V(size, boundingColl) {
    $f_scm_Builder__sizeHintBounded__I__sc_TraversableLike__V(this, size, boundingColl)
  };
  depth__I() {
    return this.sci_VectorBuilder__f_depth
  };
  depth_$eq__I__V(x$1) {
    this.sci_VectorBuilder__f_depth = x$1
  };
  display0__AO() {
    return this.sci_VectorBuilder__f_display0
  };
  display0_$eq__AO__V(x$1) {
    this.sci_VectorBuilder__f_display0 = x$1
  };
  display1__AO() {
    return this.sci_VectorBuilder__f_display1
  };
  display1_$eq__AO__V(x$1) {
    this.sci_VectorBuilder__f_display1 = x$1
  };
  display2__AO() {
    return this.sci_VectorBuilder__f_display2
  };
  display2_$eq__AO__V(x$1) {
    this.sci_VectorBuilder__f_display2 = x$1
  };
  display3__AO() {
    return this.sci_VectorBuilder__f_display3
  };
  display3_$eq__AO__V(x$1) {
    this.sci_VectorBuilder__f_display3 = x$1
  };
  display4__AO() {
    return this.sci_VectorBuilder__f_display4
  };
  display4_$eq__AO__V(x$1) {
    this.sci_VectorBuilder__f_display4 = x$1
  };
  display5__AO() {
    return this.sci_VectorBuilder__f_display5
  };
  display5_$eq__AO__V(x$1) {
    this.sci_VectorBuilder__f_display5 = x$1
  };
  $plus$eq__O__sci_VectorBuilder(elem) {
    if ((this.sci_VectorBuilder__f_lo >= this.sci_VectorBuilder__f_display0.u.length)) {
      const newBlockIndex = ((32 + this.sci_VectorBuilder__f_blockIndex) | 0);
      const xor = (this.sci_VectorBuilder__f_blockIndex ^ newBlockIndex);
      $f_sci_VectorPointer__gotoNextBlockStartWritable__I__I__V(this, newBlockIndex, xor);
      this.sci_VectorBuilder__f_blockIndex = newBlockIndex;
      this.sci_VectorBuilder__f_lo = 0
    };
    this.sci_VectorBuilder__f_display0.set(this.sci_VectorBuilder__f_lo, elem);
    this.sci_VectorBuilder__f_lo = ((1 + this.sci_VectorBuilder__f_lo) | 0);
    return this
  };
  result__sci_Vector() {
    const size = ((this.sci_VectorBuilder__f_blockIndex + this.sci_VectorBuilder__f_lo) | 0);
    if ((size === 0)) {
      const this$1 = $m_sci_Vector$();
      return this$1.sci_Vector$__f_NIL
    };
    const s = new $c_sci_Vector(0, size, 0);
    const depth = this.sci_VectorBuilder__f_depth;
    $f_sci_VectorPointer__initFrom__sci_VectorPointer__I__V(s, this, depth);
    if ((this.sci_VectorBuilder__f_depth > 1)) {
      const xor = (((-1) + size) | 0);
      $f_sci_VectorPointer__gotoPos__I__I__V(s, 0, xor)
    };
    return s
  };
  result__O() {
    return this.result__sci_Vector()
  };
  $plus$plus$eq__sc_TraversableOnce__scg_Growable(xs) {
    return $as_sci_VectorBuilder($f_scg_Growable__$plus$plus$eq__sc_TraversableOnce__scg_Growable(this, xs))
  };
  $plus$eq__O__scg_Growable(elem) {
    return this.$plus$eq__O__sci_VectorBuilder(elem)
  };
  $plus$eq__O__scm_Builder(elem) {
    return this.$plus$eq__O__sci_VectorBuilder(elem)
  };
}
function $as_sci_VectorBuilder(obj) {
  return (((obj instanceof $c_sci_VectorBuilder) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.immutable.VectorBuilder"))
}
function $isArrayOf_sci_VectorBuilder(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sci_VectorBuilder)))
}
function $asArrayOf_sci_VectorBuilder(obj, depth) {
  return (($isArrayOf_sci_VectorBuilder(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.immutable.VectorBuilder;", depth))
}
const $d_sci_VectorBuilder = new $TypeData().initClass({
  sci_VectorBuilder: 0
}, false, "scala.collection.immutable.VectorBuilder", {
  sci_VectorBuilder: 1,
  O: 1,
  scm_ReusableBuilder: 1,
  scm_Builder: 1,
  scg_Growable: 1,
  scg_Clearable: 1,
  sci_VectorPointer: 1
});
$c_sci_VectorBuilder.prototype.$classData = $d_sci_VectorBuilder;
class $c_sci_VectorIterator extends $c_sc_AbstractIterator {
  constructor(_startIndex, endIndex) {
    super();
    this.sci_VectorIterator__f_endIndex = 0;
    this.sci_VectorIterator__f_blockIndex = 0;
    this.sci_VectorIterator__f_lo = 0;
    this.sci_VectorIterator__f_endLo = 0;
    this.sci_VectorIterator__f__hasNext = false;
    this.sci_VectorIterator__f_depth = 0;
    this.sci_VectorIterator__f_display0 = null;
    this.sci_VectorIterator__f_display1 = null;
    this.sci_VectorIterator__f_display2 = null;
    this.sci_VectorIterator__f_display3 = null;
    this.sci_VectorIterator__f_display4 = null;
    this.sci_VectorIterator__f_display5 = null;
    this.sci_VectorIterator__f_endIndex = endIndex;
    this.sci_VectorIterator__f_blockIndex = ((-32) & _startIndex);
    this.sci_VectorIterator__f_lo = (31 & _startIndex);
    const x = ((endIndex - this.sci_VectorIterator__f_blockIndex) | 0);
    this.sci_VectorIterator__f_endLo = ((x < 32) ? x : 32);
    this.sci_VectorIterator__f__hasNext = (((this.sci_VectorIterator__f_blockIndex + this.sci_VectorIterator__f_lo) | 0) < endIndex)
  };
  depth__I() {
    return this.sci_VectorIterator__f_depth
  };
  depth_$eq__I__V(x$1) {
    this.sci_VectorIterator__f_depth = x$1
  };
  display0__AO() {
    return this.sci_VectorIterator__f_display0
  };
  display0_$eq__AO__V(x$1) {
    this.sci_VectorIterator__f_display0 = x$1
  };
  display1__AO() {
    return this.sci_VectorIterator__f_display1
  };
  display1_$eq__AO__V(x$1) {
    this.sci_VectorIterator__f_display1 = x$1
  };
  display2__AO() {
    return this.sci_VectorIterator__f_display2
  };
  display2_$eq__AO__V(x$1) {
    this.sci_VectorIterator__f_display2 = x$1
  };
  display3__AO() {
    return this.sci_VectorIterator__f_display3
  };
  display3_$eq__AO__V(x$1) {
    this.sci_VectorIterator__f_display3 = x$1
  };
  display4__AO() {
    return this.sci_VectorIterator__f_display4
  };
  display4_$eq__AO__V(x$1) {
    this.sci_VectorIterator__f_display4 = x$1
  };
  display5__AO() {
    return this.sci_VectorIterator__f_display5
  };
  display5_$eq__AO__V(x$1) {
    this.sci_VectorIterator__f_display5 = x$1
  };
  hasNext__Z() {
    return this.sci_VectorIterator__f__hasNext
  };
  next__O() {
    if ((!this.sci_VectorIterator__f__hasNext)) {
      throw $ct_ju_NoSuchElementException__T__(new $c_ju_NoSuchElementException(), "reached iterator end")
    };
    const res = this.sci_VectorIterator__f_display0.get(this.sci_VectorIterator__f_lo);
    this.sci_VectorIterator__f_lo = ((1 + this.sci_VectorIterator__f_lo) | 0);
    if ((this.sci_VectorIterator__f_lo === this.sci_VectorIterator__f_endLo)) {
      if ((((this.sci_VectorIterator__f_blockIndex + this.sci_VectorIterator__f_lo) | 0) < this.sci_VectorIterator__f_endIndex)) {
        const newBlockIndex = ((32 + this.sci_VectorIterator__f_blockIndex) | 0);
        const xor = (this.sci_VectorIterator__f_blockIndex ^ newBlockIndex);
        $f_sci_VectorPointer__gotoNextBlockStart__I__I__V(this, newBlockIndex, xor);
        this.sci_VectorIterator__f_blockIndex = newBlockIndex;
        const x = ((this.sci_VectorIterator__f_endIndex - this.sci_VectorIterator__f_blockIndex) | 0);
        this.sci_VectorIterator__f_endLo = ((x < 32) ? x : 32);
        this.sci_VectorIterator__f_lo = 0
      } else {
        this.sci_VectorIterator__f__hasNext = false
      }
    };
    return res
  };
}
const $d_sci_VectorIterator = new $TypeData().initClass({
  sci_VectorIterator: 0
}, false, "scala.collection.immutable.VectorIterator", {
  sci_VectorIterator: 1,
  sc_AbstractIterator: 1,
  O: 1,
  sc_Iterator: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1,
  sci_VectorPointer: 1
});
$c_sci_VectorIterator.prototype.$classData = $d_sci_VectorIterator;
class $c_Lsgl_GraphicsProvider$Graphics$Alignments$Center$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "Center"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 2014820469
  };
  toString__T() {
    return "Center"
  };
}
const $d_Lsgl_GraphicsProvider$Graphics$Alignments$Center$ = new $TypeData().initClass({
  Lsgl_GraphicsProvider$Graphics$Alignments$Center$: 0
}, false, "sgl.GraphicsProvider$Graphics$Alignments$Center$", {
  Lsgl_GraphicsProvider$Graphics$Alignments$Center$: 1,
  O: 1,
  Lsgl_GraphicsProvider$Graphics$Alignments$Alignment: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_GraphicsProvider$Graphics$Alignments$Center$.prototype.$classData = $d_Lsgl_GraphicsProvider$Graphics$Alignments$Center$;
class $c_Lsgl_GraphicsProvider$Graphics$Alignments$Left$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "Left"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 2364455
  };
  toString__T() {
    return "Left"
  };
}
const $d_Lsgl_GraphicsProvider$Graphics$Alignments$Left$ = new $TypeData().initClass({
  Lsgl_GraphicsProvider$Graphics$Alignments$Left$: 0
}, false, "sgl.GraphicsProvider$Graphics$Alignments$Left$", {
  Lsgl_GraphicsProvider$Graphics$Alignments$Left$: 1,
  O: 1,
  Lsgl_GraphicsProvider$Graphics$Alignments$Alignment: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_GraphicsProvider$Graphics$Alignments$Left$.prototype.$classData = $d_Lsgl_GraphicsProvider$Graphics$Alignments$Left$;
class $c_Lsgl_GraphicsProvider$Graphics$Alignments$Right$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "Right"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 78959100
  };
  toString__T() {
    return "Right"
  };
}
const $d_Lsgl_GraphicsProvider$Graphics$Alignments$Right$ = new $TypeData().initClass({
  Lsgl_GraphicsProvider$Graphics$Alignments$Right$: 0
}, false, "sgl.GraphicsProvider$Graphics$Alignments$Right$", {
  Lsgl_GraphicsProvider$Graphics$Alignments$Right$: 1,
  O: 1,
  Lsgl_GraphicsProvider$Graphics$Alignments$Alignment: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_GraphicsProvider$Graphics$Alignments$Right$.prototype.$classData = $d_Lsgl_GraphicsProvider$Graphics$Alignments$Right$;
class $c_Lsgl_GraphicsProvider$Graphics$FontCompanion$Bold$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "Bold"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 2076325
  };
  toString__T() {
    return "Bold"
  };
}
const $d_Lsgl_GraphicsProvider$Graphics$FontCompanion$Bold$ = new $TypeData().initClass({
  Lsgl_GraphicsProvider$Graphics$FontCompanion$Bold$: 0
}, false, "sgl.GraphicsProvider$Graphics$FontCompanion$Bold$", {
  Lsgl_GraphicsProvider$Graphics$FontCompanion$Bold$: 1,
  O: 1,
  Lsgl_GraphicsProvider$Graphics$FontCompanion$Style: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_GraphicsProvider$Graphics$FontCompanion$Bold$.prototype.$classData = $d_Lsgl_GraphicsProvider$Graphics$FontCompanion$Bold$;
class $c_Lsgl_GraphicsProvider$Graphics$FontCompanion$BoldItalic$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "BoldItalic"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 1152091445
  };
  toString__T() {
    return "BoldItalic"
  };
}
const $d_Lsgl_GraphicsProvider$Graphics$FontCompanion$BoldItalic$ = new $TypeData().initClass({
  Lsgl_GraphicsProvider$Graphics$FontCompanion$BoldItalic$: 0
}, false, "sgl.GraphicsProvider$Graphics$FontCompanion$BoldItalic$", {
  Lsgl_GraphicsProvider$Graphics$FontCompanion$BoldItalic$: 1,
  O: 1,
  Lsgl_GraphicsProvider$Graphics$FontCompanion$Style: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_GraphicsProvider$Graphics$FontCompanion$BoldItalic$.prototype.$classData = $d_Lsgl_GraphicsProvider$Graphics$FontCompanion$BoldItalic$;
class $c_Lsgl_GraphicsProvider$Graphics$FontCompanion$Italic$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "Italic"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return (-2094913968)
  };
  toString__T() {
    return "Italic"
  };
}
const $d_Lsgl_GraphicsProvider$Graphics$FontCompanion$Italic$ = new $TypeData().initClass({
  Lsgl_GraphicsProvider$Graphics$FontCompanion$Italic$: 0
}, false, "sgl.GraphicsProvider$Graphics$FontCompanion$Italic$", {
  Lsgl_GraphicsProvider$Graphics$FontCompanion$Italic$: 1,
  O: 1,
  Lsgl_GraphicsProvider$Graphics$FontCompanion$Style: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_GraphicsProvider$Graphics$FontCompanion$Italic$.prototype.$classData = $d_Lsgl_GraphicsProvider$Graphics$FontCompanion$Italic$;
class $c_Lsgl_GraphicsProvider$Graphics$FontCompanion$Normal$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "Normal"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return (-1955878649)
  };
  toString__T() {
    return "Normal"
  };
}
const $d_Lsgl_GraphicsProvider$Graphics$FontCompanion$Normal$ = new $TypeData().initClass({
  Lsgl_GraphicsProvider$Graphics$FontCompanion$Normal$: 0
}, false, "sgl.GraphicsProvider$Graphics$FontCompanion$Normal$", {
  Lsgl_GraphicsProvider$Graphics$FontCompanion$Normal$: 1,
  O: 1,
  Lsgl_GraphicsProvider$Graphics$FontCompanion$Style: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_GraphicsProvider$Graphics$FontCompanion$Normal$.prototype.$classData = $d_Lsgl_GraphicsProvider$Graphics$FontCompanion$Normal$;
class $c_Lsgl_InputProvider$Input$KeyDownEvent extends $c_O {
  constructor(outer, key) {
    super();
    this.Lsgl_InputProvider$Input$KeyDownEvent__f_key = null;
    this.Lsgl_InputProvider$Input$KeyDownEvent__f_$outer = null;
    this.Lsgl_InputProvider$Input$KeyDownEvent__f_key = key;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$KeyDownEvent__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "KeyDownEvent"
  };
  productArity__I() {
    return 1
  };
  productElement__I__O(x$1) {
    if ((x$1 === 0)) {
      return this.Lsgl_InputProvider$Input$KeyDownEvent__f_key
    } else {
      throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
    }
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    const this$2 = $m_s_util_hashing_MurmurHash3$();
    return this$2.productHash__s_Product__I__I(this, (-889275714))
  };
  toString__T() {
    return $m_sr_ScalaRunTime$()._toString__s_Product__T(this)
  };
  equals__O__Z(x$1) {
    if ((this === x$1)) {
      return true
    } else if (((x$1 instanceof $c_Lsgl_InputProvider$Input$KeyDownEvent) && ($as_Lsgl_InputProvider$Input$KeyDownEvent(x$1).Lsgl_InputProvider$Input$KeyDownEvent__f_$outer === this.Lsgl_InputProvider$Input$KeyDownEvent__f_$outer))) {
      const KeyDownEvent$1 = $as_Lsgl_InputProvider$Input$KeyDownEvent(x$1);
      const x = this.Lsgl_InputProvider$Input$KeyDownEvent__f_key;
      const x$2 = KeyDownEvent$1.Lsgl_InputProvider$Input$KeyDownEvent__f_key;
      return (x === x$2)
    } else {
      return false
    }
  };
}
function $as_Lsgl_InputProvider$Input$KeyDownEvent(obj) {
  return (((obj instanceof $c_Lsgl_InputProvider$Input$KeyDownEvent) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.InputProvider$Input$KeyDownEvent"))
}
function $isArrayOf_Lsgl_InputProvider$Input$KeyDownEvent(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_InputProvider$Input$KeyDownEvent)))
}
function $asArrayOf_Lsgl_InputProvider$Input$KeyDownEvent(obj, depth) {
  return (($isArrayOf_Lsgl_InputProvider$Input$KeyDownEvent(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.InputProvider$Input$KeyDownEvent;", depth))
}
const $d_Lsgl_InputProvider$Input$KeyDownEvent = new $TypeData().initClass({
  Lsgl_InputProvider$Input$KeyDownEvent: 0
}, false, "sgl.InputProvider$Input$KeyDownEvent", {
  Lsgl_InputProvider$Input$KeyDownEvent: 1,
  O: 1,
  Lsgl_InputProvider$Input$InputEvent: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$KeyDownEvent.prototype.$classData = $d_Lsgl_InputProvider$Input$KeyDownEvent;
class $c_Lsgl_InputProvider$Input$KeyUpEvent extends $c_O {
  constructor(outer, key) {
    super();
    this.Lsgl_InputProvider$Input$KeyUpEvent__f_key = null;
    this.Lsgl_InputProvider$Input$KeyUpEvent__f_$outer = null;
    this.Lsgl_InputProvider$Input$KeyUpEvent__f_key = key;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$KeyUpEvent__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "KeyUpEvent"
  };
  productArity__I() {
    return 1
  };
  productElement__I__O(x$1) {
    if ((x$1 === 0)) {
      return this.Lsgl_InputProvider$Input$KeyUpEvent__f_key
    } else {
      throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
    }
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    const this$2 = $m_s_util_hashing_MurmurHash3$();
    return this$2.productHash__s_Product__I__I(this, (-889275714))
  };
  toString__T() {
    return $m_sr_ScalaRunTime$()._toString__s_Product__T(this)
  };
  equals__O__Z(x$1) {
    if ((this === x$1)) {
      return true
    } else if (((x$1 instanceof $c_Lsgl_InputProvider$Input$KeyUpEvent) && ($as_Lsgl_InputProvider$Input$KeyUpEvent(x$1).Lsgl_InputProvider$Input$KeyUpEvent__f_$outer === this.Lsgl_InputProvider$Input$KeyUpEvent__f_$outer))) {
      const KeyUpEvent$1 = $as_Lsgl_InputProvider$Input$KeyUpEvent(x$1);
      const x = this.Lsgl_InputProvider$Input$KeyUpEvent__f_key;
      const x$2 = KeyUpEvent$1.Lsgl_InputProvider$Input$KeyUpEvent__f_key;
      return (x === x$2)
    } else {
      return false
    }
  };
}
function $as_Lsgl_InputProvider$Input$KeyUpEvent(obj) {
  return (((obj instanceof $c_Lsgl_InputProvider$Input$KeyUpEvent) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.InputProvider$Input$KeyUpEvent"))
}
function $isArrayOf_Lsgl_InputProvider$Input$KeyUpEvent(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_InputProvider$Input$KeyUpEvent)))
}
function $asArrayOf_Lsgl_InputProvider$Input$KeyUpEvent(obj, depth) {
  return (($isArrayOf_Lsgl_InputProvider$Input$KeyUpEvent(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.InputProvider$Input$KeyUpEvent;", depth))
}
const $d_Lsgl_InputProvider$Input$KeyUpEvent = new $TypeData().initClass({
  Lsgl_InputProvider$Input$KeyUpEvent: 0
}, false, "sgl.InputProvider$Input$KeyUpEvent", {
  Lsgl_InputProvider$Input$KeyUpEvent: 1,
  O: 1,
  Lsgl_InputProvider$Input$InputEvent: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$KeyUpEvent.prototype.$classData = $d_Lsgl_InputProvider$Input$KeyUpEvent;
class $c_Lsgl_InputProvider$Input$Keys$A$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "A"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 65
  };
  toString__T() {
    return "A"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$A$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$A$: 0
}, false, "sgl.InputProvider$Input$Keys$A$", {
  Lsgl_InputProvider$Input$Keys$A$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$A$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$A$;
class $c_Lsgl_InputProvider$Input$Keys$B$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "B"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 66
  };
  toString__T() {
    return "B"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$B$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$B$: 0
}, false, "sgl.InputProvider$Input$Keys$B$", {
  Lsgl_InputProvider$Input$Keys$B$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$B$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$B$;
class $c_Lsgl_InputProvider$Input$Keys$C$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "C"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 67
  };
  toString__T() {
    return "C"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$C$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$C$: 0
}, false, "sgl.InputProvider$Input$Keys$C$", {
  Lsgl_InputProvider$Input$Keys$C$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$C$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$C$;
class $c_Lsgl_InputProvider$Input$Keys$D$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "D"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 68
  };
  toString__T() {
    return "D"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$D$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$D$: 0
}, false, "sgl.InputProvider$Input$Keys$D$", {
  Lsgl_InputProvider$Input$Keys$D$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$D$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$D$;
class $c_Lsgl_InputProvider$Input$Keys$Down$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "Down"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 2136258
  };
  toString__T() {
    return "Down"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$Down$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$Down$: 0
}, false, "sgl.InputProvider$Input$Keys$Down$", {
  Lsgl_InputProvider$Input$Keys$Down$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$Down$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$Down$;
class $c_Lsgl_InputProvider$Input$Keys$E$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "E"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 69
  };
  toString__T() {
    return "E"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$E$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$E$: 0
}, false, "sgl.InputProvider$Input$Keys$E$", {
  Lsgl_InputProvider$Input$Keys$E$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$E$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$E$;
class $c_Lsgl_InputProvider$Input$Keys$F$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "F"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 70
  };
  toString__T() {
    return "F"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$F$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$F$: 0
}, false, "sgl.InputProvider$Input$Keys$F$", {
  Lsgl_InputProvider$Input$Keys$F$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$F$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$F$;
class $c_Lsgl_InputProvider$Input$Keys$G$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "G"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 71
  };
  toString__T() {
    return "G"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$G$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$G$: 0
}, false, "sgl.InputProvider$Input$Keys$G$", {
  Lsgl_InputProvider$Input$Keys$G$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$G$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$G$;
class $c_Lsgl_InputProvider$Input$Keys$H$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "H"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 72
  };
  toString__T() {
    return "H"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$H$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$H$: 0
}, false, "sgl.InputProvider$Input$Keys$H$", {
  Lsgl_InputProvider$Input$Keys$H$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$H$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$H$;
class $c_Lsgl_InputProvider$Input$Keys$I$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "I"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 73
  };
  toString__T() {
    return "I"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$I$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$I$: 0
}, false, "sgl.InputProvider$Input$Keys$I$", {
  Lsgl_InputProvider$Input$Keys$I$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$I$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$I$;
class $c_Lsgl_InputProvider$Input$Keys$J$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "J"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 74
  };
  toString__T() {
    return "J"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$J$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$J$: 0
}, false, "sgl.InputProvider$Input$Keys$J$", {
  Lsgl_InputProvider$Input$Keys$J$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$J$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$J$;
class $c_Lsgl_InputProvider$Input$Keys$K$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "K"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 75
  };
  toString__T() {
    return "K"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$K$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$K$: 0
}, false, "sgl.InputProvider$Input$Keys$K$", {
  Lsgl_InputProvider$Input$Keys$K$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$K$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$K$;
class $c_Lsgl_InputProvider$Input$Keys$L$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "L"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 76
  };
  toString__T() {
    return "L"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$L$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$L$: 0
}, false, "sgl.InputProvider$Input$Keys$L$", {
  Lsgl_InputProvider$Input$Keys$L$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$L$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$L$;
class $c_Lsgl_InputProvider$Input$Keys$Left$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "Left"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 2364455
  };
  toString__T() {
    return "Left"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$Left$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$Left$: 0
}, false, "sgl.InputProvider$Input$Keys$Left$", {
  Lsgl_InputProvider$Input$Keys$Left$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$Left$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$Left$;
class $c_Lsgl_InputProvider$Input$Keys$M$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "M"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 77
  };
  toString__T() {
    return "M"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$M$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$M$: 0
}, false, "sgl.InputProvider$Input$Keys$M$", {
  Lsgl_InputProvider$Input$Keys$M$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$M$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$M$;
class $c_Lsgl_InputProvider$Input$Keys$N$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "N"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 78
  };
  toString__T() {
    return "N"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$N$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$N$: 0
}, false, "sgl.InputProvider$Input$Keys$N$", {
  Lsgl_InputProvider$Input$Keys$N$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$N$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$N$;
class $c_Lsgl_InputProvider$Input$Keys$O$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "O"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 79
  };
  toString__T() {
    return "O"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$O$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$O$: 0
}, false, "sgl.InputProvider$Input$Keys$O$", {
  Lsgl_InputProvider$Input$Keys$O$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$O$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$O$;
class $c_Lsgl_InputProvider$Input$Keys$P$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "P"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 80
  };
  toString__T() {
    return "P"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$P$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$P$: 0
}, false, "sgl.InputProvider$Input$Keys$P$", {
  Lsgl_InputProvider$Input$Keys$P$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$P$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$P$;
class $c_Lsgl_InputProvider$Input$Keys$Q$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "Q"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 81
  };
  toString__T() {
    return "Q"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$Q$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$Q$: 0
}, false, "sgl.InputProvider$Input$Keys$Q$", {
  Lsgl_InputProvider$Input$Keys$Q$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$Q$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$Q$;
class $c_Lsgl_InputProvider$Input$Keys$R$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "R"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 82
  };
  toString__T() {
    return "R"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$R$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$R$: 0
}, false, "sgl.InputProvider$Input$Keys$R$", {
  Lsgl_InputProvider$Input$Keys$R$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$R$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$R$;
class $c_Lsgl_InputProvider$Input$Keys$Right$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "Right"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 78959100
  };
  toString__T() {
    return "Right"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$Right$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$Right$: 0
}, false, "sgl.InputProvider$Input$Keys$Right$", {
  Lsgl_InputProvider$Input$Keys$Right$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$Right$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$Right$;
class $c_Lsgl_InputProvider$Input$Keys$S$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "S"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 83
  };
  toString__T() {
    return "S"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$S$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$S$: 0
}, false, "sgl.InputProvider$Input$Keys$S$", {
  Lsgl_InputProvider$Input$Keys$S$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$S$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$S$;
class $c_Lsgl_InputProvider$Input$Keys$Space$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "Space"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 80085222
  };
  toString__T() {
    return "Space"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$Space$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$Space$: 0
}, false, "sgl.InputProvider$Input$Keys$Space$", {
  Lsgl_InputProvider$Input$Keys$Space$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$Space$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$Space$;
class $c_Lsgl_InputProvider$Input$Keys$T$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "T"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 84
  };
  toString__T() {
    return "T"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$T$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$T$: 0
}, false, "sgl.InputProvider$Input$Keys$T$", {
  Lsgl_InputProvider$Input$Keys$T$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$T$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$T$;
class $c_Lsgl_InputProvider$Input$Keys$U$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "U"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 85
  };
  toString__T() {
    return "U"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$U$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$U$: 0
}, false, "sgl.InputProvider$Input$Keys$U$", {
  Lsgl_InputProvider$Input$Keys$U$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$U$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$U$;
class $c_Lsgl_InputProvider$Input$Keys$Up$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "Up"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 2747
  };
  toString__T() {
    return "Up"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$Up$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$Up$: 0
}, false, "sgl.InputProvider$Input$Keys$Up$", {
  Lsgl_InputProvider$Input$Keys$Up$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$Up$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$Up$;
class $c_Lsgl_InputProvider$Input$Keys$V$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "V"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 86
  };
  toString__T() {
    return "V"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$V$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$V$: 0
}, false, "sgl.InputProvider$Input$Keys$V$", {
  Lsgl_InputProvider$Input$Keys$V$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$V$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$V$;
class $c_Lsgl_InputProvider$Input$Keys$W$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "W"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 87
  };
  toString__T() {
    return "W"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$W$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$W$: 0
}, false, "sgl.InputProvider$Input$Keys$W$", {
  Lsgl_InputProvider$Input$Keys$W$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$W$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$W$;
class $c_Lsgl_InputProvider$Input$Keys$X$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "X"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 88
  };
  toString__T() {
    return "X"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$X$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$X$: 0
}, false, "sgl.InputProvider$Input$Keys$X$", {
  Lsgl_InputProvider$Input$Keys$X$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$X$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$X$;
class $c_Lsgl_InputProvider$Input$Keys$Y$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "Y"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 89
  };
  toString__T() {
    return "Y"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$Y$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$Y$: 0
}, false, "sgl.InputProvider$Input$Keys$Y$", {
  Lsgl_InputProvider$Input$Keys$Y$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$Y$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$Y$;
class $c_Lsgl_InputProvider$Input$Keys$Z$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "Z"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 90
  };
  toString__T() {
    return "Z"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$Z$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$Z$: 0
}, false, "sgl.InputProvider$Input$Keys$Z$", {
  Lsgl_InputProvider$Input$Keys$Z$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$Z$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$Z$;
class $c_Lsgl_InputProvider$Input$MouseButtons$Left$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "Left"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 2364455
  };
  toString__T() {
    return "Left"
  };
}
const $d_Lsgl_InputProvider$Input$MouseButtons$Left$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$MouseButtons$Left$: 0
}, false, "sgl.InputProvider$Input$MouseButtons$Left$", {
  Lsgl_InputProvider$Input$MouseButtons$Left$: 1,
  O: 1,
  Lsgl_InputProvider$Input$MouseButtons$MouseButton: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$MouseButtons$Left$.prototype.$classData = $d_Lsgl_InputProvider$Input$MouseButtons$Left$;
class $c_Lsgl_InputProvider$Input$MouseButtons$Middle$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "Middle"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return (-1990474315)
  };
  toString__T() {
    return "Middle"
  };
}
const $d_Lsgl_InputProvider$Input$MouseButtons$Middle$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$MouseButtons$Middle$: 0
}, false, "sgl.InputProvider$Input$MouseButtons$Middle$", {
  Lsgl_InputProvider$Input$MouseButtons$Middle$: 1,
  O: 1,
  Lsgl_InputProvider$Input$MouseButtons$MouseButton: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$MouseButtons$Middle$.prototype.$classData = $d_Lsgl_InputProvider$Input$MouseButtons$Middle$;
class $c_Lsgl_InputProvider$Input$MouseButtons$Right$ extends $c_O {
  constructor(outer) {
    super()
  };
  productPrefix__T() {
    return "Right"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 78959100
  };
  toString__T() {
    return "Right"
  };
}
const $d_Lsgl_InputProvider$Input$MouseButtons$Right$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$MouseButtons$Right$: 0
}, false, "sgl.InputProvider$Input$MouseButtons$Right$", {
  Lsgl_InputProvider$Input$MouseButtons$Right$: 1,
  O: 1,
  Lsgl_InputProvider$Input$MouseButtons$MouseButton: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$MouseButtons$Right$.prototype.$classData = $d_Lsgl_InputProvider$Input$MouseButtons$Right$;
class $c_Lsgl_PartsResourcePathProvider$PartsResourcePath extends $c_O {
  constructor(outer, parts) {
    super();
    this.Lsgl_PartsResourcePathProvider$PartsResourcePath__f_parts = null;
    this.Lsgl_PartsResourcePathProvider$PartsResourcePath__f_$outer = null;
    this.Lsgl_PartsResourcePathProvider$PartsResourcePath__f_parts = parts;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_PartsResourcePathProvider$PartsResourcePath__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "PartsResourcePath"
  };
  productArity__I() {
    return 1
  };
  productElement__I__O(x$1) {
    if ((x$1 === 0)) {
      return this.Lsgl_PartsResourcePathProvider$PartsResourcePath__f_parts
    } else {
      throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
    }
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    const this$2 = $m_s_util_hashing_MurmurHash3$();
    return this$2.productHash__s_Product__I__I(this, (-889275714))
  };
  toString__T() {
    return $m_sr_ScalaRunTime$()._toString__s_Product__T(this)
  };
  equals__O__Z(x$1) {
    if ((this === x$1)) {
      return true
    } else if (((x$1 instanceof $c_Lsgl_PartsResourcePathProvider$PartsResourcePath) && ($as_Lsgl_PartsResourcePathProvider$PartsResourcePath(x$1).Lsgl_PartsResourcePathProvider$PartsResourcePath__f_$outer === this.Lsgl_PartsResourcePathProvider$PartsResourcePath__f_$outer))) {
      const PartsResourcePath$1 = $as_Lsgl_PartsResourcePathProvider$PartsResourcePath(x$1);
      const x = this.Lsgl_PartsResourcePathProvider$PartsResourcePath__f_parts;
      const x$2 = PartsResourcePath$1.Lsgl_PartsResourcePathProvider$PartsResourcePath__f_parts;
      return ((x === null) ? (x$2 === null) : $f_sc_GenSeqLike__equals__O__Z(x, x$2))
    } else {
      return false
    }
  };
}
function $as_Lsgl_PartsResourcePathProvider$PartsResourcePath(obj) {
  return (((obj instanceof $c_Lsgl_PartsResourcePathProvider$PartsResourcePath) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.PartsResourcePathProvider$PartsResourcePath"))
}
function $isArrayOf_Lsgl_PartsResourcePathProvider$PartsResourcePath(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_PartsResourcePathProvider$PartsResourcePath)))
}
function $asArrayOf_Lsgl_PartsResourcePathProvider$PartsResourcePath(obj, depth) {
  return (($isArrayOf_Lsgl_PartsResourcePathProvider$PartsResourcePath(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.PartsResourcePathProvider$PartsResourcePath;", depth))
}
const $d_Lsgl_PartsResourcePathProvider$PartsResourcePath = new $TypeData().initClass({
  Lsgl_PartsResourcePathProvider$PartsResourcePath: 0
}, false, "sgl.PartsResourcePathProvider$PartsResourcePath", {
  Lsgl_PartsResourcePathProvider$PartsResourcePath: 1,
  O: 1,
  Lsgl_SystemProvider$AbstractResourcePath: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_PartsResourcePathProvider$PartsResourcePath.prototype.$classData = $d_Lsgl_PartsResourcePathProvider$PartsResourcePath;
class $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font extends $c_Lsgl_GraphicsProvider$Graphics$AbstractFont {
  constructor(outer, family, style, size) {
    super();
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_family = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_style = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_size = 0;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_family = family;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_style = style;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_size = size;
    $ct_Lsgl_GraphicsProvider$Graphics$AbstractFont__Lsgl_GraphicsProvider$Graphics__(this, outer)
  };
  asCss__T() {
    const scss = $as_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$(this.Lsgl_GraphicsProvider$Graphics$AbstractFont__f_$outer).Lsgl_html5_Html5GraphicsProvider$Html5Graphics$__f_Font.toCssStyle__Lsgl_GraphicsProvider$Graphics$FontCompanion$Style__T(this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_style);
    return ((((scss + " ") + this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_size) + "px ") + this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_family)
  };
  productPrefix__T() {
    return "Html5Font"
  };
  productArity__I() {
    return 3
  };
  productElement__I__O(x$1) {
    switch (x$1) {
      case 0: {
        return this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_family;
        break
      }
      case 1: {
        return this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_style;
        break
      }
      case 2: {
        return this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_size;
        break
      }
      default: {
        throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
      }
    }
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    let acc = (-889275714);
    const hash = acc;
    const x = this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_family;
    const data = $m_sr_Statics$().anyHash__O__I(x);
    acc = $m_sr_Statics$().mix__I__I__I(hash, data);
    const hash$1 = acc;
    const x$1 = this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_style;
    const data$1 = $m_sr_Statics$().anyHash__O__I(x$1);
    acc = $m_sr_Statics$().mix__I__I__I(hash$1, data$1);
    const hash$2 = acc;
    const data$2 = this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_size;
    acc = $m_sr_Statics$().mix__I__I__I(hash$2, data$2);
    const hash$3 = acc;
    return $m_sr_Statics$().finalizeHash__I__I__I(hash$3, 3)
  };
  toString__T() {
    return $m_sr_ScalaRunTime$()._toString__s_Product__T(this)
  };
  equals__O__Z(x$1) {
    if ((this === x$1)) {
      return true
    } else {
      let $$x1;
      if ((x$1 instanceof $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font)) {
        const this$1 = $as_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font(x$1);
        $$x1 = ($as_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$(this$1.Lsgl_GraphicsProvider$Graphics$AbstractFont__f_$outer) === $as_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$(this.Lsgl_GraphicsProvider$Graphics$AbstractFont__f_$outer))
      } else {
        $$x1 = false
      };
      if ($$x1) {
        const Html5Font$1 = $as_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font(x$1);
        let $$x2;
        if ((this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_family === Html5Font$1.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_family)) {
          const x = this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_style;
          const x$2 = Html5Font$1.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_style;
          $$x2 = (x === x$2)
        } else {
          $$x2 = false
        };
        if ($$x2) {
          return (this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_size === Html5Font$1.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font__f_size)
        } else {
          return false
        }
      } else {
        return false
      }
    }
  };
}
function $as_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font(obj) {
  return (((obj instanceof $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.html5.Html5GraphicsProvider$Html5Graphics$Html5Font"))
}
function $isArrayOf_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font)))
}
function $asArrayOf_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font(obj, depth) {
  return (($isArrayOf_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.html5.Html5GraphicsProvider$Html5Graphics$Html5Font;", depth))
}
const $d_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font = new $TypeData().initClass({
  Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font: 0
}, false, "sgl.html5.Html5GraphicsProvider$Html5Graphics$Html5Font", {
  Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font: 1,
  Lsgl_GraphicsProvider$Graphics$AbstractFont: 1,
  O: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font.prototype.$classData = $d_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Font;
class $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint extends $c_O {
  constructor(outer, font, color, alignment) {
    super();
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_font = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_color = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_alignment = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_alignmentRaw = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_$outer = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_font = font;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_color = color;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_alignment = alignment;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_$outer = outer
    };
    const x = outer.Alignments__Lsgl_GraphicsProvider$Graphics$Alignments$().Left__Lsgl_GraphicsProvider$Graphics$Alignments$Left$();
    let $$x1;
    if ((x === alignment)) {
      $$x1 = "left"
    } else {
      const x$3 = outer.Alignments__Lsgl_GraphicsProvider$Graphics$Alignments$().Center__Lsgl_GraphicsProvider$Graphics$Alignments$Center$();
      if ((x$3 === alignment)) {
        $$x1 = "center"
      } else {
        const x$5 = outer.Alignments__Lsgl_GraphicsProvider$Graphics$Alignments$().Right__Lsgl_GraphicsProvider$Graphics$Alignments$Right$();
        if ((x$5 === alignment)) {
          $$x1 = "right"
        } else {
          throw new $c_s_MatchError(alignment)
        }
      }
    };
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_alignmentRaw = $$x1
  };
  withColor__T__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint(c) {
    const x$2 = this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_font;
    const x$3 = this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_alignment;
    return new $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint(this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_$outer, x$2, c, x$3)
  };
  prepareContext__Lorg_scalajs_dom_raw_CanvasRenderingContext2D__V(ctx) {
    const s = this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_color;
    ctx.fillStyle = s;
    const s$1 = this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_color;
    ctx.strokeStyle = s$1;
    ctx.font = this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_font.asCss__T();
    ctx.textAlign = this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_alignmentRaw
  };
  productPrefix__T() {
    return "Html5Paint"
  };
  productArity__I() {
    return 3
  };
  productElement__I__O(x$1) {
    switch (x$1) {
      case 0: {
        return this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_font;
        break
      }
      case 1: {
        return this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_color;
        break
      }
      case 2: {
        return this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_alignment;
        break
      }
      default: {
        throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
      }
    }
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    const this$2 = $m_s_util_hashing_MurmurHash3$();
    return this$2.productHash__s_Product__I__I(this, (-889275714))
  };
  toString__T() {
    return $m_sr_ScalaRunTime$()._toString__s_Product__T(this)
  };
  equals__O__Z(x$1) {
    if ((this === x$1)) {
      return true
    } else if (((x$1 instanceof $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint) && ($as_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint(x$1).Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_$outer === this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_$outer))) {
      const Html5Paint$1 = $as_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint(x$1);
      const x = this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_font;
      const x$2 = Html5Paint$1.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_font;
      if ((((x === null) ? (x$2 === null) : x.equals__O__Z(x$2)) && (this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_color === Html5Paint$1.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_color))) {
        const x$3 = this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_alignment;
        const x$4 = Html5Paint$1.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__f_alignment;
        return (x$3 === x$4)
      } else {
        return false
      }
    } else {
      return false
    }
  };
}
function $as_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint(obj) {
  return (((obj instanceof $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.html5.Html5GraphicsProvider$Html5Graphics$Html5Paint"))
}
function $isArrayOf_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint)))
}
function $asArrayOf_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint(obj, depth) {
  return (($isArrayOf_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.html5.Html5GraphicsProvider$Html5Graphics$Html5Paint;", depth))
}
const $d_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint = new $TypeData().initClass({
  Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint: 0
}, false, "sgl.html5.Html5GraphicsProvider$Html5Graphics$Html5Paint", {
  Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint: 1,
  O: 1,
  Lsgl_GraphicsProvider$Graphics$AbstractPaint: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint.prototype.$classData = $d_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint;
class $c_Lsgl_util_ChunkedTask$InProgress$ extends $c_Lsgl_util_ChunkedTask$Status {
  productPrefix__T() {
    return "InProgress"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 646453906
  };
  toString__T() {
    return "InProgress"
  };
}
const $d_Lsgl_util_ChunkedTask$InProgress$ = new $TypeData().initClass({
  Lsgl_util_ChunkedTask$InProgress$: 0
}, false, "sgl.util.ChunkedTask$InProgress$", {
  Lsgl_util_ChunkedTask$InProgress$: 1,
  Lsgl_util_ChunkedTask$Status: 1,
  O: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_util_ChunkedTask$InProgress$.prototype.$classData = $d_Lsgl_util_ChunkedTask$InProgress$;
let $n_Lsgl_util_ChunkedTask$InProgress$ = (void 0);
function $m_Lsgl_util_ChunkedTask$InProgress$() {
  if ((!$n_Lsgl_util_ChunkedTask$InProgress$)) {
    $n_Lsgl_util_ChunkedTask$InProgress$ = new $c_Lsgl_util_ChunkedTask$InProgress$()
  };
  return $n_Lsgl_util_ChunkedTask$InProgress$
}
class $c_sc_Seq$ extends $c_scg_SeqFactory {
  constructor() {
    super();
    $ct_scg_GenTraversableFactory__(this)
  };
  newBuilder__scm_Builder() {
    $m_sci_Seq$();
    return new $c_scm_ListBuffer()
  };
}
const $d_sc_Seq$ = new $TypeData().initClass({
  sc_Seq$: 0
}, false, "scala.collection.Seq$", {
  sc_Seq$: 1,
  scg_SeqFactory: 1,
  scg_GenSeqFactory: 1,
  scg_GenTraversableFactory: 1,
  scg_GenericCompanion: 1,
  O: 1,
  scg_TraversableFactory: 1,
  scg_GenericSeqCompanion: 1
});
$c_sc_Seq$.prototype.$classData = $d_sc_Seq$;
let $n_sc_Seq$ = (void 0);
function $m_sc_Seq$() {
  if ((!$n_sc_Seq$)) {
    $n_sc_Seq$ = new $c_sc_Seq$()
  };
  return $n_sc_Seq$
}
const $p_sc_TraversableLike__isPartLikelySynthetic$1__T__I__Z = (function($thiz, fqn$1, partStart$1) {
  const firstChar = (65535 & $uI(fqn$1.charCodeAt(partStart$1)));
  return (((firstChar > 90) && (firstChar < 127)) || (firstChar < 65))
});
const $f_sc_TraversableLike__$plus$plus__sc_GenTraversableOnce__scg_CanBuildFrom__O = (function($thiz, that, bf) {
  const b = bf.apply__O__scm_Builder($thiz.repr__O());
  if ($is_sc_IndexedSeqLike(that)) {
    const delta = that.seq__sc_TraversableOnce().size__I();
    $f_scm_Builder__sizeHint__sc_TraversableLike__I__V(b, $thiz, delta)
  };
  b.$plus$plus$eq__sc_TraversableOnce__scg_Growable($thiz.thisCollection__sc_Traversable());
  b.$plus$plus$eq__sc_TraversableOnce__scg_Growable(that.seq__sc_TraversableOnce());
  return b.result__O()
});
const $f_sc_TraversableLike__flatMap__F1__scg_CanBuildFrom__O = (function($thiz, f, bf) {
  const b = bf.apply__O__scm_Builder($thiz.repr__O());
  $thiz.foreach__F1__V(new $c_sjsr_AnonFunction1(((this$1, b$1, f$1) => ((x$2) => $as_scm_Builder(b$1.$plus$plus$eq__sc_TraversableOnce__scg_Growable($as_sc_GenTraversableOnce(f$1.apply__O__O(x$2)).seq__sc_TraversableOnce()))))($thiz, b, f)));
  return b.result__O()
});
const $f_sc_TraversableLike__to__scg_CanBuildFrom__O = (function($thiz, cbf) {
  const b = cbf.apply__scm_Builder();
  $f_scm_Builder__sizeHint__sc_TraversableLike__V(b, $thiz);
  b.$plus$plus$eq__sc_TraversableOnce__scg_Growable($thiz.thisCollection__sc_Traversable());
  return b.result__O()
});
const $f_sc_TraversableLike__toString__T = (function($thiz) {
  return $thiz.mkString__T__T__T__T(($thiz.stringPrefix__T() + "("), ", ", ")")
});
const $f_sc_TraversableLike__stringPrefix__T = (function($thiz) {
  const this$1 = $thiz.repr__O();
  const fqn = $objectClassName(this$1);
  let pos = (((-1) + $uI(fqn.length)) | 0);
  while (true) {
    let $$x1;
    if ((pos !== (-1))) {
      const index = pos;
      $$x1 = ((65535 & $uI(fqn.charCodeAt(index))) === 36)
    } else {
      $$x1 = false
    };
    if ($$x1) {
      pos = (((-1) + pos) | 0)
    } else {
      break
    }
  };
  let $$x2;
  if ((pos === (-1))) {
    $$x2 = true
  } else {
    const index$1 = pos;
    $$x2 = ((65535 & $uI(fqn.charCodeAt(index$1))) === 46)
  };
  if ($$x2) {
    return ""
  };
  let result = "";
  while (true) {
    const partEnd = ((1 + pos) | 0);
    while (true) {
      let $$x4;
      if ((pos !== (-1))) {
        const index$2 = pos;
        $$x4 = ((65535 & $uI(fqn.charCodeAt(index$2))) <= 57)
      } else {
        $$x4 = false
      };
      let $$x3;
      if ($$x4) {
        const index$3 = pos;
        $$x3 = ((65535 & $uI(fqn.charCodeAt(index$3))) >= 48)
      } else {
        $$x3 = false
      };
      if ($$x3) {
        pos = (((-1) + pos) | 0)
      } else {
        break
      }
    };
    const lastNonDigit = pos;
    while (true) {
      let $$x6;
      if ((pos !== (-1))) {
        const index$4 = pos;
        $$x6 = ((65535 & $uI(fqn.charCodeAt(index$4))) !== 36)
      } else {
        $$x6 = false
      };
      let $$x5;
      if ($$x6) {
        const index$5 = pos;
        $$x5 = ((65535 & $uI(fqn.charCodeAt(index$5))) !== 46)
      } else {
        $$x5 = false
      };
      if ($$x5) {
        pos = (((-1) + pos) | 0)
      } else {
        break
      }
    };
    const partStart = ((1 + pos) | 0);
    if (((pos === lastNonDigit) && (partEnd !== $uI(fqn.length)))) {
      return result
    };
    while (true) {
      let $$x7;
      if ((pos !== (-1))) {
        const index$6 = pos;
        $$x7 = ((65535 & $uI(fqn.charCodeAt(index$6))) === 36)
      } else {
        $$x7 = false
      };
      if ($$x7) {
        pos = (((-1) + pos) | 0)
      } else {
        break
      }
    };
    let atEnd;
    if ((pos === (-1))) {
      atEnd = true
    } else {
      const index$7 = pos;
      atEnd = ((65535 & $uI(fqn.charCodeAt(index$7))) === 46)
    };
    if ((atEnd || (!$p_sc_TraversableLike__isPartLikelySynthetic$1__T__I__Z($thiz, fqn, partStart)))) {
      const part = $as_T(fqn.substring(partStart, partEnd));
      const this$2 = result;
      if ((this$2 === "")) {
        result = part
      } else {
        result = ((part + ".") + result)
      };
      if (atEnd) {
        return result
      }
    }
  }
});
class $c_scg_IndexedSeqFactory extends $c_scg_SeqFactory {
}
class $c_sci_Seq$ extends $c_scg_SeqFactory {
  constructor() {
    super();
    $ct_scg_GenTraversableFactory__(this)
  };
  newBuilder__scm_Builder() {
    return new $c_scm_ListBuffer()
  };
}
const $d_sci_Seq$ = new $TypeData().initClass({
  sci_Seq$: 0
}, false, "scala.collection.immutable.Seq$", {
  sci_Seq$: 1,
  scg_SeqFactory: 1,
  scg_GenSeqFactory: 1,
  scg_GenTraversableFactory: 1,
  scg_GenericCompanion: 1,
  O: 1,
  scg_TraversableFactory: 1,
  scg_GenericSeqCompanion: 1
});
$c_sci_Seq$.prototype.$classData = $d_sci_Seq$;
let $n_sci_Seq$ = (void 0);
function $m_sci_Seq$() {
  if ((!$n_sci_Seq$)) {
    $n_sci_Seq$ = new $c_sci_Seq$()
  };
  return $n_sci_Seq$
}
class $c_scm_IndexedSeq$ extends $c_scg_SeqFactory {
  constructor() {
    super();
    $ct_scg_GenTraversableFactory__(this)
  };
  newBuilder__scm_Builder() {
    return $ct_scm_ArrayBuffer__(new $c_scm_ArrayBuffer())
  };
}
const $d_scm_IndexedSeq$ = new $TypeData().initClass({
  scm_IndexedSeq$: 0
}, false, "scala.collection.mutable.IndexedSeq$", {
  scm_IndexedSeq$: 1,
  scg_SeqFactory: 1,
  scg_GenSeqFactory: 1,
  scg_GenTraversableFactory: 1,
  scg_GenericCompanion: 1,
  O: 1,
  scg_TraversableFactory: 1,
  scg_GenericSeqCompanion: 1
});
$c_scm_IndexedSeq$.prototype.$classData = $d_scm_IndexedSeq$;
let $n_scm_IndexedSeq$ = (void 0);
function $m_scm_IndexedSeq$() {
  if ((!$n_scm_IndexedSeq$)) {
    $n_scm_IndexedSeq$ = new $c_scm_IndexedSeq$()
  };
  return $n_scm_IndexedSeq$
}
class $c_Lsgl_InputProvider$Input$Keys$Num0$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_InputProvider$Input$Keys$Num0$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$Keys$Num0$__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "Num0"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 2439562
  };
  toString__T() {
    return "Num0"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$Num0$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$Num0$: 0
}, false, "sgl.InputProvider$Input$Keys$Num0$", {
  Lsgl_InputProvider$Input$Keys$Num0$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$NumKey: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$Num0$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$Num0$;
class $c_Lsgl_InputProvider$Input$Keys$Num1$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_InputProvider$Input$Keys$Num1$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$Keys$Num1$__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "Num1"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 2439563
  };
  toString__T() {
    return "Num1"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$Num1$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$Num1$: 0
}, false, "sgl.InputProvider$Input$Keys$Num1$", {
  Lsgl_InputProvider$Input$Keys$Num1$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$NumKey: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$Num1$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$Num1$;
class $c_Lsgl_InputProvider$Input$Keys$Num2$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_InputProvider$Input$Keys$Num2$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$Keys$Num2$__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "Num2"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 2439564
  };
  toString__T() {
    return "Num2"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$Num2$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$Num2$: 0
}, false, "sgl.InputProvider$Input$Keys$Num2$", {
  Lsgl_InputProvider$Input$Keys$Num2$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$NumKey: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$Num2$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$Num2$;
class $c_Lsgl_InputProvider$Input$Keys$Num3$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_InputProvider$Input$Keys$Num3$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$Keys$Num3$__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "Num3"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 2439565
  };
  toString__T() {
    return "Num3"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$Num3$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$Num3$: 0
}, false, "sgl.InputProvider$Input$Keys$Num3$", {
  Lsgl_InputProvider$Input$Keys$Num3$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$NumKey: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$Num3$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$Num3$;
class $c_Lsgl_InputProvider$Input$Keys$Num4$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_InputProvider$Input$Keys$Num4$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$Keys$Num4$__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "Num4"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 2439566
  };
  toString__T() {
    return "Num4"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$Num4$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$Num4$: 0
}, false, "sgl.InputProvider$Input$Keys$Num4$", {
  Lsgl_InputProvider$Input$Keys$Num4$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$NumKey: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$Num4$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$Num4$;
class $c_Lsgl_InputProvider$Input$Keys$Num5$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_InputProvider$Input$Keys$Num5$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$Keys$Num5$__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "Num5"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 2439567
  };
  toString__T() {
    return "Num5"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$Num5$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$Num5$: 0
}, false, "sgl.InputProvider$Input$Keys$Num5$", {
  Lsgl_InputProvider$Input$Keys$Num5$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$NumKey: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$Num5$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$Num5$;
class $c_Lsgl_InputProvider$Input$Keys$Num6$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_InputProvider$Input$Keys$Num6$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$Keys$Num6$__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "Num6"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 2439568
  };
  toString__T() {
    return "Num6"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$Num6$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$Num6$: 0
}, false, "sgl.InputProvider$Input$Keys$Num6$", {
  Lsgl_InputProvider$Input$Keys$Num6$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$NumKey: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$Num6$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$Num6$;
class $c_Lsgl_InputProvider$Input$Keys$Num7$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_InputProvider$Input$Keys$Num7$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$Keys$Num7$__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "Num7"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 2439569
  };
  toString__T() {
    return "Num7"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$Num7$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$Num7$: 0
}, false, "sgl.InputProvider$Input$Keys$Num7$", {
  Lsgl_InputProvider$Input$Keys$Num7$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$NumKey: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$Num7$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$Num7$;
class $c_Lsgl_InputProvider$Input$Keys$Num8$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_InputProvider$Input$Keys$Num8$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$Keys$Num8$__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "Num8"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 2439570
  };
  toString__T() {
    return "Num8"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$Num8$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$Num8$: 0
}, false, "sgl.InputProvider$Input$Keys$Num8$", {
  Lsgl_InputProvider$Input$Keys$Num8$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$NumKey: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$Num8$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$Num8$;
class $c_Lsgl_InputProvider$Input$Keys$Num9$ extends $c_O {
  constructor(outer) {
    super();
    this.Lsgl_InputProvider$Input$Keys$Num9$__f_$outer = null;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$Keys$Num9$__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "Num9"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return 2439571
  };
  toString__T() {
    return "Num9"
  };
}
const $d_Lsgl_InputProvider$Input$Keys$Num9$ = new $TypeData().initClass({
  Lsgl_InputProvider$Input$Keys$Num9$: 0
}, false, "sgl.InputProvider$Input$Keys$Num9$", {
  Lsgl_InputProvider$Input$Keys$Num9$: 1,
  O: 1,
  Lsgl_InputProvider$Input$Keys$NumKey: 1,
  Lsgl_InputProvider$Input$Keys$Key: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$Keys$Num9$.prototype.$classData = $d_Lsgl_InputProvider$Input$Keys$Num9$;
class $c_Lsgl_InputProvider$Input$MouseDownEvent extends $c_O {
  constructor(outer, x, y, mouseButton) {
    super();
    this.Lsgl_InputProvider$Input$MouseDownEvent__f_x = 0;
    this.Lsgl_InputProvider$Input$MouseDownEvent__f_y = 0;
    this.Lsgl_InputProvider$Input$MouseDownEvent__f_mouseButton = null;
    this.Lsgl_InputProvider$Input$MouseDownEvent__f_$outer = null;
    this.Lsgl_InputProvider$Input$MouseDownEvent__f_x = x;
    this.Lsgl_InputProvider$Input$MouseDownEvent__f_y = y;
    this.Lsgl_InputProvider$Input$MouseDownEvent__f_mouseButton = mouseButton;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$MouseDownEvent__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "MouseDownEvent"
  };
  productArity__I() {
    return 3
  };
  productElement__I__O(x$1) {
    switch (x$1) {
      case 0: {
        return this.Lsgl_InputProvider$Input$MouseDownEvent__f_x;
        break
      }
      case 1: {
        return this.Lsgl_InputProvider$Input$MouseDownEvent__f_y;
        break
      }
      case 2: {
        return this.Lsgl_InputProvider$Input$MouseDownEvent__f_mouseButton;
        break
      }
      default: {
        throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
      }
    }
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    let acc = (-889275714);
    const hash = acc;
    const data = this.Lsgl_InputProvider$Input$MouseDownEvent__f_x;
    acc = $m_sr_Statics$().mix__I__I__I(hash, data);
    const hash$1 = acc;
    const data$1 = this.Lsgl_InputProvider$Input$MouseDownEvent__f_y;
    acc = $m_sr_Statics$().mix__I__I__I(hash$1, data$1);
    const hash$2 = acc;
    const x = this.Lsgl_InputProvider$Input$MouseDownEvent__f_mouseButton;
    const data$2 = $m_sr_Statics$().anyHash__O__I(x);
    acc = $m_sr_Statics$().mix__I__I__I(hash$2, data$2);
    const hash$3 = acc;
    return $m_sr_Statics$().finalizeHash__I__I__I(hash$3, 3)
  };
  toString__T() {
    return $m_sr_ScalaRunTime$()._toString__s_Product__T(this)
  };
  equals__O__Z(x$1) {
    if ((this === x$1)) {
      return true
    } else if (((x$1 instanceof $c_Lsgl_InputProvider$Input$MouseDownEvent) && ($as_Lsgl_InputProvider$Input$MouseDownEvent(x$1).Lsgl_InputProvider$Input$MouseDownEvent__f_$outer === this.Lsgl_InputProvider$Input$MouseDownEvent__f_$outer))) {
      const MouseDownEvent$1 = $as_Lsgl_InputProvider$Input$MouseDownEvent(x$1);
      if (((this.Lsgl_InputProvider$Input$MouseDownEvent__f_x === MouseDownEvent$1.Lsgl_InputProvider$Input$MouseDownEvent__f_x) && (this.Lsgl_InputProvider$Input$MouseDownEvent__f_y === MouseDownEvent$1.Lsgl_InputProvider$Input$MouseDownEvent__f_y))) {
        const x = this.Lsgl_InputProvider$Input$MouseDownEvent__f_mouseButton;
        const x$2 = MouseDownEvent$1.Lsgl_InputProvider$Input$MouseDownEvent__f_mouseButton;
        return (x === x$2)
      } else {
        return false
      }
    } else {
      return false
    }
  };
}
function $as_Lsgl_InputProvider$Input$MouseDownEvent(obj) {
  return (((obj instanceof $c_Lsgl_InputProvider$Input$MouseDownEvent) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.InputProvider$Input$MouseDownEvent"))
}
function $isArrayOf_Lsgl_InputProvider$Input$MouseDownEvent(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_InputProvider$Input$MouseDownEvent)))
}
function $asArrayOf_Lsgl_InputProvider$Input$MouseDownEvent(obj, depth) {
  return (($isArrayOf_Lsgl_InputProvider$Input$MouseDownEvent(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.InputProvider$Input$MouseDownEvent;", depth))
}
const $d_Lsgl_InputProvider$Input$MouseDownEvent = new $TypeData().initClass({
  Lsgl_InputProvider$Input$MouseDownEvent: 0
}, false, "sgl.InputProvider$Input$MouseDownEvent", {
  Lsgl_InputProvider$Input$MouseDownEvent: 1,
  O: 1,
  Lsgl_InputProvider$Input$MouseInputEvent: 1,
  Lsgl_InputProvider$Input$InputEvent: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$MouseDownEvent.prototype.$classData = $d_Lsgl_InputProvider$Input$MouseDownEvent;
class $c_Lsgl_InputProvider$Input$MouseMovedEvent extends $c_O {
  constructor(outer, x, y) {
    super();
    this.Lsgl_InputProvider$Input$MouseMovedEvent__f_x = 0;
    this.Lsgl_InputProvider$Input$MouseMovedEvent__f_y = 0;
    this.Lsgl_InputProvider$Input$MouseMovedEvent__f_$outer = null;
    this.Lsgl_InputProvider$Input$MouseMovedEvent__f_x = x;
    this.Lsgl_InputProvider$Input$MouseMovedEvent__f_y = y;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$MouseMovedEvent__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "MouseMovedEvent"
  };
  productArity__I() {
    return 2
  };
  productElement__I__O(x$1) {
    switch (x$1) {
      case 0: {
        return this.Lsgl_InputProvider$Input$MouseMovedEvent__f_x;
        break
      }
      case 1: {
        return this.Lsgl_InputProvider$Input$MouseMovedEvent__f_y;
        break
      }
      default: {
        throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
      }
    }
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    let acc = (-889275714);
    const hash = acc;
    const data = this.Lsgl_InputProvider$Input$MouseMovedEvent__f_x;
    acc = $m_sr_Statics$().mix__I__I__I(hash, data);
    const hash$1 = acc;
    const data$1 = this.Lsgl_InputProvider$Input$MouseMovedEvent__f_y;
    acc = $m_sr_Statics$().mix__I__I__I(hash$1, data$1);
    const hash$2 = acc;
    return $m_sr_Statics$().finalizeHash__I__I__I(hash$2, 2)
  };
  toString__T() {
    return $m_sr_ScalaRunTime$()._toString__s_Product__T(this)
  };
  equals__O__Z(x$1) {
    if ((this === x$1)) {
      return true
    } else if (((x$1 instanceof $c_Lsgl_InputProvider$Input$MouseMovedEvent) && ($as_Lsgl_InputProvider$Input$MouseMovedEvent(x$1).Lsgl_InputProvider$Input$MouseMovedEvent__f_$outer === this.Lsgl_InputProvider$Input$MouseMovedEvent__f_$outer))) {
      const MouseMovedEvent$1 = $as_Lsgl_InputProvider$Input$MouseMovedEvent(x$1);
      return ((this.Lsgl_InputProvider$Input$MouseMovedEvent__f_x === MouseMovedEvent$1.Lsgl_InputProvider$Input$MouseMovedEvent__f_x) && (this.Lsgl_InputProvider$Input$MouseMovedEvent__f_y === MouseMovedEvent$1.Lsgl_InputProvider$Input$MouseMovedEvent__f_y))
    } else {
      return false
    }
  };
}
function $as_Lsgl_InputProvider$Input$MouseMovedEvent(obj) {
  return (((obj instanceof $c_Lsgl_InputProvider$Input$MouseMovedEvent) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.InputProvider$Input$MouseMovedEvent"))
}
function $isArrayOf_Lsgl_InputProvider$Input$MouseMovedEvent(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_InputProvider$Input$MouseMovedEvent)))
}
function $asArrayOf_Lsgl_InputProvider$Input$MouseMovedEvent(obj, depth) {
  return (($isArrayOf_Lsgl_InputProvider$Input$MouseMovedEvent(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.InputProvider$Input$MouseMovedEvent;", depth))
}
const $d_Lsgl_InputProvider$Input$MouseMovedEvent = new $TypeData().initClass({
  Lsgl_InputProvider$Input$MouseMovedEvent: 0
}, false, "sgl.InputProvider$Input$MouseMovedEvent", {
  Lsgl_InputProvider$Input$MouseMovedEvent: 1,
  O: 1,
  Lsgl_InputProvider$Input$MouseInputEvent: 1,
  Lsgl_InputProvider$Input$InputEvent: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$MouseMovedEvent.prototype.$classData = $d_Lsgl_InputProvider$Input$MouseMovedEvent;
class $c_Lsgl_InputProvider$Input$MouseUpEvent extends $c_O {
  constructor(outer, x, y, mouseButton) {
    super();
    this.Lsgl_InputProvider$Input$MouseUpEvent__f_x = 0;
    this.Lsgl_InputProvider$Input$MouseUpEvent__f_y = 0;
    this.Lsgl_InputProvider$Input$MouseUpEvent__f_mouseButton = null;
    this.Lsgl_InputProvider$Input$MouseUpEvent__f_$outer = null;
    this.Lsgl_InputProvider$Input$MouseUpEvent__f_x = x;
    this.Lsgl_InputProvider$Input$MouseUpEvent__f_y = y;
    this.Lsgl_InputProvider$Input$MouseUpEvent__f_mouseButton = mouseButton;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$MouseUpEvent__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "MouseUpEvent"
  };
  productArity__I() {
    return 3
  };
  productElement__I__O(x$1) {
    switch (x$1) {
      case 0: {
        return this.Lsgl_InputProvider$Input$MouseUpEvent__f_x;
        break
      }
      case 1: {
        return this.Lsgl_InputProvider$Input$MouseUpEvent__f_y;
        break
      }
      case 2: {
        return this.Lsgl_InputProvider$Input$MouseUpEvent__f_mouseButton;
        break
      }
      default: {
        throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
      }
    }
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    let acc = (-889275714);
    const hash = acc;
    const data = this.Lsgl_InputProvider$Input$MouseUpEvent__f_x;
    acc = $m_sr_Statics$().mix__I__I__I(hash, data);
    const hash$1 = acc;
    const data$1 = this.Lsgl_InputProvider$Input$MouseUpEvent__f_y;
    acc = $m_sr_Statics$().mix__I__I__I(hash$1, data$1);
    const hash$2 = acc;
    const x = this.Lsgl_InputProvider$Input$MouseUpEvent__f_mouseButton;
    const data$2 = $m_sr_Statics$().anyHash__O__I(x);
    acc = $m_sr_Statics$().mix__I__I__I(hash$2, data$2);
    const hash$3 = acc;
    return $m_sr_Statics$().finalizeHash__I__I__I(hash$3, 3)
  };
  toString__T() {
    return $m_sr_ScalaRunTime$()._toString__s_Product__T(this)
  };
  equals__O__Z(x$1) {
    if ((this === x$1)) {
      return true
    } else if (((x$1 instanceof $c_Lsgl_InputProvider$Input$MouseUpEvent) && ($as_Lsgl_InputProvider$Input$MouseUpEvent(x$1).Lsgl_InputProvider$Input$MouseUpEvent__f_$outer === this.Lsgl_InputProvider$Input$MouseUpEvent__f_$outer))) {
      const MouseUpEvent$1 = $as_Lsgl_InputProvider$Input$MouseUpEvent(x$1);
      if (((this.Lsgl_InputProvider$Input$MouseUpEvent__f_x === MouseUpEvent$1.Lsgl_InputProvider$Input$MouseUpEvent__f_x) && (this.Lsgl_InputProvider$Input$MouseUpEvent__f_y === MouseUpEvent$1.Lsgl_InputProvider$Input$MouseUpEvent__f_y))) {
        const x = this.Lsgl_InputProvider$Input$MouseUpEvent__f_mouseButton;
        const x$2 = MouseUpEvent$1.Lsgl_InputProvider$Input$MouseUpEvent__f_mouseButton;
        return (x === x$2)
      } else {
        return false
      }
    } else {
      return false
    }
  };
}
function $as_Lsgl_InputProvider$Input$MouseUpEvent(obj) {
  return (((obj instanceof $c_Lsgl_InputProvider$Input$MouseUpEvent) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.InputProvider$Input$MouseUpEvent"))
}
function $isArrayOf_Lsgl_InputProvider$Input$MouseUpEvent(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_InputProvider$Input$MouseUpEvent)))
}
function $asArrayOf_Lsgl_InputProvider$Input$MouseUpEvent(obj, depth) {
  return (($isArrayOf_Lsgl_InputProvider$Input$MouseUpEvent(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.InputProvider$Input$MouseUpEvent;", depth))
}
const $d_Lsgl_InputProvider$Input$MouseUpEvent = new $TypeData().initClass({
  Lsgl_InputProvider$Input$MouseUpEvent: 0
}, false, "sgl.InputProvider$Input$MouseUpEvent", {
  Lsgl_InputProvider$Input$MouseUpEvent: 1,
  O: 1,
  Lsgl_InputProvider$Input$MouseInputEvent: 1,
  Lsgl_InputProvider$Input$InputEvent: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$MouseUpEvent.prototype.$classData = $d_Lsgl_InputProvider$Input$MouseUpEvent;
class $c_Lsgl_InputProvider$Input$TouchDownEvent extends $c_O {
  constructor(outer, x, y, pointer) {
    super();
    this.Lsgl_InputProvider$Input$TouchDownEvent__f_x = 0;
    this.Lsgl_InputProvider$Input$TouchDownEvent__f_y = 0;
    this.Lsgl_InputProvider$Input$TouchDownEvent__f_pointer = 0;
    this.Lsgl_InputProvider$Input$TouchDownEvent__f_$outer = null;
    this.Lsgl_InputProvider$Input$TouchDownEvent__f_x = x;
    this.Lsgl_InputProvider$Input$TouchDownEvent__f_y = y;
    this.Lsgl_InputProvider$Input$TouchDownEvent__f_pointer = pointer;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$TouchDownEvent__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "TouchDownEvent"
  };
  productArity__I() {
    return 3
  };
  productElement__I__O(x$1) {
    switch (x$1) {
      case 0: {
        return this.Lsgl_InputProvider$Input$TouchDownEvent__f_x;
        break
      }
      case 1: {
        return this.Lsgl_InputProvider$Input$TouchDownEvent__f_y;
        break
      }
      case 2: {
        return this.Lsgl_InputProvider$Input$TouchDownEvent__f_pointer;
        break
      }
      default: {
        throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
      }
    }
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    let acc = (-889275714);
    const hash = acc;
    const data = this.Lsgl_InputProvider$Input$TouchDownEvent__f_x;
    acc = $m_sr_Statics$().mix__I__I__I(hash, data);
    const hash$1 = acc;
    const data$1 = this.Lsgl_InputProvider$Input$TouchDownEvent__f_y;
    acc = $m_sr_Statics$().mix__I__I__I(hash$1, data$1);
    const hash$2 = acc;
    const data$2 = this.Lsgl_InputProvider$Input$TouchDownEvent__f_pointer;
    acc = $m_sr_Statics$().mix__I__I__I(hash$2, data$2);
    const hash$3 = acc;
    return $m_sr_Statics$().finalizeHash__I__I__I(hash$3, 3)
  };
  toString__T() {
    return $m_sr_ScalaRunTime$()._toString__s_Product__T(this)
  };
  equals__O__Z(x$1) {
    if ((this === x$1)) {
      return true
    } else if (((x$1 instanceof $c_Lsgl_InputProvider$Input$TouchDownEvent) && ($as_Lsgl_InputProvider$Input$TouchDownEvent(x$1).Lsgl_InputProvider$Input$TouchDownEvent__f_$outer === this.Lsgl_InputProvider$Input$TouchDownEvent__f_$outer))) {
      const TouchDownEvent$1 = $as_Lsgl_InputProvider$Input$TouchDownEvent(x$1);
      return (((this.Lsgl_InputProvider$Input$TouchDownEvent__f_x === TouchDownEvent$1.Lsgl_InputProvider$Input$TouchDownEvent__f_x) && (this.Lsgl_InputProvider$Input$TouchDownEvent__f_y === TouchDownEvent$1.Lsgl_InputProvider$Input$TouchDownEvent__f_y)) && (this.Lsgl_InputProvider$Input$TouchDownEvent__f_pointer === TouchDownEvent$1.Lsgl_InputProvider$Input$TouchDownEvent__f_pointer))
    } else {
      return false
    }
  };
}
function $as_Lsgl_InputProvider$Input$TouchDownEvent(obj) {
  return (((obj instanceof $c_Lsgl_InputProvider$Input$TouchDownEvent) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.InputProvider$Input$TouchDownEvent"))
}
function $isArrayOf_Lsgl_InputProvider$Input$TouchDownEvent(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_InputProvider$Input$TouchDownEvent)))
}
function $asArrayOf_Lsgl_InputProvider$Input$TouchDownEvent(obj, depth) {
  return (($isArrayOf_Lsgl_InputProvider$Input$TouchDownEvent(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.InputProvider$Input$TouchDownEvent;", depth))
}
const $d_Lsgl_InputProvider$Input$TouchDownEvent = new $TypeData().initClass({
  Lsgl_InputProvider$Input$TouchDownEvent: 0
}, false, "sgl.InputProvider$Input$TouchDownEvent", {
  Lsgl_InputProvider$Input$TouchDownEvent: 1,
  O: 1,
  Lsgl_InputProvider$Input$TouchInputEvent: 1,
  Lsgl_InputProvider$Input$InputEvent: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$TouchDownEvent.prototype.$classData = $d_Lsgl_InputProvider$Input$TouchDownEvent;
class $c_Lsgl_InputProvider$Input$TouchMovedEvent extends $c_O {
  constructor(outer, x, y, pointer) {
    super();
    this.Lsgl_InputProvider$Input$TouchMovedEvent__f_x = 0;
    this.Lsgl_InputProvider$Input$TouchMovedEvent__f_y = 0;
    this.Lsgl_InputProvider$Input$TouchMovedEvent__f_pointer = 0;
    this.Lsgl_InputProvider$Input$TouchMovedEvent__f_$outer = null;
    this.Lsgl_InputProvider$Input$TouchMovedEvent__f_x = x;
    this.Lsgl_InputProvider$Input$TouchMovedEvent__f_y = y;
    this.Lsgl_InputProvider$Input$TouchMovedEvent__f_pointer = pointer;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$TouchMovedEvent__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "TouchMovedEvent"
  };
  productArity__I() {
    return 3
  };
  productElement__I__O(x$1) {
    switch (x$1) {
      case 0: {
        return this.Lsgl_InputProvider$Input$TouchMovedEvent__f_x;
        break
      }
      case 1: {
        return this.Lsgl_InputProvider$Input$TouchMovedEvent__f_y;
        break
      }
      case 2: {
        return this.Lsgl_InputProvider$Input$TouchMovedEvent__f_pointer;
        break
      }
      default: {
        throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
      }
    }
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    let acc = (-889275714);
    const hash = acc;
    const data = this.Lsgl_InputProvider$Input$TouchMovedEvent__f_x;
    acc = $m_sr_Statics$().mix__I__I__I(hash, data);
    const hash$1 = acc;
    const data$1 = this.Lsgl_InputProvider$Input$TouchMovedEvent__f_y;
    acc = $m_sr_Statics$().mix__I__I__I(hash$1, data$1);
    const hash$2 = acc;
    const data$2 = this.Lsgl_InputProvider$Input$TouchMovedEvent__f_pointer;
    acc = $m_sr_Statics$().mix__I__I__I(hash$2, data$2);
    const hash$3 = acc;
    return $m_sr_Statics$().finalizeHash__I__I__I(hash$3, 3)
  };
  toString__T() {
    return $m_sr_ScalaRunTime$()._toString__s_Product__T(this)
  };
  equals__O__Z(x$1) {
    if ((this === x$1)) {
      return true
    } else if (((x$1 instanceof $c_Lsgl_InputProvider$Input$TouchMovedEvent) && ($as_Lsgl_InputProvider$Input$TouchMovedEvent(x$1).Lsgl_InputProvider$Input$TouchMovedEvent__f_$outer === this.Lsgl_InputProvider$Input$TouchMovedEvent__f_$outer))) {
      const TouchMovedEvent$1 = $as_Lsgl_InputProvider$Input$TouchMovedEvent(x$1);
      return (((this.Lsgl_InputProvider$Input$TouchMovedEvent__f_x === TouchMovedEvent$1.Lsgl_InputProvider$Input$TouchMovedEvent__f_x) && (this.Lsgl_InputProvider$Input$TouchMovedEvent__f_y === TouchMovedEvent$1.Lsgl_InputProvider$Input$TouchMovedEvent__f_y)) && (this.Lsgl_InputProvider$Input$TouchMovedEvent__f_pointer === TouchMovedEvent$1.Lsgl_InputProvider$Input$TouchMovedEvent__f_pointer))
    } else {
      return false
    }
  };
}
function $as_Lsgl_InputProvider$Input$TouchMovedEvent(obj) {
  return (((obj instanceof $c_Lsgl_InputProvider$Input$TouchMovedEvent) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.InputProvider$Input$TouchMovedEvent"))
}
function $isArrayOf_Lsgl_InputProvider$Input$TouchMovedEvent(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_InputProvider$Input$TouchMovedEvent)))
}
function $asArrayOf_Lsgl_InputProvider$Input$TouchMovedEvent(obj, depth) {
  return (($isArrayOf_Lsgl_InputProvider$Input$TouchMovedEvent(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.InputProvider$Input$TouchMovedEvent;", depth))
}
const $d_Lsgl_InputProvider$Input$TouchMovedEvent = new $TypeData().initClass({
  Lsgl_InputProvider$Input$TouchMovedEvent: 0
}, false, "sgl.InputProvider$Input$TouchMovedEvent", {
  Lsgl_InputProvider$Input$TouchMovedEvent: 1,
  O: 1,
  Lsgl_InputProvider$Input$TouchInputEvent: 1,
  Lsgl_InputProvider$Input$InputEvent: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$TouchMovedEvent.prototype.$classData = $d_Lsgl_InputProvider$Input$TouchMovedEvent;
class $c_Lsgl_InputProvider$Input$TouchUpEvent extends $c_O {
  constructor(outer, x, y, pointer) {
    super();
    this.Lsgl_InputProvider$Input$TouchUpEvent__f_x = 0;
    this.Lsgl_InputProvider$Input$TouchUpEvent__f_y = 0;
    this.Lsgl_InputProvider$Input$TouchUpEvent__f_pointer = 0;
    this.Lsgl_InputProvider$Input$TouchUpEvent__f_$outer = null;
    this.Lsgl_InputProvider$Input$TouchUpEvent__f_x = x;
    this.Lsgl_InputProvider$Input$TouchUpEvent__f_y = y;
    this.Lsgl_InputProvider$Input$TouchUpEvent__f_pointer = pointer;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_InputProvider$Input$TouchUpEvent__f_$outer = outer
    }
  };
  productPrefix__T() {
    return "TouchUpEvent"
  };
  productArity__I() {
    return 3
  };
  productElement__I__O(x$1) {
    switch (x$1) {
      case 0: {
        return this.Lsgl_InputProvider$Input$TouchUpEvent__f_x;
        break
      }
      case 1: {
        return this.Lsgl_InputProvider$Input$TouchUpEvent__f_y;
        break
      }
      case 2: {
        return this.Lsgl_InputProvider$Input$TouchUpEvent__f_pointer;
        break
      }
      default: {
        throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
      }
    }
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    let acc = (-889275714);
    const hash = acc;
    const data = this.Lsgl_InputProvider$Input$TouchUpEvent__f_x;
    acc = $m_sr_Statics$().mix__I__I__I(hash, data);
    const hash$1 = acc;
    const data$1 = this.Lsgl_InputProvider$Input$TouchUpEvent__f_y;
    acc = $m_sr_Statics$().mix__I__I__I(hash$1, data$1);
    const hash$2 = acc;
    const data$2 = this.Lsgl_InputProvider$Input$TouchUpEvent__f_pointer;
    acc = $m_sr_Statics$().mix__I__I__I(hash$2, data$2);
    const hash$3 = acc;
    return $m_sr_Statics$().finalizeHash__I__I__I(hash$3, 3)
  };
  toString__T() {
    return $m_sr_ScalaRunTime$()._toString__s_Product__T(this)
  };
  equals__O__Z(x$1) {
    if ((this === x$1)) {
      return true
    } else if (((x$1 instanceof $c_Lsgl_InputProvider$Input$TouchUpEvent) && ($as_Lsgl_InputProvider$Input$TouchUpEvent(x$1).Lsgl_InputProvider$Input$TouchUpEvent__f_$outer === this.Lsgl_InputProvider$Input$TouchUpEvent__f_$outer))) {
      const TouchUpEvent$1 = $as_Lsgl_InputProvider$Input$TouchUpEvent(x$1);
      return (((this.Lsgl_InputProvider$Input$TouchUpEvent__f_x === TouchUpEvent$1.Lsgl_InputProvider$Input$TouchUpEvent__f_x) && (this.Lsgl_InputProvider$Input$TouchUpEvent__f_y === TouchUpEvent$1.Lsgl_InputProvider$Input$TouchUpEvent__f_y)) && (this.Lsgl_InputProvider$Input$TouchUpEvent__f_pointer === TouchUpEvent$1.Lsgl_InputProvider$Input$TouchUpEvent__f_pointer))
    } else {
      return false
    }
  };
}
function $as_Lsgl_InputProvider$Input$TouchUpEvent(obj) {
  return (((obj instanceof $c_Lsgl_InputProvider$Input$TouchUpEvent) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.InputProvider$Input$TouchUpEvent"))
}
function $isArrayOf_Lsgl_InputProvider$Input$TouchUpEvent(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_InputProvider$Input$TouchUpEvent)))
}
function $asArrayOf_Lsgl_InputProvider$Input$TouchUpEvent(obj, depth) {
  return (($isArrayOf_Lsgl_InputProvider$Input$TouchUpEvent(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.InputProvider$Input$TouchUpEvent;", depth))
}
const $d_Lsgl_InputProvider$Input$TouchUpEvent = new $TypeData().initClass({
  Lsgl_InputProvider$Input$TouchUpEvent: 0
}, false, "sgl.InputProvider$Input$TouchUpEvent", {
  Lsgl_InputProvider$Input$TouchUpEvent: 1,
  O: 1,
  Lsgl_InputProvider$Input$TouchInputEvent: 1,
  Lsgl_InputProvider$Input$InputEvent: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_InputProvider$Input$TouchUpEvent.prototype.$classData = $d_Lsgl_InputProvider$Input$TouchUpEvent;
class $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas extends $c_O {
  constructor(outer, canvas) {
    super();
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_canvas = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_width = 0.0;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_height = 0.0;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_context = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_$outer = null;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_canvas = canvas;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_$outer = outer
    };
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_width = $fround($uI(canvas.width));
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_height = $fround($uI(canvas.height));
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_context = canvas.getContext("2d")
  };
  drawRect__F__F__F__F__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Paint__V(x, y, width, height, paint) {
    paint.prepareContext__Lorg_scalajs_dom_raw_CanvasRenderingContext2D__V(this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_context);
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_context.fillRect(x, y, width, height)
  };
  drawColor__T__V(color) {
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_context.fillStyle = color;
    this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_context.fillRect(0.0, 0.0, this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_width, this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_height)
  };
  productPrefix__T() {
    return "Html5Canvas"
  };
  productArity__I() {
    return 1
  };
  productElement__I__O(x$1) {
    if ((x$1 === 0)) {
      return this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_canvas
    } else {
      throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
    }
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    const this$2 = $m_s_util_hashing_MurmurHash3$();
    return this$2.productHash__s_Product__I__I(this, (-889275714))
  };
  toString__T() {
    return $m_sr_ScalaRunTime$()._toString__s_Product__T(this)
  };
  equals__O__Z(x$1) {
    if ((this === x$1)) {
      return true
    } else if (((x$1 instanceof $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas) && ($as_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas(x$1).Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_$outer === this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_$outer))) {
      const Html5Canvas$1 = $as_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas(x$1);
      const x = this.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_canvas;
      const y = Html5Canvas$1.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__f_canvas;
      return $m_sr_BoxesRunTime$().equals__O__O__Z(x, y)
    } else {
      return false
    }
  };
}
function $as_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas(obj) {
  return (((obj instanceof $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.html5.Html5GraphicsProvider$Html5Graphics$Html5Canvas"))
}
function $isArrayOf_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas)))
}
function $asArrayOf_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas(obj, depth) {
  return (($isArrayOf_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.html5.Html5GraphicsProvider$Html5Graphics$Html5Canvas;", depth))
}
const $d_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas = new $TypeData().initClass({
  Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas: 0
}, false, "sgl.html5.Html5GraphicsProvider$Html5Graphics$Html5Canvas", {
  Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas: 1,
  O: 1,
  Lsgl_GraphicsProvider$Graphics$AbstractCanvas: 1,
  Lsgl_GraphicsHelpersComponent$GraphicsExtension$RichCanvas: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas.prototype.$classData = $d_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas;
class $c_Lsgl_html5_util_Html5VerboseConsoleLoggingProvider$VerboseConsoleLogger$ extends $c_Lsgl_html5_util_Html5ConsoleLoggingProvider$ConsoleLogger {
  constructor(outer) {
    super();
    this.Lsgl_html5_util_Html5VerboseConsoleLoggingProvider$VerboseConsoleLogger$__f_logLevel = null;
    $ct_Lsgl_util_LoggingProvider$Logger__Lsgl_util_LoggingProvider__(this, outer);
    this.Lsgl_html5_util_Html5VerboseConsoleLoggingProvider$VerboseConsoleLogger$__f_logLevel = outer.Logger__Lsgl_util_LoggingProvider$Logger$().Debug__Lsgl_util_LoggingProvider$Logger$Debug$()
  };
  productPrefix__T() {
    return "VerboseConsoleLogger"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    return (-1544153211)
  };
  toString__T() {
    return "VerboseConsoleLogger"
  };
}
const $d_Lsgl_html5_util_Html5VerboseConsoleLoggingProvider$VerboseConsoleLogger$ = new $TypeData().initClass({
  Lsgl_html5_util_Html5VerboseConsoleLoggingProvider$VerboseConsoleLogger$: 0
}, false, "sgl.html5.util.Html5VerboseConsoleLoggingProvider$VerboseConsoleLogger$", {
  Lsgl_html5_util_Html5VerboseConsoleLoggingProvider$VerboseConsoleLogger$: 1,
  Lsgl_html5_util_Html5ConsoleLoggingProvider$ConsoleLogger: 1,
  Lsgl_util_LoggingProvider$Logger: 1,
  O: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_Lsgl_html5_util_Html5VerboseConsoleLoggingProvider$VerboseConsoleLogger$.prototype.$classData = $d_Lsgl_html5_util_Html5VerboseConsoleLoggingProvider$VerboseConsoleLogger$;
class $c_T2$mcII$sp extends $c_T2 {
  constructor(_1$mcI$sp, _2$mcI$sp) {
    super();
    this.T2$mcII$sp__f__1$mcI$sp = 0;
    this.T2$mcII$sp__f__2$mcI$sp = 0;
    this.T2$mcII$sp__f__1$mcI$sp = _1$mcI$sp;
    this.T2$mcII$sp__f__2$mcI$sp = _2$mcI$sp;
    $ct_T2__O__O__(this, null, null)
  };
  _1$mcI$sp__I() {
    return this.T2$mcII$sp__f__1$mcI$sp
  };
  _2$mcI$sp__I() {
    return this.T2$mcII$sp__f__2$mcI$sp
  };
  _2__O() {
    return this.T2$mcII$sp__f__2$mcI$sp
  };
  _1__O() {
    return this.T2$mcII$sp__f__1$mcI$sp
  };
}
const $d_T2$mcII$sp = new $TypeData().initClass({
  T2$mcII$sp: 0
}, false, "scala.Tuple2$mcII$sp", {
  T2$mcII$sp: 1,
  T2: 1,
  O: 1,
  s_Product2: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1,
  s_Product2$mcII$sp: 1
});
$c_T2$mcII$sp.prototype.$classData = $d_T2$mcII$sp;
class $c_sc_IndexedSeq$ extends $c_scg_IndexedSeqFactory {
  constructor() {
    super();
    this.sc_IndexedSeq$__f_ReusableCBF = null;
    $ct_scg_GenTraversableFactory__(this);
    $n_sc_IndexedSeq$ = this;
    this.sc_IndexedSeq$__f_ReusableCBF = new $c_sc_IndexedSeq$$anon$1()
  };
  newBuilder__scm_Builder() {
    $m_sci_IndexedSeq$();
    $m_sci_Vector$();
    return new $c_sci_VectorBuilder()
  };
}
const $d_sc_IndexedSeq$ = new $TypeData().initClass({
  sc_IndexedSeq$: 0
}, false, "scala.collection.IndexedSeq$", {
  sc_IndexedSeq$: 1,
  scg_IndexedSeqFactory: 1,
  scg_SeqFactory: 1,
  scg_GenSeqFactory: 1,
  scg_GenTraversableFactory: 1,
  scg_GenericCompanion: 1,
  O: 1,
  scg_TraversableFactory: 1,
  scg_GenericSeqCompanion: 1
});
$c_sc_IndexedSeq$.prototype.$classData = $d_sc_IndexedSeq$;
let $n_sc_IndexedSeq$ = (void 0);
function $m_sc_IndexedSeq$() {
  if ((!$n_sc_IndexedSeq$)) {
    $n_sc_IndexedSeq$ = new $c_sc_IndexedSeq$()
  };
  return $n_sc_IndexedSeq$
}
class $c_sc_IndexedSeqLike$Elements extends $c_sc_AbstractIterator {
  constructor(outer, start, end) {
    super();
    this.sc_IndexedSeqLike$Elements__f_end = 0;
    this.sc_IndexedSeqLike$Elements__f_index = 0;
    this.sc_IndexedSeqLike$Elements__f_$outer = null;
    this.sc_IndexedSeqLike$Elements__f_end = end;
    if ((outer === null)) {
      throw $m_sjsr_package$().unwrapJavaScriptException__jl_Throwable__O(null)
    } else {
      this.sc_IndexedSeqLike$Elements__f_$outer = outer
    };
    this.sc_IndexedSeqLike$Elements__f_index = start
  };
  hasNext__Z() {
    return (this.sc_IndexedSeqLike$Elements__f_index < this.sc_IndexedSeqLike$Elements__f_end)
  };
  next__O() {
    if ((this.sc_IndexedSeqLike$Elements__f_index >= this.sc_IndexedSeqLike$Elements__f_end)) {
      $m_sc_Iterator$().sc_Iterator$__f_empty.next__O()
    };
    const x = this.sc_IndexedSeqLike$Elements__f_$outer.apply__I__O(this.sc_IndexedSeqLike$Elements__f_index);
    this.sc_IndexedSeqLike$Elements__f_index = ((1 + this.sc_IndexedSeqLike$Elements__f_index) | 0);
    return x
  };
}
const $d_sc_IndexedSeqLike$Elements = new $TypeData().initClass({
  sc_IndexedSeqLike$Elements: 0
}, false, "scala.collection.IndexedSeqLike$Elements", {
  sc_IndexedSeqLike$Elements: 1,
  sc_AbstractIterator: 1,
  O: 1,
  sc_Iterator: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1,
  sc_BufferedIterator: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_sc_IndexedSeqLike$Elements.prototype.$classData = $d_sc_IndexedSeqLike$Elements;
class $c_sci_IndexedSeq$ extends $c_scg_IndexedSeqFactory {
  constructor() {
    super();
    $ct_scg_GenTraversableFactory__(this)
  };
  newBuilder__scm_Builder() {
    $m_sci_Vector$();
    return new $c_sci_VectorBuilder()
  };
}
const $d_sci_IndexedSeq$ = new $TypeData().initClass({
  sci_IndexedSeq$: 0
}, false, "scala.collection.immutable.IndexedSeq$", {
  sci_IndexedSeq$: 1,
  scg_IndexedSeqFactory: 1,
  scg_SeqFactory: 1,
  scg_GenSeqFactory: 1,
  scg_GenTraversableFactory: 1,
  scg_GenericCompanion: 1,
  O: 1,
  scg_TraversableFactory: 1,
  scg_GenericSeqCompanion: 1
});
$c_sci_IndexedSeq$.prototype.$classData = $d_sci_IndexedSeq$;
let $n_sci_IndexedSeq$ = (void 0);
function $m_sci_IndexedSeq$() {
  if ((!$n_sci_IndexedSeq$)) {
    $n_sci_IndexedSeq$ = new $c_sci_IndexedSeq$()
  };
  return $n_sci_IndexedSeq$
}
class $c_sjs_js_JavaScriptException extends $c_jl_RuntimeException {
  constructor(exception) {
    super();
    this.sjs_js_JavaScriptException__f_exception = null;
    this.sjs_js_JavaScriptException__f_exception = exception;
    $ct_jl_Throwable__T__jl_Throwable__Z__Z__(this, null, null, true, true)
  };
  getMessage__T() {
    return $dp_toString__T(this.sjs_js_JavaScriptException__f_exception)
  };
  fillInStackTrace__jl_Throwable() {
    this.setStackTraceStateInternal__O__(this.sjs_js_JavaScriptException__f_exception);
    return this
  };
  productPrefix__T() {
    return "JavaScriptException"
  };
  productArity__I() {
    return 1
  };
  productElement__I__O(x$1) {
    if ((x$1 === 0)) {
      return this.sjs_js_JavaScriptException__f_exception
    } else {
      throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
    }
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  hashCode__I() {
    const this$2 = $m_s_util_hashing_MurmurHash3$();
    return this$2.productHash__s_Product__I__I(this, (-889275714))
  };
  equals__O__Z(x$1) {
    if ((this === x$1)) {
      return true
    } else if ((x$1 instanceof $c_sjs_js_JavaScriptException)) {
      const JavaScriptException$1 = $as_sjs_js_JavaScriptException(x$1);
      const x = this.sjs_js_JavaScriptException__f_exception;
      const y = JavaScriptException$1.sjs_js_JavaScriptException__f_exception;
      return $m_sr_BoxesRunTime$().equals__O__O__Z(x, y)
    } else {
      return false
    }
  };
  setStackTraceStateInternal__O__(e) {
    this.jl_Throwable__f_stackTraceStateInternal = e
  };
}
function $as_sjs_js_JavaScriptException(obj) {
  return (((obj instanceof $c_sjs_js_JavaScriptException) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.scalajs.js.JavaScriptException"))
}
function $isArrayOf_sjs_js_JavaScriptException(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sjs_js_JavaScriptException)))
}
function $asArrayOf_sjs_js_JavaScriptException(obj, depth) {
  return (($isArrayOf_sjs_js_JavaScriptException(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.scalajs.js.JavaScriptException;", depth))
}
const $d_sjs_js_JavaScriptException = new $TypeData().initClass({
  sjs_js_JavaScriptException: 0
}, false, "scala.scalajs.js.JavaScriptException", {
  sjs_js_JavaScriptException: 1,
  jl_RuntimeException: 1,
  jl_Exception: 1,
  jl_Throwable: 1,
  O: 1,
  Ljava_io_Serializable: 1,
  s_Product: 1,
  s_Equals: 1,
  s_Serializable: 1
});
$c_sjs_js_JavaScriptException.prototype.$classData = $d_sjs_js_JavaScriptException;
class $c_sci_List$ extends $c_scg_SeqFactory {
  constructor() {
    super();
    this.sci_List$__f_partialNotApplied = null;
    $ct_scg_GenTraversableFactory__(this);
    $n_sci_List$ = this;
    this.sci_List$__f_partialNotApplied = new $c_sci_List$$anon$1()
  };
  newBuilder__scm_Builder() {
    return new $c_scm_ListBuffer()
  };
  empty__sc_GenTraversable() {
    return $m_sci_Nil$()
  };
}
const $d_sci_List$ = new $TypeData().initClass({
  sci_List$: 0
}, false, "scala.collection.immutable.List$", {
  sci_List$: 1,
  scg_SeqFactory: 1,
  scg_GenSeqFactory: 1,
  scg_GenTraversableFactory: 1,
  scg_GenericCompanion: 1,
  O: 1,
  scg_TraversableFactory: 1,
  scg_GenericSeqCompanion: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_sci_List$.prototype.$classData = $d_sci_List$;
let $n_sci_List$ = (void 0);
function $m_sci_List$() {
  if ((!$n_sci_List$)) {
    $n_sci_List$ = new $c_sci_List$()
  };
  return $n_sci_List$
}
class $c_sci_Stream$ extends $c_scg_SeqFactory {
  constructor() {
    super();
    $ct_scg_GenTraversableFactory__(this)
  };
  newBuilder__scm_Builder() {
    return new $c_sci_Stream$StreamBuilder()
  };
  empty__sc_GenTraversable() {
    return $m_sci_Stream$Empty$()
  };
}
const $d_sci_Stream$ = new $TypeData().initClass({
  sci_Stream$: 0
}, false, "scala.collection.immutable.Stream$", {
  sci_Stream$: 1,
  scg_SeqFactory: 1,
  scg_GenSeqFactory: 1,
  scg_GenTraversableFactory: 1,
  scg_GenericCompanion: 1,
  O: 1,
  scg_TraversableFactory: 1,
  scg_GenericSeqCompanion: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_sci_Stream$.prototype.$classData = $d_sci_Stream$;
let $n_sci_Stream$ = (void 0);
function $m_sci_Stream$() {
  if ((!$n_sci_Stream$)) {
    $n_sci_Stream$ = new $c_sci_Stream$()
  };
  return $n_sci_Stream$
}
class $c_scm_ArrayBuffer$ extends $c_scg_SeqFactory {
  constructor() {
    super();
    $ct_scg_GenTraversableFactory__(this)
  };
  newBuilder__scm_Builder() {
    return $ct_scm_ArrayBuffer__(new $c_scm_ArrayBuffer())
  };
}
const $d_scm_ArrayBuffer$ = new $TypeData().initClass({
  scm_ArrayBuffer$: 0
}, false, "scala.collection.mutable.ArrayBuffer$", {
  scm_ArrayBuffer$: 1,
  scg_SeqFactory: 1,
  scg_GenSeqFactory: 1,
  scg_GenTraversableFactory: 1,
  scg_GenericCompanion: 1,
  O: 1,
  scg_TraversableFactory: 1,
  scg_GenericSeqCompanion: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_scm_ArrayBuffer$.prototype.$classData = $d_scm_ArrayBuffer$;
let $n_scm_ArrayBuffer$ = (void 0);
function $m_scm_ArrayBuffer$() {
  if ((!$n_scm_ArrayBuffer$)) {
    $n_scm_ArrayBuffer$ = new $c_scm_ArrayBuffer$()
  };
  return $n_scm_ArrayBuffer$
}
class $c_scm_LinkedList$ extends $c_scg_SeqFactory {
  constructor() {
    super();
    $ct_scg_GenTraversableFactory__(this)
  };
  newBuilder__scm_Builder() {
    const this$2 = $ct_scm_MutableList__(new $c_scm_MutableList());
    const f = new $c_sjsr_AnonFunction1(((this$1) => ((l$2) => {
      const l = $as_scm_MutableList(l$2);
      return l.scm_MutableList__f_first0
    }))(this));
    return new $c_scm_Builder$$anon$1(this$2, f)
  };
  empty__sc_GenTraversable() {
    return $ct_scm_LinkedList__(new $c_scm_LinkedList())
  };
}
const $d_scm_LinkedList$ = new $TypeData().initClass({
  scm_LinkedList$: 0
}, false, "scala.collection.mutable.LinkedList$", {
  scm_LinkedList$: 1,
  scg_SeqFactory: 1,
  scg_GenSeqFactory: 1,
  scg_GenTraversableFactory: 1,
  scg_GenericCompanion: 1,
  O: 1,
  scg_TraversableFactory: 1,
  scg_GenericSeqCompanion: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_scm_LinkedList$.prototype.$classData = $d_scm_LinkedList$;
let $n_scm_LinkedList$ = (void 0);
function $m_scm_LinkedList$() {
  if ((!$n_scm_LinkedList$)) {
    $n_scm_LinkedList$ = new $c_scm_LinkedList$()
  };
  return $n_scm_LinkedList$
}
class $c_scm_ListBuffer$ extends $c_scg_SeqFactory {
  constructor() {
    super();
    $ct_scg_GenTraversableFactory__(this)
  };
  newBuilder__scm_Builder() {
    return new $c_scm_GrowingBuilder(new $c_scm_ListBuffer())
  };
}
const $d_scm_ListBuffer$ = new $TypeData().initClass({
  scm_ListBuffer$: 0
}, false, "scala.collection.mutable.ListBuffer$", {
  scm_ListBuffer$: 1,
  scg_SeqFactory: 1,
  scg_GenSeqFactory: 1,
  scg_GenTraversableFactory: 1,
  scg_GenericCompanion: 1,
  O: 1,
  scg_TraversableFactory: 1,
  scg_GenericSeqCompanion: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_scm_ListBuffer$.prototype.$classData = $d_scm_ListBuffer$;
let $n_scm_ListBuffer$ = (void 0);
function $m_scm_ListBuffer$() {
  if ((!$n_scm_ListBuffer$)) {
    $n_scm_ListBuffer$ = new $c_scm_ListBuffer$()
  };
  return $n_scm_ListBuffer$
}
class $c_scm_MutableList$ extends $c_scg_SeqFactory {
  constructor() {
    super();
    $ct_scg_GenTraversableFactory__(this)
  };
  newBuilder__scm_Builder() {
    return $ct_scm_MutableList__(new $c_scm_MutableList())
  };
}
const $d_scm_MutableList$ = new $TypeData().initClass({
  scm_MutableList$: 0
}, false, "scala.collection.mutable.MutableList$", {
  scm_MutableList$: 1,
  scg_SeqFactory: 1,
  scg_GenSeqFactory: 1,
  scg_GenTraversableFactory: 1,
  scg_GenericCompanion: 1,
  O: 1,
  scg_TraversableFactory: 1,
  scg_GenericSeqCompanion: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_scm_MutableList$.prototype.$classData = $d_scm_MutableList$;
let $n_scm_MutableList$ = (void 0);
function $m_scm_MutableList$() {
  if ((!$n_scm_MutableList$)) {
    $n_scm_MutableList$ = new $c_scm_MutableList$()
  };
  return $n_scm_MutableList$
}
class $c_scm_Queue$ extends $c_scg_SeqFactory {
  constructor() {
    super();
    $ct_scg_GenTraversableFactory__(this)
  };
  newBuilder__scm_Builder() {
    const this$2 = $ct_scm_MutableList__(new $c_scm_MutableList());
    const f = new $c_sjsr_AnonFunction1(((this$1) => ((x$1$2) => {
      const x$1 = $as_scm_MutableList(x$1$2);
      return $ct_scm_Queue__scm_LinkedList__scm_LinkedList__I__(new $c_scm_Queue(), x$1.scm_MutableList__f_first0, x$1.scm_MutableList__f_last0, x$1.scm_MutableList__f_len)
    }))(this));
    return new $c_scm_Builder$$anon$1(this$2, f)
  };
}
const $d_scm_Queue$ = new $TypeData().initClass({
  scm_Queue$: 0
}, false, "scala.collection.mutable.Queue$", {
  scm_Queue$: 1,
  scg_SeqFactory: 1,
  scg_GenSeqFactory: 1,
  scg_GenTraversableFactory: 1,
  scg_GenericCompanion: 1,
  O: 1,
  scg_TraversableFactory: 1,
  scg_GenericSeqCompanion: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_scm_Queue$.prototype.$classData = $d_scm_Queue$;
let $n_scm_Queue$ = (void 0);
function $m_scm_Queue$() {
  if ((!$n_scm_Queue$)) {
    $n_scm_Queue$ = new $c_scm_Queue$()
  };
  return $n_scm_Queue$
}
const $f_sc_IterableLike__take__I__O = (function($thiz, n) {
  const b = $thiz.newBuilder__scm_Builder();
  if ((n <= 0)) {
    return b.result__O()
  } else {
    b.sizeHintBounded__I__sc_TraversableLike__V(n, $thiz);
    let i = 0;
    const it = $thiz.iterator__sc_Iterator();
    while (((i < n) && it.hasNext__Z())) {
      b.$plus$eq__O__scm_Builder(it.next__O());
      i = ((1 + i) | 0)
    };
    return b.result__O()
  }
});
const $f_sc_IterableLike__copyToArray__O__I__I__V = (function($thiz, xs, start, len) {
  let i = start;
  const x = ((start + len) | 0);
  const that = $m_sr_ScalaRunTime$().array_length__O__I(xs);
  const end = ((x < that) ? x : that);
  const it = $thiz.iterator__sc_Iterator();
  while (((i < end) && it.hasNext__Z())) {
    $m_sr_ScalaRunTime$().array_update__O__I__O__V(xs, i, it.next__O());
    i = ((1 + i) | 0)
  }
});
const $f_sc_IterableLike__sameElements__sc_GenIterable__Z = (function($thiz, that) {
  if ((that instanceof $c_sci_Vector)) {
    const x2 = $as_sci_Vector(that);
    if (($thiz instanceof $c_sci_Vector)) {
      const thisVector = $as_sci_Vector($thiz);
      if ((thisVector === x2)) {
        return true
      } else {
        let equal = (thisVector.length__I() === x2.length__I());
        if (equal) {
          const length = x2.length__I();
          let index = 0;
          while (((index < length) && equal)) {
            equal = $m_sr_BoxesRunTime$().equals__O__O__Z(thisVector.apply__I__O(index), x2.apply__I__O(index));
            index = ((1 + index) | 0)
          }
        };
        return equal
      }
    }
  };
  const these = $thiz.iterator__sc_Iterator();
  const those = that.iterator__sc_Iterator();
  while ((these.hasNext__Z() && those.hasNext__Z())) {
    if ((!$m_sr_BoxesRunTime$().equals__O__O__Z(these.next__O(), those.next__O()))) {
      return false
    }
  };
  return ((!these.hasNext__Z()) && (!those.hasNext__Z()))
});
class $c_sci_Vector$ extends $c_scg_IndexedSeqFactory {
  constructor() {
    super();
    this.sci_Vector$__f_NIL = null;
    $ct_scg_GenTraversableFactory__(this);
    $n_sci_Vector$ = this;
    this.sci_Vector$__f_NIL = new $c_sci_Vector(0, 0, 0)
  };
  newBuilder__scm_Builder() {
    return new $c_sci_VectorBuilder()
  };
  empty__sc_GenTraversable() {
    return this.sci_Vector$__f_NIL
  };
}
const $d_sci_Vector$ = new $TypeData().initClass({
  sci_Vector$: 0
}, false, "scala.collection.immutable.Vector$", {
  sci_Vector$: 1,
  scg_IndexedSeqFactory: 1,
  scg_SeqFactory: 1,
  scg_GenSeqFactory: 1,
  scg_GenTraversableFactory: 1,
  scg_GenericCompanion: 1,
  O: 1,
  scg_TraversableFactory: 1,
  scg_GenericSeqCompanion: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_sci_Vector$.prototype.$classData = $d_sci_Vector$;
let $n_sci_Vector$ = (void 0);
function $m_sci_Vector$() {
  if ((!$n_sci_Vector$)) {
    $n_sci_Vector$ = new $c_sci_Vector$()
  };
  return $n_sci_Vector$
}
class $c_sc_AbstractTraversable extends $c_O {
  newBuilder__scm_Builder() {
    return this.companion__scg_GenericCompanion().newBuilder__scm_Builder()
  };
  repr__O() {
    return this
  };
  stringPrefix__T() {
    return $f_sc_TraversableLike__stringPrefix__T(this)
  };
  nonEmpty__Z() {
    return (!this.isEmpty__Z())
  };
  toList__sci_List() {
    const this$1 = $m_sci_List$();
    const cbf = this$1.scg_GenTraversableFactory__f_ReusableCBFInstance;
    return $as_sci_List($f_sc_TraversableLike__to__scg_CanBuildFrom__O(this, cbf))
  };
  mkString__T__T__T__T(start, sep, end) {
    return $f_sc_TraversableOnce__mkString__T__T__T__T(this, start, sep, end)
  };
  addString__scm_StringBuilder__T__T__T__scm_StringBuilder(b, start, sep, end) {
    return $f_sc_TraversableOnce__addString__scm_StringBuilder__T__T__T__scm_StringBuilder(this, b, start, sep, end)
  };
  sizeHintIfCheap__I() {
    return (-1)
  };
}
function $is_sc_GenSeq(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.sc_GenSeq)))
}
function $as_sc_GenSeq(obj) {
  return (($is_sc_GenSeq(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.GenSeq"))
}
function $isArrayOf_sc_GenSeq(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sc_GenSeq)))
}
function $asArrayOf_sc_GenSeq(obj, depth) {
  return (($isArrayOf_sc_GenSeq(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.GenSeq;", depth))
}
const $f_sc_SeqLike__lengthCompare__I__I = (function($thiz, len) {
  if ((len < 0)) {
    return 1
  } else {
    let i = 0;
    const it = $thiz.iterator__sc_Iterator();
    while (it.hasNext__Z()) {
      if ((i === len)) {
        return (it.hasNext__Z() ? 1 : 0)
      };
      it.next__O();
      i = ((1 + i) | 0)
    };
    return ((i - len) | 0)
  }
});
const $f_sc_SeqLike__isEmpty__Z = (function($thiz) {
  return ($thiz.lengthCompare__I__I(0) === 0)
});
function $is_sc_IndexedSeqLike(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.sc_IndexedSeqLike)))
}
function $as_sc_IndexedSeqLike(obj) {
  return (($is_sc_IndexedSeqLike(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.IndexedSeqLike"))
}
function $isArrayOf_sc_IndexedSeqLike(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sc_IndexedSeqLike)))
}
function $asArrayOf_sc_IndexedSeqLike(obj, depth) {
  return (($isArrayOf_sc_IndexedSeqLike(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.IndexedSeqLike;", depth))
}
function $is_sc_LinearSeqLike(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.sc_LinearSeqLike)))
}
function $as_sc_LinearSeqLike(obj) {
  return (($is_sc_LinearSeqLike(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.LinearSeqLike"))
}
function $isArrayOf_sc_LinearSeqLike(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sc_LinearSeqLike)))
}
function $asArrayOf_sc_LinearSeqLike(obj, depth) {
  return (($isArrayOf_sc_LinearSeqLike(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.LinearSeqLike;", depth))
}
const $f_sc_IndexedSeqOptimized__isEmpty__Z = (function($thiz) {
  return ($thiz.length__I() === 0)
});
const $f_sc_IndexedSeqOptimized__foreach__F1__V = (function($thiz, f) {
  let i = 0;
  const len = $thiz.length__I();
  while ((i < len)) {
    f.apply__O__O($thiz.apply__I__O(i));
    i = ((1 + i) | 0)
  }
});
const $f_sc_IndexedSeqOptimized__sameElements__sc_GenIterable__Z = (function($thiz, that) {
  if ($is_sc_IndexedSeq(that)) {
    const x2 = $as_sc_IndexedSeq(that);
    const len = $thiz.length__I();
    if ((len === x2.length__I())) {
      let i = 0;
      while (((i < len) && $m_sr_BoxesRunTime$().equals__O__O__Z($thiz.apply__I__O(i), x2.apply__I__O(i)))) {
        i = ((1 + i) | 0)
      };
      return (i === len)
    } else {
      return false
    }
  } else {
    return $f_sc_IterableLike__sameElements__sc_GenIterable__Z($thiz, that)
  }
});
const $f_sc_IndexedSeqOptimized__copyToArray__O__I__I__V = (function($thiz, xs, start, len) {
  let i = 0;
  let j = start;
  const x = $thiz.length__I();
  const x$1 = ((x < len) ? x : len);
  const that = (($m_sr_ScalaRunTime$().array_length__O__I(xs) - start) | 0);
  const end = ((x$1 < that) ? x$1 : that);
  while ((i < end)) {
    $m_sr_ScalaRunTime$().array_update__O__I__O__V(xs, j, $thiz.apply__I__O(i));
    i = ((1 + i) | 0);
    j = ((1 + j) | 0)
  }
});
const $f_sc_IndexedSeqOptimized__lengthCompare__I__I = (function($thiz, len) {
  return (($thiz.length__I() - len) | 0)
});
const $f_sc_IndexedSeqOptimized__toList__sci_List = (function($thiz) {
  let i = (((-1) + $thiz.length__I()) | 0);
  let result = $m_sci_Nil$();
  while ((i >= 0)) {
    const this$1 = result;
    const x = $thiz.apply__I__O(i);
    result = new $c_sci_$colon$colon(x, this$1);
    i = (((-1) + i) | 0)
  };
  return result
});
const $p_sc_LinearSeqOptimized__loop$1__I__sc_LinearSeqOptimized__I__I = (function($thiz, i, xs, len$1) {
  while (true) {
    if ((i === len$1)) {
      return (xs.isEmpty__Z() ? 0 : 1)
    } else if (xs.isEmpty__Z()) {
      return (-1)
    } else {
      const temp$i = ((1 + i) | 0);
      const temp$xs = $as_sc_LinearSeqOptimized(xs.tail__O());
      i = temp$i;
      xs = temp$xs
    }
  }
});
const $f_sc_LinearSeqOptimized__length__I = (function($thiz) {
  let these = $thiz;
  let len = 0;
  while ((!these.isEmpty__Z())) {
    len = ((1 + len) | 0);
    these = $as_sc_LinearSeqOptimized(these.tail__O())
  };
  return len
});
const $f_sc_LinearSeqOptimized__apply__I__O = (function($thiz, n) {
  const rest = $thiz.drop__I__sc_LinearSeqOptimized(n);
  if (((n < 0) || rest.isEmpty__Z())) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + n))
  };
  return rest.head__O()
});
const $f_sc_LinearSeqOptimized__foreach__F1__V = (function($thiz, f) {
  let these = $thiz;
  while ((!these.isEmpty__Z())) {
    f.apply__O__O(these.head__O());
    these = $as_sc_LinearSeqOptimized(these.tail__O())
  }
});
const $f_sc_LinearSeqOptimized__take__I__sc_LinearSeqOptimized = (function($thiz, n) {
  const b = $thiz.newBuilder__scm_Builder();
  let i = 0;
  let these = $thiz;
  while (((!these.isEmpty__Z()) && (i < n))) {
    i = ((1 + i) | 0);
    b.$plus$eq__O__scm_Builder(these.head__O());
    these = $as_sc_LinearSeqOptimized(these.tail__O())
  };
  return $as_sc_LinearSeqOptimized(b.result__O())
});
const $f_sc_LinearSeqOptimized__drop__I__sc_LinearSeqOptimized = (function($thiz, n) {
  let these = $thiz;
  let count = n;
  while (((!these.isEmpty__Z()) && (count > 0))) {
    these = $as_sc_LinearSeqOptimized(these.tail__O());
    count = (((-1) + count) | 0)
  };
  return these
});
const $f_sc_LinearSeqOptimized__sameElements__sc_GenIterable__Z = (function($thiz, that) {
  if ($is_sc_LinearSeq(that)) {
    const x2 = $as_sc_LinearSeq(that);
    if (($thiz === x2)) {
      return true
    } else {
      let these = $thiz;
      let those = x2;
      while ((((!these.isEmpty__Z()) && (!those.isEmpty__Z())) && $m_sr_BoxesRunTime$().equals__O__O__Z(these.head__O(), those.head__O()))) {
        these = $as_sc_LinearSeqOptimized(these.tail__O());
        those = $as_sc_LinearSeq(those.tail__O())
      };
      return (these.isEmpty__Z() && those.isEmpty__Z())
    }
  } else {
    return $f_sc_IterableLike__sameElements__sc_GenIterable__Z($thiz, that)
  }
});
const $f_sc_LinearSeqOptimized__lengthCompare__I__I = (function($thiz, len) {
  return ((len < 0) ? 1 : $p_sc_LinearSeqOptimized__loop$1__I__sc_LinearSeqOptimized__I__I($thiz, 0, $thiz, len))
});
function $is_sc_LinearSeqOptimized(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.sc_LinearSeqOptimized)))
}
function $as_sc_LinearSeqOptimized(obj) {
  return (($is_sc_LinearSeqOptimized(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.LinearSeqOptimized"))
}
function $isArrayOf_sc_LinearSeqOptimized(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sc_LinearSeqOptimized)))
}
function $asArrayOf_sc_LinearSeqOptimized(obj, depth) {
  return (($isArrayOf_sc_LinearSeqOptimized(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.LinearSeqOptimized;", depth))
}
function $is_sc_Iterable(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.sc_Iterable)))
}
function $as_sc_Iterable(obj) {
  return (($is_sc_Iterable(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.Iterable"))
}
function $isArrayOf_sc_Iterable(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sc_Iterable)))
}
function $asArrayOf_sc_Iterable(obj, depth) {
  return (($isArrayOf_sc_Iterable(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.Iterable;", depth))
}
class $c_sc_AbstractIterable extends $c_sc_AbstractTraversable {
  foreach__F1__V(f) {
    const this$1 = this.iterator__sc_Iterator();
    $f_sc_Iterator__foreach__F1__V(this$1, f)
  };
  take__I__O(n) {
    return $f_sc_IterableLike__take__I__O(this, n)
  };
  copyToArray__O__I__I__V(xs, start, len) {
    $f_sc_IterableLike__copyToArray__O__I__I__V(this, xs, start, len)
  };
  sameElements__sc_GenIterable__Z(that) {
    return $f_sc_IterableLike__sameElements__sc_GenIterable__Z(this, that)
  };
  toStream__sci_Stream() {
    return this.iterator__sc_Iterator().toStream__sci_Stream()
  };
}
const $f_sci_StringLike__$times__I__T = (function($thiz, n) {
  const buf = $ct_scm_StringBuilder__(new $c_scm_StringBuilder());
  const isEmpty = (n <= 0);
  const scala$collection$immutable$Range$$lastElement = (((-1) + n) | 0);
  if ((!isEmpty)) {
    let i = 0;
    while (true) {
      buf.append__T__scm_StringBuilder($thiz.toString__T());
      if ((i === scala$collection$immutable$Range$$lastElement)) {
        break
      };
      i = ((1 + i) | 0)
    }
  };
  return buf.scm_StringBuilder__f_underlying.jl_StringBuilder__f_java$lang$StringBuilder$$content
});
const $p_scm_LinkedListLike__length0__scm_Seq__I__I = (function($thiz, elem, acc) {
  while (true) {
    const this$1 = $as_scm_LinkedListLike(elem);
    if ($f_scm_LinkedListLike__isEmpty__Z(this$1)) {
      return acc
    } else {
      const this$2 = $as_scm_LinkedListLike(elem);
      const temp$elem = this$2.scm_LinkedList__f_next;
      const temp$acc = ((1 + acc) | 0);
      elem = temp$elem;
      acc = temp$acc
    }
  }
});
const $f_scm_LinkedListLike__isEmpty__Z = (function($thiz) {
  return ($thiz.scm_LinkedList__f_next === $thiz)
});
const $f_scm_LinkedListLike__head__O = (function($thiz) {
  if ($f_scm_LinkedListLike__isEmpty__Z($thiz)) {
    throw $ct_ju_NoSuchElementException__(new $c_ju_NoSuchElementException())
  } else {
    return $thiz.scm_LinkedList__f_elem
  }
});
const $f_scm_LinkedListLike__tail__scm_Seq = (function($thiz) {
  const requirement = (!$f_scm_LinkedListLike__isEmpty__Z($thiz));
  if ((!requirement)) {
    throw $ct_jl_IllegalArgumentException__T__(new $c_jl_IllegalArgumentException(), "requirement failed: tail of empty list")
  };
  return $thiz.scm_LinkedList__f_next
});
const $f_scm_LinkedListLike__drop__I__scm_Seq = (function($thiz, n) {
  let i = 0;
  let these = $as_scm_Seq($thiz);
  while (true) {
    let $$x1;
    if ((i < n)) {
      const this$1 = $as_scm_LinkedListLike(these);
      $$x1 = (!$f_scm_LinkedListLike__isEmpty__Z(this$1))
    } else {
      $$x1 = false
    };
    if ($$x1) {
      const this$2 = $as_scm_LinkedListLike(these);
      these = this$2.scm_LinkedList__f_next;
      i = ((1 + i) | 0)
    } else {
      break
    }
  };
  return these
});
const $f_scm_LinkedListLike__apply__I__O = (function($thiz, n) {
  const loc = $f_scm_LinkedListLike__drop__I__scm_Seq($thiz, n);
  if (loc.nonEmpty__Z()) {
    return $as_scm_LinkedListLike(loc).scm_LinkedList__f_elem
  } else {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + n))
  }
});
const $f_scm_LinkedListLike__foreach__F1__V = (function($thiz, f) {
  let these = $thiz;
  while (true) {
    const this$1 = these;
    if ((!$f_scm_LinkedListLike__isEmpty__Z(this$1))) {
      f.apply__O__O(these.scm_LinkedList__f_elem);
      const this$2 = these;
      these = this$2.scm_LinkedList__f_next
    } else {
      break
    }
  }
});
function $is_scm_LinkedListLike(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.scm_LinkedListLike)))
}
function $as_scm_LinkedListLike(obj) {
  return (($is_scm_LinkedListLike(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.mutable.LinkedListLike"))
}
function $isArrayOf_scm_LinkedListLike(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.scm_LinkedListLike)))
}
function $asArrayOf_scm_LinkedListLike(obj, depth) {
  return (($isArrayOf_scm_LinkedListLike(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.mutable.LinkedListLike;", depth))
}
class $c_sci_StringOps extends $c_O {
  constructor(repr) {
    super();
    this.sci_StringOps__f_repr = null;
    this.sci_StringOps__f_repr = repr
  };
  foreach__F1__V(f) {
    $f_sc_IndexedSeqOptimized__foreach__F1__V(this, f)
  };
  sameElements__sc_GenIterable__Z(that) {
    return $f_sc_IndexedSeqOptimized__sameElements__sc_GenIterable__Z(this, that)
  };
  copyToArray__O__I__I__V(xs, start, len) {
    $f_sc_IndexedSeqOptimized__copyToArray__O__I__I__V(this, xs, start, len)
  };
  lengthCompare__I__I(len) {
    return $f_sc_IndexedSeqOptimized__lengthCompare__I__I(this, len)
  };
  toList__sci_List() {
    return $f_sc_IndexedSeqOptimized__toList__sci_List(this)
  };
  iterator__sc_Iterator() {
    const this$ = this.sci_StringOps__f_repr;
    return new $c_sc_IndexedSeqLike$Elements(this, 0, $uI(this$.length))
  };
  sizeHintIfCheap__I() {
    const this$ = this.sci_StringOps__f_repr;
    return $uI(this$.length)
  };
  size__I() {
    const this$ = this.sci_StringOps__f_repr;
    return $uI(this$.length)
  };
  toStream__sci_Stream() {
    const this$ = this.sci_StringOps__f_repr;
    const this$2 = new $c_sc_IndexedSeqLike$Elements(this, 0, $uI(this$.length));
    return $f_sc_Iterator__toStream__sci_Stream(this$2)
  };
  stringPrefix__T() {
    return $f_sc_TraversableLike__stringPrefix__T(this)
  };
  mkString__T__T__T__T(start, sep, end) {
    return $f_sc_TraversableOnce__mkString__T__T__T__T(this, start, sep, end)
  };
  addString__scm_StringBuilder__T__T__T__scm_StringBuilder(b, start, sep, end) {
    return $f_sc_TraversableOnce__addString__scm_StringBuilder__T__T__T__scm_StringBuilder(this, b, start, sep, end)
  };
  toString__T() {
    const this$ = this.sci_StringOps__f_repr;
    return this$
  };
  length__I() {
    const this$ = this.sci_StringOps__f_repr;
    return $uI(this$.length)
  };
  hashCode__I() {
    const this$ = this.sci_StringOps__f_repr;
    return $f_T__hashCode__I(this$)
  };
  equals__O__Z(x$1) {
    return $m_sci_StringOps$().equals$extension__T__O__Z(this.sci_StringOps__f_repr, x$1)
  };
  compare__O__I(that) {
    const other = $as_T(that);
    const this$ = this.sci_StringOps__f_repr;
    return ((this$ === other) ? 0 : ($uZ((this$ < other)) ? (-1) : 1))
  };
  seq__sc_TraversableOnce() {
    const this$ = this.sci_StringOps__f_repr;
    return new $c_sci_WrappedString(this$)
  };
  apply__I__O(idx) {
    const this$ = this.sci_StringOps__f_repr;
    return $bC((65535 & $uI(this$.charCodeAt(idx))))
  };
  newBuilder__scm_Builder() {
    return $ct_scm_StringBuilder__(new $c_scm_StringBuilder())
  };
  thisCollection__sc_Traversable() {
    const this$ = this.sci_StringOps__f_repr;
    return new $c_sci_WrappedString(this$)
  };
  repr__O() {
    return this.sci_StringOps__f_repr
  };
}
function $as_sci_StringOps(obj) {
  return (((obj instanceof $c_sci_StringOps) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.immutable.StringOps"))
}
function $isArrayOf_sci_StringOps(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sci_StringOps)))
}
function $asArrayOf_sci_StringOps(obj, depth) {
  return (($isArrayOf_sci_StringOps(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.immutable.StringOps;", depth))
}
const $d_sci_StringOps = new $TypeData().initClass({
  sci_StringOps: 0
}, false, "scala.collection.immutable.StringOps", {
  sci_StringOps: 1,
  O: 1,
  sci_StringLike: 1,
  sc_IndexedSeqOptimized: 1,
  sc_IndexedSeqLike: 1,
  sc_SeqLike: 1,
  sc_IterableLike: 1,
  s_Equals: 1,
  sc_TraversableLike: 1,
  scg_HasNewBuilder: 1,
  scg_FilterMonadic: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1,
  sc_GenTraversableLike: 1,
  sc_Parallelizable: 1,
  sc_GenIterableLike: 1,
  sc_GenSeqLike: 1,
  s_math_Ordered: 1,
  jl_Comparable: 1
});
$c_sci_StringOps.prototype.$classData = $d_sci_StringOps;
const $p_Lsgl_html5_Html5App__runScheduler$1__V = (function($thiz) {
  if ($thiz.Lcom_regblanc_scalavator_html5_Main$__f_Scheduler.run__J__Z(new $c_RTLong(5, 0))) {
    $m_Lorg_scalajs_dom_package$().window__Lorg_scalajs_dom_raw_Window().setTimeout(((arg$outer) => (() => {
      $p_Lsgl_html5_Html5App__runScheduler$1__V(arg$outer)
    }))($thiz), 20.0)
  } else {
    $m_Lorg_scalajs_dom_package$().window__Lorg_scalajs_dom_raw_Window().setTimeout(((arg$outer$1) => (() => {
      $p_Lsgl_html5_Html5App__runScheduler$1__V(arg$outer$1)
    }))($thiz), 0.0)
  }
});
const $p_Lsgl_html5_Html5App__frameCode$1__D__sr_ObjectRef__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__Z__V = (function($thiz, now, lastTime$1, canvas$2, requestAnimationFrameSupported$1) {
  const this$1 = $as_s_Option(lastTime$1.sr_ObjectRef__f_elem);
  const dt = (now - $uD((this$1.isEmpty__Z() ? now : this$1.get__O())));
  lastTime$1.sr_ObjectRef__f_elem = new $c_s_Some(now);
  const this$2 = $m_RTLong$();
  const lo = this$2.org$scalajs$linker$runtime$RuntimeLong$$fromDoubleImpl__D__I(dt);
  const hi = this$2.RTLong$__f_org$scalajs$linker$runtime$RuntimeLong$$hiReturn;
  $f_Lsgl_GameLoopComponent__gameLoopStep__J__Lsgl_GraphicsProvider$Graphics$AbstractCanvas__V($thiz, new $c_RTLong(lo, hi), canvas$2);
  if (requestAnimationFrameSupported$1) {
    $m_Lorg_scalajs_dom_package$().window__Lorg_scalajs_dom_raw_Window().requestAnimationFrame(((arg$outer, lastTime$1$1, canvas$2$1, requestAnimationFrameSupported$1$1) => ((arg1$2) => {
      const arg1 = $uD(arg1$2);
      $p_Lsgl_html5_Html5App__frameCode$1__D__sr_ObjectRef__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__Z__V(arg$outer, arg1, lastTime$1$1, canvas$2$1, requestAnimationFrameSupported$1$1)
    }))($thiz, lastTime$1, canvas$2, requestAnimationFrameSupported$1))
  }
});
const $f_Lsgl_html5_Html5App__main__AT__V = (function($thiz, args) {
  const $$x1 = document;
  const s = $thiz.Lcom_regblanc_scalavator_html5_Main$__f_GameCanvasID;
  const canvas = $$x1.getElementById(s);
  $f_Lsgl_html5_Html5App__run__Lorg_scalajs_dom_raw_HTMLCanvasElement__V($thiz, canvas)
});
const $f_Lsgl_html5_Html5App__prepareCanvas__Lorg_scalajs_dom_raw_HTMLCanvasElement__V = (function($thiz, canvas) {
  canvas.style.width = ($uI(canvas.width) + "px");
  canvas.style.height = ($uI(canvas.height) + "px");
  canvas.width = $doubleToInt(($uD($m_Lorg_scalajs_dom_package$().window__Lorg_scalajs_dom_raw_Window().devicePixelRatio) * $uI(canvas.width)));
  canvas.height = $doubleToInt(($uD($m_Lorg_scalajs_dom_package$().window__Lorg_scalajs_dom_raw_Window().devicePixelRatio) * $uI(canvas.height)))
});
const $f_Lsgl_html5_Html5App__run__Lorg_scalajs_dom_raw_HTMLCanvasElement__V = (function($thiz, canvas) {
  $f_Lsgl_html5_Html5App__prepareCanvas__Lorg_scalajs_dom_raw_HTMLCanvasElement__V($thiz, canvas);
  $m_Lorg_scalajs_dom_package$().window__Lorg_scalajs_dom_raw_Window().onresize = ((arg$outer, canvas$1) => ((arg1$2) => {
    $f_Lsgl_html5_Html5App__sgl$html5$Html5App$$$anonfun$run$1__Lorg_scalajs_dom_raw_Event__Lorg_scalajs_dom_raw_HTMLCanvasElement__V(arg$outer, arg1$2, canvas$1)
  }))($thiz, canvas);
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_htmlCanvas = canvas;
  $f_Lsgl_html5_Html5InputProvider__registerInputListeners__V($thiz);
  $f_Lsgl_html5_Html5App__startGameLoop__V($thiz);
  $m_Lorg_scalajs_dom_package$().window__Lorg_scalajs_dom_raw_Window().setTimeout(((arg$outer$1) => (() => {
    $p_Lsgl_html5_Html5App__runScheduler$1__V(arg$outer$1)
  }))($thiz), 50.0)
});
const $f_Lsgl_html5_Html5App__startGameLoop__V = (function($thiz) {
  const canvas = new $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas($thiz.Lcom_regblanc_scalavator_html5_Main$__f_Graphics, $thiz.Lcom_regblanc_scalavator_html5_Main$__f_htmlCanvas);
  const this$1 = $thiz.Lcom_regblanc_scalavator_html5_Main$__f_TargetFps;
  let targetFramePeriod;
  if (this$1.isEmpty__Z()) {
    targetFramePeriod = $m_s_None$()
  } else {
    const arg1 = this$1.get__O();
    const fps = $uI(arg1);
    targetFramePeriod = new $c_s_Some($f_Lsgl_GameLoopComponent__framePeriod__I__J($thiz, fps))
  };
  const v = requestAnimationFrame;
  const requestAnimationFrameSupported = (!(v === (void 0)));
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_gameState.newScreen__Lsgl_GameStateComponent$GameScreen__V(new $c_Lcom_regblanc_scalavator_core_ScalavatorGame$MainScreen($thiz));
  const elem = $m_s_None$();
  const lastTime = new $c_sr_ObjectRef(elem);
  if (requestAnimationFrameSupported) {
    $m_Lorg_scalajs_dom_package$().window__Lorg_scalajs_dom_raw_Window().requestAnimationFrame(((arg$outer, lastTime$1, canvas$2, requestAnimationFrameSupported$1) => ((arg1$2) => {
      const arg1$1 = $uD(arg1$2);
      $p_Lsgl_html5_Html5App__frameCode$1__D__sr_ObjectRef__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__Z__V(arg$outer, arg1$1, lastTime$1, canvas$2, requestAnimationFrameSupported$1)
    }))($thiz, lastTime, canvas, requestAnimationFrameSupported))
  } else {
    const this$4 = $thiz.Lcom_regblanc_scalavator_html5_Main$__f_logger;
    const tag = $thiz.Lcom_regblanc_scalavator_html5_Main$__f_sgl$html5$Html5App$$Tag;
    const this$5 = this$4.Lsgl_html5_util_Html5VerboseConsoleLoggingProvider$VerboseConsoleLogger$__f_logLevel;
    const that = this$4.Lsgl_util_LoggingProvider$Logger__f_$outer.Logger__Lsgl_util_LoggingProvider$Logger$().Warning__Lsgl_util_LoggingProvider$Logger$Warning$();
    if ($f_s_math_Ordered__$greater$eq__O__Z(this$5, that)) {
      this$4.log__Lsgl_util_LoggingProvider$Logger$LogLevel__Lsgl_util_LoggingProvider$Logger$Tag__T__V(this$4.Lsgl_util_LoggingProvider$Logger__f_$outer.Logger__Lsgl_util_LoggingProvider$Logger$().Warning__Lsgl_util_LoggingProvider$Logger$Warning$(), tag, "window.requestAnimationFrame not supported, fallback to setInterval for the game loop")
    };
    const $$x1 = $m_Lorg_scalajs_dom_package$().window__Lorg_scalajs_dom_raw_Window();
    let this$6;
    if (targetFramePeriod.isEmpty__Z()) {
      this$6 = $m_s_None$()
    } else {
      const arg1$3 = targetFramePeriod.get__O();
      const t = $uJ(arg1$3);
      const lo = t.RTLong__f_lo;
      const hi = t.RTLong__f_hi;
      this$6 = new $c_s_Some($m_RTLong$().org$scalajs$linker$runtime$RuntimeLong$$toDouble__I__I__D(lo, hi))
    };
    $$x1.setInterval(((arg$outer$1, lastTime$1$1, canvas$2$1, requestAnimationFrameSupported$1$1) => (() => $f_Lsgl_html5_Html5App__sgl$html5$Html5App$$$anonfun$startGameLoop$7__sr_ObjectRef__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__Z__O(arg$outer$1, lastTime$1$1, canvas$2$1, requestAnimationFrameSupported$1$1)))($thiz, lastTime, canvas, requestAnimationFrameSupported), $uD((this$6.isEmpty__Z() ? 33.333333333333336 : this$6.get__O())))
  }
});
const $f_Lsgl_html5_Html5App__sgl$html5$Html5App$$$anonfun$run$1__Lorg_scalajs_dom_raw_Event__Lorg_scalajs_dom_raw_HTMLCanvasElement__V = (function($thiz, event, canvas$1) {
  $f_Lsgl_html5_Html5App__prepareCanvas__Lorg_scalajs_dom_raw_HTMLCanvasElement__V($thiz, canvas$1)
});
const $f_Lsgl_html5_Html5App__sgl$html5$Html5App$$$anonfun$startGameLoop$7__sr_ObjectRef__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__Z__O = (function($thiz, lastTime$1, canvas$2, requestAnimationFrameSupported$1) {
  $p_Lsgl_html5_Html5App__frameCode$1__D__sr_ObjectRef__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$Html5Canvas__Z__V($thiz, $uD(Date.now()), lastTime$1, canvas$2, requestAnimationFrameSupported$1)
});
const $f_Lsgl_html5_Html5App__$init$__V = (function($thiz) {
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_htmlCanvas = null;
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_sgl$html5$Html5App$$Tag = new $c_Lsgl_util_LoggingProvider$Logger$Tag($thiz.Logger__Lsgl_util_LoggingProvider$Logger$(), "game-loop");
  $thiz.Lcom_regblanc_scalavator_html5_Main$__f_MaxLoopStepDelta = new $c_s_Some(new $c_RTLong(1000, 0))
});
function $is_Lsgl_html5_Html5App(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.Lsgl_html5_Html5App)))
}
function $as_Lsgl_html5_Html5App(obj) {
  return (($is_Lsgl_html5_Html5App(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "sgl.html5.Html5App"))
}
function $isArrayOf_Lsgl_html5_Html5App(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.Lsgl_html5_Html5App)))
}
function $asArrayOf_Lsgl_html5_Html5App(obj, depth) {
  return (($isArrayOf_Lsgl_html5_Html5App(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lsgl.html5.Html5App;", depth))
}
function $is_sc_IndexedSeq(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.sc_IndexedSeq)))
}
function $as_sc_IndexedSeq(obj) {
  return (($is_sc_IndexedSeq(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.IndexedSeq"))
}
function $isArrayOf_sc_IndexedSeq(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sc_IndexedSeq)))
}
function $asArrayOf_sc_IndexedSeq(obj, depth) {
  return (($isArrayOf_sc_IndexedSeq(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.IndexedSeq;", depth))
}
function $is_sc_LinearSeq(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.sc_LinearSeq)))
}
function $as_sc_LinearSeq(obj) {
  return (($is_sc_LinearSeq(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.LinearSeq"))
}
function $isArrayOf_sc_LinearSeq(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sc_LinearSeq)))
}
function $asArrayOf_sc_LinearSeq(obj, depth) {
  return (($isArrayOf_sc_LinearSeq(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.LinearSeq;", depth))
}
const $p_Lcom_regblanc_scalavator_html5_Main$__VerboseConsoleLogger$lzycompute$1__V = (function($thiz) {
  if (($m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_VerboseConsoleLogger$module === null)) {
    $m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_VerboseConsoleLogger$module = new $c_Lsgl_html5_util_Html5VerboseConsoleLoggingProvider$VerboseConsoleLogger$($thiz)
  }
});
const $p_Lcom_regblanc_scalavator_html5_Main$__Html5Audio$lzycompute$1__V = (function($thiz) {
  if (($m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Html5Audio$module === null)) {
    $m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Html5Audio$module = new $c_Lsgl_html5_Html5AudioProvider$Html5Audio$($thiz)
  }
});
const $p_Lcom_regblanc_scalavator_html5_Main$__Html5Graphics$lzycompute$1__V = (function($thiz) {
  if (($m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Html5Graphics$module === null)) {
    $m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Html5Graphics$module = new $c_Lsgl_html5_Html5GraphicsProvider$Html5Graphics$($thiz)
  }
});
const $p_Lcom_regblanc_scalavator_html5_Main$__Html5System$lzycompute$1__V = (function($thiz) {
  if (($m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Html5System$module === null)) {
    $m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Html5System$module = new $c_Lsgl_html5_Html5SystemProvider$Html5System$($thiz)
  }
});
const $p_Lcom_regblanc_scalavator_html5_Main$__SilentGameLoopListener$lzycompute$1__V = (function($thiz) {
  if (($m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_SilentGameLoopListener$module === null)) {
    $m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_SilentGameLoopListener$module = new $c_Lsgl_GameLoopComponent$SilentGameLoopListener$($thiz)
  }
});
const $p_Lcom_regblanc_scalavator_html5_Main$__Logger$lzycompute$1__V = (function($thiz) {
  if (($m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Logger$module === null)) {
    $m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Logger$module = new $c_Lsgl_util_LoggingProvider$Logger$($thiz)
  }
});
const $p_Lcom_regblanc_scalavator_html5_Main$__Input$lzycompute$1__V = (function($thiz) {
  if (($m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Input$module === null)) {
    $m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Input$module = new $c_Lsgl_InputProvider$Input$($thiz)
  }
});
class $c_Lcom_regblanc_scalavator_html5_Main$ extends $c_O {
  constructor() {
    super();
    this.Lcom_regblanc_scalavator_html5_Main$__f_GameCanvasID = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_theme = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_VerboseConsoleLogger$module = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_logger = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_htmlCanvas = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_sgl$html5$Html5App$$Tag = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_MaxLoopStepDelta = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_sgl$util$SingleThreadSchedulerProvider$$Tag = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_Scheduler = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_GuardAutoPlay = false;
    this.Lcom_regblanc_scalavator_html5_Main$__f_Html5Audio$module = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_Audio = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_ProcessInputsDuringUpdate = false;
    this.Lcom_regblanc_scalavator_html5_Main$__f_sgl$html5$Html5InputProvider$$hasUserInteracted = false;
    this.Lcom_regblanc_scalavator_html5_Main$__f_sgl$html5$Html5InputProvider$$actionsOnUserInteraction = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_Html5Graphics$module = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_Graphics = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_Html5System$module = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_System = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_ResourcesRoot = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_MultiDPIResourcesRoot = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_PartsResourcePath$module = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_Window = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_lifecycleListener = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_gameState = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_TargetFps = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_SilentGameLoopListener$module = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_gameLoopListener = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_Logger$module = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_ResourceNotFoundException$module = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_ResourceFormatUnsupportedException$module = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_sgl$InputProvider$$LogTag = null;
    this.Lcom_regblanc_scalavator_html5_Main$__f_Input$module = null;
    $n_Lcom_regblanc_scalavator_html5_Main$ = this;
    $f_Lsgl_InputProvider__$init$__V(this);
    $f_Lsgl_GameLoopComponent__$init$__V(this);
    $f_Lsgl_GameStateComponent__$init$__V(this);
    this.Lcom_regblanc_scalavator_html5_Main$__f_lifecycleListener = $m_Lsgl_SilentLifecyclieListener$();
    $f_Lsgl_html5_Html5WindowProvider__$init$__V(this);
    $f_Lsgl_html5_Html5SystemProvider__$init$__V(this);
    this.Lcom_regblanc_scalavator_html5_Main$__f_Graphics = this.Html5Graphics__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$();
    $f_Lsgl_html5_Html5InputProvider__$init$__V(this);
    $f_Lsgl_html5_Html5AudioProvider__$init$__V(this);
    $f_Lsgl_util_SingleThreadSchedulerProvider__$init$__V(this);
    $f_Lsgl_html5_Html5App__$init$__V(this);
    this.Lcom_regblanc_scalavator_html5_Main$__f_logger = this.VerboseConsoleLogger__Lsgl_html5_util_Html5VerboseConsoleLoggingProvider$VerboseConsoleLogger$();
    this.Lcom_regblanc_scalavator_html5_Main$__f_GameCanvasID = "scalavator_canvas_1";
    this.Lcom_regblanc_scalavator_html5_Main$__f_theme = new $c_Lsgl_html5_themes_NoTheme()
  };
  $js$exported$meth$run__Lorg_scalajs_dom_raw_HTMLCanvasElement__O(canvas) {
    $f_Lsgl_html5_Html5App__run__Lorg_scalajs_dom_raw_HTMLCanvasElement__V(this, canvas)
  };
  VerboseConsoleLogger__Lsgl_html5_util_Html5VerboseConsoleLoggingProvider$VerboseConsoleLogger$() {
    if (($m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_VerboseConsoleLogger$module === null)) {
      $p_Lcom_regblanc_scalavator_html5_Main$__VerboseConsoleLogger$lzycompute$1__V(this)
    };
    return $m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_VerboseConsoleLogger$module
  };
  Html5Audio__Lsgl_html5_Html5AudioProvider$Html5Audio$() {
    if (($m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Html5Audio$module === null)) {
      $p_Lcom_regblanc_scalavator_html5_Main$__Html5Audio$lzycompute$1__V(this)
    };
    return $m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Html5Audio$module
  };
  Html5Graphics__Lsgl_html5_Html5GraphicsProvider$Html5Graphics$() {
    if (($m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Html5Graphics$module === null)) {
      $p_Lcom_regblanc_scalavator_html5_Main$__Html5Graphics$lzycompute$1__V(this)
    };
    return $m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Html5Graphics$module
  };
  Html5System__Lsgl_html5_Html5SystemProvider$Html5System$() {
    if (($m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Html5System$module === null)) {
      $p_Lcom_regblanc_scalavator_html5_Main$__Html5System$lzycompute$1__V(this)
    };
    return $m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Html5System$module
  };
  SilentGameLoopListener__Lsgl_GameLoopComponent$SilentGameLoopListener$() {
    if (($m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_SilentGameLoopListener$module === null)) {
      $p_Lcom_regblanc_scalavator_html5_Main$__SilentGameLoopListener$lzycompute$1__V(this)
    };
    return $m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_SilentGameLoopListener$module
  };
  Logger__Lsgl_util_LoggingProvider$Logger$() {
    if (($m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Logger$module === null)) {
      $p_Lcom_regblanc_scalavator_html5_Main$__Logger$lzycompute$1__V(this)
    };
    return $m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Logger$module
  };
  Input__Lsgl_InputProvider$Input$() {
    if (($m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Input$module === null)) {
      $p_Lcom_regblanc_scalavator_html5_Main$__Input$lzycompute$1__V(this)
    };
    return $m_Lcom_regblanc_scalavator_html5_Main$().Lcom_regblanc_scalavator_html5_Main$__f_Input$module
  };
  "run"(arg) {
    const prep0 = arg;
    return this.$js$exported$meth$run__Lorg_scalajs_dom_raw_HTMLCanvasElement__O(prep0)
  };
}
const $d_Lcom_regblanc_scalavator_html5_Main$ = new $TypeData().initClass({
  Lcom_regblanc_scalavator_html5_Main$: 0
}, false, "com.regblanc.scalavator.html5.Main$", {
  Lcom_regblanc_scalavator_html5_Main$: 1,
  O: 1,
  Lcom_regblanc_scalavator_core_ScalavatorGame: 1,
  Lsgl_html5_Html5App: 1,
  Lsgl_GameApp: 1,
  Lsgl_GraphicsProvider: 1,
  Lsgl_GraphicsHelpersComponent: 1,
  Lsgl_InputProvider: 1,
  Lsgl_AudioProvider: 1,
  Lsgl_WindowProvider: 1,
  Lsgl_SystemProvider: 1,
  Lsgl_util_LoggingProvider: 1,
  Lsgl_GameLoopComponent: 1,
  Lsgl_GameStateComponent: 1,
  Lsgl_LifecycleListenerProvider: 1,
  Lsgl_html5_Html5WindowProvider: 1,
  Lsgl_html5_Html5SystemProvider: 1,
  Lsgl_PartsResourcePathProvider: 1,
  Lsgl_html5_Html5GraphicsProvider: 1,
  Lsgl_html5_Html5InputProvider: 1,
  Lsgl_html5_Html5AudioProvider: 1,
  Lsgl_util_SingleThreadSchedulerProvider: 1,
  Lsgl_util_SchedulerProvider: 1,
  Lsgl_html5_util_Html5VerboseConsoleLoggingProvider: 1,
  Lsgl_html5_util_Html5ConsoleLoggingProvider: 1
});
$c_Lcom_regblanc_scalavator_html5_Main$.prototype.$classData = $d_Lcom_regblanc_scalavator_html5_Main$;
let $n_Lcom_regblanc_scalavator_html5_Main$ = (void 0);
function $m_Lcom_regblanc_scalavator_html5_Main$() {
  if ((!$n_Lcom_regblanc_scalavator_html5_Main$)) {
    $n_Lcom_regblanc_scalavator_html5_Main$ = new $c_Lcom_regblanc_scalavator_html5_Main$()
  };
  return $n_Lcom_regblanc_scalavator_html5_Main$
}
class $c_sc_AbstractSeq extends $c_sc_AbstractIterable {
  lengthCompare__I__I(len) {
    return $f_sc_SeqLike__lengthCompare__I__I(this, len)
  };
  isEmpty__Z() {
    return $f_sc_SeqLike__isEmpty__Z(this)
  };
  size__I() {
    return this.length__I()
  };
  toString__T() {
    return $f_sc_TraversableLike__toString__T(this)
  };
  hashCode__I() {
    return $m_s_util_hashing_MurmurHash3$().seqHash__sc_Seq__I(this.seq__sc_Seq())
  };
  equals__O__Z(that) {
    return $f_sc_GenSeqLike__equals__O__Z(this, that)
  };
}
function $is_scm_Seq(obj) {
  return (!(!((obj && obj.$classData) && obj.$classData.ancestors.scm_Seq)))
}
function $as_scm_Seq(obj) {
  return (($is_scm_Seq(obj) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.mutable.Seq"))
}
function $isArrayOf_scm_Seq(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.scm_Seq)))
}
function $asArrayOf_scm_Seq(obj, depth) {
  return (($isArrayOf_scm_Seq(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.mutable.Seq;", depth))
}
class $c_scm_AbstractSeq extends $c_sc_AbstractSeq {
  seq__scm_Seq() {
    return this
  };
  seq__sc_TraversableOnce() {
    return this.seq__scm_Seq()
  };
}
class $c_sci_Stream extends $c_sc_AbstractSeq {
  sameElements__sc_GenIterable__Z(that) {
    return $f_sc_LinearSeqOptimized__sameElements__sc_GenIterable__Z(this, that)
  };
  lengthCompare__I__I(len) {
    return $f_sc_LinearSeqOptimized__lengthCompare__I__I(this, len)
  };
  hashCode__I() {
    return $m_s_util_hashing_MurmurHash3$().seqHash__sc_Seq__I(this)
  };
  companion__scg_GenericCompanion() {
    return $m_sci_Stream$()
  };
  append__F0__sci_Stream(rest) {
    if (this.isEmpty__Z()) {
      return $as_sc_GenTraversableOnce(rest.apply__O()).toStream__sci_Stream()
    } else {
      const hd = this.head__O();
      const tl = new $c_sjsr_AnonFunction0(((this$1, rest$1) => (() => $as_sci_Stream(this$1.tail__O()).append__F0__sci_Stream(rest$1)))(this, rest));
      return new $c_sci_Stream$Cons(hd, tl)
    }
  };
  force__sci_Stream() {
    let these = this;
    let those = this;
    if ((!these.isEmpty__Z())) {
      these = $as_sci_Stream(these.tail__O())
    };
    while ((those !== these)) {
      if (these.isEmpty__Z()) {
        return this
      };
      these = $as_sci_Stream(these.tail__O());
      if (these.isEmpty__Z()) {
        return this
      };
      these = $as_sci_Stream(these.tail__O());
      if ((these === those)) {
        return this
      };
      those = $as_sci_Stream(those.tail__O())
    };
    return this
  };
  length__I() {
    let len = 0;
    let left = this;
    while ((!left.isEmpty__Z())) {
      len = ((1 + len) | 0);
      left = $as_sci_Stream(left.tail__O())
    };
    return len
  };
  toStream__sci_Stream() {
    return this
  };
  flatMap__F1__scg_CanBuildFrom__O(f, bf) {
    if ((bf.apply__O__scm_Builder(this) instanceof $c_sci_Stream$StreamBuilder)) {
      let x$1;
      if (this.isEmpty__Z()) {
        x$1 = $m_sci_Stream$Empty$()
      } else {
        const nonEmptyPrefix = new $c_sr_ObjectRef(this);
        let prefix = $as_sc_GenTraversableOnce(f.apply__O__O($as_sci_Stream(nonEmptyPrefix.sr_ObjectRef__f_elem).head__O())).toStream__sci_Stream();
        while (((!$as_sci_Stream(nonEmptyPrefix.sr_ObjectRef__f_elem).isEmpty__Z()) && prefix.isEmpty__Z())) {
          nonEmptyPrefix.sr_ObjectRef__f_elem = $as_sci_Stream($as_sci_Stream(nonEmptyPrefix.sr_ObjectRef__f_elem).tail__O());
          if ((!$as_sci_Stream(nonEmptyPrefix.sr_ObjectRef__f_elem).isEmpty__Z())) {
            prefix = $as_sc_GenTraversableOnce(f.apply__O__O($as_sci_Stream(nonEmptyPrefix.sr_ObjectRef__f_elem).head__O())).toStream__sci_Stream()
          }
        };
        x$1 = ($as_sci_Stream(nonEmptyPrefix.sr_ObjectRef__f_elem).isEmpty__Z() ? ($m_sci_Stream$(), $m_sci_Stream$Empty$()) : prefix.append__F0__sci_Stream(new $c_sjsr_AnonFunction0(((this$3, nonEmptyPrefix$1, f$1) => (() => {
          const x = $as_sci_Stream($as_sci_Stream(nonEmptyPrefix$1.sr_ObjectRef__f_elem).tail__O()).flatMap__F1__scg_CanBuildFrom__O(f$1, ($m_sci_Stream$(), new $c_sci_Stream$StreamCanBuildFrom()));
          return $as_sci_Stream(x)
        }))(this, nonEmptyPrefix, f))))
      };
      return x$1
    } else {
      return $f_sc_TraversableLike__flatMap__F1__scg_CanBuildFrom__O(this, f, bf)
    }
  };
  iterator__sc_Iterator() {
    return new $c_sci_StreamIterator(this)
  };
  foreach__F1__V(f) {
    let _$this = this;
    while (true) {
      if ((!_$this.isEmpty__Z())) {
        f.apply__O__O(_$this.head__O());
        _$this = $as_sci_Stream(_$this.tail__O());
        continue
      };
      break
    }
  };
  addString__scm_StringBuilder__T__T__T__scm_StringBuilder(b, start, sep, end) {
    b.append__T__scm_StringBuilder(start);
    if ((!this.isEmpty__Z())) {
      b.append__O__scm_StringBuilder(this.head__O());
      let cursor = this;
      let n = 1;
      if (cursor.tailDefined__Z()) {
        let scout = $as_sci_Stream(this.tail__O());
        if (scout.isEmpty__Z()) {
          b.append__T__scm_StringBuilder(end);
          return b
        };
        if ((cursor !== scout)) {
          cursor = scout;
          if (scout.tailDefined__Z()) {
            scout = $as_sci_Stream(scout.tail__O());
            while (((cursor !== scout) && scout.tailDefined__Z())) {
              b.append__T__scm_StringBuilder(sep).append__O__scm_StringBuilder(cursor.head__O());
              n = ((1 + n) | 0);
              cursor = $as_sci_Stream(cursor.tail__O());
              scout = $as_sci_Stream(scout.tail__O());
              if (scout.tailDefined__Z()) {
                scout = $as_sci_Stream(scout.tail__O())
              }
            }
          }
        };
        if ((!scout.tailDefined__Z())) {
          while ((cursor !== scout)) {
            b.append__T__scm_StringBuilder(sep).append__O__scm_StringBuilder(cursor.head__O());
            n = ((1 + n) | 0);
            cursor = $as_sci_Stream(cursor.tail__O())
          };
          const this$1 = cursor;
          if ((!this$1.isEmpty__Z())) {
            b.append__T__scm_StringBuilder(sep).append__O__scm_StringBuilder(cursor.head__O())
          }
        } else {
          let runner = this;
          let k = 0;
          while ((runner !== scout)) {
            runner = $as_sci_Stream(runner.tail__O());
            scout = $as_sci_Stream(scout.tail__O());
            k = ((1 + k) | 0)
          };
          if (((cursor === scout) && (k > 0))) {
            b.append__T__scm_StringBuilder(sep).append__O__scm_StringBuilder(cursor.head__O());
            n = ((1 + n) | 0);
            cursor = $as_sci_Stream(cursor.tail__O())
          };
          while ((cursor !== scout)) {
            b.append__T__scm_StringBuilder(sep).append__O__scm_StringBuilder(cursor.head__O());
            n = ((1 + n) | 0);
            cursor = $as_sci_Stream(cursor.tail__O())
          };
          n = ((n - k) | 0)
        }
      };
      if ((!cursor.isEmpty__Z())) {
        if ((!cursor.tailDefined__Z())) {
          b.append__T__scm_StringBuilder(sep).append__T__scm_StringBuilder("?")
        } else {
          b.append__T__scm_StringBuilder(sep).append__T__scm_StringBuilder("...")
        }
      }
    };
    b.append__T__scm_StringBuilder(end);
    return b
  };
  mkString__T__T__T__T(start, sep, end) {
    this.force__sci_Stream();
    return $f_sc_TraversableOnce__mkString__T__T__T__T(this, start, sep, end)
  };
  toString__T() {
    return $f_sc_TraversableOnce__mkString__T__T__T__T(this, "Stream(", ", ", ")")
  };
  take__I__sci_Stream(n) {
    if (((n <= 0) || this.isEmpty__Z())) {
      $m_sci_Stream$();
      return $m_sci_Stream$Empty$()
    } else if ((n === 1)) {
      const hd = this.head__O();
      const tl = new $c_sjsr_AnonFunction0(((this$2) => (() => {
        $m_sci_Stream$();
        return $m_sci_Stream$Empty$()
      }))(this));
      return new $c_sci_Stream$Cons(hd, tl)
    } else {
      const hd$1 = this.head__O();
      const tl$1 = new $c_sjsr_AnonFunction0(((this$2$1, n$1) => (() => $as_sci_Stream(this$2$1.tail__O()).take__I__sci_Stream((((-1) + n$1) | 0))))(this, n));
      return new $c_sci_Stream$Cons(hd$1, tl$1)
    }
  };
  drop__I__sci_Stream(n) {
    let _$this = this;
    while (true) {
      if (((n <= 0) || _$this.isEmpty__Z())) {
        return _$this
      } else {
        const temp$_$this = $as_sci_Stream(_$this.tail__O());
        const temp$n = (((-1) + n) | 0);
        _$this = temp$_$this;
        n = temp$n
      }
    }
  };
  stringPrefix__T() {
    return "Stream"
  };
  equals__O__Z(that) {
    return ((this === that) || $f_sc_GenSeqLike__equals__O__Z(this, that))
  };
  thisCollection__sc_Traversable() {
    return this
  };
  seq__sc_TraversableOnce() {
    return this
  };
  seq__sc_Seq() {
    return this
  };
  apply__O__O(v1) {
    const n = $uI(v1);
    return $f_sc_LinearSeqOptimized__apply__I__O(this, n)
  };
  drop__I__sc_LinearSeqOptimized(n) {
    return this.drop__I__sci_Stream(n)
  };
  take__I__O(n) {
    return this.take__I__sci_Stream(n)
  };
}
function $as_sci_Stream(obj) {
  return (((obj instanceof $c_sci_Stream) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.immutable.Stream"))
}
function $isArrayOf_sci_Stream(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sci_Stream)))
}
function $asArrayOf_sci_Stream(obj, depth) {
  return (($isArrayOf_sci_Stream(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.immutable.Stream;", depth))
}
class $c_sci_List extends $c_sc_AbstractSeq {
  length__I() {
    return $f_sc_LinearSeqOptimized__length__I(this)
  };
  sameElements__sc_GenIterable__Z(that) {
    return $f_sc_LinearSeqOptimized__sameElements__sc_GenIterable__Z(this, that)
  };
  lengthCompare__I__I(len) {
    return $f_sc_LinearSeqOptimized__lengthCompare__I__I(this, len)
  };
  hashCode__I() {
    return $m_s_util_hashing_MurmurHash3$().seqHash__sc_Seq__I(this)
  };
  iterator__sc_Iterator() {
    return new $c_sc_LinearSeqLike$$anon$1(this)
  };
  companion__scg_GenericCompanion() {
    return $m_sci_List$()
  };
  $colon$colon$colon__sci_List__sci_List(prefix) {
    return (this.isEmpty__Z() ? prefix : (prefix.isEmpty__Z() ? this : new $c_scm_ListBuffer().$plus$plus$eq__sc_TraversableOnce__scm_ListBuffer(prefix).prependToList__sci_List__sci_List(this)))
  };
  $plus$plus__sc_GenTraversableOnce__scg_CanBuildFrom__O(that, bf) {
    return ((bf === $m_sci_List$().scg_GenTraversableFactory__f_ReusableCBFInstance) ? that.seq__sc_TraversableOnce().toList__sci_List().$colon$colon$colon__sci_List__sci_List(this) : $f_sc_TraversableLike__$plus$plus__sc_GenTraversableOnce__scg_CanBuildFrom__O(this, that, bf))
  };
  toList__sci_List() {
    return this
  };
  take__I__sci_List(n) {
    if ((this.isEmpty__Z() || (n <= 0))) {
      return $m_sci_Nil$()
    } else {
      const h = new $c_sci_$colon$colon(this.head__O(), $m_sci_Nil$());
      let t = h;
      let rest = this.tail__sci_List();
      let i = 1;
      while (true) {
        if (rest.isEmpty__Z()) {
          return this
        };
        if ((i < n)) {
          i = ((1 + i) | 0);
          const nx = new $c_sci_$colon$colon(rest.head__O(), $m_sci_Nil$());
          t.sci_$colon$colon__f_tl = nx;
          t = nx;
          const this$1 = rest;
          rest = this$1.tail__sci_List()
        } else {
          break
        }
      };
      return h
    }
  };
  drop__I__sci_List(n) {
    let these = this;
    let count = n;
    while (((!these.isEmpty__Z()) && (count > 0))) {
      const this$1 = these;
      these = this$1.tail__sci_List();
      count = (((-1) + count) | 0)
    };
    return these
  };
  foreach__F1__V(f) {
    let these = this;
    while ((!these.isEmpty__Z())) {
      f.apply__O__O(these.head__O());
      const this$1 = these;
      these = this$1.tail__sci_List()
    }
  };
  reverse__sci_List() {
    let result = $m_sci_Nil$();
    let these = this;
    while ((!these.isEmpty__Z())) {
      const x$4 = these.head__O();
      const this$1 = result;
      result = new $c_sci_$colon$colon(x$4, this$1);
      const this$2 = these;
      these = this$2.tail__sci_List()
    };
    return result
  };
  stringPrefix__T() {
    return "List"
  };
  toStream__sci_Stream() {
    return (this.isEmpty__Z() ? $m_sci_Stream$Empty$() : new $c_sci_Stream$Cons(this.head__O(), new $c_sjsr_AnonFunction0(((this$1) => (() => this$1.tail__sci_List().toStream__sci_Stream()))(this))))
  };
  thisCollection__sc_Traversable() {
    return this
  };
  seq__sc_TraversableOnce() {
    return this
  };
  seq__sc_Seq() {
    return this
  };
  apply__O__O(v1) {
    const n = $uI(v1);
    return $f_sc_LinearSeqOptimized__apply__I__O(this, n)
  };
  drop__I__sc_LinearSeqOptimized(n) {
    return this.drop__I__sci_List(n)
  };
  take__I__O(n) {
    return this.take__I__sci_List(n)
  };
}
function $as_sci_List(obj) {
  return (((obj instanceof $c_sci_List) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.immutable.List"))
}
function $isArrayOf_sci_List(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sci_List)))
}
function $asArrayOf_sci_List(obj, depth) {
  return (($isArrayOf_sci_List(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.immutable.List;", depth))
}
const $p_sci_Stream$Cons__consEq$1__sci_Stream$Cons__sci_Stream$Cons__Z = (function($thiz, a, b) {
  while (true) {
    if ($m_sr_BoxesRunTime$().equals__O__O__Z(a.sci_Stream$Cons__f_hd, b.sci_Stream$Cons__f_hd)) {
      const x1 = a.tail__sci_Stream();
      if ((x1 instanceof $c_sci_Stream$Cons)) {
        const x2 = $as_sci_Stream$Cons(x1);
        const x1$2 = b.tail__sci_Stream();
        if ((x1$2 instanceof $c_sci_Stream$Cons)) {
          const x2$2 = $as_sci_Stream$Cons(x1$2);
          if ((x2 === x2$2)) {
            return true
          } else {
            a = x2;
            b = x2$2
          }
        } else {
          return false
        }
      } else {
        return b.tail__sci_Stream().isEmpty__Z()
      }
    } else {
      return false
    }
  }
});
class $c_sci_Stream$Cons extends $c_sci_Stream {
  constructor(hd, tl) {
    super();
    this.sci_Stream$Cons__f_hd = null;
    this.sci_Stream$Cons__f_tlVal = null;
    this.sci_Stream$Cons__f_tlGen = null;
    this.sci_Stream$Cons__f_hd = hd;
    this.sci_Stream$Cons__f_tlGen = tl
  };
  isEmpty__Z() {
    return false
  };
  head__O() {
    return this.sci_Stream$Cons__f_hd
  };
  tailDefined__Z() {
    return (this.sci_Stream$Cons__f_tlGen === null)
  };
  tail__sci_Stream() {
    if ((!this.tailDefined__Z())) {
      if ((!this.tailDefined__Z())) {
        this.sci_Stream$Cons__f_tlVal = $as_sci_Stream(this.sci_Stream$Cons__f_tlGen.apply__O());
        this.sci_Stream$Cons__f_tlGen = null
      }
    };
    return this.sci_Stream$Cons__f_tlVal
  };
  sameElements__sc_GenIterable__Z(that) {
    if ((that instanceof $c_sci_Stream$Cons)) {
      const x2 = $as_sci_Stream$Cons(that);
      return $p_sci_Stream$Cons__consEq$1__sci_Stream$Cons__sci_Stream$Cons__Z(this, this, x2)
    } else {
      return $f_sc_LinearSeqOptimized__sameElements__sc_GenIterable__Z(this, that)
    }
  };
  tail__O() {
    return this.tail__sci_Stream()
  };
}
function $as_sci_Stream$Cons(obj) {
  return (((obj instanceof $c_sci_Stream$Cons) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.immutable.Stream$Cons"))
}
function $isArrayOf_sci_Stream$Cons(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sci_Stream$Cons)))
}
function $asArrayOf_sci_Stream$Cons(obj, depth) {
  return (($isArrayOf_sci_Stream$Cons(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.immutable.Stream$Cons;", depth))
}
const $d_sci_Stream$Cons = new $TypeData().initClass({
  sci_Stream$Cons: 0
}, false, "scala.collection.immutable.Stream$Cons", {
  sci_Stream$Cons: 1,
  sci_Stream: 1,
  sc_AbstractSeq: 1,
  sc_AbstractIterable: 1,
  sc_AbstractTraversable: 1,
  O: 1,
  sc_Traversable: 1,
  sc_TraversableLike: 1,
  scg_HasNewBuilder: 1,
  scg_FilterMonadic: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1,
  sc_GenTraversableLike: 1,
  sc_Parallelizable: 1,
  sc_GenTraversable: 1,
  scg_GenericTraversableTemplate: 1,
  sc_Iterable: 1,
  sc_GenIterable: 1,
  sc_GenIterableLike: 1,
  sc_IterableLike: 1,
  s_Equals: 1,
  sc_Seq: 1,
  s_PartialFunction: 1,
  F1: 1,
  sc_GenSeq: 1,
  sc_GenSeqLike: 1,
  sc_SeqLike: 1,
  sci_LinearSeq: 1,
  sci_Seq: 1,
  sci_Iterable: 1,
  sci_Traversable: 1,
  s_Immutable: 1,
  sc_LinearSeq: 1,
  sc_LinearSeqLike: 1,
  sc_LinearSeqOptimized: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_sci_Stream$Cons.prototype.$classData = $d_sci_Stream$Cons;
class $c_sci_Stream$Empty$ extends $c_sci_Stream {
  isEmpty__Z() {
    return true
  };
  head__E() {
    throw $ct_ju_NoSuchElementException__T__(new $c_ju_NoSuchElementException(), "head of empty stream")
  };
  tail__E() {
    throw new $c_jl_UnsupportedOperationException("tail of empty stream")
  };
  tailDefined__Z() {
    return false
  };
  tail__O() {
    this.tail__E()
  };
  head__O() {
    this.head__E()
  };
}
const $d_sci_Stream$Empty$ = new $TypeData().initClass({
  sci_Stream$Empty$: 0
}, false, "scala.collection.immutable.Stream$Empty$", {
  sci_Stream$Empty$: 1,
  sci_Stream: 1,
  sc_AbstractSeq: 1,
  sc_AbstractIterable: 1,
  sc_AbstractTraversable: 1,
  O: 1,
  sc_Traversable: 1,
  sc_TraversableLike: 1,
  scg_HasNewBuilder: 1,
  scg_FilterMonadic: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1,
  sc_GenTraversableLike: 1,
  sc_Parallelizable: 1,
  sc_GenTraversable: 1,
  scg_GenericTraversableTemplate: 1,
  sc_Iterable: 1,
  sc_GenIterable: 1,
  sc_GenIterableLike: 1,
  sc_IterableLike: 1,
  s_Equals: 1,
  sc_Seq: 1,
  s_PartialFunction: 1,
  F1: 1,
  sc_GenSeq: 1,
  sc_GenSeqLike: 1,
  sc_SeqLike: 1,
  sci_LinearSeq: 1,
  sci_Seq: 1,
  sci_Iterable: 1,
  sci_Traversable: 1,
  s_Immutable: 1,
  sc_LinearSeq: 1,
  sc_LinearSeqLike: 1,
  sc_LinearSeqOptimized: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_sci_Stream$Empty$.prototype.$classData = $d_sci_Stream$Empty$;
let $n_sci_Stream$Empty$ = (void 0);
function $m_sci_Stream$Empty$() {
  if ((!$n_sci_Stream$Empty$)) {
    $n_sci_Stream$Empty$ = new $c_sci_Stream$Empty$()
  };
  return $n_sci_Stream$Empty$
}
const $p_sci_Vector__checkRangeConvert__I__I = (function($thiz, index) {
  const idx = ((index + $thiz.sci_Vector__f_startIndex) | 0);
  if (((index >= 0) && (idx < $thiz.sci_Vector__f_endIndex))) {
    return idx
  } else {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + index))
  }
});
class $c_sci_Vector extends $c_sc_AbstractSeq {
  constructor(startIndex, endIndex, focus) {
    super();
    this.sci_Vector__f_startIndex = 0;
    this.sci_Vector__f_endIndex = 0;
    this.sci_Vector__f_focus = 0;
    this.sci_Vector__f_dirty = false;
    this.sci_Vector__f_depth = 0;
    this.sci_Vector__f_display0 = null;
    this.sci_Vector__f_display1 = null;
    this.sci_Vector__f_display2 = null;
    this.sci_Vector__f_display3 = null;
    this.sci_Vector__f_display4 = null;
    this.sci_Vector__f_display5 = null;
    this.sci_Vector__f_startIndex = startIndex;
    this.sci_Vector__f_endIndex = endIndex;
    this.sci_Vector__f_focus = focus;
    this.sci_Vector__f_dirty = false
  };
  hashCode__I() {
    return $m_s_util_hashing_MurmurHash3$().seqHash__sc_Seq__I(this)
  };
  sizeHintIfCheap__I() {
    return this.length__I()
  };
  depth__I() {
    return this.sci_Vector__f_depth
  };
  depth_$eq__I__V(x$1) {
    this.sci_Vector__f_depth = x$1
  };
  display0__AO() {
    return this.sci_Vector__f_display0
  };
  display0_$eq__AO__V(x$1) {
    this.sci_Vector__f_display0 = x$1
  };
  display1__AO() {
    return this.sci_Vector__f_display1
  };
  display1_$eq__AO__V(x$1) {
    this.sci_Vector__f_display1 = x$1
  };
  display2__AO() {
    return this.sci_Vector__f_display2
  };
  display2_$eq__AO__V(x$1) {
    this.sci_Vector__f_display2 = x$1
  };
  display3__AO() {
    return this.sci_Vector__f_display3
  };
  display3_$eq__AO__V(x$1) {
    this.sci_Vector__f_display3 = x$1
  };
  display4__AO() {
    return this.sci_Vector__f_display4
  };
  display4_$eq__AO__V(x$1) {
    this.sci_Vector__f_display4 = x$1
  };
  display5__AO() {
    return this.sci_Vector__f_display5
  };
  display5_$eq__AO__V(x$1) {
    this.sci_Vector__f_display5 = x$1
  };
  companion__scg_GenericCompanion() {
    return $m_sci_Vector$()
  };
  length__I() {
    return ((this.sci_Vector__f_endIndex - this.sci_Vector__f_startIndex) | 0)
  };
  lengthCompare__I__I(len) {
    return ((this.length__I() - len) | 0)
  };
  initIterator__sci_VectorIterator__V(s) {
    const depth = this.sci_Vector__f_depth;
    $f_sci_VectorPointer__initFrom__sci_VectorPointer__I__V(s, this, depth);
    if (this.sci_Vector__f_dirty) {
      const index = this.sci_Vector__f_focus;
      $f_sci_VectorPointer__stabilize__I__V(s, index)
    };
    if ((s.sci_VectorIterator__f_depth > 1)) {
      const index$1 = this.sci_Vector__f_startIndex;
      const xor = (this.sci_Vector__f_startIndex ^ this.sci_Vector__f_focus);
      $f_sci_VectorPointer__gotoPos__I__I__V(s, index$1, xor)
    }
  };
  iterator__sci_VectorIterator() {
    const s = new $c_sci_VectorIterator(this.sci_Vector__f_startIndex, this.sci_Vector__f_endIndex);
    this.initIterator__sci_VectorIterator__V(s);
    return s
  };
  apply__I__O(index) {
    const idx = $p_sci_Vector__checkRangeConvert__I__I(this, index);
    const xor = (idx ^ this.sci_Vector__f_focus);
    return $f_sci_VectorPointer__getElem__I__I__O(this, idx, xor)
  };
  thisCollection__sc_Traversable() {
    return this
  };
  seq__sc_TraversableOnce() {
    return this
  };
  seq__sc_Seq() {
    return this
  };
  apply__O__O(v1) {
    return this.apply__I__O($uI(v1))
  };
  iterator__sc_Iterator() {
    return this.iterator__sci_VectorIterator()
  };
}
function $as_sci_Vector(obj) {
  return (((obj instanceof $c_sci_Vector) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.immutable.Vector"))
}
function $isArrayOf_sci_Vector(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.sci_Vector)))
}
function $asArrayOf_sci_Vector(obj, depth) {
  return (($isArrayOf_sci_Vector(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.immutable.Vector;", depth))
}
const $d_sci_Vector = new $TypeData().initClass({
  sci_Vector: 0
}, false, "scala.collection.immutable.Vector", {
  sci_Vector: 1,
  sc_AbstractSeq: 1,
  sc_AbstractIterable: 1,
  sc_AbstractTraversable: 1,
  O: 1,
  sc_Traversable: 1,
  sc_TraversableLike: 1,
  scg_HasNewBuilder: 1,
  scg_FilterMonadic: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1,
  sc_GenTraversableLike: 1,
  sc_Parallelizable: 1,
  sc_GenTraversable: 1,
  scg_GenericTraversableTemplate: 1,
  sc_Iterable: 1,
  sc_GenIterable: 1,
  sc_GenIterableLike: 1,
  sc_IterableLike: 1,
  s_Equals: 1,
  sc_Seq: 1,
  s_PartialFunction: 1,
  F1: 1,
  sc_GenSeq: 1,
  sc_GenSeqLike: 1,
  sc_SeqLike: 1,
  sci_IndexedSeq: 1,
  sci_Seq: 1,
  sci_Iterable: 1,
  sci_Traversable: 1,
  s_Immutable: 1,
  sc_IndexedSeq: 1,
  sc_IndexedSeqLike: 1,
  sci_VectorPointer: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1,
  sc_CustomParallelizable: 1
});
$c_sci_Vector.prototype.$classData = $d_sci_Vector;
class $c_sci_WrappedString extends $c_sc_AbstractSeq {
  constructor(self) {
    super();
    this.sci_WrappedString__f_self = null;
    this.sci_WrappedString__f_self = self
  };
  isEmpty__Z() {
    return $f_sc_IndexedSeqOptimized__isEmpty__Z(this)
  };
  foreach__F1__V(f) {
    $f_sc_IndexedSeqOptimized__foreach__F1__V(this, f)
  };
  sameElements__sc_GenIterable__Z(that) {
    return $f_sc_IndexedSeqOptimized__sameElements__sc_GenIterable__Z(this, that)
  };
  copyToArray__O__I__I__V(xs, start, len) {
    $f_sc_IndexedSeqOptimized__copyToArray__O__I__I__V(this, xs, start, len)
  };
  lengthCompare__I__I(len) {
    return $f_sc_IndexedSeqOptimized__lengthCompare__I__I(this, len)
  };
  toList__sci_List() {
    return $f_sc_IndexedSeqOptimized__toList__sci_List(this)
  };
  companion__scg_GenericCompanion() {
    return $m_sci_IndexedSeq$()
  };
  hashCode__I() {
    return $m_s_util_hashing_MurmurHash3$().seqHash__sc_Seq__I(this)
  };
  iterator__sc_Iterator() {
    const this$1 = this.sci_WrappedString__f_self;
    return new $c_sc_IndexedSeqLike$Elements(this, 0, $uI(this$1.length))
  };
  sizeHintIfCheap__I() {
    const this$1 = this.sci_WrappedString__f_self;
    return $uI(this$1.length)
  };
  newBuilder__scm_Builder() {
    return $m_sci_WrappedString$().newBuilder__scm_Builder()
  };
  length__I() {
    const this$1 = this.sci_WrappedString__f_self;
    return $uI(this$1.length)
  };
  toString__T() {
    return this.sci_WrappedString__f_self
  };
  seq__sc_TraversableOnce() {
    return this
  };
  seq__sc_Seq() {
    return this
  };
  compare__O__I(that) {
    const other = $as_T(that);
    const this$1 = this.sci_WrappedString__f_self;
    return ((this$1 === other) ? 0 : ($uZ((this$1 < other)) ? (-1) : 1))
  };
  apply__O__O(v1) {
    const n = $uI(v1);
    const this$1 = this.sci_WrappedString__f_self;
    return $bC((65535 & $uI(this$1.charCodeAt(n))))
  };
  apply__I__O(idx) {
    const this$1 = this.sci_WrappedString__f_self;
    return $bC((65535 & $uI(this$1.charCodeAt(idx))))
  };
  thisCollection__sc_Traversable() {
    return this
  };
}
const $d_sci_WrappedString = new $TypeData().initClass({
  sci_WrappedString: 0
}, false, "scala.collection.immutable.WrappedString", {
  sci_WrappedString: 1,
  sc_AbstractSeq: 1,
  sc_AbstractIterable: 1,
  sc_AbstractTraversable: 1,
  O: 1,
  sc_Traversable: 1,
  sc_TraversableLike: 1,
  scg_HasNewBuilder: 1,
  scg_FilterMonadic: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1,
  sc_GenTraversableLike: 1,
  sc_Parallelizable: 1,
  sc_GenTraversable: 1,
  scg_GenericTraversableTemplate: 1,
  sc_Iterable: 1,
  sc_GenIterable: 1,
  sc_GenIterableLike: 1,
  sc_IterableLike: 1,
  s_Equals: 1,
  sc_Seq: 1,
  s_PartialFunction: 1,
  F1: 1,
  sc_GenSeq: 1,
  sc_GenSeqLike: 1,
  sc_SeqLike: 1,
  sci_IndexedSeq: 1,
  sci_Seq: 1,
  sci_Iterable: 1,
  sci_Traversable: 1,
  s_Immutable: 1,
  sc_IndexedSeq: 1,
  sc_IndexedSeqLike: 1,
  sci_StringLike: 1,
  sc_IndexedSeqOptimized: 1,
  s_math_Ordered: 1,
  jl_Comparable: 1
});
$c_sci_WrappedString.prototype.$classData = $d_sci_WrappedString;
const $f_scm_ResizableArray__apply__I__O = (function($thiz, idx) {
  if ((idx >= $thiz.scm_ArrayBuffer__f_size0)) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + idx))
  };
  return $thiz.scm_ArrayBuffer__f_array.get(idx)
});
const $f_scm_ResizableArray__foreach__F1__V = (function($thiz, f) {
  let i = 0;
  const top = $thiz.scm_ArrayBuffer__f_size0;
  while ((i < top)) {
    f.apply__O__O($thiz.scm_ArrayBuffer__f_array.get(i));
    i = ((1 + i) | 0)
  }
});
const $f_scm_ResizableArray__copyToArray__O__I__I__V = (function($thiz, xs, start, len) {
  const that = (($m_sr_ScalaRunTime$().array_length__O__I(xs) - start) | 0);
  const x = ((len < that) ? len : that);
  const that$1 = $thiz.scm_ArrayBuffer__f_size0;
  const len1 = ((x < that$1) ? x : that$1);
  if ((len1 > 0)) {
    $m_s_Array$().copy__O__I__O__I__I__V($thiz.scm_ArrayBuffer__f_array, 0, xs, start, len1)
  }
});
const $f_scm_ResizableArray__ensureSize__I__V = (function($thiz, n) {
  const value = $thiz.scm_ArrayBuffer__f_array.u.length;
  const hi = (value >> 31);
  const hi$1 = (n >> 31);
  if (((hi$1 === hi) ? (((-2147483648) ^ n) > ((-2147483648) ^ value)) : (hi$1 > hi))) {
    const lo = (value << 1);
    const hi$2 = (((value >>> 31) | 0) | (hi << 1));
    let newSize__lo = lo;
    let newSize__hi = hi$2;
    while (true) {
      const hi$3 = (n >> 31);
      const b__lo = newSize__lo;
      const b__hi = newSize__hi;
      const bhi = b__hi;
      if (((hi$3 === bhi) ? (((-2147483648) ^ n) > ((-2147483648) ^ b__lo)) : (hi$3 > bhi))) {
        const this$4__lo = newSize__lo;
        const this$4__hi = newSize__hi;
        const lo$1 = (this$4__lo << 1);
        const hi$4 = (((this$4__lo >>> 31) | 0) | (this$4__hi << 1));
        const $$x1__lo = lo$1;
        const $$x1__hi = hi$4;
        newSize__lo = $$x1__lo;
        newSize__hi = $$x1__hi
      } else {
        break
      }
    };
    const this$5__lo = newSize__lo;
    const this$5__hi = newSize__hi;
    const ahi = this$5__hi;
    if (((ahi === 0) ? (((-2147483648) ^ this$5__lo) > (-1)) : (ahi > 0))) {
      const $$x2__lo = 2147483647;
      const $$x2__hi = 0;
      newSize__lo = $$x2__lo;
      newSize__hi = $$x2__hi
    };
    const this$6__lo = newSize__lo;
    const this$6__hi = newSize__hi;
    const newArray = $newArrayObject($d_O.getArrayOf(), [this$6__lo]);
    const src = $thiz.scm_ArrayBuffer__f_array;
    const length = $thiz.scm_ArrayBuffer__f_size0;
    $systemArraycopy(src, 0, newArray, 0, length);
    $thiz.scm_ArrayBuffer__f_array = newArray
  }
});
const $f_scm_ResizableArray__$init$__V = (function($thiz) {
  const x = $thiz.scm_ArrayBuffer__f_initialSize;
  $thiz.scm_ArrayBuffer__f_array = $newArrayObject($d_O.getArrayOf(), [((x > 1) ? x : 1)]);
  $thiz.scm_ArrayBuffer__f_size0 = 0
});
class $c_sci_$colon$colon extends $c_sci_List {
  constructor(head, tl) {
    super();
    this.sci_$colon$colon__f_head = null;
    this.sci_$colon$colon__f_tl = null;
    this.sci_$colon$colon__f_head = head;
    this.sci_$colon$colon__f_tl = tl
  };
  head__O() {
    return this.sci_$colon$colon__f_head
  };
  tail__sci_List() {
    return this.sci_$colon$colon__f_tl
  };
  isEmpty__Z() {
    return false
  };
  productPrefix__T() {
    return "::"
  };
  productArity__I() {
    return 2
  };
  productElement__I__O(x$1) {
    switch (x$1) {
      case 0: {
        return this.sci_$colon$colon__f_head;
        break
      }
      case 1: {
        return this.sci_$colon$colon__f_tl;
        break
      }
      default: {
        throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
      }
    }
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  tail__O() {
    return this.sci_$colon$colon__f_tl
  };
}
const $d_sci_$colon$colon = new $TypeData().initClass({
  sci_$colon$colon: 0
}, false, "scala.collection.immutable.$colon$colon", {
  sci_$colon$colon: 1,
  sci_List: 1,
  sc_AbstractSeq: 1,
  sc_AbstractIterable: 1,
  sc_AbstractTraversable: 1,
  O: 1,
  sc_Traversable: 1,
  sc_TraversableLike: 1,
  scg_HasNewBuilder: 1,
  scg_FilterMonadic: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1,
  sc_GenTraversableLike: 1,
  sc_Parallelizable: 1,
  sc_GenTraversable: 1,
  scg_GenericTraversableTemplate: 1,
  sc_Iterable: 1,
  sc_GenIterable: 1,
  sc_GenIterableLike: 1,
  sc_IterableLike: 1,
  s_Equals: 1,
  sc_Seq: 1,
  s_PartialFunction: 1,
  F1: 1,
  sc_GenSeq: 1,
  sc_GenSeqLike: 1,
  sc_SeqLike: 1,
  sci_LinearSeq: 1,
  sci_Seq: 1,
  sci_Iterable: 1,
  sci_Traversable: 1,
  s_Immutable: 1,
  sc_LinearSeq: 1,
  sc_LinearSeqLike: 1,
  s_Product: 1,
  sc_LinearSeqOptimized: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_sci_$colon$colon.prototype.$classData = $d_sci_$colon$colon;
class $c_sci_Nil$ extends $c_sci_List {
  isEmpty__Z() {
    return true
  };
  head__E() {
    throw $ct_ju_NoSuchElementException__T__(new $c_ju_NoSuchElementException(), "head of empty list")
  };
  tail__sci_List() {
    throw new $c_jl_UnsupportedOperationException("tail of empty list")
  };
  equals__O__Z(that) {
    if ($is_sc_GenSeq(that)) {
      const x2 = $as_sc_GenSeq(that);
      return x2.isEmpty__Z()
    } else {
      return false
    }
  };
  productPrefix__T() {
    return "Nil"
  };
  productArity__I() {
    return 0
  };
  productElement__I__O(x$1) {
    throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + x$1))
  };
  productIterator__sc_Iterator() {
    return new $c_sr_ScalaRunTime$$anon$1(this)
  };
  tail__O() {
    return this.tail__sci_List()
  };
  head__O() {
    this.head__E()
  };
}
const $d_sci_Nil$ = new $TypeData().initClass({
  sci_Nil$: 0
}, false, "scala.collection.immutable.Nil$", {
  sci_Nil$: 1,
  sci_List: 1,
  sc_AbstractSeq: 1,
  sc_AbstractIterable: 1,
  sc_AbstractTraversable: 1,
  O: 1,
  sc_Traversable: 1,
  sc_TraversableLike: 1,
  scg_HasNewBuilder: 1,
  scg_FilterMonadic: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1,
  sc_GenTraversableLike: 1,
  sc_Parallelizable: 1,
  sc_GenTraversable: 1,
  scg_GenericTraversableTemplate: 1,
  sc_Iterable: 1,
  sc_GenIterable: 1,
  sc_GenIterableLike: 1,
  sc_IterableLike: 1,
  s_Equals: 1,
  sc_Seq: 1,
  s_PartialFunction: 1,
  F1: 1,
  sc_GenSeq: 1,
  sc_GenSeqLike: 1,
  sc_SeqLike: 1,
  sci_LinearSeq: 1,
  sci_Seq: 1,
  sci_Iterable: 1,
  sci_Traversable: 1,
  s_Immutable: 1,
  sc_LinearSeq: 1,
  sc_LinearSeqLike: 1,
  s_Product: 1,
  sc_LinearSeqOptimized: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_sci_Nil$.prototype.$classData = $d_sci_Nil$;
let $n_sci_Nil$ = (void 0);
function $m_sci_Nil$() {
  if ((!$n_sci_Nil$)) {
    $n_sci_Nil$ = new $c_sci_Nil$()
  };
  return $n_sci_Nil$
}
const $ct_scm_LinkedList__ = (function($thiz) {
  $thiz.scm_LinkedList__f_next = $thiz;
  return $thiz
});
const $ct_scm_LinkedList__O__scm_LinkedList__ = (function($thiz, elem, next) {
  $ct_scm_LinkedList__($thiz);
  if ((next !== null)) {
    $thiz.scm_LinkedList__f_elem = elem;
    $thiz.scm_LinkedList__f_next = next
  };
  return $thiz
});
class $c_scm_LinkedList extends $c_scm_AbstractSeq {
  constructor() {
    super();
    this.scm_LinkedList__f_elem = null;
    this.scm_LinkedList__f_next = null
  };
  isEmpty__Z() {
    return $f_scm_LinkedListLike__isEmpty__Z(this)
  };
  length__I() {
    return $p_scm_LinkedListLike__length0__scm_Seq__I__I(this, this, 0)
  };
  head__O() {
    return $f_scm_LinkedListLike__head__O(this)
  };
  iterator__sc_Iterator() {
    return new $c_scm_LinkedListLike$$anon$1(this)
  };
  foreach__F1__V(f) {
    $f_scm_LinkedListLike__foreach__F1__V(this, f)
  };
  hashCode__I() {
    return $m_s_util_hashing_MurmurHash3$().seqHash__sc_Seq__I(this)
  };
  companion__scg_GenericCompanion() {
    return $m_scm_LinkedList$()
  };
  thisCollection__sc_Traversable() {
    return this
  };
  seq__sc_TraversableOnce() {
    return this
  };
  seq__sc_Seq() {
    return this
  };
  seq__scm_Seq() {
    return this
  };
  apply__O__O(v1) {
    const n = $uI(v1);
    return $f_scm_LinkedListLike__apply__I__O(this, n)
  };
  tail__O() {
    return $f_scm_LinkedListLike__tail__scm_Seq(this)
  };
}
function $as_scm_LinkedList(obj) {
  return (((obj instanceof $c_scm_LinkedList) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.mutable.LinkedList"))
}
function $isArrayOf_scm_LinkedList(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.scm_LinkedList)))
}
function $asArrayOf_scm_LinkedList(obj, depth) {
  return (($isArrayOf_scm_LinkedList(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.mutable.LinkedList;", depth))
}
const $d_scm_LinkedList = new $TypeData().initClass({
  scm_LinkedList: 0
}, false, "scala.collection.mutable.LinkedList", {
  scm_LinkedList: 1,
  scm_AbstractSeq: 1,
  sc_AbstractSeq: 1,
  sc_AbstractIterable: 1,
  sc_AbstractTraversable: 1,
  O: 1,
  sc_Traversable: 1,
  sc_TraversableLike: 1,
  scg_HasNewBuilder: 1,
  scg_FilterMonadic: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1,
  sc_GenTraversableLike: 1,
  sc_Parallelizable: 1,
  sc_GenTraversable: 1,
  scg_GenericTraversableTemplate: 1,
  sc_Iterable: 1,
  sc_GenIterable: 1,
  sc_GenIterableLike: 1,
  sc_IterableLike: 1,
  s_Equals: 1,
  sc_Seq: 1,
  s_PartialFunction: 1,
  F1: 1,
  sc_GenSeq: 1,
  sc_GenSeqLike: 1,
  sc_SeqLike: 1,
  scm_Seq: 1,
  scm_Iterable: 1,
  scm_Traversable: 1,
  s_Mutable: 1,
  scm_SeqLike: 1,
  scm_Cloneable: 1,
  s_Cloneable: 1,
  jl_Cloneable: 1,
  scm_LinearSeq: 1,
  sc_LinearSeq: 1,
  sc_LinearSeqLike: 1,
  scm_LinkedListLike: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_scm_LinkedList.prototype.$classData = $d_scm_LinkedList;
class $c_scm_AbstractBuffer extends $c_scm_AbstractSeq {
}
const $ct_scm_MutableList__ = (function($thiz) {
  $thiz.scm_MutableList__f_first0 = $ct_scm_LinkedList__(new $c_scm_LinkedList());
  $thiz.scm_MutableList__f_last0 = $thiz.scm_MutableList__f_first0;
  $thiz.scm_MutableList__f_len = 0;
  return $thiz
});
class $c_scm_MutableList extends $c_scm_AbstractSeq {
  constructor() {
    super();
    this.scm_MutableList__f_first0 = null;
    this.scm_MutableList__f_last0 = null;
    this.scm_MutableList__f_len = 0
  };
  sizeHint__I__V(size) {
    /*<skip>*/
  };
  sizeHintBounded__I__sc_TraversableLike__V(size, boundingColl) {
    $f_scm_Builder__sizeHintBounded__I__sc_TraversableLike__V(this, size, boundingColl)
  };
  $plus$plus$eq__sc_TraversableOnce__scg_Growable(xs) {
    return $f_scg_Growable__$plus$plus$eq__sc_TraversableOnce__scg_Growable(this, xs)
  };
  foreach__F1__V(f) {
    $f_sc_LinearSeqOptimized__foreach__F1__V(this, f)
  };
  drop__I__sc_LinearSeqOptimized(n) {
    return $f_sc_LinearSeqOptimized__drop__I__sc_LinearSeqOptimized(this, n)
  };
  sameElements__sc_GenIterable__Z(that) {
    return $f_sc_LinearSeqOptimized__sameElements__sc_GenIterable__Z(this, that)
  };
  lengthCompare__I__I(len) {
    return $f_sc_LinearSeqOptimized__lengthCompare__I__I(this, len)
  };
  hashCode__I() {
    return $m_s_util_hashing_MurmurHash3$().seqHash__sc_Seq__I(this)
  };
  companion__scg_GenericCompanion() {
    return $m_scm_MutableList$()
  };
  newBuilder__scm_Builder() {
    return $ct_scm_MutableList__(new $c_scm_MutableList())
  };
  isEmpty__Z() {
    return (this.scm_MutableList__f_len === 0)
  };
  head__O() {
    if ((!this.isEmpty__Z())) {
      const this$1 = this.scm_MutableList__f_first0;
      return $f_scm_LinkedListLike__head__O(this$1)
    } else {
      throw $ct_ju_NoSuchElementException__(new $c_ju_NoSuchElementException())
    }
  };
  tail__scm_MutableList() {
    const tl = $ct_scm_MutableList__(new $c_scm_MutableList());
    this.tailImpl__scm_MutableList__V(tl);
    return tl
  };
  tailImpl__scm_MutableList__V(tl) {
    const requirement = (!this.isEmpty__Z());
    if ((!requirement)) {
      throw $ct_jl_IllegalArgumentException__T__(new $c_jl_IllegalArgumentException(), "requirement failed: tail of empty list")
    };
    const this$2 = this.scm_MutableList__f_first0;
    tl.scm_MutableList__f_first0 = $as_scm_LinkedList($f_scm_LinkedListLike__tail__scm_Seq(this$2));
    tl.scm_MutableList__f_len = (((-1) + this.scm_MutableList__f_len) | 0);
    tl.scm_MutableList__f_last0 = ((tl.scm_MutableList__f_len === 0) ? tl.scm_MutableList__f_first0 : this.scm_MutableList__f_last0)
  };
  length__I() {
    return this.scm_MutableList__f_len
  };
  prependElem__O__V(elem) {
    this.scm_MutableList__f_first0 = $ct_scm_LinkedList__O__scm_LinkedList__(new $c_scm_LinkedList(), elem, this.scm_MutableList__f_first0);
    if ((this.scm_MutableList__f_len === 0)) {
      this.scm_MutableList__f_last0 = this.scm_MutableList__f_first0
    };
    this.scm_MutableList__f_len = ((1 + this.scm_MutableList__f_len) | 0)
  };
  appendElem__O__V(elem) {
    if ((this.scm_MutableList__f_len === 0)) {
      this.prependElem__O__V(elem)
    } else {
      const this$1 = this.scm_MutableList__f_last0;
      const x$1 = $ct_scm_LinkedList__(new $c_scm_LinkedList());
      this$1.scm_LinkedList__f_next = x$1;
      const this$2 = this.scm_MutableList__f_last0;
      this.scm_MutableList__f_last0 = this$2.scm_LinkedList__f_next;
      this.scm_MutableList__f_last0.scm_LinkedList__f_elem = elem;
      const this$3 = this.scm_MutableList__f_last0;
      const x$1$1 = $ct_scm_LinkedList__(new $c_scm_LinkedList());
      this$3.scm_LinkedList__f_next = x$1$1;
      this.scm_MutableList__f_len = ((1 + this.scm_MutableList__f_len) | 0)
    }
  };
  iterator__sc_Iterator() {
    return (this.isEmpty__Z() ? $m_sc_Iterator$().sc_Iterator$__f_empty : new $c_scm_MutableList$$anon$1(this))
  };
  toList__sci_List() {
    const this$1 = this.scm_MutableList__f_first0;
    const this$2 = $m_sci_List$();
    const cbf = this$2.scg_GenTraversableFactory__f_ReusableCBFInstance;
    return $as_sci_List($f_sc_TraversableLike__to__scg_CanBuildFrom__O(this$1, cbf))
  };
  $plus$eq__O__scm_MutableList(elem) {
    this.appendElem__O__V(elem);
    return this
  };
  thisCollection__sc_Traversable() {
    return this
  };
  seq__sc_TraversableOnce() {
    return this
  };
  seq__sc_Seq() {
    return this
  };
  seq__scm_Seq() {
    return this
  };
  take__I__O(n) {
    return $f_sc_LinearSeqOptimized__take__I__sc_LinearSeqOptimized(this, n)
  };
  result__O() {
    return this
  };
  $plus$eq__O__scg_Growable(elem) {
    return this.$plus$eq__O__scm_MutableList(elem)
  };
  $plus$eq__O__scm_Builder(elem) {
    return this.$plus$eq__O__scm_MutableList(elem)
  };
  apply__O__O(v1) {
    const n = $uI(v1);
    const this$1 = this.scm_MutableList__f_first0;
    return $f_scm_LinkedListLike__apply__I__O(this$1, n)
  };
  tail__O() {
    return this.tail__scm_MutableList()
  };
}
function $as_scm_MutableList(obj) {
  return (((obj instanceof $c_scm_MutableList) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.mutable.MutableList"))
}
function $isArrayOf_scm_MutableList(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.scm_MutableList)))
}
function $asArrayOf_scm_MutableList(obj, depth) {
  return (($isArrayOf_scm_MutableList(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.mutable.MutableList;", depth))
}
const $d_scm_MutableList = new $TypeData().initClass({
  scm_MutableList: 0
}, false, "scala.collection.mutable.MutableList", {
  scm_MutableList: 1,
  scm_AbstractSeq: 1,
  sc_AbstractSeq: 1,
  sc_AbstractIterable: 1,
  sc_AbstractTraversable: 1,
  O: 1,
  sc_Traversable: 1,
  sc_TraversableLike: 1,
  scg_HasNewBuilder: 1,
  scg_FilterMonadic: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1,
  sc_GenTraversableLike: 1,
  sc_Parallelizable: 1,
  sc_GenTraversable: 1,
  scg_GenericTraversableTemplate: 1,
  sc_Iterable: 1,
  sc_GenIterable: 1,
  sc_GenIterableLike: 1,
  sc_IterableLike: 1,
  s_Equals: 1,
  sc_Seq: 1,
  s_PartialFunction: 1,
  F1: 1,
  sc_GenSeq: 1,
  sc_GenSeqLike: 1,
  sc_SeqLike: 1,
  scm_Seq: 1,
  scm_Iterable: 1,
  scm_Traversable: 1,
  s_Mutable: 1,
  scm_SeqLike: 1,
  scm_Cloneable: 1,
  s_Cloneable: 1,
  jl_Cloneable: 1,
  scm_LinearSeq: 1,
  sc_LinearSeq: 1,
  sc_LinearSeqLike: 1,
  sc_LinearSeqOptimized: 1,
  scm_Builder: 1,
  scg_Growable: 1,
  scg_Clearable: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_scm_MutableList.prototype.$classData = $d_scm_MutableList;
const $p_scm_Queue__decrementLength__V = (function($thiz) {
  $thiz.scm_MutableList__f_len = (((-1) + $thiz.scm_MutableList__f_len) | 0);
  if (($thiz.scm_MutableList__f_len === 0)) {
    $thiz.scm_MutableList__f_last0 = $thiz.scm_MutableList__f_first0
  }
});
const $ct_scm_Queue__ = (function($thiz) {
  $ct_scm_MutableList__($thiz);
  return $thiz
});
const $ct_scm_Queue__scm_LinkedList__scm_LinkedList__I__ = (function($thiz, fst, lst, lng) {
  $ct_scm_MutableList__($thiz);
  $thiz.scm_MutableList__f_first0 = fst;
  $thiz.scm_MutableList__f_last0 = lst;
  $thiz.scm_MutableList__f_len = lng;
  return $thiz
});
class $c_scm_Queue extends $c_scm_MutableList {
  companion__scg_GenericCompanion() {
    return $m_scm_Queue$()
  };
  newBuilder__scm_Builder() {
    return $m_scm_Queue$().newBuilder__scm_Builder()
  };
  dequeue__O() {
    if (this.isEmpty__Z()) {
      throw $ct_ju_NoSuchElementException__T__(new $c_ju_NoSuchElementException(), "queue empty")
    } else {
      const res = this.scm_MutableList__f_first0.scm_LinkedList__f_elem;
      const this$1 = this.scm_MutableList__f_first0;
      this.scm_MutableList__f_first0 = this$1.scm_LinkedList__f_next;
      $p_scm_Queue__decrementLength__V(this);
      return res
    }
  };
  tail__scm_Queue() {
    const tl = $ct_scm_Queue__(new $c_scm_Queue());
    this.tailImpl__scm_MutableList__V(tl);
    return tl
  };
  thisCollection__sc_Traversable() {
    return this
  };
  seq__sc_Seq() {
    return this
  };
  take__I__O(n) {
    return $f_sc_LinearSeqOptimized__take__I__sc_LinearSeqOptimized(this, n)
  };
  tail__O() {
    return this.tail__scm_Queue()
  };
  tail__scm_MutableList() {
    return this.tail__scm_Queue()
  };
}
const $d_scm_Queue = new $TypeData().initClass({
  scm_Queue: 0
}, false, "scala.collection.mutable.Queue", {
  scm_Queue: 1,
  scm_MutableList: 1,
  scm_AbstractSeq: 1,
  sc_AbstractSeq: 1,
  sc_AbstractIterable: 1,
  sc_AbstractTraversable: 1,
  O: 1,
  sc_Traversable: 1,
  sc_TraversableLike: 1,
  scg_HasNewBuilder: 1,
  scg_FilterMonadic: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1,
  sc_GenTraversableLike: 1,
  sc_Parallelizable: 1,
  sc_GenTraversable: 1,
  scg_GenericTraversableTemplate: 1,
  sc_Iterable: 1,
  sc_GenIterable: 1,
  sc_GenIterableLike: 1,
  sc_IterableLike: 1,
  s_Equals: 1,
  sc_Seq: 1,
  s_PartialFunction: 1,
  F1: 1,
  sc_GenSeq: 1,
  sc_GenSeqLike: 1,
  sc_SeqLike: 1,
  scm_Seq: 1,
  scm_Iterable: 1,
  scm_Traversable: 1,
  s_Mutable: 1,
  scm_SeqLike: 1,
  scm_Cloneable: 1,
  s_Cloneable: 1,
  jl_Cloneable: 1,
  scm_LinearSeq: 1,
  sc_LinearSeq: 1,
  sc_LinearSeqLike: 1,
  sc_LinearSeqOptimized: 1,
  scm_Builder: 1,
  scg_Growable: 1,
  scg_Clearable: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_scm_Queue.prototype.$classData = $d_scm_Queue;
const $p_scm_ListBuffer__ensureUnaliased__V = (function($thiz) {
  if ($thiz.scm_ListBuffer__f_exported) {
    $p_scm_ListBuffer__copy__V($thiz)
  }
});
const $p_scm_ListBuffer__copy__V = (function($thiz) {
  _return: {
    if ($thiz.isEmpty__Z()) {
      break _return
    };
    let cursor = $thiz.scm_ListBuffer__f_scala$collection$mutable$ListBuffer$$start;
    const this$1 = $thiz.scm_ListBuffer__f_last0;
    const limit = this$1.sci_$colon$colon__f_tl;
    $thiz.clear__V();
    while ((cursor !== limit)) {
      $thiz.$plus$eq__O__scm_ListBuffer(cursor.head__O());
      const this$2 = cursor;
      cursor = this$2.tail__sci_List()
    }
  }
});
class $c_scm_ListBuffer extends $c_scm_AbstractBuffer {
  constructor() {
    super();
    this.scm_ListBuffer__f_scala$collection$mutable$ListBuffer$$start = null;
    this.scm_ListBuffer__f_last0 = null;
    this.scm_ListBuffer__f_exported = false;
    this.scm_ListBuffer__f_len = 0;
    this.scm_ListBuffer__f_scala$collection$mutable$ListBuffer$$start = $m_sci_Nil$();
    this.scm_ListBuffer__f_exported = false;
    this.scm_ListBuffer__f_len = 0
  };
  lengthCompare__I__I(len) {
    const this$1 = this.scm_ListBuffer__f_scala$collection$mutable$ListBuffer$$start;
    return $f_sc_LinearSeqOptimized__lengthCompare__I__I(this$1, len)
  };
  sameElements__sc_GenIterable__Z(that) {
    const this$1 = this.scm_ListBuffer__f_scala$collection$mutable$ListBuffer$$start;
    return $f_sc_LinearSeqOptimized__sameElements__sc_GenIterable__Z(this$1, that)
  };
  foreach__F1__V(f) {
    const this$1 = this.scm_ListBuffer__f_scala$collection$mutable$ListBuffer$$start;
    let these = this$1;
    while ((!these.isEmpty__Z())) {
      f.apply__O__O(these.head__O());
      const this$2 = these;
      these = this$2.tail__sci_List()
    }
  };
  toStream__sci_Stream() {
    return this.scm_ListBuffer__f_scala$collection$mutable$ListBuffer$$start.toStream__sci_Stream()
  };
  mkString__T__T__T__T(start, sep, end) {
    const this$1 = this.scm_ListBuffer__f_scala$collection$mutable$ListBuffer$$start;
    return $f_sc_TraversableOnce__mkString__T__T__T__T(this$1, start, sep, end)
  };
  addString__scm_StringBuilder__T__T__T__scm_StringBuilder(b, start, sep, end) {
    const this$1 = this.scm_ListBuffer__f_scala$collection$mutable$ListBuffer$$start;
    return $f_sc_TraversableOnce__addString__scm_StringBuilder__T__T__T__scm_StringBuilder(this$1, b, start, sep, end)
  };
  sizeHint__I__V(size) {
    /*<skip>*/
  };
  sizeHintBounded__I__sc_TraversableLike__V(size, boundingColl) {
    $f_scm_Builder__sizeHintBounded__I__sc_TraversableLike__V(this, size, boundingColl)
  };
  companion__scg_GenericCompanion() {
    return $m_scm_ListBuffer$()
  };
  length__I() {
    return this.scm_ListBuffer__f_len
  };
  size__I() {
    return this.scm_ListBuffer__f_len
  };
  isEmpty__Z() {
    return (this.scm_ListBuffer__f_len === 0)
  };
  nonEmpty__Z() {
    return (this.scm_ListBuffer__f_len > 0)
  };
  apply__I__O(n) {
    if (((n < 0) || (n >= this.scm_ListBuffer__f_len))) {
      throw $ct_jl_IndexOutOfBoundsException__T__(new $c_jl_IndexOutOfBoundsException(), ("" + n))
    } else {
      const this$2 = this.scm_ListBuffer__f_scala$collection$mutable$ListBuffer$$start;
      return $f_sc_LinearSeqOptimized__apply__I__O(this$2, n)
    }
  };
  $plus$eq__O__scm_ListBuffer(x) {
    $p_scm_ListBuffer__ensureUnaliased__V(this);
    const last1 = new $c_sci_$colon$colon(x, $m_sci_Nil$());
    if ((this.scm_ListBuffer__f_len === 0)) {
      this.scm_ListBuffer__f_scala$collection$mutable$ListBuffer$$start = last1
    } else {
      this.scm_ListBuffer__f_last0.sci_$colon$colon__f_tl = last1
    };
    this.scm_ListBuffer__f_last0 = last1;
    this.scm_ListBuffer__f_len = ((1 + this.scm_ListBuffer__f_len) | 0);
    return this
  };
  $plus$plus$eq__sc_TraversableOnce__scm_ListBuffer(xs) {
    while (true) {
      const x1 = xs;
      if ((x1 !== null)) {
        if ((x1 === this)) {
          const n = this.scm_ListBuffer__f_len;
          xs = $as_sc_TraversableOnce($f_sc_IterableLike__take__I__O(this, n));
          continue
        }
      };
      return $as_scm_ListBuffer($f_scg_Growable__$plus$plus$eq__sc_TraversableOnce__scg_Growable(this, xs))
    }
  };
  clear__V() {
    this.scm_ListBuffer__f_scala$collection$mutable$ListBuffer$$start = $m_sci_Nil$();
    this.scm_ListBuffer__f_last0 = null;
    this.scm_ListBuffer__f_exported = false;
    this.scm_ListBuffer__f_len = 0
  };
  toList__sci_List() {
    this.scm_ListBuffer__f_exported = (!this.isEmpty__Z());
    return this.scm_ListBuffer__f_scala$collection$mutable$ListBuffer$$start
  };
  prependToList__sci_List__sci_List(xs) {
    if (this.isEmpty__Z()) {
      return xs
    } else {
      $p_scm_ListBuffer__ensureUnaliased__V(this);
      this.scm_ListBuffer__f_last0.sci_$colon$colon__f_tl = xs;
      return this.toList__sci_List()
    }
  };
  iterator__sc_Iterator() {
    return new $c_scm_ListBuffer$$anon$1(this)
  };
  equals__O__Z(that) {
    if ((that instanceof $c_scm_ListBuffer)) {
      const x2 = $as_scm_ListBuffer(that);
      return this.scm_ListBuffer__f_scala$collection$mutable$ListBuffer$$start.equals__O__Z(x2.scm_ListBuffer__f_scala$collection$mutable$ListBuffer$$start)
    } else {
      return $f_sc_GenSeqLike__equals__O__Z(this, that)
    }
  };
  stringPrefix__T() {
    return "ListBuffer"
  };
  thisCollection__sc_Traversable() {
    return this
  };
  seq__sc_Seq() {
    return this
  };
  result__O() {
    return this.toList__sci_List()
  };
  $plus$plus$eq__sc_TraversableOnce__scg_Growable(xs) {
    return this.$plus$plus$eq__sc_TraversableOnce__scm_ListBuffer(xs)
  };
  $plus$eq__O__scg_Growable(elem) {
    return this.$plus$eq__O__scm_ListBuffer(elem)
  };
  $plus$eq__O__scm_Builder(elem) {
    return this.$plus$eq__O__scm_ListBuffer(elem)
  };
  apply__O__O(v1) {
    return this.apply__I__O($uI(v1))
  };
}
function $as_scm_ListBuffer(obj) {
  return (((obj instanceof $c_scm_ListBuffer) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.mutable.ListBuffer"))
}
function $isArrayOf_scm_ListBuffer(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.scm_ListBuffer)))
}
function $asArrayOf_scm_ListBuffer(obj, depth) {
  return (($isArrayOf_scm_ListBuffer(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.mutable.ListBuffer;", depth))
}
const $d_scm_ListBuffer = new $TypeData().initClass({
  scm_ListBuffer: 0
}, false, "scala.collection.mutable.ListBuffer", {
  scm_ListBuffer: 1,
  scm_AbstractBuffer: 1,
  scm_AbstractSeq: 1,
  sc_AbstractSeq: 1,
  sc_AbstractIterable: 1,
  sc_AbstractTraversable: 1,
  O: 1,
  sc_Traversable: 1,
  sc_TraversableLike: 1,
  scg_HasNewBuilder: 1,
  scg_FilterMonadic: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1,
  sc_GenTraversableLike: 1,
  sc_Parallelizable: 1,
  sc_GenTraversable: 1,
  scg_GenericTraversableTemplate: 1,
  sc_Iterable: 1,
  sc_GenIterable: 1,
  sc_GenIterableLike: 1,
  sc_IterableLike: 1,
  s_Equals: 1,
  sc_Seq: 1,
  s_PartialFunction: 1,
  F1: 1,
  sc_GenSeq: 1,
  sc_GenSeqLike: 1,
  sc_SeqLike: 1,
  scm_Seq: 1,
  scm_Iterable: 1,
  scm_Traversable: 1,
  s_Mutable: 1,
  scm_SeqLike: 1,
  scm_Cloneable: 1,
  s_Cloneable: 1,
  jl_Cloneable: 1,
  scm_Buffer: 1,
  scm_BufferLike: 1,
  scg_Growable: 1,
  scg_Clearable: 1,
  scg_Shrinkable: 1,
  sc_script_Scriptable: 1,
  scg_Subtractable: 1,
  scm_ReusableBuilder: 1,
  scm_Builder: 1,
  scg_SeqForwarder: 1,
  scg_IterableForwarder: 1,
  scg_TraversableForwarder: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_scm_ListBuffer.prototype.$classData = $d_scm_ListBuffer;
const $ct_scm_StringBuilder__jl_StringBuilder__ = (function($thiz, underlying) {
  $thiz.scm_StringBuilder__f_underlying = underlying;
  return $thiz
});
const $ct_scm_StringBuilder__I__T__ = (function($thiz, initCapacity, initValue) {
  const this$1 = $ct_jl_StringBuilder__I__(new $c_jl_StringBuilder(), (($uI(initValue.length) + initCapacity) | 0));
  this$1.jl_StringBuilder__f_java$lang$StringBuilder$$content = (("" + this$1.jl_StringBuilder__f_java$lang$StringBuilder$$content) + initValue);
  $ct_scm_StringBuilder__jl_StringBuilder__($thiz, this$1);
  return $thiz
});
const $ct_scm_StringBuilder__ = (function($thiz) {
  $ct_scm_StringBuilder__I__T__($thiz, 16, "");
  return $thiz
});
class $c_scm_StringBuilder extends $c_scm_AbstractSeq {
  constructor() {
    super();
    this.scm_StringBuilder__f_underlying = null
  };
  sizeHint__I__V(size) {
    /*<skip>*/
  };
  sizeHintBounded__I__sc_TraversableLike__V(size, boundingColl) {
    $f_scm_Builder__sizeHintBounded__I__sc_TraversableLike__V(this, size, boundingColl)
  };
  $plus$plus$eq__sc_TraversableOnce__scg_Growable(xs) {
    return $f_scg_Growable__$plus$plus$eq__sc_TraversableOnce__scg_Growable(this, xs)
  };
  isEmpty__Z() {
    return $f_sc_IndexedSeqOptimized__isEmpty__Z(this)
  };
  foreach__F1__V(f) {
    $f_sc_IndexedSeqOptimized__foreach__F1__V(this, f)
  };
  sameElements__sc_GenIterable__Z(that) {
    return $f_sc_IndexedSeqOptimized__sameElements__sc_GenIterable__Z(this, that)
  };
  copyToArray__O__I__I__V(xs, start, len) {
    $f_sc_IndexedSeqOptimized__copyToArray__O__I__I__V(this, xs, start, len)
  };
  lengthCompare__I__I(len) {
    return $f_sc_IndexedSeqOptimized__lengthCompare__I__I(this, len)
  };
  toList__sci_List() {
    return $f_sc_IndexedSeqOptimized__toList__sci_List(this)
  };
  companion__scg_GenericCompanion() {
    return $m_scm_IndexedSeq$()
  };
  hashCode__I() {
    return $m_s_util_hashing_MurmurHash3$().seqHash__sc_Seq__I(this)
  };
  iterator__sc_Iterator() {
    return new $c_sc_IndexedSeqLike$Elements(this, 0, this.scm_StringBuilder__f_underlying.length__I())
  };
  sizeHintIfCheap__I() {
    return this.scm_StringBuilder__f_underlying.length__I()
  };
  length__I() {
    return this.scm_StringBuilder__f_underlying.length__I()
  };
  subSequence__I__I__jl_CharSequence(start, end) {
    return this.scm_StringBuilder__f_underlying.substring__I__I__T(start, end)
  };
  $plus$eq__C__scm_StringBuilder(x) {
    this.append__C__scm_StringBuilder(x);
    return this
  };
  append__O__scm_StringBuilder(x) {
    const this$2 = this.scm_StringBuilder__f_underlying;
    const str = ("" + x);
    this$2.jl_StringBuilder__f_java$lang$StringBuilder$$content = (this$2.jl_StringBuilder__f_java$lang$StringBuilder$$content + str);
    return this
  };
  append__T__scm_StringBuilder(s) {
    const this$1 = this.scm_StringBuilder__f_underlying;
    this$1.jl_StringBuilder__f_java$lang$StringBuilder$$content = (("" + this$1.jl_StringBuilder__f_java$lang$StringBuilder$$content) + s);
    return this
  };
  append__C__scm_StringBuilder(x) {
    const this$1 = this.scm_StringBuilder__f_underlying;
    const str = $as_T(String.fromCharCode(x));
    this$1.jl_StringBuilder__f_java$lang$StringBuilder$$content = (("" + this$1.jl_StringBuilder__f_java$lang$StringBuilder$$content) + str);
    return this
  };
  toString__T() {
    return this.scm_StringBuilder__f_underlying.jl_StringBuilder__f_java$lang$StringBuilder$$content
  };
  seq__sc_TraversableOnce() {
    return this
  };
  seq__sc_Seq() {
    return this
  };
  seq__scm_Seq() {
    return this
  };
  compare__O__I(that) {
    const other = $as_T(that);
    const this$1 = this.scm_StringBuilder__f_underlying.jl_StringBuilder__f_java$lang$StringBuilder$$content;
    return ((this$1 === other) ? 0 : ($uZ((this$1 < other)) ? (-1) : 1))
  };
  result__O() {
    return this.scm_StringBuilder__f_underlying.jl_StringBuilder__f_java$lang$StringBuilder$$content
  };
  $plus$eq__O__scg_Growable(elem) {
    return this.$plus$eq__C__scm_StringBuilder($uC(elem))
  };
  $plus$eq__O__scm_Builder(elem) {
    return this.$plus$eq__C__scm_StringBuilder($uC(elem))
  };
  apply__O__O(v1) {
    const index = $uI(v1);
    return $bC(this.scm_StringBuilder__f_underlying.charAt__I__C(index))
  };
  apply__I__O(idx) {
    return $bC(this.scm_StringBuilder__f_underlying.charAt__I__C(idx))
  };
  newBuilder__scm_Builder() {
    return new $c_scm_GrowingBuilder($ct_scm_StringBuilder__(new $c_scm_StringBuilder()))
  };
  thisCollection__sc_Traversable() {
    return this
  };
}
const $d_scm_StringBuilder = new $TypeData().initClass({
  scm_StringBuilder: 0
}, false, "scala.collection.mutable.StringBuilder", {
  scm_StringBuilder: 1,
  scm_AbstractSeq: 1,
  sc_AbstractSeq: 1,
  sc_AbstractIterable: 1,
  sc_AbstractTraversable: 1,
  O: 1,
  sc_Traversable: 1,
  sc_TraversableLike: 1,
  scg_HasNewBuilder: 1,
  scg_FilterMonadic: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1,
  sc_GenTraversableLike: 1,
  sc_Parallelizable: 1,
  sc_GenTraversable: 1,
  scg_GenericTraversableTemplate: 1,
  sc_Iterable: 1,
  sc_GenIterable: 1,
  sc_GenIterableLike: 1,
  sc_IterableLike: 1,
  s_Equals: 1,
  sc_Seq: 1,
  s_PartialFunction: 1,
  F1: 1,
  sc_GenSeq: 1,
  sc_GenSeqLike: 1,
  sc_SeqLike: 1,
  scm_Seq: 1,
  scm_Iterable: 1,
  scm_Traversable: 1,
  s_Mutable: 1,
  scm_SeqLike: 1,
  scm_Cloneable: 1,
  s_Cloneable: 1,
  jl_Cloneable: 1,
  jl_CharSequence: 1,
  scm_IndexedSeq: 1,
  sc_IndexedSeq: 1,
  sc_IndexedSeqLike: 1,
  scm_IndexedSeqLike: 1,
  sci_StringLike: 1,
  sc_IndexedSeqOptimized: 1,
  s_math_Ordered: 1,
  jl_Comparable: 1,
  scm_ReusableBuilder: 1,
  scm_Builder: 1,
  scg_Growable: 1,
  scg_Clearable: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_scm_StringBuilder.prototype.$classData = $d_scm_StringBuilder;
const $ct_scm_ArrayBuffer__I__ = (function($thiz, initialSize) {
  $thiz.scm_ArrayBuffer__f_initialSize = initialSize;
  $f_scm_ResizableArray__$init$__V($thiz);
  return $thiz
});
const $ct_scm_ArrayBuffer__ = (function($thiz) {
  $ct_scm_ArrayBuffer__I__($thiz, 16);
  return $thiz
});
class $c_scm_ArrayBuffer extends $c_scm_AbstractBuffer {
  constructor() {
    super();
    this.scm_ArrayBuffer__f_initialSize = 0;
    this.scm_ArrayBuffer__f_array = null;
    this.scm_ArrayBuffer__f_size0 = 0
  };
  length__I() {
    return this.scm_ArrayBuffer__f_size0
  };
  apply__I__O(idx) {
    return $f_scm_ResizableArray__apply__I__O(this, idx)
  };
  foreach__F1__V(f) {
    $f_scm_ResizableArray__foreach__F1__V(this, f)
  };
  copyToArray__O__I__I__V(xs, start, len) {
    $f_scm_ResizableArray__copyToArray__O__I__I__V(this, xs, start, len)
  };
  sizeHintBounded__I__sc_TraversableLike__V(size, boundingColl) {
    $f_scm_Builder__sizeHintBounded__I__sc_TraversableLike__V(this, size, boundingColl)
  };
  isEmpty__Z() {
    return $f_sc_IndexedSeqOptimized__isEmpty__Z(this)
  };
  sameElements__sc_GenIterable__Z(that) {
    return $f_sc_IndexedSeqOptimized__sameElements__sc_GenIterable__Z(this, that)
  };
  lengthCompare__I__I(len) {
    return $f_sc_IndexedSeqOptimized__lengthCompare__I__I(this, len)
  };
  toList__sci_List() {
    return $f_sc_IndexedSeqOptimized__toList__sci_List(this)
  };
  hashCode__I() {
    return $m_s_util_hashing_MurmurHash3$().seqHash__sc_Seq__I(this)
  };
  iterator__sc_Iterator() {
    return new $c_sc_IndexedSeqLike$Elements(this, 0, this.scm_ArrayBuffer__f_size0)
  };
  sizeHintIfCheap__I() {
    return this.scm_ArrayBuffer__f_size0
  };
  companion__scg_GenericCompanion() {
    return $m_scm_ArrayBuffer$()
  };
  sizeHint__I__V(len) {
    if (((len > this.scm_ArrayBuffer__f_size0) && (len >= 1))) {
      const newarray = $newArrayObject($d_O.getArrayOf(), [len]);
      const src = this.scm_ArrayBuffer__f_array;
      const length = this.scm_ArrayBuffer__f_size0;
      $systemArraycopy(src, 0, newarray, 0, length);
      this.scm_ArrayBuffer__f_array = newarray
    }
  };
  $plus$eq__O__scm_ArrayBuffer(elem) {
    const n = ((1 + this.scm_ArrayBuffer__f_size0) | 0);
    $f_scm_ResizableArray__ensureSize__I__V(this, n);
    this.scm_ArrayBuffer__f_array.set(this.scm_ArrayBuffer__f_size0, elem);
    this.scm_ArrayBuffer__f_size0 = ((1 + this.scm_ArrayBuffer__f_size0) | 0);
    return this
  };
  $plus$plus$eq__sc_TraversableOnce__scm_ArrayBuffer(xs) {
    if ($is_sc_IndexedSeqLike(xs)) {
      const x2 = $as_sc_IndexedSeqLike(xs);
      const n = x2.length__I();
      const n$1 = ((this.scm_ArrayBuffer__f_size0 + n) | 0);
      $f_scm_ResizableArray__ensureSize__I__V(this, n$1);
      x2.copyToArray__O__I__I__V(this.scm_ArrayBuffer__f_array, this.scm_ArrayBuffer__f_size0, n);
      this.scm_ArrayBuffer__f_size0 = ((this.scm_ArrayBuffer__f_size0 + n) | 0);
      return this
    } else {
      return $as_scm_ArrayBuffer($f_scg_Growable__$plus$plus$eq__sc_TraversableOnce__scg_Growable(this, xs))
    }
  };
  stringPrefix__T() {
    return "ArrayBuffer"
  };
  thisCollection__sc_Traversable() {
    return this
  };
  seq__sc_TraversableOnce() {
    return this
  };
  seq__sc_Seq() {
    return this
  };
  seq__scm_Seq() {
    return this
  };
  apply__O__O(v1) {
    const idx = $uI(v1);
    return $f_scm_ResizableArray__apply__I__O(this, idx)
  };
  result__O() {
    return this
  };
  $plus$plus$eq__sc_TraversableOnce__scg_Growable(xs) {
    return this.$plus$plus$eq__sc_TraversableOnce__scm_ArrayBuffer(xs)
  };
  $plus$eq__O__scg_Growable(elem) {
    return this.$plus$eq__O__scm_ArrayBuffer(elem)
  };
  $plus$eq__O__scm_Builder(elem) {
    return this.$plus$eq__O__scm_ArrayBuffer(elem)
  };
}
function $as_scm_ArrayBuffer(obj) {
  return (((obj instanceof $c_scm_ArrayBuffer) || (obj === null)) ? obj : $throwClassCastException(obj, "scala.collection.mutable.ArrayBuffer"))
}
function $isArrayOf_scm_ArrayBuffer(obj, depth) {
  return (!(!(((obj && obj.$classData) && (obj.$classData.arrayDepth === depth)) && obj.$classData.arrayBase.ancestors.scm_ArrayBuffer)))
}
function $asArrayOf_scm_ArrayBuffer(obj, depth) {
  return (($isArrayOf_scm_ArrayBuffer(obj, depth) || (obj === null)) ? obj : $throwArrayCastException(obj, "Lscala.collection.mutable.ArrayBuffer;", depth))
}
const $d_scm_ArrayBuffer = new $TypeData().initClass({
  scm_ArrayBuffer: 0
}, false, "scala.collection.mutable.ArrayBuffer", {
  scm_ArrayBuffer: 1,
  scm_AbstractBuffer: 1,
  scm_AbstractSeq: 1,
  sc_AbstractSeq: 1,
  sc_AbstractIterable: 1,
  sc_AbstractTraversable: 1,
  O: 1,
  sc_Traversable: 1,
  sc_TraversableLike: 1,
  scg_HasNewBuilder: 1,
  scg_FilterMonadic: 1,
  sc_TraversableOnce: 1,
  sc_GenTraversableOnce: 1,
  sc_GenTraversableLike: 1,
  sc_Parallelizable: 1,
  sc_GenTraversable: 1,
  scg_GenericTraversableTemplate: 1,
  sc_Iterable: 1,
  sc_GenIterable: 1,
  sc_GenIterableLike: 1,
  sc_IterableLike: 1,
  s_Equals: 1,
  sc_Seq: 1,
  s_PartialFunction: 1,
  F1: 1,
  sc_GenSeq: 1,
  sc_GenSeqLike: 1,
  sc_SeqLike: 1,
  scm_Seq: 1,
  scm_Iterable: 1,
  scm_Traversable: 1,
  s_Mutable: 1,
  scm_SeqLike: 1,
  scm_Cloneable: 1,
  s_Cloneable: 1,
  jl_Cloneable: 1,
  scm_Buffer: 1,
  scm_BufferLike: 1,
  scg_Growable: 1,
  scg_Clearable: 1,
  scg_Shrinkable: 1,
  sc_script_Scriptable: 1,
  scg_Subtractable: 1,
  scm_IndexedSeqOptimized: 1,
  scm_IndexedSeqLike: 1,
  sc_IndexedSeqLike: 1,
  sc_IndexedSeqOptimized: 1,
  scm_Builder: 1,
  scm_ResizableArray: 1,
  scm_IndexedSeq: 1,
  sc_IndexedSeq: 1,
  sc_CustomParallelizable: 1,
  s_Serializable: 1,
  Ljava_io_Serializable: 1
});
$c_scm_ArrayBuffer.prototype.$classData = $d_scm_ArrayBuffer;
$L0 = new $c_RTLong(0, 0);
$s_Lcom_regblanc_scalavator_html5_Main__main__AT__V($makeNativeArrayWrapper($d_T.getArrayOf(), []));
}).call(this);
//# sourceMappingURL=scalavator-html5-fastopt.js.map
