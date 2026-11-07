ScalaNativeInit()
let result = sgl_add(20, 22)
precondition(result == 42, "unexpected Scala Native result: \(result)")
