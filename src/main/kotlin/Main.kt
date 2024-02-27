import java.util.Arrays
import java.util.UUID
import kotlin.math.cbrt
import kotlin.math.pow

data class Node(
    val item:String,
    val id:String = UUID.randomUUID().toString()
)

data class SimpleExpression(
    val item: String,
    val sign:String? = null,
    val finished:Boolean = false
)

class Calculator{

    val nodes = mutableMapOf<String, Pair<SimpleExpression, MutableList<String>>>()


    fun findGraphHeight() = nodes.size - 1

    private fun transformSimpleExpression(expression: MutableList<Node>, sign:String, index: Int):MutableList<Node>{


        val first = expression[index - 1]
        val second = expression[index + 1]
        // println(expression)

        // println("${expression[index - 1]} $sign ${expression[index - 1]}")

        val result = (
                when(sign){
                    "*" -> first.item.toFloat() * second.item.toFloat()
                    "/" -> first.item.toFloat() / second.item.toFloat()
                    "+" -> first.item.toFloat() + second.item.toFloat()
                    "-" -> first.item.toFloat() - second.item.toFloat()
                    "^" -> first.item.toFloat().pow(second.item.toFloat())
                    else -> first.item.toFloat() + second.item.toFloat()
                }
                ).toString()

        val node = Node(item = result, id = expression[index].id)

        if(first.id !in nodes){
            nodes[first.id] = Pair(SimpleExpression(first.item), mutableListOf())
        }

        if(second.id !in nodes){
            nodes[second.id] = Pair(SimpleExpression(second.item), mutableListOf())
        }

        nodes[node.id] = Pair(SimpleExpression(
            item = node.item,
            sign = sign,
            finished = expression.size == 3
        ), mutableListOf(first.id, second.id))

        expression[index] = node
        expression.removeAt(index - 1)
        expression.removeAt(index)




        return expression
    }
    fun replaceExpressions(baseExpression:List<Node>, startIndex:Int, endIndex:Int, newExpression:List<Node>):List<Node>{

        val output = mutableListOf<Node>()
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
    fun transformExpression(expression:List<Node>):List<Node>{

        var startDeepBracketIndex = 0
        var endDeepBracketIndex = expression.size - 1

        run breaking@{
            expression.forEachIndexed {index, element ->
                if(element.item == "("){
                    startDeepBracketIndex = index
                    for (i in index+1 until expression.size){
                        if(expression[i].item == "("){
                            startDeepBracketIndex = i
                        }
                        if(expression[i].item == ")"){
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
            for(sign in listOf(listOf("^"), listOf("*", "/"), listOf("+", "-"))){
                workingArea.forEachIndexed {index, item ->
                    if(sign.contains(item.item)){
                        simpleExpression = transformSimpleExpression(
                            expression = workingArea.toMutableList(),
                            sign = item.item,
                            index = index
                        )
                        return@breaking
                    }
                }
            }
        }

        if(simpleExpression.size == 3 && simpleExpression[0].item == "("){
            simpleExpression = listOf(simpleExpression[1])
        }

        return replaceExpressions(
            baseExpression = expression,
            startIndex = startDeepBracketIndex,
            endIndex = endDeepBracketIndex,
            newExpression = simpleExpression
        )


    }

    fun getStartNode():String{
        nodes.forEach {item ->
            if(item.value.first.finished) return item.key
        }
        return ""
    }

    fun calculate(expression: List<Node>):Float{
        if(expression.size == 1) return expression[0].item.toFloat()
        return calculate(transformExpression(expression))
    }

    fun findDependsNodesCount(nodeId:String, count:Int):Int{
        val node = nodes[nodeId]
        if(node!!.second.isEmpty()) return 0
        if(
            nodes[node!!.second[0]]!!.second.isEmpty()
            && nodes[node.second[1]]!!.second.isEmpty()
        ) return 2
        return count + 2 + findDependsNodesCount(node.second[0], count) + findDependsNodesCount(node.second[1], count)
    }

    private val visitedNodes = mutableListOf<String>()

    var graph = mutableListOf<String>()

    fun buildGraph(nodeId:String, depth:Int){

        if(visitedNodes.size == nodes.size) return
        val node = nodes[nodeId]
        if(node!!.second.isEmpty()) {
            visitedNodes.add(nodeId)
            return
        }

        val firstSubNodeId = node.second[0]
        val secondSubNodeId = node.second[1]
        val firstSubNode = nodes[firstSubNodeId]
        val secondSubNode = nodes[secondSubNodeId]

        val primeNode = if(firstSubNode!!.first.sign != null) firstSubNodeId else secondSubNodeId
        val notPrimeNode = if(firstSubNodeId != primeNode) firstSubNodeId else secondSubNodeId

        var outStr = "|"

        outStr += "\r——>${node.first.sign}"
        if(firstSubNode.first.sign == null && secondSubNode?.first?.sign == null){
            outStr += ("\r   |")
            outStr += ("\r   ——>${nodes[primeNode]?.first?.item}")
            outStr += ("\r   |")
            outStr += ("\r   ——>${nodes[notPrimeNode]?.first?.item}")
        }else{
            for(i in 0 until findDependsNodesCount(primeNode, 0) * 3){
                outStr += ("\r|")
            }
            outStr += ("\r——>${nodes[notPrimeNode]!!.first.sign ?: nodes[notPrimeNode]!!.first.item}")
        }
        outStr += ""
        graph += outStr
        buildGraph(firstSubNodeId, 0)
        buildGraph(secondSubNodeId, 0)
    }


}

fun main(args: Array<String>) {
    val expression = "5 * 2 + ( ( 13 + 4 * 5 ) + 5 + ( 4 / 2 + 5 ) ) * 2 + 15".split(" ")
    val expression2 = "4 - ( 15 + 3 * 2 ) * 2".split(" ")
    val calculator = Calculator()
    calculator.calculate(expression2.map { Node(item = it) })
    calculator.buildGraph(calculator.getStartNode(), 0)

    val tmp = calculator.graph.joinToString("").split("\r?\n|\r".toRegex()).toTypedArray()
    println(calculator.graph)

//    calculator.graph.forEach {
//        print(it)
//        print(" ")
//        print(" ")
//        print(" ")
//    }
}

