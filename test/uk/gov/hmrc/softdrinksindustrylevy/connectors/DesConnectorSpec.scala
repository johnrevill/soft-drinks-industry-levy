/*
 * Copyright 2019 HM Revenue & Customs
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

import java.time.LocalDate

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.PropertyChecks
import play.api.libs.json._
import sdil.models.des
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.softdrinksindustrylevy.models
import uk.gov.hmrc.softdrinksindustrylevy.models._
import uk.gov.hmrc.softdrinksindustrylevy.models.gen.{arbActivity, arbAddress, arbContact, arbSubRequest}

class DesConnectorSpecPropertyBased extends FunSuite with PropertyChecks with Matchers {

  import json.internal._

  test("∀ Activity: parse(toJson(x)) = x") {
    forAll { r: Activity =>
      Json.toJson(r).as[Activity] should be (r)
    }
  }

  test("∀ UkAddress: parse(toJson(x)) = x") {
    forAll { r: Address =>
      Json.toJson(r).as[Address] should be (r)
    }
  }

  test("∀ Contact: parse(toJson(x)) = x") {
    forAll { r: Contact =>
      Json.toJson(r).as[Contact] should be (r)
    }
  }

  test("∀ Subscription: parse(toJson(x)) = x") {
    forAll { r: Subscription =>
      Json.toJson(r).as[Subscription] should be (r)
    }
  }

}

class DesConnectorSpecBehavioural extends WiremockSpec with MockitoSugar {
  import play.api.test.Helpers.SERVICE_UNAVAILABLE

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future

  implicit val hc: HeaderCarrier = new HeaderCarrier

  object TestDesConnector extends DesConnector(httpClient, environment.mode, configuration, testPersistence, auditConnector) {
    override val desURL: String = mockServerUrl
  }

  val testF = Json.parse(
    """
      |{
      |  "processingDate": "2018-07-06T17:13:41Z",
      |  "idType": "ZSDL",
      |  "idNumber": "XXSDIL000100603",
      |  "regimeType": "ZSDL",
      |  "financialTransactions": [
      |    {
      |      "chargeType": "Payment on account",
      |      "mainType": "On Account",
      |      "businessPartner": "0100064318",
      |      "contractAccountCategory": "32",
      |      "contractAccount": "000916001324",
      |      "contractObjectType": "ZSDL",
      |      "contractObject": "00000000000560000907",
      |      "sapDocumentNumber": "001840000235",
      |      "sapDocumentNumberItem": "0001",
      |      "mainTransaction": "0060",
      |      "subTransaction": "0100",
      |      "originalAmount": -35000.0,
      |      "outstandingAmount": -35000.0,
      |      "items": [{
      |        "subItem": "000",
      |        "dueDate": "2018-07-06",
      |        "amount": -35000.0,
      |        "paymentReference": "XXSDIL000100603",
      |        "paymentAmount": 35000.0,
      |        "paymentMethod": "PAYMENTS MADE BY CHEQUE",
      |        "paymentLot": "PG006180506",
      |        "paymentLotItem": "000001"
      |      }]
      |    },
      |    {
      |      "chargeType": "SDIL Return Charge",
      |      "mainType": "SDIL Return Charge",
      |      "periodKey": "18C2",
      |      "periodKeyDescription": "April 2018 - June 2018",
      |      "taxPeriodFrom": "2018-05-01",
      |      "taxPeriodTo": "2018-06-30",
      |      "businessPartner": "0100064318",
      |      "contractAccountCategory": "32",
      |      "contractAccount": "000916001324",
      |      "contractObjectType": "ZSDL",
      |      "contractObject": "00000000000560000907",
      |      "sapDocumentNumber": "003320004416",
      |      "sapDocumentNumberItem": "0001",
      |      "chargeReference": "XH004100002944",
      |      "mainTransaction": "4810",
      |      "subTransaction": "1540",
      |      "originalAmount": 5000.0,
      |      "clearedAmount": 5000.0,
      |      "items": [{
      |        "subItem": "000",
      |        "dueDate": "2018-07-30",
      |        "amount": 5000.0,
      |        "clearingDate": "2018-07-06",
      |        "clearingReason": "Cleared by Payment",
      |        "paymentReference": "XXSDIL000100603",
      |        "paymentAmount": 35000.0,
      |        "paymentMethod": "PAYMENTS MADE BY CHEQUE",
      |        "paymentLot": "PG006180506",
      |        "paymentLotItem": "000001",
      |        "clearingSAPDocument": "002540000300"
      |      }]
      |    },
      |    {
      |      "chargeType": "Payment on account",
      |      "mainType": "On Account",
      |      "businessPartner": "0100064318",
      |      "contractAccountCategory": "33",
      |      "contractAccount": "000916001324",
      |      "contractObjectType": "ZSDL",
      |      "contractObject": "00000000000560000907",
      |      "sapDocumentNumber": "001840000235",
      |      "sapDocumentNumberItem": "0001",
      |      "mainTransaction": "0060",
      |      "subTransaction": "0100",
      |      "originalAmount": -35000.0,
      |      "outstandingAmount": -30000.0,
      |      "clearedAmount": -5000.0,
      |      "items": [
      |        {
      |          "subItem": "000",
      |          "dueDate": "2018-07-06",
      |          "amount": -30000.0,
      |          "paymentReference": "XXSDIL000100603",
      |          "paymentAmount": 35000.0,
      |          "paymentMethod": "PAYMENTS MADE BY CHEQUE",
      |          "paymentLot": "PG006180507",
      |          "paymentLotItem": "000002"
      |        },
      |        {
      |          "subItem": "001",
      |          "dueDate": "2018-07-06",
      |          "amount": -5000.0,
      |          "clearingDate": "2018-07-06",
      |          "clearingReason": "Allocated to Charge",
      |          "paymentReference": "XXSDIL000100603",
      |          "paymentAmount": 35000.0,
      |          "paymentMethod": "PAYMENTS MADE BY CHEQUE",
      |          "paymentLot": "PG006180507",
      |          "paymentLotItem": "000002",
      |          "clearingSAPDocument": "002540000300"
      |        }
      |      ]
      |    }
      |  ]
      |}
      |
    """.stripMargin)




  "DesConnector" should {
    "return : None when DES returns 503 for an unknown UTR" in {

      stubFor(get(urlEqualTo("/soft-drinks/subscription/details/utr/11111111119"))
        .willReturn(aResponse().withStatus(SERVICE_UNAVAILABLE)))

      val response: Future[Option[Subscription]] = TestDesConnector.retrieveSubscriptionDetails("utr", "11111111119")
      response.map { x => x mustBe None }
    }

    "return : None financial data when nothing is returned" in {

      stubFor(get(urlEqualTo("/enterprise/financial-data/ZSDL/"))
        .willReturn(aResponse()
          .withStatus(200)
            .withBody(testF.toString())
        ))

      val response: Future[Option[des.FinancialTransactionResponse]] = TestDesConnector.retrieveFinancialData("utr", None)
      response.map { x => x mustBe None }
    }

  }

}
