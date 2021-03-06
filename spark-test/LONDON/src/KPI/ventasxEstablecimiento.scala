package KPI

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.log4j._

object ventasxEstablecimiento {

  def kpi_ventas_x_establecimientos(ruta: String) = {
    val sc = new SparkContext("local[*]", "ventasxEstablecimiento")
    val rdd = sc.textFile(ruta)
    val r1 = rdd.map(r => r.split("\t")).map(r => (r(6), r(12).toDouble))
    r1.reduceByKey(_ + _).foreach(println)

    println("Detalle de ventas de establecimiento: 101098601")
    r1.filter(x => x._1.contains("101098601")).foreach(println)

    //dataCurso.filter(x => x._2 >= 40 && x._2 <= 50 && x._1.contains("Matemá")).foreach(println)

    //(101098601,781.84)
  }

  def main(args: Array[String]) {

    kpi_ventas_x_establecimientos("tablon.tsv")

  }

}