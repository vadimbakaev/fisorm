package vbakaev.app.repositories

import vbakaev.app.models.ItemConstructor

import scala.concurrent.{ExecutionContext, Future}

trait Repository[A] extends CreateRepository[A] with UpdateRepository[A] with ReadRepository[A] with DeleteRepository[A]

trait SafeRepository[A] extends CreateRepository[A] with ReadRepository[A] with ItemConstructor[A] {
  def readOrCreate(email: String)(implicit ec: ExecutionContext): Future[A] = {
    for {
      maybeItem <- read(email)
      item      <- maybeItem.map(Future.successful).getOrElse(create(newItem(email)))
    } yield item
  }
}

trait CreateRepository[A] {
  def create(item: A): Future[A]
}

trait ReadRepository[A] {
  def read(id: String): Future[Option[A]]
}

trait UpdateRepository[A] {
  def update(item: A): Future[Option[A]]
}

trait DeleteRepository[A] {
  def delete(id: String): Future[Option[A]]
}
