package vbakaev.app.models

trait ItemConstructor[A] {
  def newItem(id: String): A
}
