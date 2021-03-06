package KPI

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.log4j._

object countByPerfilDigital {

  def kpi_establecimiento_x_perfil_digital(ruta: String) = {
    val sc = new SparkContext("local[*]", "countByPerfilDigital")
    val rdd = sc.textFile(ruta)
    val r1 = rdd.map(r => r.split("\t")).map(r => (r(6), r(47)))
    //Tenemos dos alternativas
    r1.map(x => (x._1 + "," + x._2, 1)).reduceByKey(_ + _).foreach(println)
    //r1.sortByKey().countByValue.foreach(println)
  }

  def main(args: Array[String]) {

    kpi_establecimiento_x_perfil_digital("tablon.tsv")
    /*
      (537336001,1,1)
      (515160803,2,2)
      (100443301,3,2)
      
      ((904531601,2),1)
      ((418140341,3),1)
      ((503169302,3),1)
       * */

  }

}