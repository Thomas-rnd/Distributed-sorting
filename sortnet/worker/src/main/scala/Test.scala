/*import java.io.{File, PrintWriter}
import scala.util.Random

object Test {
  def main(args: Array[String]): Unit = {
    try {
      // Step 1: Create a folder with data files
      val folderPath = "/home/red/data/input"

      // Step 3: Simulate workers sampling keys
      val numWorkers = 2
      val sampledKeysByWorker = simulateSampling(numWorkers, folderPath)

      // Step 4: Simulate sending sampled keys to the master for sorting
      val keyRanges = MasterServices.createKeyRangeFromSampledKeys(sampledKeysByWorker.values.flatten.toList, numWorkers)

      // Step 5: Display the calculated key ranges
      logger.info("\nKeyRanges as bytes : ")
      keyRanges.foreach(logger.info)
      logger.info("\nKeyRanges as int : ")
      keyRanges.foreach(kr => logger.info(kr.toStringAsIntArray))
      logger.info()

      val partitionPlan = PartitionPlan(List(("1", keyRanges(0)),("2", keyRanges(1))))

      altSimulateSorting(partitionPlan, numWorkers, folderPath)
      

    } catch {
      case e: Exception =>
            logger.error(s"${e.getMessage}", e)
    }
  }

  // Helper method to simulate workers sampling keys
    def simulateSampling(
                          numWorkers: Int,
                          folderPath: String
                        ): Map[Int, List[Key]] = {
      val sampledKeysByWorker = (1 to numWorkers).map { workerId =>
        val sampledKeys =
          WorkerServices.sendSamples(folderPath)
        workerId -> sampledKeys
      }.toMap

      sampledKeysByWorker
    }
    
    def altSimulateSorting(
                          partitionPlan: PartitionPlan,
                          numWorkers: Int,
                          folderPath: String
                        ): List[Partition] = {
      val sortedFiles = WorkerServices.sortFiles(folderPath, partitionPlan)
        
      sortedFiles.foreach(logger.info)

      sortedFiles
    }
}
*/