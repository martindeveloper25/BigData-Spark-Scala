package KPI

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.log4j._

object promedio_de_transacciones_cliente {

  def kpi_promedio_transacciones_por_clientes(ruta: String) = {
    val sc = new SparkContext("local[*]", "promedio_de_transacciones_cliente")
    val rdd = sc.textFile(ruta)
    val r1 = rdd.map(r => r.split("\t")).map(r => (r(6), r(14)))
    val transacciones = r1.map(x => (x._1, 1)).reduceByKey(_ + _)
    val clientsesUnicos = r1.distinct().map(x => (x._1, 1)).reduceByKey(_ + _)
    val resultado = transacciones.join(clientsesUnicos).mapValues(x => (x._1.toFloat / x._2.toFloat))

    resultado.sortByKey().foreach(println)
  }

  def main(args: Array[String]) {

    kpi_promedio_transacciones_por_clientes("tablon.tsv")

  }

}