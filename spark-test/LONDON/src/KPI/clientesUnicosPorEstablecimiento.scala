package KPI

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.log4j._

object clientesUnicosPorEstablecimiento {

  def kpi_establecimientos_clientes_unicos(ruta: String) = {
    val sc = new SparkContext("local[*]", "clientesUnicosPorEstablecimiento")
    val rdd = sc.textFile(ruta)
    val r1 = rdd.map(r => r.split("\t")).map(r => (r(6), r(14)))
    //Tenemos dos alternativas
    r1.sortByKey().distinct().map(x => (x._1, 1)).reduceByKey(_ + _).foreach(println)
    /*
	 (554461701,4)
  (392935103,1)
  (951699402,1)
	 * */
    //r1.sortByKey().distinct().map(x => (x._1)).countByValue().foreach(println)
    /*
	(553439903,1)
  (523943306,1)
  (530711404,1)
	*/
  }

  def main(args: Array[String]) {

    kpi_establecimientos_clientes_unicos("tablon.tsv")

  }

}