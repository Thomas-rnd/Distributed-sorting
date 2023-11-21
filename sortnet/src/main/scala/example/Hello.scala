package example

object Hello extends Greeting with App {
  //logger.info(greeting)
}

trait Greeting {
  lazy val greeting: String = "hello"
}
