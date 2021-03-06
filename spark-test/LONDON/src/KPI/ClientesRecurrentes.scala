package KPI

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.log4j._
import java.util.ArrayList
import org.apache.spark.rdd.RDD

object ClientesRecurrentes {

  /**
   * Data del Tablón
   * 17 => RUC
   * 6 => CODESTABLECIMIENTO
   * 12 => MTOTRANSACCION
   * 14 => CODCLAVECIC_CLIENTE
   * 25 => CODMES
   * 43 => SEXO_CLIENTE
   * 46 => RANGO_SUELDO
   * 47 => TIPUSODIGITAL
   * 48 => DESTIPUSODIGITAL
   * 57 => RANGO_EDAD
   */

  def convertDate(x: String, concurrence: Int): Int = {

    var year = x.substring(0, 4).toInt
    var month = x.substring(4, 6).toInt
    var fecha = x
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
    var result = (year.toString() + mes.toString()).toInt

    if (concurrence == 1) {
      result = fecha.toInt
    }

    return result
  }

  def obtenerVisitas(rdd: RDD[(String, String, String, String)], inicio: String, esta: List[String], visita: List[Int]): Array[RDD[(String, Int)]] = {

    var v = new Array[RDD[(String, Int)]](visita.length)
    var contador: Int = 0
    var rango: Int = 0

    for (name <- visita) {

      rango = convertDate(inicio, visita(contador))
      var rddFiltrado = rdd.filter(x => (x._4 >= rango.toString() && x._4 <= inicio) && esta.exists(p => p.contains(x._2))).map(x => (x._1 + "-" + x._2 + "-" + x._3 + "-" + x._4, 1)).reduceByKey(_ + _)
      var rddVisitas_Frecuencia1 = rddFiltrado.filter(x => x._2 == 1).map(x => (name.toString() + "M-01", 1)).reduceByKey(_ + _)
      var rddVisitas_Frecuencia2 = rddFiltrado.filter(x => x._2 == 2).map(x => (name.toString() + "M-02", 1)).reduceByKey(_ + _)
      var rddVisitas_Frecuencia3_5 = rddFiltrado.filter(x => x._2 >= 3 & x._2 <= 5).map(x => (name.toString() + "M-03", 1)).reduceByKey(_ + _)
      var rddVisitas_Frecuencia6_10 = rddFiltrado.filter(x => x._2 >= 6 & x._2 <= 10).map(x => (name.toString() + "M-04", 1)).reduceByKey(_ + _)
      var rddVisitas_Frecuencia11_20 = rddFiltrado.filter(x => x._2 >= 11 & x._2 <= 20).map(x => (name.toString() + "M-05", 1)).reduceByKey(_ + _)
      var rddVisitas_Frecuencia21_mas = rddFiltrado.filter(x => x._2 >= 21).map(x => (name.toString() + "M-06", 1)).reduceByKey(_ + _)
      v(contador) = rddVisitas_Frecuencia1.union(rddVisitas_Frecuencia2).union(rddVisitas_Frecuencia3_5)
        .union(rddVisitas_Frecuencia6_10).union(rddVisitas_Frecuencia11_20).union(rddVisitas_Frecuencia21_mas).sortByKey()
      contador = contador + 1
    }

    return v

  }

  def kpi_clientes_recurrentes(ruta: String, esta: List[String], inicio: String, rangoVisitas: List[Int]): Array[RDD[(String, Int)]] = {

    val sc = new SparkContext("local[*]", "ClientesRecurrentes")
    val rddTablon = sc.textFile(ruta)
    val rddDepurado = rddTablon.map(r => r.split("\t")).map(r => (r(17), r(6), r(14), r(25)))

    var data = obtenerVisitas(rddDepurado, inicio, esta, rangoVisitas)

    return data

    /*
    for (a <- 0 to data.length - 1) {
      data(a).foreach(println)
    }
    * */

  }

  def main(args: Array[String]) {

    println("Establecimiento 100070934")
    val kpiCli = kpi_clientes_recurrentes("tablon.tsv", List("100070934"), "201512", List(1, 3, 6, 12, 24))
    kpiCli(0).foreach(println)
    kpiCli(1).foreach(println)

    //kpi_clientes_recurrentes("tablon.tsv", List("511612801"), "201512", Array(1, 3, 6, 12, 24))

  }

}