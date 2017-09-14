package tablon

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.log4j._


object transxClientesEstab {
  
  
def kpi_establecimientos_transacciones_x_clientes(ruta: String) = {
   val sc = new SparkContext("local[*]", "transxClientesEstab")
	val rdd = sc.textFile(ruta)
	val r1 = rdd.map(r => r.split("\t")).map(r => (r(6),r(14)))
	r1.countByValue.foreach(println)
}

   def main(args: Array[String]) {
   
      kpi_establecimientos_transacciones_x_clientes("tablon.tsv")
    
  }
  
  
}