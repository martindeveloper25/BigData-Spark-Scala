package KPI

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.log4j._
import java.util.ArrayList
import org.apache.spark.rdd.RDD

//import org.apache.spark.rdd.RDD

object ClientesRecurrentes {

  /**
   * Data Array del Tablóm
   * Posiciones
   * 17 => RUC
   * 6 => CodEstablecimiento
   * 14 => CodCliente
   * 25 => PeriodoAñoMes
   */

  def parseLine(line: String) = {

    val fields = line.split("\t")
    val codest = fields(6).toString
    val cic = fields(14).toString
    val month = fields(25).toString
    (codest, cic, month)
  }

  def convertDate(x: String, concurrence: Int): String = {

    var year = x.substring(0, 4).toInt
    var month = x.substring(4, 6).toInt
    if (concurrence > 1) {
      for (x <- 1 to concurrence) {
        month -= 1
        if (month <= 0) {
          year -= 1
          month = 12
        }
      }
    }
    var mes = ""
    if (month < 10) {
      mes = "0" + month.toString()
    } else {
      mes = month.toString()
    }
    val result = year.toString() + mes.toString()
    return result
  }

  def filterTable(x: RDD[(String, String, String)], codest: String, month: String, concurrence: Int): RDD[String] = {

    val filterByEst = x.filter(x => x._1 == codest)
    val filterByMonth = filterByEst.filter(x => x._3 <= month && x._3 >= convertDate(month, concurrence))
    val result = filterByMonth.map(x => (x._2).toString())
    return result

  }

  def calculateTotalsByRange(rdd: RDD[(String, String, String)], codest: String, month: String, concurrence: Int): RDD[(String, Int)] = {

    val filteredTable = filterTable(rdd, codest, month, concurrence)
    val totalsByCic = filteredTable.map(x => (x, 1)).reduceByKey((x, y) => x + y)

    val temp1 = totalsByCic.filter(x => x._2 == 1).map(x => ("1", 1)).reduceByKey((x, y) => x + y)
    val temp2 = totalsByCic.filter(x => x._2 == 2).map(x => ("2", 1)).reduceByKey((x, y) => x + y)
    val temp3 = totalsByCic.filter(x => x._2 >= 3 && x._2 <= 5).map(x => ("3-5", 1)).reduceByKey((x, y) => x + y)
    val temp4 = totalsByCic.filter(x => x._2 >= 6 && x._2 <= 10).map(x => ("6-10", 1)).reduceByKey((x, y) => x + y)
    val temp5 = totalsByCic.filter(x => x._2 >= 11 && x._2 <= 20).map(x => ("11-20", 1)).reduceByKey((x, y) => x + y)
    val temp6 = totalsByCic.filter(x => x._2 >= 21).map(x => ("21+", 1)).reduceByKey((x, y) => x + y)
    val totalsByVisit = temp1.union(temp2).union(temp3).union(temp4).union(temp5).union(temp6)

    return totalsByVisit
  }

  // def calculateTotalsByMonth(rdd: RDD[(String, String, String)], codest: String, month: String): RDD[(String, String)] = {
  def calculateTotalsByMonth(rdd: RDD[(String, String, String)], codest: String, month: String) {

    //val index = calculateTotalsByRange(rdd, codest, month, 1)
    val temp1 = calculateTotalsByRange(rdd, codest, month, 1)
    val temp2 = calculateTotalsByRange(rdd, codest, month, 3)
    val temp3 = calculateTotalsByRange(rdd, codest, month, 6)
    val temp4 = calculateTotalsByRange(rdd, codest, month, 12)
    val temp5 = calculateTotalsByRange(rdd, codest, month, 24)
    val temp6 = temp1.union(temp2).union(temp3).union(temp4).union(temp5)

    //val result = index.map(x => (x._1, temp6))

    temp6.foreach(println)
  }

  def kpi_clientes_recurrentes(ruta: String, esta: List[String], inicio: String) = {

    val sc = new SparkContext("local[*]", "ClientesRecurrentes")
    //Para metrizable
    val rangoVisitas = List(1, 3, 6, 12, 24)

    val rddTablon = sc.textFile(ruta)
    val rddDepurado = rddTablon.map(r => r.split("\t")).map(r => (r(17), r(6), r(14), r(25)))
    
    val rddFiltrado = rddDepurado.filter(x => (x._4 >= inicio && x._4 <= inicio) && esta.exists(p => p.contains(x._2))).map(x => (x._1 + "-" + x._2 + "-" + x._3 + "-" + x._4, 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia1 = rddFiltrado.filter(x => x._2 == 1).map(x => ("1M-01", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia2 = rddFiltrado.filter(x => x._2 == 2).map(x => ("1M-02", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia3_5 = rddFiltrado.filter(x => x._2 >= 3 & x._2 <= 5).map(x => ("1M-03", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia6_10 = rddFiltrado.filter(x => x._2 >= 6 & x._2 <= 10).map(x => ("1M-04", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia11_20 = rddFiltrado.filter(x => x._2 >= 11 & x._2 <= 20).map(x => ("1M-05", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia21_mas = rddFiltrado.filter(x => x._2 >= 21).map(x => ("1M-06", 1)).reduceByKey(_ + _)

    /*
    //3 MESES
    val rddFiltrado2 = rddDepurado.filter(x => (x._4 >= convertDate(inicio,3) && x._4 <= inicio) && esta.exists(p => p.contains(x._2))).map(x => (x._1 + "-" + x._2 + "-" + x._3 + "-" + x._4, 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia2_1 = rddFiltrado2.filter(x => x._2 == 1).map(x => ("3M-01", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia2_2 = rddFiltrado2.filter(x => x._2 == 2).map(x => ("3M-02", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia2_3_5 = rddFiltrado2.filter(x => x._2 >= 3 & x._2 <= 5).map(x => ("3M-03", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia2_6_10 = rddFiltrado2.filter(x => x._2 >= 6 & x._2 <= 10).map(x => ("3M-04", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia2_11_20 = rddFiltrado2.filter(x => x._2 >= 11 & x._2 <= 20).map(x => ("3M-05", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia2_21_mas = rddFiltrado2.filter(x => x._2 >= 21).map(x => ("3M-06", 1)).reduceByKey(_ + _)

    //6 MESES
    val rddFiltrado3 = rddDepurado.filter(x => (x._4 >= convertDate(inicio,6) && x._4 <= inicio) && esta.exists(p => p.contains(x._2))).map(x => (x._1 + "-" + x._2 + "-" + x._3 + "-" + x._4, 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia3_1 = rddFiltrado3.filter(x => x._2 == 1).map(x => ("6M-01", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia3_2 = rddFiltrado3.filter(x => x._2 == 2).map(x => ("6M-02", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia3_3_5 = rddFiltrado3.filter(x => x._2 >= 3 & x._2 <= 5).map(x => ("6M-03", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia3_6_10 = rddFiltrado3.filter(x => x._2 >= 6 & x._2 <= 10).map(x => ("6M-04", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia3_11_20 = rddFiltrado3.filter(x => x._2 >= 11 & x._2 <= 20).map(x => ("6M-05", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia3_21_mas = rddFiltrado3.filter(x => x._2 >= 21).map(x => ("6M-06", 1)).reduceByKey(_ + _)

    //12 MESES
    val rddFiltrado4 = rddDepurado.filter(x => (x._4 >= convertDate(inicio,12) && x._4 <= inicio) && esta.exists(p => p.contains(x._2))).map(x => (x._1 + "-" + x._2 + "-" + x._3 + "-" + x._4, 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia4_1 = rddFiltrado4.filter(x => x._2 == 1).map(x => ("12M-01", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia4_2 = rddFiltrado4.filter(x => x._2 == 2).map(x => ("12M-02", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia4_3_5 = rddFiltrado4.filter(x => x._2 >= 3 & x._2 <= 5).map(x => ("12M-03", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia4_6_10 = rddFiltrado4.filter(x => x._2 >= 6 & x._2 <= 10).map(x => ("12M-04", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia4_11_20 = rddFiltrado4.filter(x => x._2 >= 11 & x._2 <= 20).map(x => ("12M-05", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia4_21_mas = rddFiltrado4.filter(x => x._2 >= 21).map(x => ("12M-06", 1)).reduceByKey(_ + _)

    //24 MESES
    val rddFiltrado5 = rddDepurado.filter(x => (x._4 >= convertDate(inicio,24) && x._4 <= inicio) && esta.exists(p => p.contains(x._2))).map(x => (x._1 + "-" + x._2 + "-" + x._3 + "-" + x._4, 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia5_1 = rddFiltrado5.filter(x => x._2 == 1).map(x => ("24M-01", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia5_2 = rddFiltrado5.filter(x => x._2 == 2).map(x => ("24M-02", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia5_3_5 = rddFiltrado5.filter(x => x._2 >= 3 & x._2 <= 5).map(x => ("24M-03", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia5_6_10 = rddFiltrado5.filter(x => x._2 >= 6 & x._2 <= 10).map(x => ("24M-04", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia5_11_20 = rddFiltrado5.filter(x => x._2 >= 11 & x._2 <= 20).map(x => ("24M-05", 1)).reduceByKey(_ + _)
    val rddVisitas_Frecuencia5_21_mas = rddFiltrado5.filter(x => x._2 >= 21).map(x => ("24M-06", 1)).reduceByKey(_ + _)
*/
    //Unimos todos los RDDs
    val visitasAgrupadas1M = rddVisitas_Frecuencia1.union(rddVisitas_Frecuencia2).union(rddVisitas_Frecuencia3_5)
      .union(rddVisitas_Frecuencia6_10).union(rddVisitas_Frecuencia11_20).union(rddVisitas_Frecuencia21_mas)

      /*
    val visitasAgrupadas3M = rddVisitas_Frecuencia2_1.union(rddVisitas_Frecuencia2_2).union(rddVisitas_Frecuencia2_3_5)
      .union(rddVisitas_Frecuencia2_6_10).union(rddVisitas_Frecuencia2_11_20).union(rddVisitas_Frecuencia2_21_mas)

    val visitasAgrupadas6M = rddVisitas_Frecuencia3_1.union(rddVisitas_Frecuencia3_2).union(rddVisitas_Frecuencia3_3_5)
      .union(rddVisitas_Frecuencia3_6_10).union(rddVisitas_Frecuencia3_11_20).union(rddVisitas_Frecuencia3_21_mas)

    val visitasAgrupadas12M = rddVisitas_Frecuencia4_1.union(rddVisitas_Frecuencia4_2).union(rddVisitas_Frecuencia4_3_5)
      .union(rddVisitas_Frecuencia4_6_10).union(rddVisitas_Frecuencia4_11_20).union(rddVisitas_Frecuencia4_21_mas)

    val visitasAgrupadas24M = rddVisitas_Frecuencia5_1.union(rddVisitas_Frecuencia5_2).union(rddVisitas_Frecuencia5_3_5)
      .union(rddVisitas_Frecuencia5_6_10).union(rddVisitas_Frecuencia5_11_20).union(rddVisitas_Frecuencia5_21_mas)
*/
    //val visitasAgrupadas2 = rddVisitas_Frecuencia1_3.join(rddVisitas_Frecuencia2_3)

    /*
    println("aaaaaaaaaaaaaaaa")
    val x = Array(rddVisitas_Frecuencia1.first())
    println(x(1))
    //val x = Array(3,(6))
    
    //val x = Array(3,(1,2,3))
    
    println(x)
    
    */
    //.union(rddVisitas_Frecuencia3_5).union(rddVisitas_Frecuencia6_10).union(rddVisitas_Frecuencia11_20).union(rddVisitas_Frecuencia21_mas)

    //rddVisitas_Frecuencia1.sortByKey(false).foreach(println)

    /*
    println("1 MESES")
    visitasAgrupadas1M.sortByKey().foreach(println)

    println("3 MESES")
    visitasAgrupadas3M.sortByKey().foreach(println)

    println("6 MESES")
    visitasAgrupadas6M.sortByKey().foreach(println)

    println("12 MESES")
    visitasAgrupadas12M.sortByKey().foreach(println)
*/
    //println(x(3))

    //println("Unidos")
    //visitasAgrupadas.union(visitasAgrupadas2).foreach(println)

      /*
    val todoJunto = visitasAgrupadas1M.union(visitasAgrupadas3M).union(visitasAgrupadas6M).union(visitasAgrupadas12M).union(visitasAgrupadas24M)
    todoJunto.sortByKey().foreach(println)
    * 
    */
      
   visitasAgrupadas1M.foreach(println)

  }

  def main(args: Array[String]) {

    println("Establecimiento 100070934")
    kpi_clientes_recurrentes("tablon.tsv", List("100070934","100070905"), "201512")

    //println("Establecimiento 100070905")
    //kpi_clientes_recurrentes("tablon.tsv","100070905","201609","201610")

    //println("Establecimientos 100070934 y 100070905")
    //kpi_clientes_recurrentes_n_establecimientos("tablon.tsv","100070934","100070905","201609","201610")

    
    val sc = new SparkContext("local[*]", "ClientesRecurrentes")

    val lines = sc.textFile("tablon.tsv")

    val rdd = lines.map(parseLine)
    

    //calculateTotalsByMonth(rdd, "100070934", "201512").foreach(println)
    calculateTotalsByMonth(rdd, "100070934", "201512")
    //calculateTotalsByRange(rdd,"100070934","201512",1)

  }

}