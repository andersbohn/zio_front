package clover.tsp.front.http

import clover.tsp.front._
import clover.tsp.front.http.Service.TodoItemWithUri
import clover.tsp.front.repository.Repository
import clover.tsp.front.repository.Repository.InMemoryRepository

//import cats.effect._
//import io.circe._
import io.circe.literal._
import io.circe.generic.auto._
//import io.circe.syntax._
//import io.circe.{Encoder, Json}

import org.http4s._
//import org.http4s.circe._
//import org.http4s.dsl.io._
import org.http4s.implicits._

import scalaz.zio.{ZIO, UIO, Ref, DefaultRuntime}
import scalaz.zio.interop.catz._

import org.http4s.dsl.Http4sDsl

class JsonSpec extends HTTPSpec {
  import JsonSpec._
  import JsonSpec.todoService._

  val app = todoService.service.orNotFound


  val dsl: Http4sDsl[TodoTask] = Http4sDsl[TodoTask]
  import dsl._

  describe("Simple Service") {

    it("should create new todo items") {
      val req     = request(Method.POST, "/").withEntity(TodoItemPostForm("Test"))
      runWithEnv(
        check(
          app.run(req),
          Status.Created,
          Some(TodoItemWithUri(1L, "/1", "Test", false, None))))
    }

    it("work with json") {
    //implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]): EntityEncoder[TodoTask, A] = jsonEncoderOf[TodoTask, A]
      
    val body = json"""{"hello":"world"}"""
    
    //for {
    //  req <- request[TodoTask](Method.POST, "/").withBody(body)
    //  //a = req.shit()
    //  res <-  runWithEnv(
    //            check(
    //              app.run(req),
    //              Status.Ok,
    //              Some(TodoItemWithUri(1L, "/1", "Test", false, None))
    //            )
    //          )
    //} yield ()

    }
  }
}

object JsonSpec extends DefaultRuntime {

  val todoService: Service[Repository] = Service[Repository]("")

  val mkEnv: UIO[Repository] =
    for {
      store    <- Ref.make(Map[TodoId, TodoItem]())
      counter  <- Ref.make(0L)
      repo      = InMemoryRepository(store, counter)
      env       = new Repository {
                    override val todoRepository: Repository.Service[Any] = repo
                  }
    } yield env

  def runWithEnv[E, A](task: ZIO[Repository, E, A]): A =
    unsafeRun[E, A](mkEnv.flatMap(env => task.provide(env)))
}
