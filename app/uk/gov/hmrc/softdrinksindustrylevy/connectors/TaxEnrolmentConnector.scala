/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.softdrinksindustrylevy.connectors

import javax.inject.Singleton

import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.softdrinksindustrylevy.config.WSHttp

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxEnrolmentConnector extends ServicesConfig {

  val callbackUrl: String = getConfString("tax-enrolments.callback", "")
  val serviceName: String = getConfString("tax-enrolments.serviceName", "")

  def subscribe(safeId: String, formBundleNumber: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    WSHttp.PUT[JsObject, HttpResponse](subscribeUrl(formBundleNumber), requestBody(safeId, formBundleNumber)) map {
      Result => Result
    } recover {
      case e: UnauthorizedException => {
        handleError(e, formBundleNumber)
      }
      case e: BadRequestException =>  {
        handleError(e, formBundleNumber)
      }
    }
  }

  private def handleError(e: HttpException, formBundleNumber: String): HttpResponse = {
    Logger.error(s"Tax enrolment returned $e for ${subscribeUrl(formBundleNumber)}")
    HttpResponse(e.responseCode, Some(Json.toJson(e.message)))
  }

  private def subscribeUrl(subscriptionId: String) =
    s"${baseUrl("tax-enrolments")}/tax-enrolments/subscriptions/$subscriptionId/subscriber"

  private def requestBody(safeId: String, formBundleNumber: String): JsObject = {
    Json.obj(
      "serviceName" -> serviceName,
      "callback" -> s"$callbackUrl?subscriptionId=$formBundleNumber",
      "etmpId" -> safeId
    )
  }

  private def addHeaders(implicit hc: HeaderCarrier): HeaderCarrier = {
    hc.withExtraHeaders(
      "Environment" -> getConfString("des.environment", "")
    ).copy(authorization = Some(Authorization(s"Bearer ${getConfString("des.token", "")}")))
  }

}