package TestDataFrame

import org.apache.spark._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._

object PerfilDigitalFrame {

  /*
   * Data del Tablón
   * 17 => RUC
   * 6  => CODESTABLECIMIENTO
   * 12 => MTOTRANSACCION
   * 14 => CODCLAVECIC_CLIENTE
   * 25 => CODMES
   * 43 => SEXO_CLIENTE
   * 46 => RANGO_SUELDO
   * 47 => TIPUSODIGITAL
   * 48 => DESTIPUSODIGITAL
   * 57 => RANGO_EDAD
   */

  /*
  var spark = SparkSession
    .builder
    .appName("PocDF")
    .master("local[*]")
    .getOrCreate()
    * 
    */
  
  val sc = new SparkContext("local[*]", "PerfilDigitalFrame")
  val sqlContext = new org.apache.spark.sql.SQLContext(sc)

  def byClientes(ruta: String, esta: List[String], inicio: String, fin: String): DataFrame = {

    val rddTablon = sc.textFile(ruta).map(r => r.split("\t")).map(r => Row(r(6), r(25), r(48)))
    val cabeceras = "CODESTABLECIMIENTO CODMES DESTIPUSODIGITAL"
    val camposDF = cabeceras.split(" ").map(fieldName => StructField(fieldName, StringType, nullable = true))
    val schema = StructType(camposDF)

    val tablonDF = sqlContext.createDataFrame(rddTablon, schema)
    return tablonDF.filter((tablonDF("CODESTABLECIMIENTO") isin (esta: _*)) && (!tablonDF("DESTIPUSODIGITAL").equalTo("\\N")) && (tablonDF("CODMES").between(inicio, fin)))
      .groupBy("DESTIPUSODIGITAL").agg(count("DESTIPUSODIGITAL").as("TOTAL"))

  }

  def evolucionCompras(ruta: String, esta: List[String], inicio: String, fin: String): DataFrame = {

    val rddTablon = sc.textFile(ruta).map(r => r.split("\t")).map(r => Row(r(6), r(25), r(48), r(12).toDouble))

    val schema = new StructType()
      .add("CODESTABLECIMIENTO", StringType, true)
      .add("CODMES", StringType, true)
      .add("DESTIPUSODIGITAL", StringType, true)
      .add("MTOTRANSACCION", DoubleType, true)

    val tablonDF = sqlContext.createDataFrame(rddTablon, schema)
    return tablonDF.filter((tablonDF("CODESTABLECIMIENTO") isin (esta: _*)) && (!tablonDF("DESTIPUSODIGITAL").equalTo("\\N")) && (tablonDF("CODMES").between(inicio, fin)))
      .groupBy("CODMES", "DESTIPUSODIGITAL").agg(sum("MTOTRANSACCION").as("MONTO_TOTAL")).orderBy("CODMES","DESTIPUSODIGITAL")

  }

  def montoPromedio(ruta: String, esta: List[String], inicio: String, fin: String): DataFrame = {

    val rddTablon = sc.textFile(ruta).map(r => r.split("\t")).map(r => Row(r(6), r(25), r(48), r(12).toDouble))

    val schema = new StructType()
      .add("CODESTABLECIMIENTO", StringType, true)
      .add("CODMES", StringType, true)
      .add("DESTIPUSODIGITAL", StringType, true)
      .add("MTOTRANSACCION", DoubleType, true)

    val tablonDF = sqlContext.createDataFrame(rddTablon, schema)
    return tablonDF.filter((tablonDF("CODESTABLECIMIENTO") isin (esta: _*)) && (!tablonDF("DESTIPUSODIGITAL").equalTo("\\N")) && (tablonDF("CODMES").between(inicio, fin)))
      .groupBy("CODMES","DESTIPUSODIGITAL").agg(avg("MTOTRANSACCION").as("MONTO_PROMEDIO")).orderBy("CODMES","DESTIPUSODIGITAL")

  }

  def main(args: Array[String]) {


    val x = byClientes("tablon.tsv", List("100070934", "100070905"), "201501", "201512")
    x.show()

    val y = evolucionCompras("tablon.tsv", List("100070934", "100070905"), "201501", "201512")
    y.show()
    
    val z = montoPromedio("tablon.tsv", List("100070934", "100070905"), "201501", "201512")
    z.show()

  }

}