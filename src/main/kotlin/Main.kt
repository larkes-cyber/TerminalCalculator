import kotlin.math.pow

private fun transformSimpleExpression(expression: MutableList<String>, sign:String, index: Int):MutableList<String>{

    val first = expression[index - 1]
    val second = expression[index + 1]
    expression[index] = (
            when(sign){
                "*" -> first.toFloat() * second.toFloat()
                "/" -> first.toFloat() / second.toFloat()
                "+" -> first.toFloat() + second.toFloat()
                "-" -> first.toFloat() - second.toFloat()
                "^" -> first.toFloat().pow(second.toFloat())
                else -> first.toFloat() + second.toFloat()
            }
            ).toString()
    expression.removeAt(index - 1)
    expression.removeAt(index)

    return expression
}

fun replaceExpressions(baseExpression:List<String>, startIndex:Int, endIndex:Int, newExpression:List<String>):List<String>{

    val output = mutableListOf<String>()
    for(i in 0 until startIndex){
        output.add(baseExpression[i])
    }

    newExpression.forEach {
        output.add(it)
    }

    for(i in endIndex + 1 until baseExpression.size){
        output.add(baseExpression[i])
    }

    return output

}

fun transformExpression(expression:List<String>):List<String>{

    var startDeepBracketIndex = 0
    var endDeepBracketIndex = expression.size - 1

    run breaking@{
        expression.forEachIndexed {index, element ->
            if(element == "("){
                startDeepBracketIndex = index
                for (i in index+1 until expression.size){
                    if(expression[i] == "("){
                        startDeepBracketIndex = i
                    }
                    if(expression[i] == ")"){
                        endDeepBracketIndex = i
                        return@breaking
                    }
                }
            }
        }
    }

    val workingArea = expression.slice(startDeepBracketIndex .. endDeepBracketIndex )
    var simpleExpression = workingArea
    run breaking@{
        for(sign in listOf("^", "*", "/", "+", "-")){
            workingArea.forEachIndexed {index, item ->
                if(item == sign){
                    simpleExpression = transformSimpleExpression(
                       expression = workingArea.toMutableList(),
                        sign = sign,
                        index = index
                    )
                    return@breaking
                }
            }
        }
    }

    if(simpleExpression.size == 3 && simpleExpression[0] == "("){
        simpleExpression = listOf(simpleExpression[1])
    }

    return replaceExpressions(
        baseExpression = expression,
        startIndex = startDeepBracketIndex,
        endIndex = endDeepBracketIndex,
        newExpression = simpleExpression
    )


}

fun calculator(expression: List<String>):Float{
    println(expression.joinToString(""))
    if(expression.size == 1) return expression[0].toFloat()
    return calculator(transformExpression(expression))
}


fun main(args: Array<String>) {
    val expression = "5.2 * 2.1 + ( ( 13.4 + 4.5 * 5.1 ) + 5.5 + ( 4 / 2 + 5 ) ) * 2.5 + 15".split(" ")
    val expression2 = "5 ^ ( 1.5 + 1.5 ) * 2".split(" ")
    calculator(expression)
}

fun Int.pow(x: Int): Int = (2..x).fold(this) { r, _ -> r * this }
