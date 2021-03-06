package com.flixtech.http

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.ws.StandaloneWSResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class ApiSpec extends FlatSpec with Matchers with MockitoSugar {

  trait Context {
    val url = "http://test.url"
    val pwd = "my_password"
    val account = "my_account"
    val user = "my_user"
    val mockedTransport = mock[Transport]
    val mockedWSResponse = mock[StandaloneWSResponse]
    when(mockedWSResponse.body).thenReturn("http body")
    when(mockedWSResponse.status).thenReturn(200)
    when(mockedTransport.get(any[String], any[Map[String, String]])).thenReturn(Future(mockedWSResponse))
    val api = new HttpApi(url, account, user, pwd, mockedTransport)
  }

  "Api call" should "forward the correct parameter to pop the queue" in new Context {
    val future = api.poll
    verify(mockedTransport).get(url, Map(
      "lang" -> "en",
      "action" -> "popQueueMessagesExtern",
      "msgclass" -> "0",
      "account" -> "my_account",
      "username" -> "my_user",
      "password" -> "my_password",
      "outputformat" -> "json"
    ))
    val body = Await.result(future, 1 second)
    body should be(Some("http body"))
  }

  it should "return none in case response stats is 503" in new Context {
    when(mockedWSResponse.status).thenReturn(503)

    val future = api.poll
    verify(mockedTransport).get(url, Map(
      "lang" -> "en",
      "action" -> "popQueueMessagesExtern",
      "msgclass" -> "0",
      "account" -> "my_account",
      "username" -> "my_user",
      "password" -> "my_password",
      "outputformat" -> "json"
    ))
    val body = Await.result(future, 1 second)
    body should be(None)
  }

  
  it should "forward the correct parameter to ack the queue" in new Context {
    val future = Await.result(api.ack, 5 second)
    verify(mockedTransport).get(url, Map(
      "lang" -> "en",
      "action" -> "ackQueueMessagesExtern",
      "msgclass" -> "0",
      "account" -> "my_account",
      "username" -> "my_user",
      "password" -> "my_password",
      "outputformat" -> "json"
    ))
  }
}
