package com.sksamuel.pulsar4s

import java.util.concurrent.{CompletableFuture, TimeUnit}

import org.apache.pulsar.client.api.{Consumer => JConsumer}
import org.apache.pulsar.client.impl.ConsumerStats

import scala.compat.java8.FutureConverters
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.Try

class Consumer(consumer: JConsumer, val topic: Topic, val subscription: Subscription)
              (implicit context: ExecutionContext) {

  import Message._

  implicit def completableToFuture[T](f: CompletableFuture[T]): Future[T] = FutureConverters.toScala(f)
  implicit def voidCompletableToFuture(f: CompletableFuture[Void]): Future[Unit] = f.map(_ => ())

  def unsubscribe(): Unit = consumer.unsubscribe()
  def unsubscribeAsync: Future[Unit] = consumer.unsubscribeAsync()

  def receive: Message = {
    val msg = consumer.receive()
    Message.fromJava(msg)
  }

  def receiveAsync: Future[Message] = {
    val f = consumer.receiveAsync()
    f.map { msg => Message.fromJava(msg) }
  }

  def receive(duration: Duration): Message = {
    val msg = consumer.receive(duration.toNanos.toInt, TimeUnit.NANOSECONDS)
    Message.fromJava(msg)
  }

  def receiveT[T: MessageReader]: Either[Throwable, Message] = {
    Try {
      val msg = consumer.receive()
      Message.fromJava(msg)
    }.toEither
  }

  def receiveAsyncT[T: MessageReader]: Future[Message] = {
    val f = consumer.receiveAsync()
    f.map { msg => Message.fromJava(msg) }
  }

  def receiveT[T: MessageReader](duration: Duration): Either[Throwable, Message] = {
    Try {
      val msg = consumer.receive(duration.toNanos.toInt, TimeUnit.NANOSECONDS)
      Message.fromJava(msg)
    }.toEither
  }

  def acknowledge(message: Message): Unit = {
    consumer.acknowledge(message)
  }

  def acknowledge(messageId: MessageId): Unit = {
    consumer.acknowledge(messageId)
  }

  def acknowledgeCumulative(message: Message): Unit = {
    consumer.acknowledgeCumulative(message)
  }

  def acknowledgeCumulative(messageId: MessageId): Unit = {
    consumer.acknowledgeCumulative(messageId)
  }

  def acknowledgeAsync(message: Message): Future[Unit] = {
    consumer.acknowledgeAsync(message)
  }

  def acknowledgeAsync(messageId: MessageId): Future[Unit] = {
    consumer.acknowledgeAsync(messageId)
  }

  def acknowledgeCumulativeAsync(message: Message): Future[Unit] = {
    consumer.acknowledgeCumulativeAsync(message)
  }

  def acknowledgeCumulativeAsync(messageId: MessageId): Future[Unit] = {
    consumer.acknowledgeCumulativeAsync(messageId)
  }

  def stats: ConsumerStats = consumer.getStats

  def hasReachedEndOfTopic: Boolean = consumer.hasReachedEndOfTopic

  def redeliverUnacknowledgedMessages(): Unit = consumer.redeliverUnacknowledgedMessages()

  def seek(messageId: MessageId): Unit = consumer.seek(messageId)

  def seekAsync(messageId: MessageId): Future[Unit] = {
    consumer.seekAsync(messageId)
  }

  def close(): Unit = consumer.close()
  def closeAsync: Future[Unit] = consumer.closeAsync()
}
