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
        println(node)
        if(node!!.second.isEmpty()) return 0
        if(
            nodes[node!!.second[0]]!!.second.isEmpty()
            && nodes[node.second[1]]!!.second.isEmpty()
            ) return 2
        return count + 2 + findDependsNodesCount(node.second[0], count) + findDependsNodesCount(node.second[1], count)
    }

    private val visitedNodes = mutableListOf<String>()
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


        println("|")
        println("——>${nodes[primeNode]!!.first.sign ?: nodes[primeNode]!!.first.item}")
        for(i in 0 until findDependsNodesCount(primeNode, 0)){
            println("|")
        }
        println("——>${nodes[notPrimeNode]!!.first.sign ?: nodes[notPrimeNode]!!.first.item}")


    }


}







fun main(args: Array<String>) {
    val expression = "5 * 2 + ( ( 13 + 4 * 5 ) + 5 + ( 4 / 2 + 5 ) ) * 2 + 15".split(" ").map {
        Node(item = it)
    } //2 - 3 + 5 * ( 2 ^ 2 + 1 ) - 2
    val expression2 = "2 - 3 + 5 * ( 2 / 2 + 4 ) + 5".split(" ").map {
        Node(item = it)
    }
    val calculator = Calculator()
    calculator.calculate(expression2)
   //
    calculator.nodes.forEach { item ->
        println("${item.key} ### ${item.value}")
    }

    val secondNode = calculator.nodes[calculator.getStartNode()]!!.second[0]

    println(calculator.findDependsNodesCount(secondNode, 0))
    val graph = mapOf(
        "A" to listOf("B", "C"),
        "B" to listOf("D"),
        "C" to listOf("E"),
        "D" to emptyList(),
        "E" to emptyList()
    )


}

fun Int.pow(x: Int): Int = (2..x).fold(this) { r, _ -> r * this }
